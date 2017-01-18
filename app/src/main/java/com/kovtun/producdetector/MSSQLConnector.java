package com.kovtun.producdetector;

import com.kovtun.producdetector.Models.SettingsModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by kovtun on 23.06.2016.
 */
public class MSSQLConnector {
    private static volatile Connection instance;

    private MSSQLConnector() {
    }

    public static Connection getInstance(SettingsModel settings) throws SQLException, ClassNotFoundException
    {
        Properties connInfo = new Properties();System.out.println(2);
        Class.forName("net.sourceforge.jtds.jdbc.Driver");System.out.println(3);
        String ConnURL = "jdbc:jtds:sqlserver://"+settings.getIp()+":"+settings.getPort()+"/"+settings.getBaseName();
        System.out.println(4+" "+ConnURL);
        instance = DriverManager.getConnection(ConnURL,settings.getLogIn(),settings.getPassword());
        System.out.println(5);
        return instance;
    }
}
