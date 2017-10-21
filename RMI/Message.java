import java.net.*;
import java.io.*;
import java.util.*;
import java.rmi.RemoteException;


public class Message implements Serializable{

  String message=null;
  String username=null;
  int type;
  String userLogged=null;
  boolean accept;
  
  private static final long serialVersionUID = 1L;
  
/*
				caso 1: INFORMAÇÃO DE LOGIN
				caso 2: INFORMAÇÃO DE REGISTO

*/
  public Message(boolean accept){
    this.accept = accept;
  
  
  }

  public int getType() { return this.type; }
}
