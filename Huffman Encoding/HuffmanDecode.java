import java.io.*;

/*
 * HuffmanDecode does the decoding process for Huffman coding (only
 * decodes char values 0-127 -- why we used 128 in class to represent
 * non-leaf nodes of the Huffman tree).
 */

public class HuffmanDecode {
	
	private final int CHAR_VAL = 128;
	private HuffmanInputStream inFile; // Read byte bit-by-bit
	private BufferedWriter bWriter; // Write out translated bits
	private HuffmanTree hTree; // Tree from input in postorder
	private int totalChars; // Total number of characters in input file
	
	/**
	 * Implements the huffman decoding algorithm
	 * @param in
	 * @param out
	 */
	public HuffmanDecode(String in, String out){
		try{
			inFile = new HuffmanInputStream(in); // Read input
			totalChars = inFile.getTotalChars();
			//System.out.println("Total chars " + totalChars);
			hTree = new HuffmanTree(inFile.getTree(), (char) CHAR_VAL);
			bWriter = new BufferedWriter(new FileWriter(out));
			
			// Translate encoded file and print translation
			int curr = 0;
			while(curr < totalChars){ // End when all chars are written
				while(!hTree.atLeaf()){ // Read bytes up to leaf
					int bit = inFile.readBit(); // Get bit value
					if(bit == 0) hTree.moveToLeft(); // Left for 0
					else if(bit == 1) hTree.moveToRight(); // Right for 1
				}
				int data = hTree.current();
				bWriter.write(data);
				hTree.moveToRoot();
				curr++;
			}
			bWriter.close(); // Close writer
			//inFile.close(); // Close input
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
//		
//		long startTime = System.nanoTime();
//		
		new HuffmanDecode(args[0], args[1]);
		// terminal:
			// cd folderlocation
			// ls -l
//		
//		long endTime = System.nanoTime();
//		long totalTime = endTime - startTime;
//		 double seconds = totalTime / (double) 1000000000;
//		 seconds = (double) Math.round(seconds * 1000) / 1000;
//		 
//		 File fin = new File(args[0]);
//		 File fout = new File(args[1]);
//		 double bytesIn = fin.length();
//		 double bytesOut = fout.length();
//		 double decompress = bytesOut / bytesIn;
//		 decompress = (double) Math.round(decompress * 1000) / 1000;
////		 double space = (1 - (bytesOut / bytesIn)) * 100;
////		 space = (double) Math.round(space * 100) / 100;
//		 
//		 System.out.println("\nInitial file     : " + bytesIn + " bytes");
//		 System.out.println("De-compressed to : " + bytesOut + " bytes");
//		 System.out.println("\nDe-compression ratio of " + decompress + " in " + seconds + " seconds");
////		 System.out.println("\nSpace savings of " + space + "% in " + seconds + " seconds");
	 } 
}
