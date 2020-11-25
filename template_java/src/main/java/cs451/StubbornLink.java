package cs451;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class StubbornLink implements Link, Observer {

    private ArrayList<Host> hs;
    private FairLossLink link;
    private Timer clock;
    private ConcurrentSkipListSet<Message> deliv;
    private Observer obs;

    public StubbornLink(ArrayList<Host> hosts, Integer portNb, Observer observer) {
        hs = new ArrayList<>(hosts);
        link = new FairLossLink(portNb, this);
        clock = new Timer();
        deliv = new ConcurrentSkipListSet<>(Comparator.comparing(Message::getId));
        obs = observer;
    }

    @Override
    public void send(Host h, Message m) {
        if(!m.isAck()) deliv.add(m);
        link.send(h, m);
    }

    @Override
    public void begin() {
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
        clock.schedule(task, 0L, 200L);
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
            send(host, new Message(m.getId(), m.getSender(), m.getSenderAck(), true));
            System.out.println("Deliver SL");
            obs.deliver(m);
        }
    }
}
