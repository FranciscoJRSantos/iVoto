/*
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
  
  static DatabaseConnection database = null;
  static String rmiName;
  static int rmiPort; // 1099
  static int udpPort; // 6666
  static String dbIP;
  static int dbPort;
  UDPConnection heartbeat = null;
  boolean mainServer = true;

  private static final long serialVersionUID = 1L;

  public RMIServer() throws RemoteException {
    super();

    RMIConfigLoader newConfig = new RMIConfigLoader();
    rmiName = newConfig.getRMIName();
    rmiPort = newConfig.getRMIPort();
    dbIP = newConfig.getDBIP();
    dbPort = newConfig.getDBPort();
    database = new DatabaseConnection(dbIP,dbPort);
    startRMIServer();

  }

  public static void main(String args[]) throws RemoteException{

    RMIServer rmiServer = new RMIServer();

  }

  public void startRMIServer() {
    
    try{

      Registry r = LocateRegistry.createRegistry(rmiPort);
      Naming.rebind(rmiName,this);

      System.out.println("Main RMIServer Started");
      this.setMainServer(true);
      if (this.heartbeat == null){
        this.startUDPConnection();
      }
    } catch(ExportException ee){

        this.setMainServer(false);
        System.out.println(this.mainServer);
        if (this.heartbeat == null){
          this.startUDPConnection();
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

      System.out.println("Main UDP: " + mainUDP);
      System.out.println("Secondary UDP: " + secUDP);

      pingFrequency = config.getPingFrequency();
      retries = config.getRetries();
      mainServer = serverType;
      this.start();
      System.out.println("UDPConnection Started");

    }

    public void startUDPConnection(boolean serverType){

      UDPConnection udp = new UDPConnection(serverType);

    }

    @Override
    public void run(){

      byte [] buffer = new byte[1024];
      if(mainServer == true){

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

            System.out.println("Backup Server failed! \n Retrying pings");

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

            System.out.println("RMIServer failed \nAssuming Main Server Status");

            i = 0;

            try{
              RMIServer.this.heartbeat = null;
              RMIServer.this.mainServer = true;
              RMIServer.this.startRMIServer();
              Thread.currentThread().join();
            } catch(InterruptedException ie){

            }

          } catch (SocketException se) {

          }

        }

      }

      }
    }
  }
