import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TCPServer {
    static int serverPort;

    static int electionID;
    static String electionName;

    static int tableID;

    static ArrayList<String> candidateList;

    public static void main(String args[]) {
        int connectionCount = 0;
        int choice;
        TCPConfigLoader c = new TCPConfigLoader();
        serverPort = c.getTCPPort();


        ArrayListHolder temp = requestElectionsList();
        ArrayList<Integer> electionIDList = temp.electionIDList;
        ArrayList<String> electionNameList = temp.electionNameList;
        while (true) {
            for(int i=0; i<electionIDList.size(); i++){
                System.out.printf("\t%d - %s\n", electionIDList.get(i), electionNameList.get(i));
            }
            System.out.println("Pick the election by ID: ");
            choice = readInt();
            if (electionIDList.contains(choice)){
                electionID = choice;
                electionName = electionNameList.get(electionIDList.indexOf(choice));
                System.out.printf("Election %d '%s' was successfully picked\n", electionID, electionName);
                break;
            }
            else{
                System.out.println("There's no such ID!");
                enterToContinue();
            }
        }

        candidateList = requestCandidatesList(); //keeping it cached

        ArrayList<Integer> tableIDList = requestTableList();

        while (true) {
            for (int id : tableIDList) {
                System.out.println("\tTable #" + id);
            }
            System.out.println("Pick the table's number: ");
            choice = readInt();
            if (tableIDList.contains(choice)){
                tableID = choice;
                System.out.printf("Table #%d was successfully picked\n", tableID);
                break;
            }
            else{
                System.out.println("There's no such table!");
                enterToContinue();
            }
        }

        //System.out.printf("election id %d, election name %s, table ID %d, candidates %s\n", electionID, electionName, tableID, candidateList);
        //TODO: Thread for unlocking a terminal


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

    private static ArrayListHolder requestElectionsList() {
        //TODO request RMI. Provavelmente so eleicoes futuras ou a decorrer?
        ArrayList<Integer> fakeIDAnswer = new ArrayList<>();
        fakeIDAnswer.add(2);
        fakeIDAnswer.add(5);
        fakeIDAnswer.add(7);
        ArrayList<String> fakeElectionAnswer = new ArrayList<>();
        fakeElectionAnswer.add("Uma");
        fakeElectionAnswer.add("A outra");
        fakeElectionAnswer.add("Ultima");

        return new ArrayListHolder(fakeIDAnswer, fakeElectionAnswer);
    }

    private static ArrayList<String> requestCandidatesList(){
        //TODO request RMI with electionID (static)
        ArrayList <String> fake = new ArrayList<>();
        fake.add("Lista coiso");
        fake.add("Lista as vezes");
        fake.add("Lista só mais esta");
        return fake;
    }

    private static ArrayList<Integer> requestTableList(){
        //TODO request RMI with electionID (static)
        ArrayList<Integer> fake = new ArrayList<>();
        fake.add(1);
        fake.add(3);
        fake.add(9);
        return  fake;
    }

    static boolean checkLoginInfo(String username, String password) {
        //TODO request RMI
        return true;
    }

    static boolean registerVote() {
        //TODO send to RMI with electionID (static), tableID (static)
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
    private String name;


    public Connection(Socket aClientSocket, int numero) {
        thread_number = numero;
        //TODO: Send connection successful!
        try {
            clientSocket = aClientSocket;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.start();
        } catch (IOException e) {
            //TODO: handle connection closed
            System.out.println("Connection on place 1: " + e.getMessage());
        }
    }
    
    public void run() {
        try {
            while (true) {
                answerMessage(in.readLine());
            }

        } catch (IOException e) {
            //TODO: handle connection closed
            System.out.println("Connection on place 2: " + e.getMessage());
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
        //TODO clean up stuff like CC and Name. Send message here?
    }

    public void unblockTerminal(int CC){
        isBlocked = false;
        //TODO Send message here?
    }

}
class ArrayListHolder{
    ArrayList<Integer> electionIDList;
    ArrayList<String> electionNameList;

    public ArrayListHolder(ArrayList<Integer> electionIDList, ArrayList<String> electionNameList) {
        this.electionIDList = electionIDList;
        this.electionNameList = electionNameList;
    }
}