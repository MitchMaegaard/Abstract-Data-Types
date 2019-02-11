import java.io.*;
import java.util.*;

/*
 * HuffmanEncode does the encoding process for Huffman coding (only
 * encode char values 0-127 -- why we used 128 in class to represent
 * non-leaf nodes of the Huffman tree).
 */

public class HuffmanEncode {
	
	private final int CHAR_VAL = 128; // Default -- only use ASCII 0-127
	private int totalChars = 0; // Number of characters read from file
	private int[] charFreq = new int[CHAR_VAL]; // Frequency of each character
	private String[] charPaths = new String[CHAR_VAL]; // Paths for each character in tree
	private HuffmanTree hTree; // HuffmanTree built from encoding
	private BufferedReader bReader; // Read chars in file
	private HuffmanOutputStream outFile; // Post-encoding file to be sent to output
	
	/** Priority queue helper class with comparable objects */
	private class Item implements Comparable<Object>{
		
		private int priority;
		private Object data;
		
		/** Constructor for class Item */
		private Item(int p, Object d){
			priority = p;
			data = d;
		}
		
		/** Compare priority of current item to next item */
		public int compareTo(Object x){
			// Low number represents HIGH priority
			return priority - ((Item)x).priority;
		}
	}
	
	private PriorityQueue<Item> q = new PriorityQueue<>(CHAR_VAL); // Use priority queue to build tree
	
	public HuffmanEncode(String in, String out){
		try{
			bReader = new BufferedReader(new FileReader(in));
			getFrequencies(); // Find char frequency
			if(totalChars == 0) bReader.close(); // Case for empty file
			
			// Add elements to priority queue
			for(int i = 0; i < charFreq.length; i++){
				if(charFreq[i] != 0)
					q.add(new Item(charFreq[i], new HuffmanTree((char) i)));
			}
			buildHuffmanTree(); // Build tree based on priorities
			buildPaths(); // Traverse tree and build encodings
			outFile = new HuffmanOutputStream(out, hTree.toString(), totalChars);
			// Re-read file for encoding
			bReader = new BufferedReader(new FileReader(in));
			writeEncodedFile();
			outFile.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/** Helper method to get frequency of each char in file */
	private void getFrequencies(){
		try{
			int cVal; // Value of read char
			// -1 signals end of stream
			while((cVal = bReader.read()) != -1){
				charFreq[cVal]++;
				totalChars++;
			}
			bReader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/** Helper method to make Huffman tree */
	private void buildHuffmanTree(){
		while(q.size() != 1){
			Item left = q.poll(); // Remove lowest item (highest priority)
			Item right = q.poll(); // Remove 2nd lowest
			// Create Huffman tree with lowest value on the left
			HuffmanTree merge = new HuffmanTree((HuffmanTree) left.data, (HuffmanTree) right.data, (char) CHAR_VAL);
			int sumPri = left.priority + right.priority; // Get new priority
			q.add(new Item(sumPri, merge)); // Add new item back on to queue
		}
		hTree = (HuffmanTree) q.poll().data;
	}
	
	/** Helper method to build encodings for each character */
	private void buildPaths(){
		Iterator<String> allPaths = hTree.iterator();
		while(allPaths.hasNext()){
			String singlePath = allPaths.next();
			int character = singlePath.charAt(0);
			String currPath = singlePath.substring(1, singlePath.length());
			charPaths[character] = currPath;
		}
	}
	
	private void writeEncodedFile(){
		try{
			int cVal; // Value of read char
			// Check if reader char is valid
			while((cVal = bReader.read()) != -1){
				String path = charPaths[cVal]; // Get path to single char
				int pos = 0;
				int len = path.length();
				while(pos < len){ // Traverse path to char
					outFile.writeBit(path.charAt(pos)); // Convert bit-by-bit
					pos++;
				}
			}
			bReader.close(); // EOF
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * args[0] -- text file to decode
	 * args[1] -- initially blank file that will be compressed
	 * @param args
	 */
	 public static void main(String args[]) {
//		 
//		 long startTime = System.nanoTime();
//		 
		 new HuffmanEncode(args[0], args[1]);
//		 
//		 long endTime   = System.nanoTime();
//		 
//		 double seconds = (endTime - startTime) / (double) 1000000000;
//		 seconds = (double) Math.round(seconds * 1000) / 1000;
//		 
//		 File fin = new File(args[0]);
//		 File fout = new File(args[1]);
//		 double bytesIn = fin.length();
//		 double bytesOut = fout.length();
////		 double compress = bytesIn / bytesOut;
////		 compress = (double) Math.round(compress * 1000) / 1000;
//		 double space = (1 - (bytesOut / bytesIn)) * 100;
//		 space = (double) Math.round(space * 100) / 100;
//		 
//		 System.out.println("\nInitial file  : " + bytesIn + " bytes");
//		 System.out.println("Compressed to : " + bytesOut + " bytes");
////		 System.out.println("\nCompression ratio of " + compress + " in " + seconds + " seconds");
//		 System.out.println("\nSpace savings of " + space + "% in " + seconds + " seconds");
	 }
}
