package cs451;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantLock;

public class UniformReliableBroadcast implements Broadcast, Observer {
    private ArrayList<Host> hs;
    private BestEffortBroadcast broadcast;
    private ConcurrentSkipListSet<Integer> deliv;
    private ConcurrentHashMap<Integer, Message> recv;
    private ConcurrentHashMap<Integer, ConcurrentSkipListSet<Integer>> isAck;
    private Integer sender;
    private Observer obs;
    private ReentrantLock l;

    public UniformReliableBroadcast(ArrayList<Host> hosts, Integer portNb, Integer from, Observer observer) {
        hs = new ArrayList<>(hosts);
        broadcast = new BestEffortBroadcast(hosts, portNb, this);
        deliv = new ConcurrentSkipListSet<>();
        recv = new ConcurrentHashMap<>();
        isAck = new ConcurrentHashMap<>();
        sender = from;
        obs = observer;
        l = new ReentrantLock();
    }


    public boolean canDeliver(Integer id) {
        Integer numAck = isAck.getOrDefault(id, new ConcurrentSkipListSet<>()).size();
        return numAck > hs.size() / 2.0;
    }

    @Override
    public void deliver(Message m) {

        l.lock();
        Integer mId = m.getId();

        if(isAck.containsKey(mId)) {
            isAck.get(mId).add(m.getSender());
        } else {
            ConcurrentSkipListSet<Integer> ack = new ConcurrentSkipListSet<>();
            ack.add(m.getSender());
            isAck.put(mId, ack);
        }

        if(!recv.containsKey(mId)) {
            recv.put(mId, m);
            broadcast.broadcast(m); // new Message(m.getId(), sender, m.getSenderAck(), m.isAck())
        }

        for(Integer id: recv.keySet()) {
            if (canDeliver(id) && !deliv.contains(id)){
                System.out.println("Deliver URB");
                deliv.add(id);
                obs.deliver(recv.get(id));
;            }
        }
        l.unlock();
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
        l.lock();
        //Message msg = new Message(m.getId(), sender, sender, m.isAck());
        recv.put(m.getId(), m);
        l.unlock();
        broadcast.broadcast(m);
    }
}
