package com.kovtun.producdetector;

import android.os.Environment;

import com.kovtun.producdetector.Models.SettingsModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by kovtun on 23.06.2016.
 */
public class SettingsReader {
    private static String FileName=null;
    private final static String IP="ip";
    private final static String PORT="port";
    private final static String BASE_NAME="base_name";
    private final static String LOGIN="login";
    private final static String PASSWORD="password";
    private final static String SHOPID="shopId";
    private static String getFile_NAME()
    {
        if(FileName == null) {
            File sdPath = Environment.getExternalStorageDirectory();
            sdPath = new File(sdPath.getAbsolutePath() + "/ProductDetector");
            sdPath.mkdirs();
            FileName = sdPath.getAbsolutePath() + "/PDSettings.json";
        }
        return FileName;
    }

    public static void readData() throws JSONException, IOException {
        if (FileName==null){
             getFile_NAME();
        }
        File file=new File(FileName);
        if (!file.exists()){
            JSONObject object=new JSONObject();
            object.put(IP,"192.168.1.2");
            object.put(PORT,1801);
            object.put(BASE_NAME,"Avrora1");
            object.put(LOGIN,"root");
            object.put(PASSWORD,"As091991");
            object.put(SHOPID,"19");
            FileWriter writer=new FileWriter(file);
            writer.write(object.toString());
            writer.flush();
            writer.close();
            getSettingsFromJson(object);
        } else{
            FileReader r=new FileReader(file);
            BufferedReader reader=new BufferedReader(r);
            JSONObject object=new JSONObject(reader.readLine());
            reader.close();
            getSettingsFromJson(object);
        }

    }

    private static void getSettingsFromJson(JSONObject object) throws JSONException {
         Values.settings=new SettingsModel(object.getString(IP),object.getInt(PORT),
                 object.getString(BASE_NAME),object.getString(LOGIN),object.getString(PASSWORD),object.getInt(SHOPID));
    }
}
