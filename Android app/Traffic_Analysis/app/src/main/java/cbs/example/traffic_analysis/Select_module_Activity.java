package cbs.example.traffic_analysis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Select_module_Activity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private TextView textView;
    private Spinner spinner;
    private Button button;
    private ImageView imageView;
    private String ip,port,module,network_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_module);

        textView = (TextView) findViewById(R.id.textView23);
        spinner = (Spinner) findViewById(R.id.spinner5);
        button = (Button) findViewById(R.id.button15);
        imageView = (ImageView) findViewById(R.id.imageView12);

        spinner.setOnItemSelectedListener(this);

        ip = getIntent().getStringExtra("IP");
        port = getIntent().getStringExtra("Port");
        network_type = getIntent().getStringExtra("Mode");

        //textView.setText(ip + port);
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
        if (spinner.getSelectedItem().toString().equals("Choose")){
            Toast.makeText(getApplicationContext(),"You must select a module!!",Toast.LENGTH_SHORT).show();
        }else {
            Intent intent = new Intent(Select_module_Activity.this, Main_Mount_Activity.class);
            intent.putExtra("IP",ip);
            intent.putExtra("Port",port);
            intent.putExtra("Module",module);
            intent.putExtra("Mode",network_type);
            startActivity(intent);
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getSelectedItem().toString()){
            case "Choose": {
                imageView.setVisibility(View.INVISIBLE);
                textView.setText("Description");
                button.setEnabled(false);
                break;
            }
            case "CCTV": {
                module = "tunnel";
                imageView.setImageResource(R.drawable.tunnel);
                imageView.setVisibility(View.VISIBLE);
                textView.setText("Description");
                textView.append("\n\n  This module used in the CCTV of Experiment. The module is design for this angle to vehicle like the picture on above. You need turn camera in correct angle to get the best image identify.");
                button.setEnabled(true);
                break;
            }
            case "Simulation Drone": {
                module = "outside";
                imageView.setImageResource(R.drawable.outside2);
                imageView.setVisibility(View.VISIBLE);
                textView.setText("Description");
                textView.append("\n\n  This module used in the Simulation Drone Camera of Experiment. The module is design for this angle to vehicle like the picture on above. You need turn camera in correct angle to get the best image identify.");
                button.setEnabled(true);
                break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}