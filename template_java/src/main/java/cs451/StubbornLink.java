package cs451;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class StubbornLink implements Link, Observer {
    private Observer obs;
    private FairLossLink link;
    private Timer clock;
    private ConcurrentSkipListSet<Message> deliv;
    private ArrayList<Host> hs;

    public StubbornLink(ArrayList<Host> hosts, Integer portNb, Observer observer) {
        link = new FairLossLink(portNb, this);
        clock = new Timer();
        deliv = new ConcurrentSkipListSet<>(Comparator.comparing(Message::getId));
        hs = new ArrayList<>(hosts);
        obs = observer;
    }

    @Override
    public void send(Host h, Message m) {
        if(!m.isAck()) deliv.add(m);
        link.send(h, m);
    }

    @Override
    public void begin() { // how to select correct host acccording to sender
        link.begin();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                for (Message msg : deliv){
                    Host host = hs.get(0);
                    Integer sender = msg.getSender();
                    for(Host h: hs) {
                        if (sender == h.getId()) host = h;
                    }
                    link.send(host, msg);
                }
            }
        };
        clock.schedule(task, 0L, 250);
    }

    @Override
    public void end() {
        clock.cancel();
        link.end();
    }

    @Override
    public void deliver(Message m) {
        if(m.isAck()) deliv.remove(m);
        else {
            Host host = hs.get(0);
            Integer sender = m.getSender();
            for(Host h: hs) {
                if (sender == h.getId()) host = h;
            }
            send(host, m);
            System.out.println("Deliver SL");
            obs.deliver(m);
        }
    }
}
