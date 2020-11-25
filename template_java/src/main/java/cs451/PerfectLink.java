package cs451;

import java.util.ArrayList;
import java.util.HashSet;

public class PerfectLink implements Link, Observer{

    private StubbornLink link;
    private HashSet<Message> deliv;
    private Observer obs;

    public PerfectLink(ArrayList<Host> hosts, Integer portNb, Integer from, Observer observer) {
        link = new StubbornLink(hosts, portNb, from, this);
        deliv = new HashSet<>();
        obs = observer;
    }

    @Override
    public void deliver(Message m) {
        if (!deliv.contains(m)) {
            obs.deliver(m);
            deliv.add(m);
        }
    }

    @Override
    public void send(Host h, Message m) {
        link.send(h, m);
    }

    @Override
    public void begin() {
        link.begin();
    }

    @Override
    public void end() {
        link.end();
    }
}
