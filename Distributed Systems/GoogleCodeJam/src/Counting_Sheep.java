import java.util.Scanner;

public class Counting_Sheep {

	static Scanner s = new Scanner(System.in);
	public static void main(String[] args) {
		int cases = s.nextInt();
		for(int i =0; i<cases; i++){
			s.nextLine();
			countNumbers(s.nextInt(),i+1);
			System.out.println();
		}
	}
	
	public static void countNumbers(int number, int caseNo){
		if(number == 0){
			System.out.println("Case #"+caseNo+": INSOMNIA");
			return;
		}
		int lastNo = number, digitsEncountered = 0;
		boolean numChecked[] = new boolean[10];
		for(int i =1; i<1000 ; i++){
			int newNumber = number *i;
			lastNo = newNumber;
			while(newNumber>0){
				int x = newNumber%10;
				if(!numChecked[x]){
					numChecked[x] = true;
					digitsEncountered++;
				}
				if(digitsEncountered == 10){
					break;
				}
				newNumber/=10;
			}
			if(digitsEncountered == 10){
				break;
			}			
		}
		System.out.print("Case #"+caseNo+": "+lastNo);
	}
}
