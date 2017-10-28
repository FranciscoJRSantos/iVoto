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
import java.util.concurrent.TimeUnit;

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

  public void checkElectionStatus(){
    
    while(true){
      try{
        TimeUnit.SECONDS.sleep(10);
      } catch (InterruptedException ie){
        ie.printStackTrace();
      }
      String sql1 = "UPDATE Eleicao SET active=true WHERE inicio > NOW() AND fim < NOW();";
      String sql2 = "UPDATE Eleicao SET active=false WHERE inicio < NOW() OR fim > NOW();";

      database.submitUpdate(sql1);
      database.submitUpdate(sql2);

    }
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
      this.checkElectionStatus();
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

  public boolean isConnected() throws RemoteException { return true; }

  // TCP Methods

  /**
   * Função para verificar se um utilizador pode votar numa dada eleição.
   * @param cc - Recebe o número do cartão de cidadão de um utilizador
   * @param eleicao_id - Recebe o UniqueID da eleição onde o utilizador quer votar
   * @return Se o utilizador poder votar na eleição, retorna o nome do utilizador. Caso contrário retorna null.
   * @throws RemoteException Caso o RMI não consiga ser acedido é thrown uma RemoteException.
   */
  public String checkID(int cc, int eleicao_id) throws RemoteException {

    String toClient = null;
    String nameUser = null;
    ArrayList<String> aux;
    ArrayList<String> aux2;
    ArrayList<String> aux3;
    int role, departamento_id, faculdade_id, election_type, departamento_eleicao=0, faculdade_eleicao=0;

    String sql1 = "SELECT role,name FROM User WHERE numeroCC='" + cc + "';";
    String sql2 = "SELECT tipo FROM Eleicao WHERE ID='" + eleicao_id + "';";
    String sql3 = "SELECT faculdade_id FROM User WHERE numeroCC='" + cc + "';";
    String sql4 = "SELECT departamento_id FROM User WHERE numeroCC='" + cc + "';";
    String sql5 = "SELECT departamento_id FROM Departamento_Eleicao WHERE eleicao_id='" + eleicao_id + "';";
    String sql6 = "SELECT faculdade_id FROM Faculdade_Eleicao WHERE eleicao_id ='" + eleicao_id + "';";


    aux2 = database.submitQuery(sql1);
    if (aux2.isEmpty()){
      return null;
    }
    else {
      role = Integer.parseInt(aux2.get(0));
      nameUser = aux2.get(1);
    }

    aux2 = database.submitQuery(sql3);
    faculdade_id = Integer.parseInt(aux2.get(0));

    aux2 = database.submitQuery(sql4);
    departamento_id = Integer.parseInt(aux2.get(0));

    aux3 = database.submitQuery(sql2);
    election_type = Integer.parseInt(aux3.get(0));
    aux3 = database.submitQuery(sql5);
    if (!aux3.isEmpty()){
      departamento_eleicao = Integer.parseInt(aux3.get(0));
    }

    aux3 = database.submitQuery(sql6);
    if (!aux3.isEmpty()){
      faculdade_eleicao = Integer.parseInt(aux3.get(0));
    }

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

  /**
   * Funçao que retorna os utilizadores que estão numa mesa de voto
   * @param eleicao_id Recebe o ID da eleição onde pesquisar
   * @param mesavoto_id Recebe o ID da mesa de voto onde pesquisar
   * @return Uma ArrayList com ArrayLists de Strings em que o primeiro indice contem o cartao de cidadao do utilizador e o segundo indice tem o nome do utilizador
   */

  public ArrayList<ArrayList<String>> showUserTable(int eleicao_id, int mesavoto_id){

    ArrayList<ArrayList<String>> toClient = new ArrayList<ArrayList<String>>();
    ArrayList<String> cc;
    ArrayList<String> name;
    String sql1 = "SELECT numeroCC FROM User WHERE mesavoto_id='" + mesavoto_id + "';";
    String sql2 = "SELECT name FROM User WHERE mesavoto_id='" + mesavoto_id + "';";

    cc = database.submitQuery(sql1);
    name = database.submitQuery(sql2);

    System.out.println(cc);
    System.out.println(name);

    toClient.add(cc);
    toClient.add(name);

    return toClient;
  }

  /**
   * Adiciona uma mesa de voto a uma eleição
   * @param elecID Recebe o ID da eleição para onde se quer adicionar a mesa
   * @param idDep Recebe o ID do departamento onde vai estar a mesa
   * @return Em caso de sucesso, isto é, se o departamento nao tiver mais nenhuma mesa de voto para aquela eleição, retorna true, caso contrario retorna false
   * @throws RemoteException
   */

  public boolean addTableToElection(int elecID, int idDep) throws RemoteException{

    boolean toClient = true;
    ArrayList<String> protection;
    String protect = "SELECT * FROM MesaVoto WHERE departamento_id='" + idDep + "' AND eleicao_id='" + elecID + "';";
    protection = database.submitQuery(protect);

    if (protection.isEmpty()){
      String sql = "INSERT INTO MesaVoto (active,departamento_id,eleicao_id,numeroVotos) VALUES ('0','" + idDep + "','" + elecID + "',0);"; 
      database.submitUpdate(sql);
    }
    else{
      toClient = false;
    }

    return toClient;
  }

  /**
   * Remove uma mesa de voto de uma eleição
   * @param elecID Recebe o ID da eleição onde se quer remover a mesa
   * @param table Recebe o ID da mesa que se quer remover
   * @return Caso a mesa exista e possa ser removida retorna true, caso contrario retorna false
   * @throws RemoteException
   */

  public boolean removeTableFromElection(int elecID, int table) throws RemoteException{

    boolean toClient = true;
    ArrayList<String> protection;
    String protect = "SELECT MesaVoto WHERE='" + table + "' AND active = true;";
    protection = database.submitQuery(protect);
    if (protection.isEmpty()){
      String sql = "DELETE FROM MesaVoto WHERE ID='" + table + "';";
      database.submitUpdate(sql);
    }
    else{
      toClient = false;
    }

    return toClient;
  }

  /**
   * Autentica um utilizador
   * @param cc Recebe o numero de cartao de cidadao do eleitor
   * @param username Recebe o username do eleitor
   * @param password Recebe a password do eleitor
   * @return Caso o utilizador exista e as credenciais estejam corretas retorna true, caso contrario retorna false
   * @throws RemoteException
   */

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

  /**
   * Lista as mesas de voto associadas a uma eleição
   * @param eleicao_id Recebe o ID da eleição
   * @return Um ArrayList com o ID das mesas de voto associadas a eleiçao passada por parametro
   * @throws RemoteException
   */

  public ArrayList<ArrayList<String>> showTables(int eleicao_id) throws RemoteException{

    ArrayList<ArrayList<String>> toClient = new ArrayList<ArrayList<String>>();
    ArrayList<String> aux;
    String sql = "SELECT ID FROM MesaVoto WHERE eleicao_id='" + eleicao_id + "';";
    String sql1 = "SELECT departamento_id FROM MesaVoto WHERE eleicao_id='" + eleicao_id +"';";
    String sql2;

    aux = database.submitQuery(sql);
    if (aux.isEmpty()){
      return toClient = null;
    }
    toClient.add(aux);
    aux = database.submitQuery(sql1);
    sql2 = "SELECT nome FROM Departamento WHERE ID='" + aux.get(0) + "';";
    aux = database.submitQuery(sql2);
    if (aux.isEmpty()){
      return toClient = null;
    }
    toClient.add(aux);

    return toClient;
  }

  /**
   * Metodo que permite o voto
   * @param cc Recebe o numero de cartao de cidadao de um utilizador
   * @param lista Recebe a lista onde o utilizador quer votar
   * @param eleicao_id Recebe o id da eleição para onde o utilizador vai votar
   * @param mesavoto_id Recebe a mesa de voto onde o utilizador esta a votar
   * @return Em caso de sucesso retorna a lista em que o utilizador votou como uma string. Caso contrário retorna null.
   * @throws RemoteException
   */

  public String vote(int cc, String lista, int eleicao_id, int mesavoto_id) throws RemoteException{

    // FINNISH THISSSSSSSSSSSSSSSSSSSSSSSSSSSSSS 
    String toClient = null;
    ArrayList<String> aux1;
    ArrayList<String> aux2;
    ArrayList<String> aux3;
    String sql6 = "SELECT ID FROM Lista WHERE nome='" + lista + "' AND eleicao_id='" + eleicao_id +"';";
    aux3 = database.submitQuery(sql6);
    if (!aux3.isEmpty()){
      String sql1 = "UPDATE Lista SET votos = votos +1 WHERE nome='" + lista + "' AND eleicao_id='" + eleicao_id + "';";
      String sql2 = "SELECT ID FROM User WHERE numeroCC='" + cc + "';";

      aux1 = database.submitQuery(sql2);
      String sql3 = "SELECT hasVoted FROM User_Eleicao WHERE user_id='" + aux1.get(0) + "' AND eleicao_id='" +  eleicao_id + "';";
      aux2 = database.submitQuery(sql3);
      System.out.println(aux2);
      if (aux2.isEmpty()){
        String sql5 = "INSERT INTO User_Eleicao (user_id,eleicao_id,hasVoted,mesavoto_id,whenVoted) VALUES('" + aux1.get(0) + "','" + eleicao_id + "',true,'" + mesavoto_id + "',NOW());";
        String sql7 = "UPDATE MesaVoto SET numeroVotos=numeroVotos+1 WHERE ID='" +mesavoto_id+ "';";
        database.submitUpdate(sql7);
        database.submitUpdate(sql5);
        database.submitUpdate(sql1);
        toClient = lista;
      }
      else{
        toClient = null;
      }
    }
    else{
    }
    return toClient;
  }

  // Admin Console

  /**
   * Adiciona um utilizador
   * @param name Recebe o nome do utilizador
   * @param Address Recebe a morada do utilizador
   * @param phone Recebe o contacto do utilizador
   * @param ccn Recebe o numero do cartao de cidadão do utilizador
   * @param ccv Recebe a data de validade do cartao de cidadão do utilizador
   * @param dep Recebe o ID do departamento onde o utilizador estuda
   * @param fac Recebe o ID da faculdade onde o utilizador estudar
   * @param pass Reecebe a password do utilizador
   * @param type Recebe o tipo de utilizador que é. 1 - Estudante | 2 - Docente | 3 - Funcionário
   * @return Retorna true em caso de sucesso e false em caso de insucesso.
   * @throws RemoteException
   */

  public boolean addPerson(String name, String Address, int phone, int ccn, String ccv, int dep, int fac, String pass, int type) throws RemoteException{

    boolean toClient = true;

    String sql = "INSERT INTO User (name,hashed_password,contacto,morada,numeroCC,validadeCC,role,departamento_id,faculdade_id) VALUES ('" + name + "','" + pass +"','"+ phone+"','"+Address+"','"+ccn+"','"+ccv+"','"+type+"','"+dep+"','"+fac+"');";

    database.submitUpdate(sql);

    return toClient;
  }

  /**
   * Lista com os utilizadores que estão presentes numa mesa de voto
   * @param idTable Recebe o ID da mesa de voto
   * @return Retorna uma lista com os utilizadores presentes numa mesa de voto
   * @throws RemoteException
   */
  public ArrayList<String> tableMembers(int idTable) throws RemoteException{

    ArrayList<String> aux;
    String sql = "SELECT name FROM User WHERE mesavoto_id'" + idTable + "';";

    aux = database.submitQuery(sql);
    return aux;
  }

  /**
   * Mostra a mesa de voto onde um utilizador votou
   * @param idUser Recebe o ID do utilizador
   * @param idElec Recebe o ID da eleicao
   * @return Retorna o ID da mesa de voto onde o utilizador votou
   * @throws RemoteException
   */
  public int checkTable(int idUser, int idElec) throws RemoteException{

    int mesa;
    ArrayList<String> aux,id;
    //TODO: Get ID from CC

    String user_id = "SELECT ID FROM User WHERE numeroCC='" + idUser +  "';";
    id = database.submitQuery(user_id);
    String sql = "SELECT mesavoto_id FROM User_Eleicao WHERE user_id='" + id.get(0) + "' AND eleicao_id='" + idElec + "'";
    aux = database.submitQuery(sql);
    if (aux.isEmpty()){
      mesa = -1;
    }
    else {
      mesa = Integer.parseInt(aux.get(0)); 
    }

    return mesa;

  }

  /**
   * Mostra as eleiçoes que estão neste momento a decorrer
   * @return Retorna uma ArrayList de ArrayLists de strings em que o primeiro indice contem os IDs das eleiçoes, o segundo o titulo da eleição, o terceiro a data de inicio da eleição e em quarto a data de fim da eleição
   * @throws RemoteException
   */

  public ArrayList<ArrayList<String>> viewCurrentElections() throws RemoteException{

    ArrayList<ArrayList<String>> container = new ArrayList<>();
    ArrayList<String> ID;
    ArrayList<String> titulos;
    ArrayList<String> dateInicio;
    ArrayList<String> dateFim;

    String sql1 = "SELECT ID FROM Eleicao WHERE active = true OR inicio >= NOW();";
    ID = database.submitQuery(sql1);
    sql1 = "SELECT titulo FROM Eleicao WHERE active = true OR inicio >= NOW();";
    titulos = database.submitQuery(sql1);
    sql1 = "SELECT inicio FROM Eleicao WHERE active = true OR inicio >= NOW();";
    dateInicio = database.submitQuery(sql1);
    sql1 = "SELECT fim FROM Eleicao WHERE active = true OR inicio >= NOW();";
    dateFim = database.submitQuery(sql1);

    container.add(ID);
    container.add(titulos);
    container.add(dateInicio);
    container.add(dateFim);

    return container;
  }

  /**
   * Mostra eleições passadas ou a decorrer
   * @return  Retorna uma ArrayList de ArrayLists de strings em que o primeiro indice contem os IDs das eleiçoes, o segundo o titulo da eleição, o terceiro a data de inicio da eleição e em quarto a data de fim da eleição
   * @throws RemoteException
   */

  public ArrayList<ArrayList<String>> viewPastCurrentElections() throws RemoteException{

    ArrayList<ArrayList<String>> container = new ArrayList<>();
    ArrayList<String> ID;
    ArrayList<String> titulos;
    ArrayList<String> dateInicio;
    ArrayList<String> dateFim;

    String sql1 = "SELECT ID FROM Eleicao WHERE active = true OR inicio < NOW();";
    ID = database.submitQuery(sql1);
    sql1 = "SELECT titulo FROM Eleicao WHERE active = true OR inicio < NOW();";
    titulos = database.submitQuery(sql1);
    sql1 = "SELECT inicio FROM Eleicao WHERE active = true OR inicio < NOW();";
    dateInicio = database.submitQuery(sql1);
    sql1 = "SELECT fim FROM Eleicao WHERE active = true OR inicio < NOW();";
    dateFim = database.submitQuery(sql1);

    container.add(ID);
    container.add(titulos);
    container.add(dateInicio);
    container.add(dateFim);

    return container;
  }

  /**
   * Mostra eleições futura
   * @return  Retorna uma ArrayList de ArrayLists de strings em que o primeiro indice contem os IDs das eleiçoes, o segundo o titulo da eleição, o terceiro a data de inicio da eleição e em quarto a data de fim da eleição
   * @throws RemoteException
   */

  public ArrayList<ArrayList<String>> viewFutureElections() throws RemoteException{

    ArrayList<ArrayList<String>> container = new ArrayList<>();
    ArrayList<String> ID;
    ArrayList<String> titulos;
    ArrayList<String> dateInicio;
    ArrayList<String> dateFim;

    String sql1 = "SELECT ID FROM Eleicao WHERE inicio > NOW() AND fim > NOW();";
    ID = database.submitQuery(sql1);
    sql1 = "SELECT titulo FROM Eleicao WHERE inicio > NOW() AND fim > NOW();";
    titulos = database.submitQuery(sql1);
    sql1 = "SELECT inicio FROM Eleicao WHERE inicio > NOW() AND fim > NOW();";
    dateInicio = database.submitQuery(sql1);
    sql1 = "SELECT fim FROM Eleicao WHERE inicio > NOW() AND fim > NOW();";
    dateFim = database.submitQuery(sql1);

    container.add(ID);
    container.add(titulos);
    container.add(dateInicio);
    container.add(dateFim);

    return container;
  }

  /**
   * Mostra eleições passadas
   * @return  Retorna uma ArrayList de ArrayLists de strings em que o primeiro indice contem os IDs das eleiçoes, o segundo o titulo da eleição, o terceiro a data de inicio da eleição e em quarto a data de fim da eleição
   * @throws RemoteException
   */

  public ArrayList<ArrayList<String>> viewPastElections() throws RemoteException{

    ArrayList<ArrayList<String>> container = new ArrayList<>();
    ArrayList<String> ID;
    ArrayList<String> titulos;
    ArrayList<String> dateInicio;
    ArrayList<String> dateFim;

    String sql1 = "SELECT ID FROM Eleicao WHERE inicio < NOW();";
    ID = database.submitQuery(sql1);
    sql1 = "SELECT titulo FROM Eleicao WHERE inicio < NOW();";
    titulos = database.submitQuery(sql1);
    sql1 = "SELECT inicio FROM Eleicao WHERE inicio < NOW();";
    dateInicio = database.submitQuery(sql1);
    sql1 = "SELECT fim FROM Eleicao WHERE inicio < NOW();";
    dateFim = database.submitQuery(sql1);

    container.add(ID);
    container.add(titulos);
    container.add(dateInicio);
    container.add(dateFim);

    return container;
  }

  /**
   * Mostra os Departamentos que estão na base de dados
   * @return Retorna uma ArrayList de ArrayLists de Strings em que o primeiro indice contem o nome do departamento e o segundo contem o id
   * @throws RemoteException
   */

  public ArrayList<ArrayList<String>> verDepartamentos() throws RemoteException{

    ArrayList<ArrayList<String>> toClient = new ArrayList<ArrayList<String>>();
    ArrayList<String> ID;
    ArrayList<String> Nomes;
    String sql1 = "SELECT nome FROM Departamento;";
    String sql2 = "SELECT ID FROM Departamento;";
    ID = database.submitQuery(sql2);
    Nomes = database.submitQuery(sql1);

    toClient.add(ID);
    toClient.add(Nomes);
    return toClient;
  }

  /**
   * Mostra as Faculdades que estão na base de dados
   * @return Retorna uma ArrayList de ArrayLists de Strings em que o primeiro indice contem o nome do departamento e o segundo contem o id
   * @throws RemoteException
   */

  public ArrayList<ArrayList<String>> verFaculdades() throws RemoteException{

    ArrayList<ArrayList<String>> toClient = new ArrayList<ArrayList<String>>();
    ArrayList<String> ID;
    ArrayList<String> Nomes;
    String sql1 = "SELECT nome FROM Faculdade;";
    String sql2 = "SELECT ID FROM Faculdade;";
    ID = database.submitQuery(sql2);
    Nomes = database.submitQuery(sql1);

    toClient.add(ID);
    toClient.add(Nomes);
    return toClient;
  }

  /**
   * Remove um departamento ou uma faculdade
   * @param dep Recebe o ID do departamento ou da faculdade
   * @param flag Recebe uma flag para saber se deve remover o departamento ou a faculdade, se a flag for 1 remove o departamento, se for 2 remove a faculdade
   * @return Retorna true em caso de sucesso da remoçao e false em caso de insucesso
   * @throws RemoteException
   */

  public boolean rmDepFac(int dep, int flag) throws RemoteException{

    boolean toClient = true;
    String sql="";

    if (flag == 1){
      sql = "DELETE FROM Departamento WHERE ID='" + dep + "';";
      database.submitUpdate(sql);
    }
    else if(flag ==2){
      sql = "DELETE FROM Faculdade WHERE ID='" + dep + "';";
      database.submitUpdate(sql);
    }

    return toClient;
  }

  /**
   * Mostra todas as listas associadas a uma eleição
   * @param id Recebe o ID da eleição
   * @return Retorna uma ArrayList com os nomes das listas associadas à eleição
   * @throws RemoteException
   */

  public ArrayList<String> viewListsFromElection(int id) throws RemoteException{

    ArrayList<String> toClient;
    String sql = "SELECT nome FROM Lista WHERE eleicao_id='" + id + "';";

    toClient = database.submitQuery(sql);
    return toClient;
  }

  /**
   * Mostra as listas associadas a uma eleição que não são a lista em branco e a lista null
   * @param id Recebe o ID da eleição
   * @return Retorna uma ArrayList com os nomes das listas da eleição passada por parametro
   * @throws RemoteException
   */

  public ArrayList<String> printListsFromElection(int id) throws RemoteException{

    ArrayList<String> toClient;
    String sql = "SELECT nome FROM Lista WHERE eleicao_id='" + id + "' AND tipo!=0;";

    toClient = database.submitQuery(sql);
    return toClient;
  }

  /**
   * Adiciona uma lista ou remove uma lista de um eleição
   * @param idElec Recebe o ID da eleiçao a qual pretendemos adicionar a lista
   * @param listType Recebe o tipo de lista a criar. 1 - Estudantes | 2 - Docentes | 3 - Funcionários
   * @param List Recebe o nome a dar à lista a ser criada, ou o nome da lista a ser removida
   * @param flag Recebe a flag que determina se vamos remover ou adicionar uma lista. Se a flag for 1 adiciona uma lista, se for 2 remove uma lista
   * @return Retorna true no caso da ação ser bem sucedida ou false no caso de ter falhado
   * @throws RemoteException
   */

  public boolean manageList(int idElec,int listType, String List, int flag) throws RemoteException{
    //flag 1 - add list, flag 2 - remove list
    boolean toClient = true;
    ArrayList<String> aux;
    String sql;

    if (flag == 1){
      sql = "INSERT INTO Lista (nome,tipo,eleicao_id) VALUES ('" + List + "','" + listType + "','"+ idElec + "');"; 
      database.submitUpdate(sql);
    }
    else if(flag ==2){

      sql = "DELETE FROM Lista WHERE eleicao_id='" + idElec + "' AND nome='" +List + "';";
      database.submitUpdate(sql);
    }
    return toClient;
  }

  /**
   * Adiciona departamento ou faculdade
   * @param faculdade_id Recebe o ID da faculdade caso seja para adicionar um departamento
   * @param newName Recebe o nome da infraestrutura a ser criada
   * @param flag Recebe a flag que distingue a criação de um departamento de uma faculdade. Se a flag for 1 cria um departamento se for 2 cria uma faculdade
   * @return Retorna true em caso de sucesso ou false em caso de insucesso
   * @throws RemoteException
   */

  public boolean addDepFac(int faculdade_id, String newName, int flag) throws RemoteException{
    //add departamento / faculdade. flag 1 - dep, flag 2 - fac 
    boolean toClient = true;
    String sql;

    if(flag==1){
      sql = "INSERT INTO Departamento (nome,faculdade_id) VALUES ('" + newName + "','" + faculdade_id+ "');";
      database.submitUpdate(sql);
    }
    else if (flag==2){
      sql = "INSERT INTO Faculdade (nome) VALUES ('"+newName+"');";
      database.submitUpdate(sql);
    }
    return toClient;
  }

  /**
   * Função que altera um utilizador que esta numa mesa
   * @param idTable Recebe o ID da mesa de voto
   * @param idUser Recebe o ID do utilizador que esta na mesa atualmente
   * @param idNewUser Recebe o ID do utilizador que vai substituir
   * @return Retorna true em caso de sucesso, e false em caso de insucesso
   * @throws RemoteException
   */

  public boolean manageTable(int idTable, int idUser, int idNewUser) throws RemoteException{

    //mudar a pessoa que está na mesa 

    boolean toClient = true;
    String sql = "UPDATE User SET mesavoto_id = NULL WHERE mesavoto_id='" + idTable + "AND ID='" + idUser +"';";
    database.submitUpdate(sql);
    sql = "UPDATE User SET mesavoto_id='" + idTable + "' WHERE ID='" + idNewUser + "';";
    database.submitUpdate(sql);

    return toClient;
  }

  /**
   * Funçao que permite editar as caracteristicas de um utilizador
   * @param idUser Recebe o ID da pessoa a editar
   * @param newInfo Recebe a informação a editar como String
   * @param flag Recebe uma flag para saber a caracteristica a alterar. 1 - nome | 2 - morada | 3 - contacto | 4 - numero do cartao de cidadao | 5 - validade do cartao de cidadao | 6 - departamento onde estuda | 7 - faculdade onde estuda
   * @return Retorna true em caso de sucesso e false em caso de insucesso
   * @throws RemoteException
   */

  public boolean editPerson(int idUser, String newInfo, int flag) throws RemoteException{
    //edita a info de uma pessoa, manda a newinfo sempre como string e depois cabe ao server passar de string para int caso seja necessario. flag 1 - name, flag 2 - Address, flag 3 - phone, flag 4 - ccn, flag 5 - ccv, flag 6 - dep, flag 7 - pass

    int aux;
    String sql="";

    switch (flag){
      case 1:
        sql = "UPDATE User SET name='" + newInfo + "' WHERE numeroCC='" + idUser + "';";
        break;
      case 2:
        sql = "UPDATE User SET morada='" + newInfo + "' WHERE numeroCC='" + idUser + "';";
        break;
      case 3:
        aux = Integer.parseInt(newInfo);
        sql = "UPDATE User SET contacto='" + newInfo + "' WHERE numeroCC='" + idUser + "';";
        break;
      case 4:
        aux = Integer.parseInt(newInfo);
        sql = "UPDATE User SET numeroCC='" + aux + "' WHERE numeroCC='" + idUser + "';";
        break;
      case 5:
        sql = "UPDATE User SET validadeCC='" + newInfo + "' WHERE numeroCC='" + idUser + "';";
        break;
      case 6:
        aux = Integer.parseInt(newInfo);
        sql = "UPDATE User SET departamento_id='" + aux + "' WHERE numeroCC='" + idUser + "';";
        break;
      case 7:
        aux = Integer.parseInt(newInfo);
        sql = "UPDATE User SET faculdade_id='" + aux + "' WHERE numeroCC='" + idUser + "';";
        break;
      default:
        break;

    }
    if (!sql.equals("")){
      database.submitUpdate(sql); 
    }
    return true;
  }

  /**
   * Função de voto antecipado
   * @param idElec Recebe o ID das eleiçoes onde votar
   * @param cc Recebe o numero do cartao de cidado que vai votar
   * @param vote Recebe a lista onde o utilizador vai votar
   * @param pass Recebe a password do utilizador que vai votar
   * @return Retorna true em caso de sucesso e false em caso de insucesso
   * @throws RemoteException
   */

  public boolean anticipatedVote(int idElec, int cc, String vote, String pass) throws RemoteException{
    //vote antecipado. o int vote é um int da lista de listas disponiveis retornada pela "viewListsFromElection"
    boolean toClient = false;
    ArrayList<String> aux1;
    ArrayList<String> aux2;
    ArrayList<String> aux3;
    ArrayList<String> idUser;
    String sql6 = "SELECT ID FROM Lista WHERE nome='" + vote + "' AND eleicao_id='" + idElec +"';";
    String sql7 = "SELECT ID FROM User WHERE numeroCC='" + cc + "';";
    idUser = database.submitQuery(sql7);
    aux3 = database.submitQuery(sql6);
    if (!aux3.isEmpty()){
      String sql1 = "UPDATE Lista SET votos = votos +1 WHERE nome='" + vote + "' AND eleicao_id='" + idElec + "';";

      String sql3 = "SELECT hasVoted FROM User_Eleicao WHERE user_id='" + idUser.get(0) + "' AND eleicao_id='" +  idElec + "';";
      aux2 = database.submitQuery(sql3);
      System.out.println(aux2);
      if (aux2.isEmpty()){
        String sql5 = "INSERT INTO User_Eleicao (user_id,eleicao_id,hasVoted,mesavoto_id,whenVoted) VALUES('" + idUser.get(0) + "'," + idElec + ",true,null,NOW());";
        database.submitUpdate(sql5);
        database.submitUpdate(sql1);
        toClient = true;
      }
      else{
        toClient = false;
      }
    }
    else{
      toClient = false;
    }
    return toClient;

  }

  /**
   * Funçao para criar uma lista
   * @param nome Recebe o nome da lista a criar
   * @param tipo Recebe o tipo da lista a criar. 1 - Estudantes | 2 - Docentes | 3 - Funcionarios
   * @param eleicao_id Recebe o ID da eleição onde a lista vai estar englobada
   * @return Retorna true em caso de sucesso e false em caso de insucesso
   * @throws RemoteException
   */

  public boolean createList(String nome, int tipo, int eleicao_id) throws RemoteException{

    String sql = "INSERT INTO Lista (nome,tipo,votos,eleicao_id) VALUES('"+ nome +"','"+ tipo + "','" + "',0,'" + eleicao_id + "');";
    database.submitUpdate(sql);

    return true;
  }

  /**
   * Funçao para verificar os resultados de uma eleição
   * @param idElec Recebe o ID da eleiçao de onde se quer saber os resultados
   * @return Retorna uma ArrayList de ArrayLists de Strings em que o primeiro indice tem o nome de cada lista e o segundo indice tem o numero de votos de cada lista
   * @throws RemoteException
   */

  public ArrayList<ArrayList<String>> checkResults(int idElec) throws RemoteException{
    //recebe uma lista com [[lista,nº de votos],...]. return [[null,null]] em caso de insucesso
    ArrayList<ArrayList<String>> toClient = new ArrayList<ArrayList<String>>();
    ArrayList<String> listaNome;
    ArrayList<String> listaVotos;

    String sqlNome = "SELECT nome FROM Lista WHERE eleicao_id='" + idElec +"' ORDER BY votos DESC;";
    String sqlVotos = "SELECT votos FROM Lista WHERE eleicao_id'" + idElec +"' ORDER BY votos DESC;";

    listaNome = database.submitQuery(sqlNome);
    listaVotos = database.submitQuery(sqlVotos);

    toClient.add(listaNome);
    toClient.add(listaVotos);

    return toClient;
  }

  /**
   * Função usada para imprimir informação em tempo real sobre as mesas de voto
   * @param idTable Recebe o ID da mesa da qual queremos informação
   * @param idElec Recebe o ID da eleição à qual essa lista pertence
   * @return Retorna o numero de votos da lista pedida
   * @throws RemoteException
   */

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

  /**
   * Metodo usado para saber a hora a que um eleitor votou numa determinada eleição
   * @param idUser Recebe o ID do eleitor
   * @param idElec Recebe o ID da eleição
   * @return Retorna a data a que o utilizador votou
   * @throws RemoteException
   */

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

  /**
   * Mudar a data de uma eleição
   * @param id Recebe o ID da mesa de voto onde esta a eleição
   * @param newdate Recebe a nova data da eleição
   * @param flag Recebe uma flag para definir se a data a mudar é de inicio ou fim. 1 - Inicio | 2 - Fim
   * @return Em caso de sucesso retorna true, em caso de insucesso retorna false
   * @throws RemoteException
   */

  public boolean changeElectionsDates(int id, String newdate, int flag) throws RemoteException{

    boolean toClient = true;
    ArrayList<String> aux;
    String sql = "SELECT * FROM MesaVoto WHERE ID='" + id + "' AND active='False';";
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
      database.submitUpdate(sql);
    } 

    return toClient;
  }

  /**
   * Metodo para mudar o titulo ou a descrição de uma eleiçao
   * @param id Recebe o ID da eleiçao
   * @param text Recebe o texto a alterar
   * @param flag Recebe a flag que distingue a alteração do titulo ou da descrição
   * @return Em caso de sucesso retorna true, caso contrario retorna false
   * @throws RemoteException
   */

  public boolean changeElectionsText(int id, String text, int flag) throws RemoteException{

    boolean toClient = true;
    ArrayList<String> aux;
    String sql = "SELECT * FROM Eleicao WHERE ID='" + id + "' AND active='0';";
    aux = database.submitQuery(sql);
    if (aux.isEmpty()){
      toClient = false;
    }
    else{
      if (flag == 1){
        sql = "UPDATE Eleicao  SET titulo='" + text + "' WHERE ID='" + id + "';";
      }
      else if (flag == 2){
        sql = "UPDATE Eleicao  SET descricao='" + text + "' WHERE ID='" + id + "';";
      }
      database.submitUpdate(sql);
    } 

    return toClient;
  }

  /**
   * Editar um departamento / faculdade
   * @param dep Recebe o ID do departamento / faculdade
   * @param newName Recebe o novo nome do departamento / faculdade
   * @param flag Recebe a flag que identifica se é um departamento ou faculdade a alterar
   * @return Retorna true em caso de sucesso, caso contrario retorna falso
   * @throws RemoteException
   */

  public boolean editDepFac(int dep, String newName, int flag) throws RemoteException{

    //edita nome de departamento / faculdade. flag 1 - dep, flag 2 - fac
    String sql1;

    if (flag == 1){
      sql1 = "UPDATE Departamento SET nome='" + newName + "' WHERE ID='" + dep + "';";
      database.submitUpdate(sql1);
    }
    else if (flag == 2){
      sql1 = "UPDATE Faculdade SET nome='" + newName + "' WHERE ID='" + dep + "';";
      database.submitUpdate(sql1);
    }

    return true;
  }

  /**
   * Cria uma eleição para um Nucleo de Estudantes
   * @param beginning Recebe a data de inicio como String
   * @param end Recebe a data de fim como String
   * @param title Recebe o titulo da eleiçao como uma String
   * @param description Recebe a descrição da eleição como uma String
   * @param dep Recebe o ID do departamento ha qual esta associada a eleiçao de Nucleo de Estudantes
   * @return Retorna true em caso de sucesso e false em caso de insucesso
   * @throws RemoteException
   */

  public boolean criaEleiçãoNE(String beginning, String end, String title, String description, int dep) throws RemoteException{

    //cria eleição Nucleo de estudantes. . As eleições para núcleo de estudantes decorrem num único departamento e podem votar apenas os estudantes desse departamento.
    int eleicao_id;
    ArrayList<String> needed;
    String sql1 = "INSERT INTO Eleicao (titulo,descricao,inicio,fim,tipo,active) VALUES ('" + title + "','" + description + "','" + beginning + "','" + end + "',1,false);";
    database.submitUpdate(sql1);

    String aux = "SELECT LAST_INSERT_ID();";
    needed = database.submitQuery(aux);
    eleicao_id = Integer.parseInt(needed.get(0));

    String sql2 = "INSERT INTO Departamento_Eleicao (departamento_id, eleicao_id) VALUES ('" + dep + "','" + eleicao_id + "');";
    database.submitUpdate(sql2);

    return true;

  }

  /**
   * Cria uma eleição para o Conselho Geral
   * @param beginning Recebe a data de inicio como String
   * @param end Recebe a data de fim como String
   * @param title Recebe o titulo da eleição como String
   * @param description Recebe a descrição da eleição como String
   * @return Retorna true em caso de sucesso e false em caso de insucesso
   * @throws RemoteException
   */

  public boolean criaEleiçãoCG(String beginning, String end, String title, String description) throws RemoteException{

    // os estudantes votam apenas nas listas de estudantes, os docentes votam apenas nas listas de docentes, e os funcionários votam apenas nas listas de funcionários.
    String sql1;
    sql1 = "INSERT INTO Eleicao (titulo,descricao,inicio,fim,tipo,active) VALUES ('" + title + "','" + description + "','" + beginning + "','" + end + "',2,false);";
    database.submitUpdate(sql1);
    sql1 = "INSERT INTO Eleicao (titulo,descricao,inicio,fim,tipo,active) VALUES ('" + title + "','" + description + "','" + beginning + "','" + end + "',3,false);";
    database.submitUpdate(sql1);
    sql1 = "INSERT INTO Eleicao (titulo,descricao,inicio,fim,tipo,active) VALUES ('" + title + "','" + description + "','" + beginning + "','" + end + "',4,false);";
    database.submitUpdate(sql1);

    return true;

  }

  /**
   * Cria uma eleição para a Direção de uma Faculdade
   * @param beginning Recebe a data de inicio como String
   * @param end Recebe a data de fim como String
   * @param title Recebe o titulo da eleição como String
   * @param description Recebe a descrição da eleição como String
   * @param idFac Recebe o ID da faculdade onde se vai decorrer a eleição
   * @return Em caso de sucesso retorna true caso contrario retorna false
   * @throws RemoteException
   */

  public boolean criaEleiçãoDF(String beginning, String end, String title, String description, int idFac) throws RemoteException{

    //cria eleição para a direção da faculdade
    int eleicao_id;
    ArrayList<String> needed;
    String sql1 = "INSERT INTO Eleicao (titulo,descricao,inicio,fim,tipo,active) VALUES ('" + title + "','" + description + "','" + beginning + "','" + end + "',5,false);";
    database.submitUpdate(sql1);
    String aux = "SELECT LAST_INSERT_ID();";
    needed = database.submitQuery(aux);
    eleicao_id = Integer.parseInt(needed.get(0));

    String sql2 = "INSERT INTO Faculdade_Eleicao (faculdade_id, eleicao_id) VALUES ('" + idFac + "','" + eleicao_id + "');";
    database.submitUpdate(sql2);

    return true;
  }

  /**
   * Cria uma eleição para a Direção de um Departamento
   * @param beginning Recebe a data de inicio como String
   * @param end Recebe a data de fim como String
   * @param title Recebe o titulo da eleição como String
   * @param description Recebe a descrição da eleição como String
   * @param idDep Recebe o ID do Departamento onde vai decorrer a eleição
   * @return Em caso de sucesso retorna true caso contrario retorna false
   * @throws RemoteException
   */

  public boolean criaEleiçãoDD(String beginning, String end, String title, String description, int idDep) throws RemoteException{

    //cria eleição para a direção do departamento, concorrem e votam docentes desse departamento
    int eleicao_id;
    ArrayList<String> needed;
    String sql1 = "INSERT INTO Eleicao (titulo,descricao,inicio,fim,tipo,active) VALUES ('" + title + "','" + description + "','" + beginning + "','" + end + "',6,false);";
    database.submitUpdate(sql1);
    String aux = "SELECT LAST_INSERT_ID();";
    needed = database.submitQuery(aux);
    eleicao_id = Integer.parseInt(needed.get(0));

    String sql2 = "INSERT INTO Departamento_Eleicao (departamento_id, eleicao_id) VALUES ('" + idDep + "','" + eleicao_id + "');";
    database.submitUpdate(sql2);

    return true;
  }

  /**
   * Adiciona uma mesa de voto a uma eleição
   * @param idTable Recebe o ID da mesa de voto
   * @param numeroCC Recebe o numero do cartao de cidadao do utilizador que vai estar na mesa de voto
   * @return Em caso de sucesso retorna true, caso contrario retorna false
   * @throws RemoteException
   */

  public boolean addToTable(int idTable, int numeroCC) throws RemoteException{

    String aux = "SELECT ID FROM User WHERE numeroCC='" + numeroCC + "';";
    ArrayList<String> user_id = database.submitQuery(aux);
    if (user_id.isEmpty()){
      return false;
    }
    String sql = "UPDATE User SET mesavoto_id='" + idTable + "' WHERE ID='" + user_id.get(0) + "';";
    database.submitUpdate(sql);

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
                System.out.println("Backup server isn't responding");
                i++;
              } catch (IOException ioe){
                System.out.println("Networking Problems");
              } catch (InterruptedException ie){
                  RMIServer.this.heartbeat = null;

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
                System.out.println("Main RMI Servir not responding");
                i++;
              } catch (IOException ioe){
                System.out.println("Network Problems");
              } catch (InterruptedException ie){

              } catch (Exception e){
                e.printStackTrace();
              }

            }while(i < retries);

            System.out.println("RMIServer failed \nAssuming Main Server Status");

            try{
              aSocket.close();
              RMIServer.this.heartbeat = null;
              RMIServer.this.mainServer = true;
              RMIServer.this.startRMIServer();
              Thread.currentThread().join();
            } catch(InterruptedException ie){
              System.out.println("Thread Interrupted");
            } catch(Exception e){
              e.printStackTrace();
            }

          }

        }

      }

  }
}
