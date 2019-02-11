import java.io.*;
import java.util.*;

/*
 * Homework 3
 * 
 * Class: CS 340
 * 
 * Author: Mitch Maegaard
 * 
 * Due: Friday, Feb. 16, 2018
 * 
 * Send to: tgendreau@uwlax.edu
 */

/** Undirected graph representation to find triangles in the graph */
public class UndirectedGraph {

	private class Vertex{
		private EdgeNode edges1;
		private EdgeNode edges2;
		private int numNeighbors; // This is the same as the degree of the vertex
		private int[] orderedNeighbors = new int[0]; // Set of ordered neighbors
		
		private Vertex(){}
	}
	
	private class EdgeNode{
		private int vertex1;
		private int vertex2;
		private EdgeNode next1;
		private EdgeNode next2;
		
		private EdgeNode(int v1, int v2, EdgeNode e1, EdgeNode e2){
			// PRE: v1 < v2
			// Each node is stored only once but it will be part of two lists
			g[v1].numNeighbors++;
			g[v2].numNeighbors++;
			vertex1 = v1;
			vertex2 = v2;
			next1 = e1;
			next2 = e2;
		}
	}
	
	private Vertex[] g; // Array of vertices in graph
	
	/**
	 * Constructor -- create a graph with size vertices.
	 * The vertices will be identified by ints between 0 and size -1.
	 * The vertices are stored in an array
	 * @param size
	 */
	public UndirectedGraph(int size){
		g = new Vertex[size];
		for(int i = 0; i < size; i++){
			g[i] = new Vertex();
		}
	}
	
	/** Add a new edge between v1 and v2 */
	public void addEdge(int v1, int v2){
		if(v1 > v2){
			addEdge(v2,v1);
		}else{
			EdgeNode tempE1 = g[v1].edges1;
			EdgeNode tempE2 = g[v2].edges2;
			g[v1].edges1 = new EdgeNode(v1, v2, tempE1, null);
			g[v2].edges2 = g[v1].edges1;
			g[v2].edges2.next2 = tempE2;
		}
	}
	
	/**
	 * Print the neighbors of each vertex to standard output.
	 * There should be one line for each vertex. The format of the
	 * line should be vertex name: comma delimited list of neighbors
	 */
	public void printNeighbors(){
		// Useful for checking if we built the graph correctly
		System.out.println("\n");
		for(int i = 0; i < g.length; i++){
			EdgeNode temp = g[i].edges1;
			String list = "";
			while(temp != null){
				list += temp.vertex2 + ",";
				temp = temp.next1;
			}
			temp = g[i].edges2;
			while(temp != null){
				list += temp.vertex1 + ",";
				temp = temp.next2;
			}
			System.out.println(i + ": " + list.substring(0, list.length() - 1));
		}
	}
	
	/**
	 * Find the ordered neighbors of each vertex. Each orderedNeighbor
	 * set should be sorted in ascending order
	 */
	public void populateOrderedNeighbors(){
		// Reference create ordered neighbor sets algorithm
		for(int i = 0; i < g.length; i++){
			EdgeNode temp = g[i].edges1;
			while(temp != null){
				int v1 = i;
				int v2 = temp.vertex2;
				if((g[v1].numNeighbors < g[v2].numNeighbors) || ((g[v1].numNeighbors == g[v2].numNeighbors) && (v1 < v2))){
					
					g[v1].orderedNeighbors = Arrays.copyOf(g[v1].orderedNeighbors, g[v1].orderedNeighbors.length + 1);
					g[v1].orderedNeighbors[g[v1].orderedNeighbors.length - 1] = v2;
					Arrays.sort(g[v1].orderedNeighbors);
				}else{
					g[v2].orderedNeighbors = Arrays.copyOf(g[v2].orderedNeighbors, g[v2].orderedNeighbors.length + 1);
					g[v2].orderedNeighbors[g[v2].orderedNeighbors.length - 1] = v1;
					Arrays.sort(g[v2].orderedNeighbors);
				}
				temp = temp.next1;
			}
		}
	}
	
	/**
	 * Find the number of elements in the intersection of the
	 * orderedNeighbors of v1 and the orderedNeighbors of v2
	 * @param v1
	 * @param v2
	 * @return intCnt -- # of similar elements in v1 and v2
	 */
	private int intersectionSize(int v1, int v2){
		// Use the fact that the orderedNeighbor sets are sorted in ascending order
		// An intersection of size 1 means we have a triangle
		int intCnt = 0;
		int[] v1ordered = Arrays.copyOf(g[v1].orderedNeighbors, g[v1].orderedNeighbors.length);
		int[] v2ordered = Arrays.copyOf(g[v2].orderedNeighbors, g[v2].orderedNeighbors.length);
		if(v1ordered.length <= v2ordered.length){
			int pos = 0;
			for(int i = 0; i < v1ordered.length; i++){
				while(pos < v2ordered.length && v2ordered[pos] <= v1ordered[i]){
					if(v2ordered[pos] == v1ordered[i]){
						intCnt++;
						break;
					}
					pos++;
				}
			}
		}else{
			for(int i = 0; i < v2ordered.length; i++){
				int pos = 0;
				while(pos < v1ordered.length && v1ordered[pos] <= v2ordered[i]){
					if(v1ordered[pos] == v2ordered[i]){
						intCnt++;
						break;
					}
					pos++;
				}
			}
		}
		return intCnt;
	}
	
	/** Return the number of triangles in the graph */
	private int countTriangles(){
		int triangles = 0;
		for(int i = 0; i < g.length; i++){ // for each vertex, v1, in the graph
			int[] orderedVi = g[i].orderedNeighbors;
			for(int j = 0; j < orderedVi.length; j++){ // for each v2 in the neighbors of v1
				triangles += intersectionSize(i,orderedVi[j]); // number of elements in intersection
			}
		}
		return triangles;
	}
	
	/**
	 * Build the graph and find the number of triangles.
	 * First line contains the number of vertices (int).
	 * Remaining lines contain pairs of ints that represent edges
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		// Error if 0 or more than 1 command line arguments are given
		if (args.length != 1)
			System.out.println("ERROR: One command line argument expected.");
		else {
			BufferedReader reader = new BufferedReader(new FileReader(args[0]));
			// First line contains number of vertices
			String line = reader.readLine();
			Integer num = Integer.parseInt(line);

			UndirectedGraph g = new UndirectedGraph(num);

			// Edges specified on following lines with format: vertex1 vertex2
			line = reader.readLine();
			String[] edges;
			// Edges added by new line
			while (line != null) {
				edges = line.trim().split("\\s+");
				g.addEdge(Integer.parseInt(edges[0]), Integer.parseInt(edges[1]));
				line = reader.readLine();
			}
			reader.close();
			
			g.populateOrderedNeighbors();
			//g.printNeighbors(); // COMMENT OUT BEFORE SUBMISSION
			
			int numTriangles = g.countTriangles();
			System.out.print("\nThe number of triangles is " + numTriangles);
		}
	}
}
