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

    byte[] message;
    public String ts;
    public List<String> readlist = new ArrayList<>();
    public int rid;
    public int wts;
    public int acks;

    public SharedMemoryRegister() {

        message = null;
        ts = "";
        rid = 0;
        wts = 0;
        acks = 0;
    }

    public void write(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int id) {
        wts++;
        acks = 0;
        broadcastWrite(message, signature, nonce, signatureNonce, wts , id);
    }
    public void broadcastWrite(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int wts, int id){
        try{
            for (int p : portList) {
                getReplica(p).writeReturn(message, signature, nonce, signatureNonce, wts, id);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void targetDeliver(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int wts, int port, int id)throws Exception{
        if(wts> this.wts){
            this.wts = wts;
            this.message = message;
            super.put(message, signature, nonce, signatureNonce, id);
            sendAck(wts, port, id);
        }
    }

    public void sendAck(int wts, int port, int id) {
        try {
            getReplica(port).ackReturn(wts, port, id);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void Deliver(int wts, int port)
    {
        if(wts == this.wts){
            acks++;
            if(acks == 2){ // 2 é o número total se servers, força a fazer deliver
                acks = 0;
                return;
            }

        }
    }

    private ServerInterface getReplica(int port) throws Exception {
        Registry registry = null;
        String ip = InetAddress.getLocalHost().getHostAddress();
        registry = LocateRegistry.getRegistry(ip, port);
        ServerInterface stub = (ServerInterface) registry.lookup("" + port);
        return stub;
    }


    public void broadcastRegister(byte[] sess, PublicKey pubK, int id) throws Exception {
        for (int p : portList) {
            System.out.println("Broadcasting to " + p);

            getReplica(p).registerDeliver(sess, pubK, id);
        }
    }
}