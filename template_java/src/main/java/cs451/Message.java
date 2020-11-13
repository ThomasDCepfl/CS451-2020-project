package cs451;

import java.io.Serializable;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Objects;

public class Message implements Serializable {

    private String message;
    private InetAddress srcIP;
    private InetAddress dstIP;
    private Integer srcPort;
    private Integer dstPort;
    private Integer sender;
    private Integer senderAck;
    private Integer id;
    private Boolean ack;


    public Message(String content, Integer identifier, InetAddress sourceIP,
                   InetAddress destinationIP, Integer sourcePort, Integer destinationPort,
                   Integer from, Integer fromAck, Boolean isAck) {
        message = content;
        srcIP = sourceIP;
        dstIP = destinationIP;
        srcPort= sourcePort;
        dstPort = destinationPort;
        sender = from;
        senderAck = fromAck;
        id = identifier;
        ack = isAck;
    }

    public Message(String content, Integer identifier, Integer from,
                   Integer fromAck, Boolean isAck) {
        message = content;
        sender = from;
        senderAck = fromAck;
        id = identifier;
        ack = isAck;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String m) {
        message = m;
    }

    public InetAddress getSourceIP() {
        return srcIP;
    }

    public void setSourceIP(InetAddress sourceIP) {
        srcIP = sourceIP;
    }

    public InetAddress getDestinationIP() {
        return dstIP;
    }

    public void setDestinationIP(InetAddress destinationIP) {
        srcIP = destinationIP;
    }

    public Integer getSourcePort() {
        return srcPort;
    }

    public void setSourcePort(Integer sourcePort) {
        srcPort = sourcePort;
    }

    public Integer getDestinationPort() {
        return dstPort;
    }

    public void setDestinationPort(Integer destinationPort) {
        dstPort = destinationPort;
    }

    public Integer getSender() {
        return sender;
    }

    public void setSender(Integer from) {
        sender = from;
    }

    public Integer getSenderAck() {
        return senderAck;
    }

    public void setSenderAck(Integer ack) {
        senderAck = ack;
    }

    public Integer getId() {
        return id;
    }

    public Boolean isAck() {
        return ack;
    }

    public void setAck(boolean newAck) {
        ack = newAck;
    }

    public byte[] compress() {
        ByteBuffer toSend = ByteBuffer.allocate(8);
        toSend.putInt(id);
        toSend.put(sender.byteValue());
        toSend.put(senderAck.byteValue());
        toSend.put((byte) (ack ? 1:0));
        return toSend.array();
    }

    public static Message uncompress(byte[] received) {
        ByteBuffer bb = ByteBuffer.wrap(received);
        Integer new_id = bb.getInt();
        Integer new_sender = (int) received[4];
        Integer new_senderAck = (int) received[5];
        boolean new_ack = received[6] != 0;
        return new Message("blabla", new_id, new_sender, new_senderAck, new_ack);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        Message mes = (Message) obj;
        return (id.equals(mes.getId()) && sender.equals(mes.getSender()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message);
    }

    @Override
    public String toString() {
        return "Message " + id
                + " with content " + message
                + " sent from " + sender
                + ", originally emitted by " + senderAck
                + (ack ? ", is ack": ", is not ack");
    }
}
