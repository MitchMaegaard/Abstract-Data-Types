import java.io.*;

/*
 * AVLTree.java
 * 
 * Author: Mitch Maegaard
 * 
 * Class: CS 340 ( Homework 6 )
 * 
 * Professor Gendreau
 * 
 * Due April 4, 2018
 */

/* Implements an ALV tree of integers stored in a random access file.
 * Duplicates are recorded by a count field associated with the integers.
 */
public class AVLTree {

	final int CREATE = 0; // When we want to make a new file
	final int REUSE = 1; // When we want to reuse a file

	private RandomAccessFile f; // Access anywhere in file (by address)
	long root; // Address of the root node in the file
	long free; // Address in the file of the first node in the free list

	long minRight; // Address of node that will replace the removed value

	private static final int ALLOWED_IMBALANCE = 1; // Limit on subtree height

	private class Node{
		private long left; // 8 bytes
		private int data; // 4 bytes
		private int count; // 4 bytes
		private long right; // 8 bytes
		private int height; // 4 bytes

		/** Constructor for a new node */
		private Node(long L, int d, long R){
			left = L;
			data = d;
			right = R;
			height = 0;
			count = 1;
		}

		/** Constructor for a node that exists and is stored in the file
		 * File structure is addr; data; count; height; left; right
		 * @param addr
		 * @throws IOException
		 */
		private Node(long addr) throws IOException{
			// THIS IS WHERE WE READ NODES
			f.seek(addr);
			data = f.readInt();
			count = f.readInt();
			height = f.readInt();
			left = f.readLong();
			right = f.readLong();
		}

		/** Writes the node to the file at location addr */
		private void writeNode(long addr) throws IOException{
			// THIS IS WHERE WE WRITE NODES
			f.seek(addr);
			f.writeInt(data);
			f.writeInt(count);
			f.writeInt(height);
			f.writeLong(left);
			f.writeLong(right);
		}
	}

	public AVLTree(String fname, int mode) throws IOException{

		File file = new File(fname); // Create new file, then check for existence
		// Delete previous file if mode is CREATE
		if(mode == CREATE && file.exists()) file.delete();

		f = new RandomAccessFile(file,"rw"); // 'rw' to allow read and write
		// Create a new empty file when mode is CREATE
		if(mode == CREATE){
			f.setLength(0);
			root = 0;
			free = 0;
			f.writeLong(root);
			f.writeLong(free);
		}else{ // Use an existing file if it exists when mode is REUSE
			f.seek(0); // Go to address 0
			root = f.readLong(); // 1st long indicates root
			free = f.readLong(); // 2nd long indicates head of free list
		}
	}

	/**
	 * Get height of tree from address
	 * @param addr -- height of tree at given address
	 * @return
	 * @throws IOException
	 */
	private int height(long addr) throws IOException{
		if(addr == 0) return -1;
		f.seek(addr + 8); // 8 bytes over for allocation of 'data' and 'count'
		return f.readInt();
	}

	/**
	 * Insert d into the tree.
	 * If d is in the tree increment the count field associated with d
	 * @param d
	 * @throws IOException
	 */
	public void insert(int d) throws IOException{
		root = insert(root, d);
	}

	/** Helper method for insert */
	private long insert(long r, int d) throws IOException{
		if(r == 0){ // Setup for a new tree with the first node
			Node x = new Node(0, d, 0);
			long addr = getFree();
			x.writeNode(addr);
			return addr;
		}

		Node x = new Node(r); // Create node from address
		if(d < x.data) x.left = insert(x.left, d);
		else if(d > x.data) x.right = insert(x.right, d);
		else x.count++;

		x.writeNode(r);
		return balance(r);
	}

	/**
	 * Assume r is either balanced or within one of being balanced
	 * @param r
	 * @return
	 * @throws IOException
	 */
	private long balance(long r) throws IOException{
		
		if(r == 0) return r;

		f.seek(r+12); // Go to left address
		long left = f.readLong();
		long right = f.readLong();

		// Check if left subtree is too tall
		if(height(left) - height(right) > ALLOWED_IMBALANCE){
			f.seek(left+12);
			long leftsLeft = f.readLong();
			long leftsRight = f.readLong();
			// Check for single or double rotations
			if(height(leftsLeft) >= height(leftsRight)) r = rotateRight(r);
			else r = leftRightRotation(r);
		}
		// Check if right subtree is too tall
		else if(height(right) - height(left) > ALLOWED_IMBALANCE){
			f.seek(right+12);
			long rightsLeft = f.readLong();
			long rightsRight = f.readLong();
			// Check for single or double rotations
			if(height(rightsRight) >= height(rightsLeft)) r = rotateLeft(r);
			else r = rightLeftRotation(r);
		}

		f.seek(r+12); // Go to same r address as before check for imbalance
		long rLeft = f.readLong();
		long rRight = f.readLong();
		// Update height by 1 to max of left and right subtrees
		int rHeight = Math.max(height(rLeft), height(rRight)) + 1;
		f.seek(r+8); // Go to height and replace with new height
		f.writeInt(rHeight);
		return r;
	}

