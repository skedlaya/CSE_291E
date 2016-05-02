import java.net.*;
import java.rmi.*;


public class PingPongServer extends Exception implements ServerInterface
{
	
	static InetSocketAddress address = new InetSocketAddress(5000);
	static Skeleton skeleton;
	//static Skeleton skeleton = new Skeleton(ServerInterface.class, new PingPongServer(), address);
	int count = 0;
	public static void main(String[] args){
	
	PingServerFactory obj = new PingServerFactory();
	skeleton = obj. makePingServer();	

	}
	public String WritePong(String s) throws RMIException
	{
	
		System.out.println("inside writepong");
	if (count == 4){
			skeleton.stop();
			skeleton = null;
		}

		count += 1;
		String[] str = s.split(" ");
		return "Pong " + str[1];
	}

	
}
