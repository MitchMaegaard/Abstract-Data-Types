import java.io.*;
import java.util.*;

public class DBTable {

	RandomAccessFile rows; //the file that stores the rows in the table
	long free; //head of the free list space for rows
	int numOtherFields;
	int[] otherFieldLengths;
	//add other instance variables as needed
	BTree tree;

	private class Row {
		private int keyField;
		private char[][] otherFields;
		/*
		 Each row consists of unique key and one or more character array fields.
		 
		 Each character array field is a fixed length field (for example 10
		 characters).
		 
		 Each field can have a different length.
		 
		 Fields are padded with null characters so a field with a length of
		 of x characters always uses space for x characters.
		 */
		
		/**
		 * 
		 * @param addr
		 * @throws IOException
		 */
		public Row(long addr) throws IOException{
			rows.seek(addr);
			keyField = rows.readInt();
			
			otherFields = new char[otherFieldLengths.length][];
			for(int i = 0; i < otherFieldLengths.length; i++)
				otherFields[i] = new char[otherFieldLengths[i]];
			
			int i = 0;
			int j = 0;
			while(i != otherFields.length){
				while(j != otherFields[i].length){
					otherFields[i][j] = rows.readChar();
					j++;
				}
				j = 0;
				i++;
			}
		}
		
		/**
		 * 
		 * @param key
		 * @param other
		 * @throws IOException
		 */
		public Row(int key, char[][] other) throws IOException{
			keyField = key;
			//otherFields = other;
			otherFields = new char[otherFieldLengths.length][];
			for(int i = 0; i < otherFieldLengths.length; i++)
				otherFields[i] = new char[otherFieldLengths[i]];
			
			int i = 0;
			int j = 0;
			while(i != otherFields.length && i != otherFieldLengths.length){
				while(j != otherFields[i].length && j != otherFieldLengths[i]){
					otherFields[i][j] = other[i][j]; // this is where I messed up...
					j++;
				}
				j = 0;
				i++;
			}
		}
		
		/**
		 * 
		 * @param addr
		 * @throws IOException
		 */
		public void writeRow(long addr) throws IOException{
			rows.seek(addr);
			rows.writeInt(keyField);
			
            int i = 0; 
            int j = 0;
            while(i != otherFields.length) {
                while(j != otherFields[i].length) {
                    rows.writeChar(otherFields[i][j]);
                    j++;
                }
                j = 0;
                i++;
            }
		}
	}

	/**
	 * Constructor to create a new DBTable. A B+Tree must be created for the
	 * key field in the table. If a file with name filename exists, the file
	 * should be deleted before the new file is created.
	 * 
	 * @param filename -- name of file used to store the table
	 * @param fL -- lengths of otherFields; fL.length indicates how many other
	 * fields are a part of the row
	 * @param bsize -- block size, used to calculate order of the B+tree
	 * @throws IOException
	 */
	public DBTable(String filename, int[] fL, int bsize) throws IOException {
		File file = new File(filename);
		rows = new RandomAccessFile(file, "rw");
		tree = new BTree(filename + "BTreeFile", bsize); // Unique filename
		
		if(file.exists()) rows.setLength(0); // Reset files
		
		numOtherFields = fL.length;
		otherFieldLengths= fL;
		
		// numOtherFields
		rows.seek(0);
		rows.writeInt(numOtherFields);
		
		// Length of other fields
		for(int i = 0; i < numOtherFields; i++)
			rows.writeInt(otherFieldLengths[i]);
		
		rows.writeLong(0); // Free list
	}

	/** Constructor to open a DBTable that already exists */
	public DBTable(String filename) throws IOException{
		File file = new File(filename);
		rows = new RandomAccessFile(file, "rw");
		tree = new BTree(filename + "BTreeFile");
		
		// Update DBTable data
		rows.seek(0);
		numOtherFields = rows.readInt();
		otherFieldLengths = new int[numOtherFields];
		
		for(int i = 0; i < numOtherFields; i++){
			int otherFields = rows.readInt();
			otherFieldLengths[i] = otherFields;
		}
		
		free = rows.readLong();
	}

	/**
	 * Uses the B+tree to determine if a row with the key exists.
	 * If a row with the key is not in the table, the row is added
	 * to DBTable, the key is added into the B+tree, and insert() returns true.
	 * Otherwise, the row is not added and insert() returns false.
	 * 
	 * PRE: the length of each row is fields matches the expected length
	 * 
	 * @param key -- key value to insert into tree
	 * @param fields -- row values to insert into tree
	 * @return true if the row/key is added; false otherwise
	 * @throws IOException
	 */
	public boolean insert(int key, char[][] fields) throws IOException {
		
		boolean added = false;
		long addRow;
		
		if(free != 0) addRow = free;
		else addRow = rows.length();
		
		if(tree.insert(key, addRow)){
			Row insertedRow = new Row(key, fields);
			insertedRow.writeRow(getFree());
			added = true;
		}
		
		return added;
	}

