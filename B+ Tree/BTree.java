import java.io.*;
import java.util.*;

public class BTree {

	RandomAccessFile f;
	int order; // 'M'
	int blockSize; // M/12
	long root;
	long free;
	// Add instance variables as needed.
	static final int LEAF = -1;
	static final int NONLEAF = 1;
	
	boolean changeMin;

	private class BTreeNode {
		private int count; // Number of keys in a node
		private int[] keys;
		private long[] children;
		private long address; // The address of the node in the file
		
		/** Constructor for a new node */
		private BTreeNode(long addr, int cnt, int[] k, long[] child){
			address = addr;
			count = cnt;
			keys = k;
			children = child;
		}
		
		/** This is where we read nodes */
		private BTreeNode(long addr) throws IOException{
			address = addr;
			
			if(addr != 0){
				f.seek(addr);
				count = f.readInt();
				
				// Count specifies num keys -- max is the order-1
				keys = new int[order-1];
				for(int k = 0; k < order - 1; k++)
					keys[k] = f.readInt();
				
				// Max children is order
				children = new long[order];
				for(int c = 0; c < order; c++)
					children[c] = f.readLong();
			}
		}
		
		/** This is where we write nodes */
		private void writeBTreeNode() throws IOException{
			f.seek(address);
			f.writeInt(count);
			
			for(int k = 0; k < keys.length; k++)
				f.writeInt(keys[k]);
			
			for(int c = 0; c < children.length; c++)
				f.writeLong(children[c]);
		}
	}

	/**
	 * Makes a new B+ tree.
	 * @param filename
	 * @param bsize -- block size. Used to calculate the order of the B+ tree
	 */
	public BTree(String filename, int bsize) throws IOException {
		// All B+Tree nodes will use bsize bytes
		File file = new File(filename);
		f = new RandomAccessFile(file, "rw");
		if(file.exists()) f.setLength(0);
		blockSize = bsize;
		order = Math.floorDiv(blockSize, 12);
		root = 0;
		free = 0;
		changeMin = false;
		f.seek(16);
		f.writeInt(bsize);
	}

	/** Open an existing B+ tree */
	public BTree(String filename) throws IOException {
		File file = new File(filename);
		f = new RandomAccessFile(file,"rw");
		f.seek(0);
		root = f.readLong(); // Start at first element in access file
		free = f.readLong(); // 
		blockSize = f.readInt();
		order = Math.floorDiv(blockSize, 12);
		changeMin = false;
	}


	/**
	 * If key is not a duplicate, add key to B+tree.
	 * @param key -- new value to insert to tree
	 * @param addr -- (in DBTable) is address of the row that contains the key
	 * @return true if key is added, false if key is a duplicate
	 */
	public boolean insert(int key, long addr) throws IOException{
		BTreeNode node;
		BTreeNode newNode;
		
		// Case for initialization
		if(root == 0){
			// Node: address, count, keys[], children[]
			// Make our array to copy over 1 larger than the old
			node = new BTreeNode(getFree(), LEAF, new int[order-1], new long[order]);
			node.keys[0] = key;
			node.children[0] = addr;
			root = node.address;
			node.writeBTreeNode();
			return true;
		}
		
		Stack<BTreeNode> path = searchPath(key); // Get a path of nodes to the key value
		boolean split = true;
		long loc = -1; // Initialize location and value to -1
		int val = -1;
		
		node = path.pop(); // First node in stack
		
		// Check if the value we're trying to insert already exists
		if(checkNode(node, key))
			return false;
		
		if(hasRoom(node)){ // If there's room in the current node
			insertKey(node, key, addr, LEAF); // insert key into node
			node.writeBTreeNode(); // write to file
			split = false; // don't have to split because there was room
		}else{ // No room in current node!
			newNode = splitLeaf(node, key, addr); // Split values between node and newNode
			val = newNode.keys[0]; // update val
			// Already wrote node to file in splitLeaf()
			newNode.writeBTreeNode(); // write newnode to file
			loc = newNode.address; // update address
			split = true;
		}
		
		while(!path.empty() && split){ // more nodes in the path and we have to split
			node = path.pop(); // get next node
			if(hasRoom(node)){ // check for room
				insertKey(node, val, loc, NONLEAF); // Insert val and loc into our nonleaf node
				node.writeBTreeNode();
				split = false; // don't have to split
			}else{
				newNode = new BTreeNode(getFree(), LEAF, new int[order-1], new long[order]);
				// let val be middle value of values in the node
				// put values less than val and matching locations in node
				// put values greater than val and matching locations in newNode
				val = splitNonLeaf(node, newNode, val, loc);
				// Already wrote node and newNode to file in splitNonLeaf()
				loc = newNode.address;
				split = true;
			}
		}
		
		if(split){ // Then the root was split
			newNode = new BTreeNode(getFree(), NONLEAF, new int[order-1], new long[order]);
			// Insert address of old root, val, and loc into newNode
			newNode.children[0] = node.address;
			newNode.keys[0] = val;
			newNode.children[1] = loc;
			newNode.writeBTreeNode();
			root = newNode.address; // Update address of root
		}
		return true;
	}
	
