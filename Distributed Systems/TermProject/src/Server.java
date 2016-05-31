import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Server implements Runnable{

	int ID;
	ServerSocket server;
	Object o;
	static String myID;
	static Scanner sc = new Scanner(System.in);
	static Integer neighborMap[][] = new Integer[NodeInterface.levels][NodeInterface.entries];
	Socket root = new Socket();
	Socket neighbor[] = new Socket[NodeInterface.levels*NodeInterface.entries];
	Neighbor[] neighbors= new Neighbor[NodeInterface.levels*NodeInterface.entries];
	PrintWriter printWriter[][] = new PrintWriter[NodeInterface.levels][NodeInterface.entries];
	Scanner scanner[][] = new Scanner[NodeInterface.levels][NodeInterface.entries];
	private int maxLevel = 3;
	
	static{
		int x = 0;
		for(int i=0; i <NodeInterface.levels; i++){
			for(int j =0; j< NodeInterface.entries; j++){
				neighborMap[i][j] = x++;
			}
		}
		String myID = null;
		try {
			myID = InetAddress.getLocalHost().getHostName();
			if(!myID.contains(".cs.rit.edu"))
				myID = myID+ ".cs.rit.edu";
			myID = ""+getHash(myID);
			myID = clean(myID);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Server s = new Server(Integer.parseInt(args[0]));
		s.init();
		Thread t = new Thread(s);
		t.start();
	}
	
	public void init(){
		try {
			server = new ServerSocket(9991+ID);
			if(ID == 0){
				Thread t = new Thread(this, "listen");
				t.start();
			}
			else{
				Thread t = new Thread(this, "connect");
				t.start();
			}
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Server(int ID){
		this.ID = ID;
	}
	
	public void initSocket(int level, int entry){
		try {
			printWriter[level][entry] = new PrintWriter(neighbor[neighborMap[level][entry]].getOutputStream(), true);
			scanner[level][entry] = new Scanner(new InputStreamReader(neighbor[neighborMap[level][entry]].getInputStream()));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void initSocket(int ID){
		try {
			System.out.println("level"+ID/10);
			System.out.println("entry"+ID%10);
			printWriter[ID/10][ID%10] = new PrintWriter(neighbors[ID].socket.getOutputStream());
			scanner[ID/10][ID%10] = new Scanner(new InputStreamReader(neighbors[ID].socket.getInputStream()));
			Thread t = new Thread(this, "readChannel"+ID);
			t.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static int getHash(String objectName){
		MessageDigest md = null;
	    try {
	        md = MessageDigest.getInstance("SHA-1");
	    }
	    catch(NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } 
	    String hash =  new String(md.digest(objectName.getBytes()));
	    int hashC= hash.hashCode();
		hashC = hashC % 100;
		if(hashC<0)
			hashC = hashC*-1;
		System.out.println(objectName+" hash "+hashC);
		return hashC;
	}
	
	public static String clean(String s){
		if(s.length() == 1){
			s = "0"+s;
		}
		return s;
	}
	
	public void nextHop(int bitNo, String GUID, Neighbor n){		
		int max = 0;		
		GUID = clean(GUID);
		System.out.println(myID);
		max = maxHop(bitNo, GUID, myID);
		System.out.println("max "+max+" len "+neighborMap[max].length);
		//if(max<GUID.length())
		for(int j = 0; j < neighborMap[max].length; j++){
			if(Integer.parseInt(""+GUID.charAt(max))==j)
				if(neighbors[neighborMap[max][j]]==null){ // max correct or not
					neighbors[neighborMap[max][j]] = n;
					initSocket(neighborMap[max][j]);
					sendStringMessage(neighborMap[max][j], "OK");
					System.out.println("Added at "+max+" "+j);
					break;
				}
				else{
					PrintWriter pw;
					try {
						pw = new PrintWriter(new OutputStreamWriter(n.socket.getOutputStream()), true);
						pw.println("WAIT");
						pw.flush();
						sendStringMessage(neighborMap[max][j], "ADDNODE");
						sendStringMessage(neighborMap[max][j], n.hostName);
						sendStringMessage(neighborMap[max][j],""+n.port);
						System.out.println("Forwarding request to "+max+" "+j);
						break;
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} 
		}
	}	
	
	public void listen(){
		try {
			Socket s = server.accept();
			String hostName = s.getInetAddress().getHostName();
			Scanner t = new Scanner(s.getInputStream());
			int remotePort = t.nextInt();
			Neighbor n = new Neighbor(s, hostName, remotePort);
			if(!hostName.contains(".cs.rit.edu"))
				hostName = hostName+".cs.rit.edu";
			int hash = getHash(hostName);
			nextHop(0, ""+hash, n);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * As IDs are only 2 digit, Only level 1 will have neighbors 
	 * @param host
	 * @param port
	 */
	public void connect(String host, int port){
		try {
			Socket s = new Socket(host, port);
			Scanner t = new Scanner(s.getInputStream());
			PrintWriter pw = new PrintWriter(s.getOutputStream());
			//
			pw.println(9991+ID);
			pw.flush();
			System.out.println("connecting");
			Neighbor n = new Neighbor(s, host, port);
			while(!t.hasNext());
			String response = t.nextLine();
			System.out.println(response);
			if(response.equals("WAIT")){
				s = server.accept();
				t = new Scanner(s.getInputStream());
				host = t.nextLine();
				int remoteport = t.nextInt();
				n = new Neighbor(s, host, remoteport);
			}
			System.out.println("Added "+s.getInetAddress().getHostName()+" as root");
			root = s;
			nextHop(0,""+getHash(host),n);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendStringMessage(int ID, String message){
		printWriter[ID/10][ID%10].println(message); 
		printWriter[ID/10][ID%10].flush();
	}
	
	public void readChannel(int ID){		
		while(!scanner[ID/10][ID%10].hasNext());
		String message = scanner[ID/10][ID%10].nextLine();
		switch(message){
		case "ADDNODE":{
			System.out.println("Adding node");
			while(!scanner[ID/10][ID%10].hasNext());
			String hostName = scanner[ID/10][ID%10].nextLine();
			if(!hostName.contains(".cs.rit.edu"))
				hostName+=".cs.rit.edu";;
			System.out.println(hostName);
			while(!scanner[ID/10][ID%10].hasNext());
			int port = scanner[ID/10][ID%10].nextInt();
			System.out.println(port);
			try {
				Socket s = new Socket(hostName, port);
				PrintWriter pw = new PrintWriter(s.getOutputStream());
				String host = InetAddress.getLocalHost().getHostName();
				if(!host.contains(".cs.rit.edu"))
					host+=".cs.rit.edu";
				pw.println(host);
				pw.flush();
				pw.println(9991+this.ID);
				pw.flush();
				int hash = getHash(hostName);
				Neighbor n = new Neighbor(s, hostName, port);
				nextHop(0,""+hash,n);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
	}
	
	public int maxHop(int bitNo, String GUID, String myID){
		int max = bitNo;
		for(int i = bitNo; i < GUID.length(); i++){
			if(myID.charAt(i) == GUID.charAt(i)){
				max++;				
			}
			else
				break;
		}
		return max;
	}
	
	private Object getFileObject(Scanner s) {
		return null;
	}

	/**
	 * All information will be sent in single message as comma separated values
	 */
	private String getStringMessage(Scanner s) {
		while(!s.hasNext());
		return s.nextLine();
	}

	@Override
	public void run() {
		String name = Thread.currentThread().getName();
		if(name.contains("readChannel")){
			while(true){
				int ID = Integer.parseInt(name.substring(11));
				System.out.println("Read channel started");
				readChannel(ID);
			}
		}
		else if(name.equals("listen")){
			while(true)
				listen();
		}
		else if(name.equals("connect")){
			System.out.println("DO you want to join tapestry?");
			String host = sc.nextLine();
			int port = Integer.parseInt(sc.nextLine());
			connect(host, port);
		}
	}
}

class Neighbor{
	Socket socket;
	String hostName;
	int port;
	public Neighbor(Socket s, String host, int port){
		this.socket = s;
		this.hostName = host;
		this.port = port;
	}
}