package id.co.technomotion.androidforensicapp.helper;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by omayib on 5/17/15.
 */
public class FileHash {
    public static String sha1(final File file) {
        String hash="";
        try {
            //String datafile = "c:\\INSTLOG.TXT";
            MessageDigest md = MessageDigest.getInstance("SHA1");

            FileInputStream fis = new FileInputStream(file.getPath());
            byte[] dataBytes = new byte[1024];

            int nread = 0;

            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
            ;

            byte[] mdbytes = md.digest();

            //convert the byte to hex format
            StringBuffer sb = new StringBuffer("");
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            hash=sb.toString();
            System.out.println("Digest(in hex format):: " + sb.toString());
            String hashFileName=file.getName()+"_sha1_checksum.txt";
            String hashPath=file.getPath().substring(0,file.getPath().lastIndexOf("/"))+"/"+hashFileName;
            System.out.println("hash path "+hashPath);
            PrintStream out=new PrintStream(new FileOutputStream(hashPath));
            out.print(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hash;
    }
 }
