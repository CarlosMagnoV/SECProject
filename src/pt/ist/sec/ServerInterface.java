package pt.ist.sec;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public interface ServerInterface extends Remote{


    int checkConnection() throws RemoteException;
    void put(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce) throws Exception;
    byte[] get( byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce) throws RemoteException;
    byte[] getDigitalSignature(byte[] PublicKey) throws Exception;
    void register(byte[] pubKey, ClientInterface c) throws Exception;

}
