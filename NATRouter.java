import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NATRouter {

    public static int macInts = 11;
    private static int startIP1;
    private static int startIP2;
    private int port;
    private int poolSize;
    private int tableRefreshRate;
    private int ttl;
    int uniqueID = 0;

    public static final String macAddress = "78:ca:39:bb:5d:11";
    public static String natIP;// = "146.231.11.11";
    private String startIpInt = "10.10.";
    private String startIpExt = "146.232.";
    private ServerSocket serverSocket;

    private boolean stillRunning = true;

    private static NatThread natThread;

    public static ArrayList<ClientThread> clientList;
    public static ArrayList<NATEntry> natTable;
    public static ArrayList<String> availableIPint;
    public static ArrayList<String> availableIPext;

    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;

    /**
     * This is the constructor the the NATRouter object.
     *
     * @param port Port that the simulated router should run on
     * @param poolSize The amount of internal IPs that should be available
     */
    public NATRouter(int port, int poolSize, int tableRefreshRate, int ttl) {
        natTable = new ArrayList<>();
        clientList = new ArrayList<>();
        TableThread tableThread = new TableThread(tableRefreshRate, ttl);
        tableThread.start();
        this.poolSize = poolSize;
        this.port = port;
        this.tableRefreshRate = tableRefreshRate;
        this.ttl = ttl;
        initializeIP();
        natIP = getNewIP(Client.EXTERNAL);
    }

    private void initializeIP() {
        availableIPint = new ArrayList<>();
        availableIPext = new ArrayList<>();

        startIP1 = 0;
        startIP2 = 0;

        for (int i = 0; i < poolSize; i++) {
            String ip = genNewIP(Client.INTERNAL);
            availableIPint.add(ip);
        }

        startIP1 = 0;
        startIP2 = 0;

        for (int i = 0; i < poolSize; i++) {
            String ip = genNewIP(Client.EXTERNAL);
            availableIPext.add(ip);
        }
    }

    /**
     * This method generates a new IP for a client depending on whether the
     * client type is internal or external.
     *
     * @param type type of client
     * @return ip address
     */
    public String genNewIP(int type) {
        String ip;
        if (type == Client.INTERNAL) {
            ip = startIpInt;
        } else {
            ip = startIpExt;
        }
        ip += Integer.toString(startIP1) + "." + Integer.toString(startIP2);
        if (startIP2 == 999) {
            startIP2 = 0;
            startIP1++;
        } else {
            startIP2++;
        }
        return ip;
    }

    /**
     * This method returns the next available IP depending on the client type
     * and removes that IP from the IP pool.
     *
     * @param type type of client
     * @return IP address
     */
    public static String getNewIP(int type) {
        if (type == Client.INTERNAL) {
            return availableIPint.remove(0);
        } else {
            return availableIPext.remove(0);
        }
    }

    /**
     * This method adds an entry into the NAT table.
     *
     * @param n the NATEntry object that should be added to the table
     */
    public static void addNatEntry(NATEntry n) {
        natTable.add(n);
        printNatTable();
    }

    /**
     * This method prints the current NAT table, for debugging purposes.
     */
    public static void printNatTable() {
        System.out.println("\nNAT table\n-------------------------------------------");
        for (NATEntry curEntry : natTable) {
            System.out.println(curEntry);
        }
    }

    /**
     * This method starts the NATRouter thread, which waits for incoming
     * connections from clients.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
//            Socket natSocket = new Socket("localhost", port);
//            serverSocket.accept();                        
//            natThread = new NatThread(natSocket, natIP);
//            natThread.start();

            while (stillRunning) {
                System.out.println("Waiting for clients to connect.");
                Socket socket = serverSocket.accept();
                ClientThread c = new ClientThread(socket, uniqueID++);
                c.start();
                clientList.add(c);
                System.out.println("Client successfully connected");
            }

            try {
                serverSocket.close();
                for (ClientThread curClient : clientList) {
                    curClient.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing the server and clients.");
            }

        } catch (IOException ex) {
            Logger.getLogger(NATRouter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    synchronized static void removeClient(int id) {
        String disconnectedClient = "";
        for (int i = 0; i < clientList.size(); i++) {
            ClientThread curClient = clientList.get(i);
            if (curClient.id == id) {
                clientList.remove(i);
                break;
            }
        }
        System.out.println("Client removed");
    }

    /**
     * This method writes a Paquet object to the destination specified in the
     * Paquet object's corresponding field.
     *
     * @param p Paquet that should be sent
     */
    public static void sendPaquet(Paquet p) {
        if (p.getDestAddress().equals(natIP)) {
            natThread.writePacket(p);
        } else {
            for (ClientThread curClient : clientList) {
                if (curClient.getIP().equals(p.getDestAddress())) {
                    curClient.writePacket(p);
                    break;
                }
            }
        }
    }

    /**
     * This is the main method; creating the NATRouter and starting it's thread.
     *
     * @param args
     */
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        int poolSize = Integer.parseInt(args[1]);
        int tableRefreshRate = Integer.parseInt(args[2]);
        int ttl = Integer.parseInt(args[3]);
        NATRouter router = new NATRouter(port, poolSize, tableRefreshRate, ttl);
        router.start();
    }

}