	/**
	 * If a row with the key is in the table it is removed and true is returned,
	 * otherwise false is returned. If the row is deleted, the key must be
	 * deleted from the B+Tree. This method must make use of the B+Tree to
	 * determine if a row with the key exists.
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public boolean remove(int key) throws IOException {
		
		long removeAddr = tree.remove(key);
		
		if(removeAddr == 0) // not in table
			return false;
		
		addFree(removeAddr); // found in table, free up address
		return true;
	}

	/**
	 * Should utilize the equality search in B+Tree. The string values in the list should
	 * not include null characters. If a row with the key is not found, return an empty list.
	 * 
	 * @param key -- value used to search for a row
	 * @return list of the other fields in the row, if a row with the key is found
	 * @throws IOException
	 */
	public LinkedList<String> search(int key) throws IOException{
		
		long addr = tree.search(key);
		LinkedList<String> rowContent = new LinkedList<>();
		
		if(addr == 0)
			return rowContent;
		
		StringBuilder tempString = new StringBuilder();
		Row tempRow = new Row(addr);
		int i = 0;
		int j = 0;
		while(i != otherFieldLengths.length){
			while(j != tempRow.otherFields[i].length && tempRow.otherFields[i][j] != '\0'){
				tempString.append(tempRow.otherFields[i][j]);
				j++;
			}
			rowContent.add(tempString.toString());
			tempString.setLength(0);
			j = 0;
			i++;
		}
		return rowContent;
	}

	/**
	 * Should utilize rangeSearch() in B+Tree. For each row with a key that is in
	 * the range: low to high (inclusive), a list of the fields (including the key)
	 * in the row is added to the list returned by the call. If there are no rows
	 * with a key in the range, return an empty list.
	 * 
	 * PRE: low <= high
	 * 
	 * @param low -- minimum key value
	 * @param high -- maximum key value
	 * @return Linked list of linked lists -- list of fields in key value range
	 * @throws IOException 
	 */
	public LinkedList<LinkedList<String>> rangeSearch(int low, int high) throws IOException {
		
		LinkedList<Long> addresses = tree.rangeSearch(low, high);
		
		LinkedList<LinkedList<String>> contents = new LinkedList<>();
		
		if(addresses.size() == 0)
			return contents;
		
		StringBuilder tempString = new StringBuilder();
		
		while(addresses.size() != 0){
			LinkedList<String> rowContent = new LinkedList<>();
			Row tempRow = new Row(addresses.remove());
			rowContent.add(Integer.toString(tempRow.keyField));
			
			int i = 0;
			int j = 0;
			
			while(i != tempRow.otherFields.length){
				while(j != tempRow.otherFields[i].length && tempRow.otherFields[i][j] != '\0'){
					tempString.append(tempRow.otherFields[i][j]);
					j++;
				}
				rowContent.add(tempString.toString());
				tempString.setLength(0);
				j = 0;
				i++;
			}
			contents.add(rowContent);
		}
		return contents;
	}
	
	private void addFree(long addr) throws IOException{
		rows.seek(addr);
		rows.writeLong(free);
		free = addr;
	}
	
	private long getFree() throws IOException{
		if(free == 0) return rows.length();
		
		long curr = free;
		rows.seek(free);
		free = rows.readLong();
		return curr;
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void print() throws IOException {
		// One row per line
		
		// Use B+ tree to get in order
		// Search to leftmost leaf in B+ tree, walk across all leaves
		// Print out as we come across keys
        System.out.println("-----------------");
        System.out.println("-----------------");
        tree.print();     
        System.out.println("-----------------");
        System.out.println("-----------------");
        System.out.println("DBTable");
        System.out.println("Current free: " + free);
        System.out.println("Number of Otherfields: " + numOtherFields);
        for(int temp: otherFieldLengths) {
            System.out.println("Other field: " + temp);
        }
	}
	
	/** Close the DBTable. The table should not be used after it's closed. */
	public void close() throws IOException {
		tree.close();
		int pos = (numOtherFields+1)*4;
		rows.seek(pos);
		rows.writeLong(free);
		rows.close();
	}
	
	/**
	 * Test method for DBTable, which uses the structure of BTree
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[]args) throws IOException{
		int[] contents = {10, 20};
		int blocksize = 60; // order 5
        DBTable table = new DBTable("test1", contents, blocksize);
        char[][] testChar = new char[2][];
        testChar[0] = new char[10];
        testChar[1] = new char[20];
        
        for(int i = 0; i < testChar.length; i++)
        	for(int j = 0; j < testChar[i].length; j++)
        		testChar[i][j] = 'a';
        
        for(int i = 0; i <= 50; i+=3) table.insert(i, testChar);
        for(int i = 2; i <= 50; i+=3) table.insert(i, testChar);
        
        for(int i = 0; i <= 50; i+=3) table.search(i);
        for(int i = 2; i <= 50; i+=3) table.search(i);
        for(int i = 1; i <= 50; i+=3) table.search(i);
        
//        LinkedList<LinkedList<String>> range = table.rangeSearch(0, 50);
//        System.out.println("Number of values in our range search: " + range.size() + "\n");
        
        table.print();
        
        for(int i = 1; i <= 50; i += 2) table.remove(i);
        
        table.print();
    }
}
