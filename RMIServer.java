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
      System.out.println("RMI Server " + bindName + " ready on port " + registryPort +"!");
      rmi.mainConnection();
    }
    catch(ExportException ee){
      //System.out.println("Export Exception in RMIServer.main " + ee.getMessage());
      System.out.println("Starting Backup Server");
      try{
        new RMIServer().backupConnection();
        System.out.println("Remote");
      }catch(RemoteException re){
        System.out.println("RemoteException: " + re.getMessage());
      }
    }
    catch(RemoteException re){
      //System.out.println("Remote Exception in RMIServer.main " + re.getMessage());
    }

  }

  public void mainConnection() throws RemoteException{

    Thread mainConnection = new Thread(new Runnable(){


      @Override
      public void run(){

        DatagramSocket mainSocket = null;

        String pingMsg = "Pong";


        System.out.println("OIIIIIIIIIIIIIIIIIII");

        try{

          mainSocket = new DatagramSocket(6666);

          System.out.println("main socket listening on 6666");

          while(true){

            byte[] reply = new byte[1024];

            // Receiving ping
            DatagramPacket pong = new DatagramPacket(reply,reply.length);
            mainSocket.receive(pong);

            System.out.println(new String(pong.getData(), 0, pong.getLength()));

            // Sending pong
            byte[] msgByte = pingMsg.getBytes();

            DatagramPacket ping = new DatagramPacket(msgByte,msgByte.length,pong.getAddress(),pong.getPort());
            mainSocket.send(ping);

          }


        }catch(SocketException se){
          System.out.println("SocketException: " + se.getMessage());
        }catch(IOException ioe){
          System.out.println("IOException: " + ioe.getMessage());
        }finally{if (mainSocket != null) mainSocket.close();}
      }

    });

    mainConnection.start();
  } 

  public void backupConnection() throws RemoteException{

    Thread backupConnection = new Thread(new Runnable(){

      @Override
      public void run(){

        DatagramSocket backupSocket = null;

        String pingMsg = "Ping";

        System.out.println("XAUUUUUUUUUUUUUUUUU");

        try{

          backupSocket = new DatagramSocket(6666);

          while(true){

            byte[] msgByte = pingMsg.getBytes();

            InetAddress host = InetAddress.getByName("localhost");
            int sndPort = 6666;

            // Sending ping

            DatagramPacket ping = new DatagramPacket(msgByte,msgByte.length,host,sndPort);
            backupSocket.send(ping);

            // Receiving pong
            byte[] reply = new byte[1024];

            DatagramPacket pong = new DatagramPacket(reply,reply.length);
            backupSocket.receive(pong);
            System.out.println(new String(pong.getData(), 0, pong.getLength()));
          }


        }catch(SocketException se){
          System.out.println("SocketException: " + se.getMessage());
        }catch(IOException ioe){
          System.out.println("IOException: " + ioe.getMessage());
        }finally{if (backupSocket !=null) backupSocket.close();}
      }
    });

    backupConnection.start();

    /*Thread backupReceive = new Thread(new Runnable(){

      @Override
      public void run(){

        DatagramSocket rcvSckt = null;

        try{

          rcvSckt = new DatagramSocket();

          while(true){

            InetAddress host = InetAddress.getByName("localhost");
            int rcvPort = 6666;

          }
        }catch(SocketException se){
          System.out.println("SocketException: " + se.getMessage());
        }

      }
    });*/
  }

}
