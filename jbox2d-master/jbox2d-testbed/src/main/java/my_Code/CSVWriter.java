package my_Code;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CSVWriter {
	
	public static String fileName = "C:\\Users\\Ishmail Qasim\\OneDrive\\Documents\\Back up uni work\\Year 3\\FYP\\EVC_Log.csv";
	
	//Create new file
	public static void main(String[] args) {
		//writeNewFile();
	}
		
	public static void writeNewFile() {
		try (PrintWriter EVC_Log = new PrintWriter(new File(fileName))) {
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
			sb.append("Wheel2 dampingRatio");sb.append(",");
			
			EVC_Log.write(sb.toString());
			
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}
	
	//Add new line in CSV.
	public static void newLine(String i, String filepath) {
		
		try {
			FileWriter EVC_Log = new FileWriter(filepath, true);
			BufferedWriter bw = new BufferedWriter(EVC_Log);
			PrintWriter pw = new PrintWriter(bw);
			
			pw.flush();
			pw.println();
			pw.print(i + ",");
			pw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Append new configuration to row end.
	public static void saveResult(Object[] currentGenes, String filepath) {
		
		try {
			FileWriter EVC_Log = new FileWriter(filepath, true);
			BufferedWriter bw = new BufferedWriter(EVC_Log);
			PrintWriter pw = new PrintWriter(bw);
			
			for (int i = 0; i > currentGenes.length-1; i++) {
				pw.print(currentGenes[i].toString() + ",");
				pw.flush();
				pw.close();
			}
			newLine(currentGenes[0].toString(), filepath);
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
