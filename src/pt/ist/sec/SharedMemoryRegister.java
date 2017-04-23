package pt.ist.sec;


import javax.crypto.SecretKey;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class SharedMemoryRegister extends Server {

    public String value;
    public String ts;
    public List<String> readlist = new ArrayList<>();
    public int rid;
    public String wts;
    public int acks;

    public SharedMemoryRegister() {
        value = "";
        ts = "";
        rid = 0;
        wts = "";
        acks = 0;
    }

    public void write() {

    }

    private ServerInterface getReplica(int port) throws Exception {
        Registry registry = null;
        String ip = InetAddress.getLocalHost().getHostAddress();
        registry = LocateRegistry.getRegistry(ip, port);
        ServerInterface stub = (ServerInterface) registry.lookup("" + port);
        return stub;
    }

    public void broadcastRegister(byte[] sess, PublicKey pubK) throws Exception {
        for (int p : portList) {
            System.out.println("Broadcasting to " + p);

            getReplica(p).registerDeliver(sess, pubK);
        }
    }
}