package my_Code;

import java.util.Arrays;
import java.util.Random;

public class Evolver {

	public static Object[] currentBestGenes;
	public static long currentBestTime;
	
	public static void main(String[] args) {
		
		Object[] currentBestGenes = new Object[12];	
		evolver(currentBestGenes); //Search.		
    }
	
	public static void evolver(Object[] currentBestGenes) {
		
		int i = 3; //Num of genes to change
		Random rand = new Random();
		float newGene;
		
		while (i > 0) {
			int gene = rand.nextInt(10) + 2; //Gene list is 14 spaces long and genes occupy space 4-10

			if (gene == 4 || gene == 8) { //Wheel Size - 0.01f to 1.00f
				newGene = (float) (rand.nextInt(100) + 1) / 100;
				currentBestGenes[gene] = newGene;
			}
			else if (gene == 5 || gene == 9) { //Max Speed - Up to -200.00f
				newGene = (rand.nextInt(20001)/100)*-1;
				currentBestGenes[gene] = newGene;
			}
			else if (gene == 6 || gene == 10) { //Max Torque - Up to 100.00f
				newGene = rand.nextInt(10001)/100;
				currentBestGenes[gene] = newGene;
			}
			else { //Suspension Dampening (tightness of suspension) 0.00f to 2.00f

			}
			i--;
		}	
	}		
}
