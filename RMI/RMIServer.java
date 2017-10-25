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
import java.util.*;
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

    RMIConfigLoader newConfig = new RMIConfigLoader();
    RMIServer.rmiName = newConfig.getRMIName();
    RMIServer.rmiPort = newConfig.getRMIPort();
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

  // TCP Methods

  public boolean checkID(int cc) throws RemoteException {
    
    boolean toClient = true;
    ArrayList<String> aux;
    String sql = "SELECT numeroCC FROM User WHERE numeroCC='" + cc + "'";
    aux = database.submitQuery(sql);
    if (aux.isEmpty()){
      toClient = false;
    }
  
    return toClient;
  }

  public boolean checkLogin(String username, String password) throws RemoteException {
    boolean toClient = true;
    ArrayList<String> aux;
    String sql = "SELECT ID FROM User WHERE name='" + username + "'AND hashed_password='" + password + "';";
    aux = database.submitQuery(sql);
    if(aux.isEmpty()){
      toClient = false;
    }
  
    return toClient;
  }

  public ArrayList<String>  listCandidates(int mesavoto_id) throws RemoteException{
    
    String sql = "SELECT NAME FROM Lista WHERE mesavoto_id='" + mesavoto_id + "';";

    return database.submitQuery(sql);
  
  }

  public void vote(int cc, String lista, int eleicao_id) throws RemoteException{
  
    // FINNISH THISSSSSSSSSSSSSSSSSSSSSSSSSSSSSS 
    ArrayList<String> aux1;
    String sql1 = "UPDATE Lista SET votos = votos +1 WHERE name='" + lista + "';";
    database.submitQuery(sql1);
    String sql2 = "SELECT ID FROM User WHERE numeroCC='" + cc + "';";
    aux1 = database.submitQuery(sql2);
    System.out.println(aux1);
    String sql3 = "SELECT ID FROM Eleicao WHERE ;";
    String sql4 = "INSERT INTO User_Eleicao (user_id,eleicao_id,hasVote) VALUES('" + aux1 + "'" + "," + ",True);";
  
  }

  // Admin Console
  
  public boolean addPerson(String name, String Address, int phone, int ccn, int ccv, int dep, int fac, String pass, int type) throws RemoteException{
    
    boolean toClient = true;
    
    String sql = "INSERT INTO User (name,hashed_password,contacto,morada,numeroCC,validadeCC,role,departamento_id,faculdade_id) VALUES('" + name + "','" + pass +"','"+ phone+"','"+Address+"','"+ccn+"','"+ccv+"','"+type+"','"+dep+"','"+fac+");";

    database.submitQuery(sql);
    
    return toClient;
  }

  public ArrayList<String> tableMembers(int idTable) throws RemoteException{
  
    ArrayList<String> aux;
    String sql = "SELECT name FROM User WHERE mesavoto_id'" + idTable + "';";
    
    aux = database.submitQuery(sql);
    return aux;
  }
  
  public int checkTable(int idUser, int idElec) throws RemoteException{
    
    int mesa;
    ArrayList<String> aux;

    String sql = "SELECT ID FROM User_Eleicao WHERE user_id='" + idUser + "' AND eleicao_id='" + idElec + "'";
    aux = database.submitQuery(sql);
    if (aux.isEmpty()){
      mesa = -1;
    }
    else {
      mesa = Integer.parseInt(aux.get(0)); 
    }

    return mesa;

  } 
  
  public ArrayList<String> viewCurrentElections() throws RemoteException{
    
    ArrayList<String> aux1;
    String sql1 = "SELECT ID AND titulo FROM Eleicao";
    return database.submitQuery(sql1);
  }

  public ArrayList<String> verDepartamentos() throws RemoteException{
  
    String sql1 = "SELECT ID AND nome FROM Departamento;";
    return database.submitQuery(sql1);
  }

  public ArrayList<String> verFaculdades() throws RemoteException{
  
    String sql1 = "SELECT ID AND nome FROM Faculdade;";
    return database.submitQuery(sql1);

  }

  public boolean rmDepFac(int dep, int flag) throws RemoteException{
    
    boolean toClient = true;
    String sql;

    if (flag == 1){
        sql = "DELETE FROM Departamento WHERE ID='" + dep + "';";
    }
    else if(flag ==2){
        sql = "DELETE FROM Faculdade WHERE ID='" + dep + "';";
    }
    database.submitQuery(sql);

    return toClient;
  
  }

  public ArrayList<String> viewListsFromElection(int id) throws RemoteException{
  
    ArrayList<String> toClient;
    String sql = "SELECT Lista WHERE eleicao_id='" + id + "';";
    
    toClient = database.submitQuery(sql);
    return toClient;
  }

  public boolean manageList(int idElec, String List, int flag) throws RemoteException{
    //flag 1 - add list, flag 2 - remove list
    boolean toClient = true;
    ArrayList<String> aux;
    String sql;

    if (flag == 1){
      sql = "INSERT INTO List (nome) VALUES ('" + List + "');"; 
      database.submitQuery(sql);
    }
    else if(flag ==2){
      sql = "REMOVE FROM List WHERE ID='" + idElec + "';";
      database.submitQuery(sql);
    }
    return toClient;
  } 

  public boolean changeElectionsText(int id, String text, int flag) throws RemoteException{
   //Muda titulo ou descriçao de uma eleiçao. flag 1 - titulo, flag 2 - descrição
    boolean toClient = true;
    String sql;

    if (flag == 1){
      sql = "UPDATE Eleicao WHERE id='" + id +"' SET titulo='" + text + "';";
      database.submitQuery(sql);
    
    }
    else if (flag == 2){
      sql = "UPDATE Eleicao WHERE id='" + id +"' SET descrição='" + text + "';";
      database.submitQuery(sql);
    }

    return toClient;
  }


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

        DatagramSocket aSocket = null;
        byte [] buffer = new byte[1024];
        if(mainServer == true){
          try{
            aSocket = new DatagramSocket(mainUDP);
            aSocket.setSoTimeout(pingFrequency);
          } catch(SocketException se) {
            se.printStackTrace();
          }
          while(true){

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

                try{ Naming.unbind(RMIServer.rmiName); }
                catch(RemoteException re){}
                catch (NotBoundException nbe){}
                catch (MalformedURLException murle){}

              }

            }while(i < retries);

            System.out.println("Backup Server failed! \n Retrying pings");


          }

        }

        else if(mainServer == false){

          try{
            aSocket = new DatagramSocket(secUDP);
            aSocket.setSoTimeout(pingFrequency);
          } catch(SocketException se){
            se.printStackTrace();
          }
          System.out.println("Is Backup Server");

          while(true){

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

            try{
              RMIServer.this.heartbeat = null;
              RMIServer.this.mainServer = true;
              RMIServer.this.startRMIServer();
              aSocket.close();
              Thread.currentThread().join();
            } catch(InterruptedException ie){

            }

          }

        }

      }

  }
}
