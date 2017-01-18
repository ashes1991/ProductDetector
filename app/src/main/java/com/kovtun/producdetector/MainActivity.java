package com.kovtun.producdetector;



import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ListMenuItemView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.kovtun.producdetector.Models.Product;
import com.kovtun.producdetector.USBScaner.BarCodeListener;
import com.kovtun.producdetector.USBScaner.WorkSerialPortScaner;
import com.kovtun.producdetector.bluetooth_driver.BluetoothSPP;
import com.kovtun.producdetector.bluetooth_driver.BluetoothState;
import com.kovtun.producdetector.bluetooth_driver.DeviceList;

import org.json.JSONException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private  MainFragment mainFragment;
    private WorkSerialPortScaner wsps;
    private Handler handler;
    private final static String PRICE="price";
    private final static String NAME="name";
    private Context _context;
    private Timer timer;
    private TimerTask task;
    private int indexTimer=0;
    private int timeReklam=30;
    private boolean isTimerStoped=false;
    private AdvFragment adv;
    public static BluetoothSPP bt;
    private Menu menu;
    private boolean isBTOn=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        onCreateBluetoothInitializqScaner();
        _context=this;

        fragmentManager=getFragmentManager();
        mainFragment=new MainFragment();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mainLayout,mainFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        wsps=new WorkSerialPortScaner(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SettingsReader.readData();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        handler=new Handler(){

            @Override
            public void handleMessage(Message msg) {
                if (msg.what==5){
                    ((EditText) findViewById(R.id.editText2)).setText("");
                    ((EditText) findViewById(R.id.editText)).setText("");
                    return;
                }
                if (msg.what==1){
                    Toast.makeText(_context, "Товара нет в базе", Toast.LENGTH_SHORT).show();
                }else {

                    ((EditText) findViewById(R.id.editText2)).setText(msg.getData().getString(NAME));
                    ((EditText) findViewById(R.id.editText)).setText(msg.getData().getDouble(PRICE) + "");
                }
                    if (isTimerStoped)
                        start_timer();

            }



        };

        wsps.barCodeListener=new BarCodeListener() {
            @Override
            public void doEvent(String barcode) {

                if (getFragmentManager().getBackStackEntryCount()>1) {
                    getFragmentManager().popBackStack();
                    FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                    fragmentTransaction.remove(adv);
                    fragmentTransaction.commit();
                    adv.onDestroy();
                }

                System.out.println("bar");
                if (!isTimerStoped)
                    stop_timer();
                Product product=null;
                try {
                    Connection con=MSSQLConnector.getInstance(Values.settings);
                    Statement st=con.createStatement();
                    ResultSet rs=st.executeQuery("SELECT TOP 1 r_Prods.ProdID, r_Prods.ProdName, r_ProdMQ.BarCode, r_ProdMP.PriceMC" +
                            "                FROM r_Prods" +
                            "                INNER JOIN r_ProdMQ ON r_Prods.ProdID = r_ProdMQ.ProdID" +
                            "                INNER JOIN r_ProdMP ON r_Prods.ProdID = r_ProdMP.ProdID" +
                            "                where r_ProdMQ.BarCode='"+barcode+"'");
                    if (rs.next()) {
                        product=new Product(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getDouble(4));
                    }
                    st.close();
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (product!=null){
                    Message m=new Message();
                    Bundle data=new Bundle();
                    data.putString(NAME,product.getName());
                    data.putDouble(PRICE,product.getPrice());
                    m.setData(data);
                    handler.sendMessage(m);
                }else{
                    handler.sendEmptyMessage(1);
                }

            }
        } ;

        start_timer();
    }

    private void start_timer(){
        isTimerStoped=false;
        timer = new Timer();
        task = new TimerTask() {

            @Override
            public void run() {
                indexTimer++;
                Log.d("myLOG",indexTimer + "  "+ timeReklam);

                if(indexTimer >= timeReklam)
                {
                    showAdv();
                }

            }
        };

        timer.scheduleAtFixedRate(task, 10, 1000);
    }

    private void stop_timer(){
        isTimerStoped=true;
        timer.cancel();
        task.cancel();
        indexTimer=0;
    }

    private void showAdv() {
        stop_timer();
        if (ImageLoader.getPictures().length>0){
            handler.sendEmptyMessage(5);
            adv=new AdvFragment();
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.mainLayout,adv);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

    }


    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount()>1){
            getFragmentManager().popBackStack();
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.remove(adv);
            fragmentTransaction.commit();
        }
        if (isTimerStoped)
            start_timer();

    }

    @Override
    protected void onDestroy() {
        if (!isTimerStoped)
           stop_timer();
        super.onDestroy();
        bt.stopService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
            }
        }
    }

    private void setup(){
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {                 //Слушатель Сканер Блютуз
            public void onDataReceived(byte[] data, final String message) {
                if (getFragmentManager().getBackStackEntryCount()>1) {
                    getFragmentManager().popBackStack();
                    FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                    fragmentTransaction.remove(adv);
                    fragmentTransaction.commit();
                    adv.onDestroy();
                }
                System.out.println("bar");
                if (!isTimerStoped)
                    stop_timer();


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Product product=null;
                        boolean isError=false;
                        try {
                            Connection con=MSSQLConnector.getInstance(Values.settings);
                            Statement st=con.createStatement();

                            ResultSet rs=rs=st.executeQuery("SELECT r_Prods.ProdID, r_Prods.ProdName, r_ProdMQ.BarCode, it_BonusD.SumCC\n" +
                                    "From it_BonusD\n" +
                                    "INNER JOIN it_Bonus ON it_BonusD.ChID = it_Bonus.ChID\n" +
                                    "INNER JOIN r_Prods ON it_BonusD.ProdID = r_Prods.ProdID\n" +
                                    "INNER JOIN r_ProdMQ ON it_BonusD.ProdID = r_ProdMQ.ProdID\n" +
                                    "INNER JOIN r_ProdMP ON it_BonusD.ProdID = r_ProdMP.ProdID\n" +
                                    "Where r_ProdMQ.BarCode='"+message+"' AND r_ProdMP.PLID=(SELECT PLID from r_Stocks where StockID="+Values.settings.getShopId()+")"+
                                    "AND (r_Prods.PCatID = it_BonusD.PCatID OR it_BonusD.PCatID = 0) "+
                                    "AND (r_Prods.PGrID = it_BonusD.PGrID OR it_BonusD.PGrID = 0) "+
                                    "AND (r_Prods.PGrID1 = it_BonusD.PGrID1 OR it_BonusD.PGrID1 = 0) "+
                                    "AND (r_Prods.PGrID2 = it_BonusD.PGrID2 OR it_BonusD.PGrID2 = 0) "+
                                    "AND (r_Prods.PGrID3 = it_BonusD.PGrID3 OR it_BonusD.PGrID3 = 0) "+
                                    "AND (r_Prods.ProdID = it_BonusD.ProdID OR it_BonusD.ProdID = 0) "+
                                    "AND it_BonusD.Action = 5 AND it_Bonus.InUse = 1 AND GETDATE() BETWEEN it_Bonus.BDate AND it_Bonus.EDate AND it_Bonus.ChID <> 0 ");
                            if (rs.next()) {
                                System.out.println("Bonus");
                                product =new Product(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getDouble(4));
                            }else {
                                rs=st.executeQuery("SELECT r_Prods.ProdID, r_Prods.ProdName, r_ProdMQ.BarCode, r_ProdMP.PriceMC" +
                                        "                FROM r_Prods" +
                                        "                INNER JOIN r_ProdMQ ON r_Prods.ProdID = r_ProdMQ.ProdID" +
                                        "                INNER JOIN r_ProdMP ON r_Prods.ProdID = r_ProdMP.ProdID" +
                                        "                where r_ProdMQ.BarCode='"+message+"' AND r_ProdMP.PLID=(SELECT PLID from r_Stocks where StockID="+Values.settings.getShopId()+")");
                                System.out.println("Not bonus");
                                if (rs.next()) {
                                    product = new Product(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4));
                                }
                            }
                            st.close();
                            con.close();
                        } catch (SQLException e) {
                            insertLog(e.getMessage(),message);
                            e.printStackTrace();
                            isError=true;
                        } catch (ClassNotFoundException e) {
                            insertLog(e.getMessage(),message);
                            e.printStackTrace();
                            isError=true;
                        }
                        if (product !=null){
                            Message m=new Message();
                            Bundle data1=new Bundle();
                            data1.putString(NAME, product.getName());
                            data1.putDouble(PRICE, product.getPrice());
                            m.setData(data1);
                            handler.sendMessage(m);
                            insertLog(product);
                        }else if (!isError){
                            handler.sendEmptyMessage(1);
                            insertLog("Error product not found",message);
                        }
                    }
                }).start();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
            setup();
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);

            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_device_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
			/*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if(id == R.id.menu_disconnect) {
            if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
                bt.disconnect();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onCreateBluetoothInitializqScaner()
    {
        bt = new BluetoothSPP(this);
        if(!bt.isBluetoothAvailable())
            Toast.makeText(getApplicationContext() , "Bluetooth is not available", Toast.LENGTH_SHORT).show();
        else {
//            ProgramModel.bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
//                public void onDataReceived(byte[] data, String message) {
//                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//                }
//            });

            bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
                public void onDeviceDisconnected() {
                    Toast.makeText(getApplicationContext(), "Status : Not connect", Toast.LENGTH_SHORT).show();
                    //menu.clear();
                    //getMenuInflater().inflate(R.menu.menu_connection, menu);
                    isBTOn=false;
                    if((ImageButton)findViewById(R.id.imageButton)!=null)
                    ((ImageButton)findViewById(R.id.imageButton)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_action_on));
                }

                public void onDeviceConnectionFailed() {
                    Toast.makeText(getApplicationContext(), "Status : Connection failed", Toast.LENGTH_SHORT).show();
                }

                public void onDeviceConnected(String name, String address) {
                    Toast.makeText(getApplicationContext(), "Status : Connected to " + name, Toast.LENGTH_SHORT).show();
                    //menu.clear();
                    //getMenuInflater().inflate(R.menu.menu_disconnection, menu);
                    isBTOn=true;
                    ((ImageButton)findViewById(R.id.imageButton)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_action_off));
                }
            });
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_connection, menu);
        return true;
    }

    public void buttClick(View view){
        if(!isBTOn) {
            bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
			/*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if(isBTOn) {
            if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
                bt.disconnect();
        }
    }

    private void insertLog(Product product){
        SQLiteDB db=new SQLiteDB(this);
        if (product.getName().indexOf("'")>0){
            String[] listArray=product.getName().split("'");
            StringBuilder sb=new StringBuilder(listArray[0]);
            for (int i=1;i<listArray.length;i++){
                sb.append("''"+listArray[i]);
            }
            product.setName(sb.toString());
        }
        String query=String.format("Insert into EventLog(event,product_id,name,barcode,price,read_date) values('%s',%s,'%s','%s',%s,%s)",
                "SuccessfulRead",product.getId(),product.getName(),product.getBarcode(),product.getPrice(),new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        System.out.println(query);
        db.executeSQL(query);

    }
    private void insertLog(String text,String barcode){
        SQLiteDB db=new SQLiteDB(this);
        System.out.println(barcode+" balfour");
        System.out.println(barcode.length()+" length");
        if (barcode.indexOf(0)>=0){
            System.out.println(barcode.indexOf(0)+" "+barcode.lastIndexOf(0));
            if (barcode.indexOf(0) > 0) {
                barcode=barcode.substring(0,barcode.indexOf(0));
            }else{
                barcode=barcode.substring(barcode.lastIndexOf(0)+1);
            }
        }
        System.out.println(barcode+" after");
        for (byte b:barcode.getBytes()){
            System.out.println(b);
        }
        String insert=String.format("Insert into EventLog(event,barcode,read_date) values('%s','%s',%s)",
                text,barcode,new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        System.out.println(insert);
        db.executeSQL(insert);

    }
}
