package VRPTW.ALNS;

import java.util.ArrayList;
import java.util.Hashtable;




import javax.swing.text.html.MinimalHTMLWriter;

import Utilities.VRPTW.Clonetor;

public class LNS_VRPTW {
	
	
	private Hashtable<String, Double> routesPoolDist;
	private ArrayList<String> pool;
	private int lastPoolSize;
	private Hashtable<String, Double> routesPoolDistBest;
	private ArrayList<String> poolBest;
	private int lastPoolSizeBest;
	
	static double twWidthTolerance = 0.0;
	private int numberOfInsertionHeu;
	private int numberOfRemovalHeu;
	private int iterNonInprovement = 0;
	private int currentInsertionHeu;
	private int currentRemovalHeu;

	private double totalInsertionScore;
	private double totalRemovalScore;

	private double[] insertionScores;
	private double[] removalScores;
	private double[] insertionProb;
	private double[] removalProb;

	// s'-------------------------------------------
	private ArrayList<Node>[] routes;
	private double[] routesDist;
	private int[] routesLoad;
	private Node[] customers;
	private static ArrayList<Node> requestBank;
	static double totalDistance;
	static double FO;
	// ---------------------------------------------

	// s_best-------------------------------------------
	public ArrayList<Node>[] Broutes;
	public double[] BroutesDist;
	private int[] BroutesLoad;
	private Node[] Bcustomers;
	private static ArrayList<Node> BrequestBank;
	static double BtotalDistance;
	static double BFO;

	// ---------------------------------------------

	public LNS_VRPTW() {
		
		pool = new ArrayList<>();
		
		
		routesPoolDist = new Hashtable<>(1000);
		poolBest = new ArrayList<>();
		routesPoolDistBest = new Hashtable<>(1000);
		numberOfInsertionHeu = 5;
		totalInsertionScore = 10;
		
		insertionScores = new double[numberOfInsertionHeu];
		insertionProb = new double[numberOfInsertionHeu];
		
		insertionScores[0] = 0;
		insertionScores[1] = 0;
		insertionScores[2] = 1;
		insertionScores[3] = 7;
		insertionScores[4] = 2;
		
		
		for (int i = 0; i < insertionScores.length; i++) {
			insertionProb[i] = insertionScores[i] / totalInsertionScore;
		}
		numberOfRemovalHeu = 2;
		totalRemovalScore = 100;
		removalScores = new double[numberOfRemovalHeu];
		removalProb = new double[numberOfRemovalHeu];
		removalScores[0] = 60;
		removalProb[0] = removalScores[0] / totalRemovalScore;
		removalScores[1] = 40;
		removalProb[1] = removalScores[1] / totalRemovalScore;

	}

	/**
	 * General meta heuristic
	 */
	public void heuristic() {

		int iterations = 0;
		initializeS_best();
		boolean nonImproveReached=false;
		int maxIters =10000;
		while (iterations <= maxIters) {
			genCopy(); // generates a copy
			remove();
			upDateRoutes();
			insert();
			lastFixChance();
		
			if (isBetter(1)) {
				
				replaceNewBest();
				upDateScores();
				fillPoolBest();
				//System.out.println(iterations);
				iterNonInprovement =0;
				double dismiss = DataHandler.destroy>0.35?0.05:0.01;
				DataHandler.destroy  = Math.max(0.05, DataHandler.destroy-dismiss);
//				System.out.println(iterations + "  fo=" +FO +"  destroy->" + DataHandler.destroy);
			}else{
				iterNonInprovement++;
				
			}
			if(nonImproveReached){
//				DataHandler.destroy = 0.35;
//				nonImproveReached=false;
			}
			if (iterNonInprovement >50) {
				nonImproveReached = true;
				iterNonInprovement = 0;
				if(DataHandler.destroy >=0.35 || DataHandler.destroy == 0.05){
					DataHandler.destroy = 0.5;
				}
			}
			
			if (isBetter(1.2)) {
				
				replaceOriginal();
			}
			iterations++;

		}
		System.out.print(BFO+ "\t");
//		System.out.println();
//		System.out.println("Cuantas "  + pool.size());
//		print2();
//		Sort(pool);
	}
	
