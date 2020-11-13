package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class FairLossLink extends Thread implements Link, Observer {

    private Observer obs;
    private DatagramSocket sock;
    private byte[] bu;
    private boolean run;

    public FairLossLink(Integer portNb, Observer observer) {
        obs = observer;
        run = false;
        bu = new byte[65655];
        try {
            sock = new DatagramSocket(portNb);
        } catch (SocketException e) {
            System.out.println("Couldn't create socket in Fair Loss Link.");
        }
    }

    @Override
    public void send(Host h, Message m) {
        try {
            byte[] toSend = m.compress();
            DatagramPacket pkt = new DatagramPacket(toSend, toSend.length,
                    m.getDestinationIP(), m.getDestinationPort());
            sock.send(pkt);
        } catch (Exception e) {
            System.out.println("Couldn't send packet");
        }
    }

    @Override
    public void begin() {
        run = true;
    }

    @Override
    public void end() {
        run = false;
    }

    @Override
    public void deliver(Message m) {
        obs.deliver(m);
    }

    @Override
    public void run() {
        while(run) {
            DatagramPacket pkt = new DatagramPacket(bu, bu.length);
            try {
                sock.receive(pkt);
            } catch (IOException e) {
                System.out.println("Couldn't receive packet");
            }

            Message m = Message.uncompress(pkt.getData());
            obs.deliver(m);
         }
    }
}
