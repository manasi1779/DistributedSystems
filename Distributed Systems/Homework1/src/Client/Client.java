package Client;

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
import java.util.Random;


public class Client {
	
	public static void main(String[] args) {
		
			ServerInterface serverIntf;
			try {				
				int leafLevel = 0;
				while(Math.pow(2,leafLevel)< ServerInterface.numberOfServers)
					leafLevel++;
				leafLevel--;
				
				Random rand = new Random();
				int j = rand.nextInt(leafLevel*2);
				Client client = new Client();
				int hash = client.getHash("Steps.txt", leafLevel, j);
				Integer hostNo = hash % ServerInterface.numberOfServers;
				String hostName = ServerInterface.IDTable.get(hostNo);
				serverIntf = (ServerInterface)Naming.lookup("rmi://"+hostName+"/"+hostName);
				ServerInterface.FileData file = new ServerInterface.FileData(new File("Steps.txt"),InetAddress.getLocalHost().getHostName());
				ServerInterface.FileData fileData = serverIntf.readFile(file, leafLevel, j);
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileData.file)));
				System.out.println("Search Trail:");
				for(String host: fileData.trails){
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



