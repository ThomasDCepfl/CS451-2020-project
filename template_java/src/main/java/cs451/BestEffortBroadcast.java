package cs451;

import java.util.ArrayList;

public class BestEffortBroadcast implements Broadcast, Observer{
    private PerfectLink link;
    private ArrayList<Host> hs;
    private Observer obs;


    public BestEffortBroadcast(ArrayList<Host> hosts, Integer portNb, Observer observer) {
        hs = new ArrayList<>(hosts);
        link = new PerfectLink(hosts, portNb, this);
        obs = observer;
    }

    @Override
    public void begin() {
        link.begin();
    }

    @Override
    public void end() {
        link.end();
    }

    @Override
    public void broadcast(Message m) {
        for(Host h: hs) {
            link.send(h, m);
        }
    }

    @Override
    public void deliver(Message m) {
        System.out.println("Deliver BEB");
        obs.deliver(m);
    }
}
