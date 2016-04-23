import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class RicartAgrawalaLT implements Runnable{
	
	static volatile Integer lamportTime = 0;
	static volatile Integer countOfReplies = 0;
	static ArrayList<BufferedWriter> queue = new ArrayList<BufferedWriter>();
	String status = "RELEASED";
	static int myIndex;
	static int noOfPeers =3;
	Socket peer[] = new Socket[noOfPeers];
	int peerIDs[] = new int[noOfPeers];
	String connectedHosts[] = new String[noOfPeers];
	static ObjectOutputStream[] oos = new ObjectOutputStream[noOfPeers];
	static Scanner[] scanners = new Scanner[noOfPeers];
	static BufferedWriter[] bw = new BufferedWriter[noOfPeers];
	static ObjectInputStream[] ios = new ObjectInputStream[noOfPeers];
	static int ports[];
	static String hosts[];
	
	static {
		int p = 9991;
		ports = new int[noOfPeers+1];
		for (int i = 0; i < noOfPeers+1; i++) {
			ports[i] = p++;
		}
		hosts = new String[noOfPeers+1];
		hosts[0] = "buddy.cs.rit.edu";
		hosts[1] = "kansas.cs.rit.edu";
		hosts[2] = "medusa.cs.rit.edu";
		hosts[3] = "glados.cs.rit.edu";
	}
	
	public RicartAgrawalaLT(int index){
		myIndex = index;
	}
	
	public static void main(String[] args) {
		RicartAgrawalaLT ra = new RicartAgrawalaLT(Integer.parseInt(args[0]));
		ra.init();
	}
	
	public void init() {
		try {
			ServerSocket server = new ServerSocket(ports[myIndex]);
			Thread.sleep(5000);
			for(int i = myIndex+1; i<=noOfPeers; i++){
				System.out.println("connecting to "+hosts[i]);
				peer[i-1] = new Socket(hosts[i], ports[i]);
				peerIDs[i-1] = ports[i];
			//	connectedHosts[i-1] = hosts[i];
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
			//	System.out.println("connected to "+ hosts[]);
				peer[myIndex - index] = s;
				oos[myIndex - index] = new ObjectOutputStream(s.getOutputStream());
				bw[myIndex - index] = new BufferedWriter(new PrintWriter(oos[myIndex - index]));
				ios[myIndex - index] = iosTemp;
				scanners[myIndex - index] = scannerTemp;
				peerIDs[myIndex - index] = port;
			//	connectedHosts[myIndex - index] = hosts[];
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
		Thread getCS = new Thread(this, "getCS");
		readPeer1.start();
		readPeer2.start();
		readPeer3.start();
		getCS.start();
	}

	public void enter(){
		if(status.equals("RELEASED")){
			countOfReplies =0;
			status = "WANTED";
			System.out.println(status);
			System.out.println(lamportTime);
			for(int i =0; i<noOfPeers; i++){
				synchronized(lamportTime){
					lamportTime++;
				}
				writeToChannel(i,"CS "+lamportTime);
			}
			while(countOfReplies != noOfPeers);
			countOfReplies =0;
			resourceAccesses();
		}
	}
	
	public void resourceAccesses(){
		status = "HELD";
		countOfReplies =0;
		System.out.println(status);
		Random ran = new Random();
		try {
			int delay = Math.abs(ran.nextInt(10000) - ran.nextInt(2000));
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exit();
	}
	
	public void exit(){
		status = "RELEASED";
		System.out.println(status);
		for(int i =0; i<queue.size(); i++){
			try {
				synchronized(lamportTime){
					lamportTime++;
					}
				queue.get(i).write("FREE");
				queue.get(i).newLine();
				queue.get(i).flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void readChannel(int channelNo){
		while(!scanners[channelNo].hasNext());
		String response = scanners[channelNo].nextLine();
		if(status.equals("RELEASED")&&response.contains("CS")){
			String res[] = response.split(" ");
			int otherChannelTime = Integer.parseInt(res[1]);
			synchronized(lamportTime){
				lamportTime= Math.max(otherChannelTime, lamportTime)+1;
			}
			writeToChannel(channelNo,"FREE");
		}
		else if(status.equals("HELD")&&response.contains("CS")){
			queue.add(bw[channelNo]);
			String res[] = response.split(" ");
			int otherChannelTime = Integer.parseInt(res[1]);
			synchronized(lamportTime){
				lamportTime= Math.max(otherChannelTime, lamportTime)+1;
			}
		}
		else if(status.equals("WANTED")){
			if(response.equals("FREE")){
				synchronized(countOfReplies){
					countOfReplies++;
				}
			}
			else if(response.contains("CS")){
				String res[] = response.split(" ");
				int otherChannelTime = Integer.parseInt(res[1]);
				System.out.println("My Lamport "+lamportTime+" other Lamport Time "+otherChannelTime);
				if(otherChannelTime<lamportTime){
					writeToChannel(channelNo, "FREE");
					lamportTime++;
				}
				else{
					queue.add(bw[channelNo]);
					lamportTime = otherChannelTime+1;
				}
			}
		}
	}

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
		while(true){
		//	System.out.println(status);
			if(Thread.currentThread().getName().equals("readPeer1")){
				readChannel(0);
			}
			else if(Thread.currentThread().getName().equals("readPeer2")){
				readChannel(1);
			}
			else if(Thread.currentThread().getName().equals("readPeer3")){
				readChannel(2);
			}
			else if(Thread.currentThread().getName().equals("getCS")){
				enter();
			}
		}
	}	
}
