import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    /**
     * Static final int specifying the INTERNAL value as 0.
     */
    public static final int INTERNAL = 0;

    /**
     * Static final int specifying the EXTERNAL value as 1.
     */
    public static final int EXTERNAL = 1;

    private int type;
    private int port;
    private Socket socket;
    private ObjectOutputStream clientOut;
    private ObjectInputStream clientIn;
    private String server;
    private static String natMac;
    private static String natIP;
    private String ipAddress;
    private String macAddress;
    private byte[] packetData;

    /**
     * This method checks whether the socket is connected.
     *
     * @return True or False depending on socket connection
     */
    public boolean checkConnection() {
        return socket.isConnected();
    }

    /**
     * Constructor for Client object.
     *
     * @param server IP address of server
     * @param port Port of server
     * @param type Type of client (INTERNAL/EXTERNAL)
     * @param macAddres Mac address assigned to this client
     */
    public Client(String server, int port, int type, String macAddres) {
        this.server = server;
        this.port = port;
        this.type = type;
        this.macAddress = macAddres;
    }

    /**
     * Send a simulated packet of the chosen protocol type.
     *
     * @param protocolType
     * @param destAddress
     */
    public void sendPaquet(int protocolType, String destAddress) {
        try {
            byte[] bytes = new byte[576];
            Paquet p = new Paquet(bytes, protocolType);
            p.setDestAddress(destAddress);
            p.setSourceAddress(ipAddress);
            clientOut.writeObject(p);
        } catch (IOException e) {
            System.out.println("Exception writing to server.");
        }
    }

    /**
     * This method starts the connection to the server socket and opens the
     * necessary streams, it then returns true or false depending on whether the
     * connection was successfull.
     *
     * @return True or False depending on whether the connection was successfull
     */
    public boolean start() {
        try {
            socket = new Socket(server, port);
        } catch (IOException ex) {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        try {
            clientIn = new ObjectInputStream(socket.getInputStream());
            clientOut = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }

        try {
            dhcpMessage mes;
            if (type == INTERNAL) {
                mes = new dhcpMessage(dhcpMessage.BOOTREQUEST, INTERNAL);
            } else {
                mes = new dhcpMessage(dhcpMessage.BOOTREQUEST, EXTERNAL);
            }

            clientOut.writeObject(mes);
            dhcpMessage mesRet = (dhcpMessage) clientIn.readObject();
            if (mesRet.getType() == dhcpMessage.BOOTREPLY) {
                ipAddress = mesRet.getIP();
            }

            ARP arp = new ARP(ARP.REQUEST);
            clientOut.writeObject(arp);
            ARP arpRet = (ARP) clientIn.readObject();
            if (arpRet.getType() == ARP.REPLY) {
                natMac = arpRet.getMac();
                natIP = arpRet.getIP();
            }
			arp = new ARP(ARP.REPLY);
		    arp.setMac(macAddress);
			clientOut.writeObject(arp);
        } catch (IOException e) {
            System.out.println("Something went wrong when sharing IP");
            disconnect();
            return false;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        new ListenFromServer().start();
        return true;
    }

    /**
     * This method appropriately dissconects the client from the server by first
     * closing all the necessary sockets and streams.
     */
    public void disconnect() {
        System.out.println("Disconnect");
        try {
            if (clientIn != null) {
                clientIn.close();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        try {
            if (clientOut != null) {
                clientOut.close();
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

    /*
     * a class that waits for the message from the server
     */
    class ListenFromServer extends Thread {

        // add mutex locks for threads to have sole acces to sending stream
        @Override
        public void run() {
            while (true) {
                try {
                    // read the message form the input datastream
                    Message m = (Message) clientIn.readObject();
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
                            if (p.getType() == Paquet.ACK) {
                                System.out.println("ACK received");
                                System.out.println("Sent packet to " + p.getSourceAddress());
                            } else {
                                System.out.println("Packet received");
                                sendPaquet(Paquet.ACK, p.getSourceAddress());
                            }
                            break;
                    }

                } catch (IOException e) {
                    System.out.println(e);
                    break;
                } catch (ClassNotFoundException e2) {
                    System.err.println(e2);
                    break;
                }
            }
        }
    }

    /**
     * This method writes a ICMPMessage to the outputStream.
     *
     * @param icmp ICMPMessage that should be sent
     */
    public void writeICMP(ICMPMessage icmp) {
        try {
            clientOut.writeObject(icmp);
        } catch (IOException e) {
            System.out.println("Exception writing ICMP message");
        }
    }

    private static String randomMACAddress() {
        Random rand = new Random();
        byte[] macAddr = new byte[6];
        rand.nextBytes(macAddr);

        macAddr[0] = (byte) (macAddr[0] & (byte) 254);  //zeroing last 2 bytes to make it unicast and locally adminstrated

        StringBuilder sb = new StringBuilder(18);
        for (byte b : macAddr) {
            if (sb.length() > 0) {
                sb.append(":");
            }

            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    /**
     * This is the main method for the Client class. This method creates a new
     * Client object and then starts a infinite loop that constantly checks for
     * user inout and handles it accordingly.
     *
     * @param args [server, port, type]
     */
    public static void main(String[] args) {
        String server = args[0].trim();
        int port = Integer.parseInt(args[1].trim());
        int type = Integer.parseInt(args[2]);
        Client client = new Client(server, port, type, randomMACAddress());                

        if (!client.start()) {
            System.out.println("There is no server to connect to on:\nIP - " + "localhost" + "\nPort - 1500");
            System.exit(0);
        }
        System.out.println("Successfully connected: " + client.ipAddress);
        System.out.println("IP Address of NAT: " + natIP);
        System.out.println("Mac Address of NAT: " + natMac);
        String input = "";
        System.out.println("To simulate sending a packet, enter an IP address to send a packet to.");
        System.out.println("To ping a client, 'ping <ip>'\n");
        System.out.println("To exit enter '/e'.");

        while (true) {
            Scanner scanIn = new Scanner(System.in);
            input = scanIn.nextLine();
            if (input.equals("/e")) {
                break;
            } else if (input.startsWith("ping ")) {
                String pingIp = input.split(" ")[1];
                //writeICMP(new ICMPMessage(ICMPMessage.PING, pingIp));
            } else {
                client.sendPaquet(0, input.trim());
            }
        }
        client.disconnect();
    }

}
