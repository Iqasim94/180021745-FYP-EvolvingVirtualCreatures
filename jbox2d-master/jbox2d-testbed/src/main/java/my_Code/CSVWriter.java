package my_Code;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CSVWriter {
	
	//Create new file
	public static void main(String[] args) {
		
		try (PrintWriter EVC_Log = new PrintWriter(new File("EVC_Log.csv"))) {
			StringBuilder sb = new StringBuilder();
			sb.append("Run");sb.append(",");
			sb.append("Previous Best");sb.append(",");
			sb.append("Recorded Time");sb.append(",");
			sb.append("Evolutions");sb.append(",");
			
			sb.append("Wheel1 Size");sb.append(",");
			sb.append("Wheel1 speed");sb.append(",");
			sb.append("Wheel1 maxTorque");sb.append(",");
			sb.append("Wheel1 dampingRatio");sb.append(",");
			
			sb.append("Wheel2 Size");sb.append(",");
			sb.append("Wheel2 speed");sb.append(",");
			sb.append("Wheel2 maxTorque");sb.append(",");
			sb.append("Wheel2 dampingRatio");sb.append("\n");
			
			EVC_Log.write(sb.toString());
			
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}
	
	//Add new line in CSV.
	public static void newLine(int i, String filepath) {
		
		try {
			FileWriter EVC_Log = new FileWriter(filepath, true);
			BufferedWriter bw = new BufferedWriter(EVC_Log);
			PrintWriter pw = new PrintWriter(bw);
			
			pw.flush();
			pw.println();
			pw.print(i + ",");
			pw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Append new configuration to row end.
	public static void saveResult(double bestGen, String filepath) {
		
		try {
			FileWriter csvWriter = new FileWriter(filepath, true);
			BufferedWriter bw = new BufferedWriter(csvWriter);
			PrintWriter pw = new PrintWriter(bw);
			
			pw.print(bestGen + ",");
			pw.flush();
			pw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
