import java.util.*;

/**
 * Binary tree representation built from HuffmanEncode where
 * every leaf is represented by a character that has been
 * encoded. Characters with higher frequency are closer to the
 * root. We can traverse the tree with 0 representing a move
 * to the left, and 1 a move to the right. This is useful in
 * HuffmanDecode.
 */
public class HuffmanTree {

	/** Helper class for HuffmanTree to store data */
    private class Node {

        private Node left;
        private char data;
        private Node right;
        private Node parent;
        
        /** Node constructor with left child, right child, and parent references */
        private Node(Node L, char d, Node R, Node P) {
            left = L;
            data = d;
            right = R;
            parent = P;
        }
    }

    private Node root;
    private Node current; // This value is changed by the move methods
    private int numLeafs = 0; // Count leaves to assist in iterating
    
    /** 0 argument constructor */
    public HuffmanTree() {
        root = null;
        current = null;
    }
    
    /**
     * Makes a single node tree with d as the root
     * @param d
     */
    public HuffmanTree(char d) {
        root = new Node(null, d, null, null);
        moveToRoot(); // Set pointer
    }
    
    /**
     * HuffmanTree constructor taking in a postorder representation of a string
     * and filling non-leaf nodes with a specified character -- (128) in class.
     * @param t
     * @param nonLeaf
     */
    public HuffmanTree(String t, char nonLeaf) {
        
        Stack<HuffmanTree> stack = new Stack<>();
        
        for(int i = 0; i < t.length(); i++) {
        	
            char curr = t.charAt(i); // Get the next char in our string
            // If we get to a non-leaf char, need to pop off 2 high priorities
            if(curr == nonLeaf) {
                HuffmanTree right = stack.pop(); // Pop off highest priority and set right
                HuffmanTree left = stack.pop(); // 2nd highest priority goes left
                // Link the nodes and add back to the stack
                HuffmanTree merge = new HuffmanTree(left, right, nonLeaf);
                stack.push(merge);
                
            } else stack.push(new HuffmanTree(curr)); // Add all leaves to stack until we get to a non-leaf
        }
        // Pop off our final tree after all priorities have been added and re-set root/current positions
        HuffmanTree fullTree = stack.pop();
        root = fullTree.root;
        moveToRoot();
    }
    
    /**
     * 
     * @param b1 -- new left HuffmanTree
     * @param b2 -- new right HuffmanTree
     * @param d -- new character stored in the root
     */
    public HuffmanTree(HuffmanTree b1, HuffmanTree b2, char d) {
        root = new Node(b1.root, d, b2.root, null);
        
        root.left.parent = root;
        root.right.parent = root;
        
        moveToRoot();
    }
    
    /** Move current location back to the root */
    public void moveToRoot() {
        current = root;
    }
    
    /** Move current location to left child */
    public void moveToLeft() {
        current = current.left;
    } 
    
    /** Move current location to right child */
    public void moveToRight() {
        current = current.right;
    }
   
    /** Move current location to the parent */
    public void moveToParent() {
        current = current.parent;
    }
    
    /** Check if we're at a leaf (signals a character in our string) */
    public boolean atLeaf() {
    	return (current.left == null && current.right == null);
    }
    
    /** Character at the current location */
    public char current() {
    	return current.data;
    }
    
    /** Return a copy of our HuffmanTree to assist our iterator */
    private HuffmanTree getCopy() {
        
        if(root == null) return null; // Case for empty tree
        
        moveToRoot();
        // Start copying at the root with the initial character
        HuffmanTree treeCopy = new HuffmanTree(this.current());
        
        while(current != null) {
        	// Search and copy all the way to the left
            if(current.left != null) {
                moveToLeft();
                treeCopy.current.left = new HuffmanTree(this.current()).root;
                treeCopy.current.left.parent = treeCopy.current;
                treeCopy.moveToLeft();
            }
            // Search and copy all the way right
            else if(current.right != null) {
                moveToRight();
                treeCopy.current.right = new HuffmanTree(this.current()).root;
                treeCopy.current.right.parent = treeCopy.current;
                treeCopy.moveToRight();
            }
            // Found a leaf!
            else {
                treeCopy.numLeafs++;
                // Work our way back up the tree
                while(current != null) {
                    // Check right references to make sure we haven't passed over any nodes
                    if(current.right != null && treeCopy.current.right == null) {
                        moveToRight();
                        treeCopy.current.right = new HuffmanTree(this.current()).root;
                        treeCopy.current.right.parent = treeCopy.current;
                        treeCopy.moveToRight();
                        break;
                    }
                    moveToParent();
                    treeCopy.moveToParent();
                }
            }
        }
        // After iterating through and creating copy, get back to the root
        moveToRoot();
        treeCopy.moveToRoot();
        
        return treeCopy;
    }
    
    /** Iterator that returns the paths within the trees -- 0 is left, and 1 is right */
    private class PathIterator implements Iterator<String> {
    	
        private StringBuffer path; // Helper string to modify as we search for new paths
        private HuffmanTree copied; // Copy of tree while iterator is being called
        private int pathCount = 0; // Number of times next() has been called
        private int maxPaths; // Max times next() can be called
        
        /** PathIterator constructor -- instantiates our path string, and makes a copy of the tree */
        public PathIterator() {
            path = new StringBuffer(128); 
            copied = getCopy();
            maxPaths = copied.numLeafs;
        }
        
        /** True until we get to the final path */
        public boolean hasNext() {
        	return (pathCount < maxPaths);
        }
        
        /** Returns the next path */
        public String next() {
            int len = path.length();
            
            // Find first path and move to the far left of the tree
            if(len == 0) {
               while(!copied.atLeaf()) {
                   path.append(0);
                   copied.moveToLeft();
               }
            }
          // Find the next path and build off of the previous path
            else {
                String prevPath = path.toString();
                // Go back to parent and find new path
                path.deleteCharAt(len-1);
                copied.moveToParent();
                // If we were just on the left side, go right
                if(prevPath.charAt(prevPath.length()-1) == '0') {
                    copied.moveToRight();
                    path.append(1);
                    // Check for nested trees on the right
                    while(!copied.atLeaf()) {
                        path.append(0);
                        copied.moveToLeft();
                    }
                }
                // If we were just on the right, go back to the next parent
                else {
                	// Move up until we can shift to the right subtree
                    while(true) {
                        // Check for non-checked right subtrees
                        if(path.charAt(path.length()-1) == '0') {
                            copied.moveToParent();
                            path.deleteCharAt(path.length()-1);
                            break;
                        }
                        copied.moveToParent();
                        path.deleteCharAt(path.length()-1);
                    }
                    // Shift right
                    copied.moveToRight();
                    path.append(1);
                    // Check for left subtree in right subtree
                    while(!copied.atLeaf()) {
                        path.append(0);
                        copied.moveToLeft();
                    }
                }
            }
            pathCount++; // Found a full path!
            return copied.current() + path.toString(); // Formatting example -- 'A0101110'
        }
        
        // Not used in our implementation
        public void remove() {
             throw new UnsupportedOperationException();
        }
    }
    
    /** Iterator to find paths for every character in our tree */
    public Iterator<String> iterator() {
        return new PathIterator();
    }
    
    /** Return a postorder representation of our HuffmanTree */
    public String toString() {
    	return toString(root);
    } 
    
    /** Helper method to build string recursively */
    private String toString(Node r) {
    	if(r == null) return "";
    	
    	return toString(r.left) + toString(r.right) + r.data;
    }
}
