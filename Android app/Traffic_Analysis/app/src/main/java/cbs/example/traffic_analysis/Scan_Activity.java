package cbs.example.traffic_analysis;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Scan_Activity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private Custom_Listview custom_listview;
    private Button button;
    private EditText editText;
    private TextView textView;
    private ProgressBar progressBar;
    private ListView listView;
    private String[] ip_address;
    private String ip;
    private String port = "6000";
    private String connect_ip;
    private int ip_check;
    private int i = 101;
    private Thread server;
    private int mExitValue;

    protected ArrayList<String> found_device = new ArrayList<>();
    protected ArrayList<String> device_ip = new ArrayList<>();
    protected boolean flag1 = true;
    protected int m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        button = (Button) findViewById(R.id.button16);
        editText = (EditText) findViewById(R.id.editText6);
        textView = (TextView) findViewById(R.id.textView25);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        listView = (ListView) findViewById(R.id.listView);

        custom_listview = new Custom_Listview(getApplicationContext(),found_device,device_ip);
        listView.setOnItemClickListener(this);

        server = new Thread(new Server());
        server.start();
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
        ip_address = editText.getText().toString().split("[.]");
        ip = editText.getText().toString();

        int calculate = 0;
        char temp;

        for (int i=0;i<ip.length();i++){
            temp = ip.charAt(i);

            if (temp == '.'){
                calculate++;
            }
        }

        if (calculate == 3 && ip_address.length == 4){
            try {
                ip_check = Integer.parseInt(ip_address[0]) + Integer.parseInt(ip_address[1]) + Integer.parseInt(ip_address[2]) + Integer.parseInt(ip_address[3]);

                progressBar.setVisibility(View.VISIBLE);
                textView.setText("Searching...");
                button.setEnabled(false);

                Thread thread = new Thread(scan);
                thread.start();
            }catch (NumberFormatException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"IP address is a number",Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getApplicationContext(),"IP format is mistake",Toast.LENGTH_SHORT).show();
        }
    }

    private Runnable scan = new Runnable() {
        Handler handler = new Handler();
        @Override
        public void run() {
            for (int m = i;m <= 104;m++){
                final String search_ip = ip_address[0] + "." + ip_address[1] + "." + ip_address[2] + "." + m;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("Searching... on " + search_ip + "(" + ip_check + ")");
                    }
                });

                try {
                    Process mIpAddrProcess = Runtime.getRuntime().exec("/system/bin/ping -c 1 "+ search_ip);
                    mExitValue = mIpAddrProcess.waitFor();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }

                if(mExitValue==0){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.append(",succeed.");
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.append("Verifying...");
                        }
                    });

                    BackgroundTask action = new BackgroundTask();
                    action.execute(search_ip,port,"19");

                    int n = 0;

                    do {
                        try {
                            Thread.sleep(1000);
                            n++;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    textView.append(".");
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (n ==5){
                            break;
                        }else if (!flag1){
                            connect_ip = ip_address[0] + "." + ip_address[1] + "." + ip_address[2] + "." + m;
                            found_device.add("Drone Mount");
                            device_ip.add(connect_ip);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listView.setAdapter(custom_listview);
                                }
                            });
                            break;
                        }
                    }while (true);
                }else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.append(",failed.");
                        }
                    });
                }
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.INVISIBLE);
                    textView.setText("Finished");
                }
            });

            Message message = new Message();
            message.what = 1;
            correct_handler.sendMessage(message);
        }
    };

    private Handler correct_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 1){
                button.setEnabled(true);
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final long select_id = parent.getItemIdAtPosition(position);

        new AlertDialog.Builder(Scan_Activity.this)
                .setTitle("Skip initialization?")
                .setMessage("You will connect to " + device_ip.get((int) select_id) + "\nAre you share you want to skip the initialization?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent next = new Intent(Scan_Activity.this,Select_module_Activity.class);
                        next.putExtra("IP",device_ip.get((int) select_id));
                        next.putExtra("Port","6000");
                        startActivity(next);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent next = new Intent(Scan_Activity.this,Connect_Activity.class);
                        next.putExtra("IP",device_ip.get((int) select_id));
                        next.putExtra("Port","6000");
                        startActivity(next);
                    }
                }).show();
    }

    class Server implements Runnable{
        ServerSocket ss;
        public Socket socket;
        DataInputStream dis;
        int message;
        Handler handler = new Handler();

        @Override
        public void run() {
            try {
                ss = new ServerSocket(9600);
                while (true){
                    socket = ss.accept();
                    dis = new DataInputStream(socket.getInputStream());
                    message = dis.read();

                    switch (message){
                        case 12 : {
                            flag1 = false;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Scan_Activity.this,"Network connect is succeeded(" + message + ")",Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        }
                    }
                    socket.close();
                    dis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}