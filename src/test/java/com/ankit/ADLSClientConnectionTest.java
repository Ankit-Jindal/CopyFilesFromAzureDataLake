package com.ankit;

import com.ankit.com.ankit.util.ADLSClinetUtil;
import com.microsoft.azure.datalake.store.ADLStoreClient;
import org.junit.Assert;
import org.junit.Test;

public class ADLSClientConnectionTest {

    @Test
    public void testGetADLStoreClient(){
        ADLStoreClient client = ADLSClinetUtil.getADLStoreClient();
        Assert.assertTrue(client != null);
    }
}
