import java.io.*;
import java.util.*;

public class h4test {
    
    public static void tOuput(IntTree t, int d) {
        System.out.println("Preorder: "+t.preorder());
        System.out.println("Postorder: "+t.postorder());
        System.out.println("Levelorder: "+t.levelorder());
        System.out.println("Sum: "+t.sum());
        System.out.println("Count("+d+"): "+t.count(d));
        System.out.println("Path: "+t.path(d));
        //System.out.println(t.contains(d));
        System.out.println("\n\n");
    }
    
    public static IntTree fullTree(BufferedReader b) throws Exception {
        //create a full tree of a particular order (i.e. fi order is 2 a full binary tree is created
        //if order is 3 a full tree where every node is either a leaf or have three children
        String line = b.readLine();
        int order = Integer.parseInt(line);
        line = b.readLine();
        String values[] = line.split(" ");
        IntTree t[] = new IntTree[values.length];
        for (int i = 0; i < values.length; i++) {
            t[i] = new IntTree(Integer.parseInt(values[i]));
        }
        
        for (int i = values.length/order; i > 0; i = i/order) {
            line = b.readLine();
            values = line.split(" ");
            for (int j = 0; j < i; j++) {
                t[j] = new IntTree(Integer.parseInt(values[j]),Arrays.copyOfRange(t, order*j, order*j+order));
            }
        }
        tOuput(t[0],Integer.parseInt(values[0]));
        return t[0];
    }
    
    
	public static void main(String args[]) throws Exception {
		IntTree t1 = null;
		IntTree t2[];
        IntTree t3[];
        String values[] = null;

		BufferedReader b = new BufferedReader(new FileReader(args[0]));

		String line = b.readLine();
        int count = Integer.parseInt(line);
        t2 = new IntTree[count];
        for (int i = 0; i < count; i++) {
            line = b.readLine();
        
            values = line.split(" ");
            t1 = new IntTree(Integer.parseInt(values[0]));
        
            for (int j = 1; j< values.length; j++) {
                t1 = new IntTree(Integer.parseInt(values[j]),t1);
            }
            t2[i] = t1;
        }
        tOuput(t1,Integer.parseInt(values[0]));

        line = b.readLine();
        t1 = new IntTree(Integer.parseInt(line), t2);
        tOuput(t1,Integer.parseInt(values[0]));
  
        line = b.readLine();
        count = Integer.parseInt(line);
        t3 = new IntTree[count];
        for (int i = 0; i < count; i++ ) {
            t3[i] = fullTree(b);
        }
        
        //merge all the full trees with a parent node or 32768
        t1 = new IntTree(32768, t3);
        tOuput(t1,Integer.parseInt(b.readLine()));
	}

}