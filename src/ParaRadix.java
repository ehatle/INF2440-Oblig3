import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
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

		for(Thread t : threads){
			try{
				t.join();
			} catch (InterruptedException IE) {
				IE.printStackTrace();
			}
		}
		return b;
	}//END sort
	/**
	* Syncronized method for declaring the maximum value in a.
	*
	* @param value The proposed new max
	* @param digit The current digit
	* @see synchronized
	**/
	synchronized void pushMax(int value){
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
			updateDigits(0);
	}//END pushMax
	synchronized void updateDigits(int i){
		numDigit = 1<<bit[i];
	}
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
			pushMax(findMax());//TODO: REMOVE PASSING OF CURRENT DIGIT NUMBER
			radixSort(0);
			swapAB();//TODO: FIND A WORKAROUND
			sync1();
			updateDigits(1);
			radixSort(bit[0]);
		}//END run
		/**
		* Abstraction barrier
		*
		* @param digit The current digit on which to sort
		* @param shift The the current amount of shift needed to sort on the specified digit
		**/
		void radixSort(int shift){
			this.shift = shift;

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
