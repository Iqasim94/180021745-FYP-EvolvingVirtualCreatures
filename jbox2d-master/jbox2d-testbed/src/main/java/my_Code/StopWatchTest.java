package my_Code;

import java.util.concurrent.TimeUnit;
//import org.apache.commons.lang3.time.StopWatch;
//import java.util.Random;
import org.jbox2d.testbed.tests.CarDemo;

public class StopWatchTest extends CarDemo{
	
	public static void main(String[] args) {

/*		int i = 0;
		StopWatch stopwatch = StopWatch.createStarted();

		while (stopwatch.getNanoTime() < 5e9) {
//			i++;
		}

		System.out.println(i);		
		System.out.println(stopwatch);
		System.out.println(stopwatch.getNanoTime());
		
		
		Random rand = new Random();
		float newGene = (float) (rand.nextInt(100) + 1) / 100;
		System.out.println(newGene);
	
		for (int i = 0; i < currentGenes.length-1; i++) {
			System.out.print(currentGenes[i].toString() + " , ");
		}
*/		
		double timeConv = 1.874e+10;
		long hours = 0;
		long minutes = 0;
		long seconds = 0;
		long millieseconds = TimeUnit.NANOSECONDS.toMillis((long) timeConv);
		
		while (millieseconds > 999) {
			millieseconds = millieseconds - 1000;
			seconds++;
		}
		while (seconds > 59) {
			seconds = seconds - 60;
			minutes++;
		}
		while (minutes > 59) {
			minutes = minutes - 60;
			hours++;
		}	
		
		String timeRec = hours + ":" + minutes + ":" + seconds + "." + millieseconds;
		System.out.print(timeRec);
	}

}
