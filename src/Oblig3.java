import java.util.Arrays;
import java.util.Random;
/**
* Test-class for testing the implementation of assignment
* 3. The class was made to test the work on the assignment.
* Little effort was put into it.
*
* 42 lines of code.
*
* CHANGELOG v0.2:
** Fixed crash when run with a higher number of cores.
*
* @version 0.2
* @author emillh
**/
public class Oblig3 {
	public static void main(String[] args){
		int[] n = {1000,10000,100000,1000000,10000000,100000000};
		double[][] results = new double[2][n.length];
		for(int x = 0; x<n.length; x++){
			long[][] times = new long[2][3];
			int[] a = new int[n[x]];

			for(int y =0; y<times[0].length; y++){
				Random r = new Random(30035);
				for(int i = 0; i<a.length; i++) a[i] = r.nextInt(n[x]-1);

				SekvensiellRadix s = new SekvensiellRadix();
				ParaRadix p = new ParaRadix(a);

				long time = System.nanoTime();
				int[] sorted = p.sort();
				times[0][y] = System.nanoTime() - time;
				if(!(sorttester(sorted))) System.exit(1);

				r = new Random(30035);
				for(int i = 0; i<a.length; i++) a[i] = r.nextInt(n[x]-1);

				time = System.nanoTime();
				sorted = s.radix2(a);
				times[1][y] = System.nanoTime() - time;
				if(!(sorttester(sorted))) System.exit(1);
			}
			Arrays.sort(times[0]);
			Arrays.sort(times[1]);

			results[0][x] = (double) times[0][1]/1000000000.0;
			results[1][x] = (double) times[1][1]/1000000000.0;
			System.out.printf("\nFinished sorting n = %d", n[x]);
		}
		System.out.printf("\n\n\t\t===== RESULTS =====");
		for(int i = 0; i<n.length; i++) System.out.printf("\n\n\t\t  --- N = %d ---\nSequential time:\t%.6f\nParallel time:\t\t%.6f\tSpeedup: %.2f\n", n[i], results[1][i], results[0][i], results[1][i]/results[0][i]);
	}//END main
	/**
	* Tests if a list is successfully sorted ascendingly.
	*
	* @param x The sorted list to be tested
	**/
	static boolean sorttester(int[] x){
		for(int i = 0; (i+1)<x.length; i++){
			if(x[i]>x[i+1]) {
				System.out.printf("\nSORT FAILED! x[%d] = %d is larger than x[%d] = %d\n\n", i, x[i], i+1, x[i+1]);
				return false;
			}
		}
		return true;
	}//END sorttester
}//END Oblig3
