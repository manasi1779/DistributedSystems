import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.stream.Stream;

public class LeaderDevice extends DataGenerator{
	
	
	String base3StatusValue;
	//int noOfIoTs;
	int noOfChanges;
	String server;
	Socket leafNode;
	int port = 12345;
	//Fog Node
	Socket serversocket;
	ServerSocket contextRoot;

	public LeaderDevice(String context, int TCR, int PCR, int ID) {
		super(context, TCR, PCR, ID);
		/*this.noOfIoTs = iots;
		this.base3StatusValue = Stream.generate(() -> "1").limit(iots).reduce((a, b) -> a + b).get();*/
	}
	
	public void connect(){
		try {
			contextRoot = new ServerSocket(1486);
			serversocket = new Socket(server, port);
			BufferedReader din = new BufferedReader (
					new InputStreamReader(serversocket.getInputStream()));
			String command = din.readLine();
			if(command.equals("getUpdate")){
				updateEngine();
			}
			else if(command.equals("addIoT")){
				addIoT();
			}
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
	
	public void addIoT() {
		try {			
			BufferedReader din = new BufferedReader(
					new InputStreamReader (serversocket.getInputStream()));
			Socket socket = contextRoot.accept();
			
			PrintWriter bw = new PrintWriter(socket.getOutputStream());
			bw.println("changePredecessor");
			if(leafNode == null){
				bw.println(InetAddress.getLocalHost().getHostName());
				successorHostName = socket.getInetAddress().toString();
				leafNode = socket;
			}
			else{
				bw.println(leafNode.getInetAddress());			
				PrintWriter oldLeaf = new PrintWriter(leafNode.getOutputStream());
				oldLeaf.println("changeSuccessor");
				oldLeaf.println(socket.getLocalAddress());
				leafNode = socket;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void updateEngine(){
		try {
			PrintWriter bw = new PrintWriter(serversocket.getOutputStream());
			bw.println(noOfChanges);			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	// For later use
	public void processResponse(ResponsePacket response){
		int iotID = response.getID();
		if(response instanceof IncreaseValueResponse){
			base3StatusValue = base3StatusValue.substring(0,iotID)+"2"+base3StatusValue.substring(iotID+1);
		}
		else{
			base3StatusValue = base3StatusValue.substring(0,iotID)+"0"+base3StatusValue.substring(iotID+1);
		}
	}
	
}
