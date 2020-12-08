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
    private ConcurrentSkipListSet<Message> recv;
    private ConcurrentHashMap<Integer, Set<Integer>> causal;
    private int[] vectorClock;
    private Observer obs;
    private ReentrantLock l;

    public LocalizedCausalBroadcast(ArrayList<Host> hosts, Integer portNb, Integer from, Observer observer,
                                    Integer position, ConcurrentHashMap<Integer, Set<Integer>> causality){
        broadcast = new UniformReliableBroadcast(hosts, portNb, from, this);
        id = 0;
        order = position;
        obs = observer;
        recv =  new ConcurrentSkipListSet<>(Comparator.comparing(Message::getId));
        causal = new ConcurrentHashMap<>(causality);
        int n = hosts.size();
        vectorClock = new int[n + 1];
        for(int i = 1; i < n; ++i) {
            vectorClock[i] = 0;
        }
        l = new ReentrantLock();
    }


    private boolean compVectorClocks(int[] v1, int[] v2, Set<Integer> indices) {
        for (int i: indices) {
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
        recv.add(m);
        boolean oneMoreTime = true;
        while(oneMoreTime) {
            oneMoreTime = false;
            for(Message msg: recv){
                Integer from = msg.getSenderAck();
                l.lock();
                if(compVectorClocks(msg.getVectorClock(), vectorClock, causal.get(from))) {
                    vectorClock[from -1]++;
                    l.unlock();
                    obs.deliver(msg);
                    oneMoreTime = true;
                }
                if(l.isHeldByCurrentThread()) l.unlock();
            }

        }

    }
}
