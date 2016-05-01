package rmi;

import java.net.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/** RMI stub factory.

    <p>
    RMI stubs hide network communication with the remote server and provide a
    simple object-like interface to their users. This class provides methods for
    creating stub objects dynamically, when given pre-defined interfaces.

    <p>
    The network address of the remote server is set when a stub is created, and
    may not be modified afterwards. Two stubs are equal if they implement the
    same interface and carry the same remote server address - and would
    therefore connect to the same skeleton. Stubs are serializable.
 */
public abstract class Stub<T>
{
	// Server interface class object
	public static String classString = null;
	
	private static class ClientHandler implements Serializable, InvocationHandler
	{
		
		static final long serialVersionUID = 42L;
		
		/* Address the server */
		private InetSocketAddress serverAddress;
		

		/* Constructor*/
		public ClientHandler (InetSocketAddress serverAddress)
		{
			this.serverAddress = serverAddress;
			
		}
		
		private Object RemoteInvoke(Object proxy, Method method, Object[] args) throws Throwable
		{
			Socket clientSocket = new Socket();
			Object fromServer = null;
			ObjectOutputStream outputStream = null;
			Integer result = null;
			
			try
			{
				// Check creation
				clientSocket = new Socket();
				clientSocket.connect(this.serverAddress);
				
        		//Outputsteam object
        	    outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        	    outputStream.flush();
        	    
        	    //Inputstream object
        	    ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
        	    
        	    /* Preparing for Remote Invocation*/
        	    outputStream.writeObject(method.getName());
        	    outputStream.writeObject(method.getParameterTypes());
        	    outputStream.writeObject(args);
        	    
        	    /* Getting Results from Remote Interface */
        	    //try{
        	    result = (Integer) inputStream.readObject();
        	    fromServer = inputStream.readObject();
        	    
        	    /*}
        	    catch(Throwable ee){
        	    	if(fromServer.equals(Throwable.class))
        	    		throw new RMIException(ee);
        	    }*/
        	    clientSocket.close();

			}
			catch(Exception exp)
			{
				throw new RMIException(exp);
			}
			
			finally
			{
				if(clientSocket != null && !clientSocket.isClosed())
					clientSocket.close();
			}
			if(result == Skeleton.RESULT_ERR){
				throw (Throwable) fromServer;}

			return fromServer;
			
		}
		
		private Object LocalInvoke(Object proxy, Method method, Object[] args) throws RMIException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
		{
			/*Implement equals and hashCode? */
			if(method.getName().equals("equals")){
				return equals(proxy,method,args);
			}
			
			/*Get a client handler */
			
			ClientHandler clienthandle = (ClientHandler) Proxy.getInvocationHandler(proxy);
			
			//Hashcode. Check properly
			if(method.getName().equals("hashCode")){
				return clienthandle.serverAddress.hashCode() + proxy.getClass().hashCode();
			}
			
			//toString.
			if(method.getName().equals("toString")){
				return classString + clienthandle.serverAddress.toString() 
			    + clienthandle.serverAddress.getHostString();
			}
			
			return method.invoke(clienthandle, args);
		}
        private Boolean equals(Object proxy, Method method, Object[] args) {
            if(args.length != 1) {
                return Boolean.FALSE;
            }
            
            Object obj = args[0];
            if(obj == null) {
                return Boolean.FALSE;
            }
            
            // 1. check proxy class
            if(!Proxy.isProxyClass(obj.getClass())) {
                return Boolean.FALSE;
            }
            
            // 2. check class type
            if(!proxy.getClass().equals(obj.getClass())) {
                return Boolean.FALSE;
            }
            
            // 3. check handler type
            InvocationHandler handler = Proxy.getInvocationHandler(obj);
            if(!(handler instanceof ClientHandler)) {
                return Boolean.FALSE;
            }
            
            // 4. check remote address
            if(!serverAddress.equals(((ClientHandler) handler).serverAddress)) {
                return Boolean.FALSE;
            }
            
            return Boolean.TRUE;
        }	
		
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
		{
			try
			{
				if(RMIException.checkRMI(method))
					return RemoteInvoke(proxy, method, args);
				else
					return LocalInvoke(proxy, method, args);
			
			}
			catch(Exception exp)
			{
				throw exp;
				//throw new RMIException(exp);
			}
		}
		
	}
    /** Creates a stub, given a skeleton with an assigned adress.

        <p>
        The stub is assigned the address of the skeleton. The skeleton must
        either have been created with a fixed address, or else it must have
        already been started.

        <p>
        This method should be used when the stub is created together with the
        skeleton. The stub may then be transmitted over the network to enable
        communication with the skeleton.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param skeleton The skeleton whose network address is to be used.
        @return The stub created.
        @throws IllegalStateException If the skeleton has not been assigned an
                                      address by the user and has not yet been
                                      started.
        @throws UnknownHostException When the skeleton address is a wildcard and
                                     a port is assigned, but no address can be
                                     found for the local host.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, Skeleton<T> skeleton)
        throws UnknownHostException
    {
    	classString = c.getSimpleName();
    	/*Null Pointer Exception */
    	if ((c == null)||(skeleton == null))
    			throw new NullPointerException("Arguments to create are NULL!!");
			
