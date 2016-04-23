import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface ServerInterface extends Remote{
	
	String entryNode = "kansas.cs.rit.edu";
	int totalKeySpace = 1023; 

	class FileData implements Serializable{
		String fileName;
		byte[] fileByteArray;
		String requestor;
		ArrayList<String> route = new ArrayList<String>();
	}
	
	class ServerData implements Serializable{
		String hostname, predecessor, successor ;
		int keySpaceFrom, keySpaceTo;
		
		public ServerData(String hostName, String predecessor, String successor){
			this.hostname = hostName;
			this.predecessor = predecessor;
			this.successor = successor;
			keySpaceFrom = 0;
			keySpaceTo = 1023;			
		}
		
	}
	
	public String getHostName()throws RemoteException;
	
	public void insert(File file)throws RemoteException;
	
	public void search(String fileName)throws RemoteException;
	
	public void insertFile(FileData file)throws RemoteException;
	
	public void searchFile(FileData fileName)throws FileNotUploadedException, RemoteException;
	
	void setSuccessor(ServerInterface successor)throws RemoteException;

	void setPredecessor(ServerInterface predecessor)throws RemoteException;
	
	public ServerInterface getPredecessor()throws RemoteException;
	
	public ServerInterface getSuccessor()throws RemoteException;
	
	public void setKeySpaceFrom(int from)throws RemoteException;
	 
	public void setKeySpaceTo(int to)throws RemoteException;

	public void join()throws RemoteException;
	
	public int getKeySpaceFrom()throws RemoteException;

	public void leave()throws RemoteException;

	public File[] getFiles()throws RemoteException;

	public void removeFiles()throws RemoteException;

	public void removeNode(ServerData newServerData)throws RemoteException;

	public void addNode(ServerData newServerData)throws RemoteException;

	public void sendFileData(FileData file)throws RemoteException;

	public int getKeySpaceTo()throws RemoteException;

	byte[][] getFileBytes()throws RemoteException;

	public String[] getFileNames()throws RemoteException;

	void copyFiles(List<byte[]> copy, List<String> copyNames)throws RemoteException;
}
