// TCPServer2.java: Multithreaded server

import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class TCPServer {
    //TODO: switch to properties
    static int serverPort = 12345;

    public static void main(String args[]) {
        int numero = 0;
        ArrayList<Socket> clients = new ArrayList<>();
        try {
            System.out.println("Listening to port" + serverPort);
            ServerSocket listenSocket = new ServerSocket(serverPort);
            System.out.println("LISTEN SOCKET=" + listenSocket);
            while (true) {
                Socket clientSocket = listenSocket.accept();
                clients.add(clientSocket);
                System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);
                numero++;
                new Connection(clientSocket, numero);
            }
        } catch (IOException e) {
            System.out.println("Listen:" + e.getMessage());
        }
    }
}

//= Thread para tratar de cada canal de comunica��o com um cliente
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
        try {
            while (true) {
                String s = in.readLine();
                answerMessage(s);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void answerMessage(String s) {
        Message m = new Message(s);
        out.printf("type:%d, s3:%s, sList:%s\n", m.getType(), m.getS3(), m.getsList());
    }
}