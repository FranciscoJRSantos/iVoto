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
  UDPConnection heartbeat = null;
  boolean mainServer = true;

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
      rmiServer.setMainServer(true);
      rmiServer.startUDPConnection();


    } catch(ExportException ee){

      try{

        RMIServer backup = new RMIServer();

        backup.setMainServer(false);
        backup.startUDPConnection();

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

  public void setMainServer(boolean n){ this.mainServer = n; }

  public void startUDPConnection(){ this.heartbeat = new UDPConnection(mainServer); }

  class UDPConnection extends Thread {

    int mainUDP, secUDP;
    int pingFrequency;
    int retries;
    boolean mainServer = true;

    public UDPConnection(boolean serverType){

      RMIConfigLoader config = new RMIConfigLoader();
      mainUDP = config.getMainUDP();
      secUDP = config.getSecUDP();
      pingFrequency = config.getPingFrequency();
      retries = config.getRetries();
      mainServer = serverType;
      this.start();

    }

    @Override
    public void run(){

      byte [] buffer = new byte[1024];

      if(mainServer == true){

        System.out.println("Is Main Server");

        while(true){

          try{

            DatagramSocket aSocket = new DatagramSocket(mainUDP);
            aSocket.setSoTimeout(pingFrequency);
            byte [] message = "ping pong".getBytes();

            int i = 0;

            do {

              try{

                Thread.sleep(1000);
                DatagramPacket toSend = new DatagramPacket(message,message.length,InetAddress.getByName("localhost"),secUDP);
                aSocket.send(toSend);
                System.out.println("[UDP] Ping");
                DatagramPacket toReceive = new DatagramPacket(buffer,buffer.length);
                aSocket.receive(toReceive);
                System.out.println("[UDP] Pong");
                i=0;

              } catch (SocketTimeoutException ste){
                System.out.println("SocketTimeoutException: " + ste.getMessage());
                i++;
              } catch (IOException ioe){
                System.out.println("Problemas de rede");
              } catch (InterruptedException ie){

              }

            }while(i < retries);

            

          } catch (SocketException se) {


          }

        }

      }
      else if(mainServer == false){

        System.out.println("Is Backup Server");

        while(true){

          try{

            DatagramSocket aSocket = new DatagramSocket(secUDP);
            aSocket.setSoTimeout(pingFrequency);
            byte [] message = "ping pong".getBytes();

            int i = 0;

            do {

              try{

                Thread.sleep(1000);
                DatagramPacket toSend = new DatagramPacket(message,message.length,InetAddress.getByName("localhost"),mainUDP);
                aSocket.send(toSend);
                System.out.println("[UDP] Ping");
                DatagramPacket toReceive = new DatagramPacket(buffer,buffer.length);
                aSocket.receive(toReceive);
                System.out.println("[UDP] Pong");
                i=0;

              } catch (SocketTimeoutException ste){
                System.out.println("SocketTimeoutException: " + ste.getMessage());
                i++;
              } catch (IOException ioe){
                System.out.println("Problemas de rede");
              } catch (InterruptedException ie){

              }

            }while(i < retries);

          } catch (SocketException se) {


          }

        }

      }

      }
    }

  }
