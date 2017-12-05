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

    public boolean createUser(String numero_cc, String nome, String password_hashed, String morada, int contacto, String validade_cc, int tipo, int un_org_nome) throws RemoteException {

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

    public boolean createLista(String nome, int tipo, int eleicao_id, String numero_cc) throws RemoteException{

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

    public boolean createMesaVoto(String un_org_nome, int eleicao_id, String numero_cc) throws RemoteException{

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

    public ArrayList<String> showUtilizador(String numero_cc) throws RemoteException{

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

    public ArrayList<String> showEleicao(int id) throws RemoteException{

        ArrayList<String> eleicao;
        String sql = "SELECT * FROM eleicao WHERE id='" + id + "';";
        eleicao = database.submitQuery(sql);

        return eleicao;
    }

    public ArrayList<String> showEleicoesDecorrer() throws RemoteException{

        ArrayList<String> eleicoes;
        String sql = "SELECT * FROM eleicao WHERE inicio > NOW() AND fim > NOW()";
        eleicoes = database.submitQuery(sql);

        return eleicoes;
    }

    public ArrayList<String> showLista(String nome, int eleicao_id) throws RemoteException{

        ArrayList<String> lista;
        String sql = "SELECT * FROM lista WHERE nome LIKE " + nome + " AND eleicao_id='" + eleicao_id + "';";
        lista = database.submitQuery(sql);

        return lista;
    }

    public ArrayList<String> showUtilizadoresMesaVoto(int numero, String un_orn_name, int eleicao_id) throws RemoteException{

        ArrayList<String> utilizadores;
        String sql = "SELECT u.* FROM utilizador u, mesa_voto_utilizador mvu WHERE u.numero_cc = mvu.utilizador_numero_CC AND mvu.mesa_voto_unidade_organica_nome LIKE " + un_orn_name + "' AND mvu.eleicao_id ='" + eleicao_id + "';";
        utilizadores = database.submitQuery(sql);

        return utilizadores;
    }

    public ArrayList<String> showPersonVotingInfo(String numero_cc, int eleicao_id) throws RemoteException{

        ArrayList<String> info;
        String sql = "SELECT * FROM eleicao_utilizador WHERE numero_cc LIKE " + numero_cc + " AND eleicao_id = '" + eleicao_id + "';";
        info = database.submitQuery(sql);

        return info;

    }

    // Update

    // Delete

    public boolean deleteUtilizador(String numero_cc) throws RemoteException{

        boolean answer = true;
        String protection = "SELECT * FROM utilizador WHERE numero_cc LIKE " + numero_cc + ";";
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
