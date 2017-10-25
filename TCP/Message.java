import java.util.ArrayList;

public class Message {
    private int type;
    private int i1;
    private int i2;
    private int i3;
    private String s1;
    private String s2;
    private String s3;
    private ArrayList<String> sList;
    private Boolean isValid = true;

    //type|i1|i2|i3|s1|s2|s3|list[0]|list[1]|list[2]|...
    public Message(String msg) {
        String[] split = msg.split("\\|");
        int aux = split.length;
        if (aux < 7){
            isValid = false;
            return;
        }
        try {
            type = Integer.parseInt(split[0]);
            i1 = Integer.parseInt(split[1]);
            i2 = Integer.parseInt(split[2]);
            i3 = Integer.parseInt(split[3]);
        } catch (NumberFormatException e){
            isValid = false;
            return;
        }

        s1 = split[4];
        s2 = split[5];
        s3 = split[6];
        sList = new ArrayList<>();
        for (int i = 7; i < aux; i++) {
            sList.add(split[i]);
        }
    }

    public Message(int type, int i1, int i2, int i3, String s1, String s2, String s3, ArrayList<String> sList) {
        this.type = type;
        this.i1 = i1;
        this.i2 = i2;
        this.i3 = i3;
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
        this.sList = sList;
    }

    @Override
    public String toString() {
        return String.format("%d|%d|%d|%d|%s|%s|%s|%s", type, i1, i2, i3, s1, s2, s3, String.join("|", sList));
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

    public int getI3() {
        return i3;
    }

    public String getS1() {
        return s1;
    }

    public String getS2() {
        return s2;
    }

    public String getS3() { return s3; }

    public ArrayList<String> getsList() {
        return sList;
    }

    public Boolean getIsValid() { return isValid; }
}
