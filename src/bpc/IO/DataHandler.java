package bpc.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;

import bpc.CG.GraphManager;
import bpc.Graph.*;
public class DataHandler {

	public static int numThreads = 4;
	
	public static final int numLabels = 10;
	public static Random r = new Random();
	public static double destroy = 0.35;
	public static final double infinity = Double.POSITIVE_INFINITY;
	

	public static  int tabuMerge = 0;
	public static double AverageArc = 0;
	public static int tBar = 0;
	public static boolean seedChanged = false;
	//public static double brPenalization =100000.0;
//	public static double[] pi = new double[102];
	public static String CvsInput;
	
	
	public static int NumArcs;
	static int LastNode;
	
	public static int[][] Arcs;
	public static double[] distList;
	public static double[] timeList;
	public static double[] costList;
	public static double[] loadList;
	//static ArrayList<EdgePulse> Arcs;
	//static double speed = 1.0;
	
	
	
	public static int n;
	public static int k;
	public static int cap;
	public static int dropTime;

	
	public static int[] demand;// demand
	public static int[] service;//service duration
	public static int[] tw_a;//time window begin
	public static int[] tw_b;//time window end
	
	public static double[] x; 
	public static double[] y;
	
	
	
	private GraphManager G;
	
	public static double[][] distance;
	public static double[][] cost;
	public static double[][] SAVES;
	/**
	 * If the arc is forbidden due a particular branching, the 
	 * attribute forbidden[i][j] is set to 1, or 0 otherwise.
	 * Forbidden arcs can not be used in the subproblem.
	 */
	public static int[][] forbidden;
	

	public static double[] LowerOutgoingArc;
	
	//public static double primalBound;
	private String  type;
	private int series;
	public static int depot;