	private void fillPool() {
		for (int i = 0; i < DataHandler.k; i++) {
			String keyS = routes[i].toString();
			
			if ( routes[i].size()>2 && !routesPoolDist.containsKey(keyS)) {
				pool.add(keyS);
				routesPoolDist.put(keyS, routesDist[i]);
			}
		}
		
	}
	private void fillPoolBest() {
		for (int i = 0; i < DataHandler.k; i++) {
			String keyS = routes[i].toString();
			
			if ( routes[i].size()>2 && !routesPoolDistBest.containsKey(keyS)) {
				poolBest.add(keyS);
				routesPoolDistBest.put(keyS, routesDist[i]);
			}
		}
		
	}
	
	/**
	 * Print the current solution
	 */
	private void print() {
		for (int i = 0; i < DataHandler.k; i++) {
			upDateRoutes(i);
			System.out.println(routes[i]);
		}
		System.out.println("RB" + requestBank);

		System.out.println(totalDistance);
		System.out.println(FO);

	}
	/**
	 * Print the best solution
	 */
	private void print2() {
		int realRoutes=0;
		for (int i = 0; i < DataHandler.k; i++) {
			if(BroutesDist[i]>0){
				realRoutes++;
				System.out.println(i + " Cost: "+ BroutesDist[i] + " " + Broutes[i]);
			}
		}
		System.out.println("routes: " + realRoutes +  "\tRB" + BrequestBank);
		
		System.out.println(BtotalDistance);
		System.out.println(BFO);
		System.out.println("Insertion stats");
		System.out.println("Hue ins 1:   " + insertionProb[0]);
		System.out.println("Hue ins 2:    " + insertionProb[1]);
		System.out.println("Hue ins 3:    " + insertionProb[2]);
		System.out.println("Hue ins 4:    " + insertionProb[3]);
		System.out.println("Hue ins 5:    " + insertionProb[4]);
		System.out.println("Deletion stats");
		System.out.println("Hue del 1:   " + removalProb[0]);
		System.out.println("Hue del 2:    " + removalProb[1]);
		

	}
	
	private void colPrinter()
	{
		for (int i = 0; i < DataHandler.k; i++) {
			if( routes[i].size()>2 && routesDist[i]<=240){
				MainVRPTW.MEGASTRING +=""+ routesDist[i] + ", " + routes[i]+"\n";
			}
		}
	}

	/**
	 * Procedure to update all the routes.
	 * Main information is updated for each node 
	 * Data structure routes and global variables are updated 
	 */
	private void upDateRoutes() {
		Node customer = null;

		for (int k = 0; k < routes.length; k++) {
			int load = 0;
			for (int i = 1; i < routes[k].size(); i++) {
				customer = routes[k].get(i);
				customer.arrivalTime = routes[k].get(i - 1).exitTime
						+ DataHandler.Distance[routes[k].get(i - 1).id][customer.id];
				customer.cumulativeDist = routes[k].get(i - 1).cumulativeDist
						+ DataHandler.Distance[routes[k].get(i - 1).id][customer.id];
				customer.exitTime = Math.max(customer.arrivalTime,
						customer.tw_a) + customer.service;
				customer.route = k;
				customer.visited = i;
				load += customer.demand;
			}
			routesLoad[k] = load;
			routesDist[k] = customer.cumulativeDist;
		}
		totalDistance = 0;
		for (int i = 0; i < routesDist.length; i++) {
			totalDistance += routesDist[i];
		}
		FO = totalDistance + DataHandler.brPenalization * requestBank.size();

	}
	/**
	 * Procedure to update a single route given a a parameter.
	 * Main information is updated for each node 
	 * Data structure routes and global variables are updated 
	 * @param k route to be updated
	 */
	private void upDateRoutes(int k) {
		int load = 0;
		double dist = 0;
		Node customer = null;
		for (int i = 1; i < routes[k].size(); i++) {
			customer = routes[k].get(i);
			customer.arrivalTime = routes[k].get(i - 1).exitTime
					+ DataHandler.Distance[routes[k].get(i - 1).id][customer.id];
			customer.cumulativeDist = routes[k].get(i - 1).cumulativeDist
					+ DataHandler.Distance[routes[k].get(i - 1).id][customer.id];
			customer.exitTime = Math.max(customer.arrivalTime, customer.tw_a)
					+ customer.service;
			customer.route = k;
			customer.visited = i;
			load += customer.demand;
		}
		routesLoad[k] = load;
		routesDist[k] = customer.cumulativeDist;
		totalDistance = 0;
		for (int i = 0; i < routesDist.length; i++) {
			totalDistance += routesDist[i];
		}
		FO = totalDistance + DataHandler.brPenalization * requestBank.size();

	}
	
