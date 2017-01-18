package com.kovtun.producdetector.USBScaner;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kravchuk on 25.11.2014.
 */

public abstract class WorkSerialUSB
{
    protected Context context = null;
    private UsbDevice device;
    private UsbDeviceConnection connection = null;
    private UsbManager usbManager = null;
    private static final String  ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    UsbSerialDevice serialPort = null;
    private PendingIntent mPermissionIntent = null;


    public WorkSerialUSB(Context ctx)
    {
        context = ctx;
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        userPermissionDevice();
        startResiver();
    }

    public void userPermissionDevice()
    {
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION ), 0);
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if(!usbDevices.isEmpty())
        {
            for(Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                usbManager.requestPermission(entry.getValue(), mPermissionIntent);
            }

            boolean keep = true;
            List<UsbDevice> usbDevicesList = new ArrayList<UsbDevice>();

//            for(Map.Entry<String, UsbDevice> entry : usbDevices.entrySet())
//            {
//                device = entry.getValue();
//                int deviceVID = device.getVendorId();
//                int devicePID = device.getProductId();
//                if(getUslovieDevice(deviceVID,devicePID))  //deviceVID != 0x1d6b || (devicePID != 0x0001 || devicePID != 0x0002 || devicePID != 0x0003);
//                {
//                    device = entry.getValue();
//                    keep = false;
//                }
//                else
//                {
//                    connection = null;
//                    device = null;
//                }
//
//                if(!keep)
//                    break;
//            }
            for(Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {

                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();
                if (getUslovieDevice(deviceVID, devicePID)) {
                        usbDevicesList.add(entry.getValue());
                        keep = false;
                }
            }
            UsbDevice deviceMaxId = null;
            if(!usbDevicesList.isEmpty())
                deviceMaxId = usbDevicesList.get(0);
            for(UsbDevice dev : usbDevicesList)
                if(dev.getDeviceId() > deviceMaxId.getDeviceId())
                    deviceMaxId = dev;
            if(!keep)
            {
                device = deviceMaxId;
            }
            else
            {
                device = null;
                connection = null;
            }

        }

    }

    private void startResiver(){
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(mUsbReceiver, filter);
    }
    public void connectDevice()
    {   usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if(serialPort != null) return;
        if(device != null) {
            connection = usbManager.openDevice(device);
            if (connection != null) {
                serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                if (serialPort != null) {
                    if (serialPort.open()) {
                        setConfigSerialPort(serialPort);
                        Toast.makeText(context, "Connect", Toast.LENGTH_SHORT).show();
                    } else {
                        System.out.println(1);
                        // Serial port could not be opened, maybe an I/O error or it CDC driver was chosen it does not really fit
                    }
                } else {
                    System.out.println(2);
                    // No driver for given device, even generic CDC driver could not be loaded
                }
            } else {
                System.out.println(3);
                //Нет привилегий пользователя
            }
        }
        else
        {
            System.out.println(4);
            //нет устройств
        }
    }

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            UsbDevice usbDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (ACTION_USB_PERMISSION.equals(action)) {
                // Permission requested
                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        // User has granted permission
                        // ... Setup your UsbDeviceConnection via mUsbManager.openDevice(usbDevice) ...
                        connectDevice();
                    if (getUslovieDevice(usbDevice.getVendorId(), usbDevice.getProductId())) {
                        //Toast.makeText(context, "OK2", Toast.LENGTH_SHORT).show();
                    }
                    } else {
                        // User has denied permission
                        Toast.makeText(context, "NOT PERMISSION", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action))
            {
                synchronized (this) {
                    // Qualify the new device to suit your needs and request permission
                    //usbManager.requestPermission(usbDevice, mPermissionIntent);
                    Toast.makeText(context, "USB Found device", Toast.LENGTH_SHORT).show();

                    userPermissionDevice();

                }
            }
            else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Toast.makeText(context, "USB device removed", Toast.LENGTH_SHORT).show();
                if (serialPort!=null){
                    serialPort.close();
                    serialPort=null;
                }
            }
        }
    };
    protected abstract boolean getUslovieDevice(int deviceVID, int devicePID );
    protected abstract void setConfigSerialPort(UsbSerialDevice serialPort);
}

