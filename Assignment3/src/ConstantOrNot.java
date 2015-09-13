
import java.util.Vector;	// what does this line do?

class ConstantOrNot {

   private final int aInt = 1;
   private final String aString = "abc";
   private final Vector aVector = new Vector();

   public void doTheJob() {
	// aInt and aString are declared as final variables and below statements try to update the value of it
	// aInt = 3; why would this fail? 
	// aString = aString + "abc"; why would this fail?
	// Declaring vector as final means that vector variable cannot be pointed to by other vector;
	// So aVector = new Vector() will fail since aVector is final   	   
	aVector.add("abc");	// why does this work?
	}

    public static void main( String args[] ) {
	new ConstantOrNot().doTheJob();
    
    }
}