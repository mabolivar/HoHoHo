package bpc.Graph;

import java.util.ArrayList;
import java.util.Hashtable;

import Utilities.VRPTWCG.Rounder;
import bpc.CG.LP_Manager_forVRPTW;
import bpc.CG.GraphManager;
import bpc.Heuristics.ESPPRC.HeuristicsHandler;
import bpc.IO.DataHandler;

public class FinalNode extends Node implements Cloneable {
	
	
	
	public int id;
	
	public int demand; //Demand
	public int service;//Service Time
	public int tw_a;//Beginning of the TW
	public int tw_b;//End of the TW
	public int tw_w;//time window width
	public ArrayList<Integer> magicIndex;
	
	public double arrivalTime;//Arrival time to the node in the solution
	public double exitTime; //max(arrivalTime, tw_a)+ service
	public int route;//Route in which the node is visited
	public int visited;//Position in the route

	public double cumulativeDist;
	public double cumulativeCost;
	
	//Variables Pulso
	public ArrayList Path;
	public double PathTime;
	public double PathLoad;
	public double PathCost;
	double PathDist;

	private HeuristicsHandler heuristicHandler;
	public FinalNode(int i, int d, int s , int a, int b) {
		super(i, 0, 0, a, b);
		id = i;
		demand = d;
		service = s;
		tw_a = a;
		tw_b = b;	
		tw_w = b-a;
		magicIndex = new ArrayList<>();
		Path= new ArrayList();
	}
	

	public void iniHeuriticHandler(HeuristicsHandler heu){
		heuristicHandler = heu;
	}
	

/**********************************************************************************************************************************************************************/
/**********************************************************************************************************************************************************************/
/**********************************************************************************************************************************************************************/
/**********************************************************************************************************************************************************************/

	public void pulseBound(double PLoad, double PTime, double PCost,
			ArrayList path, int Root, double PDist) {

		if (PLoad <= DataHandler.cap && (PTime) <= tw_b) {

			if ((PCost) < GraphManager.OraclePrimalBound[Root]) {
				GraphManager.OraclePrimalBound[Root] = (PCost);

				if (PCost < GraphManager.PrimalBound) {
					GraphManager.PrimalBound = PCost;

				}
				
			}
		
		}

	}

	/**********************************************************************************************************************************************************************/

	public synchronized void pulseMT(double PLoad, double PTime, double PCost, ArrayList path, double PDist, int thread) {
			if (PLoad <= DataHandler.cap && (PTime) <= tw_b) {
				if (PCost <= GraphManager.PrimalBound) {
					GraphManager.PrimalBound = PCost;
					this.PathTime = PTime;
					this.PathCost = PCost;
					this.PathLoad = PLoad;
					this.PathDist = PDist;
					this.Path.clear();
					for (int i = 0; i < path.size(); i++) {
						this.Path.add(path.get(i));
					}
					
					this.Path.add(id);
				}
				if (PCost < 0) {
					String keyS = this.Path.toString();
					if ( !heuristicHandler.routesPoolRC.containsKey(keyS)) {
						heuristicHandler.pool.add(keyS);
						heuristicHandler.routesPoolRC.put(keyS,Rounder.round9Dec(PCost));
						heuristicHandler.routesPoolDist.put(keyS, PDist);
						heuristicHandler.generator.put(keyS, 3);
					}
				}

			}

		}
	
	
	public String toString(){
		
		return id+"";
		//return ""+id+ "->"+cumulativeCost+"<- ("+tw_a+","+ (int)arrivalTime+"-"+(int)exitTime+","+tw_b+")";
		//return ""+id + "-"+(int)(cumulativeCost);//;+"- ("+tw_a+","+ (int)arrivalTime+"-"+(int)exitTime+","+tw_b+")";
		//return ""+id + "- ("+tw_a+","+ (int)arrivalTime+"-"+(int)exitTime+","+tw_b+")";
	}
	
	public Object clone() {
		return super.clone();
	}
	
	private void SortF(ArrayList<Double> set) {
		QSF(set, 0, set.size() - 1);
	}

	public int colocarF(ArrayList<Double> e, int b, int t) {
		int i;
		int pivote;
		double valor_pivote;
		double temp;

		pivote = b;
		//valor_pivote = DataHandler.pi[e[pivote].id] ;
		valor_pivote = e.get(pivote) ;
		for (i = b + 1; i <= t; i++) {
			if (  e.get(i) < valor_pivote) {
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

	public void QSF(ArrayList<Double> e, int b, int t) {
		int pivote;
		if (b < t) {
			pivote = colocarF(e, b, t);
			QSF(e, b, pivote - 1);
			QSF(e, pivote + 1, t);
		}
	}

}

