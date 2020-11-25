package cs451;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class FIFOBroadcast implements Broadcast, Observer{

    private UniformReliableBroadcast broadcast;
    private ConcurrentHashMap<Integer, Message> recv;
    private int [] order;
    private Integer wasDeliv;
    private Integer sender;
    private Observer obs;

    public FIFOBroadcast(ArrayList<Host> hosts, Integer portNb, Integer from, Observer observer) {
        broadcast = new UniformReliableBroadcast(hosts, portNb, from, this);
        recv = new ConcurrentHashMap<>();
        int n = hosts.size();
        order = new int[n + 1];
        for(int i = 0; i <= n; ++i) {
            order[i] = 1;
        }
        wasDeliv = 1;
        sender = from;
        obs = observer;

    }

    @Override
    public void deliver(Message m) {
        Integer mId = m.getId();

        if(mId >= order[m.getSenderAck()]) {
            recv.put(mId, m);
            System.out.println("Deliver FIFO ?");
            for(Integer id: recv.keySet()) {
                Message transmission = recv.get(id);
                Integer senderAck = transmission.getSenderAck();
                System.out.println("id: " + id + " " + transmission.getId() + ", order[senderAck]" + order[senderAck]);
                if(id == order[senderAck]) {
                    recv.remove(id);
                    ++order[senderAck];
                    System.out.println("Deliver FIFO");
                    obs.deliver(transmission);
                }
            }
        }
    }

    @Override
    public void begin() {
        broadcast.begin();
    }

    @Override
    public void end() {
        broadcast.end();
    }

    @Override
    public void broadcast(Message m) {
        broadcast.broadcast(new Message(wasDeliv++, sender, m.getSenderAck(), m.isAck()));
    }
}
