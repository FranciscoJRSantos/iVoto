import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class TCPServer {
    //TODO: switch to properties
    static int serverPort;
    static int serverID;

    static int electionID;
    static String electionName;

    static int tableID;

    static ArrayList<String> candidateList;

    public static void main(String args[]) {
        int connectionCount = 0;
        int choice;
        TCPConfigLoader c = new TCPConfigLoader();
        serverPort = c.getTCPPort();

        ArrayList<Integer> electionIDList = null;
        ArrayList<String> electionNameList = null;
        requestElectionsList(electionIDList, electionNameList);

        while (true) {
            for(int i=0; i<electionIDList.size(); i++){
                System.out.printf("\t%d - %s\n", electionIDList.get(i), electionNameList.get(i));
            }
            System.out.println("Pick the election by ID: ");
            choice = readInt();
            if (electionIDList.contains(choice)){
                electionID = choice;
                electionName = electionNameList.get(electionIDList.indexOf(choice));
                System.out.printf("Election %d '%s' was successfully picked", electionID, electionName);
                break;
            }
            else{
                System.out.println("There's no such ID!");
                enterToContinue();
            }
        }

        requestCandidatesList(electionID); //keeping it cached

        ArrayList<Integer> tableIDList = null;
        requestTableList(electionID, tableIDList);

        while (true) {
            for (int id : tableIDList) {
                System.out.println("Table #" + id);
            }
            System.out.println("Pick the table's number: ");
            choice = readInt();
            if (tableIDList.contains(choice)){
                tableID = choice;
                break;
            }
            else{
                System.out.println("There's no such table!");
                enterToContinue();
            }
        }

        try {
            System.out.println("Listening to port" + serverPort);
            ServerSocket listenSocket = new ServerSocket(serverPort);
            System.out.println("LISTEN SOCKET=" + listenSocket);
            while (true) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);
                connectionCount++;
                new Connection(clientSocket, connectionCount);
            }
        } catch (IOException e) {
            System.out.println("Listen:" + e.getMessage());
        }
    }

    private static void requestElectionsList(ArrayList<Integer> electionIDList, ArrayList<String> electionNameList) {
        //TODO request RMI
        //get both their IDs and names. Change lists.
    }

    private static void requestCandidatesList(int electionID){
        //TODO request RMI and change candidateList (static)
    }

    private static void requestTableList(int electionID, ArrayList<Integer> tableIDList){
        //TODO request RMI and change tableIDList
    }

    static boolean checkLoginInfo(String username, String password) {
        //TODO
        return true;
    }

    static boolean registerVote() {
        //TODO
        return true;
    }

    public static int readInt(){
        Scanner sc = new Scanner(System.in);
        String aux;
        int num;
        while (true) {
            aux = sc.nextLine();
            try {
                num = Integer.parseInt(aux);
                return num;
            } catch (NumberFormatException e) {
                System.out.print("Not a number. Please input a number:\n->");
            }
        }
    }

    private static void enterToContinue(){
        System.out.println("Press enter to continue...");
        Scanner sc = new Scanner(System.in);
        sc.nextLine();
        return;
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
        if(isBlocked){
            out.println("Terminal is blocked. Ignoring message");
            return;
        }

        //TODO: Block timer.

        Message m = new Message(s);
        if (!m.getIsValid()){
            out.println("Message not valid!");
            return;
        }
        //out.printf("type:%d, s3:%s, sList:%s\n", m.getType(), m.getS3(), m.getsList());

        switch (m.getType()){
            case 0:

                if(TCPServer.checkLoginInfo(m.getS1(), m.getS2())){
                    out.println("Login successful.");
                }
                else{
                    out.println("Login data was incorrect");
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
        //TODO clean up stuff like CC. Send message here?
    }

    public void unblockTerminal(int CC){
        isBlocked = false;
        //TODO Send message here?
    }

}