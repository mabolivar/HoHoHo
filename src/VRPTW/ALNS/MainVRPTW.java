package VRPTW.ALNS;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;


public class MainVRPTW {
	
	static String MEGASTRING = "";
	private LNS_VRPTW generalHeuristic;
	
	public MainVRPTW(String iniFile, String type, int series) {
		
		String ini = iniFile;
		
		String file = "Solomon Instances/";
		String name = "R";
		String pre = ".txt";
		int instance = 101;
		int corridas =1;
		double[] resultados = new double[corridas];
		double rAverage = 0;
		double tAverage = 0;
		double min = 999999;
		for (int t = 0; t < corridas; t++) {
			ArrayList<Integer>[] rutas = null;
			try {
				
				if(ini==null && type == null){
					ini = file + name+instance + pre;
				}else{
					name = type;
					instance = series;
					ini = file + name+instance + pre;
				}
				System.out.print("Run " +( t+1));
				long seed = (long)Math.exp(t+1);
				System.out.println("   ALNS Heuristic: " + ini  );
//				System.out.println("Seed: " + seed);
				
				DataHandler.r.setSeed(seed);// Cambiar con el for...
				DataHandler data = new DataHandler(ini, name, instance);
				data.ReadSolomon();
				double cputime = System.currentTimeMillis();
				
				InitialInsertionHeuristic ii = new InitialInsertionHeuristic();
				ii.getInitialSol2(VRPTW_Manager.routes,VRPTW_Manager.routesLoad, VRPTW_Manager.customers,VRPTW_Manager.requestBank);
				VRPTW_Manager.calculateFO();
//				System.out.println(VRPTW_Manager.requestBank);
				generalHeuristic = new LNS_VRPTW();
				generalHeuristic.heuristic();
				cputime =(System.currentTimeMillis()-cputime)/1000.0;
				if (t>0) {
					if (generalHeuristic.getBestFO()<min) {
						min = generalHeuristic.getBestFO();
					}
					resultados[t] = generalHeuristic.getBestFO();
					rAverage+=generalHeuristic.getBestFO();
					tAverage += cputime;
				}
				rutas = new ArrayList[DataHandler.k];
				for (int i = 0; i < rutas.length; i++) {
					rutas[i] = new ArrayList<>();
					ArrayList<Node>r = generalHeuristic.getBestSol()[i];
					for (int j = 0; j < r.size(); j++) {
						rutas[i].add(r.get(j).id);
					}
				}
				System.out.println("CPU time:" + cputime);
//				System.out.println("----------------------------------------------------------------------------------------------");
				
			
			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			//@SuppressWarnings("unused")
			//Main m = new Main(rutas);

		}
		corridas--;
		tAverage = tAverage/corridas;
		rAverage =rAverage/corridas;
		double desvest = 0;
		for (int i = 1; i < corridas; i++) {
			desvest += (resultados[i]-rAverage);
		}
		/*desvest = desvest/(corridas-1);
		corridas = corridas/5;
		System.out.println("Average "+ rAverage + " Desvest: " + desvest);
		System.out.println("BestSol " + min);
		System.out.println(tAverage);
		PrintWriter pw;
		try {
			//pw = new PrintWriter("cols " +  instance + ".txt");
			//pw.write(MEGASTRING);
			//pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
	public static void main(String[] args) {
		MainVRPTW mTW = new MainVRPTW(null, null, 0);
		
	}
	public Hashtable<String, Double> getHeuTourPoolDist() {
		return generalHeuristic.getHeuPoolDist();
	}
	public ArrayList<String> getHeuTourPool() {
		return generalHeuristic.getKeys();
	}
	public Hashtable<String, Double> getHeuTourPoolDistBest() {
		return generalHeuristic.getHeuPoolDistBest();
	}
	public ArrayList<String> getHeuTourPoolBest() {
		return generalHeuristic.getKeysBest();
	}
	public ArrayList<ArrayList<Integer>> getRoutes() {
		ArrayList<ArrayList<Integer>> sol = new ArrayList<>();
		for (int i = 0; i < generalHeuristic.Broutes.length; i++) {
			ArrayList<Integer> dummy = new ArrayList<>();
			for (int j = 0; j < generalHeuristic.Broutes[i].size(); j++) {
				dummy.add(generalHeuristic.Broutes[i].get(j).id);
			}
			sol.add(dummy);
		}
		
		ArrayList<Integer> dummy = new ArrayList<>();
		dummy.add(0);
		dummy.add(0);
		sol.add(dummy);
		sol.add(dummy);
		return sol;
	}
}
