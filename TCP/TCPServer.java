import java.net.*;
import java.io.*;

public class TCPServer {
    static int tcpPort;
    public static void main(String args[]) {
        int numero = 0;
        TCPConfigLoader newConfig = new TCPConfigLoader();
        tcpPort = newConfig.getTCPPort();
        try {
            int serverPort = 12345;
            System.out.println("Listening on port: " + serverPort);
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
}

//= Thread para tratar de cada canal de comunicação com um cliente
class Connection extends Thread {
    BufferedReader in;
    PrintWriter out;
    Socket clientSocket;
    int thread_number;

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
        String clientMessage;
        try {
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("T["+thread_number + "] Received: "+ clientMessage);
                out.println("Server received: " + clientMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}