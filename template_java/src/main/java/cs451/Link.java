package cs451;

public interface Link {
    void send(Host h, Message m);
    void begin();
    void end();
}
