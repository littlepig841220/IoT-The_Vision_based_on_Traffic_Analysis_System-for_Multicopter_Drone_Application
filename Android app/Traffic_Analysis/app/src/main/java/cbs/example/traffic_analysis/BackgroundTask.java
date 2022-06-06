package cbs.example.traffic_analysis;

import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.net.Socket;

public class BackgroundTask extends AsyncTask<String,Void,String> {
    Socket s;
    DataOutputStream dos;
    String ip,message; //192.168.43.173
    int port;

    @Override
    protected String doInBackground(String... params) {

        ip = params[0];
        port = Integer.valueOf(params[1]);
        message = params[2];
        try {
            s = new Socket(ip,port);
            dos = new DataOutputStream(s.getOutputStream());
            //dos.writeUTF(message);
            dos.write(Integer.parseInt(message));

            dos.close();
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}