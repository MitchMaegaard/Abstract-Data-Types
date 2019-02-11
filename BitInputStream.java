import java.io.*;

/*
 * BitInputStream reads bytes bit by bit and translates them into 0's and 1's
 */

public class BitInputStream {
	
	 protected DataInputStream d; // Read data from file
	 protected int bitCount; // Current bit to read
	 protected int byteVal; // Current byte (8 bits) to be converted
	 
	 protected final int BYTE_SIZE = 8;
	 protected int[] bitSeq = new int[BYTE_SIZE];
	 
	 public BitInputStream(String filename) {
		 try {
			 d = new DataInputStream(new FileInputStream(filename));
		 }catch (IOException e) {
			 e.printStackTrace();
		 }
	 }
	 
	 public int readBit(){
		 // 1 byte (8 bits) have been read
		 if(bitCount % BYTE_SIZE == 0){
			 // Reset values
			 byteVal = 0;
			 bitCount = 0;
			 
			 try{
				 // Get next 8 bits from input stream
				 byteVal = d.readUnsignedByte();
				 createBitSeq(byteVal);
				 
			 }catch(Exception e){
				 e.printStackTrace();
			 }
		 }
		 bitCount++;
		 return bitSeq[bitCount - 1];
	 }
	 
	 private void createBitSeq(int bVal){
		 int byteValue = bVal;
		 for(int pos = bitSeq.length-1; pos >= 0; pos--){
			 bitSeq[pos] = byteValue % 2;
			 byteValue /= 2;
		 }
	 }

	 /** POST: Input is closed */
	 public void close(){
		 try{
			 d.close();
		 }catch(IOException e){
			 e.printStackTrace();
		 }
	 }
}
