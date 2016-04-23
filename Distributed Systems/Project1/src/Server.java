/**
 * @author Manasi Sunil Bharde
 * 
 * @email msb4977@rit.edu
 * This Server acts as a Node. If Server is not part of Chord, 
 * Insert and search can be done using Bootstrap Server that is set in ServerInterface. 
 * Currently bootstrap server is set to kansas.cs.rit.edu
 * New node joining chord can choose any node already in chord as Boot Strap Server 
 * While  
 * 
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.nio.file.Files;

public class Server extends UnicastRemoteObject   implements ServerInterface, Runnable{
	
	ServerData serverData;
	FileData fileData;
	ServerInterface predecessor, successor;
	boolean inChord = false;
	static Scanner s = new Scanner(System.in);
	
	public Server() throws RemoteException {
		try {
			String localhost = InetAddress.getLocalHost().getHostName()+".cs.rit.edu";
			serverData = new ServerData(localhost,localhost,localhost);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void init(){
		connectPredecessor();
		connectSuccessor();
		
		Thread runningThread;
		try {
			runningThread = new Thread(new Server());
			runningThread.start();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public boolean start(){
		boolean success = true;
		try {
			Naming.rebind(InetAddress.getLocalHost().getHostName()+".cs.rit.edu",this );
		} catch (RemoteException | MalformedURLException | UnknownHostException e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}
 
	public boolean stop(){
		boolean success = true;
		try {
			Naming.unbind(InetAddress.getLocalHost().getHostName()+".cs.rit.edu");
		} catch (RemoteException | MalformedURLException | UnknownHostException | NotBoundException e) {
			e.printStackTrace();	
			success = false;
		}
		return success;
	}
	
	public static void main(String[] args) {
		Server server = null;
		try {
			server = new Server();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		server.start();
		server.init();
		boolean exit = false;
		while(!exit){
		server.menu();
		System.out.println("Enter Command:");
		int option = s.nextInt();
		s.nextLine();
			switch(option){
				case 1:{
					System.out.println("Enter File Name:");
					String fileName = s.nextLine();
					server.insert(new File(fileName.toString()));
					break;
				}
				case 2:{
					System.out.println("Enter File Name:");
					String fileName = s.nextLine();
					server.search(fileName);
					break;
				}
				case 3:{
					server.view();
					break;
				}
				case 4:	{		
					server.join();
					break;
				}
				case 5:{
					server.leave();
					break;
				}
				case 6:{
					exit = true;
					break;
				}
				default:{
					System.out.println("Wrong choice");
				}
			}
		}			
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
	
	public void connectPredecessor(){
		try {
			predecessor = (ServerInterface) Naming.lookup("rmi://"+serverData.predecessor+"/"+serverData.predecessor);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void connectSuccessor(){
		try {
			successor = (ServerInterface) Naming.lookup("rmi://"+serverData.successor+"/"+serverData.successor);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void setPredecessor(ServerInterface predecessor1){
		this.predecessor = predecessor1;
		try {
			serverData.predecessor = predecessor1.getHostName();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void setSuccessor(ServerInterface successor1){
		this.successor = successor1;
		try {
			serverData.successor = successor1.getHostName();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Insert a file with keyword. After a successful insertion, display which peer stores the file and the route at the IP layer from peer to the destination peer. If the insertion fails, display "Failure".
	 * @param file
	 * @return
	 */
	@Override
	public void insert(File file){
		ServerInterface.FileData fd = new ServerInterface.FileData();		
		try {
			fd.requestor = serverData.hostname; 
			fd.fileName = file.getName();
			FileInputStream fileInputStream = new FileInputStream(file);
			fd.fileByteArray = new byte[(int) file.length()];
			fileInputStream.read(fd.fileByteArray);
			if(!inChord){
				ServerInterface root = (ServerInterface)Naming.lookup("rmi://"+entryNode+"/"+entryNode);
				root.insertFile(fd);
			}
			else
				insertFile(fd);
			while(fileData==null);
			System.out.println("File is stored at: "+fileData.route.get(fileData.route.size()-1));
			System.out.println("ROUTE");
			for(String step:fileData.route){
				System.out.println(step);
			}
		} catch (NotBoundException | IOException e) {
			System.out.println("Failure");
		}finally{
			fileData = null;
		}
	}
	
	@Override
	public void insertFile(FileData file) throws RemoteException{
		int hash = getHash(file.fileName);
		System.out.println(hash+" "+serverData.keySpaceFrom+" "+serverData.keySpaceTo);
		if(hash > serverData.keySpaceFrom && hash <= serverData.keySpaceTo){
			file.route.add(serverData.hostname);			
			System.out.println(serverData.hostname);
			//copy file to local
			try {
				File homedir = new File(System.getProperty("user.home"));
				File path = new File(homedir.getAbsolutePath()+"/"+serverData.hostname+"/");
				File x = new File(homedir.getAbsolutePath()+"/"+serverData.hostname+"/"+file.fileName);
				path.mkdirs();
				x.createNewFile();
				FileOutputStream out = new FileOutputStream(x);
				out.write(file.fileByteArray);
	    	    out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			ServerInterface client;
			try {
				client = (ServerInterface) Naming.lookup("rmi://"+file.requestor+"/"+file.requestor);
				client.sendFileData(file);
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		}
		else {
			if(hash < serverData.keySpaceFrom){
				System.out.println("smaller hash");
				if(file.route.isEmpty() || file.route.get(file.route.size()-1).equals(successor.getHostName()))
					{
						file.route.add(serverData.hostname);
						predecessor.insertFile(file);
					}
			}
			else if(hash > serverData.keySpaceTo){
				System.out.println("greater hash");
					if(file.route.isEmpty() || file.route.get(file.route.size()-1).equals(predecessor.getHostName()))
					{
						file.route.add(serverData.hostname);
						successor.insertFile(file);
					}
				}
		}
	}

	/**
	 * Search for a file with keyword. After a successful search, display which peer stores the file and the route at the IP layer from peer to the destination peer. If the search fails, display "Failure".
	 * @param fileName
	 * @return
	 */
	@Override
	public void search(String fileName){
		ServerInterface.FileData fd = new ServerInterface.FileData();
		try {			
			fd.fileName = fileName;
			fd.requestor = serverData.hostname;
			if(!inChord){
				ServerInterface root = (ServerInterface)Naming.lookup("rmi://"+entryNode+"/"+entryNode);
				root.searchFile(fd);
			}
			else
				searchFile(fd);
			while(fileData==null);
			if(fileData.fileByteArray != null){
				System.out.println("File is stored at: "+fileData.route.get(fileData.route.size()-1));
				System.out.println("ROUTE");
				for(String step:fileData.route){
					System.out.println(step);
				}
			}
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotUploadedException e) {
			System.out.println("Failure");
		}finally{
			fileData = null;
		}		
	}

	
	@Override
	public void searchFile(FileData file) throws FileNotUploadedException, RemoteException{
		int hash = getHash(file.fileName);
		if(hash > serverData.keySpaceFrom && hash <= serverData.keySpaceTo){
			//retrieve file from local folder and send to requester
			try {
				File homedir = new File(System.getProperty("user.home"));
				File x = new File(homedir.getAbsolutePath()+"/"+serverData.hostname+"/"+file.fileName);
				FileInputStream fileInputStream = new FileInputStream(x);
				file.fileByteArray = new byte[(int) x.length()];
				fileInputStream.read(file.fileByteArray);
				file.route.add(serverData.hostname);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				System.out.println("Failure");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			ServerInterface client;
			try {
				client = (ServerInterface) Naming.lookup("rmi://"+file.requestor+"/"+file.requestor);
				client.sendFileData(file);
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		}
		else if(hash < serverData.keySpaceFrom){
			if(file.route.isEmpty()|| file.route.get(file.route.size()-1).equals(successor.getHostName()))
				{
					file.route.add(serverData.hostname);
					predecessor.searchFile(file);
				}
			else
				throw new FileNotUploadedException("File Not in System");}
		else if(hash > serverData.keySpaceTo){
			if(file.route.isEmpty() || file.route.get(file.route.size()-1).equals(predecessor.getHostName()))
				{
					file.route.add(serverData.hostname);
					successor.searchFile(file);
				}
			else
				throw new FileNotUploadedException("File Not in System");}
	}
	
	public void addNode(ServerData newServerData) throws RemoteException {
		/* Get hash of node
		 * Add predecessor and successor to it
		 * Update own predecessor
		 */			
		int hash = getHash(newServerData.hostname);
		
		if(hash > serverData.keySpaceFrom && hash < serverData.keySpaceTo ){
			ServerInterface newNode;
			try {
				newNode = (ServerInterface) Naming.lookup("rmi://"+newServerData.hostname+"/"+newServerData.hostname);
				newNode.setKeySpaceFrom(serverData.keySpaceFrom);
				newNode.setKeySpaceTo(hash);
				ServerInterface newNodeSuccessor = predecessor.getSuccessor();
				predecessor.setSuccessor(newNode);
				newNode.setSuccessor(newNodeSuccessor);
				newNode.setPredecessor(predecessor);
				setPredecessor(newNode);
				setKeySpaceFrom(hash);
				//cut paste files of new hash range here
				File[] files = getFiles();
				if(files != null){
					byte[][] existingFiles = getFileBytes();
					String existingFileNames[] = new String[existingFiles.length];
					existingFileNames = getFileNames();
					List<byte[]> moveFiles = new ArrayList<byte[]>();
					List<String> moveFileNames = new ArrayList<String>();
					int i =0;
					for(byte[] checkFile:existingFiles){
						int checkHash = getHash(existingFileNames[i]);
						if(checkHash <= hash){
							moveFiles.add(checkFile);
							moveFileNames.add(existingFileNames[i]);
							Files.delete(files[i].toPath());
						}
						i++;
					}
					newNode.copyFiles(moveFiles,moveFileNames);
				}				
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		else if(hash < serverData.keySpaceFrom){
			predecessor.addNode(newServerData);
		}
		else{
			successor.addNode(newServerData);
		}
	}


	public void removeNode(ServerData newServerData) throws RemoteException {
		/* Get hash of node
		 * Add predecessor and successor to it
		 * Update own predecessor
		 */	
		int hash = getHash(newServerData.hostname);
		System.out.println(hash);
		if(hash == predecessor.getKeySpaceTo()){
			ServerInterface leavingNode;
			try {
				leavingNode = (ServerInterface) Naming.lookup("rmi://"+newServerData.hostname+"/"+newServerData.hostname);
				setPredecessor(leavingNode.getPredecessor());
				predecessor.setSuccessor(this);				
				setKeySpaceFrom(leavingNode.getKeySpaceFrom());
				leavingNode.setKeySpaceTo(1023);
				leavingNode.setKeySpaceFrom(0);
				leavingNode.setPredecessor(leavingNode);
				leavingNode.setSuccessor(leavingNode);
				//cut pastes files from predecessor to this node
				String fileList[] = leavingNode.getFileNames();
				if(fileList != null){
					byte[][] existingFiles = leavingNode.getFileBytes();
					List<byte[]> copy = new ArrayList<byte[]>();
					List<String> copyNames = new ArrayList<String>();
					leavingNode.removeFiles();
					int i =0;
					for(byte[] copyFile: existingFiles){
						copy.add(copyFile);
						copyNames.add(fileList[i++]);
					}
					copyFiles(copy, copyNames);
				}
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}			
		}
		else if(hash < serverData.keySpaceFrom){
			predecessor.removeNode(newServerData);
		}
		else{
			successor.removeNode(newServerData);
		}
	}

	
	private int getHash(String newServerData) {
	    int hostNo = newServerData.hashCode()%totalKeySpace;
	    if(hostNo < 0)
	    	hostNo*=-1;
	    return hostNo;
	}

	@Override
	public void setKeySpaceFrom(int from) {
		serverData.keySpaceFrom = from;
	}

	@Override
	public void setKeySpaceTo(int to) {
		serverData.keySpaceTo = to;
	}

	@Override
	public ServerInterface getPredecessor() {
		return predecessor;
	}

	@Override
	public ServerInterface getSuccessor() {
		return successor;
	}

	@Override
	public int getKeySpaceFrom() {
		return serverData.keySpaceFrom;
	}

	@Override
	public String getHostName() throws RemoteException {
		return serverData.hostname;
	}

	@Override
	public int getKeySpaceTo() {
		return serverData.keySpaceTo;
	}
	
	@Override
	public void copyFiles(List<byte[]> copy, List<String> copyNames) {
		File homedir = new File(System.getProperty("user.home"));
		File destPath = new File(homedir.getAbsolutePath()+"/"+serverData.hostname+"/");
		destPath.mkdirs();
		for(int i=0; i<copy.size(); i++){			
			try {
				File x = new File(homedir.getAbsolutePath()+"/"+serverData.hostname+"/"+copyNames.get(i));
				x.createNewFile();
				FileOutputStream out = new FileOutputStream(x);
				out.write(copy.get(i));
	    	    out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	public String[] getFileNames(){
		File homedir = new File(System.getProperty("user.home"));
		File path = new File(homedir.getAbsolutePath()+"/"+serverData.hostname+"/");
		return path.list();
	}
	
	
	@Override
	public byte[][] getFileBytes() {
		File homedir = new File(System.getProperty("user.home"));
		File path = new File(homedir.getAbsolutePath()+"/"+serverData.hostname+"/");
		File existingFiles[] = path.listFiles();
		byte[][] fileBytes= new byte[existingFiles.length][];
		int i =0 ;
		for(File file: existingFiles ){
			FileInputStream fileInputStream;
			try {
				fileInputStream = new FileInputStream(file);
				fileBytes[i] = new byte[(int) file.length()];
				fileInputStream.read(fileBytes[i++]);	
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					
		}
		return fileBytes;
	}

	@Override
	public File[] getFiles(){
		File homedir = new File(System.getProperty("user.home"));
		File path = new File(homedir.getAbsolutePath()+"/"+serverData.hostname+"/");
		return path.listFiles();
	}
	
	@Override
	public void removeFiles() {
		File toDelete[] = getFiles();
		for(File delete:toDelete)
			delete.delete();
	}

	@Override
	public void join() {		
		if(!inChord){
			inChord = true;			
			Registry registry;
			String node = null;
				try {					
					System.out.println("Select node to get in Chord");
					node = s.nextLine();				
					ServerInterface entry = (ServerInterface) Naming.lookup("rmi://"+node+"/"+node);
					entry.addNode(serverData);
				} catch (MalformedURLException | RemoteException | NotBoundException e) {
					e.printStackTrace();
					System.out.println("Failure");
					inChord = false;
				}
		}
		if(inChord){
			view();
		}		
	}

	private void view() {
		System.out.println("Own Name: "+serverData.hostname);
		try {
			System.out.println("IP Address: "+InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Predecessor: "+ serverData.predecessor);
		System.out.println("Successor: "+ serverData.successor);
		System.out.println("from: "+serverData.keySpaceFrom);
		System.out.println("to: "+serverData.keySpaceTo);
		File[] storedFiles = getFiles();
		if(storedFiles!=null){
			System.out.println("File List:");
			for(File stored:storedFiles)
				System.out.println(stored.getName());
		}
	}

	@Override
	public void leave() {
		if(inChord){
			inChord = false;
			try {
				ServerInterface entry = (ServerInterface) Naming.lookup("rmi://"+successor.getHostName()+"/"+successor.getHostName());
				entry.removeNode(serverData);
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				System.out.println("Failure");
				inChord = true;
			}
		}
		if(!inChord){
			view();
			stop();
		}		
	}

	@Override
	public void sendFileData(ServerInterface.FileData file) {
		this.fileData = file;
	}	
	
	@Override
	public void run() {
		while(true);
	}
}