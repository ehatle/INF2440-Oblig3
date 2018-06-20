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
