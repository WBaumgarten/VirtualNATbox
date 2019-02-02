import java.io.Serializable;

public class ICMPMessage implements Serializable, Message {

    /**
     * Static final int specifying the ERROR type as 0.
     */
    public static final int ERROR = 0;

    /**
     * Static final int specifying the PING type as 1.
     */
    public static final int PING = 1;

    private int type;
    private String msg;

    /**
     * Constructor for the ICMPMessage object.
     *
     * @param type Type of this ICMPMessage
     * @param msg The message that should be sent with this ICMPMessage.
     */
    public ICMPMessage(int type, String msg) {
        this.type = type;
        this.msg = msg;
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
     * @param type The new value for this ICMPMessage's type field
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Getter for msg.
     *
     * @return msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Setter for msg.
     *
     * @param msg The new value for this ICMPMessage's type field.
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * Returns the type of the message.
     *
     * @return 1
     */
    @Override
    public int getTypeOfMessage() {
        return 1;
    }
}
