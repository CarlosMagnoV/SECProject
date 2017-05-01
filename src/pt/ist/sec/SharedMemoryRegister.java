package pt.ist.sec;


import javax.crypto.SecretKey;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

public class SharedMemoryRegister extends Server {

    ReadListReplicas value;
    List<ReadListReplicas> readList = new ArrayList<>();
    //List<Integer> timestamps;

    public int rid;
    public Timestamp wts;
    public int acks;
    private byte[] writerSignature;

    public SharedMemoryRegister() {

        rid = 0;
        wts = null;
        acks = 0;
        value = null;
        writerSignature = null;
        //timestamps = new ArrayList<>();
    }

    public void write(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int id) throws Exception {
        wts = new Timestamp(System.currentTimeMillis());
        acks = 1;
        byte[] pass = divideMessage(message);
        writerSignature = makeServerDigitalSignature(pass);
        broadcastWrite(message, signature, nonce, signatureNonce, wts , id, writerSignature);
    }

    public void broadcastWrite(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp wts, int id, byte[] writerSignature){
        try{
            for (int p : portList) {
                getReplica(p).writeReturn(message, signature, nonce, signatureNonce, wts, Integer.parseInt(super.myPort), id, writerSignature);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void targetDeliver(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp ts, int port, int id, byte[] writerSignature)throws Exception{
        if(getTimetamp(message,signature,nonce,signatureNonce) != null) {
            if (ts.after(getTimetamp(message, signature, nonce, signatureNonce))) {
                savePassword(message, signature, nonce, signatureNonce, ts, id, writerSignature);
                sendAck(message, signature, nonce, signatureNonce, ts, port, id);
            }
        }
        else{
            savePassword(message, signature, nonce, signatureNonce, ts, id, writerSignature);
            sendAck(message, signature, nonce, signatureNonce, ts, port, id);
        }
    }

    public void sendAck(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp ts, int port, int id) {
        try {
            getReplica(port).ackReturn(message, signature, nonce, signatureNonce, ts, port, id);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void deliver(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp ts, int port, int id) throws Exception
    {
       if(ts.equals(this.wts)){
            acks++;
            if(acks > Math.ceil((int)(portList.size()+1)/2)){
                acks = 0;
                savePassword(message, signature, nonce, signatureNonce, ts, id, writerSignature);
                writerSignature = null;
            }
        }

    }

    public void read( byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int port, int id){
        rid++;
        readList = new ArrayList<>();
        value = null;
        byte[]readerPassword = getPass(message,signature,nonce,signatureNonce); //Para adicionar o seu valor da password na readlist para efeitos de posterior comparação
        Timestamp ts = getTimetamp(message,signature,nonce,signatureNonce);
        byte[] serverSignature = getServerSignature(message);
        ReadListReplicas value = new ReadListReplicas(readerPassword, ts, serverSignature);
        readList.add(value);
        broadcatRead(message, signature, nonce, signatureNonce,rid, port, id);
    }

    public void broadcatRead( byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int rid, int port, int id){
        try{
            for (int p : portList) {
                getReplica(p).readReturn(message,signature,nonce,signatureNonce,rid, port, id);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void targetReadDeliver( byte[] password, Timestamp ts, int rid, int port, int id, byte[] serverSignature)throws Exception{

        getReplica(port).sendValue(rid, id, password, ts, serverSignature);
    }

    public void deliverRead(int rid, byte[] password, Timestamp ts, byte[] serverSignature)throws Exception{
        if(this.rid == rid) {
            boolean sign = false;
            try {
                sign = verifyServerDigitalSignature(serverSignature, password);
            }
            catch (Exception e){
                return;
            }
            if (sign) {
                if (printBase64Binary(serverSignature).equals(printBase64Binary(readList.get(0).serverSignature))) {
                    Lock lock = new ReentrantLock();
                    lock.lock();
                    ReadListReplicas newValue = new ReadListReplicas(password, ts, serverSignature);
                    readList.add(newValue);
                    lock.unlock();
                    if (readList.size() > Math.ceil((int) (portList.size() + 1) / 2)) {
                        Timestamp currentTs = readList.get(0).ts;
                        int index = 0;
                        int indexMax = 0;
                        for (ReadListReplicas auxVal : readList) {
                            System.out.println(auxVal.ts.toString());
                            if (currentTs.before(auxVal.ts)) {
                                currentTs = auxVal.ts;
                                indexMax = index;
                            }
                            index++;
                        }
                        this.value = readList.get(indexMax);

                        readList = new ArrayList<>();
                    }
                }
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




    public void broadcastRegister(byte[] sess, PublicKey pubK, byte[] id, int nonce) throws Exception {
        for (int p : portList) {

            getReplica(p).registerDeliver(sess, pubK, id, nonce);
        }
    }
}