	/**
	 * This procedure update the heuristics scores and
	 * selection probabilities
	 */
	private void upDateScores() {
		insertionScores[currentInsertionHeu]++;
		totalInsertionScore++;
		removalScores[currentRemovalHeu]++;
		totalRemovalScore++;
		for (int i = 0; i < insertionProb.length; i++) {
			insertionProb[i] = insertionScores[i] / totalInsertionScore;
		}
		for (int i = 0; i < removalProb.length; i++) {
			
			removalProb[i] = removalScores[i] / totalRemovalScore;
		}
	
	}

	/**
	 * the insertion procedure select the insertion heuristic
	 */
	private void insert() {
		double prob = DataHandler.r.nextDouble();
		if ( prob< insertionProb[0]) {
			insertHeu1();
			currentInsertionHeu = 0;//Afterwards updated
		}else if(prob<insertionProb[0]+insertionProb[1]) {
			insertHeu2();
			currentInsertionHeu = 1;
		}
		else if(prob<insertionProb[0]+insertionProb[1]+insertionProb[2]) {
			insertHeu3(1);
			currentInsertionHeu = 2;
			
		}
		else if(prob<insertionProb[0]+insertionProb[1]+insertionProb[2]+insertionProb[3]) {
			insertHeu3(2);
			currentInsertionHeu = 3;
			
		}
		else {
			insertHeu3(3);
			currentInsertionHeu = 4;
//			System.out.println(22222);
		}

	}

	

