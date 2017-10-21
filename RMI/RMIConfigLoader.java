import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class RMIConfigLoader{

  String rmiName;
  int rmiPort;
  int mainUDP,secUDP;
  int pingFrequency;
  int retries;
  String dbIP;
  int dbPort;

  public RMIConfigLoader(){
    Properties prop = new java.util.Properties();
    FileInputStream input = null;

    try{
      input = new FileInputStream("rmi.properties");

      if(input == null){
        System.out.println("File could not be located!");
        return;
      }

      prop.load(input);

      this.rmiName = prop.getProperty("rmiName");
      this.rmiPort = Integer.parseInt(prop.getProperty("rmiPort"));
      this.mainUDP = Integer.parseInt(prop.getProperty("mainUDP"));
      this.secUDP = Integer.parseInt(prop.getProperty("secUDP"));
      this.pingFrequency = Integer.parseInt(prop.getProperty("pingFrequency"));
      this.retries = Integer.parseInt(prop.getProperty("retries"));
      this.dbIP = prop.getProperty("dbIP");
      this.dbPort = Integer.parseInt(prop.getProperty("dbPort"));

    } catch (IOException ioe){
        ioe.printStackTrace();
    } finally {
      if (input != null){
        try {
          input.close();
        } catch (IOException ioe){
          ioe.printStackTrace();
        }
      }
    }
  }

  public int getRMIPort() { return this.rmiPort; }

  public String getRMIName() { return this.rmiName; }

  public int getMainUDP() { return this.mainUDP; }

  public int getSecUDP() { return this.secUDP; }

  public int getPingFrequency() { return this.pingFrequency; }

  public int getRetries() { return this.retries; }

  public String getDBIP() { return this.dbIP; }

  public int getDBPort() { return this.dbPort; }

}