	/**
	 * Find the path from the root to the value we want to insert
	 * @param key
	 * @return
	 * @throws IOException
	 */
	private Stack<BTreeNode> searchPath(int key) throws IOException{
		Stack<BTreeNode> nodes = new Stack<>();
		return searchPath(key, new BTreeNode(root), nodes);
	}
	
	/** Helper method for searchPath() */
	private Stack<BTreeNode> searchPath(int k, BTreeNode checkNode, Stack<BTreeNode> path) throws IOException{
		path.push(checkNode);
		
		if(isLeaf(checkNode)) return path;
		
		//int len = Math.abs(checkNode.count);
		int len = checkNode.count;
		int pos = len;
		
		// Find correct position in node
		while(pos-1 != -1 && k < checkNode.keys[pos-1])
			pos--;
		
		// Check where to look for value
		if(pos-1 == -1) // Ran off start of keys array
			checkNode = new BTreeNode(checkNode.children[0]);
		else if(pos == len) // Ran off end of keys array
			checkNode = new BTreeNode(checkNode.children[len]);
		else // Found value between start and end of keys array
			checkNode = new BTreeNode(checkNode.children[pos]);
		
		return searchPath(k, checkNode, path);
	}
	
	/**
	 * Insert a key into a node that has room for more keys
	 * @param n -- node to insert value into
	 * @param k -- key value to insert
	 * @param addr -- address of node to insert value into
	 */
	private void insertKey(BTreeNode n, int k, long addr, int nodeType){
		int len = n.count;
		int pos = len;
		
		if(nodeType == -1){ // Inserting into a leaf
			pos = Math.abs(pos);
			while((pos-1 != -1) && (k < n.keys[pos-1])){
				n.keys[pos] = n.keys[pos-1];
				n.children[pos] = n.children[pos-1];
				pos--;
			}
			n.keys[pos] = k;
			n.children[pos] = addr;
			n.count--;
		}
		// Case for a nonLeaf
		else if(nodeType == 1){
			// Look through until we get to the value
			while((pos-1 != -1) && (k < n.keys[pos-1])){
				n.keys[pos] = n.keys[pos-1];
				n.children[pos+1] = n.children[pos];
				pos--;
			}
			
			// Now find where to insert the value, same indexing as search
			if(pos-1 == -1){
				n.keys[0] = k;
				n.children[1] = addr;
			}else if(pos == len){
				n.keys[len] = k;
				n.children[len+1] = addr;
			}else{
				n.keys[pos] = k;
				n.children[pos+1] = addr;
			}
			n.count++;
		}
	}
	
