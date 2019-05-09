package com.example.babysitter;

import android.net.nsd.NsdServiceInfo;

public class NetworkUtils {

    public NetworkUtils() {
        super();
    }

    public void registerService(int port) {
        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName("BabySitter");
        serviceInfo.setServiceType("_nsdchat._tcp");
        serviceInfo.setPort(port);

    }
}
