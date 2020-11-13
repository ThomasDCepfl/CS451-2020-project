package cs451;

import java.util.ArrayList;
import java.util.HashSet;

public class PerfectLink implements Link, Observer{
    private Observer obs;
    StubbornLink link;
    HashSet<Message> deliv;

    public PerfectLink(ArrayList<Host> hosts, Integer portNb, Observer observer) {
        link = new StubbornLink(hosts, portNb, this);
        obs = observer;
        deliv = new HashSet<>();
    }

    @Override
    public void deliver(Message m) {
        if (!deliv.contains(m)) {
            deliv.add(m);
            obs.deliver(m);
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
