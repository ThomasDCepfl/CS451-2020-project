package cs451;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;


public class Process implements Observer, Broadcast{
    private Integer id;
    private InetAddress ip;
    private Integer port;
    private DatagramSocket socket;
    private Integer count;
    private ConcurrentLinkedQueue<String> logs = new ConcurrentLinkedQueue<String>();
    private Broadcast broadcast;
    private String message;

    public Process(Integer pId, ArrayList<Host> hosts, InetAddress addr, Integer portNumber,
                    Integer numP, String content) {
        id = pId;
        ip = addr;
        port = portNumber;
        try {
            socket = new DatagramSocket(port, ip);
        } catch (SocketException e) {
            System.out.println("Socket could not be created.");
        }

        broadcast = new FIFOBroadcast(hosts, portNumber, id, this);
        count = numP;
        message = content;



    }

    private void newLog(String log) {
        logs.add(log);
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress addr) {
        ip = addr;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer portNb) {
        port = portNb;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer newId) {
        id = newId;
    }

    public Integer getProcessCount() {
        return count;
    }

    public void setProcessCount(Integer n) {
        count = n;
    }

    public ConcurrentLinkedQueue<String> getLogs() {
        return logs;
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
            Message m = new Message(message, n, id, id, false);
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
        String str = "d " + m.getSenderAck() + " " + m.getId() + "\n";
        newLog(str);
    }


}
