package bpc.Graph;

import java.util.ArrayList;
import Utilities.VRPTWCG.Rounder;
import bpc.Graph.*;
import bpc.IO.DataHandler;



/**
 * This class contains the structures for the ESPPRC heuristic
 * @author Daniel
 *
 */
public class GraphManager{

	
	//static Node[] routes;
	public static ArrayList<Node>[] routes;
	public static double[] routesDist;
	public static double[] routesCost;
	public static int[] routesLoad;
	public static Node[] customers;
	
	public static int boundStep=5;
	public static ArrayList<Node> requestBank;
	public static double totalDistance;
	public static double FO;
	
	public static int[] visited;
	public static int[][] visitedMT;
	
	public static int[][] QsetsUtilizationMT;
	
	
	public static double PrimalBound;
	public static double DualBound;
	
	public static double Barrier;
	
	public static double[][] OracleBound;
	public static double[] OraclePrimalBound;
	public static double OracleBestBound;
	
	public static FinalNode finalNode;
	
	public static double maxCost= 0 ;
	public static double minCost= Double.POSITIVE_INFINITY;
	public static double maxTWA = 0 ;
	public static double minTWA = Double.POSITIVE_INFINITY;
	public static double maxLoad = 0;
	public static double minLoad = Double.POSITIVE_INFINITY;
	
	private int numNodes;
	private int Cd;
	private int Ct;
	
	/**
	 * Pulse Stuff
	
	*/
	
	
	public GraphManager( int numNodes) {
		
		this.numNodes = numNodes;
		//nodeList = new Hashtable<Integer, VertexPulse>(numNodes);
		Cd=0;
		Ct=0;
		routes = new ArrayList[DataHandler.k];
		for (int i = 0; i < routes.length; i++) {
			routes[i]=new ArrayList<Node>();
		}
		routesDist = new double[DataHandler.k];
		routesCost = new double[DataHandler.k];
		routesLoad = new int[DataHandler.k];
		customers = new Node[numNodes];
		requestBank = new ArrayList<Node>();
		visited = new int[numNodes];
		visitedMT = new int[numNodes][DataHandler.numThreads+1];
		PrimalBound= 0;
		
		OracleBound= new double [numNodes][501];
		OraclePrimalBound= new double [numNodes];
		OracleBestBound=0;
		for(int i=1; i<numNodes; i++){
			OraclePrimalBound[i]=Double.POSITIVE_INFINITY;
		}
		
		
		finalNode = new FinalNode(numNodes, 0, 0, 0, DataHandler.tw_b[0]);
		
		
		//routeSize = new int[DataHandler.k];
		
	}


	public  int getNumNodes()
	{
		return numNodes;
	}
	
	
	public boolean addVertex(Node v) {
		customers[v.getID()] = v;
		if (v.id != 0) {
			if (v.tw_a < minTWA) {
				minTWA = v.tw_a;
			}
			if (v.tw_a > maxTWA) {
				maxTWA = v.tw_a;
			}
			if (v.demand < minLoad) {
				minLoad = v.demand;
			}
			if (v.demand > maxLoad) {
				maxLoad = v.demand;
			}
		}
		return true;
	}
	
	public int getCd()
	{
		return Cd;
	}
	public int getCt()
	{
		return Ct;
	}
	public  ArrayList<Node>[] getRoutes(){ 
			return routes;
	}
	public int[] getRoutesLoad(){
		return routesLoad;
	}
	public Node[] getCustomers(){
		return customers;
	}
	public ArrayList<Node> getRequestBank(){
		return requestBank;
	}
	static public void calculateFO(double shift){
		Node customer = null;

		for (int k = 0; k < routes.length; k++) {
			int load = 0;
			for (int i = 1; i < routes[k].size(); i++) {
				customer = routes[k].get(i);
				customer.arrivalTime  = Rounder.round6Dec(routes[k].get(i - 1).exitTime+ DataHandler.distance[routes[k].get(i - 1).id][customer.id]);
				if(customer.arrivalTime>customer.tw_b){
					System.out.println("ERROR FATAL ");
				}
				
				customer.cumulativeDist = (routes[k].get(i - 1).cumulativeDist+ DataHandler.distance[routes[k].get(i - 1).id][customer.id]);
				customer.cumulativeCost = (routes[k].get(i - 1).cumulativeCost+ DataHandler.cost[routes[k].get(i - 1).id][customer.id]);
				customer.exitTime  = Rounder.round6Dec(Math.max(customer.arrivalTime , customer.tw_a) + customer.service);
				
				customer.route = k;
				customer.visited = i;
				load += customer.demand;
			}
			routesLoad[k] = load;
			routesDist[k] = customer.cumulativeDist;
			routesCost[k] = customer.cumulativeCost-shift;
		}
		totalDistance = 0.0;
		FO = 0.0;
		for (int i = 0; i < routesDist.length; i++) {
			totalDistance += routesDist[i];
			if (routesCost[i]<FO) {
			FO = routesCost[i];
			}
		}
		

	}

	public static void reset() {
		requestBank.clear();
		for (int i = 0; i < routes.length; i++) {
			routes[i].clear();
		}
		
	}

	public static void ResetBounds() {
		PrimalBound = 0;
		OracleBound = new double[DataHandler.n + 2][501];
		OraclePrimalBound = new double[DataHandler.n + 2];
		OracleBestBound = 0;
		for (int i = 1; i < DataHandler.n + 2; i++) {
			OraclePrimalBound[i] = Double.POSITIVE_INFINITY;
		}
	}
}
