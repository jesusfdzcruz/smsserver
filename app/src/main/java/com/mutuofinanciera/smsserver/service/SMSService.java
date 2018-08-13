package com.mutuofinanciera.smsserver.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

            executeCommand(client, receiveInteger(client));
        }

        closeClient(client);
    }

    private void executeCommand(Socket client, int command){

        switch (command){
            case CommandConstants.COMMAND_SET_COUNTER:
                setMessageCounter(client);
                break;
            case CommandConstants.COMMAND_SEND_MESSAGE:
                sendMessage(client);
                break;
            case CommandConstants.COMMAND_SEND_MESSAGE_COUNTER:
                sendCounter(client, mSMSCounter);
                break;
            default:
        }
    }

    private void sendCounter(Socket client, int counter) {
        sendData(client, counter);
    }

    private void sendMessage(Socket client){
        String number = receiveData(client);
        String message = receiveData(client);

        sendMessage(number, message);
    }

    private void setMessageCounter(Socket client) {
        mSMSCounter = receiveInteger(client);
    }

    private int receiveInteger(Socket client) {
        int result = 0;
        try {
            DataInputStream inputStream = new DataInputStream (client.getInputStream());
            result = inputStream.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private String receiveData(Socket client) {
        String result = "";
        int i;
        try {
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(client.getInputStream()));
            result = inputStream.readLine();
            for(i=0; i != 1000; i++)
                System.out.println(i);
        } catch (IOException e) {
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
