public class NATEntry {
    private String internalIp;
    private String externalIp;
    private long timestamp;
    
    /**
     * This is the constructor for a NATEntry object.
     * 
     * @param internalIp Internal IP for this NAT table entry.
     * @param externalIp External IP for this NAT table entry.
     */
    public NATEntry(String internalIp, String externalIp) {
        this.internalIp = internalIp;
        this.externalIp = externalIp;
        timestamp = System.nanoTime();
    }

    /**
     * Getter for InternalIp.
     * 
     * @return internalIp
     */
    public String getInternalIp() {
        return internalIp;
    }

    /**
     * Setter for InternalIP.
     * 
     * @param internalIp The new value for the internalIp
     */
    public void setInternalIp(String internalIp) {
        this.internalIp = internalIp;
    }

    /**
     * Getter for ExternalIp.
     * 
     * @return externalIp
     */
    public String getExternalIp() {
        return externalIp;
    }

    /**
     * Setter for ExternalIp.
     * 
     * @param externalIp The new value for the externalIp
     */
    public void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }
    
    /**
     * This method resets the timestamp for this NAT table entry, in essence
     * renewing this entrie's time to live.
     */
    public void resetTimestamp() {
        timestamp = System.nanoTime();
    }
    
    /**
     * Getter for timestamp.
     * 
     * @return timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * toString method that returns this NAT table entry in a formatted String.
     * 
     * @return Formatted String of this NAT table entry
     */
    @Override
    public String toString() {
        return internalIp + " <-> " + externalIp;
    }
}
