package com.ankit;

import com.microsoft.azure.datalake.store.ADLStoreClient;
import com.microsoft.azure.datalake.store.DirectoryEntry;

import java.io.*;
import java.util.concurrent.Callable;

public class ADLTOLocalFileCopy implements Callable<String> {

    private final String sourceFile;
    private final String destinationBaseDir;
    private final ADLStoreClient client;

   public ADLTOLocalFileCopy(String sourceFile, String destinationBaseDir, ADLStoreClient client) {
        this.sourceFile = sourceFile;
        this.destinationBaseDir = destinationBaseDir;
        this.client = client;
    }

    @Override
    public String call() {
        writeFile(this.sourceFile, this.destinationBaseDir, this.client);
        return sourceFile;
    }

    private void writeFile(String sourceFile, String destinationBaseDir, ADLStoreClient client) {

        System.out.println("Starting Copy of " + sourceFile);
       long start = System.currentTimeMillis();
       long totalBytesWritten = 0l;

       long totalFileSize = 0l;
        try {
            DirectoryEntry den = client.getDirectoryEntry(sourceFile);
            totalFileSize = den.length;
        } catch (IOException e) {
            e.printStackTrace();
        }
        int bufferSize = 1048;
        if(totalFileSize > 2097152){
            bufferSize = 8 * 1024 * 256;
        }

       int idx = sourceFile.lastIndexOf("/");
       String sourceDirPath = sourceFile.substring(0, idx);
       String sourceFileName = sourceFile.substring(idx+1);

       String localDirPath = destinationBaseDir + sourceDirPath;
        File dirFile = new File(localDirPath);
        if(!dirFile.exists()){
            dirFile.mkdirs();
        }

        String destinationFile = sourceFileName;
        File destination = new File(localDirPath,destinationFile);

        // Read File
        InputStream in = null;
        OutputStream os = null;
        try {
            in = client.getReadStream(sourceFile);
            os = new FileOutputStream(destination);
            byte[] buffer = new byte[bufferSize];
            int length;
            while ((length = in.read(buffer)) > 0) {
                os.write(buffer, 0, length);
                totalBytesWritten += length;
                System.out.println("Bytes Written  "+ sourceFileName + " : "+ totalBytesWritten);
            }

        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        long totalTimeInSec = (endTime - start)/1000;
        System.out.println("Copied File : " + sourceFileName + " in Time(Sec) : " + totalTimeInSec);
    }
}