	/** Splitting a leaf */
	private BTreeNode splitLeaf(BTreeNode left, int key, long addr) throws IOException{
		
        BTreeNode right = new BTreeNode(getFree(), LEAF, new int[order-1], new long[order]);
        boolean insertedKey = false;
        int[] tempKeys = new int[order]; //Holds all the keys
        long[] tempChildren = new long[order+1]; //Holds all the children
        int keysLength = Math.abs(left.count);
        int endCase = keysLength-1;
        
        // Insert into temp array and insert key into right position
        for(int i = 0, j = 0; i < keysLength; i++, j++) {
            if(!insertedKey && key < left.keys[i]) {
            	insertedKey = true;
                tempKeys[j] = key;
                tempChildren[j] = addr;
                j++; // Move pos up for when we skip to the bottom assignment
            } else if(i == endCase) {
                //Last value of left node was smaller than key, put at end of temp array
                tempKeys[j] = left.keys[i];
                tempChildren[j] = left.children[i];
                tempKeys[keysLength] = key;
                tempChildren[keysLength] = addr;
            }
            // Fill in with remaining values
            tempKeys[j] = left.keys[i];
            tempChildren[j] = left.children[i];
        }
        
        // Fill in keys and children for right side
        for(int rightSide = tempKeys.length/2, i = 0; rightSide < tempKeys.length; rightSide++, i++) {
            right.keys[i] = tempKeys[rightSide];
            right.children[i] = tempChildren[rightSide];
        }
        
        // Reset left side
//        for(int i = 0; i < left.keys.length; i++)
//        	left.keys[i] = 0;
        
        // Re-assign left side with our new values
        for(int leftSide = 0; leftSide < tempKeys.length/2; leftSide++) {
            left.keys[leftSide] = tempKeys[leftSide];
            left.children[leftSide] = tempChildren[leftSide];
        }
        
        right.count = (left.count/2) - 1; // Add one more to right
        left.count = (tempKeys.length/2)*-1;
        
        right.children[keysLength] = left.children[keysLength]; // Take left's previous link 
        left.children[keysLength] = right.address; // Link left to right node
        
        left.writeBTreeNode();
        return right;
	}
	
	/** Create a new leaf and split the values from the current node into the current one and new node */
    private int splitNonLeaf(BTreeNode left, BTreeNode right, int key, long addr) throws IOException{
        int middle = 0; //default val
        int[] tempKeys = new int[order];
        long[] tempChildren = new long[order+1];
        boolean insertedKey = false;
        int keyLength = left.count;
        int endCase = keyLength-1;
        
        for(int i = 0, j = 0; i < keyLength; i++, j++) {
            if(!insertedKey && key < left.keys[i]) {
                insertedKey = true;
                tempKeys[j] = key;
                tempChildren[j+1] = addr;
                tempKeys[j+1] = left.keys[i];
                tempChildren[j] = left.children[i];
                j++;      
            }else if(i == endCase){
                if(!insertedKey) {
                    tempChildren[j+1] = left.children[j+1]; 
                    tempChildren[j+2] = addr;
                    tempKeys[j+1] = key;
                }else
                    tempChildren[j+1] = left.children[j];
                
                tempKeys[j] = left.keys[i];
                tempChildren[j] = left.children[i];
            }else{
                tempKeys[j] = left.keys[i];
                tempChildren[j] = left.children[i];
            }
        }
        
        // Reset left side keys and children
//        for(int i = 0; i < left.keys.length; i++){
//        	left.keys[i] = 0;
//        	left.children[i] = 0;
//        }
//        left.children[left.keys.length] = 0;
        
        left.count = tempKeys.length/2;
        if(order % 2 == 0) right.count = left.count-1;
        else right.count = left.count;
        
        for(int i = 0; i < left.count; i++) {
            left.keys[i] = tempKeys[i];
            left.children[i] = tempChildren[i];
        }
        left.children[tempKeys.length/2] = tempChildren[tempKeys.length/2];
        
        for(int i = (tempKeys.length/2)+1, j = 0; i < tempKeys.length; i++, j++) {
            right.keys[j] = tempKeys[i];
            right.children[j] = tempChildren[i];
        }
        right.children[right.count] = tempChildren[tempKeys.length];
        
        left.writeBTreeNode();
        right.writeBTreeNode();
        
        middle = tempKeys[tempKeys.length/2];
        
        return middle;
    }

