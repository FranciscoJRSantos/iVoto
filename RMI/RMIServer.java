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
import java.text.SimpleDateFormat;
import java.text.ParseException;

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
 
    System.getProperties().put("java.security.policy", "policy.all");
    System.setSecurityManager(new SecurityManager());
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

  public String checkID(int cc, int eleicao_id) throws RemoteException {

    String toClient = null;
    String nameUser = null;
    ArrayList<String> aux;
    ArrayList<String> aux2;
    ArrayList<String> aux3;
    int role, departamento_id, faculdade_id, election_type, departamento_eleicao, faculdade_eleicao;

    String sql1 = "SELECT role,name FROM User WHERE numeroCC='" + cc + "';";
    String sql2 = "SELECT tipo FROM Eleicao WHERE ID='" + eleicao_id + "';";
    String sql3 = "SELECT faculdade_id FROM User WHERE numeroCC='" + cc + "';";
    String sql4 = "SELECT departamento_id FROM User WHERE numeroCC='" + cc + "';";
    String sql5 = "SELECT departamento_id FROM Departamento_Eleicao WHERE eleicao_id='" + eleicao_id + "';";
    String sql6 = "SELECT faculdade_id FROM Faculdade_Eleicao WHERE eleicao_id ='" + eleicao_id + "';";


    aux2 = database.submitQuery(sql1);
    role = Integer.parseInt(aux2.get(0));
    nameUser = aux2.get(1);

    aux2 = database.submitQuery(sql3);
    faculdade_id = Integer.parseInt(aux2.get(0));

    aux2 = database.submitQuery(sql4);
    departamento_id = Integer.parseInt(aux2.get(0));

    aux3 = database.submitQuery(sql2);
    election_type = Integer.parseInt(aux3.get(0));
    aux3 = database.submitQuery(sql5);
    departamento_eleicao = Integer.parseInt(aux3.get(0));
    aux3 = database.submitQuery(sql6);
    faculdade_eleicao = Integer.parseInt(aux3.get(0));

    switch (election_type){
      case 1:
        // Eleiçoes Nucleo de Estudantes
        if (role == 1){
          if(departamento_eleicao == departamento_id){
            toClient = nameUser;
          }
        }
        break;
      case 2:
        if (role == 1)
          toClient = nameUser;
        // Conselho Geral Estudantes
        break;
      case 3:
        if (role == 2)
          toClient = nameUser;
        // Conselho Geral Docentes
        break;
      case 4:
        if (role == 3)
          toClient = nameUser;
        // Conselho Geral Funcionarios
        break;
      case 5:
        if (role == 2){
          if (faculdade_id == faculdade_eleicao)
            toClient = nameUser;
        }
        // Direçao Faculdade
        break;
      case 6:
        if (role == 2)
          if (departamento_id == departamento_eleicao)
            toClient = nameUser;
        // Direçao Departamento
        break;
    }

    return nameUser;
  }

  public boolean checkLogin(int cc, String username, String password) throws RemoteException {
    boolean toClient = true;
    ArrayList<String> aux;
    String sql = "SELECT ID FROM User WHERE numeroCC='"+ cc + "' AND name='" + username + "'AND hashed_password='" + password + "';";
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

  public ArrayList<String> showTables(int eleicao_id) throws RemoteException{

    ArrayList<String> aux;
    String sql = "SELECT ID FROM MesaVoto WHERE eleicao_id='" + eleicao_id + "';";

    aux = database.submitQuery(sql);
    return aux;
  }

  public boolean vote(int cc, String lista, int eleicao_id, int mesavoto_id) throws RemoteException{

    // FINNISH THISSSSSSSSSSSSSSSSSSSSSSSSSSSSSS 
    boolean toClient = true;
    ArrayList<String> aux1;
    ArrayList<String> aux2;
    String sql1 = "UPDATE Lista SET votos = votos +1 WHERE name='" + lista + "';";
    database.submitQuery(sql1);
    String sql2 = "SELECT ID FROM User WHERE numeroCC='" + cc + "';";
    aux1 = database.submitQuery(sql2);
    System.out.println(aux1);
    String sql3 = "SELECT hasVoted FROM User_Eleicao WHERE user_id='" + aux1.get(0) + "' AND eleicao_id='" +  eleicao_id + "';";
    aux2 = database.submitQuery(sql3);
    if (aux2.isEmpty()){
      String sql5 = "INSERT INTO User_Eleicao (user_id,eleicao_id,hasVoted,mesavoto_id) VALUES('" + aux1.get(0) + "'," + eleicao_id + "," + ",True,'" + mesavoto_id + "');";
      database.submitQuery(sql5);
    }
    else{
      toClient = false;
    }
    return toClient;
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

  public ArrayList<ArrayList<String>> viewCurrentElections() throws RemoteException{

    ArrayList<ArrayList<String>> container = new ArrayList<>();
    ArrayList<String> ID;
    ArrayList<String> titulos;
    ArrayList<String> dateInicio;
    ArrayList<String> dateFim;

    //TODO: only view active and future elections

    String sql1 = "SELECT ID FROM Eleicao;";
    ID = database.submitQuery(sql1);
    sql1 = "SELECT titulo FROM Eleicao;";
    titulos = database.submitQuery(sql1);
    sql1 = "SELECT inicio FROM Eleicao;";
    dateInicio = database.submitQuery(sql1);
    sql1 = "SELECT fim FROM Eleicao;";
    dateFim = database.submitQuery(sql1);

    container.add(ID);
    container.add(titulos);
    container.add(dateInicio);
    container.add(dateFim);

    return container;
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
    String sql="";

    if (flag == 1){
      sql = "DELETE FROM Departamento WHERE ID='" + dep + "';";
      database.submitQuery(sql);
    }
    else if(flag ==2){
      sql = "DELETE FROM Faculdade WHERE ID='" + dep + "';";
      database.submitQuery(sql);
    }

    return toClient;
  }

  public ArrayList<String> viewListsFromElection(int id) throws RemoteException{

    ArrayList<String> toClient;
    String sql = "SELECT nome FROM Lista WHERE eleicao_id='" + id + "';";

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

  public boolean addDepFac(int faculdade_id, String newName, int flag) throws RemoteException{
    //add departamento / faculdade. flag 1 - dep, flag 2 - fac 
    boolean toClient = true;
    String sql;

    if(flag==1){
      sql = "INSERT INTO Departamento (name,faculdade_id) VALUES ('" + newName + "','" + faculdade_id+ "';";
      database.submitQuery(sql);
    }
    else if (flag==2){
      sql = "INSERT INTO Faculdade (name) VALUES ('"+newName+"';";
      database.submitQuery(sql);
    }
    return toClient;
  }

  public boolean manageTable(int idTable, int idUser, int idNewUser) throws RemoteException{

    //mudar a pessoa que está na mesa 

    boolean toClient = true;
    String sql = "UPDATE User WHERE mesavoto_id='" + idTable + "SET mesavoto_id = NULL;";
    database.submitQuery(sql);
    sql = "UPDATE User WHERE ID='" + idNewUser + "' SET mesavoto_id='" + idTable + "';";
    database.submitQuery(sql);

    return toClient;
  }

  public boolean editPerson(int idUser, String newInfo, int flag) throws RemoteException{
    //edita a info de uma pessoa, manda a newinfo sempre como string e depois cabe ao server passar de string para int caso seja necessario. flag 1 - name, flag 2 - Address, flag 3 - phone, flag 4 - ccn, flag 5 - ccv, flag 6 - dep, flag 7 - pass

    int aux;
    String sql="";

    switch (flag){
      case 1:
        sql = "UPDATE User WHERE ID='" + idUser + "' SET name='" + newInfo + "';";
        break;
      case 2:
        sql = "UPDATE User WHERE ID='" + idUser + "' SET morada='" + newInfo + "';";
        break;
      case 3:
        aux = Integer.parseInt(newInfo);
        sql = "UPDATE User WHERE ID='" + idUser + "' SET contacto='" + aux + "';";
        break;
      case 4:
        aux = Integer.parseInt(newInfo);
        sql = "UPDATE User WHERE ID='" + idUser + "' SET numeroCC='" + aux + "';";
        break;
      case 5:
        break;
      case 6:
        aux = Integer.parseInt(newInfo);
        sql = "UPDATE User WHERE ID='" + idUser + "' SET departamento_id='" + aux + "';";
        break;
      case 7:
        aux = Integer.parseInt(newInfo);
        sql = "UPDATE User WHERE ID='" + idUser + "' SET hashed_password='" + aux + "';";
        break;
      default:
        break;

    }
    if (!sql.equals("")){
      database.submitQuery(sql); 
    }
    return true;
  }   

  public boolean anticipatedVote(int idElec, int idUser, int vote, String pass) throws RemoteException{
    //vote antecipado. o int vote é um int da lista de listas disponiveis retornada pela "viewListsFromElection"

    return true;
  } 

  public boolean createList(String nome, int tipo, int eleicao_id) throws RemoteException{
    
    String sql = "INSERT INTO Lista (name,tipo,votos,eleicao_id) VALUES('"+ nome +"','"+ tipo + "','" + "',0,'" + eleicao_id + "');";
    database.submitQuery(sql);

    return true;
  }

  public ArrayList<String> checkResults(int idElec) throws RemoteException{
    //recebe uma lista com [[lista,nº de votos],...]. return [[null,null]] em caso de insucesso
    ArrayList<String> aux;
    String sql = "SELECT nome,votos FROM Lista WHERE eleicao_id='" + idElec +"';";

    return database.submitQuery(sql);
  }

  public int TableInfo(int idTable, int idElec) throws RemoteException{
    //realtime info sobre o estado das mesas (return -1) if down, e numero de votos feitos naquela mesa (return n)
    int nVotos;
    ArrayList<String> aux;
    String sql = "SELECT numeroVotos FROM MesaVoto WHERE ID='" + idTable + "' AND eleicao_id='" + idElec + "' AND active=True;";
    aux = database.submitQuery(sql);
    if (aux.isEmpty()){
      nVotos = -1;
    }
    else {
      nVotos = Integer.parseInt(aux.get(0));
    }

    return nVotos;
  }

  public java.util.Date showHour(int idUser, int idElec) throws RemoteException{ 

    //saber quando uma pessoa votou, retorna algo que indique erro :) nao sei :) fds :)
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
    Date toClient = null;
    ArrayList<String> aux;
    String sql = "SELECT whenVoted FROM User_Eleicao WHERE user_id='" + idUser + "' AND eleicao_id='" + idElec + "';";
    aux = database.submitQuery(sql);
    try{
      toClient = formatter.parse(aux.get(0));
    } catch (ParseException pe){
      System.out.println("Invalid date");
    }

    return toClient;
  } 

  public boolean changeElectionsDates(int id, java.sql.Date newdate, int flag) throws RemoteException{

    boolean toClient = true;
    ArrayList<String> aux;
    String sql = "SELECT * FROM MetaVoto WHERE ID='" + id + "' AND active='False';";
    aux = database.submitQuery(sql);
    if (aux.isEmpty()){
      toClient = false;
    }
    else{
      if (flag == 1){
        sql = "UPDATE MesaVoto WHERE ID='" + id + "SET inicio='" + newdate + "';";
      }
      else if (flag == 2){
        sql = "UPDATE MesaVoto WHERE ID='" + id + "SET fim='" + newdate + "';";
      }
      database.submitQuery(sql);
    } 

    return toClient;
  }

  public boolean changeElectionsText(int id, String text, int flag) throws RemoteException{

    boolean toClient = true;
    ArrayList<String> aux;
    String sql = "SELECT * FROM MetaVoto WHERE ID='" + id + "' AND active='False';";
    aux = database.submitQuery(sql);
    if (aux.isEmpty()){
      toClient = false;
    }
    else{
      if (flag == 1){
        sql = "UPDATE MesaVoto WHERE ID='" + id + "SET titulo='" + text + "';";
      }
      else if (flag == 2){
        sql = "UPDATE MesaVoto WHERE ID='" + id + "SET descricao='" + text + "';";
      }
      database.submitQuery(sql);
    } 

    return toClient;
  }

  public boolean editDepFac(int dep, String newName, int flag) throws RemoteException{

    //edita nome de departamento / faculdade. flag 1 - dep, flag 2 - fac
    String sql1;

    if (flag == 1){
      sql1 = "UPDATE Departamento SET nome='" + newName + "' WHERE ID='" + dep + "';";
      database.submitQuery(sql1);
    }
    else if (flag == 2){
      sql1 = "UPDATE Faculdade SET nome='" + newName + "' WHERE ID='" + dep + "';";
      database.submitQuery(sql1);
    }

    return true;
  }

  public boolean criaEleiçãoNE(java.sql.Date beginning, java.sql.Date end, String title, String description, int dep) throws RemoteException{

    //cria eleição Nucleo de estudantes. . As eleições para núcleo de estudantes decorrem num único departamento e podem votar apenas os estudantes desse departamento.
    int eleicao_id;
    ArrayList<String> needed;
    String sql1 = "INSERT INTO Eleicao (titulo,descricao,inicio,fim,tipo) VALUES ('" + title + "','" + description + "','" + beginning + "','" + end + "',1);";
    database.submitQuery(sql1);

    String aux = "SELECT LAST_INSERT_ID();";
    needed = database.submitQuery(aux);
    eleicao_id = Integer.parseInt(needed.get(0));

    String sql2 = "INSERT INTO Departamento_Eleicao (faculdade_id, eleicao_id) VALUES (' " + dep + ",'" + eleicao_id + "');";
    database.submitQuery(sql1);
    database.submitQuery(sql2);

    return true;

  }

  public boolean criaEleiçãoCG(java.sql.Date beginning, java.sql.Date end, String title, String description) throws RemoteException{

    // os estudantes votam apenas nas listas de estudantes, os docentes votam apenas nas listas de docentes, e os funcionários votam apenas nas listas de funcionários.
    String sql1;
    sql1 = "INSERT INTO Eleicao (titulo,descricao,inicio,fim,tipo) VALUES ('" + title + "','" + description + "','" + beginning + "','" + end + "',2);";
    database.submitQuery(sql1);
    sql1 = "INSERT INTO Eleicao (titulo,descricao,inicio,fim,tipo) VALUES ('" + title + "','" + description + "','" + beginning + "','" + end + "',3);";
    database.submitQuery(sql1);
    sql1 = "INSERT INTO Eleicao (titulo,descricao,inicio,fim,tipo) VALUES ('" + title + "','" + description + "','" + beginning + "','" + end + "',4);";
    database.submitQuery(sql1);

    return true;

  }

  public boolean criaEleiçãoDF(java.sql.Date beginning, java.sql.Date end, String title, String description, int idFac) throws RemoteException{
    //cria eleição para a direção da faculdade
    int eleicao_id;
    ArrayList<String> needed;
    String sql1 = "INSERT INTO Eleicao (titulo,descricao,inicio,fim,tipo) VALUES ('" + title + "','" + description + "','" + beginning + "','" + end + "',5);";
    String aux = "SELECT LAST_INSERT_ID();";
    needed = database.submitQuery(aux);
    eleicao_id = Integer.parseInt(needed.get(0));

    String sql2 = "INSERT INTO Faculdade_Eleicao (faculdade_id, eleicao_id) VALUES (' " + idFac + ",'" + eleicao_id + "');";
    database.submitQuery(sql1);
    database.submitQuery(sql2);

    return true;
  }


  public boolean criaEleiçãoDD(java.sql.Date beginning, java.sql.Date end, String title, String description, int idDep) throws RemoteException{

    //cria eleição para a direção do departamento, concorrem e votam docentes desse departamento
    int eleicao_id;
    ArrayList<String> needed;
    String sql1 = "INSERT INTO Eleicao (titulo,descricao,inicio,fim,tipo) VALUES ('" + title + "','" + description + "','" + beginning + "','" + end + "',6);";
    String aux = "SELECT LAST_INSERT_ID();";
    needed = database.submitQuery(aux);
    eleicao_id = Integer.parseInt(needed.get(0));

    database.submitQuery(sql1);
    String sql2 = "INSERT INTO Departamento_Eleicao (faculdade_id, eleicao_id) VALUES (' " + idDep + ",'" + eleicao_id + "');";
    database.submitQuery(sql2);

    return true;
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
                DatagramPacket toSend = new DatagramPacket(message,message.length,InetAddress.getByName("127.0.0.1"),secUDP);
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
                DatagramPacket toSend = new DatagramPacket(message,message.length,InetAddress.getByName("127.0.0.1"),mainUDP);

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