	/**
	 * Single rotation with left child. Update heights and return new root.
	 * @param r -- root of the subtree to be rotated
	 * @return left -- updated root after rotating to the right
	 * @throws IOException
	 */
	private long rotateRight(long r) throws IOException{
		// Get r.left and r.left.right
		f.seek(r+12);
		long left = f.readLong();
		f.seek(left+20);
		long leftsRight = f.readLong();

		// Swap r.left and r.left.right
		f.seek(r+12);
		f.writeLong(leftsRight); // r.left becomes r.left.right
		f.seek(left+20);
		f.writeLong(r); // r.left.right becomes r.left

		// Update heights of rotated subtrees
		f.seek(r+12); // Go to same r address as before rotation
		long rLeft = f.readLong();
		long rRight = f.readLong();
		int rHeight = Math.max(height(rLeft), height(rRight)) + 1;
		f.seek(r+8); // Replace r.height with the rotated height
		f.writeInt(rHeight);

		f.seek(left+12);
		long leftsLeft = f.readLong();
		f.seek(r+8); // Get new root's height
		int rNewHeight = f.readInt();
		int leftHeight = Math.max(height(leftsLeft), rNewHeight) + 1;
		f.seek(left+8);
		f.writeInt(leftHeight);
		// r.left is our new subtree root
		return left;
	}

	/**
	 * Single rotation with the right child. Update heights and return new root.
	 * @param r -- root of subtree to be rotated
	 * @return right -- updated root after rotating to the left
	 * @throws IOException
	 */
	private long rotateLeft(long r) throws IOException{
		// Get r.right and r.right.left
		f.seek(r+20);
		long right = f.readLong();
		f.seek(right+12);
		long rightsLeft = f.readLong();

		// Swap r.right and r.right.left
		f.seek(r+20);
		f.writeLong(rightsLeft); // r.right becomes r.right.left
		f.seek(right+12);
		f.writeLong(r); // r.right.left becomes r.right

		// Update heights of rotated subtrees
		f.seek(r+12);
		long rLeft = f.readLong();
		long rRight = f.readLong();
		int rHeight = Math.max(height(rLeft), height(rRight)) + 1;
		f.seek(r+8);
		f.writeInt(rHeight);

		f.seek(right+20);
		long rightsRight = f.readLong();
		f.seek(r+8);
		int rNewHeight = f.readInt();
		int rightHeight = Math.max(height(rightsRight), rNewHeight) + 1;
		f.seek(right+8);
		f.writeInt(rightHeight);
		// r.right is our new subtree root
		return right;
	}

	/**
	 * Double rotation performed by first doing a left rotation on the
	 * left subtree, followed by a right rotation of the root. Update
	 * heights and return new root.
	 * @param r -- root of subtree to be rotated
	 * @return updated root after a left/right rotation
	 * @throws IOException
	 */
	private long leftRightRotation(long r) throws IOException{
		// Get r.left subtree
		f.seek(r+12);
		long left = f.readLong();
		// Do a left rotation on the left subtree and update
		long leftRotation = rotateLeft(left);
		f.seek(r+12);
		f.writeLong(leftRotation);
		return rotateRight(r); // Right rotation on the root
	}

	/**
	 * Double rotation performed by first doing a right rotation on the
	 * right subtree, followed by a left rotation of the root. Update
	 * heights and return new root.
	 * @param r -- root of subtree to be rotated
	 * @return updated root after a right/left rotation
	 * @throws IOException
	 */
	private long rightLeftRotation(long r) throws IOException{
		// Get r.right subtree
		f.seek(r+20);
		long right = f.readLong();
		// Do a right rotation on the right subtree and update
		long rightRotation = rotateRight(right);
		f.seek(r+20);
		f.writeLong(rightRotation);
		return rotateLeft(r); // Left rotation on the root
	}

	/**
	 * If d is in the tree return the value
	 * @param d
	 * @return
	 * @throws IOException
	 */
	public int find(int d) throws IOException{
		return find(root, d);
	}

	/** Helper method */
	private int find(long r, int d) throws IOException{
		if(r == 0) return 0;

		Node x = new Node(r);

		if(d < x.data) return find(x.left, d); // If less, search left
		if(d > x.data) return find(x.right, d); // If greater, search right
		return x.count; // Otherwise, return count
	}

