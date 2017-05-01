package pt.ist.sec;

import com.sun.org.apache.regexp.internal.RE;

import java.sql.Timestamp;

public class ReadListReplicas {

   public byte[] password;
   public Timestamp ts;
   public byte[] serverSignature;

   public ReadListReplicas(byte[] password, Timestamp ts, byte[] serverSignature){
      this.password = password;
      this.ts = ts;
      this.serverSignature = serverSignature;
   }
}
