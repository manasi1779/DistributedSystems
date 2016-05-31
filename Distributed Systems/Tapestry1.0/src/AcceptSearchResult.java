import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class AcceptSearchResult extends Thread{

	static ServerSocket aServerSocket;
	int port = 12345, port2 = 54321;
	Node myNode;

	public AcceptSearchResult( Node aNode )
	{
		myNode = aNode;
		try
		{
			aServerSocket = new ServerSocket(port2);
		}
		catch ( Exception e)
		{
			e.printStackTrace();
		}
	}

	public void run()
	{
		try
		{
			while(true) {

				Socket clnt = aServerSocket.accept();
				BufferedReader din = new BufferedReader (
						new InputStreamReader (clnt.getInputStream()));

				String operation = din.readLine();

				if(operation.equalsIgnoreCase("SearchResult"))
				{
					String result = din.readLine();

					if ( result.equalsIgnoreCase("false"))
					{
						System.out.println("File not found on the network!");
					}
					else
					{
						addFile(din);
					}
				}
				
				din.close();
				clnt.close();
			}
		}
		catch ( Exception e)
		{
			e.printStackTrace();
		}
	}

	public void addFile(BufferedReader din) {
		
		try
		{
			String filePath = System.getProperty("user.home");
			String myName = myNode.name;

			filePath += "/" + myName;
			File theDir = new File(filePath);

			// if the directory does not exist, create it
			if (!theDir.exists()) {
				//System.out.println("creating directory: " + filePath);
				boolean result = false;

				try {
					theDir.mkdirs();
					result = true;
				} 
				catch(SecurityException se) {
					System.err.println(se.getMessage());
				}        
				if(result) {    
					//System.out.println("DIR created");  
				}
			}
			else {
				//System.out.println("DIR exists");
			} 

			String pathTaken = din.readLine();
			String fileName = din.readLine();
			
			PrintWriter out = new PrintWriter(filePath + "/" + fileName);

			String myStringRead;
			while ((myStringRead = din.readLine()) != null)
			{

				out.println(myStringRead);
			}

			out.close();

			System.out.println("File created at " + filePath);
			System.out.println("Path taken: " + pathTaken);
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

}