	/**
	 * Remove one copy of d from the tree. If the copy is the last copy,
	 * remove d from the tree. If d is not in the tree, the method has
	 * no effect.
	 * @param d
	 * @throws IOException
	 */
	public void removeOne(int d) throws IOException{
		root = removeOne(root, d);
	}

	private long removeOne(long r, int d) throws IOException{
		if(r == 0) return 0; // Case for not found

		Node x = new Node(r);

		if(d < x.data) x.left = removeOne(x.left, d);
		else if(d > x.data) x.right = removeOne(x.right, d);
		else x.count--;

		// Add node to the free list if we've removed ALL copies!
		if(x.count == 0){
			addFree(r); // Add to free list
			if(x.left != 0 && x.right != 0){ // Two children
				// Replace root
				f.seek(r+12);
				long left = f.readLong();
				long right = f.readLong();
				long newRight = findMin(right);
				f.seek(minRight+12); // Go to address of small in right subtree
				f.writeLong(left); // Left is same as before
				f.writeLong(newRight); // Right is updated
				r = minRight; // Swap address
				return balance(r); // Balance tree
			}else{ // 1 or no children
				long left = x.left;
				long right = x.right;
				r = (left != 0) ? left : right; // (true/false) ? true condition : false condition
				return balance(r);
			}
		}
		x.writeNode(r);
		return balance(r);
	}

	/**
	 * Remove d from the tree. If d is not in the tree, the method has
	 * no effect.
	 * @param d
	 * @throws IOException
	 */
	public void removeAll(int d) throws IOException{
		root = removeAll(root, d);
	}

	private long removeAll(long r, int d) throws IOException{
		if(r == 0) return 0; // Case for not found

		Node x = new Node(r);

		if(d < x.data) x.left = removeAll(x.left, d);
		else if(d > x.data) x.right = removeAll(x.right, d);
		else x.count = 0;

		if(x.count == 0){
			addFree(r); // Add to free list
			if(x.left != 0 && x.right != 0){
				// Replace root
				f.seek(r+12);
				long left = f.readLong();
				long right = f.readLong();
				long newRight = findMin(right);
				f.seek(minRight+12); // Go to address of small in right subtree
				f.writeLong(left); // Left is same as before
				f.writeLong(newRight); // Right is updated
				r = minRight; // Swap address
				return balance(r); // Balance tree
			}else{
				long left = x.left;
				long right = x.right;
				r = (left != 0) ? left : right;
				return balance(r);
			}
		}
		x.writeNode(r);
		return balance(r);
	}

	/**
	 * Find replacement address and balance tree
	 * @param r
	 * @return
	 * @throws IOException
	 */
	private long findMin(long r) throws IOException{
		f.seek(r+12);
		long left = f.readLong();
		long right = f.readLong();
		//Update the left
		long farLeft;
		if(left != 0){ // Keep moving left until at far most left
			farLeft = findMin(left);
			f.seek(r+12);
			f.writeLong(farLeft);
			return balance(r);
		}else{
			minRight = r; // Found replacement value!
			return balance(right); // Right child of replacement address
		}
	}

	/**
	 * Free list; potentially extend length of file (if nothing in free list).
	 * @return
	 */
	private long getFree() throws IOException{
		// When we add to the free list, we put the address of the
		// next item of the free list in the first 8 chunks

		// If nothing in free list, extend file length
		if(free == 0) return f.length();
		// Seek to the end, then write to extend file
		long temp = free;
		f.seek(free);
		free = f.readLong();
		return temp;
	}

	/**
	 * When a node is removed from the tree, the space for that node
	 * must be added to the free list. removeAll() always adds here.
	 * removeOne() having a count value of 0 adds here
	 * @param addr
	 * @return
	 * @throws IOException
	 */
	private long addFree(long addr) throws IOException{
		// No reason to go to end -- stick at front of list
		f.seek(addr);
		f.writeLong(free);
		free = addr;
		return addr;
	}

	/** Close the random access file */
	public void close(){
		// Update root and free BEFORE we close!
		try{
			f.seek(0);
			f.writeLong(root);
			f.writeLong(free);
			f.close();

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Print an in-order representation of the tree
	 * @throws IOException
	 */
	public void print() throws IOException{
		print(root);
		System.out.println();
	}

	/** Helper method for print() */
	private void print(long r) throws IOException{
		if(r == 0) return;
		Node x = new Node(r);
		print(x.left);
		System.out.print(" ("+x.data+","+x.count+") ");
		print(x.right);
	}

//	/**
//	 * String representation of the free list
//	 * @throws IOException
//	 */
//	private String freeToString() throws IOException{
//		String freeList = free + " ";
//		f.seek(free);
//		long x = f.readLong();
//		while(x != 0){
//			freeList += x + " ";
//			f.seek(x);
//			x = f.readLong();
//		}
//		return freeList.substring(1, freeList.length());
//	}
}
