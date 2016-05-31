
public class Tapestry extends Thread {
	
	public Tapestry ()
	{
	}
	public static void main(String[] args) {
		
		Tapestry obj = new Tapestry();
		
		try
		{
			obj.start();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		Node myNode = new Node();
		
		NodeOperations addObj = new NodeOperations(myNode);
		AcceptRequest reqObj = new AcceptRequest(myNode);
		AcceptSearchResult searchObj = new AcceptSearchResult(myNode);
		
		try
		{
			(new Thread(addObj)).start();
			(new Thread(reqObj)).start();
			(new Thread(searchObj)).start();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

}
