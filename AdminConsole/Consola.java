import java.rmi.RemoteException;
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


    public Consola() {
        ACConfigLoader configs = new ACConfigLoader();
        rmiName = configs.getRmiName();
        this.connectRMI();
        this.adminConsoleMenu();
    }

    public static void main(String args[]) {
        Consola admin = new Consola();
        admin.exit();
    }

    //função que cria thread responsável pela conecção ao rmi e mantimento da mesma
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

    //garante que lê um inteiro
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

    //String para Int retornando -1 em caso de NumberFormatException
    private static int toInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nf) {
            return -1;
        }
    }

    //função do menu principal que está sempre a correr e a garantir que a consola se mantem up mesmo que a conecção vá a baixo.
    private void adminConsoleMenu() {
        int option = -1;
        Scanner sc = new Scanner(System.in);
        boolean isConnected = true;

        try {
            Thread.sleep(500);
        } catch (InterruptedException ignore) {
        }

        while (option != 0) {

            int trys = 0;
            if (!isConnected || r == null) {
                System.out.println("Connection down, trying to connect...");
            }

            while (r == null && trys < 10) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignore) {
                }
                if (r != null) {
                    System.out.println("...Connected!\n");
                }
                trys++;
            }

            while (!isConnected && trys < 10) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignore) {
                }

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
                System.out.println("8-Consultar Eleições Passadas");
                System.out.println("9-Voto Antecipado");
                System.out.println("10-Alterar Dados Pessoais");
                System.out.println("11-Gerir Membros das Mesas de Voto");
                System.out.println("0-Sair\n");
                System.out.printf("Opção: ");

                option = readInt();
                if (option < 0 || option > 13) {
                    System.out.println("Insira um valor válido, por favor.\n");
                }
            } while (option < 0 || option > 11);

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
                        checkPastElections();
                        break;

                    case 9:
                        anticipatedVote();
                        break;

                    case 10:
                        editPerson();
                        break;

                    case 11:
                        manageTablePersonal();
                        break;
                }
            } catch (RemoteException re) {
                isConnected = false;
            }
        }
        flag = false;
    }

    //editar atributos de uma pessoa existente
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
            System.out.println("6-Unidade Organica");
            System.out.println("7-Palavra-Pass");
            System.out.printf("Opção: ");
            operation = readInt();
            if (operation <= 0 || operation > 7) {
                System.out.println("Insira um valor válido, por favor.\n");
            }
        } while (operation <= 0 || operation > 7);

        switch (operation) {
            case 1:
                while (true) {
                    System.out.printf("Novo nome do utilizador:\n-> ");
                    newString = sc.nextLine();
                    if (newString.contains("|")) {
                        System.out.println("Não pode conter o caracter '|'.");
                    } else {
                        break;
                    }
                }
                break;
            case 2:
                System.out.printf("Nova morada do utilizador:\n-> ");
                newString = sc.nextLine();
                break;
            case 3:
                System.out.println("Novo contacto do utilizador: ");
                newString = String.valueOf(getPhoneOrCCN(1));
                break;

            case 4:
                System.out.println("Novo número de cartão de cidadão: ");
                newString = String.valueOf(getPhoneOrCCN(2));
                break;

            case 5:
                System.out.printf("Nova validade do cartão de cidadão:\n-> ");
                newString = sc.nextLine();
                break;

            case 6:
                System.out.printf("Nova unidade organica do utilizador:\n-> ");
                newString = String.valueOf(getUniOrgNome());
                break;
            case 7:
                System.out.printf("Nova pass do utilizador:\n-> ");
                newString = sc.nextLine();
                break;
        }

        if (r.updateUtilizador(ccn, newString, operation)) {
            System.out.println("Sucesso!");
        } else {
            System.out.println("Erro!");
        }
    }

    //voto numa eleiçao que ainda nao abriu ao publico
    private void anticipatedVote() throws RemoteException {
        Scanner sc = new Scanner(System.in);
        int elecId = pickElections(2);
        if (elecId == -1) {
            System.out.println("Erro!");
            return;
        }
        String voto;
        int ccn;

        System.out.printf("Introduza o número de cartão de cidadão!");
        ccn = getPhoneOrCCN(2);

        System.out.printf("Introduza a palavra pass: ");
        String pass = sc.nextLine();

        voto = pickListFromElection(ccn, elecId, 2);
        if (voto.equals("")) {
            System.out.println("Erro!");
            return;
        }

        if (r.anticipatedVote(ccn,voto,elecId, pass) != null) {
            System.out.println("Sucesso!");
        } else {
            System.out.println("Erro!");
        }
    }

    //consulta resultados de eleições passadas [lista, votos]
    private void checkPastElections() throws RemoteException {
        int elecId = pickElections(3);
        if (elecId == -1) {
            System.out.println("Erro!");
            return;
        }
        ArrayList<ArrayList<String>> results = r.showResultadosFromEleicao(elecId);
        if (results.get(1).size() == 0) {
            System.out.println("Não existem resultados!");
            return;
        }
        System.out.println("Resultados: ");
        System.out.println("Total de votos: " + results.get(0));
        for (int i = 0; i < results.get(0).size(); i++) {
            System.out.println("-> " + results.get(1).get(i) + " - nº de votos -> " + results.get(2).get(i) + " - percentagem de votos -> " + results.get(3).get(i));
        }
    }

    //vê para uma eleiçao em que mesa certa pessoa (CC) votou.
    private void checkVoteLocal() throws RemoteException {
        int elecID = pickElections(3);
        if (elecID == -1) {
            return;
        }

        int ccn;
        System.out.printf("Introduza o número de cartão de cidadão!");
        ccn = getPhoneOrCCN(2);

        if (r.showPersonVotingInfo(ccn, elecID) == null) {
            System.out.println("Não existem dados!");
        } else {
            System.out.println(r.showPersonVotingInfo(ccn, elecID));
        }

    }

    //edita propriedades de eleiçoes futuras já criadas
    private void editElections() throws RemoteException {
        int operation;
        Scanner sc = new Scanner(System.in);
        int elecId = pickElections(2);
        if (elecId == -1) {
            System.out.println("Erro!");
            return;
        }
        String newInput;
        boolean verify = false;

        do {
            System.out.println("O que quer editar?");
            System.out.println("1-Descrição");
            System.out.println("2-Hora de início");
            System.out.println("3-Hora de fim");
            System.out.printf("Opção: ");
            operation = readInt();
            if (operation <= 0 || operation > 3) {
                System.out.println("Insira um valor válido, por favor.\n");
            }
        } while (operation <= 0 || operation > 3);

        switch (operation) {
            case 1:
                System.out.printf("Insira a nova descrição: ");
                newInput = sc.nextLine();
                verify = r.updateEleicoesDescricao(elecId, newInput);
                break;
            case 2:
                System.out.println("Insira a nova data de início (yyyy-MM-dd HH:mm:ss).");
                newInput = getData();
                verify = r.updateEleicoesData(elecId, newInput, 1);
                break;
            case 3:
                System.out.println("Insira a nova data de fim (yyyy-MM-dd HH:mm:ss).");
                newInput = getData();
                verify = r.updateEleicoesData(elecId, newInput, 2);
                break;
        }

        if (verify) {
            System.out.println("Sucesso!");
        } else {
            System.out.println("Erro!");
        }
    }

    //adiciona ou remove uma mesa de uma eleição existentes actuais ou futura. associia um departamento caso se crie
    private void manageTables() throws RemoteException {
        int operation;
        boolean verify = false;
        String un_org_nome;

        do {
            System.out.println("Deseja adicionar ou remover uma mesa? ");
            System.out.println("1-Adicionar");
            System.out.println("2-Remover");
            System.out.printf("Opção: ");
            operation = readInt();
            if (operation <= 0 || operation > 2) {
                System.out.println("Insira um valor válido, por favor.\n");
            }
        } while (operation <= 0 || operation > 2);


        int elecID = pickElections(2);
        if (elecID == -1) {
            System.out.println("Erro!");
            return;
        }

        switch (operation) {
            case 1:
                System.out.println("Associe uma unidade organica à mesa!");
                un_org_nome = getUniOrgNome();
                if (un_org_nome == null) {
                    System.out.println("Erro!");
                    return;
                }
                System.out.println("Associe um elemento à mesa!");
                int numero_cc = getPhoneOrCCN(2);
                verify = r.createMesaVoto(un_org_nome, elecID, numero_cc);
                break;
            case 2:
                int table = pickTableFromElection(elecID);
                un_org_nome = getUniOrgNome();
                if (table != -1 && un_org_nome != null) {
                    verify = r.deleteMesaVoto(table, un_org_nome, elecID);
                }
                break;
        }

        if (verify) {
            System.out.println("Sucesso!");
        } else {
            System.out.println("Erro!");
        }
    }

    //adiciona ou edita a pessoa (CC) acossiada a uma mesa de eleiçao
    private void manageTablePersonal() throws RemoteException {
        int operation;
        boolean verify = false;
        int actual, tableID, ccn;
        String un_org_nome;

        do {
            System.out.println("Que operação quer realizar? ");
            System.out.println("1-Adicionar");
            System.out.println("2-Editar");
            System.out.printf("Opção: ");
            operation = readInt();
            if (operation <= 0 || operation > 2) {
                System.out.println("Insira um valor válido, por favor.\n");
            }
        } while (operation <= 0 || operation > 2);

        int elecID = pickElections(2);
        tableID = pickTableFromElection(elecID);
        if (tableID == -1){
          return;
        }
        if (operation == 1) {
            System.out.println("Insira o numero de cartão de cidadão da nova pessoa!");
            ccn = getPhoneOrCCN(2);
            verify = r.updateMesaVotoUtilizadores(ccn, tableID, elecID);


        } else {
            actual = pickPersonFromTable(elecID, tableID);
            if (actual == -1) {
                return;
            }
            System.out.println("Insira o numero de cartão de cidadão da nova pessoa!");
            un_org_nome = getUniOrgNome();
            ccn = getPhoneOrCCN(2);
            verify = r.updateMesaVotoUtilizadores(ccn, tableID, elecID);
        }

        if (verify) {
            System.out.println("Sucesso!");
        } else {
            System.out.println("Erro!");
        }
    }

    //mostra as pessoas acossiadas a uma mesa de uma eleiçao dadas
    private int pickPersonFromTable(int elecID, int mesaVotoId) throws RemoteException {

        Scanner sc = new Scanner(System.in);
        ArrayList<ArrayList<String>> usersList;
        usersList = r.showUtilizadoresMesaVoto(mesaVotoId, elecID);

        ArrayList<String> tableIDList = usersList.get(0);
        ArrayList<String> tableNameList = usersList.get(1);
        boolean flag = true;
        int option = 0;

        if (tableIDList.size() == 0) {
            System.out.println("Não existe nenhuma pessoa na mesa.");
            return -1;
        }

        while (flag) {
            flag = false;
            System.out.println("Qual o membro?");
            for (int i = 0; i < tableIDList.size(); i++) {
                System.out.printf(i + 1 + " - " + tableNameList.get(i));
            }

            option = readInt();
            if (option <= 0 || option > tableIDList.size()) {
                System.out.println("Insira um valor válido, por favor.\n");
                flag = true;
            }
        }

        return toInt(tableIDList.get(option - 1));

    }

    //adiciona ou remove uma lista de uma eleiçao atual ou futura
    private void manageLists() throws RemoteException {
        Scanner sc = new Scanner(System.in);

        int electionId = pickElections(2);
        if (electionId == -1) {
            System.out.println("Erro!");
            return;
        }

        int operation;
        int listType = 0;
        String list;

        do {
            System.out.println("Que operação quer realizar? ");
            System.out.println("1-Adicionar");
            System.out.println("2-Remover");
            System.out.printf("Opção: ");
            operation = readInt();
            if (operation <= 0 || operation > 2) {
                System.out.println("Insira um valor válido, por favor.\n");
            }
        } while (operation <= 0 || operation > 2);

        if (operation == 1) {
            System.out.printf("Nome da nova lista candidata: ");
            while (true) {
                list = sc.nextLine();
                if (list.contains("|")) {
                    System.out.println("Não pode conter o caracter '|'.");
                } else {
                    break;
                }
            }
            do {
                System.out.println("Tipo da Lista:");
                System.out.println("1-Estudantes");
                System.out.println("2-Docentes");
                System.out.println("3-Funcionários");
                System.out.printf("Opção: ");
                listType = readInt();
                if (listType <= 0 || listType > 3) {
                    System.out.println("Insira um valor válido, por favor.\n");
                }
            } while (listType <= 0 || listType > 3);
            int numero_cc = getPhoneOrCCN(2);
            if(r.createLista(list, listType, electionId, numero_cc)){
              System.out.println("Sucesso");
            }
            else {
              System.out.println("Erro");
            }
        } else {
            list = pickListFromElection(0,electionId,1);
            if (list.equals("")) {
                System.out.println("Erro");
                return;
            }
            if (r.deleteLista(list,electionId)) {
              System.out.println("Sucesso!");
            } else {
              System.out.println("Erro!");
            }
        }

    }

    //seleciona uma mesa de eleiçao dada
    private int pickTableFromElection(int elecID) throws RemoteException {
        ArrayList<ArrayList<String>> tableList;
        tableList = r.showMesasVotoEleicao(elecID);
        boolean flag = true;
        int option = 0;
        String table;
        
        if (tableList.get(0).isEmpty()) {
            System.out.println("Não existe nenhuma mesa.");
            return -1;
        }
        System.out.println(tableList);
        System.out.println(tableList.get(0));
        System.out.println(tableList.get(1));
        while (flag) {
            flag = false;
            System.out.println("Qual mesa de voto?");
            for (int i = 0; i < tableList.get(0).size(); i++) {
                System.out.println(i + 1 + " -> " + tableList.get(0).get(i) + " - " + tableList.get(1).get(i));
            }
            System.out.printf("Opção: ");
            option = readInt();
            if (option <= 0 || option > tableList.size()) {
                System.out.println("Insira um valor válido, por favor.\n");
                flag = true;
            }
        }

        return toInt(tableList.get(0).get(option - 1));
    }

    //dada a eleiçao mostra as listas candidatas. o "type" decide se se mostras as listas blank e null ou nao
    private String pickListFromElection(int cc, int elecID, int type) throws RemoteException {

        ArrayList<String> listsList = new ArrayList<String>();
        if (type == 1) {
            listsList = r.showListsFromElection(elecID);
        }
        else if (type == 2){
            listsList = r.pickListsFromElection(cc,elecID);
        }
        boolean flag = true;
        int option = 0;

        if (listsList.size() == 0) {
            System.out.println("Não existe nenhuma lista nesta eleição.");
            return "";
        }
        while (flag) {
            flag = false;
            System.out.println("Qual a lista?");
            for (int i = 0; i < listsList.size(); i++) {
                System.out.println(i + 1 + " -> " + listsList.get(i));
            }
            System.out.printf("Opção: ");
            option = readInt();
            if (option <= 0 || option > listsList.size()) {
                System.out.println("Insira um valor válido, por favor.\n");
                flag = true;
            }
        }

        return listsList.get(option - 1);
    }

    //escolhe uma eleição de uma lista de eleições escolhida com o "type". 1 - passadas e futuras, 2 - Futuras, 3 - Passadas, 4 - Passadas e atuais
    private int pickElections(int type) throws RemoteException {
        boolean flag = true;
        int option = 0;
        ArrayList<ArrayList<String>> electionsList = null;
        switch (type) {
            case 1:
                electionsList = r.showEleicoesDecorrer();
                break;
            case 2:
                electionsList = r.showEleicoesFuturas();
                break;
            case 3:
                electionsList = r.showEleicoesPassadas();
                break;
        }

        ArrayList<String> idList = electionsList.get(0);
        ArrayList<String> titleList = electionsList.get(1);

        if (idList.size() == 0) {
            System.out.println("Não existem eleições a apresentar!");
            return -1;
        }

        while (flag) {
            flag = false;
            System.out.println("Qual eleição?");
            for (int i = 0; i < idList.size(); i++) {
                System.out.println(i + 1 + " -> " + idList.get(i) + " - " + titleList.get(i));
            }
            System.out.printf("Opção: ");
            option = readInt();
            if (option <= 0 || option > idList.size()) {
                System.out.println("Insira um valor válido, por favor.\n");
                flag = true;
            }
        }
        return toInt(idList.get(option - 1));

    }

    //recolhe toda a informação necessaria e cria uma eleiçao
    private void createElection() throws RemoteException {
        Scanner sc = new Scanner(System.in);
        int electionType;
        String beginning, end, title, desc, un_org_nome;
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
                System.out.println("Insira um valor válido, por favor.\n");
            }
        } while (electionType <= 0 || electionType > 4);

        System.out.println("Data e hora de início (yyyy-MM-dd HH:mm:ss).");
        beginning = getData();
        System.out.println("Data e hora de fim (yyyy-MM-dd HH:mm:ss).");
        end = getData();
        System.out.println("Titulo:");
        title = sc.nextLine();
        System.out.println("Descrição:");
        desc = sc.nextLine();

        switch (electionType) {
            case 1:
                un_org_nome = getUniOrgNome();
                if (un_org_nome == null) {
                    System.out.println("Erro!");
                    return;
                }
                verify = r.createEleicao(title, beginning, end,desc, 0, un_org_nome);
                break;
            case 2:
                verify = r.createEleicao(title, beginning, end,desc, 1, null );
                break;
            case 3:
                un_org_nome = getUniOrgNome();
                if (un_org_nome == null) {
                    System.out.println("Erro!");
                    return;
                }
                verify = r.createEleicao(title, beginning, end,desc, 2, un_org_nome);
                break;
            case 4:
                un_org_nome = getUniOrgNome();
                if (un_org_nome == null) {
                    System.out.println("Erro!");
                    return;
                }
                verify = r.createEleicao(title, beginning, end,desc, 3, un_org_nome);
                break;
        }

        if (verify) {
            System.out.println("Sucesso!");
        } else {
            System.out.println("Erro!");
        }

    }

    //adiciona remove ou edita faculdades ou departamentos
    private void manageDepFac() throws RemoteException {
        Scanner sc = new Scanner(System.in);
        int operation, target;
        String un_org_nome = null;
        String name, pertence;
        boolean verify = false;

        do {
            System.out.println("Que operação quer realizar? ");
            System.out.println("1-Adicionar");
            System.out.println("2-Remover");
            System.out.println("3-Editar");
            System.out.printf("Opção: ");
            operation = readInt();
            if (operation <= 0 || operation > 3) {
                System.out.println("Insira um valor válido, por favor.\n");
            }
        } while (operation <= 0 || operation > 3);


        switch (operation) {
            case 1:
              System.out.printf("Insira o nome da nova unidade organica: ");
              name = sc.nextLine();
              System.out.printf("Insira o nome da faculdade à qual o departamento adicionado pertence: (Deixar vazio se for uma faculdade): ");
              un_org_nome = sc.nextLine();
              verify = r.createUnidadeOrganica(name, un_org_nome);
              break;
            case 2:
              un_org_nome = getUniOrgNome();
              if (un_org_nome == null) {
                System.out.println("Erro!");
                return;
              }
              verify = r.deleteUO(un_org_nome);
              break;
            case 3:
              un_org_nome = getUniOrgNome();
              if (un_org_nome == null) {
                System.out.println("Erro!");
                return;
              }
              do {
                System.out.println("Sobre o que?");
                System.out.println("1-Nome de uma unidade organica");
                System.out.println("2-Faculdade à qual pertence um departamento");
                System.out.printf("Opção: ");
                target = readInt();
                if (target <= 0 || target > 2) {
                  System.out.println("Insira um valor válido, por favor.\n");
                }
              } while (target <= 0 || target > 2);
              System.out.print("Insira o novo nome da unidade organica: ");
              name = sc.nextLine();
              verify = r.updateUnidadeOrganica(un_org_nome, name, target);
              break;
        }

        if (verify) {
            System.out.println("Sucesso!");
        } else {
            System.out.println("Erro!");
        }
    }

    //recolhe toda a informação necessaria e adiciona uma pessoa à base de dados
    private void addPerson() throws RemoteException {
        Scanner sc = new Scanner(System.in);
        int option, phone, ccn;
        String un_org_nome;
        boolean flag = true;
        String address, pass, nome;
        String ccv;
        do {
            System.out.println("Que pessoa quer adicionar?");
            System.out.println("1-Aluno");
            System.out.println("2-Docente");
            System.out.println("3-Funcionário");
            System.out.printf("Opção: ");
            option = readInt();
            if (option <= 0 || option > 3) {
                System.out.println("Insira um valor válido, por favor.\n");
            }
        } while (option < 0 || option > 3);
        System.out.printf("Introduza o número de cartão de cidadão!");
        ccn = getPhoneOrCCN(2);
        System.out.printf("Introduza o contacto telefónico!");
        phone = getPhoneOrCCN(1);
        un_org_nome = getUniOrgNome();
        if (un_org_nome == null) {
            System.out.println("Erro!");
            return;
        }
        System.out.printf("Introduza a morada da pessoa: ");
        address = sc.nextLine();
        System.out.printf("Introduza o nome da pessoa: ");
        while (true) {
            nome = sc.nextLine();
            if (nome.contains("|")) {
                System.out.println("Não pode conter o caracter '|'.");
            } else {
                break;
            }
        }
        System.out.printf("Introduza a password: ");
        pass = sc.nextLine();
        System.out.printf("Validade do Cartão de Cidadão: ");
        ccv = sc.nextLine();

        if (r.createUser(ccn, nome, pass, address, phone, ccv, option, un_org_nome)) {
            System.out.println("Sucesso!");
        } else {
            System.out.println("Erro!");
        }


    }

    //escolhe da lista de departamentos (type == 1) ou faculdades (type==2), uma opção. da return do id
    private String getUniOrgNome() throws RemoteException {
        Scanner sc = new Scanner(System.in);
        ArrayList<String> listNome;

        int option;

        listNome = r.showAllUO();
        System.out.println("Qual a unidade organica?");

        if (listNome.size() == 0) {
            System.out.println("-> Nada a apresentar!");
            return null;
        }

        do {
          for (int i = 0; i < listNome.size(); i++) {
            System.out.println(i + 1 + " - " + listNome.get(i));
          }

          System.out.printf("Opção: ");
          option = readInt();
          if (option <= 0 || option > listNome.size()) {
            System.out.println("Insira um valor válido, por favor.\n");
          }
        } while (option <= 0 || option > listNome.size());

        return listNome.get(option - 1);
    }

    //recebe o input de um numero telefonico (type==1) ou o numero de cartao de cidadão(type==2) e faz as devidas proteções
    private int getPhoneOrCCN(int type) throws RemoteException {
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

    //funçao que recebe ano mes dia hora minuto segundo e utiliza a checkData() verificar a validade da data
    private String getData() {
        int ano, mes, dia, horas, minutos, segundos;
        while (true) {
            System.out.println("Insira o ano:");
            ano = readInt();
            System.out.println("Insira o mês:");
            mes = readInt();
            System.out.println("Insira o dia:");
            dia = readInt();
            if (checkData(dia, mes, ano)) break;
            else System.out.println("Data inválida, insira de novo");
        }
        while (true) {
            System.out.println("Insira as horas:");
            horas = readInt();
            if (horas >= 0 && horas < 24) break;
            else System.out.println("Horas inválidas, insira de novo");
        }
        while (true) {
            System.out.println("Insira os minutos:");
            minutos = readInt();
            if (minutos >= 0 && minutos < 60) break;
            else System.out.println("Minutos inválidos, insira de novo");
        }
        while (true) {
            System.out.println("Insira os segundos:");
            segundos = readInt();
            if (segundos >= 0 && segundos < 60) break;
            else System.out.println("Segundos inválidos, insira de novo");
        }
        return String.format("%d-%d-%d %d:%d:%d", ano, mes, dia, horas, minutos, segundos);
    }

    //verifica a validade da data
    boolean checkData(int dia, int mes, int ano){
        if ((dia >= 1) && (mes >= 1 && mes <= 12) && (ano>=0 && ano<=9999)){
            if (mes==2){
                if (dia<=28) return true;
                else if (((ano % 400 == 0) || ((ano % 4 == 0) && (ano % 100 != 0))) && dia==29) return true;

            }
            else if ((mes == 4 || mes == 6 || mes == 9 || mes == 11) && (dia <= 30))    return true;
            else if ((mes == 1 || mes == 3 || mes == 5 || mes == 7 || mes ==8 || mes == 10 || mes == 12)&&(dia <=31))   return true;
        }
        return false;
    }

    //dá join à thread e fecha o programa
    private void exit() {
        try {
            checker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("...closing...");
    }

}
