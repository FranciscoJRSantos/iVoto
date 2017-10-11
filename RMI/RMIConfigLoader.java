import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class RMIConfigLoader{

  String rmiName;
  int rmiPort;
  int udpPort;
  int pingFrequency;
  int retries;

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
      this.udpPort = Integer.parseInt(prop.getProperty("udpPort"));
      this.pingFrequency = Integer.parseInt(prop.getProperty("pingFrequency"));
      this.retries = Integer.parseInt(prop.getProperty("retries"));

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

  public int getUDPPort() { return this.udpPort; }

  public int getPingFrequency() { return this.pingFrequency; }

  public int getRetries() { return this.retries; }

}