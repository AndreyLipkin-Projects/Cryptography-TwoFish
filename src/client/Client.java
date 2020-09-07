package client;

//java imports
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//local imports
import common.ClientMessages;
import common.CommonMethods;
import common.RSA;
import common.ServerResponse;
import common.Utils;
import server.Server;

public class Client{
	private String userID;
	private static Server server = new Server();
	private String key;
	
	private static File fileFromServer;
	private static boolean serverAck;
	
	
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getUserID() {
		return this.userID;
	}
	/*
	 * private static String byteArray2Hex(final byte[] hash) { Formatter formatter
	 * = new Formatter(); for (byte b : hash) { formatter.format("%02x", b); }
	 * formatter.close(); return formatter.toString(); }
	 */

	/*
	 * public String signImage(RSA rsa, String imagePath) throws IOException,
	 * NoSuchAlgorithmException {
	 * 
	 * File file = new File(imagePath);
	 * 
	 * FileInputStream imageStream = new FileInputStream(imagePath); byte[]
	 * imageInBytes = new byte[imageStream.available()];
	 * imageStream.read(imageInBytes);
	 * 
	 * MessageDigest md = MessageDigest.getInstance("SHA-1"); String resultOfHash =
	 * byteArray2Hex(md.digest(imageInBytes));
	 * 
	 * // String stringOfImage = new String(imageInBytes); return
	 * rsa.signature(resultOfHash);
	 * 
	 * }
	 */
	public static void main(String[] args) {
		System.out.println(false & false);
		System.out.println(false & true);
		System.out.println(true & false);
		System.out.println(true & true);
		
		
		
		RSA rsa = new RSA(2048);// in practice 2048 is more than enough
		// on sender side:
		String msg = "twoFish is nice";
		MessageDigest md;
		String digest = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
			digest = new String(md.digest(msg.getBytes()));// create a digest of the encKey
			System.out.println(digest);
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String sign = rsa.sign(digest);// sign the digest
		String encKey = rsa.encrypt(msg);// encrypt the message to be sent.
		System.out.println(encKey);
		// ==========================SEND TO RECEIVER(encKey,sign,RSA)======================================
		// on receiver side:
		String decKey = rsa.decrypt(encKey);// decrypt the received message
		System.out.println(decKey);

		// create a digest of the decrypted message
		MessageDigest md1;
		String digest1 = null;
		try {
			md1 = MessageDigest.getInstance("SHA-256");
			digest1 = new String(md1.digest(decKey.getBytes()));// create a digest of the encKey
			System.out.println(digest1);
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// verify signature by comparing the digest of decrypted message and the
		// signature
		System.out.println(rsa.verifySignature(sign, digest1));

		
		
		
		
		
		
		
		File in = null;
		try {
			/*
			// path should be user provided for the WAV file
			String wav = Utils.convertWAVtoHEX(new File("C:\\Users\\Pc\\Desktop\\WAV Sample 3 seconds.wav"));
			// System.out.println(wav);
			System.out.println("length = " + wav.length());
			String[] output = Utils.Inputsplitter(wav, 32);
			// -------------------------------------------------------------------------------------------//
			// Fill the input file in the correct line length for further processing
			FileWriter writer = new FileWriter("C:\\Users\\Pc\\Desktop\\in.txt");
			int len = output.length;
			for (int i = 0; i < len; i++) {
				writer.write(output[i] + '\n');
				writer.flush();
			}
			writer.close();
			*/

			in = new File("C:\\Users\\Pc\\Desktop\\in.txt");
			BufferedReader reader = new BufferedReader(new FileReader(in));
			
			String st;
			StringBuffer sb = new StringBuffer();
			while ((st = reader.readLine()) != null) {
				//replace every line with the first 32 characters of it
				String line = st.substring(0, 32);
				sb.append(line);
				sb.append("\n");
			}
			System.out.println("Successfully wrote to the file.");
			
			
			FileWriter myWriter = new FileWriter(in.getAbsolutePath());
			myWriter.write(sb.toString());
			reader.close();
			myWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		CommonMethods.encryptFile(in, "30302030302037442030302030302020");
		
		CommonMethods.decryptFile(in, "30302030302037442030302030302020");
	}
	
	public boolean login(String userID,String password) {
		String credentials[] = {userID,password};
		sendToServer(credentials, ClientMessages.login);
		return serverAck;
	}
	public boolean register(String userID,String password) {
		String credentials[] = {userID,password};
		sendToServer(credentials, ClientMessages.register);
		return serverAck;
	}
	public boolean unregister(String userID) {
		sendToServer(userID, ClientMessages.unregister);
		return serverAck;
	}
	public boolean exchangeKeys(String key) {
		RSA rsa = new RSA(2048);// in practice 2048 is more than enough
		CommonMethods.init();
		String digest = new String(CommonMethods.md.digest(key.getBytes()));// create a digest of the encKey
		
		String sign = rsa.sign(digest);// sign the digest
		
		String encKey = rsa.encrypt(key);// encrypt the message to be sent.
		Object res[] = {encKey,sign,rsa};
		sendToServer(res, ClientMessages.exchange_key);
		if(serverAck)
			this.key = key;
		return serverAck;
	}
	public boolean storeFile(File fileToStore,String userID) {
		
		CommonMethods.encryptFile(fileToStore, this.key);
		String fileName = fileToStore.getName().split("\\.")[0]+"_enc."+fileToStore.getName().split("\\.")[1];
		
		try {
			Files.copy(Paths.get(fileToStore.getAbsolutePath()),
					Paths.get("C:\\Users\\Pc\\Desktop\\crypto project\\client side\\"+fileName),
					StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		RSA rsa = new RSA(2048);
		CommonMethods.signFile(fileToStore,rsa);
		Object[] res = {fileToStore,userID,rsa};
		sendToServer(res, ClientMessages.store_file);
		if(serverAck)
			this.key = null;
		return serverAck;
	}
	public File getFile(String userID,String fileName) {
		RSA rsa = new RSA(2048);
		Object[] res = {userID,fileName,rsa};
		sendToServer(res, ClientMessages.request_file);
		//decrypt and verify signature
		if(fileFromServer != null) {
			String signature = Utils.extractSignature(fileFromServer);
			String contents = Utils.fileToString(fileFromServer);
			CommonMethods.decryptFile(fileFromServer, this.key);
			CommonMethods.init();
//			String contents = Utils.fileToString(fileFromServer);
			String digest = new String(CommonMethods.md.digest(contents.getBytes()));
			boolean verifySign = rsa.verifySignature(signature, digest);
			if (verifySign)
				this.key = null;
			else//signature does not match 
				fileFromServer = null;
		}
		return fileFromServer;
	}
	
	
	

	public void receiveFromServer(Object message, ServerResponse type) {
		if(type == ServerResponse.request_file_Result) {//return file
			fileFromServer = (File) message;
//			return f;
		}else {//return ack
			serverAck = (boolean) message;
//			return ack;
		}
	}

	public void sendToServer(Object message, ClientMessages type) {
		server.receiveFromClient(message, type);
	}
}