	/**
	 * If key is in B+tree, remove the key.
	 * @param key -- value to be removed from the tree
	 * @return address of the row if key is in B+tree, 0 if not
	 */
	public long remove(int key) throws IOException{
		// Not implemented until HW 8!
		if(root == 0) return 0;
		
		BTreeNode node;
		BTreeNode child;
		Stack<BTreeNode> path = searchPath(key);
		boolean tooSmall = false;
		long keyAddr = 0;
		
		node = path.pop();
		
		if(checkNode(node, key)){ // if k is in node
			keyAddr = removeLeafKey(key, node); // remove it
			// check if root is leaf
			if(node.address == root){
				// root is empty
				if(node.count == 0){
					addFree(root); // add the node to the free list
					root = 0;
					return keyAddr;
				}
				node.writeBTreeNode(); // update root
			}else{
				// need to borrow or combine
				if(minKeys(node)) tooSmall = true;
				
//				BTreeNode parent = path.pop();
//				for(int i = 0; i < parent.count && key >= parent.keys[i]; i++){
//					if(key == parent.keys[i]) parent.keys[i] = node.keys[0];
//				}
//				node.writeBTreeNode();
//				parent.writeBTreeNode();
//				return keyAddr;
				if(changeMin){
					BTreeNode parent = path.pop();
					for(int i = 0; i < parent.count && key >= parent.keys[i]; i++)
						if(key == parent.keys[i])
							parent.keys[i] = node.keys[0];
					changeMin = false;
					parent.writeBTreeNode();
				}
				node.writeBTreeNode();
				return keyAddr;
			}
		}
		else
			return keyAddr;
		
		while(!path.empty() && tooSmall){
			child = node;
			node = path.pop();
			
			if(changeMin){
				for(int i = 0; i < node.count && key >= node.keys[i]; i++)
					if(key == node.keys[i])
						node.keys[i] = child.keys[0];
				changeMin = false;
			}
			
			// check neighbors of the child
			int borrowPos = canBorrow(node, child);
			// if borrowing is possible
			if(borrowPos != -1){
				// shift vals between children and adjust the key in node that is b/n the nodes involved in borrowing
				//borrow(borrowPos, node, child);
				borrow(node, child, borrowPos);
				tooSmall = false;
			}else{
				// combine child with a neighbor and remove the key in node b/n the nodes involved in combining
				// if num keys in node is greater than or equal to the minimum required
				//combine(child, node);
				int combinePos = combinePartner(node, child);
				combine(node, child, combinePos);
				if(!minKeys(node)) tooSmall = false;
			}
		}
		
		if(tooSmall){ // Means the root is now empty
			// set root to leftmost child of the empty root and free the space used by the old root
			root = node.children[0];
			addFree(node.address);
		}
		
		return keyAddr;
 	}
	
	/** Take out a key from a leaf node */
	private long removeLeafKey(int key, BTreeNode node){
		node.count++; // free up space in node
		int max = Math.abs(node.count);
		long keyAddr;
		int pos = 0;
		
		while(node.keys[pos] != key)
			pos++;
		
		if(pos == 0 && root != node.address)
			changeMin = true;
		
		keyAddr = node.children[pos];
		
		while(pos < max){
			node.keys[pos] = node.keys[pos+1];
			node.children[pos] = node.children[pos+1];
			pos++;
		}
		return keyAddr;
	}
	
