package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class FairLossLink extends Thread implements Link, Observer {

    private DatagramSocket sock;
    private Observer obs;
    private byte[] bu;
    private boolean run;

    public FairLossLink(Integer portNb, Observer observer) {
        try {
            sock = new DatagramSocket(portNb);
        } catch (SocketException e) {
            System.out.println("Couldn't create socket in Fair Loss Link.");
        }
        obs = observer;
        bu = new byte[65655];
        run = false;
    }

    @Override
    public void send(Host h, Message m) {
        try {
            byte[] toSend = m.compress();
            DatagramPacket pkt = new DatagramPacket(toSend, toSend.length,
                    InetAddress.getByName(h.getIp()), h.getPort());
            sock.send(pkt);
        } catch (Exception e) {
            System.out.println("Couldn't send packet");
        }
    }

    @Override
    public void begin() {
        run = true;
        new Thread(this).start();
    }

    @Override
    public void end() {
        run = false;
        sock.close();
    }

    @Override
    public void deliver(Message m) {
        obs.deliver(m);
    }

    @Override
    public void run() {
        while(run) {
            try {
                DatagramPacket pkt = new DatagramPacket(bu, bu.length);
                sock.receive(pkt);
                Message m = Message.uncompress(pkt.getData());
                deliver(m);
            } catch (IOException e) {
                System.out.println("Couldn't receive packet");
            }
        }
    }
}
