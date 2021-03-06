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
    List<ReadListReplicas> readList;
    //List<Integer> timestamps;

    public int rid;
    public Timestamp wts;
    public int acks;
    private byte[] writerSignature;
    public boolean reading;


    //init
    public SharedMemoryRegister() {

        rid = 0;
        wts = null;
        acks = 1;
        value = null;
        writerSignature = null;
<<<<<<< HEAD
        reading = false;
=======
        readList = new ArrayList<>();
        reading = false;

>>>>>>> origin/master
        //timestamps = new ArrayList<>();
    }

    //write 1st phase
    public void write(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int id) throws Exception {
        wts = new Timestamp(System.currentTimeMillis());
        acks = 1;
        rid++;
        byte[] pass = divideMessage(message);
        writerSignature = makeServerDigitalSignature(pass);

        broadcastWrite(message, signature, nonce, signatureNonce, wts , id, writerSignature);

    }

    //write 1st phase
    public void broadcastWrite(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp wts, int id, byte[] writerSignature){
        try{
            for (int p : portList) {
                getReplica(p).writeReturn(message, signature, nonce, signatureNonce, wts, Integer.parseInt(super.myPort), id, writerSignature, rid);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //write 2nd phase
    public void targetDeliver(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp ts, int port, int id, byte[] writerSignature, int rid)throws Exception{

<<<<<<< HEAD
    public void targetDeliver(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp ts, int port, int id, byte[] writerSignature, int rid)throws Exception{
        Lock lock = new ReentrantLock();
        lock.lock();
=======
>>>>>>> origin/master
        if(getTimetamp(message,signature,nonce,signatureNonce) != null) {
            if(rid>1) { //confirma se já existe alguma informação no ficheiro
                if (ts.after(getTimetamp(message, signature, nonce, signatureNonce))) {
                    savePassword(message, signature, nonce, signatureNonce, ts, id, writerSignature);
                }
                sendAck(message, signature, nonce, signatureNonce, ts, port, id, rid);
            }else{
                savePassword(message, signature, nonce, signatureNonce, ts, id, writerSignature);
<<<<<<< HEAD
=======
                sendAck(message, signature, nonce, signatureNonce, ts, port, id, rid);
>>>>>>> origin/master
            }
            sendAck(message, signature, nonce, signatureNonce, ts, port, id,rid);
        }
        else{

            savePassword(message, signature, nonce, signatureNonce, ts, id, writerSignature);
<<<<<<< HEAD
            sendAck(message, signature, nonce, signatureNonce, ts, port, id,rid);
=======
            sendAck(message, signature, nonce, signatureNonce, ts, port, id, rid);
>>>>>>> origin/master
        }
        lock.unlock();
    }

<<<<<<< HEAD
    public void sendAck(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp ts, int port, int id, int rid) {
        try {
            getReplica(port).ackReturn(message, signature, nonce, signatureNonce, ts, port, id,rid);
=======
    //write 2nd phase
    public void sendAck(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp ts, int port, int id, int rid) {
        try {
            getReplica(port).ackReturn(message, signature, nonce, signatureNonce, ts, port, id, rid);
>>>>>>> origin/master

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
<<<<<<< HEAD
    public void deliver(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp ts, int port, int id,int rid) throws Exception
    {
        if(this.rid==rid) {

            acks++;
            if (acks > Math.ceil((int) (portList.size() + 1) / 2)) {
                acks = 1;
                if (reading) {
                    reading = false;
                    System.out.println("ATOMIC");
                } else {
                    savePassword(message, signature, nonce, signatureNonce, ts, id, writerSignature);
                    writerSignature = null;
                }
=======

    //write 3rd phase
    public void deliver(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp ts, int port, int id, int rid) throws Exception
    {
       if(this.rid==rid){
            acks++;
            if(acks > Math.ceil((int)(portList.size()+1)/2)){
                acks = 1;
                if(!reading) {
                    savePassword(message, signature, nonce, signatureNonce, ts, id, writerSignature);
                    writerSignature = null;
                }else{
                    reading=false;
                    System.out.println("pretty much atomic");} //ReadReturn
>>>>>>> origin/master
            }

        }

    }

    //read 1st phase
    public void read( byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int port, int id) throws Exception {
        rid++;
<<<<<<< HEAD
        acks=1;
        reading=true;
=======
        reading = true;
>>>>>>> origin/master
        readList = new ArrayList<>();
        value = null;
        byte[]readerPassword = getPass(message,signature,nonce,signatureNonce); //Para adicionar o seu valor da password na readlist para efeitos de posterior comparação
        Timestamp ts = getTimetamp(message,signature,nonce,signatureNonce);

<<<<<<< HEAD
        //talvez o writeSig

        byte[] serverSignature = getServerSignature(message);
        ReadListReplicas value = new ReadListReplicas(readerPassword, ts, serverSignature,message,signature,nonce,signatureNonce,id);
=======
        byte[] pass = divideMessage(message);
        writerSignature = makeServerDigitalSignature(pass);
        byte[] serverSignature = getServerSignature(message);

        ReadListReplicas value = new ReadListReplicas(readerPassword, ts, serverSignature, message, signature, nonce, signatureNonce,id);
>>>>>>> origin/master
        readList.add(value);
        broadcastRead(message, signature, nonce, signatureNonce,rid, port, id);
    }

<<<<<<< HEAD
    public void broadcastRead( byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int rid, int port, int id){
=======
    //read 1st phase
    public void broadcatRead( byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int rid, int port, int id){
>>>>>>> origin/master
        try{
            for (int p : portList) {
                getReplica(p).readReturn(message,signature,nonce,signatureNonce,rid, port, id);
            }
        }catch (Exception e){
           // e.printStackTrace();
        }
    }

<<<<<<< HEAD
    public void targetReadDeliver( byte[] password, Timestamp ts, int rid, int port, int id, byte[] serverSignature,byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce)throws Exception{

        getReplica(port).sendValue(rid, id, password, ts, serverSignature,message,signature,nonce,signatureNonce);
    }

    public void deliverRead(int rid, byte[] password, Timestamp ts, byte[] serverSignature,byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int id)throws Exception{
=======
    //read 2nd phase
    public void targetReadDeliver( byte[] password, Timestamp ts, int rid, int port, int id, byte[] serverSignature,byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce)throws Exception{

        getReplica(port).sendValue( rid, id, password, ts,serverSignature, message,signature,nonce,signatureNonce);
    }

    //read 3rd phase
    public void deliverRead(byte[] password, Timestamp ts, int rid, int id, byte[] serverSignature,byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce)throws Exception{
>>>>>>> origin/master
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
<<<<<<< HEAD
                    ReadListReplicas newValue = new ReadListReplicas(password, ts, serverSignature,message,signature,nonce,signatureNonce,id);
=======
                    ReadListReplicas newValue = new ReadListReplicas(password, ts, serverSignature, message, signature, nonce, signatureNonce,id);
>>>>>>> origin/master
                    readList.add(newValue);
                    lock.unlock();
                    if (readList.size() > Math.ceil((int) (portList.size() + 1) / 2)) {
                        Timestamp currentTs = readList.get(0).ts;
                        int index = 0;
                        int indexMax = 0;
                        for (ReadListReplicas auxVal : readList) {
                            if (currentTs.before(auxVal.ts)) {
                                currentTs = auxVal.ts;
                                indexMax = index;
                            }
                            index++;
                        }
                        this.value = readList.get(indexMax);
                        broadcastWrite(this.value.message, this.value.signature, this.value.nonce, this.value.signatureNonce, this.value.ts , this.value.id, writerSignature);
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