	/** Check where we can borrow space from */
	private int canBorrow(BTreeNode parent, BTreeNode keyChild) throws IOException{
		int borrow = -1;
		
		long childAddr = keyChild.address;
		
		int pos = 0;
		int childLimit = Math.abs(parent.count);
		
		while(parent.children[pos] != childAddr)
			pos++;
		
		if(pos == 0){
			BTreeNode right = new BTreeNode(parent.children[1]);
			if(hasExtraKeys(right)) borrow = 1;
		}else if(pos == childLimit){
			BTreeNode left = new BTreeNode(parent.children[childLimit - 1]);
			if(hasExtraKeys(left)) borrow = childLimit - 1;
		}else{
			BTreeNode left = new BTreeNode(parent.children[pos-1]);
			if(hasExtraKeys(left)) borrow = pos - 1;
			else{
				BTreeNode right = new BTreeNode(parent.children[pos+1]);
				if(hasExtraKeys(right)) borrow = pos + 1;
			}
		}
		return borrow;
	}
	
	/** Helper method to decide whether to borrow from either a leaf or nonleaf */
	private void borrow(BTreeNode parent, BTreeNode child, int pos) throws IOException{
		BTreeNode borrowPartner = new BTreeNode(parent.children[pos]);
//		boolean isBorrowLeft = false;
//		
//		// borrow partner is on the left
//		if(borrowPartner.keys[0] < child.keys[0]) isBorrowLeft = true;
		
		// leaf borrowing
		if(isLeaf(child)){
			// borrow from the left
			if(borrowPartner.keys[0] < child.keys[0])
				borrowFromLeaf(borrowPartner, child, parent, true);
			else
				borrowFromLeaf(borrowPartner, child, parent, false);
			
			child.count--;
			borrowPartner.count++;
		}else{ // nonleaf borrow
			// borrow from the left
			if(borrowPartner.keys[0] < child.keys[0])
				borrowFromNonLeaf(borrowPartner, child, parent, true, pos);
			else
				borrowFromNonLeaf(borrowPartner, child, parent, false, pos);
			
			child.count++;
			borrowPartner.count--;
		}
		
		child.writeBTreeNode();
		parent.writeBTreeNode();
		borrowPartner.writeBTreeNode();
	}
	
	/**  */
	private int combinePartner(BTreeNode parent, BTreeNode keyChild) throws IOException{
		long keyChildAddr = keyChild.address;
		int pos = 0;
		
		while(keyChildAddr != parent.children[pos])
			pos++;
		
		if(pos == 0)
			return 1;
		
		else
			return pos - 1;
	}
	
	/**  */
	private void combine(BTreeNode parent, BTreeNode child, int partnerAddr) throws IOException{
		BTreeNode partner = new BTreeNode(parent.children[partnerAddr]);
		int parentKeyPos;
		
		if(child.address == parent.children[0])
			parentKeyPos = 0;
		else
			parentKeyPos = partnerAddr;
		
		if(isLeaf(child))
			combineLeaf(partner, parent, child, parentKeyPos);
		else
			combineNonLeaf(partner, parent, child, parentKeyPos);
		
		while(parentKeyPos != parent.count && parentKeyPos+2 < parent.children.length){
			parent.keys[parentKeyPos] = parent.keys[parentKeyPos+1];
			parent.children[parentKeyPos+1] = parent.children[parentKeyPos+2];
			parentKeyPos++;
		}
        //parent.children[parentKeyPos] = parent.children[parentKeyPos+1];
        
        parent.count--;
        parent.writeBTreeNode();
	}
	
