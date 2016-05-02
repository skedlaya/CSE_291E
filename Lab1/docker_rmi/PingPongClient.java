import java.net.*;
import java.rmi.*;

public class PingPongClient extends Exception{


	static InetSocketAddress address = new InetSocketAddress(5000);
	static ServerInterface stub_object;
	public static void main(String[] args){
	
	try{
		stub_object = Stub.create(ServerInterface.class, address);
		System.out.println("Creating Stub Object:"); 
	}

	catch(Exception e){
		System.out.println("Error from stub.create()");
	}
	
	testPing(stub_object);	
	
	}

	public static void testPing(ServerInterface x){
		
		int fail = 0;
		int i;
		for (i=0;i<4;i++){
			
			try{
			String str = x.WritePong("Ping 12345678");	
			if (!str.equals("Pong 12345678")){
				fail ++;
			}
			
			}
			
			catch(RMIException e){
			
			System.out.println("Error in Client Ping");
			}

		}

		System.out.println(i + "Tests Completed, " + fail + " Tests Failed");
	
	}

}
