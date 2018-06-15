import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
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
/**
* Handler for sequential radix sorting. Only small changes
* were made to the original code in order to check if the
* sort was successful.
*
* 30 lines of code
* 
* CHANGELOG v1.1:
** radix2 now returns an int array.
*
* @author arnem
* @version 1.1
**/
class SekvensiellRadix{
	/**
	* Main sorting method.
	*
	* @param a The list that is to be sorted
	**/
	static int[] radix2(int [] a) {
		  //2 digit radixSort: a[]
		  int max = a[0], numBit = 2, n =a.length;
		 //a) finn max verdi i a[]
		  for (int i = 1 ; i < n ; i++)
			   if (a[i] > max) max = a[i];
		  while (max >= (1<<numBit))numBit++; //antall siffer i max

		  //bestem antall bit i siffer1 og siffer2
			int bit1 = numBit/2,
				  bit2 = numBit-bit1;
		  int[] b = new int [n];
		  radixSort( a,b, bit1, 0);   //første siffer fra a[] til b[]
		  radixSort( b,a, bit2, bit1);//andre siffer, tilbake fra b[] til a[]
		  return a;
	 }//END radix2
	 
	/** 
	* Sort 'a[]' on one digit where number of bits = 'maskLen',
	* shifted up 'shift' bits.
	*
	* @param a The list that is to be sorted into b
	* @param b The list that should recieve the sorted values from a
	* @param maskLen Number of bits in the digit that is being sorted
	* @param shift Number of bits to shift in order to find the digit
	**/
	static void radixSort ( int [] a, int [] b, int maskLen, int shift){
		  int acumVal = 0, j, n = a.length;
		  int mask = (1<<maskLen) -1;
		  int [] count = new int [mask+1];

		 //b) count=the frequency of each radix value in a
		  for (int i = 0; i < n; i++) {
			 count[(a[i]>> shift) & mask]++;
		  }

		 //c) Add up in 'count' - accumulated values
		  for (int i = 0; i <= mask; i++) {
			   j = count[i];
				count[i] = acumVal;
				acumVal += j;
		   }
		 //c) move numbers in sorted order a to b
		  for (int i = 0; i < n; i++) {
			 b[count[(a[i]>>shift) & mask]++] = a[i];
		  }
	}//END radixSort
}//END SekvensiellRadix
/**
* Handler for prallell radix sorting.
*
* 123 lines of code
*
* CHANGELOG v.02:
** Greatly increased performance
** More comments!
*
* @author	emillh
* @version	0.2
**/
class ParaRadix{
	int max, cores, numBit, numDigit;
	private CyclicBarrier cb, cb1;
	
	int[] a, b, bit, sumCount;
	private Thread[] threads;
	int[][] allCount;
	
