package cs451;

public interface Broadcast {
    void begin();
    void end();
    void broadcast(Message m);
}
