package cs451;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantLock;

public class LocalizedCausalBroadcast implements Observer, Broadcast {
    private UniformReliableBroadcast broadcast;
    private Integer id;
    private Integer order;
    private ConcurrentHashMap<Integer, ConcurrentSkipListSet<Message>> recv;
    private Set<Integer> causal;
    private int[] vectorClock;
    private Observer obs;
    private ReentrantLock l;

    public LocalizedCausalBroadcast(ArrayList<Host> hosts, Integer portNb, Integer from, Observer observer,
                                    Integer position, Set<Integer> causality){
        broadcast = new UniformReliableBroadcast(hosts, portNb, from, this);
        id = 0;
        order = position;
        obs = observer;
        recv = new ConcurrentHashMap<>();
        causal = new HashSet<>(causality);
        int n = hosts.size();
        vectorClock = new int[n + 1];
        for(int i = 1; i <= n; ++i) {
            vectorClock[i] = 0;
            recv.put(i, new ConcurrentSkipListSet<>(Comparator.comparing(Message::getId)));
        }
        l = new ReentrantLock();
    }


    private boolean compVectorClocks(int[] v1, int[] v2) {
        for (int i = 0; i < v1.length; ++i) {
            if (v1[i] > v2[i]) {
                return false;
            }
        }
        return true;
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
        int [] vc = vectorClock.clone();
        vc[order] = id;
        ++id;
        l.unlock();
        Message message = new Message(m.getId(), m.getSender(), m.getSenderAck(), m.isAck(), vc);
        broadcast.broadcast(message);
    }

    @Override
    public void deliver(Message m) {

        Integer mId = m.getId();
        if(recv.containsKey(mId)) {
            recv.get(mId).add(m);
        } else {
            ConcurrentSkipListSet<Message> received = new ConcurrentSkipListSet<>();
            received.add(m);
            recv.put(mId, received);
        }

        boolean oneMoreTime = true;
        while(oneMoreTime) {
            oneMoreTime = false;
            for (int i = 1; i < vectorClock.length; ++i) {
                for(Message msg: recv.get(i)){
                    l.lock();
                    if(compVectorClocks(msg.getVectorClock(), vectorClock) && causal.contains(i)) {
                        ++vectorClock[i];
                        l.unlock();
                        obs.deliver(msg);
                        oneMoreTime = true;
                    }
                    if(l.isHeldByCurrentThread()) l.unlock();
                }
            }
        }

    }
}
