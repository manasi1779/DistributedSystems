
import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ClientInterface extends Remote{
	
	public void sendFile(ServerInterface.FileData fileData) throws RemoteException;
}
