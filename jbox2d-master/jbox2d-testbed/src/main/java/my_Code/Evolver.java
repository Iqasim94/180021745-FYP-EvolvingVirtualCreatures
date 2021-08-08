package my_Code;

import java.util.Random;

public class Evolver {

//	public static Object[] currentBestGenes;
	
	public static void main(String[] args) {
		
//		Object[] currentBestGenes = new Object[12];	
//		evolver(currentBestGenes); //Search.		
    }
	
	public static Object[] evolver(Object[] currentBestGenes) {
		
		int i = 3; //Num of genes to change
				
		Random rand = new Random();
		float newGene;
		
		while (i > 0) {
			int gene = rand.nextInt(8) + 4; //Gene list is 12 spaces long and genes occupy space 4-10

			if (gene == 4 || gene == 8) { //Wheel Size - 0.01f to 1.0f
				newGene = (rand.nextInt(100) + 1);
				currentBestGenes[gene] = newGene/100;
			}
			else if (gene == 5 || gene == 9) { //Max Speed - Up to -200.0f
				newGene = (rand.nextInt(20001))*-1;
				currentBestGenes[gene] = newGene/100;
			}
			else if (gene == 6 || gene == 10) { //Max Torque - Up to 100.0f
				newGene = rand.nextInt(10001);
				currentBestGenes[gene] = newGene/100;
			}
			else { //Suspension Dampening (tightness of suspension) 1.0f to 10.0f
				newGene = rand.nextInt(900)+100;
				currentBestGenes[gene] = newGene/100;
			}
			i--;
		}
		return currentBestGenes;
	}		
}