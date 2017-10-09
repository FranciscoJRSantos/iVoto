/**
* @created by FranciscoJRSantos at 09/10/2017
* 
**/

import java.rmi.*;
import java.rmi.server.*;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RMIServer extends UnicastRemoteObject implements Server {

  private static final long serialVersionUID = 1L;
  public static final String rmiConfig = "rmi.txt";
  private static int registryPort; // 1099
  private static String bindName;

  public RMIServer() throws RemoteException {
    super();
  }


  public static void main(String args[]) throws RemoteException{

    new RMIServer().init();

  }

  public void init(){

    // Reading RMI Config file

    try{
      BufferedReader configReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(rmiConfig)));
      registryPort = Integer.parseInt(configReader.readLine());
      bindName = configReader.readLine();
      configReader.close();
    }
    catch(Exception e){
      System.out.println("Error opening RMI Config File");
      System.exit(1);
    }

    // Starting RMI Main Server

    try{

      RMIServer rmi = new RMIServer();
      Registry registry = LocateRegistry.createRegistry(registryPort);
      registry.rebind(bindName,rmi);
      System.out.println("RMI Server " + bindName + "ready on port " + registryPort +"!");
    }
    catch(RemoteException re){
      System.out.println("Remote Exception in RMIServer.main " + re);
    }


  } 

}