	/**
	* Constructor
	* 
	* @param a The unsorted list
	**/
	ParaRadix(int[] a){
		max = 0;
		this.a = a;
		this.b = new int[a.length];
		this.bit = new int[2];
		cores = Runtime.getRuntime().availableProcessors();
		cb = new CyclicBarrier(cores+1);
		cb1 = new CyclicBarrier(cores);
		threads = new Thread[cores];
		allCount = new int[cores][];
		for(int i =0; i<cores-1; i++)
			threads[i] = new Thread(new RadixThread(i, i*(a.length/cores), (i+1)*(a.length/cores)));
		threads[cores-1] = new Thread(new RadixThread(cores-1, (cores-1)*(a.length/cores), a.length));
	}//END constructor
	/**
	* Abstraction layer
	*
	* @return the fully sorted list.
	**/
	int[] sort(){
		for(int i =0; i<cores; i++) threads[i].start();
		sync();
		sumCount = new int[numDigit];
		sync();
		sumCount = new int[numDigit];
		sync();//TODO: REPLACE WITH A join
		return b;
	}//END sort
	/**
	* Syncronized method for declaring the maximum value in a.
	*
	* @param value The proposed new max
	* @param digit The current digit
	* @see synchronized
	**/
	synchronized void pushMax(int value, int digit){
		if(digit==1){
			if(value > max){
				max = value;
			}
			numBit = 2;
			while (max >= (1<<numBit))
				numBit++; //number of bits in max
			//number of bits in digit1 and digit 2
			bit[0] = numBit/bit.length;
			bit[1] = numBit-bit[0];
			
			//number of possible digit values
			numDigit = 1<<bit[0];
		} else {//TODO: MOVE THIS OUTSIDE OF pushMax
			numDigit = 1<<bit[1];
		}
	}//END pushMax
	/**
	* A barrier for all threads. Including the main thread.
	**/
	void sync() {
		try {// Wait for all the threads to finish
			cb.await();
		} catch (BrokenBarrierException e) {// This is triggered when the barrier goes down
			System.out.println("A thread crossed the streams!");
		} catch (InterruptedException e) {// This really shouldn't ever happen.
			System.out.println("Who you gonna call?");
		}
	}//END sync
	/**
	* A barrier for all sorting threads.
	* NOTE: DOES NOT INCLUDE THE MAIN THREAD.
	**/
	void sync1() {
		try {// Wait for all the threads to finish
			cb1.await();
		} catch (BrokenBarrierException e) {// This is triggered when the barrier goes down
			System.out.println("A thread crossed the streams!");
		} catch (InterruptedException e) {// This really shouldn't ever happen.
			System.out.println("Who you gonna call?");
		}
	}//END sync1
	/**
	* Thread class for parallell radix sort
	*
	* @see Thread
	**/
	class RadixThread implements Runnable{
		int threadNr, start, stop, shift;
		/**
		* Constructor
		*
		* @param threadNr The current thread's ID number
		* @param start Inclusive start index in a from which the thread should sort
		* @param stop Exclusive stop index in a to which the thread should sort
		**/
		RadixThread(int threadNr, int start, int stop){
			this.threadNr = threadNr;
			this.start = start;
			this.stop = stop;
		}//END constructor
		/**
		* @see Thread.run()
		**/
		public void run(){
			radixSort(1, 0);
			swapAB();//TODO: FIND A WORKAROUND
			sync1();
			radixSort(2, bit[0]);
			sync();
		}//END run
		/**
		* Abstraction barrier
		*
		* @param digit The current digit on which to sort
		* @param shift The the current amount of shift needed to sort on the specified digit
		**/
		void radixSort(int digit, int shift){
			this.shift = shift;
			max = 0;
			if(digit == 1)
				max = findMax();
			pushMax(max, digit);//TODO: REMOVE PASSING OF CURRENT DIGIT NUMBER
			sync();
			allCount[threadNr] = countFreq();
			sync1();
			
			//calculating sumCount
			int numStop = (threadNr+1)*numDigit/cores;
			for (int i = threadNr*numDigit/cores; i<numStop; i++){
				sumCount[i] = 0;
				for(int j = 0; j<cores; j++) sumCount[i] += allCount[j][i];
			}
			
			sync1();
			sortToB();
			sync1();
		}//END radixSort
		/**
		* Abstraction barrier.
		*
		* @return The highest number in a with an index lower than stop and higher than start
		**/
		int findMax(){
			int myMax = a[start];
			for (int i = start+1 ; i < stop ; i++)
				if (a[i] > myMax)
					myMax = a[i];
			return myMax;
		}//END findMax
		/**
		* Counts the frequency of all of the current digits in a with an index lower than stop and higher than start
		* 
		* @return An integer array of the frequencies
		**/
		int[] countFreq(){
			int mask = numDigit -1;
			int[] count = new int[numDigit];
			for(int i =start; i<stop; i++)
				count[(a[i]>> shift) & mask]++;
			return count;
		}//END countFreq
		/**
		* Sorts all elements in a with an index lower than stop and higher or equal to start into b
		**/
		void sortToB(){
			int mask = numDigit -1;
			int[] prevCounts = new int[numDigit];
			for(int i = 0; i<numDigit; i++){
				for(int j =0; j<i; j++) prevCounts[i] +=sumCount[j];
				for(int j =0; j<threadNr; j++) prevCounts[i] +=allCount[j][i];
			}
			for(int i = start; i<stop; i++){
				int j = (a[i]>>shift)&mask;
				b[prevCounts[j]] = a[i];
				prevCounts[j]++;
			}
		}//END sortToB
		/**
		* Swaps all elements between a and b
		**/
		void swapAB(){
			for(int i = start; i<stop; i++){
				int tmp = a[i];
				a[i] = b[i];
				b[i] = tmp;
			}
		}//END swapAB
	}//END RadixThread
}//END ParaRadix