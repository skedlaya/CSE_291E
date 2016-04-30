package rmi;

import java.net.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/** RMI skeleton

    <p>
    A skeleton encapsulates a multithreaded TCP server. The server's clients are
    intended to be RMI stubs created using the <code>Stub</code> class.

    <p>
    The skeleton class is parametrized by a type variable. This type variable
    should be instantiated with an interface. The skeleton will accept from the
    stub requests for calls to the methods of this interface. It will then
    forward those requests to an object. The object is specified when the
    skeleton is constructed, and must implement the remote interface. Each
    method in the interface should be marked as throwing
    <code>RMIException</code>, in addition to any other exceptions that the user
    desires.

    <p>
    Exceptions may occur at the top level in the listening and service threads.
    The skeleton's response to these exceptions can be customized by deriving
    a class from <code>Skeleton</code> and overriding <code>listen_error</code>
    or <code>service_error</code>.
*/
public class Skeleton<T>
{
	// Server interface class object
	private Class<T> serverIntClass;
	
	//Server object
	private T server;
	
	//IP address of the server
	private InetSocketAddress inetSocketAddress;
	
	// Server socket
	private ServerSocket serverSocket;
	
	// Flag to check if server has started
	private boolean serverStarted = false;
	
    public static final int RESULT_OK   = 0;
    
    public static final int RESULT_ERR  = 1;
    /** Creates a <code>Skeleton</code> with no initial server address. The
        address will be determined by the system when <code>start</code> is
        called. Equivalent to using <code>Skeleton(null)</code>.

        <p>
        This constructor is for skeletons that will not be used for
        bootstrapping RMI - those that therefore do not require a well-known
        port.

        @param c An object representing the class of the interface for which the
                 skeleton server is to handle method call requests.
        @param server An object implementing said interface. Requests for method
                      calls are forwarded by the skeleton to this object.
        @throws Error If <code>c</code> does not represent a remote interface -
                      an interface whose methods are all marked as throwing
                      <code>RMIException</code>.
        @throws NullPointerException If either of <code>c</code> or
                                     <code>server</code> is <code>null</code>.
     */
    public Skeleton(Class<T> c, T server)
    {   
    	this(c,server,null);
    	//this.serverIntClass = c;
    	//this.server = server;
    	//this.inetSocketAddress = null;
        //throw new UnsupportedOperationException("not implemented");
        if(!RMIException.checkRemoteInt(c)){
                throw new Error("The class server interface is not a remote interface.");
        }

        if((server == null)||(c == null)){
                throw new NullPointerException("Server or interface server class is null");
        }

        if(!c.isInterface()){
                throw new Error("The class server interface is not a interface.");
        }

    }

    /** Creates a <code>Skeleton</code> with the given initial server address.

        <p>
        This constructor should be used when the port number is significant.

        @param c An object representing the class of the interface for which the
                 skeleton server is to handle method call requests.
        @param server An object implementing said interface. Requests for method
                      calls are forwarded by the skeleton to this object.
        @param address The address at which the skeleton is to run. If
                       <code>null</code>, the address will be chosen by the
                       system when <code>start</code> is called.
        @throws Error If <code>c</code> does not represent a remote interface -
                      an interface whose methods are all marked as throwing
                      <code>RMIException</code>.
        @throws NullPointerException If either of <code>c</code> or
                                     <code>server</code> is <code>null</code>.
     */
    public Skeleton(Class<T> c, T server, InetSocketAddress address)
    {
    	this.serverIntClass = c;
    	this.server = server;
    	this.inetSocketAddress = address;
    	if(!RMIException.checkRemoteInt(c)){
    		throw new Error("The class server interface is not a remote interface.");
    	}
    	if((server == null)||(c == null)){
    		throw new NullPointerException("Server or interface server class is null");
    	}

        if(!c.isInterface()){
                throw new Error("The class server interface is not a interface.");
        }

        //throw new UnsupportedOpertionException("not implemented");
    }

    /** Called when the listening thread exits.

        <p>
        The listening thread may exit due to a top-level exception, or due to a
        call to <code>stop</code>.

        <p>
        When this method is called, the calling thread owns the lock on the
        <code>Skeleton</code> object. Care must be taken to avoid deadlocks when
        calling <code>start</code> or <code>stop</code> from different threads
        during this call.

        <p>
        The default implementation does nothing.

        @param cause The exception that stopped the skeleton, or
                     <code>null</code> if the skeleton stopped normally.
     */
    protected void stopped(Throwable cause)
    {
    }

    /** Called when an exception occurs at the top level in the listening
        thread.

        <p>
        The intent of this method is to allow the user to report exceptions in
        the listening thread to another thread, by a mechanism of the user's
        choosing. The user may also ignore the exceptions. The default
        implementation simply stops the server. The user should not use this
        method to stop the skeleton. The exception will again be provided as the
        argument to <code>stopped</code>, which will be called later.

        @param exception The exception that occurred.
        @return <code>true</code> if the server is to resume accepting
                connections, <code>false</code> if the server is to shut down.
     */
    protected boolean listen_error(Exception exception)
    {
        return false;
    }

    /** Called when an exception occurs at the top level in a service thread.

        <p>
        The default implementation does nothing.

        @param exception The exception that occurred.
     */
    protected void service_error(RMIException exception)
    {
    }

    //Class ServerHandler
    private class ServerHandler extends Thread{
 
