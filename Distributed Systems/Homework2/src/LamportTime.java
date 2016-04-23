import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class LamportTime {

	boolean hasMessage = false;
	int state =0;
	Socket peer;
	ObjectOutputStream oos;
	Message marker;
	ObjectInputStream ios;
	ArrayList<Event> events = new ArrayList<Event>();
	Message m;
	
	public static void main(String[] args) {

	}

	public void start(){
		try {
			ServerSocket server = new ServerSocket(9991);
			peer = server.accept();
			oos = new ObjectOutputStream(peer.getOutputStream());
			ios = new ObjectInputStream(peer.getInputStream());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void initMessaging(){
		marker = new Message("Marker");
		marker.state = 0;
		sendMessage();
	}
	
	public void sendMessage(){
		try {			
			oos.writeObject(marker);
			hasMessage = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getMessage(){
		try {
			m = (Message) ios.readObject();
			hasMessage = true;
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void rotateMessage(){
		while(m.state < 101 && hasMessage){
			m.state++;
			Event e1 = new Event("Event"+m.state);
			events.add(e1);
			sendMessage();
			getMessage();
			Event e2 = new Event("Event"+m.state);
			events.add(e2);
		}
		startSnapShot();
	}
	
	public void startSnapShot(){
		recordState();
		sendMessage();
		recordChannel();
		getMessage();
	}
	
	public void recordState(){
		
	}
	
	public void recordChannel(){
		
	}
}



