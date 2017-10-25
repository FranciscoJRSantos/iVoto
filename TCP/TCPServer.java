import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class TCPServer {
    static int serverPort;

    static int electionID;
    static String electionName;

    static int tableID;

    static ArrayList<String> candidateList;

    static List<Connection> connectionList = Collections.synchronizedList(new ArrayList<Connection>());

    public static void main(String args[]) {
        int connectionCount = 0;
        int choice;
        TCPConfigLoader c = new TCPConfigLoader();
        serverPort = c.getTCPPort();


        ArrayList<Object> electionData = requestElectionsList();
        ArrayList<Integer> electionIDList = (ArrayList<Integer>) electionData.get(0);
        ArrayList<String> electionNameList = (ArrayList<String>) electionData.get(1);
        //TODO: Receive date. Waiting for data type decision
        while (true) {
            for (int i = 0; i < electionIDList.size(); i++) {
                System.out.printf("\t%d - %s\n", electionIDList.get(i), electionNameList.get(i));
            }
            System.out.println("Pick the election by ID: ");
            choice = readInt();
            if (electionIDList.contains(choice)) {
                electionID = choice;
                electionName = electionNameList.get(electionIDList.indexOf(choice));
                System.out.printf("Election %d '%s' was successfully picked\n", electionID, electionName);
                break;
            } else {
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
            if (tableIDList.contains(choice)) {
                tableID = choice;
                System.out.printf("Table #%d was successfully picked\n", tableID);
                break;
            } else {
                System.out.println("There's no such table!");
                enterToContinue();
            }
        }

        //System.out.printf("election id %d, election name %s, table ID %d, candidates %s\n", electionID, electionName, tableID, candidateList);

        new AdminCommands();


        try {
            System.out.println("Listening to port" + serverPort);
            ServerSocket listenSocket = new ServerSocket(serverPort);
            System.out.println("LISTEN SOCKET=" + listenSocket);
            while (true) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);
                connectionCount++;
                connectionList.add(new Connection(clientSocket, connectionCount));
            }
        } catch (IOException e) {
            System.out.println("Listen:" + e.getMessage());
        }
    }

    private static ArrayList<Object> requestElectionsList() {
        //TODO request RMI. Provavelmente so eleicoes futuras ou a decorrer?
        ArrayList<Integer> fakeIDAnswer = new ArrayList<>();
        fakeIDAnswer.add(2);
        fakeIDAnswer.add(5);
        fakeIDAnswer.add(7);
        ArrayList<String> fakeElectionAnswer = new ArrayList<>();
        fakeElectionAnswer.add("Uma");
        fakeElectionAnswer.add("A outra");
        fakeElectionAnswer.add("Ultima");

        ArrayList<Object> fakeTuplo = new ArrayList<>();
        fakeTuplo.add(fakeIDAnswer);
        fakeTuplo.add(fakeElectionAnswer);

        return fakeTuplo;
    }

    private static ArrayList<String> requestCandidatesList() {
        //TODO request RMI with electionID (static)
        ArrayList<String> fake = new ArrayList<>();
        fake.add("Lista coiso");
        fake.add("Lista as vezes");
        fake.add("Lista só mais esta");
        return fake;
    }

    private static ArrayList<Integer> requestTableList() {
        //TODO request RMI with electionID (static)
        ArrayList<Integer> fake = new ArrayList<>();
        fake.add(1);
        fake.add(3);
        fake.add(9);
        return fake;
    }

    static String checkCC(int cc){
        //TODO request RMI, also send election!
        //return null if not found. Return name otherwise!
        return "That Guy";
    }

    static boolean checkLoginInfo(String username, String password) {
        //TODO request RMI
        return true;
    }

    static boolean registerVote() {
        //TODO send to RMI with electionID (static), tableID (static)
        return true;
    }

    public static int readInt() {
        Scanner sc = new Scanner(System.in);
        String aux;
        int num;
        while (true) {
            aux = sc.nextLine();
            try {
                num = Integer.parseInt(aux);
                return num;
            } catch (NumberFormatException e) {
                System.out.println("Not a number. Please input a number:");
            }
        }
    }

    public static void enterToContinue() {
        System.out.println("Press enter to continue...");
        Scanner sc = new Scanner(System.in);
        sc.nextLine();
        return;
    }
}

