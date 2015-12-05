package VRPTW.ALNS;

import java.util.ArrayList;

public class Node implements Cloneable{
	
	
	
	public int id;
	
	public int demand; //Demand
	public int service;//Service Time
	public int tw_a;//Beginning of the TW
	public int tw_b;//End of the TW
	public int tw_w;//time window width
	
	
	public double arrivalTime;//Arrival time to the node in the solution
	public double exitTime; //max(arrivalTime, tw_a)+ service
	public int route;//Route in which the node is visited
	public int visited;//Position in the route

	public double cumulativeDist;

	
	
	
	public Node(int i, int d, int s , int a, int b) {
		id = i;
		demand = d;
		service = s;
		tw_a = a;
		tw_b = b;	
		tw_w = b-a;
	}
	
	public int  getID()
	{
		return id;
	}
	


	
	
	public String toString(){
		
		return id+"";
		//return ""+id;//+ "-"+cumulativeDist+"- ("+tw_a+","+ (int)arrivalTime+"-"+(int)exitTime+","+tw_b+")";
		//return ""+id + "-"+(int)(cumulativeDist);//;+"- ("+tw_a+","+ (int)arrivalTime+"-"+(int)exitTime+","+tw_b+")";
		//return ""+id + "- ("+tw_a+","+ (int)arrivalTime+"-"+(int)exitTime+","+tw_b+")";
	}
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}

