package com.hs.testbluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 56;
    BluetoothSocket socket = null;
    BluetoothDevice targetDevice; // 从搜索中找到的目标设备
    BluetoothReceiver bluetoothReceiver = new BluetoothReceiver(this);
    private BluetoothAdapter mBluetoothAdapter;

    public static String stringToBinary(String input) {
        if (input == null)
            throw new IllegalArgumentException("Input string cannot be null");

        StringBuilder binaryStringBuilder = new StringBuilder();

        for (char character : input.toCharArray()) {
            String binaryChar = Integer.toBinaryString(character);
            // 确保每个字符都有8位，如果不足8位，可以在前面补零
            while (binaryChar.length() < 8) {
                binaryChar = "0" + binaryChar;
            }
            binaryStringBuilder.append(binaryChar).append(" "); // 添加空格以区分字符
        }

        return binaryStringBuilder.toString().trim(); // 去除末尾多余的空格
    }
TextView text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH}, 100);
        setContentView(R.layout.activity_main);
        text = findViewById(R.id.text);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "请打开蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            return;
        }


        findExisting();
        if (targetDevice == null) {
            find();
        } else {
            read();
        }

//
//        try {
//            socket = targetDevice.createRfcommSocketToServiceRecord(targetDevice.getUuids()[1].getUuid());
     /*       socket = targetDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
//            socket.close();
            Log.d("zzw", "1socket.isConnected()" + socket.isConnected());
            socket.connect();
            Log.d("zzw", "socket.isConnected()" + socket.isConnected());


            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(("log com2 rtcm1005b ontime 10").getBytes());
            outputStream.flush();

            InputStream inputStream = socket.getInputStream();
            DataInputStream in = new DataInputStream(inputStream);*/

//            // 连接成功，可以准备接收数据
//        } catch (IOException e) {
//            e.printStackTrace();
//            // 处理连接错误
//        }


    }

    public void findExisting() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {

            if (device.getName().contains("M100")) {
                targetDevice = device;
                if (device.getUuids() != null)
                    for (ParcelUuid uuid : device.getUuids()) {
                        if (uuid != null)
                            Log.d("zzw", " device name:" + device.getName() + "," + uuid);

                    }


                break;
                // 找到目标设备，可以尝试连接它
                // 请注意，您可能需要用户输入配对码等信息
                // 连接设备的代码将在下一步中描述
            }
        }
    }

    public void find() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothReceiver, filter);
        mBluetoothAdapter.startDiscovery();
    }

    public void read() {
        if (targetDevice == null) {
            return;
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = targetDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
//            socket.close();
                    Log.d("zzw", "1socket.isConnected()" + socket.isConnected());
                    socket.connect();
                    Log.d("zzw", "socket.isConnected()" + socket.isConnected());


                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(("log com2 rtcm1005b ontime 10").getBytes());
                    outputStream.flush();

                    InputStream inputStream = socket.getInputStream();
                    DataInputStream in = new DataInputStream(inputStream);
                    while (true) {

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        try {
                            Log.d("zzw", "startRead");
                            bytesRead = inputStream.read(buffer);
                            String content = new String(buffer);
                            Log.d("zzw", content);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder.append(text.getText().toString());
                                    stringBuilder.append(content);
                                    text.setText(stringBuilder.toString());
                                }
                            });

                        } catch (IOException e) {
                            Log.d("zzw", "startRead exception");
                            throw new RuntimeException(e);
                        }
                        // 读取文件内容
                     /*   try {
                            int l;
                            byte[] b = new byte[1024];
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            while (true) {

                                if (!((l = in.read(b)) != -1)) break;

                                out.write(b, 0, l);
                                break;
                            }
                            String s = new String(out.toByteArray(), "GBK");
                            Log.d("zzw", s);*/
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        });

    }

    private void pairDevice(BluetoothDevice device) {
        // 检查设备是否已配对
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            // 设备尚未配对，可以尝试进行配对
            try {
                if (device.getName() != null && device.getName().contains("M100")) {

                    // 在配对之前，可以停止设备的搜索
                    mBluetoothAdapter.cancelDiscovery();

                    // 开始配对
                    device.createBond();
                    findExisting();
                    //found
                    if (targetDevice != null) {
                        read();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 处理配对错误
            }
        } else {
            findExisting();
            read();
            // 设备已经配对
            // 在这里执行适当的操作
        }
    }

    class BluetoothReceiver extends BroadcastReceiver {
        WeakReference<MainActivity> weakReference;

        BluetoothReceiver(MainActivity activity) {
            weakReference = new WeakReference(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("zzw", "receiver");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // 从intent对象中获取蓝牙设备的信息
                if (weakReference != null && weakReference.get() != null) {
                    weakReference.get().pairDevice(device);
                }

            }
            //蓝牙配对
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDED:
                        Log.i("zzw","onReceive: 配对完成");
                        if (weakReference != null && weakReference.get() != null) {
                            weakReference.get().findExisting();
                            weakReference.get().read();
                        }
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.i("zzw","onReceive: 正在配对");
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.i("zzw","onReceive: 取消配对");
                        break;
                }
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                 Log.i("zzw","扫描完成");
            }

        }
    }


}