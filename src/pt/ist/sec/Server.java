package pt.ist.sec;


import com.sun.deploy.util.SessionState;

import javax.crypto.*;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.beans.Expression;
import java.io.*;

import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.MulticastChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

import java.security.*;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import static java.util.Arrays.copyOfRange;
import static javax.xml.bind.DatatypeConverter.*;

public class Server implements ServerInterface{

    private static String DataFileLoc = System.getProperty("user.dir") + "/data/storage.txt";
    private static String LogFile = System.getProperty("user.dir") + "/log/log.txt";
    private static String SignFile = System.getProperty("user.dir") + "/log/signatures.txt";
    private static String RegFile = System.getProperty("user.dir") + "/data/register.txt";
    private static String byteFile = System.getProperty("user.dir") + "/data/byteFile";
    private static String certFile = System.getProperty("user.dir") + "/serverData/server.cer";

    public static String myPort;
    public static Boolean amWriter = true;
    private static String KeyStoreFile = System.getProperty("user.dir") + "/serverData/KeyStore.jks";

    private static Key ServerPrivateKey;
    private static PublicKey ServerPublicKey;

    private static ArrayList<ClientClass> clientList = new ArrayList<>();
    public static ArrayList<Integer> portList = new ArrayList<>();
    public static SharedMemoryRegister reg;
    private static ServerInterface server;
    public static int totalId = 0;

    public Server(){

    }

