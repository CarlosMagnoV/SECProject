package pt.ist.sec;

import com.sun.org.apache.regexp.internal.RE;

import java.sql.Timestamp;

public class ReadListReplicas {

   public byte[] password;
   public Timestamp ts;

   public ReadListReplicas(byte[] password, Timestamp ts){
      this.password = password;
      this.ts = ts;
   }
}
