import java.rmi.*;
import java.util.*;

public interface ServerInterface extends Remote{
  // Create 
  public boolean createUser(int numero_cc, String nome, String password_hashed, String morada, int contacto, String validade_cc, int tipo, String un_org_nome) throws RemoteException;
  public boolean createUnidadeOrganica(String nome, String pertence) throws RemoteException;
  public boolean createEleicao(String titulo, String inicio, String fim, String descricao, int tipo, String un_org_nome) throws RemoteException;
  public boolean createLista(String nome, int tipo, int eleicao_id, int numero_cc) throws RemoteException;
  public boolean createMesaVoto(String un_org_nome, int eleicao_id, int numero_cc) throws RemoteException;
  // Read
  public ArrayList<String> showUtilizador(int numero_cc) throws RemoteException;
  public ArrayList<String> showUO(String nome) throws RemoteException;
  public ArrayList<String> showAllUO() throws RemoteException;
  public ArrayList<String> showEleicao(int id) throws RemoteException;
  public ArrayList<ArrayList<String>> showEleicoesDecorrer() throws RemoteException;
  public ArrayList<ArrayList<String>> showEleicoesPassadas() throws RemoteException;
  public ArrayList<ArrayList<String>> showEleicoesFuturas() throws RemoteException;
  public ArrayList<String> showLista(String nome, int eleicao_id) throws RemoteException;
  public ArrayList<String> showPersonVotingInfo(int numero_cc, int eleicao_id) throws RemoteException;
  public ArrayList<ArrayList<String>> showMesasVotoEleicao(int eleicao_id) throws RemoteException;
  public ArrayList<ArrayList<String>> showUtilizadoresMesaVoto(int numero, int eleicao_id) throws RemoteException;
  public ArrayList<String> pickListsFromElection(int numero_cc, int eleicao_id) throws RemoteException;
  public ArrayList<String> showListsFromElection(int eleicao_id) throws RemoteException;
  public ArrayList<ArrayList<String>> showResultadosFromEleicao(int eleicao_id) throws RemoteException;
  // Update
  public boolean updateUtilizador(int cc, String new_info, int flag) throws RemoteException;
  public String vote(int cc, String lista, int eleicao_id, int mesavoto_id) throws RemoteException;
  public String anticipatedVote(int cc, String lista, int eleicao_id, String pass) throws RemoteException;
  public boolean updateEleicoesData(int id, String newdate, int flag) throws RemoteException;
  public boolean updateEleicoesDescricao(int id, String newdate) throws RemoteException;
  public boolean updateUnidadeOrganica(String nome, String novo_nome, int flag) throws RemoteException;
  public boolean updateMesaVotoUtilizadores(int numero_cc, int mesa_voto_numero, int id_eleicao) throws RemoteException;
  // Delete
  public boolean deleteUtilizador(int numero_cc) throws RemoteException;
  public boolean deleteUO(String nome) throws RemoteException;
  public boolean deleteLista(String nome, int eleicao_id) throws RemoteException;
  public boolean deleteMesaVoto(int numero,String un_org_nome, int eleicao_id) throws RemoteException;
  // Security
  public String checkCC(int numero_cc, int eleicao_id) throws RemoteException;
  public boolean checkLogin(int numero_cc, String nome, String password_hashed) throws RemoteException;
  public boolean isConnected() throws RemoteException;
}
