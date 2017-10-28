import java.util.ArrayList;

public class Message {
    private int type;
    private int i1;
    private int i2;
    private String s1;
    private String s2;
    private ArrayList<String> sList;
    private Boolean isValid = false;

    //type|i1|i2|s1|s2|sList[0]|sList[1]|sList[2]|...
    //Client -> Server: 0 login; 1 vote.
    //Server -> Client: 0 confirmation; 1 error; 2 blocked; 3 not logged; 4 info; 5 unblocked
    public Message(String msg) {
        if(msg==null) return;
        String[] split = msg.split("\\|");
        int aux = split.length;
        if (aux < 5) return;
        try {
            type = Integer.parseInt(split[0]);
            i1 = Integer.parseInt(split[1]);
            i2 = Integer.parseInt(split[2]);
        } catch (NumberFormatException e){ return;}

        s1 = split[3];
        s2 = split[4];

        sList = new ArrayList<>();
        for (int i = 5; i < aux; i++) {
            sList.add(split[i]);
        }
        isValid = true;
    }

    public Message(int type, int i1, int i2, String s1, String s2, ArrayList<String> sList) {
        this.type = type;
        this.i1 = i1;
        this.i2 = i2;
        this.s1 = s1;
        this.s2 = s2;
        this.sList = sList;
    }

    @Override
    public String toString() {
        String aux;
        if(sList!=null){
             aux = String.join("|", sList);
        } else {
            aux = "";
        }
        return String.format("%d|%d|%d|%s|%s|%s", type, i1, i2, s1, s2, aux);
    }

    public int getType() {
        return type;
    }

    public int getI1() {
        return i1;
    }

    public int getI2() {
        return i2;
    }

    public String getS1() {
        return s1;
    }

    public String getS2() {
        return s2;
    }

    public ArrayList<String> getsList() {
        return sList;
    }

    public Boolean getIsValid() { return isValid; }
}
