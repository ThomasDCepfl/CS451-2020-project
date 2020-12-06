package cs451;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class Message implements Serializable {

    private Integer sender;
    private Integer senderAck;
    private Integer id;
    private Boolean ack;
    private int[] vectorClock;

    public Message(Integer identifier, Integer from, Integer fromAck, Boolean isAck) {
        sender = from;
        senderAck = fromAck;
        id = identifier;
        ack = isAck;
        vectorClock = null;
    }

    public Message(Integer identifier, Integer from, Integer fromAck, Boolean isAck, int[] vClock) {
        sender = from;
        senderAck = fromAck;
        id = identifier;
        ack = isAck;
        vectorClock = vClock.clone();
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

    public int[] getVectorClock() {
        if(vectorClock == null) return null;
        return vectorClock.clone();
    }

    public byte[] compress() {
        ByteBuffer toSend = ByteBuffer.allocate(8);
        toSend.putInt(id);
        toSend.put(sender.byteValue());
        toSend.put(senderAck.byteValue());
        toSend.put((byte) (ack ? 1:0));
        if(vectorClock != null) {
            int size = 4 * vectorClock.length;
            ByteBuffer clock = ByteBuffer.allocate(size);
            for(int v: vectorClock) clock.put((byte) v);
            toSend.put((byte) size);
            byte [] toSendArray = toSend.array();
            byte [] clockArray = clock.array();
            byte [] newToSend = new byte[toSendArray.length + clockArray.length];
            int i = 0;
            for(byte b: toSendArray) {
                newToSend[i] = b;
                ++i;
            }
            for(byte b: clockArray) {
                newToSend[i] = b;
                ++i;
            }
            return newToSend;
        }
        toSend.put((byte) 0);
        return toSend.array();
    }

    public static Message uncompress(byte[] received) {
        ByteBuffer bb = ByteBuffer.wrap(received);
        Integer newId = bb.getInt();
        Integer newSender = (int) received[4];
        Integer newSenderAck = (int) received[5];
        boolean newAck = received[6] != 0;
        Integer size = (int) received[7];

        if(size > 0) {
            ByteBuffer clock = ByteBuffer.wrap(received, 8, size);
            int [] newVectorClock = new int[(int) Math.ceil(size / 4.0)];
            for(int i = 0; i < newVectorClock.length; ++i) {
                newVectorClock[i] = clock.getInt();
            }
            return new Message(newId, newSender, newSenderAck, newAck, newVectorClock);
        }
        return new Message(newId, newSender, newSenderAck, newAck);
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
                + (ack ? ", is ack": ", is not ack")
                + ", with vector clock " + Arrays.toString(vectorClock);
    }
}
