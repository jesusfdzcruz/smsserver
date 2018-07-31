package com.mutuofinanciera.smsserver.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;


public class SMSService extends Service {

    public static final String TAG = "SMSSERVICE";

    public static final int PORT = 3512;
    public static final String SMS_BODY_TAG = "sms_body";
    public static final String SMS_URI_FORMAT = "smsto: %s";

    private boolean run;

    private int mSMSCounter;

    private ServerSocket mSocketServer;

    private Thread mThread;

    public SMSService() {
        Log.v(TAG, "Constructor");

        mSMSCounter = 0;

        run = true;

        mThread = new Thread(new Runnable() {
            public void run() {
                while (run) {
                    readMessage();
                }
            }
        });
    }

    private void readMessage() {
        Log.v(TAG, "waiting for socket");
        Socket client = acceptClient();

        if (client != null) {
            Log.v(TAG, "Client found");
            sendData(client, mSMSCounter);

            String number = receiveData(client);
            String message = receiveData(client);

            sendMessage(number, message);
        }

        closeClient(client);
    }

    private String receiveData(Socket client) {
        String result = "";
        try {
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(client.getInputStream()));
            result = inputStream.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void closeClient(Socket client) {
        try {

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendData(Socket client, int data) {
        PrintStream outputStream;
        try {

            outputStream = new PrintStream(client.getOutputStream());
            outputStream.println(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendData(Socket client, String data) {
        PrintStream outputStream;
        try {

            outputStream = new PrintStream(client.getOutputStream());
            outputStream.println(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Socket acceptClient() {
        Socket client = null;

        try {

            client = mSocketServer.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return client;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "Bind called it");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createServer(PORT);

        mThread.start();
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "Stop server");
        super.onDestroy();

        stopServer();
    }

    private void createServer(int port) {

        try {

            mSocketServer = new ServerSocket(port);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private void stopServer() {

        try {
            run = false;
            mSocketServer.close();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private void sendMessage(String destinationAddress, String message) {

        SmsManager smsManager = SmsManager.getDefault();

        smsManager.sendTextMessage(destinationAddress, null, message,
                null, null);

        mSMSCounter++;
    }
}
