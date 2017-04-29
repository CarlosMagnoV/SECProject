package pt.ist.sec;


import javax.crypto.SecretKey;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SharedMemoryRegister extends Server {

    ReadListReplicas value;
    List<ReadListReplicas> readList;
    //List<Integer> timestamps;

    public int ts;
    public int rid;
    public int wts;
    public int acks;

    public SharedMemoryRegister() {

        rid = 0;
        wts = 0;
        acks = 0;
        value = new ReadListReplicas();
        readList = new ArrayList<>();
        //timestamps = new ArrayList<>();
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
            value.message = message;
            value.signature = signature;
            value.nonce = nonce;
            value.signatureNonce = signatureNonce;
            value.ts = wts;
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
    public void deliver(int wts, int port)
    {
        if(wts == this.wts){
            acks++;
            if(acks > portList.size()/2){
                acks = 0;
                return;
            }
        }
        // DO NOT DELIVER HERE
    }

    public void read(int port, int id){
        rid++;
        readList = null;
        broadcatRead(rid, port, id);
    }

    public void broadcatRead(int rid, int port, int id){
        try{
            for (int p : portList) {
                getReplica(p).readReturn(rid, port, id);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void targetReadDeliver(int rid, int port, int id)throws Exception{
        getReplica(port).sendValue(rid, port, id, value);
    }

    public void deliverRead(int rid, ReadListReplicas value ){
        if(this.rid == rid){
            Lock lock = new ReentrantLock();
            lock.lock();
            readList.add(value);
            lock.unlock();
            if(readList.size() > portList.size()/2){
                int currentTs= value.ts;
                int index = 0;
                int indexMax = 0;
                for (ReadListReplicas auxVal: readList){
                    if(currentTs<=auxVal.ts){
                        currentTs=auxVal.ts;
                        indexMax = index;
                    }
                    index++;
                }
                this.value = readList.get(indexMax);
                readList = null;
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




    public void broadcastRegister(byte[] sess, PublicKey pubK, byte[] id) throws Exception {
        for (int p : portList) {
            System.out.println("Broadcasting to " + p);

            getReplica(p).registerDeliver(sess, pubK, id);
        }
    }
}