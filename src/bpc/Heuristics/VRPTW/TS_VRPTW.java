package bpc.Heuristics.VRPTW;

import java.util.ArrayList;
import java.util.Random;

import code.DH;
import code.F;
import bpc.Graph.GraphManager;
import bpc.Graph.Node;
import bpc.IO.DataHandler;
import Utilities.*;
import Utilities.VRPTWCG.Clonetor;
import Utilities.VRPTWCG.Rounder;

public class TS_VRPTW {

	private static final int TWO_OPT = 0;
	private static final int NODE_CHANGE_ROUTE = 1;
	private static final int SWAP_IN_ROUTE = 2;
	private static final String DF = null;

	private ArrayList<ArrayList<Integer>> solution;

	// s'-------------------------------------------
	private ArrayList<ArrayList<Integer>> routes;
//	private ArrayList<ArrayList<Integer>> routes;
	private ArrayList<Double> routesDist;
	private ArrayList<Integer> routesLoad;
	private Node[] customers;
	static double FO;
	// ---------------------------------------------

	private int[] operatorStage;
	private int[] operatorPerformed;
	private int forbiden;

	private int tsIterations = 0;
	public boolean seAtasco;
	double primalbound = Double.POSITIVE_INFINITY;
	public DH data;
	public TS_VRPTW(DH nData) {
		data = nData;
		primalbound = Double.POSITIVE_INFINITY;
		tsIterations = 0;
	}

	public int[][] routesToFormat(ArrayList<ArrayList<Integer>> routesSol){
		int[][] sol = new int[DH.num_gifts-1][2];
		ArrayList<Integer> r;
		int gift, iterador= 0;
		for (int i = 0; i < routesSol.size(); i++) {
			r = routesSol.get(i);
			for (int j = 1; j < r.size()-1; j++) {
				gift = r.get(j);
				sol[iterador][0]=gift;
				sol[iterador][1]=i;
				iterador++;
			}
		}
		
		
		
		return sol;
		
	}
	public void run(int[][] sol) {
		routes=new ArrayList<>();
		routesLoad = new ArrayList<Integer>();
		routesDist = new ArrayList<Double>();
		int r, gift = 0;
		for (int i = 1; i < sol.length; i++) {
			gift = sol[i][0];
			r = sol[i][1];
			if (r<routes.size()) {
				routes.get(r).add(routes.get(r).size() - 1, gift);
			} else {
				routes.add(r, new ArrayList<Integer>());
				routes.get(r).add(0);
				routes.get(r).add(gift);
				routes.get(r).add(0);
			}
		}
		
		seAtasco = false;
		double test = F.calcObjectiveFuenction(routesToFormat(routes), data);
		System.out.println("Initial OF" + test);
		
		Random rr = new Random(324);
		this.forbiden = 10;
		tsIterations = 0;
		resetTabuSearch();
		solution = new ArrayList<>();
		solution.addAll(routes);
		for (int i = 0; i < routes.size(); i++) { // basisRoutes.size()
			ArrayList<Integer> route = routes.get(i);
			setRoute(route, i);
		}
		while (tsIterations < 500) {
			System.out.print("Iteración: " + tsIterations );
			calcFO();
			if (FO < primalbound) {
				primalbound = FO;
				//test = F.calcObjectiveFuenction(routesToFormat(routes), data);
				solution = new ArrayList<>();
				solution.addAll(routes);
				System.out.println(" --> " + FO);
				
			}
//			else{
//				double gap = Math.abs(primalbound-FO)/primalbound;
				System.out.println("Bad luck!!!1 ");
//				if (gap<0.05 && rr.nextDouble() < 0.9) {
//					routes = new ArrayList<>();
//					routes.addAll(solution);
//				}
//			}
			generateNeighbors();

			updateTabus();
			tsIterations++;

		}
		int[][] finalSol =  routesToFormat(solution); 
		test = F.calcObjectiveFuenction(finalSol, data);
		System.out.println("Final OF " + test);
		DH.printSolToFile(finalSol);
		
		// for (int i = 0; i <routes.size(); i++) {
		// System.out.println(routesDist.get(i )+ " ->" +routes.get(i) );
		// }
		System.out.println("FO:= " + primalbound);
		System.out.println("TS-END\n");
	}

	

	/**
	 * Set the route coming from the pool into the TS data structure. The method
	 * fails if the route that is being set have a forbidden arc (arcs that
	 * can't be used due to the branching procedure).
	 * 
	 * @param route
	 *            Can be basic variable route or a random one.
	 * @param varIndex
	 *            The index of the route in the master problem.
	 * @return <code>true</code> if the route was successfully loaded,
	 *         <code>false</code> other wise.
	 */
	private boolean setRoute(ArrayList<Integer> route, int varIndex) {
		routesLoad.add(varIndex, 0);
		routesDist.add(varIndex, 0.0);
		upDateRoutes("SetRoute", varIndex);
		return true;
	}

