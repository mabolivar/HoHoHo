package Utilities.VRPTWCG;

public class Rounder {
	public static final double deviation = 0.0000000000;
	
	
	public static double  round6Dec( double rounded) {
		return (Math.round(rounded*1000000)/1000000.0);
//		return (Math.round(rounded*100)/100.0);
		//return rounded;
	}
	public static double  round9Dec( double rounded) {
		return (Math.round(rounded*1000000000)/1000000000.0);
		//return rounded;
	}
	
}
