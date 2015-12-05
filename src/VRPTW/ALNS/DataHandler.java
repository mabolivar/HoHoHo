package VRPTW.ALNS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;



public class DataHandler {

	public static final int numLabels = 10;
	public static Random r = new Random(0);
	public static double destroy = 0.35;
	public static final double infinity = Double.POSITIVE_INFINITY;
	public static double AverageArc = 0;
	public static double brPenalization =100000.0;
	String CvsInput;
	
	int NumArcs;
	int LastNode;
	int Source;
	//static int[][] Arcs;
	//static ArrayList<EdgePulse> Arcs;
//	static double speed = 1.0;
	
	
	static int[] distList;
	
	static int n;
	static int k;
	static int cap;
	static int tripDelimiter;
	static int dropTime;

	
	static int[] demand;// demand
	static int[] service;//service duration
	static int[] tw_a;//time window begin
	static int[] tw_b;//time window end
	
	static double[] x; 
	static double[] y;
	
	
	
	private VRPTW_Manager G;
	
	static double[][] Distance;
	static double[][] saves;
	

	static double[] LowerOutgoingArc;

	public static double primalBound;
	private String  type;
	private int series;

	public DataHandler(String instanceFile, String name, int instance) {
		CvsInput = instanceFile;
		Source = 0;
		type = name;
		series=instance;
		
	}
	public void ReadSolomon() throws NumberFormatException, IOException {
		
		readCap_and_Vels();
		File file = new File(CvsInput);
		BufferedReader bufRdr = new BufferedReader(new FileReader(file));
		String line = bufRdr.readLine(); //READ Num Nodes
		
		//k= Integer.parseInt(spread[1])+5;
		n = 100;
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
		
		G = new VRPTW_Manager(n+1); 
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
		
		
		Distance = new double[n + 1][n + 1];
		saves = new double[n+1][n+1];
		
		for (int i = 0; i <= n; i++) {
			for (int j = i; j <= n; j++) {
				// Redondio a un decimal pero está x10 para que quede entero
				// para el SP
				double d_ij = Math.sqrt(Math.pow((x[i] - x[j]), 2)	+ Math.pow((y[i] - y[j]), 2));
				double dINT = Math.floor(d_ij*10)/10;
				Distance[i][j] = dINT;
				Distance[j][i] = dINT;
				AverageArc += 2*dINT;
				saves[i][j] = Distance[0][i]+Distance[j][0]-Distance[i][j];
				saves[j][i] = saves[i][j];
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
		AverageArc = AverageArc/Math.pow(n+1, 2);
		
	
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
					k=30;//Integer.parseInt(spread[3])+100;
					cap=Integer.parseInt(spread[2]);
				}
			}
		}
		
		
	}
	public void ReadC() throws NumberFormatException, IOException {
		
		
		File file = new File(CvsInput);
		BufferedReader bufRdr = new BufferedReader(new FileReader(file));
		String line = bufRdr.readLine(); //READ Num Nodes
		
		String[] spread = line.split(" "); 
		//k= (int)(1.5*Integer.parseInt(spread[1]));
		k= Integer.parseInt(spread[1]);
		n = Integer.parseInt(spread[2]);
		
		line = bufRdr.readLine(); //READ second line D and Q
		spread = line.split(" ");
		cap = Integer.parseInt(spread[1]);
		tripDelimiter = Integer.parseInt(spread[0]);
		
		
		G = new VRPTW_Manager(n+1); 
		int arcos = (n+1)*(n+1)-(n+1);
		
	
		x = new double[n+1]; 
		y = new double[n+1]; 
		demand = new int[n+1]; 
		service =  new int[n+1];
		tw_a =  new int[n+1];
		tw_b =  new int[n+1];
		LowerOutgoingArc = new double[n+1];
		
		line = bufRdr.readLine(); //READ depot
		spread = line.split(" "); 
		
		x[0] = (Double.parseDouble(spread[1]));
		y[0] = (Double.parseDouble(spread[2]));
		service[0] = (int)(Double.parseDouble(spread[3]));
		demand[0]= (int)(Double.parseDouble(spread[4]));
		tw_a[0]= -Integer.parseInt(spread[8]);;
		tw_b[0]= Integer.parseInt(spread[8]);
		
		G.addVertex(new Node(Integer.parseInt(spread[0]),demand[0],service[0], tw_a[0],tw_b[0]));
		int custumerNumber = 1;
		
		while (custumerNumber<=n) {
			line = bufRdr.readLine();
			spread = line.split(" ");
			x[custumerNumber] = (Double.parseDouble(spread[1]));
			y[custumerNumber] = (Double.parseDouble(spread[2]));
			service[custumerNumber] = (int)(Double.parseDouble(spread[3]));
			demand[custumerNumber]= (int)(Double.parseDouble(spread[4]));
			tw_a[custumerNumber]= Integer.parseInt(spread[8]);
			tw_b[custumerNumber]= Integer.parseInt(spread[9]);
			G.addVertex(new Node(Integer.parseInt(spread[0]),demand[custumerNumber],service[custumerNumber], tw_a[custumerNumber],tw_b[custumerNumber]));
			custumerNumber++;
		}
		
		
		Distance = new double[n + 1][n + 1];
		saves = new double[n+1][n+1];
		
		for (int i = 0; i <= n; i++) {
			for (int j = i; j <= n; j++) {
				// Redondio a un decimal pero está x10 para que quede entero
				// para el SP
				double d_ij = Math.sqrt(Math.pow((x[i] - x[j]), 2)	+ Math.pow((y[i] - y[j]), 2));
				double dINT = Math.floor(d_ij*10.0)/10.0;
				Distance[i][j] = dINT;
				Distance[j][i] = dINT;
				AverageArc += 2*dINT;
				saves[i][j] = Distance[0][i]+Distance[j][0]-Distance[i][j];
				saves[j][i] = saves[i][j];
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
		AverageArc = AverageArc/Math.pow(n+1, 2);
		
	
	}

}
