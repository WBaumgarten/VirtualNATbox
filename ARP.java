import java.io.Serializable;

public class ARP implements Serializable, Message {

    public static final int REQUEST = 1;
    public static final int REPLY = 2;
    private String ip;
    private String mac;
    private int type;

    public ARP(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setIP(String ip) {
        this.ip = ip;
    }

    public String getIP() {
        return ip;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getMac() {
        return mac;
    }

    @Override
    public int getTypeOfMessage() {
        return 3;
    }

}
