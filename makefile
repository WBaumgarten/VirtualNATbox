JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        ARP.java \
        Client.java \
        ClientThread.java \
	dhcpMessage.java \
	EthernetFrame.java \
	ICMPMessage.java \
	Message.java \
	NATEntry.java \
	NATRouter.java \
	NatThread.java \
	Paquet.java \
	TableThread.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
