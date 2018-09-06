package com.ankit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ADLProperties {

    public final static String accountFQDN;
    public final static String clientId;
    public final static String authTokenEndpoint;
    public final static String clientKey;

static {
    Properties prop = new Properties();
    try {
        InputStream propertyStream = ADLProperties.class.getClassLoader().getResourceAsStream("configuration.properties");
        if (propertyStream != null) {
            prop.load(propertyStream);
        } else {
            throw new RuntimeException();
        }
    } catch (RuntimeException | IOException e) {
        System.out.println("\nFailed to load config.properties file.");

    }

    accountFQDN = prop.getProperty("accountFQDN");
    clientId = prop.getProperty("clientId");
    authTokenEndpoint = prop.getProperty("authTokenEndpoint");
    clientKey = prop.getProperty("clientKey");

}
/*    public static Properties loadProperties() {
        Properties prop = new Properties();
        try {
            InputStream propertyStream = ADLProperties.class.getClassLoader().getResourceAsStream("configuration.properties");
            if (propertyStream != null) {
                prop.load(propertyStream);
            } else {
                throw new RuntimeException();
            }
        } catch (RuntimeException | IOException e) {
            System.out.println("\nFailed to load config.properties file.");

        }

        accountFQDN = prop.getProperty("accountFQDN");
        clientId = prop.getProperty("clientId");
        authTokenEndpoint = prop.getProperty("authTokenEndpoint");
        clientKey = prop.getProperty("clientKey");

        return prop;
    }*/
}
