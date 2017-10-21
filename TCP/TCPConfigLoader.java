import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class TCPConfigLoader{

    int tcpPort;

    public TCPConfigLoader(){
        Properties prop = new java.util.Properties();
        FileInputStream input = null;

        try{
            input = new FileInputStream("tcp.properties");

            if(input == null){
                System.out.println("File could not be located!");
                return;
            }

            prop.load(input);

            this.tcpPort = Integer.parseInt(prop.getProperty("tcpPort"));


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

    public int getTCPPort() { return this.tcpPort; }

}