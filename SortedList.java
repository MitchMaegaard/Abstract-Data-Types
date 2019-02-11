//import java.io.*;
//import java.util.*;

/*
 * Homework 1
 * 
 * Class: CS 340
 * 
 * Author: Mitch Maegaard
 * 
 * Due: Wednesday, Jan. 31, 2018
 * 
 * Send to: tgendreau@uwlax.edu
 */

/**
 * Implements a generic sorted linked list of Comparable objects.
 * The list is sorted in ascending order.
 *
 * @param <T>
 */
public class SortedList<T extends Comparable<? super T>> {

	// Nested helper class to generate linked list
	private class Node{
		private T data; // Data type of elements -- generic
		private Node next; // Pointer to the next element in our list
		// Primary constructor, instantiates data type and pointer
		private Node(T d, Node n){
			data = d;
			next = n;
		}
		// Secondary constructor, instantiates data type ONLY
		// Added for simplicity of moving elements around when comparing
		private Node(T d){
			data = d;
			next = null;
		}
	}
	
	private Node head; // Reference to first node in list
	private int size; // Number of elements in the list
	
	/**
	 * Constructor for an empty list
	 */
	public SortedList(){
		head = null; // No sentinel node
		size = 0; // Begin with an empty list
	}
	
	/**
	 * Constructor to create a NEW SortedList from two SortedLists.
	 * We are given the precondition that each list is not empty.
	 * @param s1
	 * @param s2
	 */
	public SortedList(SortedList<T> s1, SortedList<T> s2){
//		// Can NOT use the insert method
//		// Create method to combine s1 and s2 while maintaining order
//		head = merge(s1.head, s2.head);
//		
//		// Size of new list will be the size of both lists
//		size = s1.size + s2.size + 1;
		
		// NEW IMPELEMENTATION FROM GENDREAU
		Node temp1;
		Node temp2;
		if(s1.head.data.compareTo(s2.head.data) < 0){
			head = new Node(s1.head.data,null);
			temp1 = s1.head.next;
			temp2 = s2.head;
		}else{
			head = new Node(s2.head.data,null);
			temp1 = s1.head;
			temp2 = s2.head;
		}
		merge(temp1,temp2);
		size = s1.size + s2.size;
	}
	
//	/**
//	 * Recursive method to combine two sorted lists.
//	 * @param head1
//	 * @param head2
//	 * @return 
//	 */
//	public SortedList<T>.Node merge(Node head1, Node head2){
//		
//		Node comb = null; // Pointer to join our lists together
//		
//		// Check for border cases
//		if(head1 == null) return head2;
//		if(head2 == null) return head1;
//		
//		if(head1.data.compareTo(head2.data) < 0){
//			comb = head1;
//			// Recursive call to increment node pointer
//			comb.next = merge(head1.next, head2);
//		}else{
//			comb = head2;
//			comb.next = merge(head1, head2.next);
//		}
//		
//		return comb;
//	}
	
	/** NEW MERGE METHOD FROM GENDREAU */
	private void merge(Node temp1, Node temp2){
		Node last = head;
		while(temp1 != null && temp2 != null){
			if(temp1.data.compareTo(temp2.data) < 0){
				last.next = new Node(temp1.data,null);
				temp1 = temp1.next;
			}else{
				last.next = new Node(temp2.data,null);
				temp2 = temp2.next;
			}
			last = last.next;
		}
		while(temp1 != null){
			last.next = new Node(temp1.data,null);
			last = last.next;
			temp1 = temp1.next;
		}
		while(temp2 != null){
			last.next = new Node(temp2.data,null);
			last = last.next;
			temp2 = temp2.next;
		}
	}
	
	/**
	 * Insert item into list so the list remains sorted.
	 * Can contain duplicates!
	 * @param item
	 */
	public void insert(T item){
		
		// NEW IMPLEMENTATION BY GENDREAU
		size++;
		// Handle 2 cases of inserting an item at the beginning
		if(head == null || head.data.compareTo(item) > 0){
			head = new Node(item,head);
			return;
		}
		Node temp = head; // Instantiate after initial check to avoid 2 pointers
		// Advance temp until we find insertion point
		while(temp.next != null && temp.next.data.compareTo(item) < 0)
			temp = temp.next;
		temp.next = new Node(item,temp.next); // Add new item
		
//		// Node to be inserted
//		Node newNode = new Node(item);
//		// If list is initially empty
//		if(head == null){
//			head = newNode;
//			return;
//		}
//		// If new element is less than our current first element
//		else if(item.compareTo(head.data) < 0){
//			// Swap head and newNode
//			newNode.next = head;
//			head = newNode;
//		}
//		// If new element is in the middle or at the end of our list
//		else{
//			// Use 2 pointers
//			Node temp = head.next; // 1st keeps track of where we are in the list
//			Node prev = head; // 2nd will be our element/position to swap
//			while(temp != null){
//				if(item.compareTo(temp.data) < 0) break;
//				// Increment each pointer
//				prev = temp;
//				temp = temp.next;
//			}
//			// Swap prev and newNode
//			newNode.next = prev.next;
//			prev.next = newNode;
//		}
//		// Increment size here to avoid duplicate code
//		size++;
	}
	