	private void calcFO() {
		FO = F.calcObjectiveFuenction(routesToFormat(routes), data);;
//		for (int i = 0; i < routesDist.size(); i++) {
//			FO += routesDist.get(i);
//		}

	}

	/**
	 * Procedure to update a single route given a parameter. Main information is
	 * updated for each node Data structure routes and global variables are
	 * updated
	 * 
	 * @param origen
	 * @param varIndex
	 */
	private void upDateRoutes(String origen, int varIndex) {
		int load = 10;
		int customer = -1,last = -1;
		double cumulativeDist = 0;
		for (int i = 1; i < routes.get(varIndex).size(); i++) {
			customer = routes.get(varIndex).get(i);
			last = routes.get(varIndex).get(i - 1);
			cumulativeDist+= F.haversine(data.latitutes[customer], data.longitud[customer],data.latitutes[last],data.longitud[last]);
			load += data.weight[customer];
		}
		if (customer != -1) {
			routesLoad.set(varIndex, load);
			routesDist.set(varIndex, cumulativeDist);
		} else {
			System.err.println(routes.get(varIndex));
		}

	}

	private void updateTabus() {
		for (int i = 0; i < operatorPerformed.length; i++) {
			if (operatorPerformed[i] != -1) {
				operatorStage[i]++;
			}
			if (operatorStage[i] >= forbiden) {
				operatorPerformed[i] = -1;
				operatorStage[i] = 0;
			}
		}

	}

	private void resetTabuSearch() {
		operatorPerformed = new int[DH.num_gifts+1];
		operatorStage = new int[DH.num_gifts+1];
		for (int i = 0; i < operatorPerformed.length; i++) {
			operatorPerformed[i] = -1;
			operatorStage[i] = 0;
		}
	}

