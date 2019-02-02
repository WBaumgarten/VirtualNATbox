import java.io.Serializable;

public class Paquet implements Serializable, Message {

    /**
     * Static final int specifying the TCP value as 0.
     */
    public static final int TCP = 0;

    /**
     * Static final int specifying the UDP value as 1.
     */
    public static final int UDP = 1;

    /**
     * Static final int specifying the ICMP value as 2.
     */
    public static final int ICMP = 2;

    /**
     * Static final int specifying the ACK value as 3.
     */
    public static final int ACK = 3;

    private String sourceAddress;
    private String destAddress;
    private byte[] packetData;
    private int protocolField;
    private int type;
    private int code;
    private int checkSum;

    /**
     * This is the constructor for the Paquet object. This is used when a Paquet
     * object is created without type.
     *
     * @param data Data that should be encased within the paquet
     */
    public Paquet(byte[] data) {
        packetData = data;
    }

    /**
     * This is the constructor for the Paquet object. This is used when a Paquet
     * object is created with type.
     *
     * @param data Data that should be encased within the paquet
     * @param type The type of this Paquet
     */
    public Paquet(byte[] data, int type) {
        packetData = data;
        this.type = type;
    }

    /**
     * Getter for sourceAddress.
     *
     * @return sourceAddress
     */
    public String getSourceAddress() {
        return sourceAddress;
    }

    /**
     * Setter for sourceAddress.
     *
     * @param sourceAddress The new value of this Paquet's sourceAddress
     */
    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    /**
     * Getter for destAddress.
     *
     * @return destAddress
     */
    public String getDestAddress() {
        return destAddress;
    }

    /**
     * Setter for destAddress.
     *
     * @param destAddress The new value of this Paquet's destAddress
     */
    public void setDestAddress(String destAddress) {
        this.destAddress = destAddress;
    }

    /**
     * Getter for packetData.
     *
     * @return packetData
     */
    public byte[] getPacketData() {
        return packetData;
    }

    /**
     * Setter for packetData.
     *
     * @param packetData The new value of this Paquet's packetData
     */
    public void setPacketData(byte[] packetData) {
        this.packetData = packetData;
    }

    /**
     * Getter for protocolField.
     *
     * @return protocolField
     */
    public int getProtocolField() {
        return protocolField;
    }

    /**
     * Setter for protocolField.
     *
     * @param protocolField The new value of this Paquet's protocolField
     */
    public void setProtocolField(int protocolField) {
        this.protocolField = protocolField;
    }

    /**
     * Getter for type.
     *
     * @return type
     */
    public int getType() {
        return type;
    }

    /**
     * Setter for type.
     *
     * @param type The new value of this Paquet's type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Getter for code.
     *
     * @return code
     */
    public int getCode() {
        return code;
    }

    /**
     * Setter for code.
     *
     * @param code The new value of this Paquet's code
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Getter for checkSum.
     *
     * @return checkSum
     */
    public int getCheckSum() {
        return checkSum;
    }

    /**
     * Setter for checkSum.
     *
     * @param checkSum The new value of this Paquet's checkSum
     */
    public void setCheckSum(int checkSum) {
        this.checkSum = checkSum;
    }

    /**
     * This method returns the type of this message.
     *
     * @return 2
     */
    @Override
    public int getTypeOfMessage() {
        return 2;
    }
}
