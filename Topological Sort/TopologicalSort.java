import java.io.*;
import java.util.*;

/*
 * Homework 2
 * 
 * Class: CS 340
 * 
 * Author: Mitch Maegaard
 * 
 * Due: Wednesday, Feb. 7, 2018
 * 
 * Send to: tgendreau@uwlax.edu
 */

/** Adjacency list representation of a directed graph. */
public class TopologicalSort {

	/** Helper class to create vertex nodes. */
	private class VertexNode{
		private String name;
		private VertexNode nextV;
		private EdgeNode edges;
		private int indegree;
		
		private VertexNode(String n, VertexNode v){
			name = n;
			nextV = v;
			edges = null;
			indegree = 0;
		}
	}
	
	/** Helper class to create edge nodes. */
	private class EdgeNode{
		private VertexNode vertex1;
		private VertexNode vertex2;
		private EdgeNode nextE;
		
		private EdgeNode(VertexNode v1, VertexNode v2, EdgeNode e){
			vertex1 = v1;
			vertex2 = v2;
			nextE = e;
		}
	}
	
	private VertexNode vertices; // Head of the list of vertex nodes
	private int numVertices; // Number of vertices in our graph
	
	/** Constructor */
	public TopologicalSort(){
		vertices = null;
		numVertices = 0;
	}
	
	/**
	 * PRE: Vertex list is sorted in ascending order using the name as the key
	 * 
	 * A vertex with name 's' has been added to the vertex list and the vertex
	 * list is sorted in ascending order using the name as the key
	 * @param s
	 */
	public void addVertex(String s){
		VertexNode newV = new VertexNode(s,null);
		
		if(vertices == null) vertices = newV;
		else{
			VertexNode temp = vertices;
			while(temp.nextV != null)
				temp = temp.nextV;
			temp.nextV = newV;
		}
		numVertices++;
	}
	
	/**
	 * PRE: Vertices n1 and n2 have already been added
	 * 
	 * Add the new edge (n1,n2) to the n1 edge list
	 * @param n1
	 * @param n2
	 */
	public void addEdge(String n1, String n2){
		VertexNode v1 = vertices;
		VertexNode v2 = vertices;

		while(v1.name.compareTo(n1) != 0)
			v1 = v1.nextV;
		while(v2.name.compareTo(n2) != 0)
			v2 = v2.nextV;
		
		EdgeNode newE = new EdgeNode(v1,v2,null);
		
		if(v1.edges == null)
			v1.edges = newE;
		else{
			EdgeNode temp = v1.edges;
			while(temp.nextE != null)
				temp = temp.nextE;
			temp.nextE = newE;
		}
		v2.indegree++;
	}
	
	/** Store the vertex names in an array. */
	private VertexNode[] toVertexArray(){
		VertexNode[] vertexNames = new VertexNode[numVertices];
		VertexNode temp = vertices;
		for(int i = 0; i < numVertices; i++){
			vertexNames[i] = temp;
			temp = temp.nextV;
		}
		return vertexNames;
	}
	
	/**
	 * Return vertices when they have an indegree of 0.
	 * Sets up a queue for the elements to be returned in a FIFO order.
	 * 
	 * @param vertexNames - Array of vertices in the graph
	 * @return vertex with an indegree of 0
	 */
	private VertexNode queue(VertexNode[] vertexNames){
		for(int i = 0; i < vertexNames.length; i++){
			if(vertexNames[i] != null && vertexNames[i].indegree == 0){
				return vertexNames[i];
			}
		}
		return null;
	}
	
	/**
	 * Decrement the indegrees of vertices adjacent to a specified
	 * vertex by 1 once it has been added to the queue (indegree of 0).
	 * 
	 * @param qVertex - last item added to the queue
	 */
	private void updateAdjacentIndegrees(VertexNode qVertex){
		EdgeNode temp = qVertex.edges;
		while(temp != null){
			temp.vertex2.indegree--;
			temp = temp.nextE;
		}
	}
	
	/** Get initial indegrees */
	private int[] initIndegrees(){
		int[] initIndegree = new int[numVertices];
		VertexNode temp = vertices;
		for(int i = 0; i < numVertices; i++){
			initIndegree[i] = temp.indegree;
			temp = temp.nextV;
		}
		return initIndegree;
	}
	
	/**
	 * Immediately check that an indegree of zero exists
	 * so we know a cycle doesn't exist
	 * @return
	 */
	private boolean checkInitialZero(){
		int[] initIndegree = initIndegrees();
		for(int i = 0; i < initIndegree.length; i++)
			if(initIndegree[i] == 0)
				return true;
		return false;
	}
	
	/**
	 * Returns null when the graph contains a cycle.
	 * Otherwise, returns a string containing the names of vertices
	 * separated by blanks in a topological order.
	 * 
	 * @return a string of the vertices in topological order
	 */
	public String topoSort(){
		if(!checkInitialZero())
			return null; // Cycle if no starting 0's
		
		VertexNode temp;
		VertexNode[] vertexNames = toVertexArray();
		String topo = "";
		
		while(queue(vertexNames) != null){
			temp = queue(vertexNames);
			topo += temp.name + " ";
			updateAdjacentIndegrees(temp);
			
			for(int i = 0; i <  vertexNames.length; i++)
				if(vertexNames[i] == temp)
					vertexNames[i] = null;
			
		}
		if(topo.length() == numVertices*2)
			return topo;
		
		return null;
	}
	
	/**
	 * Expects one command line argument, which is the name of
	 * a text file. Main should build the graph from the description
	 * in the file and then try to find a topological ordering for
	 * the graph.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		// Error if 0 or more than 1 command line arguments are given
		if(args.length != 1) System.out.println("ERROR: One command line argument expected.");
		
		else{
			TopologicalSort sort = new TopologicalSort();
			BufferedReader reader = new BufferedReader(new FileReader(args[0]));
			
			// Read in first line to get vertices
			String line = reader.readLine();
			String[] vNames = line.trim().split("\\s+");
			// Add vertices to graph
			for(int i = 0; i < vNames.length; i++){
				sort.addVertex(vNames[i]);
			}
			// Start reading next line to get edges
			line = reader.readLine();
			String[] edges;
			// Edges added by new line
			while(line != null){
				edges = line.trim().split("\\s+");
				// Only accept 2 vertices at a time for creating edges
				if(edges.length > 2)
					throw new IndexOutOfBoundsException("Edges need to be added in pairs only!");
				for(int i = 0; i < edges.length; i++){
					sort.addEdge(edges[0], edges[1]);
				}
				line = reader.readLine();
			}
			reader.close();
			
			String sorted = sort.topoSort();
			// Only print the topological ordering if one exists
			if(sorted != null) System.out.println(sorted);
			else System.out.println("No topological ordering exists for the graph!");
		}
	}
}
