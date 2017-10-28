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
        int connectionCount = 0;
        int choice;
        TCPConfigLoader c = new TCPConfigLoader();
        serverPort = c.getTCPPort();
        rmiName = c.getRMIName();

        while (!connectToRMI());

		//request elections list and ask for choice
        while(true) {
            ArrayList<ArrayList<String>> electionData = requestElectionsList();
            if(electionData==null) continue;
            System.out.println(electionData);
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

		//request list of voting tables of picked election and ask for choice
        while (true) {
            ArrayList<String> tableIDList = requestTableList();
            if(tableIDList==null) continue;
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

		//request picked table's staff list and ask for choice + login
        while (true) {
            ArrayList<ArrayList<String>> staffData = requestTableStaff();
            if(staffData==null) continue;
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
                        System.out.println("Login failed!");
                        enterToContinue();
                    }
                } else {
                    System.out.println("No staff has such CC!");
                    enterToContinue();
                }
            }
            break;
        }
		
		//Open a new thread for administration commands (checking CCs and unblocking terminals)
        new AdminCommands();

		//Accept all new connections, store them in a list, and create a thread to handle them
        try {
            System.out.println("[STATUS] Listening to port" + serverPort);
            ServerSocket listenSocket = new ServerSocket(serverPort);
            System.out.println("[STATUS] LISTEN SOCKET=" + listenSocket);
            while (true) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("[STATUS] CLIENT_SOCKET (created at accept())=" + clientSocket);
                connectionCount++;
                connectionList.add(new Connection(clientSocket, connectionCount));
            }
        } catch (IOException e) {
            System.out.println("Listen:" + e.getMessage());
        }
    }

    private static boolean connectToRMI() {
		//try to connect for 30 seconds, stop trying after that
        long timestamp = System.currentTimeMillis();
        boolean failed = false;
        while (true) {
            try {
                r = (ServerInterface) Naming.lookup(rmiName);
                return true;
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                if (System.currentTimeMillis() - timestamp > 30000) {
                    System.out.println("[Warning] Couldn't connect to RMI after 30s.");
                    return false;
                } else if (!failed) {
                    System.out.println("[Warning] Error connecting to RMI. Trying to reconnect...");
                    failed = true;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    return false;
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
                if(!connectToRMI()) return null;
            }
        }


    }

    private static ArrayList<String> requestTableList() {
        while (true) {
            try {
                return r.showTables(electionID).get(0);
            } catch (RemoteException e) {
                System.out.println("[Warning] Failed to use RMI showTables. Retrying connection");
                if(!connectToRMI()) return null;
            }
        }
    }

    private static ArrayList<ArrayList<String>> requestTableStaff(){
        while (true) {
            try {
                return r.showUserTable(electionID, tableID);
            } catch (RemoteException e) {
                System.out.println("[Warning] Failed to use RMI showTables. Retrying connection");
                if(!connectToRMI()) return null;
            }
        }
    }

    static String checkCC(int cc) {
        //returns null if not found/not allowed. Returns name otherwise!
        while (true) {
            try {
                return r.checkID(cc, electionID);
            } catch (RemoteException e) {
                System.out.println("[Warning] Failed to use RMI checkID. Retrying connection");
                if(!connectToRMI()) return null;
            }
        }
    }

    static boolean checkLoginInfo(int cc, String username, String password) {
        while (true) {
            try {
                return r.checkLogin(cc, username, password);
            } catch (RemoteException e) {
                System.out.println("[Warning] Failed to use RMI checkLogin. Retrying connection");
                if(!connectToRMI()) return false;
            }
        }
    }

    static ArrayList<String> requestCandidatesList() {
        while (true) {
            try {
                return r.viewListsFromElection(electionID);
            } catch (RemoteException e) {
                System.out.println("[Warning] Failed to use RMI viewListsFromElection. Retrying connection");
                if(!connectToRMI()) return null;
            }
        }
    }

    static String registerVote(int cc, String candidateName) {
        while (true) {
            try {
                return r.vote(cc, candidateName, electionID, tableID);
            } catch (RemoteException e) {
                System.out.println("[Warning] Failed to use RMI vote. Retrying connection");
                if(!connectToRMI()) return null;
            }
        }
    }

    static int readInt() {
		//keeps asking for input until it receives an int
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
		//Halts the program until enter is pressed
        System.out.println("Press enter to continue...");
        Scanner sc = new Scanner(System.in);
        sc.nextLine();
        return;
    }
}

