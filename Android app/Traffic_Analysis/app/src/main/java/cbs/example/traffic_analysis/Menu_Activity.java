package cbs.example.traffic_analysis;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Menu_Activity extends AppCompatActivity implements View.OnClickListener {
    private ScrollView scrollView;
    private Button button,button2,button3,button4;
    private Spinner spinner,spinner2,spinner3,spinner4;
    private TextView textView,textView2,textView3,textView4,textView5;
    private EditText editText,editText2,editText3;
    private String network_type = "wi-fi";

    private static String user = "chtholly";
    private static String pasd = "77380145";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        scrollView = (ScrollView) findViewById(R.id.ScrollView);
        button = (Button) findViewById(R.id.button9);
        button2 = (Button) findViewById(R.id.button13);
        button3 = (Button) findViewById(R.id.button11);
        button4 = (Button) findViewById(R.id.button12);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner2 = (Spinner) findViewById(R.id.spinner2);
        spinner3 = (Spinner) findViewById(R.id.spinner3);
        spinner4 = (Spinner) findViewById(R.id.spinner4);
        textView = (TextView) findViewById(R.id.textView12);
        textView2 = (TextView)findViewById(R.id.textView16);
        textView3 = (TextView) findViewById(R.id.textView17);
        textView4 = (TextView) findViewById(R.id.textView18);
        textView5 = (TextView) findViewById(R.id.textView19);
        editText = (EditText) findViewById(R.id.editText3);
        editText2 = (EditText) findViewById(R.id.editText4);
        editText3 = (EditText) findViewById(R.id.editText5);

        scrollView.setOnClickListener(this);

        spinner.setOnItemSelectedListener(sample);
        spinner2.setOnItemSelectedListener(RTSP);
        spinner3.setOnItemSelectedListener(location);
        spinner4.setOnItemSelectedListener(communication);
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
        switch (v.getId()){
            case R.id.ScrollView: {
                View view = this.getCurrentFocus();
                if (view != null){
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                break;
            }
            case R.id.button13: {
                if (user.equals(editText.getText().toString()) & pasd.equals(editText2.getText().toString())){
                    textView2.setVisibility(View.VISIBLE);
                    spinner2.setVisibility(View.VISIBLE);
                    button2.setEnabled(false);
                    button3.setEnabled(false);
                    button4.setEnabled(false);
                    editText.setEnabled(false);
                    editText2.setEnabled(false);
                }else {
                    Toast.makeText(getApplicationContext(),"Password or Account is not correct",Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.button9: {
                switch (spinner2.getSelectedItem().toString()){
                    case "Experiment": {
                        switch (spinner.getSelectedItem().toString()){
                            case "CCTV-Tunnel01": {
                                start("tunnel01");
                                break;
                            }
                            case "CCTV-Tunnel02": {
                                start("tunnel02");
                                break;
                            }
                            case "Simulation Drone Camera-Outside01": {
                                start("outside01");
                                break;
                            }
                            case "Simulation Drone Camera-Outside02": {
                                start("outside02");
                                break;
                            }
                            case "Simulation Drone Camera-Outside03": {
                                start("outside03");
                                break;
                            }
                            case "failed01": {
                                start("failed01");
                                break;
                            }
                            case "test": {
                                Intent intent = new Intent(getApplicationContext(),Test_Activity.class);
                                startActivity(intent);
                                break;
                            }
                        }
                        break;
                    }
                    case "CCTV": {
                        switch (spinner3.getSelectedItem().toString()){
                            case "Freeway 1 North 0K+400 大業隧道": {
                                //start("RTSP-Tunnel");
                                Intent intent = new Intent(getApplicationContext(),Main_Webview_Activity.class);
                                intent.putExtra("video_number","RTSP-Tunnel");
                                startActivity(intent);
                                break;
                            }
                            case "Freeway 3 North 380K+725 中寮隧道南口": {
                                //start("RTSP-Outside");
                                Intent intent = new Intent(getApplicationContext(),Main_Webview_Activity.class);
                                intent.putExtra("video_number","RTSP-Outside");
                                startActivity(intent);
                                break;
                            }
                        }
                        break;
                    }
                    case "Drone Mount": {
                        if (editText3.getText().toString().equals("")){
                            Intent scan = new Intent(Menu_Activity.this,Scan_Activity.class);
                            startActivity(scan);
                        }else {
                            final String ip_address = editText3.getText().toString();
                            String[] ip_address_check = editText3.getText().toString().split("[.]");

                            int calculate = 0;
                            char temp;

                            for (int i=0;i<ip_address.length();i++){
                                temp = ip_address.charAt(i);

                                if (temp == '.'){
                                    calculate++;
                                }
                            }

                            if (calculate == 3){
                            /*String key = (String) vehicle.getSelectedItem() + "," + communication.getSelectedItem();
                            user_data.edit().putString(key,ip_address).apply();*/
                                try {
                                    int ip_check = Integer.parseInt(ip_address_check[0]) + Integer.parseInt(ip_address_check[1]) + Integer.parseInt(ip_address_check[2]) + Integer.parseInt(ip_address_check[3]);

                                    new AlertDialog.Builder(Menu_Activity.this)
                                            .setTitle("Skip initialization?")
                                            .setMessage("Are you share you want to skip the initialization?")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent next = new Intent(Menu_Activity.this,Select_module_Activity.class);
                                                    next.putExtra("IP",ip_address);
                                                    next.putExtra("Port","6000");
                                                    next.putExtra("Mode",network_type);
                                                    startActivity(next);
                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent next = new Intent(Menu_Activity.this,Connect_Activity.class);
                                                    next.putExtra("IP",ip_address);
                                                    next.putExtra("Port","6000");
                                                    next.putExtra("Mode",network_type);
                                                    startActivity(next);
                                                }
                                            }).show();
                                }catch (NumberFormatException e) {
                                    Toast.makeText(getApplicationContext(), "IP address is a number", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }else {
                                Toast.makeText(getApplicationContext(),"IP format is mistake",Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    }
                    default: {
                        Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case R.id.button11: {
                editText.setText("");
                editText2.setText("");
                //user_data.edit().clear().apply();
                break;
            }
            case R.id.button12: {
                //No function
                break;
            }
        }
    }

    private void start(String video_number){
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.putExtra("video_number",video_number);
        startActivity(intent);
    }

    private Spinner.OnItemSelectedListener RTSP = new Spinner.OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getSelectedItem().toString()){
                case "Choose": {
                    textView3.setVisibility(View.GONE);
                    spinner.setVisibility(View.GONE);
                    textView4.setVisibility(View.GONE);
                    spinner3.setVisibility(View.GONE);
                    textView5.setVisibility(View.GONE);
                    spinner4.setVisibility(View.GONE);
                    editText3.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                    break;
                }
                case "Experiment": {
                    textView3.setVisibility(View.VISIBLE);
                    spinner.setVisibility(View.VISIBLE);

                    textView4.setVisibility(View.GONE);
                    spinner3.setVisibility(View.GONE);
                    textView5.setVisibility(View.GONE);
                    spinner4.setVisibility(View.GONE);
                    editText3.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                    break;
                }
                case "CCTV": {
                    textView4.setVisibility(View.VISIBLE);
                    spinner3.setVisibility(View.VISIBLE);

                    textView3.setVisibility(View.GONE);
                    spinner.setVisibility(View.GONE);
                    textView5.setVisibility(View.GONE);
                    spinner4.setVisibility(View.GONE);
                    editText3.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                    break;
                }
                case "Drone Mount": {
                    textView5.setVisibility(View.VISIBLE);
                    spinner4.setVisibility(View.VISIBLE);

                    textView4.setVisibility(View.GONE);
                    spinner3.setVisibility(View.GONE);
                    textView3.setVisibility(View.GONE);
                    spinner.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                    break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private Spinner.OnItemSelectedListener sample = new Spinner.OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getSelectedItem().toString()){
                case "Choose": {
                    textView.setVisibility(View.INVISIBLE);
                    button.setEnabled(false);
                    break;
                }
                case "CCTV-Tunnel01": {
                    textView.setText("Freeway 1 North 0K+400\n2021-01-06 14:13:14\n大業隧道");
                    textView.setVisibility(View.VISIBLE);
                    button.setEnabled(true);
                    break;
                }
                case "CCTV-Tunnel02": {
                    textView.setText("Freeway 1 North 0K+400\n2021-01-06 14:01:58\n大業隧道");
                    textView.setVisibility(View.VISIBLE);
                    button.setEnabled(true);
                    break;
                }
                case "Simulation Drone Camera-Outside01": {
                    textView.setText("Freeway 3 North 380K+725\n2021-06-02 16:18:03\n中寮隧道南口");
                    textView.setVisibility(View.VISIBLE);
                    button.setEnabled(true);
                    break;
                }
                case "Simulation Drone Camera-Outside02": {
                    textView.setText("Freeway 3 North 380K+725\n2021-06-02 16:04:35\n中寮隧道南口");
                    textView.setVisibility(View.VISIBLE);
                    button.setEnabled(true);
                    break;
                }
                case "Simulation Drone Camera-Outside03": {
                    textView.setText("Freeway 3 North 380K+725\n2021-12-30 14:24:17\n中寮隧道南口");
                    textView.setVisibility(View.VISIBLE);
                    button.setEnabled(true);
                    break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private Spinner.OnItemSelectedListener location = new Spinner.OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getSelectedItem().toString()){
                case "Choose": {
                    textView.setVisibility(View.INVISIBLE);
                    button.setEnabled(false);
                    break;
                }
                case "Freeway 1 North 0K+400 大業隧道": {
                    textView.setText("Use RTSP view Freeway 1 North 0K+400 大業隧道 CCTV camera");
                    textView.setVisibility(View.VISIBLE);
                    button.setEnabled(true);
                    break;
                }
                case "Freeway 3 North 380K+725 中寮隧道南口": {
                    textView.setText("Use RTSP view Freeway 3 North 380K+725 中寮隧道南口 CCTV camera");
                    textView.setVisibility(View.VISIBLE);
                    button.setEnabled(true);
                    break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private Spinner.OnItemSelectedListener communication = new Spinner.OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getSelectedItem().toString()){
                case "Choose": {
                    textView.setVisibility(View.INVISIBLE);
                    editText3.setVisibility(View.INVISIBLE);
                    button.setEnabled(false);
                    break;
                }
                case "4G Mobile Network": {
                    textView.setText("Use 4G Mobile Network to connect device");
                    textView.setVisibility(View.VISIBLE);
                    editText3.setVisibility(View.VISIBLE);
                    editText3.setText("10.8.0.3");
                    network_type = "lte";
                    button.setEnabled(true);
                    break;
                }
                case "Wi-Fi": {
                    textView.setText("Use Wi-Fi to connect device");
                    textView.setVisibility(View.VISIBLE);
                    editText3.setVisibility(View.VISIBLE);
                    editText3.setText("192.168.4.1");
                    network_type = "wi-fi";
                    button.setEnabled(true);
                    break;
                }
                default:{
                    textView.setText("Still on test. Don't use!");
                    break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
}