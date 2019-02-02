public class EthernetFrame {

    private String srcMacAddress;
    private String destMacAddress;
    private Paquet packet;

    public EthernetFrame(String srcMacAddress, String destMacAddress, Paquet packet) {
        this.srcMacAddress = srcMacAddress;
        this.destMacAddress = destMacAddress;
        this.packet = packet;
    }

}
