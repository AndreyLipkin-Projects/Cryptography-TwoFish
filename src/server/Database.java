package server;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {
	//password should be hashed before insertion
	private  HashMap<String,String> users;//save tuples of (userID,password)
	private  HashMap<String,ArrayList<File>> files;
	private  MessageDigest md;
	public static Database instance = null;
	
	private Database() throws NoSuchAlgorithmException {
		users = new HashMap<>();
		files = new HashMap<>();
		md = MessageDigest.getInstance("SHA-256");
	}
	public void populateDB() {
		this.users.put("miras", new String(md.digest("123456".getBytes())));
		this.files.put("miras", new ArrayList<>());
		this.users.put("rani", new String(md.digest("123456789".getBytes())));
		this.files.put("rani", new ArrayList<>());
		this.users.put("Andrey", new String(md.digest("456123".getBytes())));
		this.files.put("Andrey", new ArrayList<>());
		
	}
	public static Database getInstance() throws NoSuchAlgorithmException {
		if(instance == null)
			return new Database();
		return instance;
	}
	public boolean addUser(String userID,String password) {
		String hashPass = new String(md.digest(password.getBytes()));
		if(users.containsKey(userID)) {
			System.out.println("user exists");
			return false;
		}
		users.put(userID, hashPass);
		files.put(userID, new ArrayList<>());
		return true;
	}
	public boolean removeUser(String userID) {
		if(!users.containsKey(userID)) {
			System.out.println("no such user");
			return false;
		}
		users.remove(userID);
		files.remove(userID);
		return true;
	}
	public boolean checkCredentials(String userID,String password) {
		if(!users.containsKey(userID)) {
			System.out.println("no such user");
			return false;
		}
		String hashPass = new String(md.digest(password.getBytes()));
		String storedPass = users.get(userID);
		return hashPass.equals(storedPass);
	}
	public boolean addFile(String userID,File file) {
		if(!users.containsKey(userID)) {
			System.out.println("no such user");
			return false;
		}
		ArrayList<File> filesList = files.get(userID);
		filesList.add(file);
		files.replace(userID, files.get(userID), filesList);
		return true;
	}
	public File getFile(String userID,String fileName) {
		for (File f : files.get(userID)) 
			if(f.getName().equals(fileName))
				return f;
		return null;
	}
	
	
}
