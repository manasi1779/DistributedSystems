package Node2;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Node2.ServerInterface.FileData;

public interface ClientInterface extends Remote{
	
	public void sendFile(FileData fileData) throws RemoteException;
}
