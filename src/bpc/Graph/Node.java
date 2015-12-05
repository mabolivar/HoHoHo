package bpc.Graph;

import java.util.ArrayList;

import bpc.IO.DataHandler;

public class Node implements Cloneable{
	
	
	
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
	boolean FirstTime=true;
	
	public ArrayList<Integer> Q_i;
	
	public Node(int i, int d, int s , int a, int b) {
		id = i;
		demand = d;
		service = s;
		tw_a = a;
		tw_b = b;	
		tw_w = b-a;
		Q_i = new ArrayList<Integer>();
		magicIndex = new ArrayList<>();
	}
	


	
	public int  getID()
	{
		return id;
	}

	public String toString(){
		return id+"";
	}
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	public void autoSort(){
		Sort(this.magicIndex);
	}
	private synchronized void Sort(ArrayList<Integer> set) {
		QS(set, 0, set.size() - 1);
	}

	public int colocar(ArrayList<Integer> e, int b, int t) {
		int i;
		int pivote;
		double valor_pivote;
		int temp;

		pivote = b;
		//valor_pivote = DataHandler.pi[e[pivote].id] ;
		valor_pivote = DataHandler.costList[e.get(pivote)] ;
		for (i = b + 1; i <= t; i++) {
			if (   DataHandler.costList[e.get(i)]< valor_pivote) {
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

	public void QS(ArrayList<Integer> e, int b, int t) {
		int pivote;
		if (b < t) {
			pivote = colocar(e, b, t);
			QS(e, b, pivote - 1);
			QS(e, pivote + 1, t);
		}
	}


	private boolean lastStep(ArrayList<Integer> path, double pCost, double pTime) {

	if(path.size()<=1){
		return false;
	}
	else{
		int prevNode =  path.get(path.size()-1);
		int directNode = path.get(path.size()-2);
		double directCost = pCost-DataHandler.cost[prevNode][id]-DataHandler.cost[directNode][prevNode]+DataHandler.cost[directNode][id];
		
		if(directCost<=pCost && DataHandler.forbidden[directNode][id]!=1){
		/*	System.out.println("----------------------------------------------------------------");
			System.out.println("ID: "+id);
			System.out.println(path);
			System.out.println("pCost: "+pCost);
			System.out.println("dirCost: "+directCost);
			System.out.println("Costo de "+prevNode+" a "+id+": "+DataHandler.cost[prevNode][id]);
			System.out.println("Costo de "+directNode+" a "+id+": "+DataHandler.cost[directNode][id]);
			System.out.println("----------------------------------------------------------------");
			*/
			return true;
		}
		
		
		
	}
	
	
	return false;
}



}
