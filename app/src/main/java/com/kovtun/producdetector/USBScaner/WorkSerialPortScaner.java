package com.kovtun.producdetector.USBScaner;

import android.content.Context;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

/**
 * Created by kravchuk on 25.11.2014.
 */
public class WorkSerialPortScaner extends WorkSerialUSB {

    private static final int ProductId = 0x4204;
    private static final int VendorId = 0x05f9;

    public BarCodeListener barCodeListener = null;
    private String read_result_barcode = "";


    public WorkSerialPortScaner(Context ctx) {
        super(ctx);
    }

    @Override
    protected boolean getUslovieDevice(int deviceVID, int devicePID) {
        //return (deviceVID != 0x1d6b || (devicePID != 0x0001 || devicePID != 0x0002 || devicePID != 0x0003)
        //        && (deviceVID == 0x05f9 && devicePID == 0x4204 ));   // Datalogic barcode scaner
        return (deviceVID == VendorId && devicePID == ProductId);  //Datalogic barcode scaner
    }

    @Override
    protected void setConfigSerialPort(UsbSerialDevice serialPort) {
        serialPort.setBaudRate(9600);
        serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
        serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
        serialPort.setParity(UsbSerialInterface.PARITY_NONE);
        serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
        serialPort.read(mCallback);

    }


    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback()
    {
        @Override
        public void onReceivedData(byte[] arg0)
        {
            try {
                if ((char) arg0[0] == '\r') {
                    if (barCodeListener != null)
                        barCodeListener.doEvent(read_result_barcode);
                    read_result_barcode = "";
                } else {
                    read_result_barcode = read_result_barcode.concat(String.valueOf((char) arg0[0]));
                    System.err.print(String.valueOf(arg0));
                }
            } catch (Exception ex) { ex.printStackTrace();}
        }
    };
}
