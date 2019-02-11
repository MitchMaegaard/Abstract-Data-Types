import java.io.*;

/*
 * BitOutputStream reads in chars and convert them into
 * readable types to be decoded by BitInputStream
 */

public class BitOutputStream {
	 //add additional protected variables as needed
	 //do not modify the public methods signatures or add public methods
	 protected DataOutputStream d;
	 protected int bitCount; // how many digits from buffer have been used
	 protected int byteVal; // next set of digits
	 protected final int BYTE_SIZE = 8;
	 
	 public BitOutputStream(String filename) {
		 try {
			 d = new DataOutputStream(new FileOutputStream(filename));
			 bitCount = 0;
			 byteVal = 0;
		 }
		 catch (IOException e) {
			 e.printStackTrace();
		 }
	 }
	 
	 public void writeBit(char bit){
		 if(bit == '1'){
			 int basePow = 7 - bitCount;
			 byteVal += Math.pow(2, basePow);
		 }
		 bitCount++;
		 
		 if(bitCount == BYTE_SIZE){
			 try{
				 d.writeByte(byteVal);
				 
				 bitCount = 0;
				 byteVal = 0;
				 
			 }catch(Exception e){
				 e.printStackTrace();
			 }
		 }
	 }

	 public void close(){
		 try{
			 // In case we have leftover bits!!
			 if(bitCount % BYTE_SIZE != 0){
				 d.writeByte(byteVal);
				 bitCount = 0;
				 byteVal = 0;
				 
			 }
			 d.close();
			 
		 }catch(Exception e){
			 e.printStackTrace();
		 }
	 }
}
