/**
  * Factorization.java
  * 
  * Version: 1.0
  * 
  * Revisions:  
  */

/**
  * This program dispays the prime factorization of a particular number
  *     
  * @author			  Sri Praneeth Iyyapu 
  * @author           Manasi Sunil Bharde
  */

public class Factorization{

/**
  * This is the main program.
  *
  * @param args                takes the number given from command line
  */
  
	public static void main(String []args){
		
		String str=new String("");
		int num = Integer.parseInt(new String(args[0]));
		int org_num = num;
		
		for (int i = 2; i <= num; i++) {
			
			while (num % i == 0) {
				
				str = str+String.valueOf(i)+" "+"*"+" ";
				num = num/i;
				
			}
			
		}
		
		// Prints the prime factorization of the number
		
		System.out.println(org_num+" = "+str+"\b\b\b"+" "+" ");
	}
}
