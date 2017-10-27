import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class TCPServer {
    static int serverPort;
    static String rmiName;
    static ServerInterface r;

    static int electionID;
    static String electionName;

    static int tableID;

    static ArrayList<String> candidateList;

    static final List<Connection> connectionList = Collections.synchronizedList(new ArrayList<Connection>());

    public static void main(String args[]) {
        int connectionCount = 0;
        int choice;
        TCPConfigLoader c = new TCPConfigLoader();
        serverPort = c.getTCPPort();
        rmiName = c.getRMIName();


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

        //TODO: This should only be requested right when the election starts!
        //Maybe have a thread waiting for the start, changing a boolean to true and caching the list.
        //Same as in unblocking in Admin Commands
        candidateList = requestCandidatesList(); //keeping it cached

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

    static String checkCC(int cc) {
        //TODO request RMI, also send election!
        //return null if not found. Return name otherwise!
        return "That Guy";
    }

    static boolean checkLoginInfo(String username, String password) {
        //TODO request RMI
        return true;
    }

    static String registerVote(int cc, String s1) {
        //TODO send to RMI along with electionID (static), tableID (static)
        //receive voted list (name, null or white)
        return "fake";
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
    private BufferedReader in;
    public PrintWriter out;
    private int cc;
    private String name;
    public boolean isBlocked;
    private boolean isLogged;
    public final AtomicBoolean recentActivity = new AtomicBoolean(false);
    private TimeoutTimer timer = null;


    Connection(Socket aClientSocket, int n) {
        terminalID = n;
        isBlocked = true;
        isLogged = false;
        try {
            Socket clientSocket = aClientSocket;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.printf("Connection with table #%d successful! You are voting terminal #%d\n", TCPServer.tableID, terminalID);
            this.start();
        } catch (IOException e) {
            endConnection();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                answerMessage(in.readLine());
            }

        } catch (IOException e) {
            endConnection();
        }
    }

    private void answerMessage(String s) {
        //TODO: Make sure everything waits up to 30s!

        if (isBlocked) {
            out.println("Terminal is blocked. Message ignored.");
            return;
        }

        synchronized (recentActivity) {
            recentActivity.set(true);
            recentActivity.notify();
        }

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
                if (!isLogged) {
                    out.println("You can't vote yet! Login first.");
                    break;
                }
                //TODO: Send vote, allow for null our white votes
                String aux = TCPServer.registerVote(cc, m.getS1());
                if (aux != null){
                    out.printf("Vote for candidate %s registered successfully.\n", aux);
                    blockTerminal();
                }
                else {
                    out.println("An error happened while voting. Please vote again.");
                    break;
                }
                break;
            default:
                out.println("Type non-existent!");
                break;
        }
    }

    public synchronized boolean blockTerminal() {
        isBlocked = true;
        isLogged = false;
        cc = 0;
        name = null;
        recentActivity.set(false);
        timer.interrupt();
        System.out.printf("[STATUS] Terminal #%d was blocked.\n", terminalID);
        out.println("This terminal has been blocked.");
        //TODO Still testing this
        return true;
    }

    public synchronized boolean unblockTerminal(int voterCC, String voterName) {
        isBlocked = false;
        cc = voterCC;
        name = voterName;
        recentActivity.set(false);
        timer = new TimeoutTimer(this);
        System.out.printf("Unblocked terminal #%d. Timeout will occur if inactive for 120 seconds\n", terminalID);
        out.printf("This terminal has been unlocked for %s (CC: %d). Timeout will occur if inactive for 120 seconds\nPlease login:\n", name, cc);
        //TODO Still testing this
        return true;
    }

    private void endConnection() {
        synchronized (TCPServer.connectionList) {
            TCPServer.connectionList.remove(this);
        }
        if (timer != null) timer.interrupt();
        System.out.printf("[Warning] Connection with terminal #%d was closed. Removed from the list of terminals\n", terminalID);
    }

}

class TimeoutTimer extends Thread {
    //120 seconds = 120000 ms
    private static final int TIMEOUT_VALUE = 12000;
    Connection parent;

    TimeoutTimer(Connection connection) {
        parent = connection;
        this.start();
    }

    @Override
    public void run() {
        long endTime = System.currentTimeMillis() + TIMEOUT_VALUE;
        long timeRemaining;
        synchronized (parent.recentActivity) {
            while (true) {
                timeRemaining = TIMEOUT_VALUE;
                //Protecting for spurious wakeups
                while (timeRemaining > 0 && !parent.recentActivity.get()) {
                    try {
                        parent.recentActivity.wait(timeRemaining);
                        timeRemaining = endTime - System.currentTimeMillis();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                //if it reaches here we're sure TIMEOUT_VALUE milliseconds have passed or there has been recent activity
                if (parent.recentActivity.get()) {
                    parent.recentActivity.set(false);
                    parent.out.println("[TIMER] Activity detected, timeout timer reset to 120 seconds.");
                } else {
                    parent.blockTerminal();
                    return;
                }
            }
        }
    }
}

class AdminCommands extends Thread {
    AdminCommands() {
        this.start();
    }

    @Override
    public void run() {
        //TODO: Only allow unblocking if election is in progress!
        //Maybe a thread?

        while (true) {
            int cc = 0;
            String name = null;
            int option = 0;

            System.out.println("=============Voting Table=============\n");
            while (true) {
                System.out.println("Insert voter's CC number:");
                cc = TCPServer.readInt();
                name = TCPServer.checkCC(cc);
                if (name != null) {
                    System.out.println("Inserted CC number is valid and can vote in this election!");
                    break;
                } else {
                    System.out.println("CC not in database or not allowed to vote in this election.");
                    TCPServer.enterToContinue();
                }

            }
            while (true) {
                //synchronized to avoid changes while iterating
                int aux = 0;
                synchronized (TCPServer.connectionList) {
                    for (Connection c : TCPServer.connectionList) {
                        if (c.isBlocked) {
                            System.out.println("\tVoting terminal #" + c.terminalID);
                            aux++;
                        } else {
                            System.out.println("\tVoting terminal #" + c.terminalID + "[IN USE, CAN'T BE PICKED]");
                        }
                    }
                }
                if (aux == 0) {
                    System.out.println("No voting terminals are available! Operation cancelled.");
                    break;
                }

                System.out.println("Pick a terminal to unlock:");
                option = TCPServer.readInt();

                Connection auxC = null;
                synchronized (TCPServer.connectionList) {
                    for (Connection c : TCPServer.connectionList) {
                        if (c.terminalID == option) {
                            if (!c.isBlocked) break;
                            auxC = c;
                            break;
                        }
                    }
                }
                if (auxC == null) {
                    System.out.println("Terminal non-existent or in use.");
                } else {
                    auxC.unblockTerminal(cc, name);
                    break;
                }

            }
            TCPServer.enterToContinue();
        }
    }
}