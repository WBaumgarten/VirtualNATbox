public interface Message {
    // 0 - dhcp
    // 1 - ICMP
    // 2 - packet
    // 3 - ARP
    public int getTypeOfMessage();
}
