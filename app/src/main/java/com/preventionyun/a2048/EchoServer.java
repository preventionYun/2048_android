package com.preventionyun.a2048;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by JiYun on 2017. 5. 24..
 */
public class EchoServer {
    private final static String TAG = "EchoServer";

    private String servName;
    private int servPort;
    private String myNickName, peerNickName;
    private Socket socket;
    private Handler hSendThread, hMainThread;
    private DataOutputStream oStream;
    private ProgressDialog ringProgressDialog;
    private MainActivity mainActivity;
    private boolean isFirstSend = true;
    private final int maxWaitingTime = 3000;    // 3 seconds
    private final int sleepDuration = 100;      // 100 ms
    private final int maxSleepCount = maxWaitingTime / sleepDuration;
    private int serverState = 0;
    private final int flagConnecting = 1;   // indicate that ConnectThread is running
    private final int flagConnected = 2;    // indicate that socket is connected
    private final int flagSendRunning = 4;  // indicate that SendThread is running
    private final int flagRecvRunning = 8;  // indicate that RecvThread is running
    private final int ServerAvailable = (flagConnected | flagSendRunning | flagRecvRunning);
    private final int ServerUnavailable = 0;
    public int nCharsSent = 0;
    public int nCharsRecv = 0;

    public EchoServer(Handler h, MainActivity a){
        hMainThread = h;
        mainActivity = a;
    }
    public boolean isAvailable(){ return ((serverState & ServerAvailable) == ServerAvailable); }
    public boolean connect(String hname, int hport, String myName, String peerName) {
        Log.d(TAG, "connect() called in serverState = " + serverState);
        isFirstSend = true; // needs to be reset so that nickname can be notified to the server
        if ((serverState & flagConnecting) == flagConnecting || (serverState & flagConnected) == flagConnected) {
            return false;
        }
        if (waitForServerState(ServerAvailable, "MainThread") == false) {
            Log.d(TAG, "waitForServerState(Unavailable) timed out!");
            //return false; !!
        }

        // At this point, serverState == ServerUnavailable
        setServerStateFlag(flagConnecting);
        servName = hname;
        servPort = hport;
        myNickName = myName;
        peerNickName = peerName;
        ringProgressDialog = ProgressDialog.show(mainActivity, "Please wait ...",
                "Connecting to " + hname + ":" + hport, true);
        ringProgressDialog.setCancelable(true);
        startThread(runnableConnect);
        // ConnectThread is terminated as soon as it establishes a connection to EchoServer.
        nCharsSent = 0;
        nCharsRecv = 0;
        return true;
    }

    public boolean disconnect() {
        Log.d(TAG, "disconnect() called in serverState = " + serverState);
        if((serverState & (flagConnecting | flagConnected)) == 0)
            return false;
        if(waitForServerState(flagConnected, "MainThread") == false) {
            Log.d(TAG, "waitForServerState(Connected) timed out!");
            return false;
        }
        sendMessage(hSendThread, 0, 'Z');   // 'Z' signifies the end of connection.
        sleep(1000);    // we have to wait for the 'quit' string to be sent to the server.
        // At this point, serverState == ServerConnected or ServerAvailable.
        if ((serverState & flagConnected) == flagConnected) {
            try { socket.close(); }
            catch (Exception e) { e.printStackTrace(); }
        }
        return true;
    }
    private char validKeys[] = { 'a', 'd', 's', 'w', ' ', '0', '1', '2', '3', '4', '5', '6', 'Q'};
    private boolean isValidKey(char key){
        for(int i = 0; i < validKeys.length; i++){
            if(validKeys[i] == key) return true;
        }
        return false;
    }

