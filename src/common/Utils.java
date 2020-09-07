package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author sala
 */
public class Utils {
    public static String writeInput(int[] plainText) {
    	StringBuilder sb = new StringBuilder();
        for(int i = 0; i <= 3; i++) {
            for(byte b  : TwoFish.asBytes(plainText[i])) {
            	sb.append(String.format("%02X", b));
            }
        }
        return sb.toString();
    }

    public static void printInternal(int[] whitened) {
        for(int whitenedEntry : whitened) {
            System.out.print(String.format("%02X ", whitenedEntry));
        }
        System.out.println();
    }
	public static int[] StringTointArray(String st) {
		int[] res = new int[st.length() / 2];
		int c = 0;
		for (int i = 0; i < st.length() - 1; i += 2) {
			int x = Integer.parseInt(st.substring(i, i + 2), 16);
			res[c++] = x;
		}
		int[] p = new int[4];
		for (int i = 0; i < p.length; i++) {
			p[i] = res[4 * i] + 256 * res[4 * i + 1] + (res[4 * i + 2] << 16) + (res[4 * i + 3] << 24);
		}
		return p;
	}
	public static String toHexString(String in) {
		String out;
		StringBuffer sb = new StringBuffer();
		char ch[] = in.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			String hexString = Integer.toHexString(ch[i]);
			sb.append(hexString);
		}
		out = sb.toString();
		if(out.length() < 32) {//pad with zeros
			char[] c = new char[32-out.length()];
			Arrays.fill(c, '0');
			String s = new String(c);
			out = s + out;
		}
		return out;
	}
	
	public static String[] Inputsplitter(String input, int length)
	{
		int len=input.length();
		int zeroPadCheck=len%32;//Save how many zeros we need to zero-pad the last input line
		if(zeroPadCheck!=0) len=(input.length()/32)+1;
		else len=input.length()/32;
		
	    String[] output = new String[len];
	    int pos = 0;
	    for(int i=0;i<len;i++)
	    {	
	    	//Zero-padding the last line in an uneven Modulo32 input
	    	if((i==len-1)&&(zeroPadCheck!=0)) {				
	    		output[i]=input.substring(pos, pos+zeroPadCheck);
		    	char[] repeat = new char[zeroPadCheck];
		    	Arrays.fill(repeat, '0');
		    	output[i] += new String(repeat);
	    		break;
	    	}
	        output[i] = input.substring(pos, pos+length);
	        pos = pos + length;
	        
	    }
	    return output;
	}
	
	
	public static String convertWAVtoHEX(File wavInput) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(wavInput));
		int value;
//		int count = 0;
		while ((value = reader.read()) != -1) {
			sb.append(String.format("%04x", value).toUpperCase());
//			count += 4;
//			if(count == 32) {
//				sb.append("\n");
//				count = 0;
//			}
//			for (byte b : TwoFish.asBytes(value)) {
//				sb.append(String.format("%02X", b));
//			}
		}
		reader.close();
		return sb.toString();
	}
	
	
	public static String fileToString(File f) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String st;
			while ((st = br.readLine()) != null) {
				sb.append(st);
				sb.append("\n");
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	
	
	
	public static String extractSignature(File f) {
		BufferedReader reader = null;
		FileWriter myWriter = null;
		String signature = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			
			String st;
			StringBuffer sb = new StringBuffer();
			//extract the first line, i.e. the signature
			if((st = reader.readLine()) != null)
				signature = st;
			//read the rest of the file, 
			while ((st = reader.readLine()) != null) {
				sb.append(st);
				sb.append("\n");
			}
			
			myWriter = new FileWriter(f.getAbsolutePath());
			myWriter.write(sb.toString());
			reader.close();
			myWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return signature;
	}
}
