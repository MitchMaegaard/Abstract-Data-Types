import java.io.*;

/*
 * HuffmanInputStream reads the encoded file and converts
 * the file back into readable text
 */

public class HuffmanInputStream extends BitInputStream {
	
	 private String tree; // Postorder representation from input file
	 private int totalChars; // Number of chars in file
	 
	 /**
	  * HuffmanInputStream constructor
	  * @param filename
	  */
	 public HuffmanInputStream(String filename) {
		 super(filename);
		 try {
			 tree = d.readUTF();
			 totalChars = d.readInt();
			 //System.out.println(totalChars); // Debugging
		 }catch (IOException e) {
			 e.printStackTrace();
		 } 
	 }
	 
	 /** HuffmanTree representation */
	 public String getTree() {
		 return tree;
	 }
	 
	 /** Number of chars converted */
	 public int getTotalChars() {
		 return totalChars;
	 }
}
