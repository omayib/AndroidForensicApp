package id.co.technomotion.androidforensicapp.helper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by omayib on 5/17/15.
 */
public class FileCompressor {
    public static void compresToZip(String outputPath,String filePath,String fileName) {
        System.out.println("zip:"+outputPath);
        System.out.println("zip:"+filePath);
        System.out.println("zip:"+fileName);
        byte[] buffer = new byte[1024];

        try{

            FileOutputStream fos = new FileOutputStream(outputPath+fileName+".zip");
            ZipOutputStream zos = new ZipOutputStream(fos);
            ZipEntry ze= new ZipEntry(fileName);
            zos.putNextEntry(ze);
            FileInputStream in = new FileInputStream(filePath);

            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }

            in.close();
            zos.closeEntry();

            //remember close it
            zos.close();

            System.out.println("Done");

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

}
