class X {

    public static void main( String args[] ) {
    
	int n = 0;
	
	here:

	while ( true )  {
		// loop is executed for start value of n = 0, n = 2
		while ( ( ( n != 3 ) || ( n != 5 ) ) && ( n < 4 ) )  {
			//value of n is 1 while executing below statement
			//hence equality is false and statement a is not printed
			//for 2nd iteration n = 3 after this statement
			if ( ++n == 0 )
				System.out.println("a/	n is " + n );
			//value of n is 2 after below statement is executed
			//hence equality is true for n = 1 and statement b is printed
			//for 2nd iteration n = 4 after this statement
			else if ( n++ == 1 )    
				System.out.println("b/	n is " + n );
			//For 1st iteration this is not executed and value of n = 2 at the end of 1st iteration.
			//for 2nd iteration n = 5 after this statement i.e. at the end of 2nd iteration
			else if ( n++ == 2 )
				System.out.println("c/	n is " + n );
			//else block is not executed for 1st iteration since above if statement was true.
			//for 2nd iteration else block is executed and statement d is printed
			else 
				System.out.println("d/	n is " + n );

			System.out.println("	executing break here");
		}
		//n = 5 while the following statement is getting executed 
		//since 5 mod 2 is not zero, condition n % 3 != 0 is executed
		//since 5 mod 3 is not zero, 3 is printed
		System.out.println( n % 2 == 0 ?
					( n == 4 ? "=" : "!" )
				      : ( n % 3 != 0 ? "3" : "!3" ));
		break here;
	}
    }
}