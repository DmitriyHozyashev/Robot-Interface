package com.leartgjoni.app.pathfindingsimulator;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class GridActivity extends FragmentActivity {
    GridView gridv;
    private static final int REQUEST_ENABLE_BT = 1;
    static final int RECIEVE_MESSAGE = 1;
    static final String TAG = "myLogs";
    private BluetoothAdapter btAdapter = null;
    /*private*/public BluetoothSocket btSocket = null;
    private static String address = "20:11:02:47:01:60"; // MAC-адрес БТ модуля
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public ConnectedThread mConnectedThread;
    static Handler h;
    private StringBuilder sb = new StringBuilder();

    Button startMove;
    Button stopMove;
    TextView rezultateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        startMove = (Button) findViewById(R.id.start);
        stopMove = (Button) findViewById(R.id.stop);
        rezultateText = (TextView) findViewById(R.id.rezultate);
        gridv = (GridView) findViewById(R.id.grid);
        gridv.init2(this);

        stopMove.setVisibility(View.GONE);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        rezultateText.setText("Сначала рекомендуется построить препятствия");
        startMove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                gridv.startClicked = 1;
                if(gridv.cont == 0) {
                    rezultateText.setText("");
                    startMove.setVisibility(View.GONE);
                    stopMove.setVisibility(View.VISIBLE);
                }
                else
                {
                    rezultateText.setText("");
                    startMove.setVisibility(View.GONE);
                    stopMove.setVisibility(View.VISIBLE);
                    gridv.stoped = 0;
                    gridv.Astar();
                }
                mConnectedThread.sendData("0");
            }
        });
        stopMove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (gridv.cont == 1) {
                    gridv.startClicked = 0;
                    gridv.stoped = 1;
                    rezultateText.setText("Перемещение остановлено");
                    gridv.astarthread.cancel(true);
                    startMove.setVisibility(View.VISIBLE);
                    stopMove.setVisibility(View.GONE);
                    mConnectedThread.sendData("9");
                }
            }
        });

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                   // если приняли сообщение в Handler
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);
                        sb.append(strIncom);                                                // формируем строку
                        int endOfLineIndex = sb.indexOf("\r\n");                            // определяем символы конца строки
                        if (endOfLineIndex > 0) {                                            // если встречаем конец строки,
                            String sbprint = sb.substring(0, endOfLineIndex);               // то извлекаем строку
                            sb.delete(0, sb.length());                                      // и очищаем sb
                            rezultateText.setText("Ответ от Arduino: " + sbprint);
                        }
                        break;
                }
            };
        };
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - попытка соединения...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Соединяемся...");
        try {
            btSocket.connect();
            Log.d(TAG, "...Соединение установлено и готово к передачи данных...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Создание Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth не поддерживается");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth включен...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    public void relocaton(String command)
    {
        mConnectedThread.sendData(command);
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Получаем кол-во байт и само сообщение в байтовый массив "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Отправляем в очередь сообщений Handler
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    try {
                        sleep(100);
                    } catch (InterruptedException err) {
                        err.printStackTrace();
                    }
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void sendData(String message) {
            Log.d(TAG, "...Данные для отправки: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Ошибка отправки данных: " + e.getMessage() + "...");
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
