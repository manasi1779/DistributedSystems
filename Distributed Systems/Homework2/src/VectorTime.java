/**
 * @author Manasi
 * This program calculates vector time for events happening at process
 * and between different processes. 
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

public class VectorTime implements Runnable{
	
	static Object eventsObject = new Object();
	static String peerNames[] = new String[2];
	static Object readChannel[] = new Object[2];
	static long initialTime;
	static State snapshot;
	static int myIndex;
	Socket peer[] = new Socket[2];
	static ObjectOutputStream[] oos = new ObjectOutputStream[2];
	static Scanner[] s = new Scanner[2];
	static BufferedWriter[] bw = new BufferedWriter[2];
	static ObjectInputStream[] ios = new ObjectInputStream[2];
	static volatile Integer balance = 1000;
	static int ports[];
	static String hosts[];
	static Integer transactionAmount = 0;
	static volatile ArrayList<Event> events;

	public VectorTime(int index) {
		myIndex = index;
	}
	
	public static void main(String[] args) {
		int index = Integer.parseInt(args[0]);
		VectorTime vt = new VectorTime(index);
		vt.start();
	}
	
	static {
		int p = 9991;
		ports = new int[3];
		for (int i = 0; i < 3; i++) {
			ports[i] = p++;
		}
		for (int i = 0; i < 2; i++) {
			readChannel[i] = new Object();
		}
		
		hosts = new String[3];
		hosts[0] = "kansas.cs.rit.edu";
		hosts[1] = "glados.cs.rit.edu";
		hosts[2] = "gorgon.cs.rit.edu";
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
			events = new ArrayList<Event>();
			Thread.sleep(5000);
			peer[(myIndex + 2) % 2] = new Socket(hosts[(myIndex + 2) % 3], ports[(myIndex + 2) % 3]);
			peerNames[(myIndex + 2) % 2] = hosts[(myIndex + 2) % 3];
			Thread.sleep(3000);
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
			thread1.start();
			thread2.start();
			thread3.start();
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
		synchronized(eventsObject){
		try {
				withdraw(amount);
				bw[process].write(amount.toString());
				bw[process].newLine();
				bw[process].flush();
				MyVector temp;
				if(events.size()>0){
					temp = events.get(events.size()-1).vectorTime;
					//oos[process].writeObject(temp);
				}
				else{
					temp = new MyVector(3);
					//oos[process].writeObject(new MyVector(3));
				}
				for(int i =0; i<3; i++){
					bw[process].write(temp.time[i].toString());
					bw[process].newLine();
					bw[process].flush();
				}
				//oos[process].flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();}}
	}
	
	/**
	 * Reads money received from other process, deposits it to own balance
	 * @param process
	 * @return
	 */
	public int getMoney(int process) {
		int amount = 0;
		while (!s[process].hasNext())
			;
		amount = Integer.parseInt(s[process].nextLine());
//		System.out.println("Reveived money " + amount + " from " + peerNames[process]);
		deposit(amount);
		return amount;
	}
	
	/**
	 * Reads vector sent along with money by process
	 * @param process
	 * @return
	 */
	public MyVector getVector(int process){
		MyVector processvector = new MyVector(3);
		
			System.out.print("");
		//processvector = (MyVector)ios[process].readObject();
		for(int i =0; i < 3; i++){
			while(!s[process].hasNext());
			processvector.time[i] = Integer.parseInt(s[process].nextLine());
		}
		System.out.println("Received vector "+processvector);
		return processvector;
	}

	/**
	 * Assigns vector to intraprocess event
	 * @param event
	 */
	public void assignVector(Event event){
		synchronized(eventsObject){
			event.vectorTime = new MyVector(3);
			if(events.size() > 0){
				MyVector vector = events.get(events.size()-1).vectorTime;
				for(int i =0; i<3; i++){
					if(i == myIndex)
						event.vectorTime.time[i] = vector.time[i]+1;
					else
						event.vectorTime.time[i] = vector.time[i];
				}
			}
			events.add(event);
			System.out.println(event.event);
			System.out.println(event.vectorTime);
		}
	}	

	/**
	 * Assigns vector if money is received from other process with a vector time stamp 
	 * @param e
	 * @param processVector
	 */
	private void assignVector(Event e, MyVector processVector) {
		synchronized(eventsObject){
			e.vectorTime = new MyVector(3);
			if(events.size() > 0){
				MyVector vector = events.get(events.size()-1).vectorTime;
				for(int i =0; i<3; i++){
					if(i == myIndex)
						e.vectorTime.time[i] = Math.max(vector.time[i]+1, processVector.time[i]+1);
					else
						e.vectorTime.time[i] = Math.max(vector.time[i], processVector.time[i]);
				}
			}
			events.add(e);
			System.out.println(e.event);
			System.out.println(e.vectorTime);
		}
	}
	
	@Override
	public void run() {
		String name = Thread.currentThread().getName();
		switch (name) {
		case "readChannel1": {
			while (true) {
				readChannel(0);
			}
			//break;
		}
		case "readChannel2": {
			while (true) {
				readChannel(1);
			}
			//break;
		}
		case "writeChannel": {
			while(true) {
				transact();
			}
			/*synchronized(eventsObject){
				displayEvents();
			}*/
			//break;
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
					int process = ran.nextInt(2);
					sendMoney(process, amount);
					synchronized(transactionAmount){
						transactionAmount+=amount;}
					assignVector(new Event("Sent "+peerNames[process]+" "+amount));
					break;
				}
				default:{
					break;
				}
				}
			}
	}
	

	private void displayEvents() {
		for(int i = 0; i<events.size();i++){
			System.out.println(events.get(i).event);
			System.out.println("Vector "+events.get(i).vectorTime);
			/*for(int j=0; j <3; j++)
			System.out.println("For "+hosts[j]+" "+events.get(i).vectorTime.time[j]);*/
		}
	}

	/**
	 * Reads channel i for amount received and vector time stamp received.
	 * @param i
	 */
	private void readChannel(int i) {
	//	synchronized(readChannel){
			System.out.print("");
			while (!s[i].hasNext());
			int amount = getMoney(i);
			synchronized(transactionAmount){
				transactionAmount+=amount;}
			MyVector processVector = getVector(i);
			Event e = new Event("Received from "+peerNames[i]+" "+amount);
			assignVector(e, processVector);
	//	}
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
		output += "0: "+time[0]+" 1: "+time[1]+" 2: "+time[2];
		return output;
	}
}