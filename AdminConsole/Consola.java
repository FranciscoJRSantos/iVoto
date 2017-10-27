import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;


public class Consola {
    private static final long serialVersionUID = 1L;
    private static String rmiName;
    static ServerInterface r;
    boolean flag = true;
    Thread checker;


    public Consola() throws RemoteException, InterruptedException {
        ACConfigLoader configs = new ACConfigLoader();
        rmiName = configs.getRmiName();
        try {
            this.connectRMI();
        } catch (MalformedURLException | NotBoundException ignored) {
        }
        this.adminConsoleMenu();

    }

    public static void main(String args[]) throws RemoteException, NotBoundException, MalformedURLException, InterruptedException {
        Consola admin = new Consola();
        admin.exit();
    }

    private void connectRMI() throws RemoteException, NotBoundException, MalformedURLException {
        checker = new Thread(() -> {
            while (flag) {
                try {
                    r = (ServerInterface) Naming.lookup(Consola.rmiName);

                } catch (NotBoundException | RemoteException | MalformedURLException nbe) {
                }
            }
        });
        checker.start();
    }

    public static int toInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException var3) {
            return -1;
        }
    }

    private void adminConsoleMenu() throws RemoteException, InterruptedException {
        int option = -1;
        Scanner sc = new Scanner(System.in);
        boolean isConnected = true;
        Thread.sleep(500);
        while (option != 0) {
            int trys = 0;
            if (isConnected == false || r == null) {
                System.out.println("Connection down, trying to connect...");
            }
            while (r == null && trys < 10) {

                Thread.sleep(500);
                if (r != null) {
                    System.out.println("...Connected!\n");
                }
            }
            while (isConnected == false && trys < 10) {
                trys++;
                Thread.sleep(500);
                try {
                    boolean flag = r.isConnected();
                    if (flag == true) {
                        isConnected = true;
                        System.out.println("...Connected!\n");
                        break;
                    }
                } catch (RemoteException re) {
                }
            }
            if (trys == 10) {
                System.out.println("Connection failed... Try Again.\n");
            }

            do {
                System.out.println("----------MENU----------");
                System.out.println("1-Registar Pessoa");
                System.out.println("2-Gerir de Darpartamentos e Faculdades");
                System.out.println("3-Criar Eleição");
                System.out.println("4-Gerir Lista de Candidatos");
                System.out.println("5-Gerir Mesas de Voto");
                System.out.println("6-Alterar Propriedades de Eleição");
                System.out.println("7-Consulta de Local de Voto");
                System.out.println("8-Estado das Mesas de Voto");
                System.out.println("9-Consulta dos eleitores (tempo real)");
                System.out.println("10-Consultar Eleições Passadas");
                System.out.println("11-Voto Antecipado");
                System.out.println("12-Alterar Dados Pessoais");
                System.out.println("13-Gerir Membros das Mesas de Voto");
                System.out.println("0-Sair\n");
                System.out.printf("Opção: ");
                String aux = sc.next();
                option = toInt(aux);
                if (option < 0 || option > 5) {
                    System.out.println("Insira um valor válido, çá xabor.\n");
                }
            } while (option < 0 || option > 5);
            try {
                boolean teste = r.isConnected();
            } catch (RemoteException re) {
                isConnected = false;
            }

        }
        flag = false;
    }

    public String getsDate() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Insira a data no seguinte formato -> dd-MM-yyyy");
        return sc.next();
    }


    public java.sql.Date convertToSQLDate(String startDate) throws ParseException {
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
        java.util.Date date = sdf1.parse(startDate);
        java.sql.Date sqlStartDate = new java.sql.Date(date.getTime());
        return sqlStartDate;
    }

    public void exit() throws InterruptedException {
        checker.join();
        System.out.println("...closing...");
    }

}
