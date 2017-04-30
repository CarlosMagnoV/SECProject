package pt.ist.sec;


import javax.crypto.SecretKey;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.sql.Timestamp;
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
    public Timestamp wts;
    public int acks;

    public SharedMemoryRegister() {

        rid = 0;
        wts = null;
        acks = 0;
        value = new ReadListReplicas();
        readList = new ArrayList<>();
        //timestamps = new ArrayList<>();
    }

    public void write(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int id) {
        wts = new Timestamp(System.currentTimeMillis());;
        acks = 0;
        broadcastWrite(message, signature, nonce, signatureNonce, wts , id);
    }

    public void broadcastWrite(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp wts, int id){
        try{
            for (int p : portList) {
                getReplica(p).writeReturn(message, signature, nonce, signatureNonce, wts, id);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void targetDeliver(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp ts, int port, int id)throws Exception{
        if(ts.after(getTimetamp(message,signature,nonce,signatureNonce))){

            sendAck(ts, port, id);
        }
    }

    public void sendAck(Timestamp ts, int port, int id) {
        try {
            getReplica(port).ackReturn(ts, port, id);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void deliver(Timestamp ts, int port)
    {
        if(ts.equals(this.wts)){
            acks++;
            if(acks > portList.size()/2){
                acks = 0;
                //put
            }
            return;
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