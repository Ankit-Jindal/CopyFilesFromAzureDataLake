package com.ankit;

import com.ankit.com.ankit.util.ADLSClinetUtil;
import com.microsoft.azure.datalake.store.ADLStoreClient;
import com.microsoft.azure.datalake.store.DirectoryEntry;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class AccesAzureFiles {

    private static final Logger LOGGER = Logger.getLogger(AccesAzureFiles.class);

    public static void main(String[] args) {

        LOGGER.info("Starting File Downloader");

        ADLStoreClient client = ADLSClinetUtil.getADLStoreClient();

        String sourceDirPath = "/test-io-tahoe"; //"/test-io-tahoe/CSV/CRACSV/";
        String sourceFileName = "ADDRESS_TYPES.csv";
        String sourceFileWithPath = sourceDirPath + sourceFileName;

        String destiNationFilePath = "/Users/ankitkumar/Documents/POCs/downladfromazure/Test/";
        String destFileWithPath = destiNationFilePath + sourceFileName;

        List<String> fileNames = new ArrayList<>();
        allFilesInAzureDir(sourceDirPath, client, fileNames);

        ExecutorService service = Executors.newFixedThreadPool(10);

        List<Future> taskStatus = new ArrayList<>();

        Set<String> processedFile = new HashSet<>();
       for (String fileName : fileNames) {
           ADLTOLocalFileCopy copyfromADl = new ADLTOLocalFileCopy(fileName,destiNationFilePath,client);
           taskStatus.add(service.submit(copyfromADl));
        }


        while(processedFile.size() != fileNames.size()) {
            System.out.println(processedFile);
            for (Future<String> result : taskStatus) {
                try {
                    String copiedFile = result.get(10000, TimeUnit.MILLISECONDS);
                    System.out.println("Copied File " + copiedFile);
                    taskStatus.remove(copiedFile);
                    processedFile.add(copiedFile);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Exiting service");
        service.shutdown();
        System.out.println("Exiting Main");
    }

    private static List<String> allFilesInAzureDir(String baseDir, ADLStoreClient client, List<String> sourceFiles){
        try {
            DirectoryEntry directoryEntry = client.getDirectoryEntry(baseDir);
            if("DIRECTORY".equals(directoryEntry.type.name())){
                List<DirectoryEntry> directoryEntries = client.enumerateDirectory(baseDir);
                for (DirectoryEntry dirEntry : directoryEntries) {
                    printDirectoryInfo(dirEntry);
                    allFilesInAzureDir(dirEntry.fullName, client, sourceFiles);
                }

            } else {
                sourceFiles.add(directoryEntry.fullName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  sourceFiles;
    }

    private static void writeFile(String sourceFile, String destinationFile, ADLStoreClient client) {

        int indx  = destinationFile.lastIndexOf("/");

        String dirPath = destinationFile.substring(0,indx);

        File dirFile = new File(dirPath);

        if(!dirFile.exists()){
            dirFile.mkdirs();
        }

        File destination = new File(destinationFile);

        // Read File
        InputStream in = null;
        OutputStream os = null;
        try {
            in = client.getReadStream(sourceFile);
            os = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                os.write(buffer, 0, length);
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
    }

    private static void printDirectoryInfo(DirectoryEntry ent) {
        System.out.format("Name: %s%n", ent.name);
        System.out.format("  FullName: %s%n", ent.fullName);
        System.out.format("  Length: %d%n", ent.length);
        System.out.format("  Type: %s%n", ent.type.toString());
        System.out.format("  Group: %s%n", ent.group);
        System.out.format("  User: %s%n", ent.user);
        System.out.format("  Permission: %s%n", ent.permission);
        System.out.format("  mtime: %s%n", ent.lastModifiedTime.toString());
        System.out.format("  atime: %s%n", ent.lastAccessTime.toString());
        System.out.println();
    }
}
