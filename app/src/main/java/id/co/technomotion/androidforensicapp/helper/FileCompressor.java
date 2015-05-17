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

            // delete original data
            new File(filePath).delete();
            new File(tarArchivePath).delete();

            System.out.println("Done");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