	private void generateNeighbors() {
		int nodeChanged = -1;
		int operator = -1;
		Random ran = new Random(7);
		int delRoute = -1;
		int insRoute = -1;
		int delNodeIndex = -1;
		int insNodeIndex = -1;
		double aletorio = seAtasco ? ran.nextDouble() : 0;
		aletorio = ran.nextDouble();
		int cambios = 0;
		double minDelta = 00;
		ArrayList<Integer> r = null;
		ArrayList<Integer> p = null;
		minDelta = 1;
		if(minDelta==0){
			for (int r1 = 0; r1 < routes.size(); r1++) {
				r = routes.get(r1);
				for (int i = 1; i < r.size() - 1; i++) {
					if (notTabu(r.get(i), SWAP_IN_ROUTE)) {
						for (int j = i + 1; j < r.size() - 1; j++) {
							double delta = -F.distance(r.get(i - 1),r.get(i))
									- F.distance(r.get(i),r.get(i + 1))
									- F.distance(r.get(j - 1),r.get(j))
									- F.distance(r.get(j),r.get(j + 1))
									+ F.distance(r.get(i - 1),r.get(j))
									+ F.distance(r.get(j),r.get(i + 1))
									+ F.distance(r.get(j - 1),r.get(i))
									+ F.distance(r.get(i),r.get(j + 1));
							if (i == j + 1) {
								delta += 3 * F.distance(r.get(i),r
										.get(j));
							}
							//delta=delta*(DH.weight[r.get(i)]-DH.weight[r.get(j)]);
							delta = delta<0?delta:delta*(DH.weight[r.get(i)]-DH.weight[r.get(j)]);
							if (delta < minDelta ||DH.weight[r.get(i)]-DH.weight[r.get(j)]<0 || aletorio > 0.98) {
								if (true) {
									minDelta = delta;
									nodeChanged = r.get(i);
									operator = SWAP_IN_ROUTE;
									delRoute = r1;
									insRoute = r1;
									delNodeIndex = i;
									insNodeIndex = j;
									//System.out.print("CAMBIANDOP!!!!");
									addTabu(nodeChanged, SWAP_IN_ROUTE);
									addTabu(routes.get(delRoute).get(insNodeIndex), SWAP_IN_ROUTE);
									addTabu(nodeChanged, TWO_OPT);
									addTabu(routes.get(delRoute).get(insNodeIndex), TWO_OPT);
									int delNode = routes.get(delRoute).get(delNodeIndex);
									int  insNode = routes.get(delRoute).get(insNodeIndex);
									routes.get(delRoute).set(delNodeIndex, insNode);
									routes.get(delRoute).set(insNodeIndex, delNode);
									upDateRoutes("TS REMOVE delRoute", delRoute);
								}
							}
						}
					}
				}
			}
		}
//		System.out.println("Checking NODE_CHANGE_ROUTE");
		
			minDelta = 0;
			cambios = 0;
			if(minDelta==0){
			for (int r1 = 0; r1 < routes.size(); r1+=tsIterations+1) {
//				System.out.println("Routa " + r1+ " acumulados!!-> " + cambios);
				r = routes.get(r1);
				for (int r1_i = 1; r1_i < r.size() - 1; r1_i++) {
					int rc = r.get(r1_i);
					if (notTabu(rc, NODE_CHANGE_ROUTE)) {
						for (int r2 = Math.max(0, r1-2); r2 < r1+2; r2++) {
						//int r2=r1+1;
							if (r2!=r1 && r2<routes.size()) {
								p = routes.get(r2);
								if (routesLoad.get(r2) + DH.weight[rc] <= 1000) {
									for (int r2_j = 1; r2_j < p.size(); r2_j++) {
										double deltaDel = -F.distance(r.get(r1_i),r.get(r1_i + 1))
												- F.distance(r.get(r1_i - 1),r.get(r1_i))
												+ F.distance(r.get(r1_i - 1),r.get(r1_i + 1));
										double deltaIns = F.distance(p.get(r2_j - 1), rc)
												+ F.distance(rc, p.get(r2_j))
												- F.distance(p.get(r2_j - 1),p.get(r2_j));
										double foDelta = (deltaDel + deltaIns);
	
										if (foDelta < minDelta || aletorio > 0.99) {
											if (checkInsertionFeasibility(r2_j, rc,r1)) {
												delRoute = r1;
												insRoute = r2;
												delNodeIndex = r1_i;
												insNodeIndex = r2_j;
												
												minDelta = foDelta;
												nodeChanged = rc;
												operator = NODE_CHANGE_ROUTE;
												addTabu(nodeChanged, NODE_CHANGE_ROUTE);
												addTabu(p.get(r2_j), NODE_CHANGE_ROUTE);
												int swaping1 = routes.get(delRoute).remove(delNodeIndex);
												int swaping2 = routes.get(delRoute).remove(delNodeIndex);
												routes.get(insRoute).add(insNodeIndex, swaping1);
												routes.get(delRoute).add(delNodeIndex, swaping2);
												upDateRoutes("TS Inst instRoute", insRoute);
												upDateRoutes("TS Inst delRoute", delRoute);
												r1_i = 1;
												rc = r.get(r1_i);
												r2_j = 1;
												if(routes.get(delRoute).size()==2){
													ArrayList<Integer> borrar= routes.remove(delRoute);
													r1=r1-1==-1?0:r1--;
													r = routes.get(Math.min(r1, routes.size()-1));
													rc = r.get(r1_i);
													borrar.size();
												}
												cambios++;
												
											}
										}
									}
								}
							}
						}
					}
				}
			}
			}
			minDelta =0;
			if(minDelta==0){
				for (int r1 = 0; r1 < routes.size(); r1++) {
				r = routes.get(r1);
				for (int i = 1; i < r.size() - 1; i++) {
					if (notTabu(r.get(i), TWO_OPT)) {
						for (int j = i + 1; j < r.size() - 2; j++) {
							int i_arc1 = i - 1;
							int j_arc1 = i;
							int l_arc2 = j;
							int k_arc2 = j + 1;
							double delta = -F.distance(r.get(i_arc1),r
									.get(j_arc1))
									- F.distance(r.get(l_arc2),r
											.get(k_arc2))
									+ F.distance(r.get(i_arc1),r
											.get(l_arc2))
									+ F.distance(r.get(j_arc1),r
											.get(k_arc2));
//							delta = delta<0?delta:delta*(DH.weight[r.get(j)]-DH.weight[r.get(i)]);
							if (delta < minDelta || aletorio > 0.98) {
								if (true) {
									minDelta = delta;
//									System.out.println(delta);
									nodeChanged = r.get(i);
									operator = TWO_OPT;
									delRoute = r1;
									insRoute = r1;
									delNodeIndex = i;
									insNodeIndex = j;
									addTabu(nodeChanged, TWO_OPT);
									addTabu(routes.get(delRoute).get(insNodeIndex), TWO_OPT);
									doTwoOpt(delRoute, delNodeIndex, insNodeIndex);
									upDateRoutes("TS 2-opt delRoute", delRoute);
//									cambios++;
//									if(cambios>=100){
//										minDelta = 0;
//										j = r.size();
//										i= r.size();
//									}
								}
							}
						}
					}
				}
			}
			}
			
//		System.out.println("updating");
		if (operator == TWO_OPT) {// Borrar
//			addTabu(nodeChanged, TWO_OPT);
//			addTabu(routes.get(delRoute).get(insNodeIndex), TWO_OPT);
//			doTwoOpt(delRoute, delNodeIndex, insNodeIndex);
//			upDateRoutes("TS 2-opt delRoute", delRoute);

		} else if (operator == SWAP_IN_ROUTE) {
			// System.out.println("SWAP: " +delNodeIndex + " vs " + insNodeIndex
			// + "  node: "+ nodeChanged + " olRoute: " + routes.get(delRoute) +
			// " new route: " + routes.get(insRoute));

//			addTabu(nodeChanged, SWAP_IN_ROUTE);
//			addTabu(routes.get(delRoute).get(insNodeIndex), SWAP_IN_ROUTE);
//			addTabu(nodeChanged, TWO_OPT);
//			addTabu(routes.get(delRoute).get(insNodeIndex), TWO_OPT);
//			int delNode = routes.get(delRoute).get(delNodeIndex);
//			int  insNode = routes.get(delRoute).get(insNodeIndex);
//			routes.get(delRoute).set(delNodeIndex, insNode);
//			routes.get(delRoute).set(insNodeIndex, delNode);
			// System.out.println("SWAP: " + nodeChanged + " olRoute: " +
			// routes.get(delRoute) + " new route: " + routes.get(insRoute));

//			upDateRoutes("TS REMOVE delRoute", delRoute);
		} else if (operator == NODE_CHANGE_ROUTE) { // insertar
		// System.out.println("nodE: " + nodeChanged + " olRoute: " +
		// routes.get(delRoute) + " new route: " + routes.get(insRoute));
//			addTabu(nodeChanged, NODE_CHANGE_ROUTE);
//			int swaping = routes.get(delRoute).remove(delNodeIndex);
//			routes.get(insRoute).add(insNodeIndex, swaping);
//			upDateRoutes("TS Inst instRoute", insRoute);
//			upDateRoutes("TS Inst delRoute", delRoute);
			// System.out.println("nodE: " + nodeChanged + " olRoute: " +
			// routes.get(delRoute) + " new route: " + routes.get(insRoute));
		} else {
			// resetTabuSearch();
			seAtasco = true;
			for (int i = 0; i < operatorPerformed.length; i++) {
				System.out.println("se quedo");
				if (operatorPerformed[i] != -1) {
					operatorStage[i] += 100 * forbiden;
				}
			}
		}

	}

