import java.rmi.*;
import java.rmi.server.*;
import java.net.*;

public class RMIServer extends UnicastRemoteObject implements Server {

  private static final long serialVersionUID = 1L;

  public RMIServer() throws RemoteException {
    super();
  }



  public static void main(String args[]) {

    try{

      RMIServer rmi = new RMIServer();
      Naming.rebind("rmi://localhost:8000",rmi);
      System.out.println("RMI Server ready!");
    }
    catch(RemoteException re){
      System.out.println("Remote Exception in RMIServer.main " + re);
    }
    catch(MalformedURLException murle){
      System.out.println("MalformedURLException in RMIServer.main " + murle);
    }

  }
}