	/**  */
	private void combineLeaf(BTreeNode partner, BTreeNode parent, BTreeNode child, int parentKeyPos) throws IOException{
		int newKeyTotal = child.count + partner.count; 

		int posChild = Math.abs(child.count);
		int posPartner = Math.abs(partner.count);

		int posTotal = posChild + posPartner;
		if(child.address == parent.children[0]) {
			for(int i = posChild, j = 0; i < posTotal; i++, j++) {
				child.keys[i] = partner.keys[j];
				child.children[i] = partner.children[j]; 
			}

			child.count = newKeyTotal;
			child.children[child.keys.length] = partner.children[partner.keys.length]; //swap next reference 
			child.writeBTreeNode();
			addFree(partner.address);
		} else {
			for(int i = posPartner, j = 0; i < posTotal; i++, j++) {
				partner.keys[i] = child.keys[j];
				partner.children[i] = child.children[j]; 
			} 

			partner.count = newKeyTotal;
			partner.children[partner.keys.length] = child.children[child.keys.length];
			partner.writeBTreeNode();
			addFree(child.address);
		}
	}
	
	/**  */
    private void combineNonLeaf(BTreeNode partner, BTreeNode parent, BTreeNode child, int parentKeyPos) throws IOException {
        int newKeyTotal = child.count + partner.count + 1; 
        int posChild = child.count;
        int posPartner = partner.count;
        
        if(child.address == parent.children[0]) {
            child.keys[posChild] = parent.keys[parentKeyPos];
            posChild++;
            for(int j = 0; posChild < newKeyTotal; posChild++, j++) {
                child.keys[posChild] = partner.keys[j];
                child.children[posChild] = partner.children[j];
            }
            
            child.children[newKeyTotal] = partner.children[posPartner];
            child.count = newKeyTotal;
            child.writeBTreeNode();
            addFree(partner.address);
        } else {
            partner.keys[posPartner] = parent.keys[parentKeyPos];
            posPartner++;
            for(int j = 0; posPartner < newKeyTotal; posPartner++, j++) {
                partner.keys[posPartner] = child.keys[j];
                partner.children[posPartner] = child.children[j];
            }
            
            partner.children[newKeyTotal] = child.children[posChild];
            partner.count = newKeyTotal;
            partner.writeBTreeNode();
            addFree(child.address);
        }
    }
	
    /** Helper method to grab values from a leaf node */
	private void borrowFromLeaf(BTreeNode borrowPartner, BTreeNode child, BTreeNode parent, boolean left){
		int maxCKeys = Math.abs(child.count); // current info
		int maxBKeys = Math.abs(borrowPartner.count); // borrow info
		int parentKeys = parent.count; // parent info
		
		if(left){
			for(int i = maxCKeys; i > 0; i--){
				child.keys[i] = child.keys[i-1];
				child.children[i] = child.children[i-1];
			}
			child.keys[0] = borrowPartner.keys[maxBKeys-1];
			child.children[0] = borrowPartner.children[maxBKeys-1];
			
			// Change parent key value
			for(int i = 0; i < parentKeys; i++){
				if(child.keys[0] < parent.keys[i]){
					parent.keys[i] = child.keys[0];
					break;
				}
			}
		}else{
			int changeParentKeysPos;
			// Change the parent key value for the borrowed partner key value
			for(changeParentKeysPos = 0; changeParentKeysPos < parentKeys; changeParentKeysPos++){
				if(parent.keys[changeParentKeysPos] == borrowPartner.keys[0]){
					parent.keys[changeParentKeysPos] = borrowPartner.keys[1];
					break;
				}
			}
			
			child.keys[maxCKeys] = borrowPartner.keys[0];
			child.children[maxCKeys] = borrowPartner.children[0];
			
			for(int i = 0; i < maxBKeys-1; i++){ // TODO
				borrowPartner.keys[i] = borrowPartner.keys[i+1];
				borrowPartner.children[i] = borrowPartner.children[i+1];
			}
			
			//parent.keys[changeParentKeysPos - 1] = child.keys[0];
		}
	}
	
