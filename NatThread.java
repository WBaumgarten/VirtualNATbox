import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NatThread extends Thread {

    Socket socket;
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    String ip;
    String mac;

    public NatThread(Socket socket, String ip) {
        try {
            this.socket = socket;
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            //inputStream = new ObjectInputStream(socket.getInputStream());            
            //ip = NATRouter.getNewIP(Client.EXTERNAL);
            this.ip = ip;
        } catch (IOException ex) {
            Logger.getLogger(NatThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    boolean isInternal(String ip) {
        return ip.substring(0, 6).equalsIgnoreCase("10.10.");
    }

    @Override
    public void run() {
        System.out.println("Nat IP: " + ip);
        while (true) {
            try {
                if (!socket.isConnected()) {
                    break;
                }
                inputStream = new ObjectInputStream(socket.getInputStream());
                Message m = (Message) inputStream.readObject();
                switch (m.getTypeOfMessage()) {
                    case 0:
                        dhcpMessage dM = (dhcpMessage) m;
                        System.out.println("DHCP received");
                        break;
                    case 1:
                        ICMPMessage iM = (ICMPMessage) m;
                        System.out.println("ICMP message: " + iM.getMsg());
                        break;
                    case 2:
                        Paquet p = (Paquet) m;
                        System.out.println("Evaluating packet");
                        if (!isInternal(p.getSourceAddress()) && isInternal(p.getDestAddress())) {
                            boolean sent = false;
                            for (int i = 0; i < NATRouter.natTable.size(); i++) {
                                if (ip.equalsIgnoreCase(NATRouter.natTable.get(i).getInternalIp())) { //ip = natTable.ip
                                    //Forward paquet according to table
                                    p.setDestAddress(NATRouter.natTable.get(i).getInternalIp());
                                    sent = true;
                                    NATRouter.sendPaquet(p);
                                    break;
                                }
                            }

                        }
                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(NatThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(NatThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void writePacket(Paquet p) {
        try {
            outputStream.writeObject(p);
        } catch (IOException e) {
            System.out.println("Exception writing");
        }
    }

    public void writeICMP(ICMPMessage icmp) {
        try {
            outputStream.writeObject(icmp);
        } catch (IOException e) {
            System.out.println("Exception writing ICMP message");
        }
    }

    public void close() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            System.err.println(e);
        }

        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            System.err.println(e);
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