//Starts a thread to handle each new connection
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
            out.println(new Message(0, TCPServer.tableID, terminalID, null, null, null));
            out.printf("[Pretty Print] Connection with table #%d successful! You are voting terminal #%d\n", TCPServer.tableID, terminalID);
            this.start();
        } catch (IOException e) {
            endConnection();
        }
    }

	//the thread is always blocked waiting for a new message from the client. If connection is broken, the thread is closed
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

	//as soon as a message is received it is interpreted and then answered
    private void answerMessage(String s) {
        //type|i1|i2|s1|s2|list[0]|list[1]|list[2]|...

        if (isBlocked) {
            out.println(new Message(2, 0, 0, null, null, null));
            out.println("[Pretty Print] Terminal is blocked. Message ignored.");
            return;
        }

		//notifies timer thread of new activity
        synchronized (recentActivity) {
            recentActivity.set(true);
            recentActivity.notify();
        }

        Message m = new Message(s);
        if (!m.getIsValid()) {
            out.println(new Message(1, 0, 0, null, null, null));
            out.println("[Pretty Print] Message not valid!");
            return;
        }

        switch (m.getType()) {
            case 0:
                if (TCPServer.checkLoginInfo(cc, m.getS1(), m.getS2())) {
                    out.println(new Message(0, 0, 0, null, null, null));
                    out.println("[Pretty Print] Login successful.");
                    ArrayList<String> candidateList = TCPServer.requestCandidatesList();
                    if (candidateList == null){
                        out.println(new Message(1, 0, 0, null, null, null));
                        out.println("[Pretty Print] Failed to get candidate list");
                        break;
                    }
                    out.println(new Message(4, 0, 0, null, null, candidateList));
                    isLogged = true;
                    String result = "[Pretty Print]\n";
                    for (String candidate : candidateList) {
                        result = String.format("%s\t%s\n", result, candidate);
                    }
                    result = result.concat("Pick your candidate\nUsage: 1|0|0|[Candidate's name]|0\n");
                    out.println(result);
                } else {
                    out.println(new Message(1, 0, 0, null, null, null));
                    out.println("[Pretty Print] Login failed");
                }
                break;
            case 1:
                if (!isLogged) {
                    out.println(new Message(3, 0, 0, null, null, null));
                    out.println("[Pretty Print] You can't vote yet! Login first.");
                    break;
                }
                String aux = TCPServer.registerVote(cc, m.getS1());
                if (aux != null) {
                    out.println(new Message(0, 0, 0, aux, null, null));
                    out.printf("[Pretty Print] %s vote registered successfully.\n", aux);
                    blockTerminal();
                } else {
                    out.println(new Message(1, 0, 0, null, null, null));
                    out.println("[Pretty Print] An error happened while voting. Please try to vote again.");
                    break;
                }
                break;
            default:
                out.println(new Message(1, 0, 0, null, null, null));
                out.println("[Pretty Print] Message type non-existent!");
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

        out.println(new Message(2, 0, 0, null, null, null));
        out.println("[Pretty Print] This terminal has been blocked.");
        return true;
    }

    public synchronized boolean unblockTerminal(int voterCC, String voterName) {
        isBlocked = false;
        cc = voterCC;
        name = voterName;
        recentActivity.set(false);
        timer = new TimeoutTimer(this);
        System.out.printf("Unblocked terminal #%d. Timeout will occur if inactive for 120 seconds\n", terminalID);
        out.println(new Message(5, 0, 0, null, null, null));
        out.printf("[Pretty Print] This terminal has been unlocked for %s (CC: %d). Timeout will occur if inactive for 120 seconds\nPlease login:\nUsage: 0|0|0|[username]|[password]\n", name, cc);
        return true;
    }

	//called when the TCP connection is broken. Ends the thread gracefully and any possibly existing timer threads.
    private void endConnection() {
        TCPServer.connectionList.remove(this);
        if (timer != null) timer.interrupt();
        System.out.printf("[Warning] Connection with terminal #%d was closed. Removed from the list of terminals\n", terminalID);
    }

}

//A thread for keeping track of message activity. Blocks the terminal if inactive for too long
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

//thread for adming commands
class AdminCommands extends Thread {
    AdminCommands() {
        this.start();
    }

    @Override
    public void run() {
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
                    System.out.println("CC confirmation failed (probably not in database or not allowed to vote)");
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
