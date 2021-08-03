package my_Code;

import java.util.Arrays;
import java.util.Random;

public class Evolver {

	public static int[] currentBestGenes;
//	public static Object[] currentBestGenes;
	public static long currentBestTime;
	
	public static void main(String[] args) {
		
		//Routes matrix of distance of x to y.
		int[] currentBestGenes = {0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		System.out.print(Arrays.toString(currentBestGenes) + "\n");
//		Object[] currentBestGenes = new Object[10];	
		evolver(currentBestGenes); //Search.		
    }
	
	public static void evolver(int[] currentBestGenes) {
		
		int i = 3;
		int x;
		Random rand = new Random();
		
		while (i > 0) {
			int gene = rand.nextInt(10) + 4;

			if (gene == 0 || gene == 5) {
				currentBestGenes[gene]++;
			}
			else if (gene == 1 || gene == 6) {
				x = currentBestGenes[gene] * 2;
				currentBestGenes[gene] = x;
			}
			else if (gene == 2 || gene == 7) {
				x = currentBestGenes[gene] - 20;
				currentBestGenes[gene] = x;
			}
			else if (gene == 3 || gene == 8) {
				currentBestGenes[gene] = 100;
			}
			else {
				currentBestGenes[gene]--;
			}
			i--;
		}	
		System.out.print(Arrays.toString(currentBestGenes));
	}
/*	
    //Return the SUM of all routes values found from the random number generation.	
	public static double getFitness(Integer[] cities, double[][] randomRoute) {
		int size = randomRoute.length;
		double n = 0;
		for (int i = 0; i < size-1; i++) {
			n += randomRoute[cities[i]][cities[i+1]];
		}
		n += randomRoute[cities[size-1]][cities[0]]; //Add route Last Node to A.
		return n;
	}
*/		
}
