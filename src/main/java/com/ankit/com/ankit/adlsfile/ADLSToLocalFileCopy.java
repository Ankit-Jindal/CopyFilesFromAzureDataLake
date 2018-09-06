package com.ankit.com.ankit.adlsfile;

import com.microsoft.azure.datalake.store.ADLStoreClient;
import com.microsoft.azure.datalake.store.DirectoryEntry;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class ADLSToLocalFileCopy implements Callable<String> {

    private static final Logger LOGGER = Logger.getLogger(ADLSToLocalFileCopy.class);

    private final String sourceFile;
    private final String destinationBaseDir;
    private final ADLStoreClient client;
    private final CountDownLatch latch;

    public ADLSToLocalFileCopy(String sourceFile, String destinationBaseDir, ADLStoreClient client, CountDownLatch latch) {
        this.sourceFile = sourceFile;
        this.destinationBaseDir = destinationBaseDir;
        this.client = client;
        this.latch = latch;
    }

    @Override
    public String call() throws Exception {
        try {
            String copiedFileName = writeFile(this.sourceFile, this.destinationBaseDir, this.client);
            latch.countDown();
            return  copiedFileName;
        } catch (Exception e) {
            LOGGER.error("Copy Failed " + sourceFile , e);
            throw  e;
        }
        //return sourceFile;
    }

    private String writeFile(String sourceFile, String destinationBaseDir, ADLStoreClient client) throws IOException, Exception{

        LOGGER.debug("File Copy Initiated for file " + sourceFile);

        // to calculate total time taken in copy from azure to local
        long start = System.currentTimeMillis();

        // to calculate the buffer size on the bases of file size in azure
        long totalFileSize = 0l;
        try {
            DirectoryEntry den = client.getDirectoryEntry(sourceFile);
            totalFileSize = den.length;
            LOGGER.info("FILE :: SIZE (bytes) " + sourceFile +" :: " + totalFileSize);

        // Size in bits
        int bufferSize = 1048;
        if(totalFileSize > 2097152){  // 2097152 = 256 KB [Size of block in spinning disk]
            bufferSize = 8 * 1024 * 256;
        }

        int idx = sourceFile.lastIndexOf("/");
        String sourceDirPath = sourceFile.substring(0, idx);
        String sourceFileName = sourceFile.substring(idx+1);

        String localDirPath = destinationBaseDir + sourceDirPath;

        File dirFile = new File(localDirPath);
        if(!dirFile.exists()){
            if (!dirFile.mkdirs()) throw new Exception("Not able to create directories " + localDirPath);
        }

        String destinationFile = sourceFileName;

        LOGGER.info("Source File :: Destination File " + sourceFile + " :: " + localDirPath + "/" + destinationFile);

        File destination = new File(localDirPath,destinationFile);

        boolean iscopySuccess = copyFile(sourceFile, destination, client, bufferSize);

        LOGGER.info("Copied File : " + sourceFileName + " in Time(Sec) : " + (System.currentTimeMillis() - start)/1000);
        return sourceFile;
        } catch (IOException e) {
            LOGGER.error("Failed to copy File : " + sourceFile, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("File Copy Failed " , e);
            throw e;
        }
    }

    private boolean copyFile(String sourceFile_ADLS, File destinaltionFile, ADLStoreClient adlsClinet, int bufferSize) throws IOException{

        // to manage the progress
        boolean isCopySucces = true;
        long totalBytesWritten = 0l;

        InputStream in = null;
        OutputStream os = null;
        try {
            in = client.getReadStream(sourceFile_ADLS);
            os = new FileOutputStream(destinaltionFile);
            byte[] buffer = new byte[bufferSize];
            int length;
            while ((length = in.read(buffer)) > 0) {
                os.write(buffer, 0, length);
                totalBytesWritten += length;
                LOGGER.info("Bytes Written  "+ sourceFile_ADLS + " : "+ totalBytesWritten);
            }
        } catch (IOException ex) {
            LOGGER.error("File copy failed for " + sourceFile_ADLS, ex);
            isCopySucces = false;
            throw ex;
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
        return isCopySucces;
    }
}