	/**
	 * Cluster insertion
	 */
	private void insertHeu1() {
		boolean sePuede = true;
		for (int ib = 0; ib < requestBank.size(); ib++) {
			Node rc = requestBank.get(ib);
			int whichCluster = ClustersManager.whichCluster[rc.id];
			int[] nodesRouteInCluster = new int[DataHandler.k];
			int routeClust = -1;
			int routeClust2 = -1;
			int maxNumber = 0;
			//More alike routes to the selected cluster
			for (int j = 0; j < ClustersManager.clusters[whichCluster].size(); j++) {
				Node nc = customers[ClustersManager.clusters[whichCluster].get(j)];
				if (nc.route != -1) {
					int ncRoute = nc.route;
					nodesRouteInCluster[ncRoute]++;
					if (nodesRouteInCluster[ncRoute] > maxNumber) {
						maxNumber = nodesRouteInCluster[ncRoute];
						routeClust2 = routeClust;
						routeClust = ncRoute;
					}
				}
			}

			
			double minDelta = Double.POSITIVE_INFINITY;
			int minRoute = -1;
			int minPos = -1;
			routeClust = routeClust == -1 ? DataHandler.r.nextInt(DataHandler.k) : routeClust;
			routeClust2 = routeClust2 == -1 ? routeClust : routeClust2;
			for (int k = 0; k < 2; k++) {
				int rk = k == 0 ? routeClust : routeClust2;
				if (routesLoad[rk] + rc.demand <= DataHandler.cap) {
					ArrayList<Node> r = routes[rk];
					for (int i = 1; i < r.size(); i++) {
						sePuede = true;

						double foDelta = DataHandler.Distance[r.get(i - 1).id][rc.id]
								+ DataHandler.Distance[rc.id][r.get(i).id]
								- DataHandler.Distance[r.get(i - 1).id][r
										.get(i).id];
						if (foDelta < minDelta) {
							double arrival_i = r.get(i - 1).exitTime
									+ DataHandler.Distance[r.get(i - 1).id][rc.id];
							double exit_i = Math.max(arrival_i, rc.tw_a)
									+ rc.service;
							if (arrival_i <= rc.tw_b) {// feasible TW for rc
								rc.arrivalTime = arrival_i;
								rc.exitTime = exit_i;
								r.add(i, rc);// rc is temporally added, if the
												// tour is feasible, then is
												// temporal
								for (int j = i + 1; j < r.size() && sePuede; j++) {
									double arrival_j = exit_i
											+ DataHandler.Distance[r.get(j - 1).id][r.get(j).id];
									if (arrival_j > r.get(j).tw_b) {
										sePuede = false;
										r.remove(i);
										rc.arrivalTime = -1;
										rc.exitTime = -1;
									} else {
										exit_i = Math.max(arrival_j,
												r.get(j).tw_a)
												+ r.get(j).service;
									}
								}
								if (sePuede) {
									r.remove(i);
									rc.arrivalTime = -1;
									rc.exitTime = -1;
									minDelta = foDelta;
									minRoute = rk;
									minPos = i;
								}
							}
						}
					}
				}
			}
			if (minRoute != -1) {
				//There is a feasible insertion
				requestBank.remove(ib);
				ib--;
				routes[minRoute].add(minPos, rc);
				routesLoad[minRoute] += rc.demand;
				upDateRoutes(minRoute);
				swapHeu(minRoute);
			}
		}

	}
	
	
	/**
	 * Basic greedy heuristic
	 */
	private void insertHeu2() {
		boolean sePuede = true;
		for (int ib = 0; ib < requestBank.size(); ib++) {
			Node rc = requestBank.get(ib);
			double minDelta = Double.POSITIVE_INFINITY;
			int minRoute = -1;
			int minPos = -1;
			for (int k = 0; k < DataHandler.k; k++) {
				if (routesLoad[k] + rc.demand <= DataHandler.cap) {
					ArrayList<Node> r = routes[k];
					for (int i = 1; i < r.size(); i++) {
						sePuede = true;

						double foDelta = DataHandler.Distance[r.get(i - 1).id][rc.id]+ DataHandler.Distance[rc.id][r.get(i).id]
								- DataHandler.Distance[r.get(i - 1).id][r.get(i).id];
						if (foDelta < minDelta) {
							double arrival_i = r.get(i - 1).exitTime	+ DataHandler.Distance[r.get(i - 1).id][rc.id];
							double exit_i = Math.max(arrival_i, rc.tw_a)+ rc.service;
							if (arrival_i <= rc.tw_b) {// feasible TW for rc
								rc.arrivalTime = arrival_i;
								rc.exitTime = exit_i;
								r.add(i, rc);// rc is temporally added, if the tour is feasible, then is temporal
								for (int j = i + 1; j < r.size() && sePuede; j++) {
									double arrival_j = exit_i	+ DataHandler.Distance[r.get(j - 1).id][r.get(j).id];
									if (arrival_j > r.get(j).tw_b) {
										sePuede = false;
										r.remove(i);
										rc.arrivalTime = -1;
										rc.exitTime = -1;
							
									} else {
										exit_i = Math.max(arrival_j,r.get(j).tw_a)+ r.get(j).service;
									}
								}
								if (sePuede) {
									//Save the best insertion so far
									r.remove(i);
									rc.arrivalTime = -1;
									rc.exitTime = -1;
									minDelta = foDelta;
									minRoute = k;
									minPos = i;
								}
							}
						}
					}
				}
			}
			if (minRoute != -1) { 
				//If some insertion is feasible
				requestBank.remove(ib);
				ib--;
				routes[minRoute].add(minPos, rc);
				routesLoad[minRoute] += rc.demand;
				upDateRoutes(minRoute);
				swapHeu(minRoute);
			}
		}
	}
	
