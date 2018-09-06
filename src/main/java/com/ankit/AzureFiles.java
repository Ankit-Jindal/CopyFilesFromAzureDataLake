package com.ankit;

import com.microsoft.azure.datalake.store.ADLException;
import com.microsoft.azure.datalake.store.ADLStoreClient;
import com.microsoft.azure.datalake.store.DirectoryEntry;
import com.microsoft.azure.datalake.store.IfExists;
import com.microsoft.azure.datalake.store.oauth2.AccessTokenProvider;
import com.microsoft.azure.datalake.store.oauth2.ClientCredsTokenProvider;
import com.microsoft.azure.datalake.store.oauth2.DeviceCodeTokenProvider;

import java.io.*;
import java.util.Arrays;
import java.util.List;
public class AzureFiles {
    private static String accountFQDN = "iotahoe.azuredatalakestore.net"; //"FILL-IN-HERE";  // full account FQDN, not just the account name
/*    private static String clientId = "76ca35ac-2d18-46c8-8ee1-c67bd810aeba"; //[User Object ID]
    private static String authTokenEndpoint = "https://login.microsoftonline.com/io-tahoe.com/oauth2/token";
    private static String clientKey = "FILL-IN-HERE";*/

    //713485e0-96a4-4e30-902a-1ca52b11485a [Subscription ID]

    //"e9f49c6b-5ce5-44c8-925d-015017e9f7ad";

    private static String nativeAppId = "3e557ce9-9461-4560-abf0-393ebf6b6aa4"; //[App ID]

    //private static String nativeAppId = "a45a25d0-e556-4b87-b9df-6f61e664d02e";
    public static void main(String[] args) {
/*        AccessTokenProvider provider = new ClientCredsTokenProvider(authTokenEndpoint, clientId, clientKey);
        ADLStoreClient client = ADLStoreClient.createClient(accountFQDN, provider);


        */

        try {
            AccessTokenProvider provider = new DeviceCodeTokenProvider(nativeAppId);
            ADLStoreClient client = ADLStoreClient.createClient(accountFQDN, provider);

            DirectoryEntry entr = client.getDirectoryEntry("/");
            //client.getReadStream()

            System.out.println(entr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


//76ca35ac-2d18-46c8-8ee1-c67bd810aeba