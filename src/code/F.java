package code;

import java.util.ArrayList;
import java.util.HashMap;

public class F {

	public static final double R = 6371;// 6372.800; // In meters

	public static double haversine(double lat1, double lon1, double lat2,
			double lon2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double a = Math.pow(Math.sin(dLat / 2), 2)
				+ Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1)
				* Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return R * c;
	}

	public static double distance(int gift1, int gift2) {
		double dij = haversine(DH.latitutes[gift1], DH.longitud[gift1],
				DH.latitutes[gift2], DH.longitud[gift2]);
		return dij;
	}

	public static void main(String[] a) {
		System.out.println(haversine(90, 0, 83.40854619, -27.95755831));
	}

	public static double calcObjectiveFuenction(int[][] solution, DH data) {
		ArrayList<Integer> route_ids = new ArrayList<Integer>();
		HashMap<Integer, ArrayList<Integer>> routes = new HashMap<Integer, ArrayList<Integer>>();
		int r, gift = 0;
		for (int i = 1; i < solution.length; i++) {
			gift = solution[i][0];
			r = solution[i][1];
			if (routes.containsKey(r)) {
				routes.get(r).add(routes.get(r).size() - 1, gift);
			} else {
				routes.put(r, new ArrayList<Integer>());
				routes.get(r).add(0);
				routes.get(r).add(gift);
				routes.get(r).add(0);
				route_ids.add(r);
			}
		}

		double totalCost = 0;
		double routeCost = 0;
		double routeWeight = 0;
		double dist= 0;
		int num_clients_route = 0;
		int v_i = -1;
		int v_j = -1;
		for (int K = 0; K < route_ids.size(); K++) {
			r = route_ids.get(K);
			routeCost = 0;
			routeWeight = 10;
			num_clients_route = routes.get(r).size();
			for (int i = 0; i < num_clients_route; i++) {
				v_i = routes.get(r).get(i);
				routeWeight += data.weight[v_i];
			}
			for (int i = 1; i < num_clients_route; i++) {
				double weight_until_i = 0;
				for (int ii = 1; ii < i; ii++) {
					v_i = routes.get(r).get(ii);
					weight_until_i += data.weight[v_i];
				}
				v_i = routes.get(r).get(i);
				v_j = routes.get(r).get(i - 1);
				dist= haversine(data.latitutes[v_i], data.longitud[v_i],data.latitutes[v_j],data.longitud[v_j]);
				routeCost+=((routeWeight-weight_until_i)*dist);
			}
			totalCost += routeCost;
		}

		return totalCost;

	}
}
