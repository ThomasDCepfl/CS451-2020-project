package cs451;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;


public class Process implements Observer, Broadcast{
    private Integer id;

    private Integer count;
    private ConcurrentLinkedQueue<String> logs = new ConcurrentLinkedQueue<String>();
    private Broadcast broadcast;
    private String message;

    public Process(Integer pId, ArrayList<Host> hosts, Integer portNumber,
                    Integer numP, String content) {
        id = pId;
        broadcast = new FIFOBroadcast(hosts, portNumber, pId, this);
        count = numP;
        message = content;

    }

    private void newLog(String log) {
        logs.add(log);
    }

    public void write() {
        try {
            FileOutputStream f = new FileOutputStream(message);
            for(String log: logs) {
                f.write(log.getBytes());
            }
        } catch (IOException e) {
            System.out.println("Couldn't write logs into file.");
        }
    }

    @Override
    public void begin() {
        broadcast.begin();
        for(Integer n = 1; n <= count; ++n) {
            Message m = new Message(n, id, id, false);
            broadcast(m);
            String str = "b " + m.getId() + "\n";
            newLog(str);
        }
    }

    @Override
    public void end() {
        broadcast.end();
    }

    @Override
    public void broadcast(Message m) {
        broadcast.broadcast(m);
    }

    @Override
    public void deliver(Message m) {
        System.out.println("Deliver process");
        String str = "d " + m.getSenderAck() + " " + m.getId() + "\n";
        newLog(str);
    }


}
