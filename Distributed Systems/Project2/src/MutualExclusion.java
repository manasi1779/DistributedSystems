/**
 * @author Manasi
 * Application makes intraprocess and interprocess money transactions.
 * Processes compete for Critical section mutually exclusively for certain period of times.
 * Vector time between processes is used to decide which process enters critical section. 
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class MutualExclusion implements Runnable{
	static Object eventsObject = new Object();
	static Object writeObject[] = new Object[3];
	static long initialTime;
	static int myIndex;
	String status = "RELEASED";
	static volatile Integer countOfReplies = 0;
	static volatile Integer balance = 1000;
	static Integer transactionAmount = 0;
	static volatile ArrayList<Event> events;
	static int noOfPeers =3;
	Socket peer[] = new Socket[noOfPeers];
	int peerIDs[] = new int[noOfPeers];
	static ObjectOutputStream[] oos = new ObjectOutputStream[noOfPeers];
	static Scanner[] scanners = new Scanner[noOfPeers];
	static BufferedWriter[] bw = new BufferedWriter[noOfPeers];
	static ObjectInputStream[] ios = new ObjectInputStream[noOfPeers];
	static int ports[];
	static String hosts[];
	static volatile ArrayList<Integer> queue = new ArrayList<Integer>();

	public MutualExclusion(int index) {
		myIndex = index;
	}
	
	public static void main(String[] args) {
		int index = Integer.parseInt(args[0]);
		MutualExclusion vt = new MutualExclusion(index);
		vt.init();
	}
	
	static {
		int p = 9991;
		ports = new int[noOfPeers+1];
		for (int i = 0; i < noOfPeers+1; i++) {
			ports[i] = p++;
		}
		for(int i =0; i<noOfPeers; i++){
			writeObject[i] = new Object();
		}
		hosts = new String[noOfPeers+1];
		hosts[0] = "buddy.cs.rit.edu";
		hosts[1] = "kansas.cs.rit.edu";
		hosts[2] = "medusa.cs.rit.edu";
		hosts[3] = "glados.cs.rit.edu";
	}
	
	/**
	 * Initializes the communication between 4 processes. Servers names and ports are already stored
	 * Starts threads for each operation.
	 */
	public void init() {
		try {
			events = new ArrayList<Event>();
			ServerSocket server = new ServerSocket(ports[myIndex]);
			Thread.sleep(5000);
			for(int i = myIndex+1; i<=noOfPeers; i++){
				System.out.println("connecting to "+hosts[i]);
				peer[i-1] = new Socket(hosts[i], ports[i]);
				peerIDs[i-1] = ports[i];
				oos[i-1] = new ObjectOutputStream(peer[i-1].getOutputStream());
				bw[i-1] = new BufferedWriter(new PrintWriter(oos[i-1]));
				bw[i-1].write(""+ports[myIndex]);
				bw[i-1].newLine();
				bw[i-1].flush();
				ios[i-1] = new ObjectInputStream(peer[i-1].getInputStream());
				scanners[i-1] = new Scanner(ios[i-1]);
				Thread.sleep(1000);
			}
			for(int i = myIndex-1; i>=0; i--){
				Socket s = server.accept();
				ObjectInputStream iosTemp = new ObjectInputStream(s.getInputStream());
				Scanner scannerTemp = new Scanner(iosTemp);
				while(!scannerTemp.hasNext());
				int port = Integer.parseInt(scannerTemp.nextLine());
				int index = ports[myIndex] - port;
				peer[myIndex - index] = s;
				oos[myIndex - index] = new ObjectOutputStream(s.getOutputStream());
				bw[myIndex - index] = new BufferedWriter(new PrintWriter(oos[myIndex - index]));
				ios[myIndex - index] = iosTemp;
				scanners[myIndex - index] = scannerTemp;
				peerIDs[myIndex - index] = port;
			}
			for(int i =0; i<noOfPeers; i++){
				System.out.println(peerIDs[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Thread readPeer1 = new Thread(this, "readPeer1");
		Thread readPeer2 = new Thread(this, "readPeer2");
		Thread readPeer3 = new Thread(this, "readPeer3");
		Thread writeChannel = new Thread(this, "writeChannel");
		Thread getCS = new Thread(this, "getCS");
		readPeer1.start();
		readPeer2.start();
		readPeer3.start();
		writeChannel.start();
		getCS.start();
	}

	/**
	 * Withdraws money from balance
	 * @param amount
	 */
	public void withdraw(int amount) {
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
		MyVector temp;
		synchronized(eventsObject){
			synchronized(writeObject[process]){
			writeToChannel(process,"SEND");
			withdraw(amount);
			writeToChannel(process,amount.toString());
			if(events.size()>0){
				temp = events.get(events.size()-1).vectorTime;
			}
			else{
				temp = new MyVector(4);
			}
			for(int i =0; i<4; i++){
					writeToChannel(process,temp.time[i].toString());
				}
			}
		}
	}
	
	/**
	 * Reads money received from other process, deposits it to own balance
	 * @param process
	 * @return
	 */
	public int getMoney(int process) {
		int amount = 0;
		while (!scanners[process].hasNext());
		amount = Integer.parseInt(scanners[process].nextLine());
		deposit(amount);
		return amount;
	}
	
	/**
	 * Reads vector sent along with money by process
	 * @param process
	 * @return
	 */
	public MyVector getVector(int process){
		MyVector processvector = new MyVector(4);
		System.out.print("");
		for(int i =0; i < 4; i++){
			while(!scanners[process].hasNext());
			processvector.time[i] = Integer.parseInt(scanners[process].nextLine());
		}
		return processvector;
	}

	/**
	 * Assigns vector to intraprocess event
	 * @param event
	 */
	public void assignVector(Event event){
		synchronized(eventsObject){
			event.vectorTime = new MyVector(4);
			if(events.size() > 0){
				MyVector vector = events.get(events.size()-1).vectorTime;
				for(int i =0; i<4; i++){
					if(i == myIndex)
						event.vectorTime.time[i] = vector.time[i]+1;
					else
						event.vectorTime.time[i] = vector.time[i];
				}
			}
			events.add(event);
	//		System.out.println(event.event);
	//		System.out.println(event.vectorTime);
		}
	}	

	/**
	 * Assigns vector if money is received from other process with a vector time stamp 
	 * @param e
	 * @param processVector
	 */
	private void assignVector(Event e, MyVector processVector) {
		synchronized(eventsObject){
			e.vectorTime = new MyVector(4);
			if(events.size() > 0){
				MyVector vector = events.get(events.size()-1).vectorTime;
				for(int i =0; i<4; i++){
					if(i == myIndex)
						e.vectorTime.time[i] = Math.max(vector.time[i]+1, processVector.time[i]+1);
					else
						e.vectorTime.time[i] = Math.max(vector.time[i], processVector.time[i]);
				}
			}
			events.add(e);
			System.out.println(e.event);
		//	System.out.println(e.vectorTime);
		}
	}
	
	/**
	 * Enters critical section
	 */
	public void enter(){
		if(status.equals("RELEASED")){
			countOfReplies =0;
			synchronized(eventsObject){
				status = "WANTED";
				System.out.println(status);
				assignVector(new Event("Sending CS message to Peers"));
				MyVector temp = events.get(events.size()-1).vectorTime;
				for(int i =0; i<noOfPeers; i++){					
					synchronized(writeObject[i]){
						writeToChannel(i,"CS");
						for(int j =0; j<4; j++){
							writeToChannel(i,temp.time[j].toString());
						}
					}
				}
			}
			while(countOfReplies != noOfPeers);
			countOfReplies = 0;
			resourceAccesses();
		}
	}
	
	/**
	 * In the critical section
	 */
	public void resourceAccesses(){
		status = "HELD";
		countOfReplies =0;
		System.out.println(status);
		Random ran = new Random();
		try {
			int delay = Math.abs(ran.nextInt(10000) - ran.nextInt(2000));
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		exit();
	}
	
	/**
	 * Exiting critical section by notifying waiting processes.
	 */
	public void exit(){
		status = "RELEASED";
		System.out.println(status);
		System.out.println("Queue size "+queue.size());
		for(int i =0; i<queue.size(); i++){
			try {
				synchronized(writeObject[queue.get(i)]){
					bw[queue.get(i)].write("FREE");
					bw[queue.get(i)].newLine();
					bw[queue.get(i)].flush();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		queue = new ArrayList<Integer>();
		assignVector(new Event("Left CS"));
	}
	
	/**
	 * Reads channel input and processes requests  
	 * @param channelNo
	 */
	public void readChannel(int channelNo){
		while(!scanners[channelNo].hasNext());
		String response = scanners[channelNo].nextLine();
	//	System.out.println(response+" from "+peerIDs[channelNo]);
		if(response.equals("SEND")){
			int amount = getMoney(channelNo);
			synchronized(transactionAmount){
				transactionAmount+=amount;}
			MyVector processVector = getVector(channelNo);
			Event e = new Event("Received from "+peerIDs[channelNo]+" "+amount);
			assignVector(e, processVector);
		}
		else if(status.equals("RELEASED")&&response.equals("CS")){
			synchronized(writeObject[channelNo]){
				writeToChannel(channelNo,"FREE");
			}
			MyVector processVector = getVector(channelNo);
			assignVector(new Event("CS request from "+peerIDs[channelNo]), processVector);
		}
		else if(status.equals("HELD")&&response.equals("CS")){
			queue.add(channelNo);
			MyVector processVector = getVector(channelNo);
			assignVector(new Event("CS request from "+peerIDs[channelNo]), processVector);
		}
		else if(status.equals("WANTED")){
			if(response.equals("FREE")){
				synchronized(countOfReplies){
					countOfReplies++;
				}
			}
			else if(response.equals("CS")){
				MyVector processVector = getVector(channelNo);
				MyVector myVector;
				int i = 0;
				if(events.size()>0){
					myVector = events.get(events.size()-1).vectorTime;
					for(; i < 3; i++){
						if(myVector.time[i] >= processVector.time[i])
							break;
					}
				}
				//My vector time is strictly less than processVector
				if(i == 3){
					queue.add(channelNo);
				}
				else{
					synchronized(writeObject[channelNo]){
						writeToChannel(channelNo, "FREE");
					}
					assignVector(new Event("CS request from "+peerIDs[channelNo]), processVector);
				}
			}
		}
	}

	/**
	 * Writes command to outgoing channel 
	 * @param channelNo
	 * @param command
	 */
	public void writeToChannel(int channelNo, String command){
		try {
			bw[channelNo].write(command);
			bw[channelNo].newLine();
			bw[channelNo].flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		String name = Thread.currentThread().getName();
		switch (name) {
		case "readPeer1": {
			while (true) {
				readChannel(0);
			}
			//break;
		}
		case "readPeer2": {
			while (true) {
				readChannel(1);
			}
			//break;
		}
		case "readPeer3": {
			while (true) {
				readChannel(2);
			}
			//break;
		}
		case "writeChannel": {
			while(true) {
				transact();
			}
		}
		case "getCS":{
			while(true){
				enter();
			}
		}
		default: {
			System.out.println("default");
			break;
		}
	}
}

	/**
	 * Every 5th second performs one of the three operations specified
	 */
	private void transact() {		
			long currentTime = System.currentTimeMillis();
			if ((currentTime - initialTime) % 5000 == 0) {
				Random ran = new Random();
				int option = ran.nextInt(3);
				switch(option){
				case 0:{
					int amount = ran.nextInt(100);
					withdraw(amount);
					synchronized(transactionAmount){
					transactionAmount+=amount;}
					assignVector(new Event("Withdraw "+amount));
					break;
				}
				case 1:{
					int amount = ran.nextInt(100);
					deposit(amount);
					synchronized(transactionAmount){
						transactionAmount+=amount;}
					assignVector(new Event("Deposit "+amount));
					break;
				}
				case 2:{
					int amount = ran.nextInt(100);
					int process = ran.nextInt(3);
					sendMoney(process, amount);
					synchronized(transactionAmount){
						transactionAmount+=amount;}
					assignVector(new Event("Sent "+peerIDs[process]+" "+amount));
					break;
				}
				default:{
					break;
				}
				}
			}
	}
}

class Event{
	String event;
	MyVector vectorTime;
	
	public Event(String event){
		this.event = event;
		
	}
}

class MyVector implements Serializable{
	Integer time[];
	
	public MyVector(int noOfProcesses){
		time = new Integer[noOfProcesses];
		for(int i = 0; i < noOfProcesses;i++){
			time[i] = 0;
		}
	}	
	
	public String toString(){
		String output = "";
		output += "0: "+time[0]+" 1: "+time[1]+" 2: "+time[2]+" 3:"+time[3];
		return output;
	}
}

