package Server;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import Client.ServerInterface;


public class Server extends UnicastRemoteObject  implements ServerInterface, Runnable{

	String serverName;
	HashMap<Integer, Integer> requestTable = new HashMap<Integer, Integer>();
	protected Server() throws RemoteException {
		super();
	}
	
	public Server(String hostName) throws RemoteException{
		super();
		this.serverName = hostName;
	}

	@Override
	public FileData readFile(FileData fileData, int i, int j) {
		File sendfile = null;
		String hostName = null;
		try {
			int hash = getHash(fileData.file.getName(), i, j);
			int hostNo = hash % numberOfServers;
			System.out.println(i+" "+j+" "+hash);
			hostName = IDTable.get(hostNo);
			int count = 1;
			if(requestTable.containsKey(hash)){
				count = requestTable.get(hash) + 1;
				requestTable.put(hash, count);
				System.out.println("came here");
				File homedir = new File(System.getProperty("user.home"));
				sendfile = new File(homedir.getAbsolutePath()+"/"+hostName+"/"+fileData.file.getName());	
				fileData.file = sendfile;
				fileData.trails.add(hostName);
				if(count%5==0)
					replicate(fileData.file.getName(), i, j);
			}
			else{					
				if(i==0)
					throw new FileNotFoundException("File not uploaded to server");
				hash = getHash(fileData.file.getName(), i-1, j/2);
				hostNo = hash % numberOfServers;
				hostName = IDTable.get(hostNo);
				ServerInterface serverIntf =  (ServerInterface)Naming.lookup("rmi://"+hostName+"/"+hostName);
				fileData = serverIntf.readFile(fileData, i-1, j/2);
				fileData.trails.add(hostName);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return fileData;
	}
	
	public void insertFile(File file, int i, int j){
		try {
			int hash = getHash(file.getName(), i, j);
			Integer hostNo = hash % numberOfServers;
			String hostName = IDTable.get(hostNo);
			if(hostName.equals(serverName)){
				
			//	Path dest = Paths.get("/home/stu2/s0/msb4977/"+serverName+"/"+file.getName());
				//Path dest = Paths.get("/"+serverName+"/"+file.getName());
				File homedir = new File(System.getProperty("user.home"));
				File path = new File(homedir.getAbsolutePath()+"/"+serverName+"/");
			//	File path = new File("./"+serverName+"/");
			//	File x = new File("./"+serverName+"/"+file.getName());
				File x = new File(homedir.getAbsolutePath()+"/"+serverName+"/"+file.getName());
				path.mkdirs();
				x.createNewFile();
				FileOutputStream out = new FileOutputStream(x);
				FileInputStream in = new FileInputStream(file);
				byte[] buffer = new byte[1024];
		    	System.out.println("came here");
	    	    int length;
	    	    //copy the file content in bytes 
	    	    while ((length = in.read(buffer)) > 0){
	    	    	out.write(buffer, 0, length);
	    	    }
	    	    in.close();
	    	    out.close();
				requestTable.put(hash, 0);
			}
			else{
				ServerInterface serverIntf =  (ServerInterface)Naming.lookup("rmi://"+hostName+"/"+hostName);
				serverIntf.insertFile(file, i, j);
			}
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		Server server;
		try {
			server = new Server(args[0]);
			server.serverName = args[0];
			System.out.println(server.serverName);
			Naming.rebind(server.serverName, server);
			server.insertFile(new File("Steps.txt"),0, 0);
			
			Thread runningThread = new Thread(new Server());
			runningThread.start();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void replicate(String file, int i, int j){
		try {
			int hash = getHash(file, i+1, 2*j);
			Integer hostNo = hash % numberOfServers;
			String hostName = IDTable.get(hostNo);
			ServerInterface serverIntf =  (ServerInterface)Naming.lookup("rmi://"+hostName+"/"+hostName);
			File homedir = new File(System.getProperty("user.home"));
			serverIntf.insertFile(new File(file), i+1,2*j );
			
			hash = getHash(file, i+1, 2*j+1);
			hostNo = hash % numberOfServers;
			hostName = IDTable.get(hostNo);
			serverIntf =  (ServerInterface)Naming.lookup("rmi://"+hostName+"/"+hostName);
			serverIntf.insertFile(new File(file),i+1, 2*j+1);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public int getHash(String fileName, int i, int j) {
		FileEntry entry = new FileEntry(fileName, i, j);
		return entry.hashCode();
	}
	
	@Override
	public void run() {
		while(true);
	}	
}

class FileEntry{
	String fileName;
	int i, j;
	
	public FileEntry(String fileName, int i, int j){
		this.fileName = fileName;
		this.i = i;
		this.j = j;
	}
	
	@Override
	public int hashCode(){
		return (fileName+i+j).hashCode();
	}
}
