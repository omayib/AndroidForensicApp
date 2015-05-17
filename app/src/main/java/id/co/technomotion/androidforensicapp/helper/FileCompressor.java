package id.co.technomotion.androidforensicapp.helper;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2Utils;
import org.apache.commons.compress.utils.IOUtils;
import org.itadaki.bzip2.BZip2OutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import id.co.technomotion.androidforensicapp.tar.TarEntry;
import id.co.technomotion.androidforensicapp.tar.TarOutputStream;

/**
 * Created by omayib on 5/17/15.
 */
public class FileCompressor {
    public static void compressToTarBz2(String outputPath,String filePath,String fileName){
        byte[] buffer = new byte[1024];
        String tarArchivePath=outputPath+fileName+".tar";
        String tarBz2FilePath=tarArchivePath+".bz2";
        try {

            //https://github.com/kamranzafar/jtar
            File originalFile=new File(filePath);

            // Output file stream
            FileOutputStream dest = new FileOutputStream(tarArchivePath);

            // Create a TarOutputStream
            TarOutputStream out = new TarOutputStream( new BufferedOutputStream( dest ) );

            out.putNextEntry(new TarEntry(originalFile, originalFile.getName()));
            BufferedInputStream origin = new BufferedInputStream(new FileInputStream(originalFile));
            int count;
            byte data[] = new byte[2048];

            while((count = origin.read(data)) != -1) {
                out.write(data, 0, count);
            }

            out.flush();
            origin.close();
            out.close();

            // now, lets compress using bz2 algorithm
            // reference : http://code.google.com/p/jbzip2/source/browse/trunk/jbzip2/src/demo/Compress.java
//            File outputBz2File=new File(tarBz2FilePath);
//            System.out.println("tar file ready "+outputBz2File.getPath());
//            InputStream inputStream=new BufferedInputStream(new FileInputStream(new File(tarArchivePath)));
//            OutputStream outputStream=new BufferedOutputStream(new FileOutputStream(outputBz2File),1024);
//            BZip2OutputStream bZip2OutputStream=new BZip2OutputStream(outputStream);
//
//            byte[] bz2buffer=new byte[1024];
//            int bytesRead;
//            while ((bytesRead=inputStream.read(bz2buffer))!=-1){
//                bZip2OutputStream.write(bz2buffer,0,bytesRead);
//            }
//            outputStream.close();


            File bz2File=new File(tarBz2FilePath);
            byte[] buf = new byte[1024];
            int len;

            BZip2CompressorOutputStream bz2Out=new BZip2CompressorOutputStream(new FileOutputStream(bz2File));
            FileInputStream fileInputStream=new FileInputStream(new File(tarArchivePath));

            // Transfer bytes from the input file to the GZIP output stream
            while ((len = fileInputStream.read(buf)) > 0) {
                bz2Out.write(buf, 0, len);
            }

            fileInputStream.close();

            // Complete the GZIP file
            bz2Out.finish();
            bz2Out.close();

            FileHash.sha1(new File(tarBz2FilePath));
            System.out.println("Done");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void compresToZip(String outputPath,String filePath,String fileName) {
        System.out.println("zip:"+outputPath);
        System.out.println("zip:"+filePath);
        System.out.println("zip:"+fileName);
        byte[] buffer = new byte[1024];

        try{
            String fileCompressedPath=outputPath+fileName+".zip";
            FileOutputStream fos = new FileOutputStream(fileCompressedPath);
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

            FileHash.sha1(new File(fileCompressedPath));
            System.out.println("Done");
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

}
