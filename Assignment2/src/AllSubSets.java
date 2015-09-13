/**
 2       * A program that gives all combinations of people that may attend 
 3       * party for which n people are invited.
 4       * Pass number of people as argument.
 5       * @version   1
 6       *
 7       * @author1    Manasi Sunil Bharde
 8       * @author2    Sri Praneeth Iyyapu
 9       * Revisions:
10       *      $Log$
11       */

class Subsets {
	Integer numberOfPeople;
	ArrayList<String> combinations;
	
	public Subsets(){
		combinations = new ArrayList<String>();
	}
	/**
	 * 
	 * @param args 1. Number of people invited for party
	 */
	public static void main(String[] args) {
		Subsets ss = new Subsets();
		ss.numberOfPeople = Integer.parseInt(args[0]);
		ss.getSets();
		// Sorts combinations list in ascending order as desired in output
		//	Collections.sort(ass.combinations);
		System.out.print("{ {}, ");
		for(int index = 0; index<ss.combinations.size(); index++)
			System.out.print(" {"+ss.combinations.get(index)+"}, ");
		System.out.print("\b\b"+" "+"}");			
	}

	/**
	 * Creates list of combinations which represents people attending party.
	 */
	private void getSets(){
		for(int i=1; i<=numberOfPeople; i++){			
			combinations.add(""+i);
			for(int k=i;k<numberOfPeople;k++){
				String exp = ""+i;			
				for(int j=k; j<numberOfPeople; ){				
				exp=exp.concat(""+(++j));
				//Integer combination = Integer.parseInt(exp);
				combinations.add(exp);}
			}
		}
	}
}

class ArrayList<T>{
	int MAX_SIZE = 1000;
	int index;
	Object[] array;
	
	public ArrayList(){
		array = new Object[MAX_SIZE];
		index = 0;
	}
	
	public int size(){
		return index;
	}
	
	public void add(T item){
		array[index++] = item;
	}
	
	public T get(int index){
		return (T) array[index];
	}
}