	/** Helper method to grab values from nonLeaf nodes */
	private void borrowFromNonLeaf(BTreeNode borrowPartner, BTreeNode child, BTreeNode parent, boolean left, int borrowPos){
		int maxCKeys = Math.abs(child.count);
		int maxBKeys = Math.abs(borrowPartner.count);
		
		if(left){ // case for borrowing from the left
			
			for(int i = maxCKeys; i > 0; i--){
				child.keys[i] = child.keys[i-1];
				child.children[i] = child.children[i-1];
			}
			
			child.keys[0] = parent.keys[borrowPos];
			child.children[0] = borrowPartner.children[maxBKeys];
			parent.keys[borrowPos] = borrowPartner.keys[maxBKeys - 1];
		}else{ // case for the right
			child.keys[maxCKeys] = parent.keys[borrowPos - 1];
			child.children[maxCKeys+1] = borrowPartner.children[0];
			parent.keys[borrowPos-1] = borrowPartner.keys[0];
			
			for(int i = 0; i < maxBKeys - 1; i++){
				borrowPartner.keys[i] = borrowPartner.keys[i+1];
				borrowPartner.children[i] = borrowPartner.children[i+1];
			}
			
			borrowPartner.children[maxBKeys-1] = borrowPartner.children[maxBKeys];
		}
	}
	
	/**
	 * Equality search for the value k
	 * @param k
	 * @return if key is found, address of the row with the key is returned
	 * (otherwise 0)
	 */
	public long search(int k) throws IOException{
		return search(root, k);
	}
	
	private long search(long r, int k) throws IOException{
		BTreeNode x = new BTreeNode(r);
		
		if(x.address == 0) return 0;
		int len; // middle of keys array
		
		if(isLeaf(x)){
			int i = 0;
			len = Math.abs(x.count);
			while(i < len){
				if(k == x.keys[i])
					return x.children[i];
				i++;
			}
			return 0;
		}
		len = x.count;
		int pos = len;
		
		while((pos - 1 != -1) && (k < x.keys[pos-1]))
			pos--;
		
		if(pos == -1)
			x = new BTreeNode(x.children[0]);
		else if(pos == len)
			x = new BTreeNode(x.children[len]);
		else
			x = new BTreeNode(x.children[pos]);
		
		return search(x.address, k);
	}

	/**
	 * PRE: low <= high
	 * @param low -- low end of key range
	 * @param high -- high end of key range
	 * @return a list of row addresses for all keys in the range low to high (inclusive)
	 * @return empty list when no keys are in the range
	 */
	public LinkedList<Long> rangeSearch(int low, int high) throws IOException{
		LinkedList<Long> list = new LinkedList<>();
		return rangeSearch(new BTreeNode(root), low, high, list);
	}
	
	/** Helper method for our range search. Makes use of search() for low and high values */
	private LinkedList<Long> rangeSearch(BTreeNode node, int low, int high, LinkedList<Long> link) throws IOException{
		
//		LinkedList<Long> addrs = new LinkedList<>();
//		
//		long min = search(r, low);
//		long max = search(r, high);
//		
//		if(min == 0 || max == 0) return null; // Check if min == max?
//		
//		addrs.add(min);
//		
//		BTreeNode curr = new BTreeNode(min);
//		BTreeNode stop = new BTreeNode(max);
//		
//		// This also stops before the end of the list (last child is 0)
//		while(curr.children[order-1] != stop.children[order-1]){
//			BTreeNode temp = new BTreeNode(curr.children[order-1]);
//			addrs.add(curr.children[order-1]);
//			curr = temp;
//		}
//		
//		return addrs;
		
		if(node.address == 0) return link;
		
		int len;
		if(isLeaf(node)){
			int i = 0;
			len = Math.abs(node.count);
			
			while(i < len){
				if(high < node.keys[i])
					break;
				if(low <= node.keys[i])
					link.add(node.children[i]);
				i++;
			}
			
			if(i >= len)
				checkNext(high, new BTreeNode(node.children[node.keys.length]), link);
			
			return link;
		}
		
		len = node.count;
		int pos = len;
		
		while(pos - 1 != -1 && (low < node.keys[pos-1]))
			pos--;
		
		if(pos-1 == -1)
			node = new BTreeNode(node.children[0]);
		else if(pos == len)
			node = new BTreeNode(node.children[len]);
		else
			node = new BTreeNode(node.children[pos]);
		
		return rangeSearch(node, low, high, link);
	}
	
