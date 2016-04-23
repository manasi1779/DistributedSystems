
/**
 * The algorithm is defined through two rules, the marker receiving rule and the
marker sending rule. The marker sending rule obligates processes to send
a marker after they have recorded their state, but before they send any other messages.
The marker receiving rule obligates a process that has not recorded its state to do
so. In that case, this is the first marker that it has received. It notes which messages
subsequently arrive on the other incoming channels. When a process that has already
saved its state receives a marker (on another channel), it records the state of that channel
as the set of messages it has received on it since it saved its state.
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Random;
import java.util.Scanner;

public class Snapshot implements Runnable {
	static String peerNames[] = new String[2];
	static Object readChannel[] = new Object[2];
	static Object writeChannel[] = new Object[2];
	static long initialTime;
	static volatile State snapshot;
	static volatile Integer snapsReceived = 0;
	static volatile Boolean waitingForSnaps = false; 
	static int myIndex;
	Socket peer[] = new Socket[2];
	static ObjectOutputStream[] oos = new ObjectOutputStream[2];
	static Scanner[] s = new Scanner[2];
	static BufferedWriter[] bw = new BufferedWriter[2];
	static ObjectInputStream[] ios = new ObjectInputStream[2];
	static volatile Integer balance = 1000;
	static volatile String state = "transact";
	static volatile Integer markers = 0;
	static boolean markersReceived[];
	static int ports[];
	static String hosts[];
	
	/**
	 * Static initialization
	 */
	static {
		int p = 9991;
		ports = new int[3];
		markersReceived = new boolean[2];
		for (int i = 0; i < 3; i++) {
			ports[i] = p++;
		}
		for (int i = 0; i < 2; i++) {
			readChannel[i] = new Object();
			writeChannel[i] = new Object();
			markersReceived[i] = false;
		}
		hosts = new String[3];
		hosts[0] = "buddy.cs.rit.edu";
		hosts[1] = "doors.cs.rit.edu";
		hosts[2] = "medusa.cs.rit.edu";
	}

	/**
	 * Create snapshot
	 * @param index
	 */
	public Snapshot(int index) {
		myIndex = index;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int index = Integer.parseInt(args[0]);
		Snapshot snapshot = new Snapshot(index);
		snapshot.start();
	}

	/**
	 * Initializes the communication between 3 processes. Servers names and ports are already stored
	 * Starts threads for each operation.
	 */
	public void start() {
		try {

			ServerSocket server = new ServerSocket(ports[myIndex]);
			/*
			 * Connections 
			 * 0 - 1 - waiting, 2 - creating 
			 * 1 - 2 - waiting, 0 - creating 
			 * 2 - 0 - waiting, 1 - creating
			 */
			Thread.sleep(5000);
			peer[(myIndex + 2) % 2] = new Socket(hosts[(myIndex + 2) % 3], ports[(myIndex + 2) % 3]);
			peerNames[(myIndex + 2) % 2] = hosts[(myIndex + 2) % 3];
			Thread.sleep(5000);
			peer[(myIndex + 1) % 2] = server.accept();
			peerNames[(myIndex + 1) % 2] = hosts[(myIndex + 1) % 3]; 
			for (int i = 0; i < 2; i++) {
				oos[i] = new ObjectOutputStream(peer[i].getOutputStream());
				ios[i] = new ObjectInputStream(peer[i].getInputStream());
				s[i] = new Scanner(ios[i]);
				bw[i] = new BufferedWriter(new PrintWriter(oos[i]));
				System.out.println(peerNames[i]);
			}
			initialTime = System.currentTimeMillis();
			Thread thread1 = new Thread(this, "readChannel1");
			Thread thread2 = new Thread(this, "readChannel2");
			Thread thread3 = new Thread(this, "writeChannel");
			Thread thread4 = new Thread(this, "record");
			thread1.start();
			thread2.start();
			thread3.start();
			thread4.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Withdraws money from balance
	 * @param amount
	 */
	public void withDraw(int amount) {
		synchronized (balance) {
			balance -= amount;
		}
	}

	/**
	 * Adds money to balance
	 * @param amount
	 */
	public void deposit(int amount) {
		synchronized (balance) {
			balance += amount;
		}
	}

	/**
	 * sends amount to process and also appends current vector time stamp of process.
	 * @param process
	 * @param amount
	 */
	public void sendMoney(int process, Integer amount) {
		try {			
			synchronized (writeChannel[process]) {
				withDraw(amount);
				bw[process].write("SEND");
				bw[process].newLine();
				bw[process].flush();
				bw[process].write(amount.toString());
				bw[process].newLine();
				bw[process].flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Write snapshot to outgoing channels 
	 */
	public void writeSnapshot(){
		for(int i =0; i < 2; i++){
			synchronized (writeChannel[i]) {
				System.out.println("writing snapshot");
				try {
					bw[i].write("Snapshot");
					bw[i].newLine();
					bw[i].flush();
					oos[i].writeObject(snapshot);
					oos[i].flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Gets money and deposits to own balance
	 * @param process
	 * @return
	 */
	public int getMoney(int process) {
		int amount = 0;
		synchronized(readChannel[process]){
		while (!s[process].hasNext())
			;
		amount = Integer.parseInt(s[process].nextLine());
		System.out.println("Received money " + amount + " from " + peerNames[process]);
		deposit(amount);
		return amount;
		}
	}

	/**
	 * Reads marker and stores state from other process
	 * @param process
	 */
	public void readMarker(int process) {
		synchronized(readChannel[process]){
		while (!s[process].hasNext())
			;
		System.out.println("Got marker from " + peerNames[process]);
		snapshot.otherStates[process] = Integer.parseInt(s[process].nextLine());
		}
	}

	/**
	 * 
	 */
	public void transact() {
		state = state;
		while (!(state.equals("record")||state.equals("recording"))) {
			long currentTime = System.currentTimeMillis();
			if (myIndex == 0 && (currentTime - initialTime) % 2000 == 0) {
				System.out.println(waitingForSnaps+" "+snapsReceived);
				while(waitingForSnaps);
				//Sends marker before writing anything to channel
					recordState();
					state = "record";
			} else if ((currentTime - initialTime) % 1000 == 0) {
				Random random = new Random();
				int ran = random.nextInt(2);
				Integer sendMoney;
				synchronized (balance) {
					sendMoney = random.nextInt(balance / 3);
				}
				sendMoney(ran, sendMoney);
				System.out.println("Sent " + sendMoney + " to " + peerNames[ran]);
			}
		}
	}

	public void getSnapShot() {
		if (state.equals("record")) {
			//Send markers before writing anything else to channels
			synchronized(writeChannel[0]){
				synchronized(writeChannel[1]){
					sendMarker();
					recordChannel();
					System.out.println("Next Snapshot started");
				}
			}
		}
	}

	public void sendMarker() {
		for (int i = 0; i < 2; i++) {
			synchronized(writeChannel[i]){
			System.out.println("Sending marker to " + peerNames[i]);
			try {
					bw[i].write("Marker");
					bw[i].newLine();
					bw[i].flush();
					bw[i].write(snapshot.state.toString());
					bw[i].newLine();
					bw[i].flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			}
		}
	}

	public void recordState() {
		synchronized(balance){
			snapshot = new State(2);
			snapshot.state = balance;
		}
	}

	public void recordChannel() {
		state = "recording";
	}

	public void readChannel(int process) {
		while (!s[process].hasNext())
			;
		String command = s[process].nextLine();
		//System.out.println("command is "+command+" from channel "+ process);
		if (state.equals("recording") || state.equals("record")) {
			if (!markersReceived[process]) {
				 if (command.equals("Marker")) {					
					synchronized (markersReceived) {
						markersReceived[process] = true;
						markers++;}
					//	System.out.println("Received here "+markers);
						synchronized(waitingForSnaps){
						waitingForSnaps = true;
						}
						readMarker(process);
						if (markers == 2)
							stopRecording();						
										
				}else if(command.equals("Snapshot")){
					readSnapshot(process);
				}else if (command.equals("SEND")) {
					int received = getMoney(process);
					snapshot.channels[process].state += received;
					System.out.println("Channel " + peerNames[process] + " updated " + snapshot.channels[process].state);
				}
			} else {
				if(command.equals("Snapshot")){
					readSnapshot(process);
				}else if (command.equals("SEND")) {
					getMoney(process);
				}else if(command.equals("Marker")){
					String trash = s[process].nextLine();
					System.out.println("Invalid condition");
					// Recording this channel is already completed 
					// hence another marker should not be seen on this channel in recording state 
				}
			}
		}else if (state.equals("transact")) {
		//	System.out.println("else " + command);
			if (command.equals("Marker")) {
				// locking write channels as process must send markers before anything else
				synchronized(writeChannel[0]){
					synchronized(writeChannel[1]){
					synchronized (markersReceived) {
						markersReceived[process] = true;
						markers++;
					}					
					recordState();
					recordChannel();	
					synchronized(waitingForSnaps){
					waitingForSnaps = true;
					}
					readMarker(process);
					sendMarker();				
				}
			}
			}else if(command.equals("Snapshot")){
				readSnapshot(process);
			}else if (command.equals("SEND")) {
				getMoney(process);
		}}
		// }
	}

	private void readSnapshot(int process) {
			try {
				synchronized(readChannel[process]){
				System.out.println("Reading another snapshot");
				State otherSnap = (State) ios[process].readObject();
				System.out.println("Snapshot from "+peerNames[process]);
				System.out.println("Balance at peer "+otherSnap.state);
				for (int i = 0; i < 2; i++) {
					System.out.println(
							"Incoming channel " + i + " had " + otherSnap.channels[i].state);
				}
				synchronized(waitingForSnaps){
				synchronized(snapsReceived){					
						snapsReceived++;
						if(snapsReceived%2 == 0)
							waitingForSnaps = false;
					}
				}}
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private void stopRecording() {	
			System.out.println("own state: " + snapshot.state);
			synchronized (markersReceived) {
				for (int i = 0; i < 2; i++) {
					if(myIndex==0){
						System.out.println(
								"Incoming channel " + peerNames[i] + " had " + snapshot.channels[i].state);
						System.out.println("balance at " + peerNames[i] + " " + snapshot.otherStates[i]);
					}
					markersReceived[i] = false;
				}
				markers = 0;
			}		
		writeSnapshot();
		state = "transact";
	}

	@Override
	public void run() {
		String name = Thread.currentThread().getName();
		switch (name) {
		case "readChannel1": {
			while (true) {
				state = state;
				readChannel(0);
			}
		}
		case "readChannel2": {
			while (true) {
				state = state;
				readChannel(1);
			}
		}
		case "writeChannel": {
			while (true) {
				state = state;
				transact();
			}
		}
		case "record": {
			while (true) {
				state = state;
				getSnapShot();
			}
		}
		default: {
			System.out.println("default");
			break;
		}
		}
	}
}

class State implements Serializable{
	Integer state;
	Integer otherStates[];
	volatile Channel channels[];

	public State(int noOfchannels) {
		channels = new Channel[noOfchannels];
		otherStates = new Integer[noOfchannels];
		for (int i = 0; i < noOfchannels; i++) {
			channels[i] = new Channel();
		}
	}
}

class Channel implements Serializable{
	volatile int state;
}
