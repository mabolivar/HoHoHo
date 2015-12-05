package code;

import bpc.Heuristics.VRPTW.TS_VRPTW;

public class MainHohoho {

	public static void main(String[] args){
		DH data = new DH();
		data.readData();		
		data.readSolution();
		TS_VRPTW ts= new TS_VRPTW(data);
		ts.run(data.solution);
		double OF = F.calcObjectiveFuenction(data.getSolution(),data);
		System.out.println("Función Objetivo = " + OF);
		
	}
	
	
	
	
}