	private void checkNext(int high, BTreeNode checkNode, LinkedList<Long> link) throws IOException{
		if(checkNode.address == 0) return;
		
		int i = 0;
		int len = Math.abs(checkNode.count);
		
		while(i < len){
			if(high < checkNode.keys[i]) break;
			link.add(checkNode.children[i]);
			i++;
		}
		
		checkNext(high, new BTreeNode(checkNode.children[checkNode.keys.length]), link);
	}
	
	/** Check node to see if it's a leaf */
	private boolean isLeaf(BTreeNode node){
		return (node.count < 0);
	}
	
	private boolean checkNode(BTreeNode node, int k) throws IOException{
		
		if(node.address == 0) return false;
		
		for(int i = 0; i < Math.abs(node.count); i++)
			if(node.keys[i] == k) return true;
		return false;
	}
	
	/** Check node to see if we can add more keys */
	private boolean hasRoom(BTreeNode bNode){
		return (Math.abs(bNode.count) < (order - 1));
	}
	
	/** Check node to see if we have the minimum number of keys */
	private boolean minKeys(BTreeNode node){
		// Be sure to 
		// Root node can have min of 1 leaf
		if(node.address == root)
			return node.count < 1;
		// Other nodes have min of ceiling(order/2)-1
		return(Math.abs(node.count) < Math.ceil(order/2.0) - 1);
	}
	
	private boolean hasExtraKeys(BTreeNode node){
		return Math.abs(node.count) > Math.ceil(order/2.0) - 1;
	}
	
	/** Free list; potentially extend length of list. */
	private long getFree() throws IOException{
		// No removal method for HW7, we can just return length for now
		if(free == 0) return f.length();
		long tmp = free;
		f.seek(free);
		free = f.readLong();
		return tmp;
	}
	
	/**
	 * When a node is removed from the tree, the space for that node
	 * must be added to the free list.
	 * @param addr -- address of the node we're adding to the free list
	 * @return
	 * @throws IOException
	 */
	private void addFree(long addr) throws IOException{
		// Add to front of list
		f.seek(addr);
		f.writeLong(free);
		free = addr;
		//return addr;
	}
	
	/**
	 * Print the B+tree to standard output, one node per line.
	 * @throws IOException
	 */
	public void print() throws IOException{
        System.out.println("PRINTING B+TREE:\n");
        System.out.println("Block Size: " + blockSize + "\t(Order " + order + ")");
        System.out.println("Root: " + root + "\t\t\tFree: " + free + "\n");
        System.out.println("Address\t\t||\tCount\t||\t\tKeys\t\t\t||\t\tChildren");
        System.out.println("-----------------------------------------------------------"
        		+ "-------------------------------------------------------");
        Stack<BTreeNode> nodes = new Stack<>();
        nodes.push(new BTreeNode(root));
        while(!nodes.empty()) {
        	BTreeNode temp = nodes.pop();
        	if(temp.address != 0) {

        		System.out.print("\t" + temp.address + "\t||\t" + temp.count + "\t||\t");
        		for(int i = 0; i < Math.abs(temp.keys.length); i++)
        			System.out.print(temp.keys[i] + "\t");
        		
        		System.out.print("||\t");
        		for(int i = 0; i < Math.abs(temp.children.length); i++){
        			System.out.print(temp.children[i] + "\t");
        			
        			if(!isLeaf(temp) && temp.children[i] != 0)
        				nodes.push(new BTreeNode(temp.children[i]));
        		}
        		System.out.println();
            }
        }
    }

	/** Close the B+tree. Tree should not be accessed after close() is called */
	public void close() throws IOException {
		// Before close, update root and free!
		try{
			f.seek(0);
			f.writeLong(root);
			f.writeLong(free);
			f.close();

		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
