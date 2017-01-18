package com.kovtun.producdetector.Models;

/**
 * Created by kovtun on 23.06.2016.
 */
public class SettingsModel {
    private String ip;
    private int port;
    private String BaseName;
    private String logIn;
    private String password;
    private int shopId;

    public SettingsModel(String ip, int port, String baseName, String logIn, String password,int shopId) {
        this.ip = ip;
        this.port = port;
        BaseName = baseName;
        this.logIn = logIn;
        this.password = password;
        this.shopId=shopId;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getBaseName() {
        return BaseName;
    }

    public String getLogIn() {
        return logIn;
    }

    public String getPassword() {
        return password;
    }

    public int getShopId() {
        return shopId;
    }
}
