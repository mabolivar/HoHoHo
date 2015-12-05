package VRPTW.ALNS;

import java.util.ArrayList;

import Utilities.VRPTW.Clonetor;

public class InitialInsertionHeuristic {

	public InitialInsertionHeuristic() {
		// TODO Auto-generated constructor stub
	}

	private boolean fix(Node customer, ArrayList<Node>[] routes, int[] routesLoad, Node[] customers) {
		boolean sePuede = true;
		for (int j = 0; j < DataHandler.k; j++) {
			if(routesLoad[j]+ customer.demand <=DataHandler.cap){
				for (int i = 1; i < routes[j].size() - 1 && sePuede; i++) {
					ArrayList<Node> route = routes[j];

					double retraso = (DataHandler.Distance[route.get(i).getID() - 1][customer.id]
							+ DataHandler.Distance[customer.id][route.get(i)
									.getID()] - DataHandler.Distance[route.get(i).getID() - 1][route.get(i).getID()]) + customer.service;
					for (int k = i; k < routes[j].size() && sePuede; k++) {
						if (route.get(k).arrivalTime + retraso > route.get(k).tw_b) {
							sePuede = false;
						}
					}
					if (sePuede) {
						customer.arrivalTime=route.get(i-1).exitTime+ DataHandler.Distance[route.get(i-1).id][customer.id];
						customer.exitTime = Math.max(customer.arrivalTime, customer.tw_a) + customer.service;
						customer.route = j;
						customer.visited = i;
												
						for (int k = i; k <routes[j].size() && sePuede; k++) {
							route.get(k).arrivalTime += retraso;
							route.get(k).exitTime = Math.max(route.get(k).arrivalTime, route.get(k).tw_a) + route.get(k).service; ;
							route.get(k).visited++;
						}
						routes[j].add(i, customer);
						routesLoad[j] += customer.demand;
//						System.out.println("AAA" + customer);
						return true;
					}
				}
			}
		}
		return false;
	}
	

	

	
	public void getInitialSol2(ArrayList<Node>[] routes, int[] routesLoad,Node[] customers, ArrayList<Node> requestBank) {
			Node[] permutation = Clonetor.cloneArrayList(customers);// Create a copy
//			Sort(permutation); //
//			for (int i = 0; i < permutation.length; i++) {
//				System.out.print(permutation[i].getID()+", ");
//			}
//			System.out.println();
			int routeSize =1;
			customers[0].exitTime=0;
			for (int j = 0; j < DataHandler.k; j++) {
				routes[j].add(customers[0]);
			}
			for (int i = 1; i < permutation.length; i++) {
				Node customer = customers[permutation[i].getID()];
				routeSize = (int) (i / DataHandler.k)+1;
				double newArrivalTime = 0.0; // arrival time for i
				double newArrivalDist= 0.0;
				int bestRouteToInsert = -1;
				double bestSaveFor_i = -1;
				for (int j = 0; j < DataHandler.k; j++) {
						customer = customers[permutation[i].getID()]; // Customer is j in the arc (i,j)
						int lastCustomer_id = routes[j].get(routes[j].size() - 1).getID(); // i in the arc (i,j)
						//double save_ij = Math.abs(customers[lastCustomer_id].exitTime + DataHandler.Distance[customer.getID()][lastCustomer_id]- customer.tw_a);
						double save_ij =  DataHandler.saves[customer.id][lastCustomer_id];
						int newLoad = routesLoad[j] + customer.demand;
						double distance_ij = DataHandler.Distance[lastCustomer_id][customer.id];
						double nArrivalTime = customers[lastCustomer_id].exitTime+ distance_ij;
						double nArrivalDist = customers[lastCustomer_id].cumulativeDist+ distance_ij;
						//if (routes[j].size() <= routeSize+122) {
							if (save_ij > bestSaveFor_i) {
								if (newLoad <= DataHandler.cap) {// feasible by load?
									if (nArrivalTime <= customer.tw_b) { // feasible by TW?
										bestSaveFor_i = save_ij;
										bestRouteToInsert = j;
										newArrivalTime = nArrivalTime;
										newArrivalDist = nArrivalDist;
									} //else {System.out.println(customer+ " murio por TW");}
								} //else {System.out.println(customer + " murio por cap");}
							}

						//}

					}

					if (bestRouteToInsert != -1) {
						routeSize = (int) (i / DataHandler.k);
						customer.arrivalTime = newArrivalTime;
						customer.cumulativeDist = newArrivalDist;
						customer.exitTime = Math.max(customer.arrivalTime,customer.tw_a) + customer.service;
						customer.route = bestRouteToInsert;
						customer.visited = routes[bestRouteToInsert].size();
						routes[bestRouteToInsert].add(customer);
						routesLoad[bestRouteToInsert] += customer.demand;
					} else {
						//System.out.println("arreglo algo" + customer);
						boolean fixed = fix(customer, routes, routesLoad, customers);
						if (!fixed) {
							
							customer.arrivalTime = -1;
							customer.exitTime = -1;
							customer.route = -1;
							customer.visited = -1;
							requestBank.add(customer);
						}

					}

				
			}
			for (int j = 0; j < DataHandler.k; j++) {
				Node a = (Node) customers[0].clone();
				a.route = j;
				a.visited = routes[j].size();
				a.cumulativeDist = routes[j].get(routes[j].size()-1).cumulativeDist+ DataHandler.Distance[routes[j].get(routes[j].size()-1).id][0];
				routes[j].add(a);
			}
			
			for (int k = 0; k <requestBank.size(); k++) {
				Node c = requestBank.get(k);
				if(fix(c, routes, routesLoad, customers)){
					requestBank.remove(k);
					k--;
//					System.out.println("arreglo");
				}
			}
			
		}

	
	
	
	
	
	
	
	
	
	
	
	private void Sort(Node[] set) {
		QS(set, 0, set.length - 1);
	}

	public int colocar(Node[] e, int b, int t) {
		int i;
		int pivote, valor_pivote;
		Node temp;

		pivote = b;
		valor_pivote = e[pivote].tw_a ;
		for (i = b + 1; i <= t; i++) {
			if ( e[i].tw_a < valor_pivote) {
				pivote++;
				temp = e[i];
				e[i] = e[pivote];
				e[pivote] = temp;
			}
		}
		temp = e[b];
		e[b] = e[pivote];
		e[pivote] = temp;
		return pivote;
	}

	public void QS(Node[] e, int b, int t) {
		int pivote;
		if (b < t) {
			pivote = colocar(e, b, t);
			QS(e, b, pivote - 1);
			QS(e, pivote + 1, t);
		}
	}

}