	public DataHandler(String instanceFile, String name, int instance) {
		CvsInput = instanceFile;
		type = name;
		series=instance;
		
	}
	
	
	
	
	/**
	 * Read a solomon instance
	 * @param onlyESPPRC true is only one ESPPRC is going to be solved. False for CG
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public void ReadSolomon(boolean onlyESPPRC, int numNodes) throws NumberFormatException, IOException {
		
		readCap_and_Vels();
		File file = new File(CvsInput);
		BufferedReader bufRdr = new BufferedReader(new FileReader(file));
		String line = bufRdr.readLine(); //READ Num Nodes
		
		//k= Integer.parseInt(spread[1])+5;
		n = numNodes;
		
		StringTokenizer t = new StringTokenizer(line, " ");
		
		x = new double[n+1]; 
		y = new double[n+1]; 
		demand = new int[n+1]; 
		service =  new int[n+1];
		tw_a =  new int[n+1];
		tw_b =  new int[n+1];
		LowerOutgoingArc = new double[n+1];
		
		String[] cosa = new String[7];
		int indexCosa = 0;
		while (t.hasMoreTokens()) {
			cosa[indexCosa] = t.nextToken();
			indexCosa++;
		}
		
		
		x[0] =Double.parseDouble(cosa[1]);
		y[0] =Double.parseDouble(cosa[2]);
		service[0] = (int)(Double.parseDouble(cosa[6]));
		demand[0]=(int)(Double.parseDouble(cosa[3]));
		tw_a[0]= (int)(Double.parseDouble(cosa[4]));
		tw_b[0]= (int)(Double.parseDouble(cosa[5]));
		tBar  = tw_b[0];
		G = new GraphManager(n+1); 
		int arcos = (n+1)*(n+1)-(n+1);
		G.addVertex(new Node(0,demand[0],service[0],-tw_b[0],tw_b[0]));
		int custumerNumber = 1;
	
		while (custumerNumber<=n) {
			indexCosa=0;
			cosa= new String[7];
			line = bufRdr.readLine();
			t = new StringTokenizer(line, " ");
			while (t.hasMoreTokens()) {
				cosa[indexCosa] = t.nextToken();
				indexCosa++;
			}
			x[custumerNumber] =Double.parseDouble(cosa[1]);
			y[custumerNumber] =Double.parseDouble(cosa[2]);
			service[custumerNumber] = (int)(Double.parseDouble(cosa[6]));
			demand[custumerNumber]=(int)(Double.parseDouble(cosa[3]));
			tw_a[custumerNumber]= (int)(Double.parseDouble(cosa[4]));
			tw_b[custumerNumber]= (int)(Double.parseDouble(cosa[5]));
			
			G.addVertex(new Node(custumerNumber,demand[custumerNumber],service[custumerNumber], tw_a[custumerNumber],tw_b[custumerNumber]));
			custumerNumber++;
		}
		
		
		distance = new double[n + 1][n + 1];
		cost = new double[n + 1][n + 1];
		SAVES = new double[n+1][n+1];
		distList = new double[arcos];
		costList = new double[arcos];
		loadList = new double[arcos];
		timeList = new double[arcos];
		Arcs = new int[arcos][2];
		int arc = 0;
		for (int i = 0; i <= n; i++) {
			for (int j = 0; j <= n; j++) {
				// Redondio a un decimal pero está x10 para que quede entero
				// para el SP
				double d_ij = Math.sqrt(Math.pow((x[i] - x[j]), 2)	+ Math.pow((y[i] - y[j]), 2));
				double dINT = Math.floor(d_ij*10)/10;
				distance[i][j] = dINT;
				distance[j][i] = dINT;

				if(dINT<GraphManager.minCost){
					GraphManager.minCost = dINT;
				}
				if(dINT>GraphManager.maxCost){
					GraphManager.maxCost = dINT;
				}
				
				cost[i][j] = dINT;
				cost[j][i] = dINT;
				if ((i==0 && (i!=j))  ||((i!=j) && tw_a[i] + service[i] + dINT <= tw_b[j]) ) {
					distList[arc] = dINT;
					costList[arc] = cost[i][j];
					Arcs[arc][0] = i;
					Arcs[arc][1] = j;
					timeList[arc] = dINT + service[i];
					loadList[arc] = demand[j];
					int a1 = arc;
					G.customers[i].magicIndex.add(a1);
					
					// System.out.println(i+ " " + j + " " +loadList[arc] + " "+ timeList[arc]  + " " + distList[arc]);
					//System.out.println(arc);
					arc++;
				}
				/*if (i==0||j==0||tw_a[j] + service[j] + dINT <= tw_b[i]) {
					int narc = arc + 1;
					distList[arc] = dINT;
					costList[arc] = cost[j][i];
					Arcs[arc][0] = j;
					Arcs[arc][1] = i;
					timeList[arc] = dINT + service[j];
					loadList[arc] = demand[i];
					int a1 = arc;
					G.customers[j].magicIndex.add(a1);
					arc++;
				}
				
				*/
				//System.out.println("i: " + i + "  j: " +j + " :   "+cost[i][j]);
				AverageArc += dINT;
				SAVES[i][j] = distance[0][i]+distance[j][0]-distance[i][j];
				SAVES[j][i] = SAVES[i][j];
				if(dINT<LowerOutgoingArc[i])
				{
					LowerOutgoingArc[i]=dINT;
				}
				if(dINT<LowerOutgoingArc[j])
				{
					LowerOutgoingArc[j]=dINT;
				}
				
			}
		}
		
		NumArcs =arc;
		
		
		AverageArc = AverageArc/NumArcs;
//		System.out.println("\t"+ AverageArc);
		for (int i = 0; i < n; i++) {
			G.getCustomers()[i].autoSort();
		}
	
	}

private void readCap_and_Vels() throws IOException {
		File file = new File("Solomon Instances/AA_Data.txt");
		BufferedReader bufRdr = new BufferedReader(new FileReader(file));
		for (int i = 0; i < 6; i++) {
			String line = bufRdr.readLine(); //READ Num Nodes
			String[] spread = line.split(":");
			if(type.equals(spread[0])){
				int serie = Integer.parseInt(spread[1]);
				if (series-serie<50) {
					k=Integer.parseInt(spread[3]);
					cap=Integer.parseInt(spread[2]);
					return;
				}
			}
		}
		
		
	}
	
	public static void calDualBound() {
		GraphManager.DualBound = Double.POSITIVE_INFINITY;
		for (int i = 0; i < NumArcs; i++) {
			if (timeList[i] != 0
					&& costList[i] / timeList[i] <= GraphManager.DualBound) {
				GraphManager.DualBound = costList[i] / timeList[i];
			}
		}
	}
}
