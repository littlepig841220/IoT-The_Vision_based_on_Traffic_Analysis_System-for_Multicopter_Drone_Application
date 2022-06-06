package cbs.example.traffic_analysis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Date;

public class Detail_Activity extends AppCompatActivity implements View.OnClickListener {
    private TextView textView,textView2;
    private ImageButton imageButton,imageButton2;
    private ImageView imageView,imageView2;
    private ArrayList<String> start_time_result = new ArrayList<>();
    private ArrayList<String> end_time_result = new ArrayList<>();
    private ArrayList<String> start_coordinate_result = new ArrayList<>();
    private ArrayList<String> end_coordinate_result = new ArrayList<>();
    private ArrayList<String> travel_time_result = new ArrayList<>();
    private ArrayList<String> distance_result = new ArrayList<>();
    private ArrayList<String> speed_result = new ArrayList<>();
    private ArrayList<String> image_result = new ArrayList<>();
    private ArrayList<String> compare_result = new ArrayList<>();
    private float travel_time_average,distance_average,speed_average;
    int index = 0;
    int X1,Y1,C1;
    int X2,Y2,C2;
    private String lane = "Null";
    private String video_number;
    private DatabaseReference databaseReference;
    private float traffic_threshold1 = 200.0f;
    private float traffic_threshold2 = 400.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        textView = (TextView) findViewById(R.id.textView10);
        textView2 = (TextView) findViewById(R.id.textView11);
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton2 = (ImageButton) findViewById(R.id.imageButton2);
        imageView = (ImageView) findViewById(R.id.imageView7);
        imageView2 = (ImageView) findViewById(R.id.imageView8);

        start_time_result = getIntent().getStringArrayListExtra("start_time_result");
        end_time_result = getIntent().getStringArrayListExtra("end_time_result");
        start_coordinate_result = getIntent().getStringArrayListExtra("start_coordinate_result");
        end_coordinate_result = getIntent().getStringArrayListExtra("end_coordinate_result");
        travel_time_result = getIntent().getStringArrayListExtra("travel_time_result");
        distance_result = getIntent().getStringArrayListExtra("distance_result");
        speed_result = getIntent().getStringArrayListExtra("speed_result");
        image_result = getIntent().getStringArrayListExtra("image_result");
        travel_time_average = getIntent().getFloatExtra("travel_time_average",0);
        distance_average = getIntent().getFloatExtra("distance_average",0);
        speed_average = getIntent().getFloatExtra("speed_average",0);
        compare_result = getIntent().getStringArrayListExtra("compare_result");
        video_number = getIntent().getStringExtra("video_number");

        //textView.setText(start_time_result.size() + "\n" + end_time_result.size() + "\n" + start_coordinate_result.size() + "\n" + end_coordinate_result.size() + "\n" + travel_time_result.size() + "\n" + distance_result.size() + "\n" + speed_result.size() + "\n" + image_result.size());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String path = formatter.format(date);

        databaseReference = FirebaseDatabase.getInstance().getReference("Traffic Analysis/" + path);

        textView.append("Number of cars: " + image_result.size() + "\nTravel time average: " + travel_time_average + "\nDistance average: " + distance_average + "\nSpeed average: " + speed_average + "\n");

        find_equation(video_number);
        show_data(index);

        if (video_number.equals("outside03") || video_number.equals("RTSP-Outside") || video_number.equals("outside")){
            traffic_threshold1 = 100.0f;
            traffic_threshold2 = 300.0f;
        }

        if (speed_average <= traffic_threshold1){
            imageView2.setImageResource(R.drawable.red_light);
        }else if (speed_average > traffic_threshold1 && speed_average <= traffic_threshold2){
            imageView2.setImageResource(R.drawable.yellow_light);
        }else {
            imageView2.setImageResource(R.drawable.green_light);
        }

