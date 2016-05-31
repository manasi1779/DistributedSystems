import java.util.Scanner;

public class Node extends Server implements NodeInterface {
	
	static Scanner s = new Scanner(System.in);
	static int myIndex;
	//static ArrayList
	
	public static void main(String[] args) {
		int index = Integer.parseInt(args[0]);
		Node node = new Node(index);
	}
	
	public Node(int index) {
		super(index);
		myIndex = index;
	}	
	
	public void menu(){
		System.out.println("Menu");
		System.out.println("1. Insert File");
		System.out.println("2. Search File");
		System.out.println("3. View");
		System.out.println("4. Join");
		System.out.println("5. Leave");
		System.out.println("6. Exit");
		System.out.println("Enter option");
	}
	
	public void switchToOption(){
		int option = Integer.parseInt(s.nextLine());
		switch(option){
		case 1:{
			System.out.println("Enter File Name:");
			String fileName = s.nextLine();
			insertObject(fileName);
			break;
		}
		case 2:{
			System.out.println("Enter File Name:");
			String fileName = s.nextLine();
			searchObject(fileName);
		}
		case 3:{
			//view();
			break;
		}
		case 4:	{		
			join();
			break;
		}
		case 5:{
			leave();
			break;
		}
		case 6:{
			//exit = true;
			break;
		}
		default:{
			System.out.println("Wrong choice");
		}
		
		}
	}

	@Override
	public void publishObject(ObjectDataInterface object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unpublishObject(ObjectDataInterface object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void routeToObject(ObjectDataInterface object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void routeToNode(NodeInterface node, ObjectDataInterface object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getGUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void multicast() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reRouteObjects() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyNeighbors() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTable() {
		// TODO Auto-generated method stub
		
	}
	
	private void connectTo(int receiver){
		
	}
	
	@Override
	public void insertObject(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void searchObject(String name) {
		
	}
	

	@Override
	public void join() {
		//int ID = getHash(addressTable.get(myIndex));
		String node = null;
		System.out.println("Select node to get in Chord");
		node = s.nextLine();
	}

	@Override
	public void leave() {
		
	}

}