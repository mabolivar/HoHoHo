package VRPTW.ALNS;

import java.util.ArrayList;

import org.omg.CORBA.DATA_CONVERSION;

public class ClustersManager {

	static ArrayList<Integer>[] clusters = new ArrayList[DataHandler.k];
	static double centroideX[]  = new double[DataHandler.k];
	static double centroideY[]  = new double[DataHandler.k];
	static double tw_aCluster[]  = new double[DataHandler.k];
	static double tw_bCluster[]  = new double[DataHandler.k];
	static double capCluster[]  = new double[DataHandler.k];
	static double services[]  = new double[DataHandler.k];

	static int whichCluster[]  = new int[DataHandler.n+1];
	ArrayList<Integer> c = new ArrayList<Integer>();
	static double cX= 0 ;
	static double cY= 0;
	
	public ClustersManager() {
		// TODO Auto-generated constructor stub
		centroideTotal();
		for (int i = 1; i < VRPTW_Manager.customers.length; i++) {
			c.add(i);
		}
		for (int i = 0; i <DataHandler.k; i++) {
			clusters[i] = new ArrayList<Integer>();
			tw_aCluster[i] = DataHandler.infinity;
			tw_bCluster[i] = 0.0;
		}
		findKFar(cX, cY, 1);
		cluster();
		for (int i = 0; i < clusters.length; i++) {
			for (int j = 0; j < clusters[i].size(); j++) {
				//System.out.println(i+" "+clusters[i].get(j));
			}
		}
		System.out.println();
	}

	private void cluster() {
		// TODO Auto-generated method stub
		while (c.size()>0) {
			findKFar(cX, cY, -1);
		}
	}

	private void findKFar(double cX, double cY, int first) {
		// TODO Auto-generated method stub
		for (int i = 0; i < DataHandler.k && c.size()>0; i++) {
			double maxDist = 0 ; 
			if (first != 1) {
				cX = centroideX[i];
				cY = centroideY[i];
				maxDist = DataHandler.infinity;

				
			}else
			{
				if (i > 0) {
					cX =0;
					cY =0;
					for (int j = 0; j < i; j++) {
						cX+=centroideX[j];
						cY += centroideY[j];
					}
					
				}
			}
			
			
			int index = -1; 
			int lastChance = -1;
			for (int j = 0; j <c.size(); j++) {
				double cAnterior = DataHandler.infinity;
				double d_cent_j = Math.sqrt(Math.pow((cX - DataHandler.x[c.get(j)]), 2)	+ Math.pow((cY - DataHandler.y[c.get(j)]), 2));
				if (first ==1) {
					for (int k = 0; k < DataHandler.k; k++) {
						double a = Math.sqrt(Math.pow((centroideX[k] - DataHandler.x[c.get(j)]), 2)	+ Math.pow((centroideY[k] - DataHandler.y[c.get(j)]), 2));
						if (a<cAnterior) {
							cAnterior = a;
						}
					}
					
				}else{
					double pe = 0, pl = 0, plMax, pCap = 0;
					
					if (tw_aCluster[i] + d_cent_j + services[i]> DataHandler.tw_b[c.get(j)]) {
						//pe =(DataHandler.AverageArc+d_cent_j)*(1 + tw_aCluster[i]/DataHandler.tw_b[c.get(j)]);
						pe = (1 + tw_aCluster[i]/DataHandler.tw_b[c.get(j)]);
					}
					if (tw_bCluster[i] + d_cent_j + services[i]> DataHandler.tw_b[c.get(j)]) {
						//pl = (DataHandler.AverageArc+d_cent_j)*(DataHandler.tw_b[c.get(j)]/tw_bCluster[i] );
						pl = (1+ DataHandler.tw_b[c.get(j)]/tw_bCluster[i] );
					}
					if (capCluster[i]+ DataHandler.demand[c.get(j)]> DataHandler.cap) {
						//pCap = DataHandler.AverageArc*(1+ DataHandler.cap/capCluster[i]) ;
						pCap = (1+ DataHandler.cap/capCluster[i]) ;
					}
					d_cent_j =  d_cent_j*(pe +pl+pCap);
				}
				if (first*d_cent_j>first*maxDist && cAnterior> DataHandler.AverageArc) {
					maxDist = d_cent_j;
					index = j;
				}else if (first*d_cent_j>first*maxDist && cAnterior< DataHandler.AverageArc){
					lastChance = j;
				}
			}
			if (index==-1 ) {
				index = lastChance;
			}
			//System.out.print(index + "  ");
			//System.out.println(c.get(index));
			centroideX[i] = (clusters[i].size()*centroideX[i] + DataHandler.x[c.get(index)])/(clusters[i].size()+1);
			centroideY[i] = (clusters[i].size()*centroideY[i] + DataHandler.y[c.get(index)])/(clusters[i].size()+1);
			services[i] = (clusters[i].size()*services[i] + DataHandler.service[c.get(index)])/(clusters[i].size()+1);
			tw_aCluster[i] = tw_aCluster[i] > DataHandler.tw_a[c.get(index)]? DataHandler.tw_a[c.get(index)]: tw_aCluster[i];
			tw_bCluster[i] = tw_bCluster[i] < DataHandler.tw_b[c.get(index)]? DataHandler.tw_b[c.get(index)]: tw_bCluster[i];
			capCluster[i] = capCluster[i] + DataHandler.demand[c.get(index)];
			clusters[i].add(c.get(index));
			whichCluster[c.get(index)] =i;
			c.remove(index);
			
		}
	}

	private void centroideTotal() {
		// TODO Auto-generated method stub
		for (int i = 1; i < VRPTW_Manager.customers.length; i++) {
			cX += DataHandler.x[i];
			cY += DataHandler.y[i];
		}
		cY= cY/DataHandler.n;
		cX= cX/DataHandler.n;
	}
	
}
