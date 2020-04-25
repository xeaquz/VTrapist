/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.vtrapist2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ChatController {



    // * 블루투스 관련 * //
    private static final String APP_NAME = "bluetoothEX";
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private AcceptThread acceptThread; // 받는 thread
    private ConnectThread connectThread; // 연결 thread
    private ReadWriteThread connectedThread; // 읽고 쓰는 thread
    private int state;

    static final int STATE_NONE = 0; // X
    static final int STATE_LISTEN = 1; // 듣는중
    static final int STATE_CONNECTING = 2; // 연결중
    static final int STATE_CONNECTED = 3; // 연결됨

    // Init
    public ChatController(Context context, Handler handler) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        state = STATE_NONE;

        this.handler = handler;
    }

    // chat 연결의 현재 상태 설정
    private synchronized void setState(int state) {
        this.state = state;

        handler.obtainMessage(PlayVideoSignal.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    // 현재 연결 상태 얻기
    public synchronized int getState() {
        return state;
    }

    // start service
    public synchronized void start() {

        // 현재 연결 thread가 존재한다면, 그 thread 취소 및 초기화 - connect
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // 연결된 thread가 존재한다면, 그 thread 취소 및 초기화 - ReadWrite
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // acceptThread가 없다면, 새로 생성 및 시작
        setState(STATE_LISTEN);
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    // Remote 장치에 대한 연결 초기화
    // connection & readWrite thread 초기화
    // connectTread 시작
    public synchronized void connect(BluetoothDevice device) {
        // Cancel any thread
        if (state == STATE_CONNECTING) {
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
        }

        // Cancel running thread
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Start the thread to connect with the given device
        // 주어진 장치와 연결하기 위해 thread 시작
        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTING);
    }

    // 블루투스 연결 관리
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        // Thread들 초기화

        // Cancel the thread
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // Cancel running thread
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        // 연결을 관리하고 전송을 수행하기 위해 thread 시작
        connectedThread = new ReadWriteThread(socket);
        connectedThread.start();

        // UI Activity에 연결된 장치의 이름을 전송
        Message msg = handler.obtainMessage(PlayVideoSignal.MESSAGE_DEVICE_OBJECT);
        Bundle bundle = new Bundle();
        bundle.putParcelable(PlayVideoSignal.DEVICE_OBJECT, device);
        msg.setData(bundle);
        handler.sendMessage(msg);

        setState(STATE_CONNECTED); // 연결된 상태로 설정
    }

    // 모든 thread 멈추기
    public synchronized void stop() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        setState(STATE_NONE); // X인 상태로 설정
    }

    public void write(byte[] out) {
        ReadWriteThread r;
        synchronized (this) {
            if (state != STATE_CONNECTED)
                return;
            r = connectedThread;
        }
        r.write(out);
    }

    // 연결이 실패했을때
    private void connectionFailed() {
        Message msg = handler.obtainMessage(PlayVideoSignal.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Unable to connect device");
        msg.setData(bundle);
        handler.sendMessage(msg);

        // Start the service over to restart listening mode
        ChatController.this.start();
    }

    // 연결을 잃어버렸을때
    private void connectionLost() {
        Message msg = handler.obtainMessage(PlayVideoSignal.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Device connection was lost");
        msg.setData(bundle);
        handler.sendMessage(msg);

        // Start the service over to restart listening mode
        ChatController.this.start();
    }

    // runs while listening for incoming connections
    // 수신 연결을 듣는동안 실행
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                // listen 을 수행하는 Insecure socket 을 얻어온다.
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            serverSocket = tmp;
        }

        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket;
            while (state != STATE_CONNECTED) { // 이미 연결된 상태가 아닐때
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }

                // 연결이 받아들여졌을때
                if (socket != null) {
                    synchronized (ChatController.this) {
                        switch (state) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate
                                // new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
        }
    }

    // runs while attempting to make an outgoing connection
    // Connection을 시도할 때 실행됨
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket tmp = null;
            try {
                //  해당장비에 uuid 를 이용해 insecure connection. (클라이언트 소켓 생성)
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = tmp;
        }

        public void run() {
            setName("ConnectThread");

            // 연결이 느려지므로, 항상 Discovery 취소해야 함.
            bluetoothAdapter.cancelDiscovery();

            // BluetoothSocket에 연결하기
            try {
                socket.connect();
            } catch (IOException e) { // 에러 발생시 소켓 닫기 -> connectionFailed
                try {
                    socket.close();
                } catch (IOException e2) {
                }
                connectionFailed();
                return;
            }

            // 끝남 -> ConnectThread 리셋
            synchronized (ChatController.this) {
                connectThread = null;
            }

            // 연결된 thread 시작
            connected(socket, device);
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    // Remote 장치와 연결중에 실행됨
    private class ReadWriteThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ReadWriteThread(BluetoothSocket socket) {
            this.bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream
            while (true) {
                try {
                    // InputStream 으로부터 읽어옴
                    bytes = inputStream.read(buffer);

                    // 얻은 Bytes를 UI Activity에 전송
                    handler.obtainMessage(PlayVideoSignal.MESSAGE_READ, bytes, -1,
                            buffer).sendToTarget();
                } catch (IOException e) {
                    connectionLost(); // 에러가 생겨 connection이 사라짐.
                    // Start the service over to restart listening mode
                    ChatController.this.start();
                    break;
                }
            }
        }

        // OutputStream에 적기
        public void write(byte[] buffer) {
            try {
                outputStream.write(buffer);
                // 얻은 bytes를 UI activity에 전송
                handler.obtainMessage(PlayVideoSignal.MESSAGE_WRITE, -1, -1,
                        buffer).sendToTarget();
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