	/**
	 * k-Regret heuristic
	 */
	private void insertHeu3(int mRegrets) {
		
		while (requestBank.size() > 0) {
			Node max_Request = null;
			int maxIb = -1;
			boolean feasible = true;
			double max_Cstar = 0; // c*
			int[] minRoute = new int[requestBank.size()];
			int[] minPos = new int[requestBank.size()];
			for (int ib = 0; ib < requestBank.size(); ib++) {
				Node rc = requestBank.get(ib);
				double[] regrets = new double[mRegrets];
				for (int i = 0; i < regrets.length; i++) {
					regrets[i] = Double.POSITIVE_INFINITY;
				}
				
				
				for (int k = 0; k < DataHandler.k; k++) {
					double minDelta = Double.POSITIVE_INFINITY;
					if (routesLoad[k] + rc.demand <= DataHandler.cap) { // Check route capacity
						ArrayList<Node> r = routes[k];
						for (int i = 1; i < r.size(); i++) {
							feasible = true;

							//foDelta  => Delta f_(i,k)
							double foDelta = DataHandler.Distance[r.get(i - 1).id][rc.id]+ DataHandler.Distance[rc.id][r.get(i).id]
									- DataHandler.Distance[r.get(i - 1).id][r.get(i).id];
							if (foDelta < minDelta) {
								
								double arrival_i = r.get(i - 1).exitTime+ DataHandler.Distance[r.get(i - 1).id][rc.id];
								double exit_i = Math.max(arrival_i, rc.tw_a)+ rc.service;
								
								if (arrival_i <= rc.tw_b) {// feasible TW for rc
									rc.arrivalTime = arrival_i;
									rc.exitTime = exit_i;
									r.add(i, rc);// rc is temporally added, if
													// the tour is feasible,
													// then is temporal
									for (int j = i + 1; j < r.size()&& feasible; j++) {
										double arrival_j = exit_i+ DataHandler.Distance[r.get(j - 1).id][r.get(j).id];
										if (arrival_j > r.get(j).tw_b) {
											feasible = false;
											r.remove(i);
											rc.arrivalTime = -1;
											rc.exitTime = -1;
										} else {
											exit_i = Math.max(arrival_j,r.get(j).tw_a)+ r.get(j).service;
										}
									}
									if (feasible) {
										// Save the best insertion so far
										r.remove(i);
										rc.arrivalTime = -1;
										rc.exitTime = -1;
										minDelta = foDelta;
										if (foDelta<regrets[0]) {
											minRoute[ib] = k;
											minPos[ib] = i;
										}
										
									}
								}
							}
						}
					}
					for (int i = 0; i < regrets.length; i++) {
						if (minDelta<regrets[i]) {
							for (int j = regrets.length-1; j >i; j--) {
								regrets[j]=regrets[j-1];
							}
							regrets[i] = minDelta;
							i+=100;
						}
					}
				}
				double  cstar = 0;
				for (int i = 1; i < regrets.length; i++) {
					cstar += (regrets[i] - regrets[0]);
				}
				if (cstar >= max_Cstar) {
					max_Cstar = cstar;
					maxIb = ib;
					max_Request = rc;
				}
			}
			if (maxIb != -1) {
				// If some insertion is feasible
				requestBank.remove(maxIb);
				routes[minRoute[maxIb]].add(minPos[maxIb], max_Request);
				routesLoad[minRoute[maxIb]] += max_Request.demand;
				upDateRoutes(minRoute[maxIb]);
				swapHeu(minRoute[maxIb]);
			}else{
				return;
			}
			
		}

	}

	/**
	 * The remove procedure call a removal heuristic
	 */
	private void remove() {
		// TODO Auto-generated method stub

		if (DataHandler.r.nextDouble() < removalProb[0]) {
			removeHeu1();
			currentRemovalHeu = 0;
		} else {
			removeHeu2();
			currentRemovalHeu = 1;
		}
	}

	/**
	 * Worst (customer) removal
	 */
	private void removeHeu1() {
		// TODO Auto-generated method stub

		int q = 1 + DataHandler.r.nextInt((int) (DataHandler.n * DataHandler.destroy) - 0);

		while (q > 0) {
			double maxCost_is = 0;
			int route = -1;
			int id = -1;
			for (int k = 0; k < routes.length; k++) {
				ArrayList<Node> r = routes[k];
				for (int i = 1; i < r.size() - 1; i++) {
					double delta = -DataHandler.Distance[r.get(i).id][r.get(i + 1).id]
							- DataHandler.Distance[r.get(i - 1).id][r.get(i).id]
							+ DataHandler.Distance[r.get(i - 1).id][r.get(i + 1).id];
					if (-delta > maxCost_is) {
						maxCost_is = -delta;
						route = k;
						id = r.get(i).id;
					}
				}
			}
			if (route != -1 && customers[id].route != -1) {
				// System.out.println("sale " + id + " de " + route);
				customers[id].arrivalTime = -1;
				customers[id].exitTime = -1;
				customers[id].cumulativeDist = -1;
				customers[id].route = -1;
				customers[id].visited = -1;
				boolean aa = routes[route].remove(customers[id]);
				// System.out.println(aa);
				routesLoad[route] -= customers[id].demand;
	
				requestBank.add(DataHandler.r.nextInt(requestBank.size() + 1), customers[id]);
			} else {
				q = 0;
			}
			q--;
		}
	}