    public boolean send(char ch) {
        if (isValidKey(ch) == false){
            Log.d(TAG, "not valid key (" +  ch + ")");
            return false;
        }
        if((serverState & (flagConnecting | flagConnected)) == 0)
            return false;
        if(waitForServerState(ServerAvailable, "MainThread") == false) {
            Log.d(TAG, "waitForServerState(Available timed out!");
            return false;
        }   // at this point, serverState == ServerAvailable
        if(isFirstSend){
            sendMessage(hSendThread, 0, 'A');   // 'A' signifies the start of connection.
            isFirstSend = false;
        }
        sendMessage(hSendThread, 0, ch);
        return true;
    }
    private Runnable runnableConnect = new Runnable() {
        @Override
        public void run() {
            try {
                SocketAddress socketAddress = new InetSocketAddress(servName, servPort);
                socket = new Socket();
                Log.d(TAG, "서버 접속.. IP : " + servName + " Port : " + servPort);
                socket.connect(socketAddress, maxWaitingTime);  // if this fails, then it will raise an exception
                setServerStateFlag(flagConnected);
                startThread(runnableSend);
                startThread(runnableRecv);
            } catch (Exception e){
                Log.d(TAG, "ConnectThread : connect() fails!");
                e.printStackTrace();
            }
            resetServerStateFlag(flagConnecting);
            ringProgressDialog.dismiss();
        }
    };
    private Runnable runnableSend = new Runnable(){
        @Override
        public void run() {
            setServerStateFlag(flagSendRunning);
            try{ oStream = new DataOutputStream(socket.getOutputStream()); }
            catch (Exception e) { e.printStackTrace(); }
            Looper.prepare();   // The message loop starts
            hSendThread = new Handler() {   // defined here to be available after the loop starts
                public void handleMessage(Message msg) {
                    try{
                        String line;
                        char ch = (char) msg.arg1;
                        if (ch == 'A') // At the beginning, we have to notify the server of my nickname.
                            line = "/nick " + myNickName + String.valueOf('\n');
                        else if (ch == 'Z') // At the end, we have to ask the server to close the connection.
                            line = "/quit\n";
                        else {  // Then, data can be transmitted to the server.
                            line = "/msg " + peerNickName + String.valueOf(' ') + String.valueOf((char) msg.arg1) + String.valueOf('\n');
                            nCharsSent++;
                        }
                        Log.d(TAG, "SendThread : writeBytes(" + line + ")");
                        oStream.writeBytes(line);
                        if(ch=='A')sleep(3000);
                    } catch (Exception e) { e.printStackTrace(); }
                }
            };
            Looper.loop();  // The message loop ends
            resetServerStateFlag(flagSendRunning);
            Log.d(TAG, "SendThread terminated");

            if((serverState & flagConnected) == flagConnected){
                try { socket.close(); }
                catch (Exception e) { e.printStackTrace(); }
                resetServerStateFlag(flagConnected);
                Log.d(TAG, "Socket closed");
            }
        }
    };
    private Runnable runnableRecv = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "runnableRecv");
            setServerStateFlag(flagRecvRunning);
            try{
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line, nicknameP2, peerNickNameP2 = peerNickName + ": ";
                char ch;
                while(true){
                    do { line = br.readLine(); } while (line.length() == 0);
                    // !! line = line.replaceAll("\u001B\\[[]")
                    Log.d(TAG, "RecvThread : (" + line + ") = readLine()");
                    nicknameP2 = line.substring(0, peerNickNameP2.length());
                    if(peerNickNameP2.compareTo(nicknameP2) != 0) {
                        Log.d(TAG, "not valid nickname (" + nicknameP2 + ")");
                        continue;
                    }
                    ch = line.charAt(nicknameP2.length());
                    if (isValidKey(ch) == false) {
                        Log.d(TAG, "not valid key (" + ch + ")");
                        continue;
                    }
                    else {
                        nCharsRecv++;
                        Log.d(TAG, "(nCharsSent, nCharsRecv) = (" + nCharsSent + ", " + nCharsRecv + ")");
                        sendMessage(hMainThread, 0, ch);
                    }
                }
            } catch (Exception e){  // abnormal close
                Log.d(TAG, "Socket closed abnormally");
            }
            resetServerStateFlag(flagConnected);
            hSendThread.getLooper().quit(); // to terminate SendThread
            resetServerStateFlag(flagRecvRunning);
            Log.d(TAG, "RecvThread terminated");
        }
    };
    private void sendMessage(Handler h, int type, char ch){
        Message msg = Message.obtain(h, type);
        msg.arg1 = ch;
        h.sendMessage(msg);
    }
    private void startThread(Runnable runnable){
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }
    public void sleep(int time){
        try { Thread.sleep(time); }
        catch (Exception e) { e.printStackTrace(); }
    }
    synchronized private void setServerStateFlag(int flag) { serverState = (serverState | flag); }
    synchronized private void resetServerStateFlag(int flag) { serverState = (serverState & ~flag); }
    private boolean waitForServerState(int flag, String who){
        Log.d(TAG, who + " : waitForServerState(" + flag + ") called");
        int count = 0;
        while (((serverState & flag) != flag) && count < maxSleepCount){
            Log.d(TAG, who + " : waitForServerState(" + flag + "&" + serverState + ") waiting...");
            sleep(sleepDuration);
            count++;
        }
        if(((serverState & flag) == flag)) return true;
        else return false;
    }
}
