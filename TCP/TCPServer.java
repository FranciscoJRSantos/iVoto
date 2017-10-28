import javax.xml.bind.SchemaOutputResolver;
import java.net.*;
import java.io.*;
import java.rmi.*;
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

    static final List<Connection> connectionList = Collections.synchronizedList(new ArrayList<Connection>());

    public static void main(String args[]) {
        //TODO: Callbacks for admin console?
        int connectionCount = 0;
        int choice;
        TCPConfigLoader c = new TCPConfigLoader();
        serverPort = c.getTCPPort();
        rmiName = c.getRMIName();

        connectToRMI();

        while(true) {
            ArrayList<ArrayList<String>> electionData = requestElectionsList();
            ArrayList<String> electionIDList = electionData.get(0);
            ArrayList<String> electionNameList = electionData.get(1);
            if (electionIDList.size() == 0){
                System.out.println("No elections available now or scheduled to happen");
                enterToContinue();
                continue;
            }
            while (true) {
                for (int i = 0; i < electionIDList.size(); i++) {
                    System.out.printf("\t%s - %s\n", electionIDList.get(i), electionNameList.get(i));
                }
                System.out.println("Pick the election by ID: ");
                choice = readInt();
                if (electionIDList.contains(Integer.toString(choice))) {
                    electionID = choice;
                    electionName = electionNameList.get(electionIDList.indexOf(Integer.toString(choice)));
                    System.out.printf("Election %d '%s' was successfully picked\n", electionID, electionName);
                    break;
                } else {
                    System.out.println("There's no such ID!");
                    enterToContinue();
                }
            }
            break;
        }

        while (true) {
            ArrayList<String> tableIDList = requestTableList();
            if (tableIDList.size()==0){
                System.out.println("No voting tables available for this election");
                enterToContinue();
                continue;
            }
            while (true) {
                for (String id : tableIDList) {
                    System.out.println("\tVoting table #" + id);
                }
                System.out.println("Pick the voting table's number: ");
                choice = readInt();
                if (tableIDList.contains(Integer.toString(choice))) {
                    tableID = choice;
                    System.out.printf("Voting table #%d was successfully picked\n", tableID);
                    break;
                } else {
                    System.out.println("There's no such voting table!");
                    enterToContinue();
                }
            }
            break;
        }

        while (true) {
            ArrayList<ArrayList<String>> staffData = requestTableStaff();
            ArrayList<String> staffCCList = staffData.get(0);
            ArrayList<String> staffNameList = staffData.get(1);
            if (staffCCList.size()==0){
                System.out.println("No staff inserted for this voting table");
                enterToContinue();
                continue;
            }
            int staffCC;
            String staffName;
            while (true) {
                for (int i = 0; i < staffCCList.size(); i++) {
                    System.out.printf("\tCC:%s\tNome:%s\n", staffCCList.get(i), staffNameList.get(i));
                }
                System.out.println("Pick staff to login by CC:");
                choice = readInt();
                if (staffCCList.contains(Integer.toString(choice))) {
                    staffCC = choice;
                    staffName = staffNameList.get(staffCCList.indexOf(Integer.toString(choice)));
                    System.out.printf("Picked staff member '%s' (CC: %d)\n", staffName, staffCC);
                    System.out.println("Insert password");
                    Scanner sc = new Scanner(System.in);
                    if (checkLoginInfo(staffCC, staffName, sc.nextLine())) {
                        System.out.println("Login successful");
                        break;
                    } else {
                        System.out.println("Wrong password!");
                        enterToContinue();
                    }
                } else {
                    System.out.println("No staff has such CC!");
                    enterToContinue();
                }
            }
            break;
        }

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

    private static void connectToRMI() {
        long timestamp = System.currentTimeMillis();
        boolean failed = false;
        while (true) {
            try {
                r = (ServerInterface) Naming.lookup(rmiName);
                break;
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                if (System.currentTimeMillis() - timestamp > 30000) {
                    System.out.println("[Warning] Couldn't connect to RMI after 30s. Operation dropped");
                    break;
                } else if (!failed) {
                    System.out.println("[Warning] Error connecting to RMI. Trying to reconnect...");
                    failed = true;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    break;
                }
            }
        }
    }

    private static ArrayList<ArrayList<String>> requestElectionsList() {
        while (true) {
            try {
                return r.viewCurrentElections();
            } catch (RemoteException e) {
                System.out.println("[Warning] Failed to use RMI viewCurrentElections. Retrying connection");
                connectToRMI();
            }
        }


    }

    private static ArrayList<String> requestTableList() {
        while (true) {
            try {
                return r.showTables(electionID);
            } catch (RemoteException e) {
                System.out.println("[Warning] Failed to use RMI showTables. Retrying connection");
                connectToRMI();
            }
        }
    }

    private static ArrayList<ArrayList<String>> requestTableStaff(){
        while (true) {
            try {
                return r.showUserTable(electionID, tableID);
            } catch (RemoteException e) {
                System.out.println("[Warning] Failed to use RMI showTables. Retrying connection");
                connectToRMI();
            }
        }
    }

    static String checkCC(int cc) {
        //return null if not found. Return name otherwise!
        while (true) {
            try {
                return r.checkID(cc, electionID);
            } catch (RemoteException e) {
                System.out.println("[Warning] Failed to use RMI checkID. Retrying connection");
                connectToRMI();
            }
        }
    }

    static boolean checkLoginInfo(int cc, String username, String password) {
        while (true) {
            try {
                return r.checkLogin(cc, username, password);
            } catch (RemoteException e) {
                System.out.println("[Warning] Failed to use RMI checkLogin. Retrying connection");
                connectToRMI();
            }
        }
    }

    static ArrayList<String> requestCandidatesList() {
        while (true) {
            try {
                return r.viewListsFromElection(electionID);
            } catch (RemoteException e) {
                System.out.println("[Warning] Failed to use RMI viewListsFromElection. Retrying connection");
                connectToRMI();
            }
        }
    }

    static String registerVote(int cc, String candidateName) {
        //TODO send to RMI along with electionID (static), tableID (static)
        //receive voted list (name, null or white)
        while (true) {
            try {
                return r.vote(cc, candidateName, electionID, tableID);
            } catch (RemoteException e) {
                System.out.println("[Warning] Failed to use RMI vote. Retrying connection");
                connectToRMI();
            }
        }
    }

    static int readInt() {
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

    static void enterToContinue() {
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
        //type|i1|i2|s1|s2|list[0]|list[1]|list[2]|...

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

        switch (m.getType()) {
            case 0:
                if (TCPServer.checkLoginInfo(cc, m.getS1(), m.getS2())) {
                    out.println("Login successful.");
                    isLogged = true;
                    String result = "";
                    ArrayList<String> candidateList = TCPServer.requestCandidatesList();
                    for (String candidate : candidateList) {
                        result = String.format("%s\t%s\n", result, candidate);
                    }
                    result = result.concat("Pick your candidate\nUsage: 1|0|0|[Candidate's name]|0\n");
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
                if (aux != null) {
                    out.printf("Vote for candidate %s registered successfully.\n", aux);
                    blockTerminal();
                } else {
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
        out.printf("This terminal has been unlocked for %s (CC: %d). Timeout will occur if inactive for 120 seconds\nPlease login:\nUsage: 0|0|0|[username]|[password]\n", name, cc);
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
    private static final int TIMEOUT_VALUE = 120000;
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