import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class NodeOperations extends Thread {

	int port = 12345;
	Node theNode;
	final int limitOfHash = 100;
	String root = "glados.cs.rit.edu";

	public NodeOperations(Node theObj)
	{
		theNode = theObj;
	}

	public void run()
	{
		Scanner read = new Scanner(System.in);

		String entered;
		boolean joined = false;
		
		while ( true )
		{
			System.out.println();
			System.out.println("~~~~~~~~~~~~~~Options~~~~~~~~~~~~~~");
			System.out.println("-----------------------------------");
			
			if ( joined == false )
			{
				System.out.println("Join");
			}
			
			else
			{
				System.out.println("Add *filename*");
				System.out.println("Search *filename*");
			}
			
			
			System.out.println("-----------------------------------");
			System.out.println();
			System.out.println("Enter your choice: ");
			
			entered = read.nextLine();
			System.out.println();
			String[] optionArr = entered.split(" ");
			
			if ( entered.equalsIgnoreCase("join"))
			{
				joined = true;
				addNode();
				//break;
			}

			if ( optionArr[0].equalsIgnoreCase("add"))
			{
				String fileName = optionArr[1];
				System.out.println("Got to add " + fileName + " in the network");
				
				addFile(optionArr);
			}
			
			else if ( optionArr[0].equalsIgnoreCase("search") )
			{
				String fileName = optionArr[1];
				System.out.println("Have to search for " + fileName + " in the network");
				
				searchFile(optionArr);
			}
		}

	}
	
	
	public void searchFile(String[] optionArr) 
	{
		
		String fileName = optionArr[1];
		String[] fileArr = fileName.split("/");

		int length = fileArr.length;

		String theFileName = fileArr[length-1];
		
		int hashOfFile = theFileName.hashCode() % limitOfHash;
		
		
		if ( hashOfFile < 0 )
		{
			hashOfFile *= (-1);
		}
		
		String theHash = String.valueOf(hashOfFile) + "\n";
		
		if ( hashOfFile / 10 == 0 )
		{
			theHash = "0" + theHash;
		}
		
		try
		{
			Socket mySock = new Socket(root, port);

				OutputStream os = mySock.getOutputStream();

				String operation = "search\n";
				String requestor = theNode.name + "\n";
				String path = "glados" + "\n";

				byte[] operationArr = new byte[(int) operation.length()];
				operationArr = operation.getBytes();

				byte[] hashArr = new byte[(int) theHash.length()];
				hashArr = theHash.getBytes();
				
				byte[] reqArr = new byte[(int) requestor.length()];
				reqArr = requestor.getBytes();
				
				byte[] pathArr = new byte[(int) path.length()];
				pathArr = path.getBytes();
				
				String myfileName = theFileName + "\n";

				byte[] fName = new byte[(int) myfileName.length()];
				fName = myfileName.getBytes();

				os.write(operationArr, 0, operationArr.length);
				os.write(fName, 0, fName.length);
				os.write(hashArr, 0, hashArr.length);
				os.write(reqArr, 0, reqArr.length);
				os.write(pathArr, 0, pathArr.length);

				os.flush();
				os.close();
				mySock.close();
		}
		catch ( Exception e )
		{
			System.err.println(e.getMessage());
		}
	}

	public void addFile( String[] optionArr)
	{
		String fileName = optionArr[1];
		String[] fileArr = fileName.split("/");

		int length = fileArr.length;

		String theFileName = fileArr[length-1];
		
		int hashOfFile = theFileName.hashCode() % limitOfHash;
		
		
		
		if ( hashOfFile < 0 )
		{
			hashOfFile *= (-1);
		}
		
		String theHash = String.valueOf(hashOfFile) + "\n";
		
		if ( hashOfFile / 10 == 0 )
		{
			theHash = "0" + theHash;
		}
		
		try
		{
			File fileToFind = new File(fileName);

			if ( fileToFind.exists() && fileToFind.isFile())
			{
				Socket mySock = new Socket(root, port);

				OutputStream os = mySock.getOutputStream();

				String operation = "insert\n";

				byte[] operationArr = new byte[(int) operation.length()];
				operationArr = operation.getBytes();

				byte[] hashArr = new byte[(int) theHash.length()];
				hashArr = theHash.getBytes();
				
				String myfileName = theFileName + "\n";

				byte[] fName = new byte[(int) myfileName.length()];
				fName = myfileName.getBytes();

				byte[] fileContents = new byte[(int) fileToFind.length()];
				FileInputStream fisToClient = new FileInputStream
						(fileToFind);
				BufferedInputStream bisClient = new BufferedInputStream
						(fisToClient);
				bisClient.read(fileContents, 0, fileContents.length);

				os.write(operationArr, 0, operationArr.length);
				os.write(fName, 0, fName.length);
				os.write(hashArr, 0, hashArr.length);
				os.write(fileContents, 0, fileContents.length);

				os.flush();
				os.close();
				mySock.close();
			}

			else
			{
				System.err.println("File not found");
			}


		}
		catch ( Exception e )
		{
			System.err.println(e.getMessage());
		}
	}

	public void addNode( )
	{
		if ( !theNode.name.equalsIgnoreCase("glados"))
		{
			System.out.println("Connecting to glados");
			try
			{
				Socket mySocket = new Socket ( theNode.root, port );
				OutputStream os = mySocket.getOutputStream();

				String operation = "join\n";
				String myName = theNode.name + "\n";
				String id = theNode.ID + "\n";

				System.out.println("Sending " + myName + " " + id);


				byte[] opArr = new byte[(int) operation.length()];
				opArr = operation.getBytes();

				byte[] nameArr = new byte[(int) myName.length()];
				nameArr = myName.getBytes();

				byte[] idArr = new byte[(int) id.length()];
				idArr = id.getBytes();

				os.write(opArr, 0, opArr.length);
				os.write(nameArr, 0, nameArr.length);
				os.write(idArr, 0, idArr.length);

				os.flush();
				System.out.println("Waiting for response");
				Scanner sc = new Scanner(mySocket.getInputStream());
				while(!sc.hasNext());
				String response = sc.nextLine();
				System.out.println(response);
				if(response.equals("FILES"))
					receiveFiles(mySocket);
				os.close();				
				mySocket.close();
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Files received by root are stored at local
	 */
	public static void receiveFiles(Socket s){
		Scanner sc;
		try {
			System.out.println("In receive");
			sc = new Scanner(s.getInputStream());
			int noOfFiles = sc.nextInt();
			sc.nextLine();
			DataInputStream is = new DataInputStream(s.getInputStream());
			File homedir = new File(System.getProperty("user.home"));
			File destPath = new File(homedir.getAbsolutePath()+"/"+InetAddress.getLocalHost().getHostName()+"/");
			destPath.mkdirs();
			for(int i=0; i<noOfFiles; i++){			
				try {
					String fileName = sc.nextLine();
					System.out.println("receiving "+fileName);
					File x = new File(homedir.getAbsolutePath()+"/"+InetAddress.getLocalHost().getHostName()+"/"+fileName);
					int fileLength = sc.nextInt();
					sc.nextLine();
					x.createNewFile();
					FileOutputStream out = new FileOutputStream(x);
					byte[] fileBytes = new byte[fileLength];
					is.readFully(fileBytes, 0, fileLength);
					out.write(fileBytes);
		    	    out.close();
		    	    System.out.println("Received "+fileName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
