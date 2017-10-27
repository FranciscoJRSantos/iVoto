import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ACConfigLoader {
    String rmiName;

    public ACConfigLoader(){
        Properties prop = new java.util.Properties();
        FileInputStream input = null;

        try{
            input = new FileInputStream("adminconsole.properties");

            if(input==null){
                System.out.println("Sorry, unable to find ");
                return;
            }

            prop.load(input);
            this.rmiName=prop.getProperty("rmiName");

        } catch (IOException e) {
            e.printStackTrace();
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

    public String getRmiName(){return this.rmiName;}
}
