package cs451;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantLock;

public class UniformReliableBroadcast implements Broadcast, Observer {
    private BestEffortBroadcast broadcast;
    private ArrayList<Host> hs;
    private ConcurrentSkipListSet<Integer> deliv;
    private ConcurrentHashMap<Integer, Message> recv;
    private ConcurrentHashMap<Integer, ConcurrentSkipListSet<Integer>> isAck;
    private Integer sender;
    private Observer obs;
    private ReentrantLock l;

    public UniformReliableBroadcast(ArrayList<Host> hosts, Integer portNb, Integer from, Observer observer) {
        broadcast = new BestEffortBroadcast(hosts, portNb, observer);
        hs = new ArrayList<>(hosts);
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
        Integer mId = m.getId();

        l.lock();

        if(isAck.get(mId) == null) {
            ConcurrentSkipListSet<Integer> ack = new ConcurrentSkipListSet<>();
            ack.add(m.getSender());
            isAck.put(mId, ack);
        } else {
            ConcurrentSkipListSet<Integer> ack = isAck.get(mId);
            ack.add(m.getSender());
            isAck.replace(mId, ack);
        }

        if(!recv.containsKey(mId)) {
            recv.put(mId, m);
            broadcast.broadcast(new Message(m.getMessage(), m.getId(), sender, m.getSender(), m.isAck()));
        }

        for(Integer id: recv.keySet()) {
            if (!deliv.contains(id) && canDeliver(id)){
                obs.deliver(recv.get(id));
                deliv.add(id);
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
        Message msg = new Message(m.getMessage(), m.getId(), sender, m.getSender(), m.isAck());
        l.unlock();
        broadcast.broadcast(msg);
    }
}
