package server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;

import client.Client;
import common.ClientMessages;
import common.CommonMethods;
import common.RSA;
import common.ServerResponse;
import common.Utils;

public class Server {
	private Database db;
	private static Client client = new Client();
	private String key;
	
	public Server() {
		try {
			this.db = Database.getInstance();
			this.db.populateDB();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void login(String userID,String password) {
		boolean loginResult = db.checkCredentials(userID, password);
		sendToClient(loginResult, ServerResponse.login_Result);
	}
	private void register(String userID,String password) {
		boolean registerResult = db.addUser(userID, password);
		sendToClient(registerResult,ServerResponse.register_Result);
	}
	private void unregister(String userID) {
		boolean unregisterResult = db.removeUser(userID);
		sendToClient(unregisterResult, ServerResponse.unregister_Result);
	}
	
	/*
	 * to store a file the user must first exchange keys
	 */
	private void exchangeKey(String encKey,String signature,RSA rsa) {
		//must decrypt key using rsa and validate authenticity
		String decKey = rsa.decrypt(encKey);// decrypt the received key
		System.out.println(decKey);
		//if md is null, initialise it
		CommonMethods.init();
		// create a digest of the decKey
		String digest = new String(CommonMethods.md.digest(decKey.getBytes()));
		System.out.println(digest);
		//verify signature by comparing the digest of decrypted message and the signature
		boolean exchgKeyResult = rsa.verifySignature(signature, digest);
		if(exchgKeyResult)
			this.key = decKey;
		sendToClient(exchgKeyResult, ServerResponse.exchange_Key);
	}
	private void storeFile(File encryptedFile,RSA rsa,String userID) {
		//copy encrypted file to server directory
		String fileName = encryptedFile.getName().split("\\.")[0]+"_enc."+encryptedFile.getName().split("\\.")[1];
		try {
			Files.copy(Paths.get(encryptedFile.getAbsolutePath()),
					Paths.get("C:\\Users\\Pc\\Desktop\\crypto project\\server side\\"+fileName),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		//need to "destroy" the key for this session, i.e. assigning it to null
		String signature = Utils.extractSignature(encryptedFile);
		String contents = Utils.fileToString(encryptedFile);
		CommonMethods.decryptFile(encryptedFile, this.key);
		CommonMethods.init();
//		String contents = Utils.fileToString(encryptedFile);
		String digest = new String(CommonMethods.md.digest(contents.getBytes()));
		boolean verifySign = rsa.verifySignature(signature, digest);
		boolean res = false;
		if(verifySign) {
			res = db.addFile(userID, encryptedFile);
			//if signature is verified, destroy the key
			this.key = null;
		}
		fileName = encryptedFile.getName().split("\\.")[0]+"_dec."+encryptedFile.getName().split("\\.")[1];
		try {
			Files.copy(Paths.get(encryptedFile.getAbsolutePath()),
					Paths.get("C:\\Users\\Pc\\Desktop\\crypto project\\server side\\"+fileName),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendToClient(res, ServerResponse.store_file_Result);
	}
	private void getFile(String userID,String filename,RSA rsa) {
		File f = db.getFile(userID, filename);
		if(f != null) {
			//encrypt file using key exchanged earlier and send it to client.
			CommonMethods.encryptFile(f, this.key);
			//sign the file before sending it
			CommonMethods.signFile(f,rsa);
			//need to "destroy" the key for this session, i.e. assigning it to null
			this.key = null;
		}
		sendToClient(f, ServerResponse.request_file_Result);
	}
	
	public void receiveFromClient(Object message,ClientMessages type) {
		RSA rsa;
		Object[] args;
		switch(type){
			case login:
				String[] loginArgs = (String[]) message;
				login(loginArgs[0], loginArgs[1]);
				break;
			case store_file:
				args = (Object[]) message;
				File f = (File) args[0];
				String userID = (String) args[1];
				rsa = (RSA) args[2];
				storeFile(f, rsa, userID);
				break;
			case register:
				String[] registerArgs = (String[]) message;
				register(registerArgs[0], registerArgs[1]);
				break;
			case unregister:
				String userIDToRemove = (String) message;
				unregister(userIDToRemove);
				break;
			case exchange_key:
				args = (Object[]) message;
				String encKey = (String) args[0];
				String sign = (String) args[1];
				rsa = (RSA) args[2];
				exchangeKey(encKey, sign, rsa);
				break;
			//--------------------------//	
			case request_file:
				args = (Object[]) message;
				String requestingUser = (String) args[0];
				String fileName = (String) args[1];
				rsa = (RSA) args[2];
				getFile(requestingUser, fileName, rsa);
				break;
		}
	}
	public void sendToClient(Object message,ServerResponse type) {
		client.receiveFromServer(message, type);
	}
}