    public static void main(String[] args) {

        try {
            reg = new SharedMemoryRegister();
            getMyPublic();
            System.out.println("connecting . . .");
            server = new Server();
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(server, 0);

            String ip = InetAddress.getLocalHost().getHostAddress();

            myPort = args[0];
            System.setProperty("java.rmi.server.hostname", ip);
            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
            registry.bind(args[0], stub);

            connectReplicas(args);

            FileInputStream fis = new FileInputStream(KeyStoreFile);
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(fis, "changeit".toCharArray()); // esta password é a pass do keystore

            java.security.cert.Certificate cert = keystore.getCertificate("server-alias");
            ServerPrivateKey = keystore.getKey("server-alias","changeit".toCharArray());




            System.err.println("Server ready. Connected in: " + ip + ":" + args[0]);
        } catch (Exception e) {
            System.err.println("Server connection error: " + e.toString());
            e.printStackTrace();
        }

        fileCreation(DataFileLoc);
        fileCreation(LogFile);
        fileCreation(RegFile);
        fileCreation(byteFile);
        fileCreation(SignFile);

        try {
            while (true) Thread.sleep(Long.MAX_VALUE);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void getMyPublic()throws Exception{
        FileInputStream fin = new FileInputStream(certFile);
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate)f.generateCertificate(fin);
        ServerPublicKey = certificate.getPublicKey();

    }

    public void registerDeliver(byte[] sessKey, PublicKey pKey, byte[] id, int nonce)throws Exception{
        byte[] clientSession = DecryptionAssymmetric(sessKey);
        SecretKey originalKey = new SecretKeySpec(clientSession,"AES");
        byte[] clearId = DecryptionAssymmetric(id);
        addClient(pKey.getEncoded(),originalKey, Integer.parseInt(new String(clearId)));
        for(ClientClass client: clientList){
            if(client.id == Integer.parseInt(new String(clearId))){
                client.setNonce(nonce);
            }
        }
        System.out.println("Cliente adicionado com ID: " + Integer.parseInt(new String(clearId)));
    }

<<<<<<< HEAD
=======
    //write 2nd phase
>>>>>>> origin/master
    public void writeReturn(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp wts, int port, int id, byte[] writerSignature, int rid)throws Exception{
        amWriter = false;

        for(ClientClass c : clientList) {
            if(c.id == id) {
<<<<<<< HEAD
                c.myReg.targetDeliver(message, signature, nonce, signatureNonce, wts, port, id, writerSignature,rid);
=======
                c.myReg.targetDeliver(message, signature, nonce, signatureNonce, wts, port, id, writerSignature, rid);
>>>>>>> origin/master
            }
        }
    }
    //write 3rd phase
    public void ackReturn(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp ts, int port, int id, int rid) throws Exception{

<<<<<<< HEAD
    public void ackReturn(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp ts, int port, int id, int rid) throws Exception{
        for(ClientClass c : clientList) {
            if(c.id == id) {
                c.myReg.deliver(message, signature, nonce, signatureNonce, ts, port, id,rid);
=======
        for(ClientClass c : clientList) {
            if(c.id == id) {
                c.myReg.deliver(message, signature, nonce, signatureNonce, ts, port, id, rid);
>>>>>>> origin/master
            }
        }
    }

    //read 2nd phase
    public void readReturn( byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int rid, int port, int id)throws Exception{

        for(ClientClass c : clientList) {
            if(c.id == id) {
                byte[] password = getPass(message,signature,nonce,signatureNonce);
                Timestamp ts = getTimetamp(message,signature,nonce,signatureNonce);
                byte[] serverSignature = getServerSignature(message);
                c.myReg.targetReadDeliver(password,ts,rid,port,id, serverSignature,message,signature,nonce,signatureNonce);
                c.getNextNonce();
            }
        }
    }

<<<<<<< HEAD
    public void sendValue(int rid, int id, byte[] password, Timestamp ts,byte[] serverSignature,byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce)throws Exception{
        for(ClientClass c : clientList) {
            if(c.id == id) {
                c.myReg.deliverRead(rid, password, ts, serverSignature,message,signature,nonce,signatureNonce,id);
=======
    //read 3rd phase
    public void sendValue(int rid, int id, byte[] password, Timestamp ts,byte[] serverSignature,byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce)throws Exception{
        for(ClientClass c : clientList) {
            if(c.id == id) {
                c.myReg.deliverRead(password, ts, rid, id, serverSignature,message,signature,nonce,signatureNonce);
>>>>>>> origin/master
            }
        }
    }

    public byte[] DecryptionAssymmetric(byte[] ciphertext) throws Exception {

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, ServerPrivateKey);
        byte[] cipherData = cipher.doFinal(ciphertext);

        return cipherData;
    }
    //Updates the port list
    public static void connectReplicas(String[] ports) throws Exception{

        for(int i = 1; i < ports.length; i++){
            Registry registry = null;

            String ip = InetAddress.getLocalHost().getHostAddress();
            registry = LocateRegistry.getRegistry(ip, Integer.parseInt(ports[i]));

            server = new Server();
            UnicastRemoteObject.exportObject(server, 0);
            ServerInterface stub = (ServerInterface) registry.lookup(ports[i]);
            portList.add(Integer.parseInt(ports[i]));

            stub.registerServer(myPort);
        }
    }

    public void registerServer(String port) throws Exception{
        portList.add(Integer.parseInt(port));

    }

    private void storageSignture(ClientClass client, byte[] signature){
        Lock lock = new ReentrantLock();
        lock.lock();
        String pubKey = printBase64Binary(client.getPublicKey().getEncoded());
        String signString = printBase64Binary(signature);

        try {
            File file = new File(SignFile);
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String line;
            Path path = Paths.get(SignFile);

            Charset charset = Charset.forName("ISO-8859-1");

            List<String> lines = Files.readAllLines(path, charset);

            int i = 0;
            boolean newData = true;
            boolean isEmpty = true;
            while ((line = br.readLine()) != null) {
                isEmpty = false;
                if(line.equals(pubKey)){
                    line = br.readLine();
                    newData = false;
                    break;
                }
                else{
                    br.readLine();
                    br.readLine();
                    i+= 3;
                }
            }

            if(newData){
                if(!isEmpty){
                    lines.add("");
                    lines.add("");
                    lines.add("");
                }
                lines.add(i, pubKey);
                lines.add(i + 1, signString);
            }
            else{
                lines.remove(i + 1);
                lines.add(i + 1, line  + " || " + signString);
            }

            Files.write(path, lines, charset);
            br.close();
            fileReader.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        lock.unlock();
    }

    public static void fileCreation(String path) {
        File file = new File(path);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        }
        catch(IOException e) {
            System.out.println("File problem: " + e);
            e.printStackTrace();
        }
    }

    private static void writeByteCode(byte[] code, int index){
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            if(index >= 0) {
                //FileOutputStream output = new FileOutputStream(byteFile);
                File output = new File(byteFile);
                RandomAccessFile raf = new RandomAccessFile(output, "rw");
                raf.seek(index*16);
                raf.write(code);
                raf.close();
            }
            else{
                FileOutputStream output = new FileOutputStream(byteFile, true);
                output.write(code);
                output.close();
            }
        }
        catch(IOException e){
            System.out.println("Error writing password in file: " + e);
            e.printStackTrace();
        }
        lock.unlock();
    }

    private byte[] readByteCode(int index){

        try {

            Path path = Paths.get(byteFile);

            byte[] byteArray = copyOfRange(Files.readAllBytes(path),index*16,(index*16)+16);

            return byteArray;
        }
        catch(IOException e){
            System.out.println("Error reading password from file: " + e);
            e.printStackTrace();
            return null;
        }
    }


    public void storeData(byte[] pass, String pKeyString, String domainString, String usernameString, Timestamp ts, byte[] writerSignature)throws Exception{
        Lock lock = new ReentrantLock();
        lock.lock();
        String elements = domainString + " " + usernameString;
        String signature = printBase64Binary(writerSignature);


        File file = new File(DataFileLoc);
        FileReader fileReader = new FileReader(file);
        BufferedReader br = new BufferedReader(fileReader);
        String line;
        Path path = Paths.get(DataFileLoc);


        Charset charset = Charset.forName("ISO-8859-1");

        List<String> lines = Files.readAllLines(path, charset);

        int i = 0;
        Boolean newData = true;
        while ((line = br.readLine()) != null) {
            if (line.equals(pKeyString)) {
                line = br.readLine();
                if (line.equals(domainString)) {
                    line = br.readLine();
                    if (line.equals(usernameString)) {
                        writeByteCode(pass, Integer.parseInt(br.readLine()));
                        newData = false;
                        lines.remove(i+5);
                        lines.remove(i+4);
                        lines.add(i+4, ts.toString());
                        lines.add(i+5, signature);
                        break;
                    } else {
                        br.readLine();
                        br.readLine();
                        br.readLine();
                    }
                } else {
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    br.readLine();
                }
            } else {
                br.readLine();
                br.readLine();
                br.readLine();
                br.readLine();
                br.readLine();
            }
            i += 6;
        }
        if (newData) {
            Files.write(Paths.get(DataFileLoc),
                    (pKeyString + "\n" + domainString + "\n" + usernameString + "\n" + (getLastNumber()+1) + "\n" + ts + "\n" + signature + "\n").getBytes(),
                    StandardOpenOption.APPEND);
            writeByteCode(pass, -1);
        } else {
            Files.write(path, lines, charset);
        }

        br.close();
        lock.unlock();
    }



    public byte[] EncryptCommunication(byte[] plaintext, SecretKey SessKey) throws Exception{

        // Initialize cipher object

        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aesCipher.init(Cipher.ENCRYPT_MODE, SessKey);

        // Encrypt the cleartext
        byte[] ciphertext = aesCipher.doFinal(plaintext);

        return ciphertext;

    }

    public byte[] DecryptCommunication(byte[] ciphertext, SecretKey SessKey) throws Exception{

        // Initialize cipher object
        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, SessKey);

        // Encrypt the cleartext
        byte[] plaintext = aesCipher.doFinal(ciphertext);
        return plaintext;
    }

    public byte[] EncryptionAssymmetric(byte[] plaintext, PublicKey key) throws Exception{

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherData = cipher.doFinal(plaintext);

        return cipherData;
    }

    public int checkConnection(){
        return 1;
    }

    public void savePassword(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, Timestamp ts, int id, byte[] writerSignature)throws Exception{

        byte[] pKeyBytes = null;
        byte[] restMsg = null;
        byte[] decryptNonce = null;
        ClientClass client = clientList.get(0);

        for(ClientClass element: clientList) {

            if(element.id == id) {

                byte[] Bmsg = DecryptCommunication(message, element.getSessionKey());
                pKeyBytes = copyOfRange(Bmsg, 0, 294); // parte da chave publica
                restMsg = copyOfRange(Bmsg, 294, Bmsg.length); // resto dos argumentos
                decryptNonce = DecryptCommunication(nonce, element.getSessionKey());
                client = element;
            }
        }

        //if(pKeyBytes == null){}

        PublicKey ClientPublicKey = null;

        ClientPublicKey =
                KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pKeyBytes));

        if(!verifyDigitalSignature(signature, message, ClientPublicKey)&&!verifyDigitalSignature(signatureNonce, decryptNonce, ClientPublicKey)){ //If true, signature checks
            return;
        }

        storageSignture(client, signature);


        if (!client.checkNonce(decryptNonce)) {
            return;
        }
        String dom = new String(copyOfRange(restMsg, 0, 30), "ASCII");
        String usr = new String(copyOfRange(restMsg, 30, 60), "ASCII");



        byte[] pass = copyOfRange(restMsg, 60, restMsg.length);
        String domFinal = rmPadd(dom.toCharArray());
        String usrFinal = rmPadd(usr.toCharArray());


        String pKeyString = printBase64Binary(pKeyBytes);
        String domainString = domFinal;
        String usernameString = usrFinal;

        storeData(pass,pKeyString,domainString,usernameString, ts, writerSignature);


    }

    public  void put(byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce , int id) throws Exception{

            for(ClientClass c : clientList) {
                if(c.id == id) {
                    c.myReg.write(message, signature, nonce, signatureNonce, id);
                    if(portList.size() == 0){
                        savePassword(message,signature,nonce,signatureNonce, c.myReg.wts, id, makeServerDigitalSignature(message));
                    }
                }
            }

    }


    public Timestamp getTimetamp(byte[] message,byte[]signature,byte[] nonce,byte[] signatureNonce){

        byte[] pKeyBytes = null;
        ClientClass client = clientList.get(0);
        byte[] restMsg = null;
        byte[] decryptNonce = null;
        for(ClientClass element: clientList) {



            try {
                byte[] Bmsg = DecryptCommunication(message, element.getSessionKey());
                pKeyBytes = copyOfRange(Bmsg,0,294); // parte da chave publica
                restMsg = copyOfRange(Bmsg, 294, Bmsg.length); // resto dos argumentos
                decryptNonce = DecryptCommunication(nonce, element.getSessionKey());
                client = element;

            }
            catch(Throwable e){

            }
        }
        if(pKeyBytes == null){return null;}

        try {
            String dom = new String(copyOfRange(restMsg, 0, 30), "ASCII");
            String usr = new String(copyOfRange(restMsg, 30, restMsg.length), "ASCII");
            String domFinal = rmPadd(dom.toCharArray());
            String usrFinal = rmPadd(usr.toCharArray());


            String pKeyString = printBase64Binary(pKeyBytes);
            String domainString = domFinal;
            String usernameString = usrFinal;

            //String elements = domainString+" "+usernameString;


            File file = new File(DataFileLoc);
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String line;
            Path path = Paths.get(DataFileLoc);

            Charset charset = Charset.forName("ISO-8859-1");

            List<String> lines = Files.readAllLines(path,charset);

            int i=0;
            Boolean newData = true;
            while((line = br.readLine()) != null){
                if(line.equals(pKeyString)){
                    line = br.readLine();
                    if (line.equals(domainString)) {
                        line = br.readLine();
                        if (line.equals(usernameString)){
                            line = br.readLine();
                            line = br.readLine();
                            return Timestamp.valueOf(line);
                        }
                        else{
                            br.readLine();
                            br.readLine();
                            br.readLine();
                        }
                    }
                    else{
                        br.readLine();
                        br.readLine();
                        br.readLine();
                        br.readLine();
                    }
                }
                else{
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    br.readLine();
                }
                i+=6;
            }

        }
        catch (Exception e){
            System.out.println("Error: Couldn't locate the file.");
            e.printStackTrace();
            return null;
        }

        return null;
    }

    public byte[] getServerSignature(byte[] message){

        byte[] pKeyBytes = null;
        ClientClass client = clientList.get(0);
        byte[] restMsg = null;
        for(ClientClass element: clientList) {



            try {
                byte[] Bmsg = DecryptCommunication(message, element.getSessionKey());
                pKeyBytes = copyOfRange(Bmsg,0,294); // parte da chave publica
                restMsg = copyOfRange(Bmsg, 294, Bmsg.length); // resto dos argumentos
                client = element;

            }
            catch(Throwable e){

            }
        }
        if(pKeyBytes == null){return null;}

        try {
            String dom = new String(copyOfRange(restMsg, 0, 30), "ASCII");
            String usr = new String(copyOfRange(restMsg, 30, restMsg.length), "ASCII");
            String domFinal = rmPadd(dom.toCharArray());
            String usrFinal = rmPadd(usr.toCharArray());


            String pKeyString = printBase64Binary(pKeyBytes);
            String domainString = domFinal;
            String usernameString = usrFinal;

            //String elements = domainString+" "+usernameString;


            File file = new File(DataFileLoc);
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String line;
            Path path = Paths.get(DataFileLoc);

            Charset charset = Charset.forName("ISO-8859-1");

            List<String> lines = Files.readAllLines(path,charset);

            int i=0;
            Boolean newData = true;
            while((line = br.readLine()) != null){
                if(line.equals(pKeyString)){
                    line = br.readLine();
                    if (line.equals(domainString)) {
                        line = br.readLine();
                        if (line.equals(usernameString)){
                            line = br.readLine();
                            line = br.readLine();
                            line = br.readLine();
                            return parseBase64Binary(line);
                        }
                        else{
                            br.readLine();
                            br.readLine();
                            br.readLine();
                        }
                    }
                    else{
                        br.readLine();
                        br.readLine();
                        br.readLine();
                        br.readLine();
                    }
                }
                else{
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    br.readLine();
                }
                i+=6;
            }

        }
        catch (Exception e){
            System.out.println("Error: Couldn't locate the file.");
            e.printStackTrace();
            return null;
        }

        return null;
    }


    private int getLastNumber(){

        int number = -1;
        String line = "";

        try {
            File file = new File(DataFileLoc);
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);

            while((line =br.readLine())!= null){
                for(int i = 0; i < 2; i++){
                    line = br.readLine();
                }
                number = Integer.parseInt(br.readLine());
                br.readLine();
                br.readLine();
            }

            return number;
        }
        catch(Exception e){
            e.printStackTrace();
            return number;      //No caso de não ter nenhum valor
        }



    }


    public static String rmPadd(char[] s)throws Exception
    {
        if(s[28] == '0')
        {
            char c = s[29];
            int x = Character.getNumericValue(c);
            char[] fin = copyOfRange(s,0, 28-x);
            return concatenate(fin);
        }
        else
        {
            char[] c = copyOfRange(s,28,30);
            int x = Integer.parseInt(new String(c));
            char[] fin = copyOfRange(s,0,28-x);
            return concatenate(fin);
        }

    }

    private static String concatenate (char[] c){
        String str = "";
        for(char a: c){
            str += a;
        }
        return str;
    }


    public static byte[] makeDigitalSignature(byte[] bytes, PrivateKey privateKey) throws Exception {

        // get a signature object using the SHA-1 and RSA combo
        // and sign the plaintext with the private key
        Signature sig = Signature.getInstance("SHA256WithRSA");
        sig.initSign(privateKey);
        sig.update(bytes);
        byte[] signature = sig.sign();

        return signature;
    }

    public static byte[] makeServerDigitalSignature(byte[] bytes) throws Exception {

        // get a signature object using the SHA-1 and RSA combo
        // and sign the plaintext with the private key
        Signature sig = Signature.getInstance("SHA256WithRSA");
        sig.initSign((PrivateKey)ServerPrivateKey);
        sig.update(bytes);
        byte[] signature = sig.sign();

        return signature;
    }

    //Verifies that the data received is from the expected entity with the that entity's public key
    public static boolean verifyDigitalSignature(byte[] signature, byte[] message, PublicKey publicKeyClient) throws Exception {

        // verify the signature with the public key
        Signature sig = Signature.getInstance("SHA256WithRSA");

        sig.initVerify(publicKeyClient);

        sig.update(message);
        try {
            return sig.verify(signature);
        } catch (SignatureException se) {
            System.out.println("Caught exception while verifying " + se);
            return false;
        }
    }

    public static boolean verifyServerDigitalSignature(byte[] signature, byte[] message) throws Exception {

        // verify the signature with the public key
        Signature sig = Signature.getInstance("SHA256WithRSA");

        sig.initVerify(ServerPublicKey);

        sig.update(message);
        try {
            return sig.verify(signature);
        } catch (SignatureException se) {
            System.out.println("Caught exception while verifying " + se);
            return false;
        }
    }

    public byte[] getDigitalSignature(byte[] PublicKey){

        ClientClass client = null;

        for(ClientClass element: clientList){

            try {
                DecryptCommunication(PublicKey, element.getSessionKey());
                client = element;
            }
            catch(Exception e){

            }

        }

        try {
            return EncryptCommunication(client.getLastSignature(), client.getSessionKey());
        }
        catch(Exception e){
            System.out.println("Error retrieving digital signature: " + e);
            return null;
        }
    }
    public byte[] retrievePassword(byte[] message, byte[] password){
        ClientClass client = null;

        for(ClientClass element: clientList){

            try {
                DecryptCommunication(message, element.getSessionKey());
                client = element;
            }
            catch(Exception e){
            }

        }

        byte[] bytes = null;

        try {

            ArrayList<byte[]> list = new ArrayList<>();
            list.add(password);
            list.add(client.getNextNonce());




            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (byte[] element : list) {
                //out.write(element);
                baos.write(element);
            }
            bytes = baos.toByteArray();

            client.setSignature(makeDigitalSignature(bytes, (PrivateKey)ServerPrivateKey)); //Assina o plaintext

            return EncryptCommunication(bytes, client.getSessionKey());
        }
        catch(Exception e){
           // e.printStackTrace();
        }

        return null;

    }

    public byte[] get( byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce, int id) throws Exception{

        SharedMemoryRegister obj = new SharedMemoryRegister();

        byte[] password = null;
        if(portList.size() == 0) { // Se não houver replicas, lemos do ficheiro
            password = getPass(message, signature, nonce, signatureNonce);
        }else{
            for(ClientClass c : clientList) {
                if(c.id == id) {
                    c.myReg.read(message, signature, nonce, signatureNonce, Integer.parseInt(myPort), id);
                    obj = c.myReg;
                }
            }
            password = obj.value.password;
        }


        return retrievePassword(message, password);
    }

    public byte[] divideMessage(byte[] message) {

        byte[] pKeyBytes = null;
        ClientClass client = clientList.get(0);
        byte[] restMsg = null;
        for (ClientClass element : clientList) {


            try {
                byte[] Bmsg = DecryptCommunication(message, element.getSessionKey());
                pKeyBytes = copyOfRange(Bmsg, 0, 294); // parte da chave publica
                restMsg = copyOfRange(Bmsg, 294, Bmsg.length); // resto dos argumentos
                client = element;

            } catch (Throwable e) {

            }
        }

        return copyOfRange(restMsg, 60, restMsg.length);
    }

    public byte[] getPass( byte[] message, byte[] signature, byte[] nonce, byte[] signatureNonce){

        byte[] pKeyBytes = null;
        ClientClass client = clientList.get(0);
        byte[] restMsg = null;
        byte[] decryptNonce = null;
        for(ClientClass element: clientList) {



            try {
                byte[] Bmsg = DecryptCommunication(message, element.getSessionKey());
                pKeyBytes = copyOfRange(Bmsg,0,294); // parte da chave publica
                restMsg = copyOfRange(Bmsg, 294, Bmsg.length); // resto dos argumentos
                decryptNonce = DecryptCommunication(nonce, element.getSessionKey());
                client = element;

            }
            catch(Throwable e){

            }
        }
        if(pKeyBytes == null){return null;}

        PublicKey ClientPublicKey = null;
        try {
            ClientPublicKey =
                    KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pKeyBytes));

        if(!verifyDigitalSignature(signature, message, ClientPublicKey)&&!verifyDigitalSignature(signatureNonce, decryptNonce, ClientPublicKey)){ //If true, signature checks
            return null;
        }

            storageSignture(client, signature);

        if(!client.checkNonce(decryptNonce)){
            return null;
        }

        }
        catch(Exception e){
            System.err.println("(Retrieve)Signature error: " + e.toString());
            e.printStackTrace();
        }

