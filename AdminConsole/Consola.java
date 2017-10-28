//TODO: horas nas eleiçoes
//TODO: mudar nomes
//TODO: next();
//TODO: datas proteção

import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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


    public Consola() throws InterruptedException {
        ACConfigLoader configs = new ACConfigLoader();
        rmiName = configs.getRmiName();
        this.connectRMI();
        this.adminConsoleMenu();
    }

    public static void main(String args[]) throws InterruptedException {
        Consola admin = new Consola();
        admin.exit();
    }

    private void connectRMI() {
        checker = new Thread(() -> {
            while (flag) {
                try {
                    r = (ServerInterface) Naming.lookup(Consola.rmiName);

                } catch (NotBoundException e) {
                    System.out.println("Not bound");
                } catch (RemoteException e) {
                    //System.out.println("remote");
                } catch (MalformedURLException e) {
                    System.out.println("malformed");
                }
            }
        });
        checker.start();
    }

    private static int readInt() {
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

    private static int toInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nf) {
            return -1;
        }
    }

    private void adminConsoleMenu() throws InterruptedException {
        int option = -1;
        Scanner sc = new Scanner(System.in);
        boolean isConnected = true;
        Thread.sleep(500);
        while (option != 0) {

            int trys = 0;
            if (!isConnected || r == null) {
                System.out.println("Connection down, trying to connect...");
            }

            while (r == null && trys < 10) {
                Thread.sleep(500);
                if (r != null) {
                    System.out.println("...Connected!\n");
                }
                trys++;
            }

            while (!isConnected && trys < 10) {
                Thread.sleep(500);
                try {
                    boolean flag = r.isConnected();
                    if (flag) {
                        isConnected = true;
                        System.out.println("...Connected!\n");
                        break;
                    }
                } catch (RemoteException ignored) {
                }
                trys++;
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

                option = readInt();
                if (option < 0 || option > 13) {
                    System.out.println("Insira um valor válido, çá xabor.\n");
                }
            } while (option < 0 || option > 13);

            try {
                switch (option) {
                    case 1:
                        addPerson();
                        break;

                    case 2:
                        manageDepFac();
                        break;

                    case 3:
                        createElection();
                        break;

                    case 4:
                        manageLists();
                        break;

                    case 5:
                        manageTables();
                        break;

                    case 6:
                        editElections();
                        break;

                    case 7:
                        checkVoteLocal();
                        break;

                    case 8:

                    case 9:

                    case 10:
                        checkPastElections();
                        break;

                    case 11:
                        anticipatedVote();
                        break;

                    case 12:
                        editPerson();
                        break;

                    case 13:
                        manageTablePersonal();
                        break;
                }
            } catch (RemoteException re) {
                isConnected = false;
            }
        }
        flag = false;
    }

    private void editPerson() throws RemoteException {
        Scanner sc = new Scanner(System.in);
        int operation, ccn;
        String aux, newString = null;

        System.out.printf("Introduza o número de cartão de cidadão!");
        ccn = getPhoneOrCCN(2);

        do {
            System.out.println("O que quer editar? ");
            System.out.println("1-Nome");
            System.out.println("2-Morada");
            System.out.println("3-Telemóvel");
            System.out.println("4-Número de cartão de cidadão");
            System.out.println("5-Validade do cartão de cidadão");
            System.out.println("6-Departamento");
            System.out.println("7-Faculdade");
            System.out.println("8-Palavra-Pass");
            System.out.printf("Opção: ");
            operation = readInt();
            if (operation <= 0 || operation > 8) {
                System.out.println("Insira um valor válido, çá xabor.\n");
            }
        } while (operation <= 0 || operation > 8);

        switch(operation){
            case 1:
                System.out.println("Novo nome do utilizador: ");
                newString = sc.nextLine();
                break;
            case 2:
                System.out.println("Nova morada do utilizador: ");
                newString = sc.nextLine();
                break;
            case 3:
                System.out.println("Novo contacto do utilizador: ");
                newString = String.valueOf(getPhoneOrCCN(1));
                break;

            case 4:
                System.out.println("Novo número de cartão de cidadão: ");
                newString = String.valueOf(getPhoneOrCCN(1));
                break;

            case 5:
                System.out.println("Nova validade do cartão de cidadão: ");
                newString = sc.nextLine();
                break;

            case 6:
                System.out.println("Novo departamento do utilizador: ");
                newString=String.valueOf(getDepOrFacId(1));
                break;
            case 7:
                System.out.println("Novo departamento do utilizador: ");
                newString=String.valueOf(getDepOrFacId(2));
                break;
            case 8:
                System.out.println("Nova pass do utilizador: ");
                newString = sc.nextLine();
                break;
        }

        if(r.editPerson(ccn, newString, operation)){
            System.out.println("Sucesso!");
        }else{
            System.out.println("Erro!");
        }
    }

    private void anticipatedVote() throws RemoteException{
        Scanner sc = new Scanner(System.in);
        int elecId = pickElections(2);
        if(elecId==-1){
            System.out.println("Erro!");
            return;
        }
        String voto;
        int ccn;

        System.out.printf("Introduza o número de cartão de cidadão!");
        ccn = getPhoneOrCCN(2);

        System.out.println("Introduza a palavra pass: ");
        String pass = sc.nextLine();

        voto = pickListFromElection(elecId, 2);
        if(voto.equals("")){
            System.out.println("Erro!");
            return;
        }

        if(r.anticipatedVote(elecId, ccn, vote, pass)){
            System.out.println("Sucesso!");
        }else{
            System.out.println("Erro!");
        }
    }

    private void checkPastElections() throws RemoteException{
        int elecId = pickElections(3);
        if(elecId==-1){
            System.out.println("Erro!");
            return;
        }
        ArrayList<ArrayList<String>> results = r.checkResults(elecId);
        if(results.get(1).size()==0){
            System.out.println("Não existem resultados!");
            return;
        }
        System.out.println("Resultados: ");
        for(int i = 0; i<results.get(0).size(); i++){
            System.out.println("-> " + results.get(0).get(i) + " - nº de votos - " + results.get(1).get(i));
        }
    }

    private void checkVoteLocal() throws RemoteException{
        int elecID = pickElections(4);
        if(elecID==-1){
            System.out.println("Erro!");
            return;
        }

        int ccn;
        System.out.printf("Introduza o número de cartão de cidadão!");
        ccn = getPhoneOrCCN(2);

        if(r.checkTable(ccn,elecID)==-1){
            System.out.println("Erro!");
        }else{
            System.out.println("Votou na mesa " + r.checkTable(ccn,elecID));
        }

    }

    private void editElections() throws RemoteException{
        int operation;
        Scanner sc = new Scanner(System.in);
        int elecId = pickElections(2);
        if(elecId==-1){
            System.out.println("Erro!");
            return;
        }
        String newInput;
        boolean verify = false;

        do {
            System.out.println("O que quer editar?");
            System.out.println("1-Titulo");
            System.out.println("2-Descrição");
            System.out.println("3-Hora de início");
            System.out.println("3-Hora de fim");
            System.out.printf("Opção: ");
            operation = readInt();
            if (operation <= 0 || operation > 2) {
                System.out.println("Insira um valor válido, çá xabor.\n");
            }
        } while (operation <= 0 || operation > 2);

        switch (operation){
            case 1:
                System.out.printf("Insira o novo titulo: ");
                newInput = sc.nextLine();
                verify = r.changeElectionsText(elecId, newInput, 1);
                break;
            case 2:
                System.out.printf("Insira a nova descrição: ");
                newInput = sc.nextLine();
                verify = r.changeElectionsText(elecId, newInput, 2);
                break;
            case 3:
                System.out.printf("Insira a nova data de início: ");
                newInput = sc.nextLine();
                verify = r.changeElectionsDates(elecId, newInput, 1);
                break;
            case 4:
                System.out.printf("Insira a nova data de fim: ");
                newInput = sc.nextLine();
                verify = r.changeElectionsDates(elecId, newInput, 2);
                break;
        }

        if(verify){
            System.out.println("Sucesso!");
        }else{
            System.out.println("Erro!");
        }
    }

    private void manageTables() throws RemoteException{
        int operation;
        boolean verify = false;

        do {
            System.out.println("Deseja adicionar ou remover uma mesa? ");
            System.out.println("1-Adicionar");
            System.out.println("2-Remover");
            System.out.printf("Opção: ");
            operation = readInt();
            if (operation <= 0 || operation > 2) {
                System.out.println("Insira um valor válido, çá xabor.\n");
            }
        } while (operation <= 0 || operation > 2);


        int elecID =  pickElections(2);
        if(elecID==-1){
            System.out.println("Erro!");
            return;
        }

        switch (operation){
            case 1:
                System.out.println("Associe um departamento à mesa!");
                int idDep = getDepOrFacId(1);
                if(idDep==-1){
                    System.out.println("Erro!");
                    return;
                }
                verify = r.addTableToElection(elecID, idDep);
                break;
            case 2:
                int table = pickTableFromElection(elecID);
                if(table!=-1){
                    verify = r.removeTableFromElection(elecID, table);
                }
                break;
        }

        if(verify) {
            System.out.println("Sucesso!");
        }else{
            System.out.println("Erro!");
        }
    }

    private void manageTablePersonal() throws RemoteException{
        int operation;
        boolean verify = false;
        int userID, tableID;

        int elecID =  pickElections(2);
        tableID = pickTableFromElection(elecID); 
        userID = pickPersonFromTable(elecID,tableID); 

        if(verify) {
            System.out.println("Sucesso!");
        }else{
            System.out.println("Erro!");
        }
    }

    private int pickPersonFromTable(int elecID, int mesavoto_id) throws RemoteException{

        Scanner sc = new Scanner(System.in);
        ArrayList<ArrayList<String>> usersList;
        mesavoto_id = pickTableFromElection(elecID);
        usersList = r.showUserTable(elecID, mesavoto_id);
        
        ArrayList<String> tableIDList = usersList.get(0);
        ArrayList<String> tableNameList = usersList.get(1);
        boolean flag = true;
        int option = 0;

        if(tableIDList.size()==0){
            System.out.println("Não existe nenhuma mesa.");
            return -1;
        }
        while(flag) {
            flag=false;
            System.out.println("Qual o utilizador?");
            for (int i = 0; i < tableIDList.size(); i++) {
                    System.out.printf(i+1 + " - " + tableNameList.get(i));
            }

            option = readInt();
            if(option <= 0 || option > tableIDList.size()-1){
                System.out.println("Opção Inválida.");
                flag = true;
            }
        }

        return toInt(tableIDList.get(option-1));
    
    }

    private void manageLists() throws RemoteException{
        Scanner sc = new Scanner(System.in);

        int electionId = pickElections(1);
        if(electionId==-1){
            System.out.println("Erro!");
            return;
        }

        int operation;
        int listType=0;
        String list;

        do {
            System.out.println("Que operação quer realizar? ");
            System.out.println("1-Adicionar");
            System.out.println("2-Remover");
            System.out.printf("Opção: ");
            operation = readInt();
            if (operation <= 0 || operation > 2) {
                System.out.println("Insira um valor válido, çá xabor.\n");
            }
        } while (operation <= 0 || operation > 2);

        if (operation == 1){
            System.out.println("Nome da nova lista candidata: ");
            list = sc.nextLine();
            System.out.println("Tipo da nova Lista candidata: ");
            do {
              System.out.println("Tipo da Lista:");
              System.out.println("1-Estudantes");
              System.out.println("2-Docentes");
              System.out.println("3-Funcionários");
              System.out.printf("Opção: ");
              listType = readInt();
              if (listType <= 0 || listType > 3) {
                System.out.println("Insira um valor válido, çá xabor.\n");
              }
            } while (listType <= 0 || listType > 3);

        }
        else {
            list = pickListFromElection(electionId,1);
            if(list.equals("")){
                System.out.println("Erro");
                return;
            }
        }

        if(r.manageList(electionId, listType, list, operation)) {
            System.out.println("Sucesso!");
        }else{
            System.out.println("Erro!");
        }
    }

    private int pickTableFromElection(int elecID) throws RemoteException{
        ArrayList<String> tableList;
        tableList = r.showTables(elecID);
        boolean flag = true;
        int option = 0;
        String table;

        if(tableList.size()==0){
            System.out.println("Não existe nenhuma mesa.");
            return -1;
        }
        while(flag) {
            flag=false;
            System.out.println("Qual mesa de voto?");
            for (int i = 0; i < tableList.size(); i++) {
                System.out.println( i+1 + " -> " + tableList.get(i));
            }
            option = readInt();
            if(option <= 0 || option > tableList.size()-1){
                System.out.println("Opção Inválida.");
                flag = true;
            }
        }

        return toInt(tableList.get(option-1));
    }

    private String pickListFromElection(int elecID, int type) throws RemoteException{

        ArrayList<String> listsList;
        if(type==1){
            listsList = r.printListsFromElection(elecID);

        }else{
            listsList = r.viewListsFromElection(elecID);
        }
        boolean flag = true;
        int option = 0;

        if(listsList.size() ==0){
            System.out.println("Não existe nenhuma lista nesta eleição.");
            return "";
        }
        while(flag) {
            flag=false;
            System.out.println("Qual a lista?");
            for (int i = 0; i < listsList.size(); i++) {
                System.out.println(i + 1 + " -> " + listsList.get(i));
            }

            option = readInt();
            if(option<=0 || option > listsList.size()-1){
                flag = true;
            }
        }

        return listsList.get(option-1);
    }

    private int pickElections(int type) throws RemoteException{
        boolean flag = true;
        int option = 0;
        ArrayList<ArrayList<String>> electionsList;
        if(type==1) {
            electionsList = r.viewCurrentElections();
        }if(type==2){
            electionsList = r.viewFutureElections();

        }if(type==3){
            electionsList= r.viewPastElections();

        }else{
            electionsList = r.viewPastCurrentElections();
        }

        ArrayList<String> idList = electionsList.get(0);
        ArrayList<String> titleList = electionsList.get(1);

        if(idList.size()==0){
            System.out.println("Não existem eleições a apresentar!");
            return -1;
        }

        while(flag) {
            flag=false;
            System.out.println("Qual eleição?");
            for (int i = 0; i < idList.size(); i++) {
                System.out.println(i + 1 + " -> " + idList.get(i) + " - " + titleList.get(i));
            }
            System.out.printf("Opção: ");
            option = readInt();
            if(option<=0 || option > idList.size()-1){
                flag = true;
            }
        }
        return toInt(idList.get(option-1));

    }

    private void createElection() throws RemoteException{
        Scanner sc = new Scanner(System.in);
        int electionType, id;
        String beginning, end, title, desc;
        boolean verify = false;

        do {
            System.out.println("Que tipo de eleição quer criar? ");
            System.out.println("1-Núcleo de Estudantes");
            System.out.println("2-Concelho Geral");
            System.out.println("3-Direção de Departamento");
            System.out.println("4-Direção de Faculdade");
            System.out.printf("Opção: ");
            electionType = readInt();
            if (electionType <= 0 || electionType > 4) {
                System.out.println("Insira um valor válido, çá xabor.\n");
            }
        } while (electionType <= 0 || electionType > 4);

        System.out.println("Data e hora de início: ");
        beginning = sc.nextLine();
        System.out.println("Data e hora de fim: ");
        end = sc.nextLine();
        System.out.println("Titulo:");
        title = sc.nextLine();
        System.out.println("Descrição:");
        desc = sc.nextLine();

        switch (electionType){
            case 1:
                id = getDepOrFacId(1);
                if(id==-1){
                    System.out.println("Erro!");
                    return;
                }
                verify = r.criaEleiçãoNE(beginning, end, title, desc, id);
                break;
            case 2:
                verify = r.criaEleiçãoCG(beginning, end, title, desc);
                break;
            case 3:
                id = getDepOrFacId(1);
                if(id==-1){
                    System.out.println("Erro!");
                    return;
                }
                verify = r.criaEleiçãoDD(beginning, end, title, desc, id);
                break;
            case 4:
                id = getDepOrFacId(2);
                if(id==-1){
                    System.out.println("Erro!");
                    return;
                }
                verify = r.criaEleiçãoDF(beginning, end, title, desc, id);
                break;
        }

        if(verify){
            System.out.println("Sucesso!");
        }else{
            System.out.println("Erro!");
        }

    }

    private void manageDepFac() throws RemoteException{
        Scanner sc = new Scanner(System.in);
        int operation, target, id=0;
        String name;
        boolean verify = false;

        do {
            System.out.println("Que operação quer realizar? ");
            System.out.println("1-Adicionar");
            System.out.println("2-Remover");
            System.out.println("3-Editar");
            System.out.printf("Opção: ");
            operation = readInt();
            if (operation <= 0 || operation > 3) {
                System.out.println("Insira um valor válido, çá xabor.\n");
            }
        } while (operation <= 0 || operation > 3);

        do {
            System.out.println("Sobre quê? ");
            System.out.println("1-Departamentos");
            System.out.println("2-Faculdades");
            System.out.printf("Opção: ");
            target = readInt();
            if (target <= 0 || target > 2) {
                System.out.println("Insira um valor válido, çá xabor.\n");
            }
        } while (target <= 0 || target > 2);

        switch (operation){
            case 1:
              if (target == 1){
                id = getDepOrFacId(2);
                  if(id==-1){
                      System.out.println("Erro!");
                      return;
                  }
                System.out.printf("Insira o nome do novo departamento: ");
              }
              else if (target == 2){
                id = 0;
                System.out.printf("Insira o nome da nova faculdade: ");
              }
                name = sc.nextLine();
                verify = r.addDepFac(id, name, target);
                break;
            case 2:
                id = getDepOrFacId(1);
                if(id==-1){
                    System.out.println("Erro!");
                    return;
                }
                verify = r.rmDepFac(id, target);
                break;
            case 3:
                id = getDepOrFacId(1);
                if(id==-1){
                    System.out.println("Erro!");
                    return;
                }
                System.out.print("Insira o novo nome do departamento: ");
                name = sc.nextLine();
                verify = r.editDepFac(id, name, target);
                break;
        }

        if(verify){
            System.out.println("Sucesso!");
        }else{
            System.out.println("Erro!");
        }
    }

    private void addPerson() throws RemoteException {
        Scanner sc = new Scanner(System.in);
        int option, phone, ccn, iddep, idfac;
        boolean flag = true;
        String address, pass, aux, nome;
        String ccv;
        do {
            System.out.println("Que pessoa quer adicionar?");
            System.out.println("1-Aluno");
            System.out.println("2-Docente");
            System.out.println("3-Funcionário");
            System.out.printf("Opção: ");
            option = readInt();
            if (option <= 0 || option > 3) {
                System.out.println("Insira um valor válido, çá xabor.\n");
            }
        } while (option < 0 || option > 3);
        System.out.printf("Introduza o contacto telefónico!");
        phone = getPhoneOrCCN(1);
        System.out.printf("Introduza o número de cartão de cidadão!");
        ccn = getPhoneOrCCN(2);
        iddep = getDepOrFacId(1);
        if(iddep==-1){
            System.out.println("Erro!");
            return;
        }
        idfac = getDepOrFacId(2);
        if(idfac==-1){
            System.out.println("Erro!");
            return;
        }
        System.out.printf("Introduza a morada da pessoa: ");
        address = sc.nextLine();
        System.out.printf("Introduza o nome da pessoa: ");
        nome = sc.nextLine();
        System.out.printf("Introduza a password: ");
        pass = sc.nextLine();
        System.out.printf("Validade do Cartão de Cidadão: ");
        ccv = sc.nextLine();

        if(r.addPerson(nome, address, phone, ccn, ccv, iddep, idfac, pass, option)){
            System.out.println("Sucesso!");
        }else{
            System.out.println("Erro!");
        }


    }

    private java.sql.Date getData(){
        java.sql.Date data;
        while(true){
            String Data = getsDate();
            try {
                data= convertToSQLDate(Data);
                break;
            } catch (ParseException e) {
                System.out.println("Data Inválida");
            }
        }
        return data;
    }

    private int getDepOrFacId(int type) throws RemoteException{
        Scanner sc = new Scanner(System.in);
        ArrayList<String> listId;
        ArrayList<String> listNome;
        ArrayList<ArrayList<String>> globalList;

        int option;
        if(type==1) {
            globalList = r.verDepartamentos();
            listId = globalList.get(0);
            listNome = globalList.get(1);
            System.out.println("Qual o departamento?");

        }else{
            listId = r.verFaculdades().get(0);
            listNome = r.verFaculdades().get(1);
            System.out.println("Qual a Faculdade?");

        }
        if(listId.size()==0){
            System.out.println("-> Nada a apresentar!");
            return -1;
        }

        do {
            for(int i = 0; i<listId.size(); i++){
                System.out.println(i+1 + " - " + listNome.get(i));
            }

            System.out.printf("Opção: ");
            option = readInt();
            if (option <= 0 || option > listId.size()-1) {
                System.out.println("Insira um valor válido, çá xabor.\n");
            }
        } while (option <= 0 || option > listId.size()-1);

        return toInt(listId.get(option-1));
    }

    private int getPhoneOrCCN(int type) throws RemoteException{
        Scanner sc = new Scanner(System.in);
        int phoneOrCCN = 0;
        do {
            System.out.printf(" -> ");
            flag = true;
            String aux = sc.nextLine();
            if ((type == 1 && aux.length() != 9) || (type == 2 && aux.length() != 8)) {
                flag = false;
                System.out.println("O número de digitos incorrecto!");
            } else {
                phoneOrCCN = toInt(aux);
                if (phoneOrCCN == -1) {
                    flag = false;
                    System.out.println("Input inválido!");
                }
            }
        } while (!flag);
        return phoneOrCCN;
    }

    private String getsDate() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Insira a data no seguinte formato -> dd-MM-yyyy");
        return sc.next();
    }

    private java.sql.Date convertToSQLDate(String startDate) throws ParseException {
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
        java.util.Date date = sdf1.parse(startDate);
        java.sql.Date sqlStartDate = new java.sql.Date(date.getTime());
        return sqlStartDate;
    }

    private void exit() throws InterruptedException {
        checker.join();
        System.out.println("...closing...");
    }

}