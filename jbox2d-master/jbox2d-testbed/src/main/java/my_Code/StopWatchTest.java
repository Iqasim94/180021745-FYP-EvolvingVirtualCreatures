package my_Code;

import java.util.Random;

import org.apache.commons.lang3.time.StopWatch;

public class StopWatchTest {
	
	public static void main(String[] args) {

/*		int i = 0;
		StopWatch stopwatch = StopWatch.createStarted();

		while (stopwatch.getNanoTime() < 5e9) {
//			i++;
		}

		System.out.println(i);		
		System.out.println(stopwatch);
		System.out.println(stopwatch.getNanoTime());
*/		
		
		Random rand = new Random();
		float newGene = (float) (rand.nextInt(100) + 1) / 100;
		System.out.println(newGene);
	}

}
