import java.io.*;
import java.util.*;

/*
 * Homework 4
 * 
 * Class: CS 340
 * 
 * Author: Mitch Maegaard
 * 
 * Due: Friday, Mar. 2, 2018
 * 
 * Send to: tgendreau@uwlax.edu
 */

public class IntTree {
	
	private class Node{
		private int data;
		private Node firstChild;
		private Node sibling;
		private Node parent;
		
		private Node(int d,Node f,Node s,Node p){
			data = d;
			firstChild = f;
			sibling = s;
			parent = p;
		}
	}
	
	private Node root;
	
	/** Create a one node tree */
	public IntTree(int d){
		root = new Node(d,null,null,null);
	}
	
	/**
	 * Create a tree with d[0] as the root value and the
	 * other values as children of the root
	 * 
	 * @param d
	 */
	public IntTree(int[] d){
		Node[] nodes = new Node[d.length];
		root = new Node(d[0],null,null,null);
		for(int i = 1; i < nodes.length; i++){
			Node added = new Node(d[i],null,null,root);
			nodes[i-1] = added;
		}
		root.firstChild = nodes[0];
		for(int i = 0; i < nodes.length - 1; i++)
			nodes[i].sibling = nodes[i+1];
	}
	
	/**
	 * Create a new tree whose children are the trees in t
	 * and whose root value is d
	 * 
	 * @param t
	 * @param d
	 */
	public IntTree(int d, IntTree[] t){
		root = new Node(d,t[0].root,null,null);
		for(int i = 0; i < t.length - 1; i++)
			t[i].root.sibling = t[i+1].root;
		for(int i = 0; i < t.length; i++)
			t[i].root.parent = root;
	}
	
	public IntTree(int d, IntTree c){
		root = new Node(d,c.root,null,null);
		c.root.parent = root;
	}
	
	/**
	 * Separate ints with commas. RECURSIVE!
	 * @return pre - a string of the ints in the tree in preorder
	 */
	public String preorder(){
		String pre = preorder(root);
		return pre.substring(0, pre.length() - 1);
	}
	
	/** Helper method for preorder */
	private String preorder(Node r){
		if(r == null) return "";
		return r.data + "," + preorder(r.firstChild) + preorder(r.sibling);
	}
	
	/**
	 * Separate ints with commas. RECURSIVE!
	 * @return post - a string of the ints in the tree in postorder
	 */
	public String postorder(){
		String post = postorder(root);
		return post.substring(0, post.length() - 1);
	}
	
	/** Helper method for postorder */
	private String postorder(Node r){
		if(r == null) return "";
		return postorder(r.firstChild) + postorder(r.sibling) + r.data + ",";
	}
	
	/**
	 * Level order is also known as breadth-first order.
	 * Separate the ints with commas. RECURSIVE!
	 * 
	 * @return level - a string of the ints in the tree in level order
	 */
	public String levelorder(){
		return levelorder(root);
	}
	
	private String levelorder(Node r){
		String level = "";
		LinkedList<Node> q = new LinkedList<>();
		if(r == null) return level;
		q.add(r);
		while(q.size() > 0){
			Node curr = q.remove();
			level += curr.data + ",";
			curr = curr.firstChild;
			while(curr != null){
				q.add(curr);
				curr = curr.sibling;
			}
		}
		return level.substring(0, level.length() - 1);
	}
	
	/**
	 * Return the ints in the path from the first occurrence
	 * of d in the tree to the root of the tree. The
	 * “first occurrence” means the first occurrence found in
	 * a preorder traversal. The implementation must use the
	 * parent reference to create the path. Separate ints with
	 * commas. ITERATIVE!
	 * 
	 * @param d
	 * @return p
	 */
	public String path(int d){
		String p = "";
		p = path(d,root,p) + "," + d;
		
		StringBuilder rev = new StringBuilder();
		String[] words = p.split(",");
		for(int i = words.length-1; i >= 0; i--)
			rev.append(words[i]).append(",");
		return rev.toString().substring(0, rev.length() - 2);
	}
	
	private String path(int d, Node r, String s){
		if(r == null) return "";
		if(r.data != d){
			s = path(d,r.firstChild,s+","+r.data) + path(d,r.sibling,s+","+r.data);
		}
		return s;
	}
	
	/**
	 * Return the number of times d appears in the tree. RECURSIVE!
	 * 
	 * @param d
	 * @return count
	 */
	public int count(int d){
		return count(d,root);
	}
	
	private int count(int d, Node r){
		if(r == null) return 0;
		int count = count(d,r.firstChild) + count(d,r.sibling);
		if(r.data == d) count++;
		return count;
	}

	/** Return the sum of the ints in the tree. ITERATIVE! */
	public int sum(){
		return sum(root);
	}
	
	private int sum(Node r){
		if(r == null) return 0;
		return r.data + sum(r.firstChild) + sum(r.sibling);
	}
}
