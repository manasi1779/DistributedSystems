package Node;
import java.io.File;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface ServerInterface extends Remote{
	
	public HashMap<Integer,String> IDTable = new HashMap<Integer,String>();
	int numberOfServers = 11;
	Init init = new Init();
	
//	public void addID(String ID);
	
	public FileData readFile(FileData fileData, int i, int j) throws RemoteException;	
	
	public void insertFile(File fileName, int i, int j) throws RemoteException; 
	
	class Init{
		static{
			IDTable.put(0,"kansas.cs.rit.edu");
			IDTable.put(1,"buddy.cs.rit.edu");
			IDTable.put(2,"glados.cs.rit.edu");
			IDTable.put(3,"doors.cs.rit.edu");
			IDTable.put(4,"idaho.cs.rit.edu");
			IDTable.put(5,"medusa.cs.rit.edu");
			IDTable.put(6,"gorgon.cs.rit.edu");
			IDTable.put(7,"kinks.cs.rit.edu");
			IDTable.put(8,"newyork.cs.rit.edu");
			IDTable.put(8,"joplin.cs.rit.edu");
			IDTable.put(9,"yes.cs.rit.edu");
			IDTable.put(10,"holly.cs.rit.edu");
		}
	}
	
	class FileData implements Serializable{
		public File file;
		public ArrayList<String> trails = new ArrayList<String>();
		
		public FileData(File file,String serverName){
			this.file = file;
			trails.add(serverName);
		}
	}

	public int getHash(String string, int leafLevel, int j)throws RemoteException;	
	
}
