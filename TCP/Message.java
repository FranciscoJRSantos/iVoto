import java.util.ArrayList;

public class Message {
    private int type;
    private int i1;
    private int i2;
    private String s1;
    private String s2;
    private ArrayList<String> sList;
    private Boolean isValid = true;

    //type|i1|i2|s1|s2|list[0]|list[1]|list[2]|...
    public Message(String msg) {
        String[] split = msg.split("\\|");
        int aux = split.length;
        if (aux < 5){
            isValid = false;
            return;
        }
        try {
            type = Integer.parseInt(split[0]);
            i1 = Integer.parseInt(split[1]);
            i2 = Integer.parseInt(split[2]);
        } catch (NumberFormatException e){
            isValid = false;
            return;
        }

        s1 = split[3];
        s2 = split[4];

        sList = new ArrayList<>();
        for (int i = 5; i < aux; i++) {
            sList.add(split[i]);
        }
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
        return String.format("%d|%d|%d|%s|%s|%s", type, i1, i2, s1, s2, String.join("|", sList));
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
