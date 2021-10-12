package netty;

public class MQWrapper {
    public int type;
    public String json;

    public MQWrapper(int type, String json) {
        this.type = type;
        this.json = json;
    }
}
