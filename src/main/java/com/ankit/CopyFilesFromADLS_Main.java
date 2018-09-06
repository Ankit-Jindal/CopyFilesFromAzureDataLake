package com.ankit;

import com.ankit.com.ankit.adlsfile.ADLSToLocalFileCopy;
import com.ankit.com.ankit.util.ADLSClinetUtil;
import com.ankit.com.ankit.util.Constants;
import com.microsoft.azure.datalake.store.ADLStoreClient;
import com.microsoft.azure.datalake.store.DirectoryEntry;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class CopyFilesFromADLS_Main {
    private static final Logger LOGGER = Logger.getLogger(CopyFilesFromADLS_Main.class);

    public static void main(String[] args) {

        LOGGER.info("Starting File Downloader");

        ADLStoreClient client = ADLSClinetUtil.getADLStoreClient();

        String sourceDirPath = "/test-io-tahoe/CSV/CRACSV"; //"/test-io-tahoe/CSV/CRACSV/";
        String sourceFileName = "ADDRESS_TYPES.csv";
        String sourceFileWithPath = sourceDirPath + sourceFileName;

        String destiNationFilePath = "/Users/ankitkumar/Documents/POCs/ExploreAzure/data/";
        String destFileWithPath = destiNationFilePath + sourceFileName;

        List<String> fileNames = new ArrayList<>();
        allFilesInAzureDir(sourceDirPath, client, fileNames);

        ExecutorService service = Executors.newFixedThreadPool(10);

        List<Future> taskStatus = new ArrayList<>();

        CountDownLatch latch = new CountDownLatch(fileNames.size());
        Set<String> processedFile = new HashSet<>();
        for (String fileName : fileNames) {
            ADLSToLocalFileCopy copyfromADl = new ADLSToLocalFileCopy(fileName, destiNationFilePath, client, latch);
            taskStatus.add(service.submit(copyfromADl));
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
/*        while (processedFile.size() != fileNames.size()) {
            System.out.println(processedFile);
            for (Future<String> result : taskStatus) {
                try {
                    String copiedFile = result.get(50000, TimeUnit.MILLISECONDS);
                    System.out.println("Copied File " + copiedFile);
                    //taskStatus.remove(result);
                    processedFile.add(copiedFile);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        }*/
        LOGGER.info("Exiting service");
        service.shutdown();
        LOGGER.info("Exiting Main");
    }

    private static List<String> allFilesInAzureDir(String baseDir, ADLStoreClient client, List<String> sourceFiles){
        try {
            DirectoryEntry directoryEntry = client.getDirectoryEntry(baseDir);
            if(Constants.DIRECTORY.equals(directoryEntry.type.name())){
                List<DirectoryEntry> directoryEntries = client.enumerateDirectory(baseDir);
                for (DirectoryEntry dirEntry : directoryEntries) {
                    //printDirectoryInfo(dirEntry);
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