    	//Create a client 
    	private Socket clientSocket;
    	
    	//Constructor
    	public ServerHandler(Socket clientSocket){
    	    this.clientSocket = clientSocket;	
    	}
    	
        @Override
        public void run() {
        	System.out.println("Entered serverHandler block");
        	ObjectOutputStream outputStream = null;
        	try{
        		//Outputsteam object
        	    outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        	    outputStream.flush();
        	    
        	    //Inputstream object
        	    ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
        	    
        	    // Get argumnets for method invoke 
        	    String methodName = (String) inputStream.readObject();
        	    Class[] methodParamTypes = (Class[]) inputStream.readObject();
        	    Object[] methodArgs = (Object[]) inputStream.readObject();
        	    
        	    // Get object corresponding to method requested by stub
        	    
        	    Method method = serverIntClass.getDeclaredMethod(methodName, methodParamTypes);
        	    Object serverObject = method.invoke(server, methodArgs);
        	    
        	    // Send object back to stub
        	    outputStream.writeObject(RESULT_OK);
        	    outputStream.writeObject(serverObject);
        	    
        	    clientSocket.close();
        	}
        	catch(InvocationTargetException exp){
        		try {
        			outputStream.writeObject(RESULT_ERR);
        			outputStream.writeObject(exp.getCause());
                	} 
        		catch (IOException expIO) {
                    throw new Error(expIO.getMessage());
                	}
        	}
        	catch (Exception Exp) {
                try {
        			outputStream.writeObject(RESULT_ERR);
                    outputStream.writeObject(Exp);
                  	} 
                catch (IOException expIO) {
                    }
            } 
        	
        	finally {
                try {
                	    System.out.println("In final block1");
                        if(clientSocket != null && !clientSocket.isClosed()) {
                        	clientSocket.close();
                        }
                    	System.out.println("In final block2");
                  	} 
                catch (IOException exp) {
                        exp.getMessage();
                    }
            }
        	
        }    	
}
    
    
    /** Starts the skeleton server.

        <p>
        A thread is created to listen for connection requests, and the method
        returns immediately. Additional threads are created when connections are
        accepted. The network address used for the server is determined by which
        constructor was used to create the <code>Skeleton</code> object.

        @throws RMIException When the listening socket cannot be created or
                             bound, when the listening thread cannot be created,
                             or when the server has already been started and has
                             not since stopped.
     */
    public synchronized void start() throws RMIException
    {
    	System.out.println("1. Entered skeleton start!!!!");
    	
    	// Create address if null (depends on skeleton constructor)
    	if(inetSocketAddress == null){
    		inetSocketAddress = new InetSocketAddress(0);
    	}

    	System.out.println("2. Entered skeleton start!!!!");

    	// Exception if server has started
    	if(serverStarted == true)
    	    throw new RMIException("start(): Server has already started");
    	System.out.println("3. Entered skeleton start!!!!");

    	// Create and bind listening socket
    	// Throw RMI exception if it fails
    	try {
    		serverSocket = new ServerSocket();
    		serverSocket.bind(inetSocketAddress);
    	}
    	catch(Throwable exp){
    		throw new RMIException("Server socket creation failed!!!!!!");
    	}
    	
    	System.out.println("4. Entered skeleton start!!!!");
    	serverStarted = true;
        new Thread(new Runnable() {
            @Override 
            public void run() {
            	System.out.println("5. Entered skeleton start!!!!");
                while(serverStarted && !serverSocket.isClosed()){
                	System.out.println("Waiting for connection!!!!");
                	try{
                		System.out.println("Skeleton Port number is:"+ inetSocketAddress.getPort());
                		Socket clientSocket = serverSocket.accept();
                		System.out.println("thread started!!!!");
                		//ServerHandler serverHandler = new ServerHandler(clientSocket);
                		//serverHandler.start();
                		new ServerHandler(clientSocket).start();
                		
                		System.out.println("After serverhandler thread creation in start");
                		
                	}catch(SocketException e) {
                		System.out.println("Socket Exception!!!!");
                    } 
                	catch(Exception ee){
                	
                	}
                	catch(Throwable exp){
                		throw new Error(exp.getLocalizedMessage());
                	}
                }
            }
            
        }).start();
      
       // t.start();    	    
    	    
        //throw new UnsupportedOperationException("not implemented");
    }

    /** Stops the skeleton server, if it is already running.

        <p>
        The listening thread terminates. Threads created to service connections
        may continue running until their invocations of the <code>service</code>
        method return. The server stops at some later time; the method
        <code>stopped</code> is called at that point. The server may then be
        restarted.
     */
    public synchronized void stop()
    {
    	System.out.println("Entered stop"+serverStarted);
    	if(serverStarted == false)
    		return;
    	else{
    		try{
    			serverSocket.close();
    			if(serverSocket.isClosed())
    				System.out.println("serverSocket is closed successfullt checking is closed");
    			this.stopped(null);
    		}
    		catch(Throwable exp){
    			System.out.println("In stop exception");
    			this.stopped(exp);
    		}
    		
    		serverStarted = false;
    	}	

    }
    
    public InetSocketAddress getROR(){
    	if(serverSocket != null && serverSocket.isBound()){
    		return new InetSocketAddress(serverSocket.getInetAddress(), serverSocket.getLocalPort());
    	}
    	
    	return inetSocketAddress;
    }
}
