/*
 * @created by FranciscoJRSantos at 09/10/2017
 * 
 **/

import java.io.IOException;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RMIServer extends UnicastRemoteObject implements ServerInterface {

    private static final long serialVersionUID = 1L;
    private static DatabaseConnection database = null;
    private static String rmiName;
    private static int rmiPort; // 1099
    private UDPConnection heartbeat = null;
    private boolean mainServer = true;

    private RMIServer() throws RemoteException {

        RMIConfigLoader newConfig = new RMIConfigLoader();
        RMIServer.rmiName = newConfig.getRMIName();
        RMIServer.rmiPort = newConfig.getRMIPort();
        String dbIP = newConfig.getDBIP();
        int dbPort = newConfig.getDBPort();
        database = new DatabaseConnection(dbIP, dbPort);
        startRMIServer();
    }

    public static void main(String args[]) throws RemoteException {

        System.getProperties().put("java.security.policy", "policy.all");
        System.setSecurityManager(new SecurityManager());
        new RMIServer();

    }

    private void startRMIServer() {

        try {

            Registry r = LocateRegistry.createRegistry(rmiPort);
            Naming.rebind(rmiName, this);

            System.out.println("Main RMIServer Started");
            if (this.heartbeat == null) {
                this.startUDPConnection();
            }
        } catch (ExportException ee) {

            this.setMainServer(false);
            System.out.println(this.mainServer);
            if (this.heartbeat == null) {
                this.startUDPConnection();
            }
            System.out.println("Backup RMIServer Starting");
        } catch (RemoteException re) {
            System.out.println("RemoteException: " + re);
        } catch (MalformedURLException murle) {
            System.out.println("MalformedURLException: " + murle);
        }
    }

    private void setMainServer(boolean n) {
        this.mainServer = n;
    }

    private void startUDPConnection() {
        this.heartbeat = new UDPConnection(mainServer);
    }
  
    // Create

    public boolean createUser(int numero_cc, String nome, String password_hashed, String morada, int contacto, String validade_cc, int tipo, String un_org_nome) throws RemoteException {

        boolean answer = true;
        if ((tipo >= 0) && (tipo <= 3)){
            String proc_call = "CALL createUtilizador(@" + numero_cc + ",@" + nome + ",@" + password_hashed + ",@" + morada + ",@" + contacto + ",@" + validade_cc + ",@" + tipo + ",@" + un_org_nome + ");";
            database.submitQuery(proc_call);
        }
        else answer = false;

        return answer;
    }

    public boolean createUnidadeOrganica(String nome, String pertence) throws RemoteException{

        boolean answer = true;
        ArrayList protection;
        String sql1,sql2;

        sql1 = "SELECT * FROM unidade_organica WHERE nome='" + nome + "';";
        protection = database.submitQuery(sql1);
        if (protection.isEmpty()){
            sql2 = "INSERT INTO unidade_organica (nome,pertence) VALUES('" + nome + "','" + pertence + "');";
            database.submitUpdate(sql2);

        }
        else answer = false;
        return answer;
    }

    public boolean createEleicao(String titulo, String inicio, String fim, String descricao, int tipo, String un_org_nome) throws RemoteException{
        boolean answer = true;
        if ((tipo >= 0) && (tipo < 3)){
            String proc_call = "CALL createEleicao(@" + titulo + ",@" + inicio + ",@" + fim + ",@" + descricao + ",@" + tipo + ",@" + un_org_nome + ");";
            database.submitQuery(proc_call);
        }
        else answer = false;

        return answer;
    }

    public boolean createLista(String nome, int tipo, int eleicao_id, int numero_cc) throws RemoteException{

        boolean answer = true;
        String protection = "SELECT * FROM eleicao WHERE eleicao_id=" + eleicao_id + " AND inicio>NOW() AND fim > NOW();";
        ArrayList security = database.submitQuery(protection);

        if (security.isEmpty()){
            if ((tipo >= 0) && (tipo < 3)){
                String proc_call = "CALL createEleicao(@" + nome + ",@" + tipo + ",@" + eleicao_id + ",@" + numero_cc + ");";
                database.submitQuery(proc_call);
            }
            else answer = false;
        }
        else answer=false;

        return answer;

    }

    public boolean createMesaVoto(String un_org_nome, int eleicao_id, int numero_cc) throws RemoteException{

        boolean answer = true;
        ArrayList check;
        String protection = "SELECT COUNT(*) FROM mesa_voto WHERE unidade_organica_nome LIKE '" + un_org_nome + "' AND eleicao_id='" + eleicao_id + "';";
        check = database.submitQuery(protection);

        if ((Integer) check.get(0) > 1){
            answer = false;
        }
        else{
            String proc_call = "CALL createMesaVoto(@" + un_org_nome + ",@" + eleicao_id + ",@" + numero_cc + ");";
            database.submitQuery(proc_call);
        }
        return answer;
    }

    // Read

    public ArrayList<String> showUtilizador(int numero_cc) throws RemoteException{

        ArrayList<String> utilizador;
        String sql = "SELECT * FROM utilizador WHERE numero_cc LIKE " + numero_cc + ";";
        utilizador = database.submitQuery(sql);

        return utilizador;

    }

    public ArrayList<String> showUO(String nome) throws RemoteException{

        ArrayList<String> unidade_organica;
        String sql = "SELECT * FROM unidade_organica WHERE nome LIKE " + nome + ";";
        unidade_organica = database.submitQuery(sql);

        return unidade_organica;
    }

    public ArrayList<String> showAllUO() throws RemoteException{

      ArrayList<String> unidades_organicas;
      String sql_un_orgs = "SELECT nome FROM unidade_organica";

      unidades_organicas = database.submitQuery(sql_un_orgs);

      return unidades_organicas;

    }

    public ArrayList<String> showEleicao(int id) throws RemoteException{

        ArrayList<String> eleicao;
        String sql = "SELECT * FROM eleicao WHERE id='" + id + "';";
        eleicao = database.submitQuery(sql);

        return eleicao;
    }

    public ArrayList<ArrayList<String>> showResultadosFromEleicao(int eleicao_id) throws RemoteException{ 
    // recebe uma lista com [[lista,nº de votos],...]. return [[null,null]] em caso de insucesso
      ArrayList<ArrayList<String>> resultados = new ArrayList<ArrayList<String>>();
      ArrayList<String> nome_lista;
      ArrayList<String> votos_lista;

      String sqlNome = "SELECT nome FROM lista WHERE eleicao_id='" + eleicao_id +"' ORDER BY votos DESC;";
      String sqlVotos = "SELECT votos FROM lista WHERE eleicao_id'" + eleicao_id +"' ORDER BY votos DESC;";

      nome_lista = database.submitQuery(sqlNome);
      votos_lista = database.submitQuery(sqlVotos);

      resultados.add(nome_lista);
      resultados.add(votos_lista);

      return resultados;
    }

    public ArrayList<ArrayList<String>> showEleicoesDecorrer() throws RemoteException{

        ArrayList<ArrayList<String>> eleicoes = new ArrayList<ArrayList<String>>();
        ArrayList<String> id;
        ArrayList<String> descricao;
        ArrayList<String> local;
        String sql_id = "SELECT id FROM eleicao WHERE inicio > NOW() AND fim > NOW();";
        String sql_descricao = "SELECT descricao FROM eleicao WHERE inicio> NOW() AND fim > NOW();";
        String sql_local = "SELECT unidade_organica_nome FROM eleicao AS e, unidade_organica_eleicao uoe WHERE e.inicio> NOW() AND e.fim> NOW() AND e.id = uoe.eleicao_id;";
        id = database.submitQuery(sql_id);
        descricao = database.submitQuery(sql_descricao);
        local = database.submitQuery(sql_local);

        eleicoes.add(id);
        eleicoes.add(descricao);
        eleicoes.add(local);

        return eleicoes;
    }

    public ArrayList<ArrayList<String>> showEleicoesPassadas() throws RemoteException{

        ArrayList<ArrayList<String>> eleicoes = new ArrayList<ArrayList<String>>();
        ArrayList<String> id;
        ArrayList<String> descricao;
        ArrayList<String> local;
        String sql_id = "SELECT id FROM eleicao WHERE inicio < NOW();";
        String sql_descricao = "SELECT descricao FROM eleicao WHERE inicio < NOW();";
        String sql_local = "SELECT unidade_organica_nome FROM eleicao AS e, unidade_organica_eleicao uoe WHERE e.inicio < NOW() AND e.id = uoe.eleicao_id;";
        id = database.submitQuery(sql_id);
        descricao = database.submitQuery(sql_descricao);
        local = database.submitQuery(sql_local);

        eleicoes.add(id);
        eleicoes.add(descricao);
        eleicoes.add(local);

        return eleicoes;
    }

    public ArrayList<ArrayList<String>> showEleicoesFuturas() throws RemoteException{

        ArrayList<ArrayList<String>> eleicoes = new ArrayList<ArrayList<String>>();
        ArrayList<String> id;
        ArrayList<String> descricao;
        ArrayList<String> local;
        String sql_id = "SELECT id FROM eleicao WHERE inicio < NOW() AND fim > NOW();";
        String sql_descricao = "SELECT descricao FROM eleicao WHERE inicio < NOW() AND fim > NOW();";
        String sql_local = "SELECT unidade_organica_nome FROM eleicao AS e, unidade_organica_eleicao uoe WHERE e.inicio < NOW() AND e.fim() > NOW() AND e.id = uoe.eleicao_id;";
        id = database.submitQuery(sql_id);
        descricao = database.submitQuery(sql_descricao);
        local = database.submitQuery(sql_local);

        eleicoes.add(id);
        eleicoes.add(descricao);
        eleicoes.add(local);

        return eleicoes;
    }

    public ArrayList<String> showLista(String nome, int eleicao_id) throws RemoteException{

        ArrayList<String> lista;
        String sql = "SELECT * FROM lista WHERE nome LIKE " + nome + " AND eleicao_id='" + eleicao_id + "';";
        lista = database.submitQuery(sql);

        return lista;
    }

    public ArrayList<String> showListsFromElection(int eleicao_id) throws RemoteException{
      
      ArrayList<String> lists;
      String sql_get_lists = "SELECT nome FROM lista WHERE eleicao_id='" + eleicao_id + "' AND tipo_utilizador != 0;";
      
      lists = database.submitQuery(sql_get_lists);

      return lists;
    }

    public ArrayList<String> pickListsFromElection(int numero_cc, int eleicao_id) throws RemoteException{

      String sql_get_lists = "SELECT nome FROM lista WHERE eleicao_id = '" + eleicao_id + "' AND tipo_utilizador = ( SELECT tipo FROM utilizador WHERE numero_cc='" + numero_cc + "' ) OR tipo_utilizador = 0;";
      ArrayList<String> lists = database.submitQuery(sql_get_lists);

      return lists;
    }

    public ArrayList<ArrayList<String>> showUtilizadoresMesaVoto(int numero, String un_orn_name, int eleicao_id) throws RemoteException{

        ArrayList<ArrayList<String>> utilizadores = new ArrayList<ArrayList<String>>();
        ArrayList<String> id;
        ArrayList<String> nome;
        String sql_id = "SELECT u.id FROM utilizador AS u, mesa_voto_utilizador AS mvu WHERE u.numero_cc = mvu.utilizador_numero_CC AND mvu.mesa_voto_unidade_organica_nome LIKE " + un_orn_name + "' AND mvu.eleicao_id ='" + eleicao_id + "';";
        String sql_nome = "SELECT u.nome FROM utilizador AS u, mesa_voto_utilizador AS mvu WHERE u.numero_cc = mvu.utilizador_numero_CC AND mvu.mesa_voto_unidade_organica_nome LIKE " + un_orn_name + "' AND mvu.eleicao_id ='" + eleicao_id + "';";
        id = database.submitQuery(sql_id);
        nome = database.submitQuery(sql_nome);

        utilizadores.add(id);
        utilizadores.add(nome);

        return utilizadores;
    }

    public ArrayList<String> showPersonVotingInfo(int numero_cc, int eleicao_id) throws RemoteException{

        ArrayList<String> info;
        String sql = "SELECT * FROM eleicao_utilizador WHERE numero_cc ='" + numero_cc + "' AND eleicao_id = '" + eleicao_id + "';";
        info = database.submitQuery(sql);

        return info;

    }

    public ArrayList<ArrayList<String>> showMesasVotoEleicao(int eleicao_id) throws RemoteException{
  
        ArrayList<ArrayList<String>> mesas = new ArrayList<ArrayList<String>>();
        ArrayList<String> numero_mesas;
        ArrayList<String> localizacao_mesas;

        String sql_mesas = "SELECT numero FROM mesa_voto WHERE eleicao_id=" + eleicao_id + ";";
        String sql_un_org = "SELECT unidade_organica_nome FROM unidade_organica_eleicao WHERE eleicao_id =" + eleicao_id + ";";
        numero_mesas = database.submitQuery(sql_mesas);
        localizacao_mesas = database.submitUpdate(sql_un_org);

        mesas.add(numero_mesas);
        mesas.add(localizacao_mesas);

        return mesas;
    }

    public ArrayList<String> showListasFromEleicao(int eleicao_id) throws RemoteException{

      ArrayList<String> listas;
      String sql_listas = "SELECT nome FROM lista WHERE eleicao_id = '" + eleicao_id + "';";

      listas = database.submitQuery(sql_listas);

      return listas;
    }

    // Update
    
    public boolean updateUtilizador(int cc, String new_info, int flag){

      int aux;
      String sql="";

      switch (flag){
        case 1:
          sql = "UPDATE utilizador SET nome='" + new_info + "' WHERE numero_cc='" + cc + "';";
          break;
        case 2:
          sql = "UPDATE utilizador SET morada='" + new_info + "' WHERE numero_cc='" + cc + "';";
          break;
        case 3:
          aux = Integer.parseInt(new_info);
          sql = "UPDATE utilizador SET contacto='" + new_info + "' WHERE numero_cc='" + cc + "';";
          break;
        case 4:
          aux = Integer.parseInt(new_info);
          sql = "UPDATE utilizador SET numero_cc='" + aux + "' WHERE numero_cc='" + cc + "';";
          break;
        case 5:
          sql = "UPDATE utilizador SET validade_cc='" + new_info + "' WHERE numero_cc='" + cc + "';";
          break;
        case 6:
          aux = Integer.parseInt(new_info);
          sql = "UPDATE unidade_organica_utilizador SET unidade_organica_nome='" + aux +";";
          break;
        case 7:
          sql = "UPDATE utilizador SET password_hashed = " + new_info + ";";
        default:
          break;

      }
      if (!sql.equals("")){
        database.submitUpdate(sql); 
      }
      else return false;

      return true;
    }

    public boolean updateEleicoesDescricao(int id, String newdate) throws RemoteException{

      boolean toClient = true;
      String sql = "SELECT * FROM eleicao WHERE ID='" + id + "' AND inicio < NOW() AND fim > NOW();";
      ArrayList<String> aux = database.submitQuery(sql);
      if (aux.isEmpty()){
        toClient = false;
      }
      else{
          sql = "UPDATE eleicao SET descricao ='" + newdate + "' WHERE id ='" + id + "';";
          database.submitUpdate(sql);
      } 

      return toClient;
    }
    public boolean updateEleicoesData(int id, String newdate, int flag) throws RemoteException{

      boolean toClient = true;
      String sql = "SELECT * FROM eleicao WHERE ID='" + id + "' AND inicio < NOW() AND fim > NOW();";
      ArrayList<String> aux = database.submitQuery(sql);
      if (aux.isEmpty()){
        toClient = false;
      }
      else{
        if (flag == 1){
          sql = "UPDATE Eleicao SET inicio='" + newdate + "' WHERE ID='" + id + "';";
        }
        else if (flag == 2){
          sql = "UPDATE Eleicao SET fim ='" + newdate + "' WHERE ID='" + id + "';";
        }
        database.submitUpdate(sql);
      } 

      return toClient;
    }

    public boolean updateUnidadeOrganica(String nome, String novo_nome, int flag) throws RemoteException{

      //edita nome de departamento / faculdade. flag 1 - dep, flag 2 - fac
      String sql;

      if (flag == 1){
        sql = "UPDATE unidade_organica SET nome='" + novo_nome + "' WHERE nome='" + nome + "';";
      }
      else if (flag == 2){
        sql = "UPDATE unidade_organica SET pertence='" + novo_nome + "' WHERE nome='" + nome + "';";
      }

      database.submitUpdate(sql);
      return true;
    }

    public boolean updateMesaVotoUtilizadores(int numero_cc, String unidade_organica_nome , int id_eleicao){

      String sql = "INSERT INTO mesa_voto_utilizador (mesa_voto_unidade_organica_nome, mesa_voto_eleicao_id, utilizador_numero_cc) VALUES (" +unidade_organica_nome + "," +  id_eleicao + "," + numero_cc + ") ON DUPLICATE KEY UPDATE SET mesa_voto_unidade_organica_nome = '" + unidade_organica_nome + "','" + id_eleicao + "','" + numero_cc + "' WHERE numero_cc ='" + numero_cc +"';";

      database.submitUpdate(sql);

      return true;

    }

    public String vote(int cc, String lista, int eleicao_id, int mesavoto_id) throws RemoteException{

      String toClient = null;
      ArrayList<String> aux1;
      ArrayList<String> aux2;
      ArrayList<String> aux3;
      String sql6 = "SELECT nome FROM lista WHERE nome='" + lista + "' AND eleicao_id='" + eleicao_id +"';";
      aux3 = database.submitQuery(sql6);
      if (lista.equals("")){
        aux3.add("Blank"); 
      }
      else if (!aux3.isEmpty()){
        aux3.add("Null");
      }

      String sql2 = "SELECT * FROM eleicao_utilizador WHERE utilizador_numero_cc  ='" + cc + "' AND eleicao_id='" +  eleicao_id + "';";

      aux2 = database.submitQuery(sql2);
      System.out.println(aux2);
      if (aux2.isEmpty()){
        String sql3 = "SELECT unidade_organica_nome FROM mesa_voto WHERE eleicao_id = '" + eleicao_id + "' AND numero='" + mesavoto_id + "';";
        aux1 = database.submitQuery(sql3);
        String sql4 = "INSERT INTO eleicao_utilizador (unidade_organica_nome,eleicao_id,utilizador_numero_cc) VALUES('" + aux1.get(0) + "','" + eleicao_id + "',true,'" + mesavoto_id + "',NOW());";
        String sql5 = "UPDATE mesa_voto SET numeroVotos=numeroVotos+1 WHERE ID='" +mesavoto_id+ "';";
        String sql1 = "UPDATE lista SET votos = votos +1 WHERE nome LIKE " + lista + " AND eleicao_id='" + eleicao_id + "';";
        database.submitUpdate(sql5);
        database.submitUpdate(sql4);
        database.submitUpdate(sql1);
        toClient = lista;
      }
      else{
        toClient = null;
      }
      return toClient;
    }

    public String anticipatedVote(int cc, String lista, int eleicao_id, String pass) throws RemoteException{

      String toClient = null;
      ArrayList<String> aux1;
      ArrayList<String> aux2;
      ArrayList<String> aux3;
      ArrayList<String> check;
      String sql1 = "SELECT * FROM utilizador WHERE numero_cc='" + cc + "' AND password_hashed LIKE " + pass + ";";
      check = database.submitQuery(sql1);
      if (check.isEmpty()){
        return toClient;
      }
      String sql2 = "SELECT nome FROM lista WHERE nome LIKE " + lista + " AND eleicao_id='" + eleicao_id +"';";
      aux3 = database.submitQuery(sql2);
      if (lista.equals("")){
        aux3.add("Blank"); 
      }
      else if (!aux3.isEmpty()){
        aux3.add("Null");
      }

      String sql3 = "SELECT * FROM eleicao_utilizador WHERE utilizador_numero_cc  ='" + cc + "' AND eleicao_id='" +  eleicao_id + "';";

      aux2 = database.submitQuery(sql3);
      System.out.println(aux2);
      if (aux2.isEmpty()){
        String sql4 = "INSERT INTO eleicao_utilizador (unidade_organica_nome,eleicao_id,utilizador_numero_cc) VALUES('" + aux1.get(0) + "','" + eleicao_id + "',true,NULL,NOW());";
        String sql5 = "UPDATE lista SET votos = votos +1 WHERE nome LIKE " + lista + " AND eleicao_id='" + eleicao_id + "';";
        database.submitUpdate(sql4);
        database.submitUpdate(sql5);
        toClient = lista;
      }
      else{
        toClient = null;
      }
      return toClient;
    }
    // Delete

    public boolean deleteUtilizador(int numero_cc) throws RemoteException{

        boolean answer = true;
        String protection = "SELECT * FROM utilizador WHERE numero_cc ='" + numero_cc + "';";
        ArrayList safety = database.submitQuery(protection);

        if (safety.isEmpty()){
            answer = false;
        }
        else{
            String sql = "DELETE FROM utilizador WHERE numero_cc='" + numero_cc +"';";
            database.submitQuery(sql);
        }

        return answer;
    }

    public boolean deleteUO(String nome) throws RemoteException{

        boolean answer = true;
        String protection = "SELECT * FROM unidade_organica WHERE unidade_organica LIKE " + nome + ";";
        ArrayList safety = database.submitQuery(protection);

        if (safety.isEmpty()){
            answer = false;
        }
        else{
            String sql = "DELETE FROM unidade_organica WHERE nome LIKE " + nome +";";
            database.submitQuery(sql);
        }

        return answer;
    }

    public boolean deleteLista(String nome, int eleicao_id) throws RemoteException{

        boolean answer = true;
        String protection = "SELECT * FROM lista WHERE nome LIKE " + nome + " AND eleicao_id=" + eleicao_id + ";";
        ArrayList safety = database.submitQuery(protection);

        if (safety.isEmpty()){
            answer = false;
        }
        else{
            String sql = "DELETE FROM lista WHERE nome='" + nome +"' AND eleicao id=" + eleicao_id + ";";
            database.submitQuery(sql);
        }

        return answer;
    }

    public boolean deleteMesaVoto(int numero,String un_org_nome, int eleicao_id) throws RemoteException{

        boolean answer = true;
        String protection = "SELECT * FROM mesa WHERE un_org_nome LIKE " + un_org_nome + " AND eleicao_id=" + eleicao_id + " AND numero=" + numero + ";";
        ArrayList safety = database.submitQuery(protection);

        if (safety.isEmpty()){
            answer = false;
        }
        else{
            String sql = "DELETE FROM mesa WHERE un_org_nome LIKE " + un_org_nome + " AND eleicao_id=" + eleicao_id + " AND numero=" + numero + ";";
            database.submitQuery(sql);
        }

        return answer;
    }

    // Security
    
    public boolean isConnected() throws RemoteException{
      return true;
    }

    public boolean checkLogin(int numero_cc, String nome, String password_hashed) throws RemoteException{
       
       boolean answer = true;
       String sql = "SELECT * FROM utilizador WHERE numero_cc = '" + numero_cc + "' AND nome LIKE " + nome + " AND password_hashed LIKE " + password_hashed + ";";
       ArrayList check = database.submitQuery(sql);

       if (check.isEmpty()){
          answer = false;
       }
       else answer = true;


       return answer;
    }

    public String checkCC(int numero_cc, int eleicao_id) throws RemoteException{

      String answer = null;
      String sql_user_data = "SELECT nome,tipo FROM utilizador WHERE numero_cc ='" + numero_cc + "';";
      ArrayList<String> user_data = database.submitQuery(sql_user_data);

      if (!user_data.isEmpty()){
        String sql_eleicao_data = "SELECT e.tipo, uoe.unidade_organica_nome FROM eleicao AS e, unidade_organica_eleicao AS uoe WHERE e.eleicao_id='" + eleicao_id + "' AND uoe.eleicao_id ='" + eleicao_id + "';";
        ArrayList<String> eleicao_data = database.submitQuery(sql_eleicao_data);

        if(!eleicao_data.isEmpty()){
          if (eleicao_data.get(0).equals("1")){
            answer = user_data.get(0);
          }
          else{ 
            String sql_user_unorg = "SELECT unidade_organica_nome FROM unidade_organica_utilizador WHERE numero_cc ='" + numero_cc + "';";
            ArrayList<String> user_unorg = database.submitQuery(sql_user_unorg);

            if (!user_unorg.isEmpty()){
              if((eleicao_data.get(0).equals("0") || eleicao_data.get(0).equals("2") ) && eleicao_data.get(1).equals(user_unorg.get(0))){
                answer = user_data.get(0);
              }
              else {
                String sql_check_pertence = "SELECT uo1.pertence, uo2.pertence FROM unidade_organica AS uo1, unidade_organica AS uo2 WHERE uo1.nome LIKE " + eleicao_data.get(1) + " AND uo2.nome LIKE " + user_unorg.get(0) + ";";
                ArrayList<String> unorg_pertence = database.submitQuery(sql_check_pertence);

                if(eleicao_data.get(0).equals("3") && unorg_pertence.get(0).equals(unorg_pertence.get(1))){
                  answer = user_data.get(0);
                } else answer = null;

              }
            } else answer = null;

          }
        } else answer = null;

      } else answer = null;

      return answer;
    }

    class UDPConnection extends Thread {

        int mainUDP, secUDP;
        int pingFrequency;
        int retries;
        boolean mainServer = true;

        UDPConnection(boolean serverType) {

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

        @Override
        public void run() {

            DatagramSocket aSocket = null;
            byte[] buffer = new byte[1024];
            if (mainServer) {
                try {
                    aSocket = new DatagramSocket(mainUDP);
                    aSocket.setSoTimeout(pingFrequency);
                } catch (SocketException se) {
                    se.printStackTrace();
                }
                while (true) {

                    byte[] message = "ping pong".getBytes();

                    int i = 0;
                    do {
                        try {

                            Thread.sleep(1000);
                            DatagramPacket toSend = new DatagramPacket(message, message.length, InetAddress.getByName("127.0.0.1"), secUDP);
                            aSocket.send(toSend);
                            System.out.println("[UDP] Ping");
                            DatagramPacket toReceive = new DatagramPacket(buffer, buffer.length);
                            aSocket.receive(toReceive);
                            System.out.println("[UDP] Pong");
                            i = 0;

                        } catch (SocketTimeoutException ste) {
                            System.out.println("Backup server isn't responding");
                            i++;
                        } catch (IOException ioe) {
                            System.out.println("Networking Problems");
                        } catch (InterruptedException ie) {
                            RMIServer.this.heartbeat = null;

                            try {
                                Naming.unbind(RMIServer.rmiName);
                            } catch (RemoteException re) {
                            } catch (NotBoundException nbe) {
                            } catch (MalformedURLException murle) {
                            }

                        }

                    } while (i < retries);

                    System.out.println("Backup Server failed! \n Retrying pings");


                }

            } else if (!mainServer) {

                try {
                    aSocket = new DatagramSocket(secUDP);
                    aSocket.setSoTimeout(pingFrequency);
                } catch (SocketException se) {
                    se.printStackTrace();
                }
                System.out.println("Is Backup Server");

                while (true) {

                    byte[] message = "ping pong".getBytes();

                    int i = 0;

                    do {

                        try {

                            Thread.sleep(1000);
                            DatagramPacket toSend = new DatagramPacket(message, message.length, InetAddress.getByName("127.0.0.1"), mainUDP);

                            aSocket.send(toSend);
                            System.out.println("[UDP] Ping");
                            DatagramPacket toReceive = new DatagramPacket(buffer, buffer.length);
                            aSocket.receive(toReceive);
                            System.out.println("[UDP] Pong");
                            i = 0;

                        } catch (SocketTimeoutException ste) {
                            System.out.println("Main RMI Server not responding");
                            i++;
                        } catch (IOException ioe) {
                            System.out.println("Network Problems");
                        } catch (InterruptedException ie) {

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } while (i < retries);

                    System.out.println("RMIServer failed \nAssuming Main Server Status");

                    try {
                        aSocket.close();
                        RMIServer.this.heartbeat = null;
                        RMIServer.this.mainServer = true;
                        RMIServer.this.startRMIServer();
                        Thread.currentThread().join();
                    } catch (InterruptedException ie) {
                        System.out.println("Thread Interrupted");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }

        }

    }
}
