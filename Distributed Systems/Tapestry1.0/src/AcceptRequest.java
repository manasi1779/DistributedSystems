import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class AcceptRequest extends Thread {

	final int maxIndices = 10;
	static ServerSocket aServerSocket;
	int port = 12345, port2 = 54321;
	Node myNode;

	public AcceptRequest( Node aNode )
	{
		myNode = aNode;
		try{
			aServerSocket = new ServerSocket(port);
		}
		catch ( Exception e){
			e.printStackTrace();
		}
	}

	public void run()
	{
		try{
			while(true) {
				Socket clnt = aServerSocket.accept();
				BufferedReader din = new BufferedReader (
						new InputStreamReader (clnt.getInputStream()));
				String operation = din.readLine();
				if ( operation.equalsIgnoreCase("join")){
					//System.out.println("A new node wants to join");
					String nameOfNode = din.readLine();
					String id = din.readLine();
					//System.out.println("Received name " + nameOfNode + " id " + id);
					addToMyRoutingTable( nameOfNode, id );
					sendFiles(nameOfNode, clnt);
					din.close();
					clnt.close();
				}else if ( operation.equalsIgnoreCase("insert")){
					String fileName = din.readLine();
					String fileHash = din.readLine();
					System.out.println("Received fileName " + fileName + " with hash " + fileHash);
					int hashIndex0 = Character.getNumericValue(fileHash.charAt(0));
					int hashIndex1 = Character.getNumericValue(fileHash.charAt(1));
					findDestination(hashIndex0, hashIndex1, fileName, fileHash, din);
					din.close();
					clnt.close();
				}else if ( operation.equalsIgnoreCase("addFile")){
					//add the file on self
					System.out.println("Going to store file on myself");
					String fileName = din.readLine();
					String fileHash = din.readLine();
					addFile(fileName, fileHash, din);
					din.close();
					clnt.close();
				}else if ( operation.equalsIgnoreCase("search")){
					String fileName = din.readLine();
					String fileHash = din.readLine();
					String requestor = din.readLine();
					String path = din.readLine();
					System.out.println("Have to search for " + fileName + " with hash " + fileHash);
					int hashIndex0 = Character.getNumericValue(fileHash.charAt(0));
					int hashIndex1 = Character.getNumericValue(fileHash.charAt(1));
					findFileLocation(hashIndex0, hashIndex1, fileName, fileHash, requestor, path);
					din.close();
					clnt.close();
				}else if ( operation.equalsIgnoreCase("searchOnMe")){
					String fileName = din.readLine();
					String fileHash = din.readLine();
					String requestor = din.readLine();
					String path = din.readLine();
					searchFile(fileName, fileHash, requestor, path);
				}else{
					din.close();
					clnt.close();
				}
			}
		}catch ( Exception e ){
			e.printStackTrace();
		}
	}

	public void searchFile(String theFileName, String fileHash, String requestor, String route)
	{
		//direct search on present node
		try{
			String path = System.getProperty("user.home");
			String myName = InetAddress.getLocalHost().getHostName();
			path += "/" + myName;
			String fullPath = path +  "/" + theFileName;
			File fileToFind = new File(fullPath);
			if ( fileToFind.exists() && fileToFind.isFile()){
				String operation = "SearchResult\n";
				byte[] opArr = new byte[(int) operation.length()];
				opArr = operation.getBytes();
				String fileExists = "True\n";
				String fileNameForClient = theFileName + "\n";
				byte[] foundArr = new byte[(int) fileExists.length()];
				foundArr = fileExists.getBytes();
				byte[] fNArr = new byte[(int) fileNameForClient.length()];
				fNArr = fileNameForClient.getBytes();
				byte[] fileContents = new byte[(int) fileToFind.length()];
				FileInputStream fisToClient = new FileInputStream
						(fileToFind);
				BufferedInputStream bisClient = new BufferedInputStream
						(fisToClient);
				bisClient.read(fileContents, 0, fileContents.length);
				String requestorHost = requestor + ".cs.rit.edu";
				Socket sock = new Socket(requestorHost, port2);
				String toSendToClient = route + "\n"; //trail
				byte[] cliArr = new byte[(int) toSendToClient.length()];
				cliArr = toSendToClient.getBytes();
				OutputStream os = sock.getOutputStream();
				os.write(opArr, 0, opArr.length);
				os.write(foundArr, 0, foundArr.length);
				os.write(cliArr, 0, cliArr.length);
				os.write(fNArr, 0, fNArr.length);
				os.write(fileContents, 0, fileContents.length);
				os.flush();
				bisClient.close();
				fisToClient.close();
				sock.close();
			}else{
				String operation = "SearchResult\n";

				byte[] opArr = new byte[(int) operation.length()];
				opArr = operation.getBytes();

				String fileExists = "False\n";

				byte[] foundArr = new byte[(int) fileExists.length()];
				foundArr = fileExists.getBytes();

				String requestorHost = requestor + ".cs.rit.edu";
				Socket sock = new Socket(requestorHost, port2);
				OutputStream os = sock.getOutputStream();

				os.write(opArr, 0, opArr.length);
				os.write(foundArr, 0, foundArr.length);
				os.flush();
				sock.close();
			}
		}catch ( Exception e){
			e.printStackTrace();
		}
	}



	public void searchFileOnParticularNode(String nodeName, String fileName, String fileHash, String requestor, String path) {

		//send direct search on only this node. No need to check surrogate
		try{
			String host = nodeName + ".cs.rit.edu";
			Socket mySock = new Socket(host, port);
			OutputStream os = mySock.getOutputStream();
			String operation = "searchOnMe\n";
			fileHash += "\n";
			requestor += "\n";
			path +=  "\n";
			byte[] operationArr = new byte[(int) operation.length()];
			operationArr = operation.getBytes();
			byte[] hashArr = new byte[(int) fileHash.length()];
			hashArr = fileHash.getBytes();
			byte[] reqArr = new byte[(int) requestor.length()];
			reqArr = requestor.getBytes();
			byte[] pathArr = new byte[(int) path.length()];
			pathArr = path.getBytes();
			String myfileName = fileName + "\n";
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
		}catch ( Exception e ){
			System.err.println(e.getMessage());
		}
	}



	private void beginSearchOnAnotherNode(String nodeName, String fileName, String fileHash, String requestor,
			String path) {
		//will resend search req to that node
		try{
			String host = nodeName + ".cs.rit.edu";
			Socket mySock = new Socket(host, port);
			OutputStream os = mySock.getOutputStream();
			String operation = "search\n";
			fileHash += "\n";
			requestor += "\n";
			path += "\n";
			byte[] operationArr = new byte[(int) operation.length()];
			operationArr = operation.getBytes();
			byte[] hashArr = new byte[(int) fileHash.length()];
			hashArr = fileHash.getBytes();
			byte[] reqArr = new byte[(int) requestor.length()];
			reqArr = requestor.getBytes();
			byte[] pathArr = new byte[(int) path.length()];
			pathArr = path.getBytes();
			String myfileName = fileName + "\n";
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
		}catch ( Exception e ){
			System.err.println(e.getMessage());
		}
	}

	public void findFileLocation(int hash0, int hash1, String fileName, String fileHash, String requestor, String path)
	{
		int currentNodeID0 = Character.getNumericValue(myNode.ID.charAt(0));
		int currentNodeID1 = Character.getNumericValue(myNode.ID.charAt(1));

		//First index matched
		if ( currentNodeID0 == hash0 ){
			if ( currentNodeID1 == hash1 ){
				System.out.println("Found exact match with myself. Have to search in my directory " + myNode.name);
				if ( ! myNode.name.equalsIgnoreCase("glados")){
					path += " -> " + myNode.name;
				}
				searchFile(fileName, fileHash, requestor, path);
			}//Look for exact match with second index
			else if ( myNode.levelOne[hash1].size() != 0){
				//We found an exact match
				System.out.println("Found exact match at " +  myNode.levelOne[hash1].get(0)
						+ " with name " + myNode.levelOneNodeNames[hash1].get(0));
				/*if ( path.equals("empty"))
				{
					path = myNode.name;
				}
				else  if ( !path.contains(myNode.name))
				{
					path += " -> " + myNode.name;
				}*/
				if ( ! myNode.name.equalsIgnoreCase("glados"))
				{
					path += " -> " + myNode.levelOneNodeNames[hash1].get(0);
				}
				searchFileOnParticularNode(myNode.levelOneNodeNames[hash1].get(0), fileName, fileHash, requestor, path);
				//addFileToAnotherNode(fileName, fileHash, din, myNode.levelOneNodeNames[hash1].get(0));
			}//Exact match for second digit is not present, got to look for next available one in level one
			else{
				System.out.println("No exact match found, so going to check for a surrogate");
				boolean foundIndex = false;
				int savedIndex = -1;
				String nodeName = "";
				int currentDifference = hash1 - currentNodeID1;
				int parsingDifference = 100;

				for ( int temp = hash1; temp < maxIndices; temp++ ){
					if ( myNode.levelOne[temp].size() > 0){
						parsingDifference = hash1 - temp;
						if ( parsingDifference >= 0 && (parsingDifference < currentDifference || currentDifference < 0 ) ){
							savedIndex = temp;
							foundIndex = true;
						}
					}
				}
				if ( !foundIndex ){
					for ( int temp = 0; temp < hash1; temp++ ){
						if ( myNode.levelOne[temp].size() > 0){
							parsingDifference = hash1 - temp;
							if ( parsingDifference >= 0 && (parsingDifference < currentDifference || currentDifference < 0 ) ){
								savedIndex = temp;
								foundIndex = true;
							}
						}
					}
				}

				if ( !foundIndex ){
					System.out.println("No other is closer, have to check on myself " + myNode.name);
					if ( ! myNode.name.equalsIgnoreCase("glados")){
						path += " -> " + myNode.name;
					}
					searchFile(fileName, fileHash, requestor, path);
				}//Found some surrogate
				else{
					String nameOfTheNode = "";
					if ( myNode.levelOne[savedIndex].size() > 1 ){
						int minDistance = 100;
						int sizeToParse = myNode.levelOne[savedIndex].size();
						int parsed = 0;
						int position;
						while ( parsed < sizeToParse ){
							int currentDistance;
							int theHashOfNode = Character.getNumericValue(myNode.levelOne[savedIndex].get(parsed).charAt(1));
							currentDistance = hash1 - theHashOfNode;
							if ( currentDistance >= 0 && currentDistance < minDistance ){
								position = parsed;
								minDistance = currentDistance;
								nameOfTheNode = myNode.levelOneNodeNames[savedIndex].get(parsed);
							}
							parsed++;
						}
					}else{
						nameOfTheNode = myNode.levelOneNodeNames[savedIndex].get(0);
					}
					System.out.println("Found surrogate " + nameOfTheNode);
					//if ( ! myNode.name.equalsIgnoreCase("glados"))
					//{
						path += " -> " + nameOfTheNode;
					//}
					searchFileOnParticularNode(nameOfTheNode, fileName, fileHash, requestor, path);
				}
			}
		}//End of first index matched
		//First itself did not match so look for first index match
		else
		{
			System.out.println("First index has not matched. So now I need to look for someone starting with first index");

			if ( myNode.levelZero[hash0].size() != 0 )
			{
				//found match with 0th index, so ping any one of these. We will ping first one
				System.out.println("This node matched index 0 digit. Transferring request to " + myNode.levelZero[hash0].get(0)
						+ " with name " + myNode.levelZeroNodeNames[hash0].get(0));

				//sendFileToAnotherNode(fileName, fileHash, din, myNode.levelZeroNodeNames[hash0].get(0));
				
				beginSearchOnAnotherNode(myNode.levelZeroNodeNames[hash0].get(0), fileName, fileHash, requestor, path);
			}

			else
			{
				System.out.println("Nobody with first index match. So need to look for a surrogate.");

				//System.out.println("No exact match found, so going to check for a surrogate");
				boolean foundIndex = false;
				int savedIndex = -1;
				String nodeName = "";
				int currentDifference = hash0 - currentNodeID0;
				//System.out.println("My difference " + currentDifference );
				int parsingDifference = 100;

				for ( int temp = hash0; temp < maxIndices ; temp++ )
				{
					if ( myNode.levelZero[temp].size() > 0)
					{
						parsingDifference = hash0 - temp;

						if ( parsingDifference >= 0 && (parsingDifference < currentDifference || currentDifference < 0 ) )
						{
							savedIndex = temp;
							foundIndex = true;
						}
					}
				}

				if ( !foundIndex )
				{
					for ( int temp = 0; temp < hash0; temp++ )
					{
						if ( myNode.levelZero[temp].size() > 0)
						{
							parsingDifference = hash0 - temp;

							if ( parsingDifference >= 0 && (parsingDifference < currentDifference || currentDifference < 0 ) )
							{
								savedIndex = temp;
								foundIndex = true;
							}
						}
					}
				}

				if ( !foundIndex )
				{
					System.out.println("No other is closer, going to search on myself " + myNode.name);
					if ( ! myNode.name.equalsIgnoreCase("glados"))
					{
						path += " -> " + myNode.name;
					}
					searchFile(fileName, fileHash, requestor, path);
				}

				else
				{
					String nameOfTheNode = "";

					if ( myNode.levelZero[savedIndex].size() > 1 )
					{
						//System.out.println("There is more than 1 node at this index");
						int minDistance = 100;
						int sizeToParse = myNode.levelZero[savedIndex].size();
						int parsed = 0;
						int position;


						while ( parsed < sizeToParse )
						{
							int currentDistance;
							int nodeHash = Integer.parseInt(myNode.levelZero[savedIndex].get(parsed));
							int totalFileHash = (hash0 * 10) + hash1;
							//int theHashOfNode = Character.getNumericValue(myNode.levelZero[savedIndex].get(parsed).charAt(1));
							//currentDistance = hash0 - theHashOfNode;
							currentDistance = totalFileHash - nodeHash;
							System.out.println("For " + myNode.levelZeroNodeNames[savedIndex].get(parsed) + " the diff is " + currentDifference);
							if ( currentDistance >= 0 && currentDistance < minDistance )
							{
								position = parsed;
								minDistance = currentDistance;
								nameOfTheNode = myNode.levelZeroNodeNames[savedIndex].get(parsed);
							}
							parsed++;
						}
					}

					else
					{
						nameOfTheNode = myNode.levelZeroNodeNames[savedIndex].get(0);
					}

					System.out.println("Found a surrogate " + nameOfTheNode);

					//if ( ! myNode.name.equalsIgnoreCase("glados"))
					//{
						path += " -> " + nameOfTheNode;
					//}
					
					searchFileOnParticularNode(nameOfTheNode, fileName, fileHash, requestor, path);
					//addFileToAnotherNode(fileName, fileHash, din, nameOfTheNode);
				}
			}
		}
	}

	public void addFile(String fileName, String fileHash, BufferedReader din)
	{
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

			PrintWriter out = new PrintWriter(filePath + "/" + fileName);

			String myStringRead;
			while ((myStringRead = din.readLine()) != null)
			{

				out.println(myStringRead);
			}

			out.close();

			System.out.println("File created at " + filePath);
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	public void addFileToAnotherNode(String fileName, String fileHash, BufferedReader din, String nodeName) {

		try
		{
			String hostName = nodeName + ".cs.rit.edu";
			Socket insertClient = new Socket(hostName, port);
			BufferedReader myReader = new BufferedReader (
					new InputStreamReader (insertClient.getInputStream()));

			OutputStream os = insertClient.getOutputStream();

			String theOp = "addFile\n";
			byte[] theOpArr = new byte[(int) theOp.length()];
			theOpArr = theOp.getBytes();

			String fileWithNewLine = fileName + "\n";
			String fileHashWithNewLine = fileHash + "\n";

			byte[] fNameArr = new byte[(int) fileWithNewLine.length()];
			fNameArr = fileWithNewLine.getBytes();

			byte[] fHashArr = new byte[(int) fileHashWithNewLine.length()];
			fHashArr = fileHashWithNewLine.getBytes();

			os.write(theOpArr, 0, theOpArr.length);
			os.write(fNameArr, 0, fNameArr.length);
			os.write(fHashArr, 0, fHashArr.length);

			String myStringRead;

			while ((myStringRead = din.readLine()) != null)
			{
				myStringRead += "\n";

				byte[] toWrite = new byte[(int) myStringRead.length()];
				toWrite = myStringRead.getBytes();

				os.write(toWrite, 0, toWrite.length);
			}

			os.flush();
			os.close();
			insertClient.close();
		}
		catch ( Exception e)
		{
			e.printStackTrace();
		}

	}

	public void sendFileToAnotherNode(String fileName, String fileHash, BufferedReader din, String nodeName) {

		try
		{
			String hostName = nodeName + ".cs.rit.edu";
			Socket insertClient = new Socket(hostName, port);
			BufferedReader myReader = new BufferedReader (
					new InputStreamReader (insertClient.getInputStream()));

			OutputStream os = insertClient.getOutputStream();

			String theOp = "insert\n";
			byte[] theOpArr = new byte[(int) theOp.length()];
			theOpArr = theOp.getBytes();

			String fileWithNewLine = fileName + "\n";
			String fileHashWithNewLine = fileHash + "\n";

			byte[] fNameArr = new byte[(int) fileWithNewLine.length()];
			fNameArr = fileWithNewLine.getBytes();

			byte[] fHashArr = new byte[(int) fileHashWithNewLine.length()];
			fHashArr = fileHashWithNewLine.getBytes();

			os.write(theOpArr, 0, theOpArr.length);
			os.write(fNameArr, 0, fNameArr.length);
			os.write(fHashArr, 0, fHashArr.length);

			String myStringRead;

			while ((myStringRead = din.readLine()) != null)
			{
				myStringRead += "\n";

				byte[] toWrite = new byte[(int) myStringRead.length()];
				toWrite = myStringRead.getBytes();

				os.write(toWrite, 0, toWrite.length);
			}

			os.flush();
			os.close();
			insertClient.close();
		}
		catch ( Exception e)
		{
			e.printStackTrace();
		}

	}

	public void findDestination(int hash0, int hash1, String fileName, String fileHash, BufferedReader din)
	{
		int currentNodeID0 = Character.getNumericValue(myNode.ID.charAt(0));
		int currentNodeID1 = Character.getNumericValue(myNode.ID.charAt(1));

		//First index matched
		if ( currentNodeID0 == hash0 )
		{
			if ( currentNodeID1 == hash1 )
			{
				System.out.println("Found exact match with myself. Obj will be stored with me " + myNode.name);

				addFile(fileName, fileHash, din);
			}

			//Look for exact match with second index
			else if ( myNode.levelOne[hash1].size() != 0)
			{
				//We found an exact match
				System.out.println("Found exact match at " +  myNode.levelOne[hash1].get(0)
						+ " with name " + myNode.levelOneNodeNames[hash1].get(0));

				addFileToAnotherNode(fileName, fileHash, din, myNode.levelOneNodeNames[hash1].get(0));
			}

			//Exact match for second digit is not present, got to look for next available one in level one
			else
			{
				System.out.println("No exact match found, so going to check for a surrogate");
				boolean foundIndex = false;
				int savedIndex = -1;
				String nodeName = "";
				int currentDifference = hash1 - currentNodeID1;
				int parsingDifference = 100;

				for ( int temp = hash1; temp < maxIndices; temp++ )
				{
					if ( myNode.levelOne[temp].size() > 0)
					{
						parsingDifference = hash1 - temp;

						if ( parsingDifference >= 0 && (parsingDifference < currentDifference || currentDifference < 0 ) )
						{
							savedIndex = temp;
							foundIndex = true;
						}
					}
				}

				if ( !foundIndex )
				{
					for ( int temp = 0; temp < hash1; temp++ )
					{
						if ( myNode.levelOne[temp].size() > 0)
						{
							parsingDifference = hash1 - temp;

							if ( parsingDifference >= 0 && (parsingDifference < currentDifference || currentDifference < 0 ) )
							{
								savedIndex = temp;
								foundIndex = true;
							}
						}
					}
				}

				if ( !foundIndex )
				{
					System.out.println("No other is closer, going to store on myself " + myNode.name);
					addFile(fileName, fileHash, din);
				}

				//Found some surrogate
				else
				{
					String nameOfTheNode = "";

					if ( myNode.levelOne[savedIndex].size() > 1 )
					{
						int minDistance = 100;
						int sizeToParse = myNode.levelOne[savedIndex].size();
						int parsed = 0;
						int position;
						while ( parsed < sizeToParse )
						{
							int currentDistance;
							int theHashOfNode = Character.getNumericValue(myNode.levelOne[savedIndex].get(parsed).charAt(1));
							currentDistance = hash1 - theHashOfNode;

							if ( currentDistance >= 0 && currentDistance < minDistance )
							{
								position = parsed;
								minDistance = currentDistance;
								nameOfTheNode = myNode.levelOneNodeNames[savedIndex].get(parsed);
							}
							parsed++;
						}
					}

					else
					{
						nameOfTheNode = myNode.levelOneNodeNames[savedIndex].get(0);
					}

					System.out.println("Found surrogate " + nameOfTheNode);
					addFileToAnotherNode(fileName, fileHash, din, nameOfTheNode);
				}

			}
		}//End of first index matched

		//First itself did not match so look for first index match
		else
		{
			System.out.println("First index has not matched. So now I need to look for someone starting with first index");

			if ( myNode.levelZero[hash0].size() != 0 )
			{
				//found match with 0th index, so ping any one of these. We will ping first one
				System.out.println("This node matched index 0 digit. Transferring request to " + myNode.levelZero[hash0].get(0)
						+ " with name " + myNode.levelZeroNodeNames[hash0].get(0));

				sendFileToAnotherNode(fileName, fileHash, din, myNode.levelZeroNodeNames[hash0].get(0));

			}

			else
			{
				System.out.println("Nobody with first index match. So need to look for a surrogate.");

				boolean foundIndex = false;
				int savedIndex = -1;
				String nodeName = "";
				int currentDifference = hash0 - currentNodeID0;
				int parsingDifference = 100;

				for ( int temp = hash0; temp < maxIndices ; temp++ )
				{
					if ( myNode.levelZero[temp].size() > 0)
					{
						parsingDifference = hash0 - temp;

						if ( parsingDifference >= 0 && (parsingDifference < currentDifference || currentDifference < 0 ) )
						{
							savedIndex = temp;
							foundIndex = true;
						}
					}
				}

				if ( !foundIndex )
				{
					for ( int temp = 0; temp < hash0; temp++ )
					{
						if ( myNode.levelZero[temp].size() > 0)
						{
							parsingDifference = hash0 - temp;

							if ( parsingDifference >= 0 && (parsingDifference < currentDifference || currentDifference < 0 ) )
							{
								savedIndex = temp;
								foundIndex = true;
							}
						}
					}
				}

				if ( !foundIndex )
				{
					System.out.println("No other is closer, going to store on myself " + myNode.name);
					addFile(fileName, fileHash, din);
				}

				else
				{
					String nameOfTheNode = "";

					if ( myNode.levelZero[savedIndex].size() > 1 )
					{
						//System.out.println("There is more than 1 node at this index");
						int minDistance = 100;
						int sizeToParse = myNode.levelZero[savedIndex].size();
						int parsed = 0;
						int position;


						while ( parsed < sizeToParse )
						{
							int currentDistance;
							int nodeHash = Integer.parseInt(myNode.levelZero[savedIndex].get(parsed));
							int totalFileHash = (hash0 * 10) + hash1;
							//int theHashOfNode = Character.getNumericValue(myNode.levelZero[savedIndex].get(parsed).charAt(1));
							//currentDistance = hash0 - theHashOfNode;
							currentDistance = totalFileHash - nodeHash;
							System.out.println("For " + myNode.levelZeroNodeNames[savedIndex].get(parsed) + " the diff is " + currentDifference);
							if ( currentDistance >= 0 && currentDistance < minDistance )
							{
								position = parsed;
								minDistance = currentDistance;
								nameOfTheNode = myNode.levelZeroNodeNames[savedIndex].get(parsed);
							}
							parsed++;
						}
					}

					else
					{
						nameOfTheNode = myNode.levelZeroNodeNames[savedIndex].get(0);
					}

					System.out.println("Found a surrogate " + nameOfTheNode);
					addFileToAnotherNode(fileName, fileHash, din, nameOfTheNode);
				}
			}
		}
	}

	public void addToMyRoutingTable(String nameOfNode, String id) {

		int level0 = Character.getNumericValue(id.charAt(0));
		int level1 = Character.getNumericValue(id.charAt(1));

		//System.out.println("First index " + level0);
		//System.out.println("Second index " + level1);

		myNode.addToTable(0, level0, id, nameOfNode);

		//myNode.level0.add(level0, id);

		int myLevel0= Character.getNumericValue(myNode.ID.charAt(0));

		if ( myLevel0 == level0 )
		{
			myNode.addToTable(1, level1, id, nameOfNode);
			//myNode.level1.add(level1, id);
		}

		//if I am root, add new node to other nodes tables as well

		if ( myNode.name.equalsIgnoreCase("glados"))
		{
			try
			{
				for ( int temp = 0; temp < maxIndices; temp++ )
				{
					if ( myNode.levelZero[temp].size() > 0 )
					{
						int size = myNode.levelZero[temp].size();

						int count = 0;

						//There may be many nodes at same index
						while ( count < size )
						{
							String currentHost = myNode.getNeighborName (0,temp, count);
							//System.out.println("Adding to " + currentHost);

							//for current node, add every neighbor
							for ( int innerTemp = 0; innerTemp < maxIndices; innerTemp++ )
							{
								if ( myNode.levelZero[innerTemp].size() > 0 )
								{
									int innerSize = myNode.levelZero[innerTemp].size();

									int currentCount = 0;

									while ( currentCount < innerSize )
									{
										String neighbor = myNode.getNeighborName (0,innerTemp, currentCount);
										String neighborID = myNode.getNeighborID(0, innerTemp, currentCount);
										//System.out.println("Neighbor " + neighbor);

										if ( !currentHost.equalsIgnoreCase(neighbor) )
										{
											String hostNameToSend = currentHost + ".cs.rit.edu";

											Socket mySocket = new Socket ( hostNameToSend, port );
											OutputStream os = mySocket.getOutputStream();

											String operation = "join\n";
											String myName = neighbor + "\n";
											String idToSend = neighborID + "\n";
											//id += "\n";

											byte[] opArr = new byte[(int) operation.length()];
											opArr = operation.getBytes();

											byte[] nameArr = new byte[(int) myName.length()];
											nameArr = myName.getBytes();

											byte[] idArr = new byte[(int) idToSend.length()];
											idArr = idToSend.getBytes();

											os.write(opArr, 0, opArr.length);
											os.write(nameArr, 0, nameArr.length);
											os.write(idArr, 0, idArr.length);

											os.flush();
											//System.out.println("Waiting for response");
											Scanner sc = new Scanner(mySocket.getInputStream());
											while(!sc.hasNext());
											String response = sc.nextLine();
											//System.out.println(response);
											if(response.equals("FILES"))
												NodeOperations.receiveFiles(mySocket);
											os.close();
											mySocket.close();
										}

										currentCount++;
									}
								}
							}
							count++;
						}
					}
				}
			}
			catch ( Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	/**
	 * Files sent by root upon getting added
	 */
	public void sendFiles(String destNode, Socket s){
		ArrayList<String> fileNames = reRoute(""+getHash(destNode));
		PrintWriter pw = null ;
		OutputStream os = null;
		OutputStreamWriter osr = null;
		try {
			os = s.getOutputStream();
			osr = new OutputStreamWriter(os);
			pw = new PrintWriter(os, true);
			if(fileNames.size() == 0){
				osr.write("NONE\n");
				osr.flush();
				return;
			}
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		pw.println("FILES\n");
		pw.println(fileNames.size());
		File existingFiles[] = null;
		File homedir = new File(System.getProperty("user.home"));
		File path;
		try {
			path = new File(homedir.getAbsolutePath()+"/"+InetAddress.getLocalHost().getHostName()+"/");
			existingFiles = path.listFiles();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		try {			
			for(int i = 0; i < existingFiles.length; i++){
				if(fileNames.contains(existingFiles[i].getName())){
					System.out.println("Sending file"+existingFiles[i].getName());
					pw.println(existingFiles[i].getName());
					pw.println(existingFiles[i].length());
					FileInputStream fileInputStream = new FileInputStream(existingFiles[i]);
					byte[] fileBytes = new byte[(int) existingFiles[i].length()];
					fileInputStream.read(fileBytes);
					os.write(fileBytes);
					os.flush();
					existingFiles[i].delete();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * check whether file at this node have files for new node's hash
	 */
	public ArrayList<String> reRoute(String newNodeHash){
		//System.out.println("Checking rerouting");
		String path = System.getProperty("user.home");
		String myName;
		ArrayList<String> copyFiles = new ArrayList<String>();
		int ownHash = Integer.parseInt(myNode.ID);
		int newHash = Integer.parseInt(newNodeHash);
		try {
			myName = InetAddress.getLocalHost().getHostName();
			path += "/" + myName;
			File theDir = new File(path);
			String fileNames[] = theDir.list();
			if(fileNames!=null)
			for(int i =0; i < fileNames.length; i++){
				int fileHash = getHash(fileNames[i]);
				int dif1 = ownHash -fileHash, dif2 = newHash-fileHash;
				if(dif1 <0)
					dif1*=-1;
				if(dif2<0)
					dif2*=-1;
				if( fileHash/10 == newHash/10 && fileHash/10 != ownHash/10 || fileHash/10 == newHash/10 && dif2 < dif1){
					copyFiles.add(fileNames[i]);
					//System.out.println("Transferring file "+fileNames[i]);
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return copyFiles;
	}

	
	public static int getHash(String name){
		int hash = name.hashCode();
		hash = hash % 100;
		if(hash < 0)
			hash = hash * -1;
		return hash;
	}

	public boolean newerIsCloser(int ownHash, int fileHash, int newHash){
		boolean result = false; 
		int dif1 = ownHash -fileHash, dif2 = newHash-fileHash;
		if(dif1 <0)
			dif1*=-1;
		if(dif2<0)
			dif2*=-1;
		if( fileHash/10 == newHash/10 && fileHash/10 != ownHash/10 || fileHash/10 == newHash/10 && dif2 < dif1)
			result = true;
		return result;
		
	}
}