	/**
	 * Random removal
	 */
	private void removeHeu2() {
		int q =1 + DataHandler.r
				.nextInt((int) (DataHandler.n * DataHandler.destroy) - 0);
//		q = 2;//(int) (DataHandler.n * DataHandler.destroy);
		while (q > 0) {
			int id = 1 + DataHandler.r.nextInt(DataHandler.n);
			if (customers[id].route != -1) {
				int rr = customers[id].route;
				customers[id].arrivalTime = -1;
				customers[id].exitTime = -1;
				customers[id].cumulativeDist = -1;
				customers[id].route = -1;
				customers[id].visited = -1;
				routes[rr].remove(customers[id]);
				routesLoad[rr] -= customers[id].demand;
				requestBank.add(customers[id]);
				// System.out.print(id+", ");
			} else {
				q = 0;
			}
			q--;
		}
	}

	/**
	 * Local search procedure 
	 * Performs swaps  (2 opts) over the route k.  a node is subject to be swap 
	 * if the vehicle reaches too early
	 * @param k route to optimize. if k<0, a random route is selected.
	 */
	private void swapHeu(int k) {
		if (k < 0) {
			k = DataHandler.r.nextInt(DataHandler.k);
		}

		// System.out.println("selected route " + k );
		ArrayList<Node> r = routes[k];
		for (int i = 1; i < r.size() - 1; i++) {
			double gap_i = r.get(i).tw_b - r.get(i).arrivalTime;
//			if (gap_i / r.get(i).tw_w > twWidthTolerance) {
				for (int j = i + 1; j < r.size() - 1; j++) {
//					if (!(i == 1 && j == routes[k].size() - 2)
//							&& !(j == 1 && i == routes[k].size() - 2)) {
						double gap_j = r.get(j).tw_b - r.get(j).arrivalTime;
						int i_arc1 = i - 1;
						int j_arc1 = i;
						int l_arc2 = j;
						int k_arc2 = j + 1;
						double delta = -(DataHandler.Distance[r.get(i_arc1).id][r.get(j_arc1).id] - DataHandler.Distance[r.get(l_arc2).id][r.get(k_arc2).id])
								+ DataHandler.Distance[r.get(i_arc1).id][r.get(l_arc2).id]+ DataHandler.Distance[r.get(j_arc1).id][r.get(k_arc2).id];
						
						if (delta < 0){// && gap_j < gap_i) { //Profitable swap?
							
							if (checkSwapFeasibility(k, i, j)) { //Check the swap feasibility
								Node n_i = r.get(i);
								r.set(i, r.get(j));
								r.set(j, n_i);
								upDateRoutes(k);
//								System.out.println("algo hizo");
								return;
							}
						}
//					}

				}
//			}

		}

	}

