import java.io.*;
import java.util.*;

public class TestSortedList {

	public SortedList<Integer> getIntList(Scanner scan, boolean prompt){
		SortedList<Integer> list = new SortedList<>();
		if(prompt){
			System.out.print("Enter a line of ints separated by commas: ");
		}
		String line = scan.nextLine();
		String input[] = line.split(",");
		for(int i = 0; i < input.length; i++){
			list.insert(new Integer(input[i]));
		}
		return list;
	}
	
	public SortedList<String> getStringList(Scanner scan, boolean prompt){
		SortedList<String> list = new SortedList<>();
		if(prompt)
			System.out.print("Enter a line of string separated by commas: ");
		String line = scan.nextLine();
		String input[] = line.split(",");
		for (int i = 0; i < input.length; i++){
			list.insert(input[i]);
		}
		return list;
	}
	
	public void removeInts(Scanner scan, SortedList<Integer> list, boolean prompt){
		if(prompt)
			System.out.print("Enter a line of ints to remove separated by commas: ");
		String line = scan.nextLine();
		String input[] = line.split(",");
		for(int i = 0; i < input.length; i++)
			list.remove(new Integer(input[i]));
	}
		
	public void removeStrings(Scanner scan, SortedList<String> list, boolean prompt){
		if (prompt)
			System.out.print("Enter a line of strings to remove separated by commas: ");
		String line = scan.nextLine();
		String input[] = line.split(",");
		for (int i = 0; i < input.length; i++)
			list.remove(input[i]);
	}
		
	public void testIntList(Scanner scan, boolean prompt){
		SortedList<Integer> list1 = getIntList(scan, prompt);
		SortedList<Integer> list2 = getIntList(scan, prompt);
		SortedList<Integer> list3 = new SortedList<Integer>(list1, list2);
		removeInts(scan, list3, prompt);
		System.out.println(list3);
	}
	
	public void testStringList(Scanner scan, boolean prompt){
		SortedList<String> list1 = getStringList(scan, prompt);
		SortedList<String> list2 = getStringList(scan, prompt);
		SortedList<String> list3 = new SortedList<String>(list1, list2);
		removeStrings(scan, list3, prompt);
		System.out.println(list3);
	}
	
	public TestSortedList(int dataType){
		Scanner scan = new Scanner(System.in);
		if (dataType == 1) { //integer input
			testIntList(scan, true);
		}else{ //string input
			testStringList(scan, true);
		}
	}
		
	public TestSortedList(int dataType, String filename) throws IOException{
		Scanner scan = new Scanner(new File(filename));
		if(dataType == 1){ //integer input
			testIntList(scan, false);
		}else{ //string input
			testStringList(scan, false);
		}
	}
	
	public static void main(String args[]) throws IOException{
		if(args.length == 0){ //Error
			System.out.println("Error: you must provide at least one command line argument: the data type of the input. 1 means integers and 2 means strings");
		}else if(args.length == 1){ //get input from standard in
			new TestSortedList(new Integer(args[0]));
		}else if(args.length == 2){ //get input from a file with a name given in args[1]
			new TestSortedList(new Integer(args[0]),args[1]);
		}
	}
}
