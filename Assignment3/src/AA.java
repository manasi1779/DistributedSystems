public class AA extends A {

  int aInt = 1;		

  AA() {
	aInt = 11;
  }
  public int intPlusPlus()	{
	return ++aInt;
  }
  public String toString()      {
        return this.getClass().getName() + ": " + aInt;
  }

  public static void main(String args[]) {
	AA aAA = new AA();
	A   aA = (A)aAA;
	aAA.intPlusPlus();
	aA.intPlusPlus();
	System.out.println(aA);
	System.out.println(aAA);
	System.out.println("aA: " + aA.aInt);
  }
}