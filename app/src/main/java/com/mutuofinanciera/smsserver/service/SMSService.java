package com.mutuofinanciera.smsserver.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;


public class SMSService extends Service {

    public static final String TAG = "SMSSERVICE";

    public static final int PORT = 3512;

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
                    mainTask();
                }
            }
        });
    }

    private void mainTask() {
        Log.v(TAG, "waiting for socket");

        executeCommand(receiveInteger());
    }

    private void executeCommand(int command) {

        switch (command){
            case CommandConstants.COMMAND_SET_COUNTER:
                setMessageCounter();
                break;
            case CommandConstants.COMMAND_SEND_MESSAGE:
                sendMessage();
                break;
            case CommandConstants.COMMAND_SEND_MESSAGE_COUNTER:
                sendCounter(mSMSCounter);
                break;
            default:
        }
    }

    private void sendCounter(int counter) {
        sendData(counter);
    }

    private void sendMessage() {
        String number = receiveData();
        String message = receiveData();

        sendMessage(number, message);
    }

    private void setMessageCounter() {
        mSMSCounter = receiveInteger();
    }

    private int receiveInteger() {
        int result = -1;

        try {
            Socket client = acceptClient();

            DataInputStream inputStream = new DataInputStream (client.getInputStream());
            result = inputStream.readInt();

            closeClient(client);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private String receiveData() {
        String result = null;
        try {
            Socket client = acceptClient();

            DataInputStream inputStream = new DataInputStream(client.getInputStream());
            result = inputStream.readUTF();

            closeClient(client);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void closeClient(Socket client) {
        try {

            if(client != null)
                client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendData(int data) {
        PrintStream outputStream;
        try {
            Socket client = acceptClient();

            outputStream = new PrintStream(client.getOutputStream());
            outputStream.println(data);

            closeClient(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendData(String data) {
        PrintStream outputStream;
        try {
            Socket client = acceptClient();

            outputStream = new PrintStream(client.getOutputStream());
            outputStream.println(data);

            closeClient(client);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Socket acceptClient() {
        Socket client = null;

        try {

            client = mSocketServer.accept();
        } catch (Exception e) {
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