        try {
            String dom = new String(copyOfRange(restMsg, 0, 30), "ASCII");
            String usr = new String(copyOfRange(restMsg, 30, restMsg.length), "ASCII");
            String domFinal = rmPadd(dom.toCharArray());
            String usrFinal = rmPadd(usr.toCharArray());


            String pKeyString = printBase64Binary(pKeyBytes);
            String domainString = domFinal;
            String usernameString = usrFinal;

            //String elements = domainString+" "+usernameString;


            File file = new File(DataFileLoc);
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String line;
            Path path = Paths.get(DataFileLoc);

            Charset charset = Charset.forName("ISO-8859-1");

            List<String> lines = Files.readAllLines(path,charset);

            int i=0;
            Boolean newData = true;
            while((line = br.readLine()) != null){
                if(line.equals(pKeyString)){
                    line = br.readLine();
                    if (line.equals(domainString)) {
                        line = br.readLine();
                        if (line.equals(usernameString)){
                            line = br.readLine();
                            //System.out.println(line);
                            //System.out.println(parseBase64Binary(line));
                            return readByteCode(Integer.parseInt(line));
                        }
                        else{
                            br.readLine();
                            br.readLine();
                            br.readLine();
                        }
                    }
                    else{
                        br.readLine();
                        br.readLine();
                        br.readLine();
                        br.readLine();
                    }
                }
                else{
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    br.readLine();
                }
                i+=6;
            }

        }
        catch (Exception e){
            System.out.println("Error: Couldn't locate the file.");
            e.printStackTrace();
            return "noFile".getBytes();
        }

