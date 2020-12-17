package netty;

public class MQWrapper {
    public static final int SELF = 1;
    public int type;
    public String json;
    public int self; //1 send msg to self on onther device

    public MQWrapper(int type, String json) {
        this.type = type;
        this.json = json;
    }

    public MQWrapper(int type, String json, int self) {
        this.type = type;
        this.json = json;
        this.self = self;
    }
}
