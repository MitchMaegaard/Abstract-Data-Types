import java.io.*;
import java.util.*;

public class AVLTreeTest {
	
	public static void main(String args[]) throws IOException {

		BufferedReader b = new BufferedReader(new FileReader(args[0]));
		int numtrees = Integer.parseInt(b.readLine());
		AVLTree trees[] = new AVLTree[numtrees];
		String filenames[] = new String[numtrees];

		for (int i = 0; i < numtrees; i++) {
			filenames[i] = b.readLine();
			trees[i] = new AVLTree(filenames[i],0);
		}

		System.out.println("Test 1");
		for (int j = 0; j < numtrees; j++) {
			System.out.println("tree "+j);
			//System.out.print("tree " + j + " :");
			String nums[] = b.readLine().split(" ");
			for (int i = 0; i < nums.length; i++) {
				trees[j].insert(Integer.parseInt(nums[i]));
			}
			//trees[j].print();
		}

		System.out.println("Test 2");
		for (int j = 0; j < numtrees; j++) {
			System.out.println("tree "+j);
			//System.out.println("tree " + j + "- find: ");
			String nums[] = b.readLine().split(" ");
			for (int i = 0; i < nums.length; i++) {
				//System.out.print(nums[i] + "? ");
				System.out.println(trees[j].find(Integer.parseInt(nums[i])));
			}
		}

		System.out.println("\nTest 3");
		for (int j = 0; j < numtrees; j++) {
			System.out.println("tree "+j);
			//System.out.print("tree " + j + " :");
			String nums[] = b.readLine().split(" ");
			for (int i = 0; i < nums.length; i++) {
				trees[j].removeAll(Integer.parseInt(nums[i]));
			}
			//trees[j].print();
		}

		System.out.println("Test 4");
		for (int j = 0; j < numtrees; j++) {
			System.out.println("tree "+j);
			//System.out.println("tree " + j + "- find: ");
			String nums[] = b.readLine().split(" ");
			for (int i = 0; i < nums.length; i++) {
				//System.out.print(nums[i] + "? ");
				System.out.println(trees[j].find(Integer.parseInt(nums[i])));
			}
		}

		System.out.println("\nTest 5");
		for (int j = 0; j < numtrees; j++) {
			trees[j].close();
		}

		System.out.println("\nTest 6");
		for (int i = 0; i < numtrees; i++) {
			trees[i] = new AVLTree(filenames[i],1);
		}

		System.out.println("Test 7");
		for (int j = 0; j < numtrees; j++) {
			System.out.println("tree "+j);
			//System.out.print("tree " + j + " :");
			String nums[] = b.readLine().split(" ");
			for (int i = 0; i < nums.length; i++) {
				trees[j].insert(Integer.parseInt(nums[i]));
			}
			//trees[j].print();
		}

		System.out.println("Test 8");
		for (int j = 0; j < numtrees; j++) {
			System.out.println("tree "+j);
			//System.out.println("tree " + j + "- find: ");
			String nums[] = b.readLine().split(" ");
			for (int i = 0; i < nums.length; i++) {
				//System.out.print(nums[i] + "? ");
				System.out.println(trees[j].find(Integer.parseInt(nums[i])));
			}
		}

		System.out.println("\nTest 9");
		for (int j = 0; j < numtrees; j++) {
			System.out.println("tree "+j);
			//System.out.print("tree " + j + " :");
			String nums[] = b.readLine().split(" ");
			for (int i = 0; i < nums.length; i++) {
				trees[j].removeOne(Integer.parseInt(nums[i]));
			}
			//trees[j].print();
		}

		System.out.println("Test 10");
		for (int j = 0; j < numtrees; j++) {
			System.out.println("tree "+j);
			//System.out.println("tree " + j + "- find: ");
			String nums[] = b.readLine().split(" ");
			for (int i = 0; i < nums.length; i++) {
				//System.out.print(nums[i] + "? ");
				System.out.println(trees[j].find(Integer.parseInt(nums[i])));
			}
		}

		System.out.println("\nDone");
	}
}
