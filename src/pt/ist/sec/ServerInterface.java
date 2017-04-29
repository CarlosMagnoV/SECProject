package pt.ist.sec;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public interface ServerInterface extends Remote{


    int checkConnection() throws RemoteException;
    void put(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int id) throws Exception;
    byte[] get( byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int id) throws RemoteException;
    byte[] getDigitalSignature(byte[] PublicKey) throws Exception;
    void register(byte[] pubKey, ClientInterface c) throws Exception;
    void registerDeliver(byte[] sessKey, PublicKey pKey, byte[] id)throws Exception;
    void registerServer(String port) throws Exception;
    void writeReturn(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int wts, int id) throws Exception;
    void readReturn(int rid, int port, int id) throws Exception;
    void ackReturn(int wts, int port, int id) throws Exception;
    void sendValue(int rid, int port, int id, ReadListReplicas value)throws Exception;
}
