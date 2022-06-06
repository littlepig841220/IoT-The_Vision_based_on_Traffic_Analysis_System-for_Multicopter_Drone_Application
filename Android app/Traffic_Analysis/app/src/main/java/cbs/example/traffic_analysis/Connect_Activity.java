package cbs.example.traffic_analysis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Connect_Activity extends AppCompatActivity implements View.OnClickListener {
    private TextView textView;
    private Button button;
    private ImageView imageView,imageView2,imageView3;

    protected boolean flag1 = true;
    protected boolean flag2 = true;
    protected boolean flag3 = true;
    protected String ip,port,network_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        textView = (TextView) findViewById(R.id.textView28);
        button = (Button) findViewById(R.id.button14);
        imageView = (ImageView) findViewById(R.id.imageView9);
        imageView2 = (ImageView)findViewById(R.id.imageView10);
        imageView3 = (ImageView) findViewById(R.id.imageView11);

        ip = getIntent().getStringExtra("IP");
        port = getIntent().getStringExtra("Port");
        network_type = getIntent().getStringExtra("Mode");
        //port = "6000";

        Thread thread2 = new Thread(new Server());
        thread2.start();

        //textView.setText(ip);

        Thread thread = new Thread(start);
        thread.start();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Fullscreen_setting fullscreenSetting = new Fullscreen_setting();
            fullscreenSetting.window = getWindow();
            fullscreenSetting.fullscreen();
        }
    }

    @Override
    public void onClick(View v) {
        if (button.getText().equals("Next")){
            Intent next = new Intent(Connect_Activity.this,Select_module_Activity.class);
            next.putExtra("IP",ip);
            next.putExtra("Port",port);
            next.putExtra("Mode",network_type);
            startActivity(next);
        }else {
            Intent back = new Intent(Connect_Activity.this,Menu_Activity.class);
            startActivity(back);
        }
    }

    private Runnable start = new Runnable() {
        Handler handler = new Handler();
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("connecting on " + ip + "...");
                    }
                });
                Process  mIpAddrProcess = Runtime.getRuntime().exec("/system/bin/ping -c 1 "+ ip);
                int mExitValue = mIpAddrProcess.waitFor();
                if(mExitValue==0){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.append(",succeed.");
                        }
                    });

                    Thread.sleep(1000);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.append("Verifying...");
                        }
                    });

                    BackgroundTask action = new BackgroundTask();
                    action.execute(ip,port,"11");

                    do {
                        Thread.sleep(1000);
                    }while (flag1);

                    BackgroundTask action2_forward = new BackgroundTask();
                    action2_forward.execute(ip,port,"13");

                    do {
                        Thread.sleep(1000);
                    }while (flag2);

                    BackgroundTask action2_forward2 = new BackgroundTask();
                    action2_forward2.execute(ip,port,"15");

                    do {
                        Thread.sleep(1000);
                    }while (flag3);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText("finished");
                        }
                    });

                    Message message = new Message();
                    message.what = 1;
                    correct_handler.sendMessage(message);
                }else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.append(",failed.");
                        }
                    });

                    Message message = new Message();
                    message.what = 2;
                    correct_handler.sendMessage(message);
                }
                Thread.sleep(1000);
            }
            catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    };

    private Handler correct_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 1){
                button.setEnabled(true);
            }else {
                button.setText("Back");
                button.setEnabled(true);
            }
        }
    };

    class Server implements Runnable{
        ServerSocket ss;
        Socket socket;
        DataInputStream dis;
        int message;
        Handler handler = new Handler();

        @Override
        public void run() {
            try {
                ss = new ServerSocket(9500);
                while (true){
                    socket = ss.accept();
                    dis = new DataInputStream(socket.getInputStream());
                    message = dis.read();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            switch (message){
                                case 12 : {
                                    flag1 = false;
                                    imageView.setImageResource(R.drawable.green_light);
                                    break;
                                }
                                case 14 : {
                                    flag2 = false;
                                    imageView2.setImageResource(R.drawable.green_light);
                                    break;
                                }
                                case 16 : {
                                    flag3 = false;
                                    imageView3.setImageResource(R.drawable.green_light);
                                    break;
                                }
                            }
                        }
                    });
                    socket.close();
                    dis.close();

                    /*if (message == 16){
                        break;
                    }*/
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}