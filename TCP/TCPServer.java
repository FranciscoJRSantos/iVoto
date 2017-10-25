import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class TCPServer {
    //TODO: switch to properties
    static int serverPort;
    static int id;
    static String election;

    public static void main(String args[]) {
        int numero = 0;
        TCPConfigLoader c = new TCPConfigLoader();
        serverPort = c.getTCPPort();
        //TODO: Antes de começar tudo, dar setup a numero de mesa de voto e eleicao
        


        try {
            System.out.println("Listening to port" + serverPort);
            ServerSocket listenSocket = new ServerSocket(serverPort);
            System.out.println("LISTEN SOCKET=" + listenSocket);
            while (true) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);
                numero++;
                new Connection(clientSocket, numero);
            }
        } catch (IOException e) {
            System.out.println("Listen:" + e.getMessage());
        }
    }

    static boolean checkLoginInfo(String username, String password) {
        //TODO
        return true;
    }

    static boolean registerVote() {
        //TODO
        return true;
    }
}

//= Thread para tratar de cada canal de comunica��o com um cliente
class Connection extends Thread {
    private BufferedReader in;
    private PrintWriter out;
    private Socket clientSocket;
    private int thread_number;
    private boolean isBlocked;
    private int cc;


    public Connection(Socket aClientSocket, int numero) {
        thread_number = numero;
        try {
            clientSocket = aClientSocket;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.start();
        } catch (IOException e) {
            System.out.println("Connection:" + e.getMessage());
        }
    }

    //=============================
    public void run() {
        try {
            while (true) {
                answerMessage(in.readLine());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void answerMessage(String s) {
        Message m = new Message(s);
        if (!m.getIsValid()){
            out.println("Message not valid!");
            return;
        }
        //out.printf("type:%d, s3:%s, sList:%s\n", m.getType(), m.getS3(), m.getsList());

        //TODO: Check if unblocked. Unblocked timer.
        switch (m.getType()){
            case 0:

                if(TCPServer.checkLoginInfo(m.getS1(), m.getS2())){
                    out.println("Login successful.");
                }
                else{
                    out.println("Login infos incorrect");
                    /*Send candidates. they should be cached I guess? TODO*/
                }
                break;
            case 1:
                if(TCPServer.registerVote()){
                    out.println("");
                }
                else {

                }
                break;
            default:
                out.println("Type non-existent!");
                break;
        }
    }

    public void blockTerminal(){
        isBlocked = true;
        //TODO clean up stuff like CC
    }

    public void unblockTerminal(int CC){
        isBlocked = false;
        //TODO
    }

}