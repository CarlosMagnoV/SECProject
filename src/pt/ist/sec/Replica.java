package pt.ist.sec;

public class Replica {
    public static ServerInterface replica;
    public int port;

    public Replica(int port, ServerInterface replica){
        this.port = port;
        this.replica = replica;
    }

}
