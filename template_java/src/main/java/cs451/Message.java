package cs451;

import java.io.Serializable;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Objects;

public class Message implements Serializable {

    private Integer sender;
    private Integer senderAck;
    private Integer id;
    private Boolean ack;

    public Message( Integer identifier, Integer from,
                   Integer fromAck, Boolean isAck) {
        sender = from;
        senderAck = fromAck;
        id = identifier;
        ack = isAck;
    }


    public Integer getSender() {
        return sender;
    }

    public Integer getSenderAck() {
        return senderAck;
    }

    public Integer getId() {
        return id;
    }

    public Boolean isAck() {
        return ack;
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
        return new Message(new_id, new_sender, new_senderAck, new_ack);
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
        return Objects.hash(id, sender);
    }

    @Override
    public String toString() {
        return "Message " + id
                + " sent from " + sender
                + ", originally emitted by " + senderAck
                + (ack ? ", is ack": ", is not ack");
    }
}
