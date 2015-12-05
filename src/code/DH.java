package code;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DH {
	
	public static final int num_gifts = 100001;
	public static double[] latitutes;
	public static double[] longitud;
	public static double[] weight;
	public static double capacity = 1000;
	
	/**
	 * giftID,Trip
	 */
	public int[][] solution;
	
	public DH() {
		latitutes = new double[num_gifts];
		longitud = new double[num_gifts];
		weight = new double[num_gifts];
	}
	
	public void readData() {
		latitutes[0] = 90;
		longitud[0] = 0;
		weight[0] = 0;

		String csvFile = "./Data/giftsdata.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {

			br = new BufferedReader(new FileReader(csvFile));
			br.readLine(); // leer rotulos
			int gift_id = 1;
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] gift = line.split(cvsSplitBy);
				latitutes[gift_id] = Double.parseDouble(gift[1]);
				longitud[gift_id] = Double.parseDouble(gift[2]);
				weight[gift_id] = Double.parseDouble(gift[3]);
				gift_id++;
			}
			System.out.println("Done");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void readSolution() {
		String csvFile = "./Results/sol2015_12_02 121144.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		solution = new int[num_gifts][2];

		try {

			br = new BufferedReader(new FileReader(csvFile));
			br.readLine(); // leer rotulos
			int iterador = 1;
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] gift = line.split(cvsSplitBy);
				solution[iterador][0] = Integer.parseInt(gift[0]);
				solution[iterador][1] = Integer.parseInt(gift[1]);
				iterador++;
			}
			System.out.println("Done");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void printData() {
		for (int i = 1; i < latitutes.length; i++) {
			System.out.println(i + "/" + latitutes[i] + "/" + longitud[i] + "/"
					+ weight[i]);
		}

	}

	public int[][] getSolution() {
		// TODO Auto-generated method stub
		return solution;
	}

	public void printSolution() {
		for (int i = 1; i < solution.length; i++) {
			System.out.println("->>" + solution[i][0] + "/" + solution[i][1]);
		}

	}

	public static void printSolToFile(int[][] solution) {
		try
		{
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			// get current date time with Date()
			Date date = new Date();
			System.out.println(dateFormat.format(date));
			String filename = "./Results/sol"+ dateFormat.format(date).replaceAll(":", "").replaceAll("/", "_")+".csv";
			FileWriter writer = new FileWriter(filename);
			writer.append("GiftId");
			writer.append(',');
			writer.append("TripId");
			writer.append('\n');
			for (int i = 0; i < solution.length; i++) {
				writer.append(""+solution[i][0]);
				writer.append(',');
				writer.append(""+solution[i][1]);
				writer.append('\n');
			}
			writer.flush();
		    writer.close();
		}
		catch(IOException e)
		{
		     e.printStackTrace();
		} 
	    }

}