//= Thread para tratar de cada canal de comunica��o com um cliente
class Connection extends Thread {
    public int terminalID;
    public boolean isBlocked;
    private BufferedReader in;
    private PrintWriter out;
    private Socket clientSocket;
    private int cc;
    private String name;


    public Connection(Socket aClientSocket, int n) {
        terminalID = n;
        isBlocked = true;
        try {
            clientSocket = aClientSocket;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.printf("Connection with table #%d successful! You are voting terminal #%d\n", TCPServer.tableID, terminalID);
            this.start();
        } catch (IOException e) {
            //TODO: handle connection closed
            System.out.println("Connection on place 1: " + e.getMessage());
        }
    }

    @Override
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
        //TODO: Make sure everything waits up to 30s!

        if (isBlocked) {
            out.println("Terminal is blocked. Message ignored.");
            return;
        }

        //TODO: Unblock timeout

        Message m = new Message(s);
        if (!m.getIsValid()) {
            out.println("Message not valid!");
            return;
        }
        //out.printf("type:%d, s3:%s, sList:%s\n", m.getType(), m.getS3(), m.getsList());

        switch (m.getType()) {
            case 0:

                if (TCPServer.checkLoginInfo(m.getS1(), m.getS2())) {
                    out.println("Login successful.");
                    int aux = 0;
                    String result = "";
                    for (String candidate : TCPServer.candidateList) {
                        result = String.format("%s\t%d - %s\n", result, aux++, candidate);
                    }
                    result = result.concat("Pick your candidate: ");
                    out.println(result);
                } else {
                    out.println("Login data was incorrect");
                }
                break;
            case 1:
                //TODO
                if (TCPServer.registerVote()) {
                    out.println("");
                } else {

                }
                break;
            default:
                out.println("Type non-existent!");
                break;
        }
    }

    public boolean blockTerminal() {
        isBlocked = true;
        //TODO clean up stuff like CC and Name. Send message here?
        return true;
    }

    public boolean unblockTerminal(int voterCC, String voterName) {
        isBlocked = false;
        cc = voterCC;
        name = voterName;
        out.printf("This terminal has been unlocked for %s (CC: %d). Timeout will occur if inactive for 120 seconds\n", name, cc);
        out.println("Please login");
        //TODO Still testing this
        return true;
    }

}

class AdminCommands extends Thread{
    public AdminCommands() {
        this.start();
    }

    @Override
    public void run() {
        //TODO: Only allow it if election is in progress
        while (true){
            int cc = 0;
            String name = null;
            int option = 0;

            System.out.println("=============Voting Table=============\n");
            while (true) {
                System.out.println("Insert voter's CC number:");
                cc = TCPServer.readInt();
                name = TCPServer.checkCC(cc);
                if (name!=null) {
                    System.out.println("Inserted CC number is valid!");
                    break;
                } else {
                    System.out.println("CC not in database.");
                    TCPServer.enterToContinue();
                }

            }
            while (true) {
                //synchronized to avoid changes while iterating
                int aux = 0;
                synchronized (TCPServer.connectionList) {
                    for (Connection c : TCPServer.connectionList) {
                        if(c.isBlocked){
                            System.out.println("\tVoting terminal #" + c.terminalID);
                            aux++;
                        } else {
                            System.out.println("\tVoting terminal #" + c.terminalID + "[IN USE, CAN'T BE PICKED]");
                        }
                    }
                }
                if(aux == 0){
                    System.out.println("No voting terminals are available! Operation cancelled.");
                    break;
                }

                System.out.println("Pick a terminal to unlock:");
                //TODO: Protect against terminal getting deleted meanwhile!
                option = TCPServer.readInt();

                Connection auxC = null;
                synchronized (TCPServer.connectionList) {
                    for (Connection c : TCPServer.connectionList) {
                        if (c.terminalID == option){
                            if(!c.isBlocked){
                                break;
                            }
                            auxC = c;
                            break;
                        }
                    }
                }
                if(auxC == null){
                    System.out.println("Terminal non-existent or in use.");
                }
                else{
                    if (auxC.unblockTerminal(cc, name)){
                        System.out.printf("Unblocked terminal %d. Timeout will occur if inactive for 120 seconds\n", auxC.terminalID);
                        break;
                    }
                }

            }
            TCPServer.enterToContinue();
        }
    }
}