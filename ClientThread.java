import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread extends Thread {

    Socket socket;
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    String ip;
    String mac;
    int id;    

    /**
     * This is the constructor for a ClientThread object.
     * 
     * @param socket Socket to which the client will connect
     * @param id Unique id for each ClientThread
     */
    public ClientThread(Socket socket, int id) {
        this.id = id;
        this.socket = socket;        
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            dhcpMessage mes = (dhcpMessage) inputStream.readObject();            
            if (mes.getType() == dhcpMessage.BOOTREQUEST) {
                if (mes.getIpType() == Client.INTERNAL) {
                    ip = NATRouter.getNewIP(Client.INTERNAL);
                } else {
                    ip = NATRouter.getNewIP(Client.EXTERNAL);
                }                
                
                dhcpMessage mesRet = new dhcpMessage(dhcpMessage.BOOTREPLY);
                mesRet.setIP(ip);                                        
                outputStream.writeObject(mesRet);
            }
            
            ARP arp = (ARP)inputStream.readObject();
            if (arp.getType() == ARP.REQUEST) {
                ARP ret = new ARP(ARP.REPLY);
                ret.setIP(NATRouter.natIP);
                ret.setMac(NATRouter.macAddress);
                outputStream.writeObject(ret);
            }

			arp = (ARP) inputStream.readObject();
            if (arp.getType() == ARP.REPLY) {
                mac = arp.getMac();
            }
			System.out.println("Mac received: " + mac);
        } catch (IOException e) {
            System.err.println("Exception creating new Input/Output streams: " + e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Getter for ip.
     * 
     * @return ip
     */
    public String getIP() {
        return ip;
    }

    /**
     * This method starts the clientThread which constantly receives paquets 
     * and then handles them accordingly.
     * Special cases for paquets that are handles are:
     * Internal -> Internal
     * Internal -> External
     * External -> Internal
     * External -> External
     */
    @Override
    public void run() {
        while (true) {
            try {
                if (!socket.isConnected()) {
                    break;
                }
                
                Paquet pRecv = (Paquet) inputStream.readObject();
                System.out.println("Evaluating packet");

				boolean destExists = false;
				if (pRecv.getDestAddress().equals(NATRouter.natIP)) {
					destExists = true;
				} else {
		            for (ClientThread clientThread : NATRouter.clientList) {
		                if (clientThread.getIP().equals(pRecv.getDestAddress())) {
		                    destExists = true;
		                    break;
		                }
		            }
				}
				
				if (destExists) {
		            if (isInternal(pRecv.getSourceAddress()) && isInternal(pRecv.getDestAddress())) { //internal -> internal
		                NATRouter.sendPaquet(pRecv);
		            } else if (isInternal(pRecv.getSourceAddress()) && !isInternal(pRecv.getDestAddress())) { //internal -> external
		                System.out.println("IP Header modified: SourceAddress: " + pRecv.getSourceAddress() + " -> " + NATRouter.natIP);
		                pRecv.setSourceAddress(NATRouter.natIP);                   
		                
		                //Add or edit natTable entry
		                for (int i = 0; i < NATRouter.natTable.size(); i++) {
		                    if (ip.equalsIgnoreCase(NATRouter.natTable.get(i).getInternalIp())) {
		                        NATRouter.natTable.remove(i);
		                    } else if (pRecv.getDestAddress().equalsIgnoreCase(NATRouter.natTable.get(i).getExternalIp())) {
		                        NATRouter.natTable.remove(i);
		                    }
		                }
		                                   
		                NATEntry n = new NATEntry(ip, pRecv.getDestAddress());
		                NATRouter.addNatEntry(n);                   
		                NATRouter.sendPaquet(pRecv);
		                
		            } else if (!isInternal(pRecv.getSourceAddress()) && pRecv.getDestAddress().equals(NATRouter.natIP)) { //external -> internal
		                boolean sent = false;                    
		                for (int i = 0; i < NATRouter.natTable.size(); i++) {
		                    if (ip.equalsIgnoreCase(NATRouter.natTable.get(i).getExternalIp())) {
		                        //Forward paquet according to table
		                        System.out.println("IP Header modified: DestinationAddress: " + pRecv.getDestAddress() + " -> " + NATRouter.natTable.get(i).getInternalIp());
		                        pRecv.setDestAddress(NATRouter.natTable.get(i).getInternalIp());
		                        sent = true;
		                        NATRouter.sendPaquet(pRecv);
		                        break;
		                    }
		                }
		                if (!sent) {
		                    //drop paquet
		                    writeICMP(new ICMPMessage(ICMPMessage.ERROR, "The paquet could not be delivered!"));
		                }
		            } else { //external -> external
		                //drop paquet
		                writeICMP(new ICMPMessage(ICMPMessage.ERROR, "The paquet could not be delivered!"));
		            }

				} else {
                    writeICMP(new ICMPMessage(ICMPMessage.ERROR, "The destination address does not exist."));
				}

            } catch (IOException | ClassNotFoundException e) {
                break;
            }            
        }
        
        // add ip to pool
        if (isInternal(ip)) {
            NATRouter.availableIPint.add(ip);
        } else {
            NATRouter.availableIPext.add(ip);
        }
        
        close();
        System.out.println("Client disconnected");
        System.out.println("IP " + ip + " added back to pool of available addresses.");
    }

    /**
     * This method writes a Paquet object to outputStream.
     *
     * @param p Paquet that should be sent
     */
    public void writePacket(Paquet p) {
        try {
            outputStream.writeObject(p);
        } catch (IOException e) {
            System.out.println("Exception writing");
        }
    }
    
    /**
     * This method writes a ICMPMessage to the outputStream.
     * 
     * @param icmp ICMPMessage that should be sent
     */
    public void writeICMP(ICMPMessage icmp) {
        try {
            outputStream.writeObject(icmp);
        } catch (IOException e) {
            System.out.println("Exception writing ICMP message");
        }
    }

    /**
     * This method closes all the necessary streams for this thread to be
     * closed appropriately.
     */
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
        
        NATRouter.clientList.remove(this);
    }

    boolean isInternal(String ip) {        
        return ip.substring(0, 6).equalsIgnoreCase("10.10.");
    }

}