        if(!c.isInterface()){
                throw new Error("The class server interface is not a interface.");
        }

        
    	/* if c is not a remote interface */
    	if(!RMIException.checkRemoteInt(c))
    	{
    		throw new Error ("c is not a remote interface");
    	}


    	
    	InetSocketAddress checkROR = skeleton.getROR();
    	
    	if (checkROR == null)
    			throw new IllegalStateException("ROR is NULL");
    	
    	/*Implement UnknownHostException here */
    	
    	ClientHandler cli = new ClientHandler(checkROR);
    	return (T) Proxy.newProxyInstance(c.getClassLoader(), new Class[] {c}, cli);
    	
    }

    /** Creates a stub, given a skeleton with an assigned address and a hostname
        which overrides the skeleton's hostname.

        <p>
        The stub is assigned the port of the skeleton and the given hostname.
        The skeleton must either have been started with a fixed port, or else
        it must have been started to receive a system-assigned port, for this
        method to succeed.

        <p>
        This method should be used when the stub is created together with the
        skeleton, but firewalls or private networks prevent the system from
        automatically assigning a valid externally-routable address to the
        skeleton. In this case, the creator of the stub has the option of
        obtaining an externally-routable address by other means, and specifying
        this hostname to this method.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param skeleton The skeleton whose port is to be used.
        @param hostname The hostname with which the stub will be created.
        @return The stub created.
        @throws IllegalStateException If the skeleton has not been assigned a
                                      port.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, Skeleton<T> skeleton,
                               String hostname)
    {
    	/*Null Pointer Exception */
    	if ((c == null)||(skeleton == null)||(hostname == null))
    			throw new NullPointerException("Arguments to create are NULL!!");

        if(!c.isInterface()){
                throw new Error("The class server interface is not a interface.");
        }

    	/* if c is not a remote interface */
    	if(!RMIException.checkRemoteInt(c))
    	{
    		throw new Error ("c is not a remote interface");
    	}
    	
    	InetSocketAddress getRORaddress = skeleton.getROR();
    	
    	InetSocketAddress checkROR = new InetSocketAddress(hostname, getRORaddress.getPort());
    	
    	if (checkROR == null)
    			throw new IllegalStateException("ROR is NULL");
    	
    	/* Check for failures here */
    	
    	ClientHandler cli = new ClientHandler(checkROR);
    	return (T) Proxy.newProxyInstance(c.getClassLoader(), new Class[] {c}, cli);
    }

    /** Creates a stub, given the address of a remote server.

        <p>
        This method should be used primarily when bootstrapping RMI. In this
        case, the server is already running on a remote host but there is
        not necessarily a direct way to obtain an associated stub.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param address The network address of the remote skeleton.
        @return The stub created.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, InetSocketAddress address)
    {
    	/*Null Pointer Exception */
    	if ((c == null)||(address == null))
    			throw new NullPointerException("Arguments to create are NULL!!");
        
    	/* if c is not a remote interface */
    	if(!RMIException.checkRemoteInt(c))
    	{
    		throw new Error ("c is not a remote interface");
    	}
    	
        if(!c.isInterface()){
                throw new Error("The class server interface is not a interface.");
        }


    	/* Check for failures here */
    	
    	ClientHandler cli = new ClientHandler(address);
    	return (T) Proxy.newProxyInstance(c.getClassLoader(), new Class[] {c}, cli);
   }
}
