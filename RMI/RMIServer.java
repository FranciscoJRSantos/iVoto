/**
* @created by FranciscoJRSantos at 09/10/2017
* 
**/

import java.rmi.*;
import java.rmi.server.*;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.*;
import java.util.concurrent.TimeoutException;

public class RMIServer extends UnicastRemoteObject implements ServerInterface {

  static int rmiPort; // 1099
  static String rmiName;
  static int udpPort; // 6666
  static UDPConnection  heartbeat = null;

  private static final long serialVersionUID = 1L;

  public RMIServer() throws RemoteException {
    super();

    RMIConfigLoader newConfig = new RMIConfigLoader();
    rmiName = newConfig.getRMIName();
    rmiPort = newConfig.getRMIPort();
  }


  public static void main(String args[]) throws RemoteException{

    try{

      RMIServer rmiServer = new RMIServer();

      System.out.println(rmiName);
      Registry r = LocateRegistry.createRegistry(rmiPort);
      Naming.rebind(rmiName,rmiServer);

      System.out.println("Main RMIServer Started");

      heartbeat = new UDPConnection();

    } catch(ExportException ee){

      try{

        RMIServer backup = new RMIServer();

      } catch ( RemoteException re) {
        System.out.println("RemoteException: ");
        return;
      }

      System.out.println("Backup RMIServer Starting");
      
    } catch(RemoteException re){
      System.out.println("RemoteException: " + re);
      return; 
    } catch(MalformedURLException murle){
      System.out.println("MalformedURLException: " + murle);
      return;
    } 

  }

  static class UDPConnection extends Thread {

    static DatagramSocket aSocket;
    static int udpPort;
    static int pingFrequency;
    static int retries;

    public UDPConnection(){

      RMIConfigLoader config = new RMIConfigLoader();
      udpPort = config.getUDPPort();
      pingFrequency = config.getPingFrequency();
      retries = config.getRetries();
      aSocket = null;
      this.start();

    }

    @Override
    public void run(){

      byte [] buffer = new byte[1024];

      while(true){

        try{

          aSocket = new DatagramSocket(udpPort);
          aSocket.setSoTimeout(pingFrequency);

          int i = 0;

          do {

            try{

              byte [] message = "ping pong".getBytes();

              DatagramPacket toSend = new DatagramPacket(message,message.length,InetAddress.getByName("localhost"),udpPort);
              aSocket.send(toSend);
              System.out.println("[UDP] Ping");
              DatagramPacket toReceive = new DatagramPacket(buffer,buffer.length);
              aSocket.receive(toReceive);
              System.out.println("[UDP] Pong");
              i=0;

            } catch (SocketTimeoutException ste){
              i++;
            } catch (IOException ioe){
              System.out.println("Problemas de rede");
            }

          }while(i < retries);

        } catch (SocketException se) {


        }

      }

    }

  }

}
