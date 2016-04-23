/**
@Author: Manasi Sunil Bharde
This is client connecting to peer-peer Distributed System implementing DHT
**/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;



public class Client extends UnicastRemoteObject implements ClientInterface{
	
	protected Client() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	ServerInterface.FileData fileData;
	
	public static void main(String[] args) {
		
			ServerInterface serverIntf;
			try {				
				int leafLevel = 0;
				while(Math.pow(2,leafLevel)< 7)
					leafLevel++;
				leafLevel--;
				
				Random rand = new Random();
				int j = rand.nextInt(leafLevel*2);
				Client client = new Client();
				Naming.rebind(InetAddress.getLocalHost().getHostName()+".cs.rit.edu", client);
				int hash = client.getHash("Steps.txt", leafLevel, j);
				Integer hostNo = hash % ServerInterface.numberOfServers;
				String hostName = ServerInterface.IDTable.get(hostNo);
				serverIntf = (ServerInterface)Naming.lookup("rmi://"+hostName+"/"+hostName);
				ServerInterface.FileData file = new ServerInterface.FileData(new File("Steps.txt"),InetAddress.getLocalHost().getHostName()+".cs.rit.edu");
				file.trails.add(hostName);
				serverIntf.readFile(file, leafLevel, j);
				while(client.fileData==null);
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(client.fileData.file)));
				System.out.println("Search Trail:");
				for(String host: client.fileData.trails){
					System.out.println(host);
				}		
				String line;
				while((line = br.readLine()) != null){
					System.out.println(line);
				}
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public int getHash(String fileName, int i, int j) {
		FileEntry entry = new FileEntry(fileName, i, j);
		return (fileName+i+j).hashCode();
		/*MessageDigest md = null;
	    try {
	        md = MessageDigest.getInstance("SHA-1");
	    }
	    catch(NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }	    
	    return md.digest((fileName+i+j).getBytes()).hashCode();*/
	}

	@Override
	public void sendFile(ServerInterface.FileData fileData) throws RemoteException {
		this.fileData = fileData; 
	}
}