	private void doTwoOpt(int r1, int i, int j) {
		ArrayList<Integer> route = routes.get(r1);

		ArrayList<Integer> r = new ArrayList<>();
		int i_arc1 = i - 1;
		int j_arc1 = i;
		int l_arc2 = j;
		int k_arc2 = j + 1;
		int iter = 0;
		int step = 1;
		while (r.size() < route.size()) {

			r.add((Integer) route.get(iter));

			if (iter == i_arc1) {
				iter = l_arc2;
				step = -1;
			} else if (iter == j_arc1) {
				iter = k_arc2;
				step = +1;
			} else if (iter == l_arc2) {
				iter += step;
			} else if (iter == k_arc2) {
				iter += step;
			} else {
				iter += step;
			}
		}
		routes.set(r1, r);
	}

	/**
	 * Check feasibility for a node insertion
	 * 
	 * @param i
	 *            Position in the route where the node is going to be inserted
	 * @param rc
	 *            Request customer, node to be inserted
	 * @param r
	 *            Route in which the node is going to be inserted
	 * @return true if the insertion is feasible, false otherwise
	 */
	private boolean checkInsertionFeasibility(int i, int rc, int r) {
		boolean feasible = true;
		if(routesLoad.get(r)+DH.weight[rc]>=DH.capacity){
			feasible = false;
		}
		return feasible;
	}

	/**
	 * Add a tabu for a node (This method set the contrary operator)
	 * 
	 * @param nodeChange
	 *            Node inserted or removed
	 * @param operator
	 *            Contrary operator performed
	 */
	private void addTabu(int nodeChange, int operator) {
		operatorPerformed[nodeChange] = operator;
		operatorStage[nodeChange] = 1;
	}

	private boolean notTabu(int id, int operator) {
		if (operatorPerformed[id] == operator) {
			return false;
		} else {
			return true;
		}
	}


	public double getPrimalBound() {
		return primalbound;
	}

	
}
