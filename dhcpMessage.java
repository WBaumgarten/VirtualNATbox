import java.io.Serializable;

public class dhcpMessage implements Serializable, Message {

    public static final int BOOTREQUEST = 1;
    public static final int BOOTREPLY = 2;
    private String ip;
    private int type;
    private int ipType;

    public dhcpMessage(int type, int ipType) {
        this.type = type;
        this.ipType = ipType;
    }

    public dhcpMessage(int type) {
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

    public int getIpType() {
        return ipType;
    }

    public void setIpType(int ipType) {
        this.ipType = ipType;
    }

    @Override
    public int getTypeOfMessage() {
        return 0;
    }

}