        databaseReference.child("Number of cars").setValue(image_result.size());
        databaseReference.child("Travel time average").setValue(travel_time_average);
        databaseReference.child("Distance average").setValue(distance_average);
        databaseReference.child("Speed average").setValue(speed_average);
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
            case R.id.imageButton: {
                if (index > 0){
                    index--;

                    show_data(index);
                }
                break;
            }
            case R.id.imageButton2: {
                if (index < distance_result.size() - 1){
                    index++;

                    show_data(index);
                }
                break;
            }
        }
    }

    private void show_data(int index){
        String[] location_array = start_coordinate_result.get(index).split(",");

        if (video_number.equals("tunnel01") || video_number.equals("tunnel02") || video_number.equals("RTSP-Tunnel") || video_number.equals("tunnel")){
            int location = X1 * Integer.valueOf(location_array[0]) + Y1 * Integer.valueOf(location_array[1]) + C1;

            if (location > 0){
                lane = "right";
            }else if (location < 0){
                lane = "left";
            }else {
                lane = "On the boundary";
            }
        }else if (video_number.equals("outside01") || video_number.equals("outside02") || video_number.equals("outside03") || video_number.equals("RTSP-Outside") || video_number.equals("outside")){
            int location = X1 * Integer.valueOf(location_array[0]) + Y1 * Integer.valueOf(location_array[1]) + C1;
            int location2 = X2 * Integer.valueOf(location_array[0]) + Y2 * Integer.valueOf(location_array[1]) + C2;

            if (location > 0){
                lane = "right";
            }else if (location2 < 0){
                lane = "left";
            }else if (location < 0 && location2 > 0){
                lane = "middle";
            }else {
                lane = "On the boundary";
            }
        }

        textView2.setText("Car number: " + (index + 1) +
                "\nStart time: " + start_time_result.get(index) +
                "\nEnd time: " + end_time_result.get(index) +
                "\nStart coordinate: " + start_coordinate_result.get(index) +
                "\nEnd coordinate: " + end_coordinate_result.get(index) +
                "\nTravel time: " + travel_time_result.get(index) +
                "\nDistance: " + distance_result.get(index) +
                "\nSpeed ratio: " + speed_result.get(index) +
                "\nLane: " + lane +
                "\nCompare average: " + compare_result.get(index));

        try {
            String data = image_result.get(index);
            Mat mat = new Mat(Long.parseLong(data));
            Bitmap bitmap = Bitmap.createBitmap(mat.cols(),mat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat,bitmap);
            imageView.setImageBitmap(bitmap);
        }catch (Exception e){
            e.printStackTrace();
        }

        databaseReference.child((index + 1) + "/Start time").setValue(start_time_result.get(index));
        databaseReference.child((index + 1) + "/End time").setValue(end_time_result.get(index));
        databaseReference.child((index + 1) + "/Start coordinate").setValue(start_coordinate_result.get(index));
        databaseReference.child((index + 1) + "/End coordinate").setValue(end_coordinate_result.get(index));
        databaseReference.child((index + 1) + "/Travel time").setValue(travel_time_result.get(index));
        databaseReference.child((index + 1) + "/Distance").setValue(distance_result.get(index));
        databaseReference.child((index + 1) + "/Speed").setValue(speed_result.get(index));
        databaseReference.child((index + 1) + "/Lane").setValue(lane);
        databaseReference.child((index + 1) + "/Compare average").setValue(compare_result.get(index));
    }

    private void find_equation(String video_number){
        if (video_number.equals("tunnel01") || video_number.equals("tunnel02") || video_number.equals("tunnel")){
            int x1,x2,y1,y2;

            x1 = 1150;
            y1 = 400;

            x2 = 950;
            y2 = 948;

            X1=y2-y1;
            Y1=-(x2-x1);
            C1=(-(x2-x1)*-y1)+((y2-y1)*-x1);

            textView.append("Lane equation: " +X1+"x+"+Y1+"y+"+C1+"=0\n");

            databaseReference.child("Lane equation").setValue(X1+"x+"+Y1+"y+"+C1+"=0");
        }else if (video_number.equals("outside01") || video_number.equals("outside02")){
            int x1,x2,y1,y2;
            int x3,x4,y3,y4;

            x1 = 1300;
            y1 = 400;

            x2 = 400;
            y2 = 948;

            x3 = 1150;
            y3 = 400;

            x4 = 0;
            y4 = 948;


            X1=y2-y1;
            Y1=-(x2-x1);
            C1=(-(x2-x1)*-y1)+((y2-y1)*-x1);

            X2=y4-y3;
            Y2=-(x4-x3);
            C2=(-(x4-x3)*-y3)+((y4-y3)*-x3);

            textView.append("Lane equation 1: " +X1+"x+"+Y1+"y+"+C1+"=0\n");//R
            textView.append("Lane equation 2: " +X2+"x+"+Y2+"y+"+C2+"=0\n");//L

            databaseReference.child("Lane equation 1").setValue(X1+"x+"+Y1+"y+"+C1+"=0");
            databaseReference.child("Lane equation 2").setValue(X2+"x+"+Y2+"y+"+C2+"=0");
        }else if (video_number.equals("RTSP-Tunnel")){
            int x1,x2,y1,y2;

            x1 = 610;
            y1 = 350;

            x2 = 520;
            y2 = 790;

            X1=y2-y1;
            Y1=-(x2-x1);
            C1=(-(x2-x1)*-y1)+((y2-y1)*-x1);

            textView.append("Lane equation: " +X1+"x+"+Y1+"y+"+C1+"=0\n");

            databaseReference.child("Lane equation").setValue(X1+"x+"+Y1+"y+"+C1+"=0");
        }else if (video_number.equals("outside03") || video_number.equals("RTSP-Outside") || video_number.equals("outside")){
            //Toast.makeText(getApplicationContext(),"test",Toast.LENGTH_SHORT).show();
            int x1,x2,y1,y2;
            int x3,x4,y3,y4;

            x1 = 1950;
            y1 = 500;

            x2 = 1115;
            y2 = 948;

            x3 = 1800;
            y3 = 500;

            x4 = 690;
            y4 = 948;


            X1=y2-y1;
            Y1=-(x2-x1);
            C1=(-(x2-x1)*-y1)+((y2-y1)*-x1);

            X2=y4-y3;
            Y2=-(x4-x3);
            C2=(-(x4-x3)*-y3)+((y4-y3)*-x3);

            textView.append("Lane equation 1: " +X1+"x+"+Y1+"y+"+C1+"=0\n");//R
            textView.append("Lane equation 2: " +X2+"x+"+Y2+"y+"+C2+"=0\n");//L

            databaseReference.child("Lane equation 1").setValue(X1+"x+"+Y1+"y+"+C1+"=0");
            databaseReference.child("Lane equation 2").setValue(X2+"x+"+Y2+"y+"+C2+"=0");
        }
    }
}