	/**
	 * Remove ALL occurrences of item from the list.
	 * @param item
	 */
	public void remove(T item){
		
		// NEW IMPLEMENTATION BY GENDREAU
		while(head != null && head.data.compareTo(item) == 0){
			head = head.next;
			size--;
		}
		if(head == null) return; // If we've removed every item in the list
		Node temp = head;
		while(temp.next != null && temp.next.data.compareTo(item) <= 0){
			if(temp.next.data.compareTo(item) == 0){
				temp.next = temp.next.next;
				size--;
			}else
				temp = temp.next;
		}
		
//		// Check if list is already empty -- nothing to remove
//		if(head == null) return;
//		
//		// Use 2 pointers as in insert()
//		Node temp = head;
//		Node prev = null;
//		
//		// Need to check all elements, so OK to run off list
//		while(temp != null){
//			// Check if element to be removed is the same as
//			// the one at our pointer
//			if(item.compareTo(temp.data) == 0){
//				// If we remove head, move head up 1 position
//				if(temp == head){
//					head = head.next;
//				}
//				// Otherwise, set the position behind the one we
//				// want to remove to the next element in the list
//				else{
//					prev.next = temp.next;
//				}
//				size--;
//			}
//			// If elements aren't the same, move prev up
//			else{
//				prev = temp;
//			}
//			// Move to check next element in the list
//			temp = temp.next;
//		}
//		//size--; DO NOT decrement here
//		// This would only account for the deletion of 1 item
	}
	
	/**
	 * Return the number of times item is found in the list.
	 * @param item
	 * @return
	 */
	public int find(T item){
		int count = 0; // Counter for times item was in list
		Node temp = head; // Pointer
		
		while(temp != null){
			// If the value at our pointer is the same as
			// the element we are looking for, increment counter
			if(item.compareTo(temp.data) == 0)
				count++;
			// Go to next element
			temp = temp.next;
		}
		return count;
	}
	
	/**
	 * Return the number of items in the list.
	 * @return
	 */
	public int size(){
		return size;
	}
	
	/**
	 * Return a string representation of the list enclosed by brackets
	 * and separated by commas. Example: [2,3,7,10,50,107]
	 */
	public String toString(){
		// Border case for empty list
		if(head == null) return "";
		
		// Initially fill string with first element
		String stringList = "[" + head.data;
		Node temp = head.next; // Pointer to add next element
		
		while(temp != null){
			// Separate entries with commas and add next element
			// Make sure we don't add an unnecessary comma at the end
			stringList = stringList + "," + temp.data;
			temp = temp.next; // Increment our pointer
		}
		// Close bracket only AFTER all elements have been added
		stringList = stringList + "]";
		
		return stringList;
	}
	
	/**
	 * Main driver to perform tests on implementation. Do not need
	 * to include in submitted code.
	 * @param args
	 */
	public static void main(String[] args){
		// First test on integer list
		SortedList<Integer> intList = new SortedList<>();
		
		intList.insert(7);
		intList.insert(10);
		intList.insert(3);
		intList.insert(2);
		intList.insert(107);
		intList.insert(50);
		intList.insert(10);
		
		// Test insert(), size(), and toString() methods
		System.out.println("\nLinked list of size " + intList.size() + " is:");
		System.out.println(intList.toString());
		
		intList.remove(10);
		
		// Test remove() method
		System.out.println("\nLinked list of size " + intList.size() + " after deletion:");
		System.out.println(intList.toString());
		
		// Test find() method
		int findInt1 = 50;
		int findInt2 = 10;
		
		System.out.println("\nThe number of times " + findInt1 + " is in list: " + intList.find(findInt1));
		System.out.println("\nThe number of times " + findInt2 + " is in list: " + intList.find(findInt2));
		
		// Same tests with strings to test generic implementation
		SortedList<String> stringList = new SortedList<>();
		
		stringList.insert("peach");
		stringList.insert("apple");
		stringList.insert("grape");
		stringList.insert("banana");
		stringList.insert("pineapple");
		stringList.insert("watermelon");
		stringList.insert("grape");
		
		System.out.println("\nLinked list of strings of size " + stringList.size() + " is:");
		System.out.println(stringList.toString());
		
		stringList.remove("apple");
		stringList.remove("peach");
		
		System.out.println("\nLinked list of size " + stringList.size() + " after deletion:");
		System.out.println(stringList.toString());
		
		String findString1 = "grape";
		String findString2 = "apple";
		
		System.out.println("\nThe number of times " + findString1 + " is in list: " + stringList.find(findString1));
		System.out.println("\nThe number of times " + findString2 + " is in list: " + stringList.find(findString2));
		
		// TEST CASE ON AN EMPTY LIST
		SortedList<Integer> emptyList = new SortedList<>();
		
		emptyList.insert(2);
		emptyList.insert(3);
		emptyList.insert(2);
		emptyList.insert(3);
		emptyList.insert(7);
		
		System.out.println("\nLinked list of size " + emptyList.size() + " before we remove all elements:");
		System.out.println(emptyList.toString());
		
		emptyList.remove(2);
		emptyList.remove(3);
		emptyList.remove(7);
		emptyList.remove(5); // Test remove on an element that never existed
		
		System.out.println("\nLinked list after removing all elements has size "
							+ emptyList.size() + " and is represented by:");
		System.out.println(emptyList.toString());
		
		// TEST USING OUR 2ND CONSTRUCTOR
		SortedList<Integer> list1 = new SortedList<>();
		
		list1.insert(8);
		list1.insert(4);
		list1.insert(2);
		list1.insert(6);
		
		System.out.println("\nFirst sorted list of size " + list1.size() + ":");
		System.out.println(list1.toString());
		
		SortedList<Integer> list2 = new SortedList<>();
		
		list2.insert(5);
		list2.insert(1);
		list2.insert(7);
		list2.insert(3);
		list2.insert(9);
		
		System.out.println("\nSecond sorted list of size " + list2.size() + ":");
		System.out.println(list2.toString());
		
		SortedList<Integer> list3 = new SortedList<>(list1, list2);
		
		System.out.println("\nMerged lists of size " + list3.size() + ":");
		System.out.println(list3.toString());
	}
}
