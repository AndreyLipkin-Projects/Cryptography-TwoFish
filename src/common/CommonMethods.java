package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import common.Utils;

public class CommonMethods {
	public static MessageDigest md =  null;
	// plainText file should be in format of 16 bytes per line all in hex (32 hex
	// characters)
	// key should be 16 bytes
	public static void init() {
		if(md == null) {
			try {
				md = MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	//encrypts the file in place - does not create a new one.
	public static void encryptFile(File plainText, String key) {
		int[] keyArr = Utils.StringTointArray(Utils.toHexString(key));
		BufferedReader reader = null;
		FileWriter myWriter = null;
		try {
			reader = new BufferedReader(new FileReader(plainText));
			
			String st;
			StringBuffer sb = new StringBuffer();
			while ((st = reader.readLine()) != null) {
				// encrypt each line and write it back
				int[] encryptedLine = TwoFish.encrypt(Utils.StringTointArray(st), keyArr);
				String cipherLine = Utils.writeInput(encryptedLine);
				sb.append(cipherLine);
				sb.append("\n");
			}
			
			myWriter = new FileWriter(plainText.getAbsolutePath());
			myWriter.write(sb.toString());
			reader.close();
			myWriter.close();
			System.out.println("Successfully encrypted file.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// cipherText file should be in format of 16 bytes per line all in hex (32 hex
	// characters)
	// key should be 16 bytes
	public static void decryptFile(File cipherText, String key) {
		// this code opens an input file, writes it's contents on an out file (created
		// within)
		int[] keyArr = Utils.StringTointArray(Utils.toHexString(key));
		BufferedReader reader = null;
		FileWriter myWriter = null;
		try {
			reader = new BufferedReader(new FileReader(cipherText));
			
			String st;
			StringBuffer sb = new StringBuffer();
			while ((st = reader.readLine()) != null) {
				// decrypt each line and write it back
				int[] decryptedLineInt = TwoFish.decrypt(Utils.StringTointArray(st), keyArr);
				String decryptedLine = Utils.writeInput(decryptedLineInt);
				sb.append(decryptedLine);
				sb.append("\n");
			}
			
			myWriter = new FileWriter(cipherText.getAbsolutePath());
			myWriter.write(sb.toString());
			reader.close();
			myWriter.close();
			System.out.println("Successfully decrypted file.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void signFile(File fileToSign,RSA rsa) {
		String fileContent = Utils.fileToString(fileToSign);
		String signature = signMsg(fileContent,rsa);
		String newContent = signature+"\n"+fileContent;
		try {
			FileWriter myWriter = new FileWriter(fileToSign.getAbsolutePath());
			myWriter.write(newContent);
			myWriter.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	//rsa should also be initialised with 2048
	public static String signMsg(String msg2sign,RSA rsa) {
		init();
		String digest = new String(md.digest(msg2sign.getBytes()));//create a digest of the file
		return rsa.sign(digest);
	}
	
}
