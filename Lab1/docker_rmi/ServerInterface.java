import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote 
{
	public String WritePong(String text) throws RMIException;
}
