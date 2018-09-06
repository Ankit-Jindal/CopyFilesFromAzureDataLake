package com.ankit.com.ankit.util;

import com.ankit.ADLProperties;
import com.microsoft.azure.datalake.store.ADLStoreClient;
import com.microsoft.azure.datalake.store.oauth2.AccessTokenProvider;
import com.microsoft.azure.datalake.store.oauth2.ClientCredsTokenProvider;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ADLSClinetUtil {

private static final Logger LOGGER = Logger.getLogger(ADLSClinetUtil.class);

    private static final Properties properties;
    static {
        properties = new Properties();
        try {
            InputStream propertyStream = ADLProperties.class.getClassLoader().getResourceAsStream("configuration.properties");
            if (propertyStream != null) {
                properties.load(propertyStream);
            } else {
                throw new RuntimeException();
            }
        } catch (RuntimeException | IOException e) {
           LOGGER.error("Failed to ADLStoreClient properties.", e);
        }
    }

    public static Properties getProperties(){
        return properties;
    }

    private static Properties loadADLClientConnProperties() {
        Properties adlClientConnprop = new Properties();
        try {
            InputStream propertyStream = ADLProperties.class.getClassLoader().getResourceAsStream("configuration.properties");
            if (propertyStream != null) {
                adlClientConnprop.load(propertyStream);
            } else {
                throw new RuntimeException();
            }
        } catch (RuntimeException | IOException e) {
            LOGGER.error("Failed to ADLStoreClient properties.", e);
        }
        return adlClientConnprop;
    }

    public static ADLStoreClient getADLStoreClient() {

        Properties adlClientConnprop = loadADLClientConnProperties();
        String accountFQDN = adlClientConnprop.getProperty(Constants.ACCOUNT_FQDN);
        String clientId = adlClientConnprop.getProperty(Constants.CLIENT_ID);
        String authTokenEndpoint = adlClientConnprop.getProperty(Constants.AUTH_TOKEN_ENDPOINT);
        String clientKey = adlClientConnprop.getProperty(Constants.CLIENT_KEY);

        LOGGER.info("Creating ADLStoreClient for client id " + clientId);

        AccessTokenProvider provider = new ClientCredsTokenProvider(authTokenEndpoint, clientId, clientKey);
        ADLStoreClient adlStoreClient = ADLStoreClient.createClient(accountFQDN, provider);

        LOGGER.info("Created ADLStoreClient for client id " + clientId);

        return  adlStoreClient;
    }
}
