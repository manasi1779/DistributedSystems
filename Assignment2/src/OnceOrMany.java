/**
  * OnceOrMany.java
  * 
  * Version: 1.0
  * 
  * Revisions:  
  */
  
/**
  * This program demonstrates the basic difference between string object and string literal
  *     
  * @author				Sri Praneeth Iyyapu 
  * @author             Manasi Sunil Bharde
  */

class OnceOrMany {

/**
  * This method is used to evaluate various string expressions
  * @param literal          the first string
  * @param aNewString       the second string 
  *
  * @return	            	comparision of both the strings 
  */

	public static boolean singelton(String literal, String aNewString)	{
		return ( literal == aNewString );
	}

/**
  * This is the main program
  *
  * @param args       ignored  
  */
 
	public static void main( String args[] ) {
		
		// A new string value "xyz" is stored in the string constant pool if the value is
		// not present already and the string reference variable aString is pointed to that value
		
		String aString = "xyz";
		
		// First the string "1.	xyz == aString:	" concatenates with the string "xyz" and the 
		// result "1.	xyz == aString: xyz" is compared with aString which is "xyz" and prints out false
		
		System.out.println("1.	xyz == aString:	" +     "xyz" == aString   );
		
		// Since aString points to "xyz" and it is again compared to  "xyz"(in other words
		// both of them point to the same memory location), it evaluates to true and is concatenated 
		// with "2.	xyz == aString:	" and prints out 2.      xyz == aString: true
		
		System.out.println("2.	xyz == aString:	" +   ( "xyz" == aString ) );
		
		// A new string object is created with the value "xyz"

		String newString = new String("xyz");
		
		// "xyz" is taken from the string constant pool and newString points to some other object
		// in the heap. As both of them points to different objects and thus have different references,
		// the expression ("xyz" == newString) evaluates to false and is concatenated with 
		// "xyz == new String(xyz)\n	" to print out xyz == new String(xyz)
		//                                                     false
		
		System.out.println("xyz == new String(xyz)\n	" + ("xyz" == newString) );
		
		// "xyz" from string constant pool is compare with itself(both point to same memorylocation in the string 
		// constant pool) hence evaluates to true and prints out 1: true

		System.out.println("1: " + singelton("xyz", "xyz"));
		
		// "xyz" from string constant pool and new String("xyz") refer to values of different objects
		// hence evaluates to false and prints out 2: false
		
		System.out.println("2: " + singelton("xyz", new String("xyz") ));
		
		// "xyz" is compared to the concatenation of "xy" + "z" which is nothing but "xyz"(in other words
		// both of them point to the same memory location) hence evaluates to true and prits out 3: true
		
		System.out.println("3: " + singelton("xyz", "xy" + "z"));
		
		// concatenation of "x" + "y" + "z" which is nothing but "xyz" is compared to itself (in other words
		// both of them point to the same memory location) hence evaluates to true and prits out 4: true
		
		System.out.println("4: " + singelton("x" + "y" + "z", "xyz"));
		
		// "x" + "y" + new String("z") and "xyz" are pointed to different memory loacations in the heap
		// hence their comparision evaluates to false and prints out 5: false
		
		System.out.println("5: " + singelton("x" + "y" + new String("z"), "xyz"));
		
		// concatenation of "x" + ( "y" + "z") which is nothing but "xyz" is compared to itself (in other words
		// both of them point to the same memory location) hence evaluates to true and prits out 6: true
		
		System.out.println("6: " + singelton("x" + ( "y" + "z"), "xyz"));
		
		// concatenation of 'x' + ( "y" + "z") which is nothing but "xyz" is compared to itself (in other words
		// both of them point to the same memory location) hence evaluates to true and prits out 7: true
		
		System.out.println("7: " + singelton('x' + ( "y" + "z"), "xyz"));
	}
}