	/**
	 * Fix operator, try to fix a node in the request bank
	 * @param rc node to be fixed
	 * @return true, if the node is inserted
	 */
	private boolean fix(Node rc) {
		boolean sePuede = true;
		for (int k = 0; k < DataHandler.k; k++) {
			if (routesLoad[k] + rc.demand <= DataHandler.cap) {// &&
																// k!=rc.route){
				ArrayList<Node> r = routes[k];
				for (int i = 1; i < r.size(); i++) {
					sePuede = true;
					double arrival_i = r.get(i - 1).exitTime
							+ DataHandler.Distance[r.get(i - 1).id][rc.id];
					double exit_i = Math.max(arrival_i, rc.tw_a) + rc.service;
					if (arrival_i <= rc.tw_b) {// feasible TW for rc
						rc.arrivalTime = arrival_i;
						rc.exitTime = exit_i;
						r.add(i, rc);// rc is temporally added, if the tour is
										// feasible, then is temporal
						for (int j = i + 1; j < r.size() && sePuede; j++) {
							double arrival_j = exit_i
									+ DataHandler.Distance[r.get(j - 1).id][r.get(j).id];
							if (arrival_j > r.get(j).tw_b) {
								sePuede = false;
								r.remove(i);
								rc.arrivalTime = -1;
								rc.exitTime = -1;
							} else {
								exit_i = Math.max(arrival_j, r.get(j).tw_a)
										+ r.get(j).service;
							}
						}
						if (sePuede) {
							// System.out.print(requestBank
							// +"en Fix().. removed ? " + rc);
							boolean removed = requestBank.remove(rc);
							// System.out.println(removed);
							upDateRoutes(k);
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * Fix procedure over all the unassigned customers
	 */
	private void lastFixChance() {
		for (int i = 0; i < requestBank.size(); i++) {
			fix(requestBank.get(i));
		}
	}

	/**
	 * check if a swap feasible
	 * @param k route to swap
	 * @param i node r(k,i)
	 * @param j node r(k,j)
	 * @return true if the swap is feasible
	 */
	private boolean checkSwapFeasibility(int k, int i, int j) {
		// TODO Auto-generated method stubint load= 0;
		ArrayList<Node> r = Clonetor.cloneArrayList(routes[k]);
		Node n_i = r.get(i);
		r.set(i, r.get(j));
		r.set(j, n_i);
		int load = 0;
		double dist = 0;

		double exit_i = 0;// exit from depot
		for (int l = 1; l < r.size(); l++) {
			double arrival_j = exit_i
					+ DataHandler.Distance[r.get(l - 1).id][r.get(l).id];
			if (arrival_j > r.get(l).tw_b) {
//				System.out.println(r + " i: " + i  + "  j: " + j+ " inff " + l ); 
				return false;
			} else {
				exit_i = Math.max(arrival_j, r.get(l).tw_a) + r.get(l).service;
			}
		}
		return true;
	}

	/**
	 * Evaluates if the obtained solution is better than the current best
	 * @return true if so
	 */
	private boolean isBetter(double gap) {
		// calculateFOB();

		calculateFO();
		if (FO < BFO*gap) {
			return true;
		}
		return false;
	}

	
	/**
	 * Procedures to keep updated multiple copies
	 */
	
	private void replaceOriginal() {
		// TODO Auto-generated method stub
		VRPTW_Manager.routes = Clonetor.cloneArrayList(routes);
		VRPTW_Manager.routesDist = routesDist.clone();
		VRPTW_Manager.routesLoad = routesLoad.clone();
		for (int i = 0; i < routes.length; i++) {
			ArrayList<Node> r = VRPTW_Manager.routes[i];
			for (int j = 1; j < r.size() - 1; j++) {
				VRPTW_Manager.customers[r.get(j).id] = r.get(j);
			}
		}
		// VRPTW_Manager.customers = Clonetor.cloneArrayList(customers);
		VRPTW_Manager.requestBank = Clonetor.cloneArrayList(requestBank);
		for (int i = 0; i < VRPTW_Manager.requestBank.size(); i++) {
			customers[VRPTW_Manager.requestBank.get(i).id] = VRPTW_Manager.requestBank
					.get(i);
		}
		VRPTW_Manager.FO = FO;
		VRPTW_Manager.totalDistance = totalDistance;
	}

	private void replaceNewBest() {
		Broutes = Clonetor.cloneArrayList(routes);
		BroutesDist = routesDist.clone();
		BroutesLoad = routesLoad.clone();
		Bcustomers = new Node[DataHandler.n + 1];
		for (int i = 0; i < Broutes.length; i++) {
			ArrayList<Node> r = Broutes[i];
			for (int j = 1; j < r.size() - 1; j++) {
				// System.out.println(i + " " +j );
				Bcustomers[r.get(j).id] = r.get(j);
			}
		}
		BrequestBank = Clonetor.cloneArrayList(requestBank);
		for (int i = 0; i < BrequestBank.size(); i++) {
			customers[BrequestBank.get(i).id] = BrequestBank.get(i);
		}
		BFO = FO;
		BtotalDistance = totalDistance;

	}

	private void initializeS_best() {
		Broutes = Clonetor.cloneArrayList(VRPTW_Manager.routes);
		BroutesDist = VRPTW_Manager.routesDist.clone();
		BroutesLoad = VRPTW_Manager.routesLoad.clone();
		Bcustomers = new Node[DataHandler.n + 1];
		for (int i = 0; i < Broutes.length; i++) {
			ArrayList<Node> r = Broutes[i];
			for (int j = 1; j < r.size() - 1; j++) {
				// System.out.println(i + " " +j );
				Bcustomers[r.get(j).id] = r.get(j);
			}
		}
		BrequestBank = Clonetor.cloneArrayList(VRPTW_Manager.requestBank);
		for (int i = 0; i < BrequestBank.size(); i++) {
			Bcustomers[BrequestBank.get(i).id] = BrequestBank.get(i);
		}
		BFO = VRPTW_Manager.FO;
		BtotalDistance = VRPTW_Manager.totalDistance;
	}

	private void genCopy() {
		routes = Clonetor.cloneArrayList(VRPTW_Manager.routes);
		routesDist = VRPTW_Manager.routesDist.clone();
		routesLoad = VRPTW_Manager.routesLoad.clone();
		customers = new Node[DataHandler.n + 1];
		for (int i = 0; i < routes.length; i++) {
			ArrayList<Node> r = routes[i];
			for (int j = 0; j < r.size() - 1; j++) {
				// System.out.println(i + " " +j );
				customers[r.get(j).id] = r.get(j);
			}
		}
		// customers = Clonetor.cloneArrayList(VRPTW_Manager.customers);
		requestBank = Clonetor.cloneArrayList(VRPTW_Manager.requestBank);
		for (int i = 0; i < requestBank.size(); i++) {
			customers[requestBank.get(i).id] = requestBank.get(i);
		}
		totalDistance = VRPTW_Manager.totalDistance;
		FO = VRPTW_Manager.FO;

	}

	public void calculateFO() {
		double sum = 0.0;
		for (int i = 0; i < routes.length; i++) {
			Node last = routes[i].get(routes[i].size() - 1);
			double x = last.cumulativeDist + DataHandler.Distance[last.id][0];
			routesDist[i] = x;
			sum += x;
		}
		totalDistance = sum;
		sum += requestBank.size() * DataHandler.brPenalization;
		FO = sum;
	}
	public double getBestFO(){
		return BFO;
	}
	
	public ArrayList<Node>[] getBestSol(){
		return Broutes;
	}
	/*
	 * public void calculateFOB(){ double sum=0.0; for (int i = 0; i <
	 * Broutes.length; i++) { Node last = Broutes[i].get(Broutes[i].size()-1);
	 * double x = last.cumulativeDist + DataHandler.Distance[last.id][0];
	 * BroutesDist[i] = x; sum+= x; } totalDistance = sum;
	 * sum+=requestBank.size()*100000; FO = sum; }
	 */
	public Hashtable<String, Double> getHeuPoolDistBest(){
		return routesPoolDistBest;
	}
	
	public ArrayList<String> getKeysBest(){
		return poolBest;
	}
	
	public Hashtable<String, Double> getHeuPoolDist(){
		return routesPoolDist;
	}
	
	public ArrayList<String> getKeys(){
		return pool;
	}
	public void Sort(ArrayList<String> set) {
		QS(set, 0, set.size() - 1);
	}

	public int colocar(ArrayList<String> e, int b, int t) {
		int i;
		int pivote;
		double valor_pivote;
		String temp;

		pivote = b;
		//valor_pivote = DataHandler.pi[e[pivote].id] ;
		valor_pivote = routesPoolDist.get(e.get(pivote)) ;
		for (i = b + 1; i <= t; i++) {
			if (  routesPoolDist.get( e.get(i)) < valor_pivote) {
				pivote++;
				temp = e.get(i);
				e.set(i, e.get(pivote));
				e.set(pivote,temp);
			}
		}
		temp =  e.get(b);
		e.set(b, e.get(pivote));
		e.set(pivote,temp);
		return pivote;
	}

	public void QS(ArrayList<String> e, int b, int t) {
		int pivote;
		if (b < t) {
			pivote = colocar(e, b, t);
			QS(e, b, pivote - 1);
			QS(e, pivote + 1, t);
		}
	}

}