        return null;
    }


    public void register(byte[] pubKey, ClientInterface c) throws Exception{

        SecretKey SessKey = generateSession();

        Lock lock = new ReentrantLock();
        lock.lock();
        totalId ++;
        int id = totalId; // we copy global totalID to a local id to pass to the client
        addClient(pubKey,SessKey, totalId);
        lock.unlock();

        PublicKey realClientPubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKey));


        //pass the session key to client
        // FALTA DIGITAL SIGNATURE!!!!!!!!!!!!!!!!!!!!!!!!!!
        c.setSessionKey(EncryptionAssymmetric(SessKey.getEncoded(),realClientPubKey),EncryptionAssymmetric(Integer.toString(id).getBytes(),realClientPubKey));
        int newNonce = 0;
        for(ClientClass client: clientList){
            if(client.getPublicKey().equals(realClientPubKey) && client.getSessionKey().equals(SessKey)){
                 newNonce = Integer.parseInt(new String(DecryptCommunication(c.getNonce(), SessKey), "ASCII"));
                client.setNonce(newNonce);
            }
        }

        reg.broadcastRegister(EncryptionAssymmetric(SessKey.getEncoded(), ServerPublicKey), realClientPubKey, EncryptionAssymmetric(Integer.toString(id).getBytes(),ServerPublicKey), newNonce);
    }





    private SecretKey generateSession()throws Exception {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(128);
        return keygen.generateKey();

    }

    private void addClient(byte[] clientPublicKey, SecretKey sessionKey, int id){

        SharedMemoryRegister reg = new SharedMemoryRegister();
        try {
            clientList.add(new ClientClass(sessionKey, KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(clientPublicKey)),id, reg));
        }catch(Exception e){
            System.out.println("Error adding client: "+ e);
        }
    }



}
