import java.io.*;

/*
 * HuffmanOutputStream writes out a HuffmanTree representation,
 * the total number of chars that were read, and converting the
 * chars into a binary file
 */

public class HuffmanOutputStream extends BitOutputStream {
	
	/**
	 * HuffmanOutputStream constructor
	 * @param filename
	 * @param tree
	 * @param totalChars
	 */
	public HuffmanOutputStream(String filename, String tree, int totalChars) {

		 super(filename);
		 
		 try {
			 d.writeUTF(tree);
			 d.writeInt(totalChars);
		 }
		 catch (IOException e) {
			 e.printStackTrace();
		 }
	}
	
}
