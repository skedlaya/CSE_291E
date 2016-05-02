import java.net.*;
import java.rmi.*;


public class PingServerFactory extends Exception 
{

        static InetSocketAddress address = new InetSocketAddress(5000);
        static Skeleton skeleton = new Skeleton(ServerInterface.class, new PingPongServer(), address);
        int count = 0;

	public static Skeleton makePingServer(){
                try{
                        skeleton.start();
                }

                catch(RMIException e){
                        System.out.println("error in skeleton.start");
                }
	
		return skeleton;
	}

}
