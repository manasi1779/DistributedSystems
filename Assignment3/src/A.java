public class A {

  int aInt = 1;		

  A() {
	aInt = 11;
  }
  public int intPlusPlus()	{
	return ++aInt;
  }
  public String toString()	{
	return this.getClass().getName() + ": " + aInt;
  }

  public static void main(String args[]) {
	A aA = new A();
	aA.intPlusPlus();
	System.out.println(aA);
  }
}