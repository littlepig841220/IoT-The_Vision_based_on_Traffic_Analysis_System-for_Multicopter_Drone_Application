package cbs.example.traffic_analysis;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Analysis_Activity extends AppCompatActivity implements View.OnClickListener{
    private TextView textView,textView2,textView3;
    private ImageView imageView_left,imageView_right,imageView_status;
    private Button button,button2,button3,button4;
    private EditText editText;
    private ArrayList<String> data_in_time = new ArrayList<>();//input
    private ArrayList<String> data_in_mat = new ArrayList<>();//input
    private ArrayList<Mat> data_in_frame = new ArrayList<>();
    private ArrayList<Integer> data_in_position_x = new ArrayList<>();//input
    private ArrayList<Integer> data_in_position_y = new ArrayList<>();//input
    private ArrayList<String> start_time_result = new ArrayList<>();
    private ArrayList<String> end_time_result = new ArrayList<>();
    private ArrayList<String> start_coordinate_result = new ArrayList<>();
    private ArrayList<String> end_coordinate_result = new ArrayList<>();
    private ArrayList<String> travel_time_result = new ArrayList<>();
    private ArrayList<String> distance_result = new ArrayList<>();
    private ArrayList<String> speed_result = new ArrayList<>();
    private ArrayList<Float> travel_time_value = new ArrayList<>();
    private ArrayList<Float> distance_value = new ArrayList<>();
    private ArrayList<Float> speed_value = new ArrayList<>();
    private ArrayList<Mat> image_value = new ArrayList<>();
    private ArrayList<Double> compare_value = new ArrayList<>();
    private ArrayList<String> image_result = new ArrayList<>();
    private ArrayList<String> compare_result = new ArrayList<>();
    private int index = 0;
    private int finalI,segment,X,Y,C;
    private Double result;
    private Mat src,hist1,hist2,resource1,resource2,mat;
    private Thread thread;
    private String[] start_time_array;
    private String[] end_time_array;
    private float travel_time,distance,speed,travel_time_average,distance_average,speed_average;
    private float histogram_threshold = 0.8f;
    private float traffic_threshold1 = 200.0f;
    private float traffic_threshold2 = 400.0f;//300
    private boolean show_histograms = true;
    private int histograms_method = 0;
    private String video_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        textView = (TextView) findViewById(R.id.textView4);
        textView2 = (TextView) findViewById(R.id.textView6);
        textView3 = (TextView) findViewById(R.id.textView7);
        imageView_left = (ImageView) findViewById(R.id.imageView2);
        imageView_right = (ImageView) findViewById(R.id.imageView3);
        imageView_status = (ImageView) findViewById(R.id.imageView4);
        button = (Button) findViewById(R.id.button3);
        button2 = (Button) findViewById(R.id.button4);
        button3 = (Button) findViewById(R.id.button5);
        button4 = (Button) findViewById(R.id.button6);
        editText = (EditText) findViewById(R.id.editText2);

        data_in_time = getIntent().getStringArrayListExtra("data_in_time");
        data_in_mat = getIntent().getStringArrayListExtra("data_in_mat");
        data_in_position_x = getIntent().getIntegerArrayListExtra("data_in_position_x");
        data_in_position_y = getIntent().getIntegerArrayListExtra("data_in_position_y");
        video_number = getIntent().getStringExtra("video_number");

        if (video_number.equals("tunnel01") || video_number.equals("tunnel02") || video_number.equals("tunnel")){
            histogram_threshold = 0.8f;
        }else if (video_number.equals("outside01") || video_number.equals("outside02") || video_number.equals("RTSP-Tunnel")){
            histogram_threshold = 0.87f;
        }else if (video_number.equals("outside03") || video_number.equals("RTSP-Outside") || video_number.equals("outside")){
            histogram_threshold = 0.84f;
            traffic_threshold1 = 100.0f;
            traffic_threshold2 = 300.0f;
        }

        for (int n = 0;n < data_in_mat.size(); n++){
            data_in_frame.add(new Mat(Long.parseLong(data_in_mat.get(n))));
        }

        textView.append("Total have " + data_in_time.size() + " data and " + data_in_frame.size() + "pictures\n");
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
            case R.id.button3: {
                if (index == data_in_frame.size()){
                    Toast.makeText(getApplicationContext(),"Out of the pictures",Toast.LENGTH_SHORT).show();
                    index = 0;
                }else {
                    show_image(index);

                    index++;
                }
                break;
            }
            case R.id.button4: {
                thread = new Thread(scan);
                thread.start();

                button.setEnabled(false);
                button2.setEnabled(false);
                button3.setEnabled(false);
                break;
            }
            case R.id.button5: {
                if (button3.getText().toString().equals("Next")){
                    Intent next = new Intent(getApplicationContext(),Detail_Activity.class);
                    next.putStringArrayListExtra("start_time_result",start_time_result);
                    next.putStringArrayListExtra("end_time_result",end_time_result);
                    next.putStringArrayListExtra("start_coordinate_result",start_coordinate_result);
                    next.putStringArrayListExtra("end_coordinate_result",end_coordinate_result);
                    next.putStringArrayListExtra("travel_time_result",travel_time_result);
                    next.putStringArrayListExtra("distance_result",distance_result);
                    next.putStringArrayListExtra("speed_result",speed_result);
                    next.putStringArrayListExtra("compare_result",compare_result);
                    next.putExtra("travel_time_average",travel_time_average);
                    next.putExtra("distance_average",distance_average);
                    next.putExtra("speed_average",speed_average);
                    next.putExtra("video_number",video_number);
                    next.putStringArrayListExtra("image_result",image_result);
                    startActivity(next);
                }else {
                    thread = new Thread(count);
                    thread.start();

                    button.setEnabled(false);
                    button2.setEnabled(false);
                    button3.setEnabled(false);
                }
                break;
            }
            case R.id.button6: {
                final String[] command = editText.getText().toString().split("\\s+");
                if (command.length == 1){
                    String order = command[0];
                    switch (order){
                        case "clear": {
                            textView.setText("");
                            editText.setText("");
                            break;
                        }
                        case "restart": {
                            AlertDialog.Builder alert = new AlertDialog.Builder(Analysis_Activity.this);
                            alert.setIcon(R.drawable.red_light);
                            alert.setTitle("Restart?");
                            alert.setMessage("In this section will give up all of data in memory. It will recollect");
                            alert.setNegativeButton("Cancel",null);
                            alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Analysis_Activity.this,MainActivity.class);
                                    startActivity(intent);
                                }
                            });
                            alert.show();
                            break;
                        }
                        default: {
                            Toast.makeText(getApplicationContext(),"Wrong command or in this section can't use it.",Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }else {
                    switch (command[0]){
                        case "view": {
                            try {
                                index = Integer.valueOf(command[1]);

                                if (index < data_in_frame.size()){
                                    show_image(index);

                                    editText.setText("");
                                }else {
                                    Toast.makeText(getApplicationContext(),"No index number in data.",Toast.LENGTH_SHORT).show();
                                }
                            }catch (NumberFormatException e){
                                Toast.makeText(getApplicationContext(),"view command only can use number",Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        case "histograms": {
                            try {
                                switch (Integer.valueOf(command[1])){
                                    case 0: {
                                        show_histograms = false;
                                        textView.append("Disable to show histograms.\n");
                                        Toast.makeText(getApplicationContext(),"Disable to show histograms",Toast.LENGTH_SHORT).show();
                                        editText.setText("");
                                        break;
                                    }
                                    case 1: {
                                        show_histograms = true;
                                        textView.append("Enable to show histograms.\n");
                                        Toast.makeText(getApplicationContext(),"Enable to show histograms",Toast.LENGTH_SHORT).show();
                                        editText.setText("");
                                        break;
                                    }
                                    default:{
                                        Toast.makeText(getApplicationContext(),"0 to disable, 1 to enable",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }catch (NumberFormatException e){
                                Toast.makeText(getApplicationContext(),"histograms command only can use number",Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        case "compare": {
                            try {
                                if (Integer.valueOf(command[1]) < data_in_frame.size() && Integer.valueOf(command[2]) < data_in_frame.size()){
                                    histograms_compare(Integer.valueOf(command[1]),Integer.valueOf(command[2]));
                                    editText.setText("");
                                }else {
                                    Toast.makeText(getApplicationContext(),"No index number in data.",Toast.LENGTH_SHORT).show();
                                }
                            }catch (NumberFormatException e){
                                Toast.makeText(getApplicationContext(),"compare command only can use number",Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        case "sudo":{
                            AlertDialog.Builder alert = new AlertDialog.Builder(Analysis_Activity.this);

                            final EditText password = new EditText(Analysis_Activity.this);
                            password.setHint("Victory in Europe Day");
                            password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

                            alert.setView(password);
                            alert.setIcon(R.drawable.red_light);
                            alert.setMessage("Some change will cause program crash or enable protection process to limit function");
                            alert.setNegativeButton("Cancel",null);
                            alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (password.getText().toString().equals("19450508")){
                                        textView.append("correct.\n");

                                        switch (command[1]){
                                            case "method": {
                                                try {
                                                    switch (Integer.valueOf(command[2])){
                                                        case 0: {
                                                            histograms_method = 0;
                                                            button3.setVisibility(View.VISIBLE);
                                                            editText.setText("");
                                                            textView.append("Histograms method set on 0\n");
                                                            break;
                                                        }
                                                        case 1: {
                                                            histograms_method = 1;
                                                            button3.setVisibility(View.INVISIBLE);
                                                            editText.setText("");
                                                            textView.append("Histograms method set on 1\n");
                                                            break;
                                                        }
                                                        case 2: {
                                                            histograms_method = 2;
                                                            button3.setVisibility(View.INVISIBLE);
                                                            editText.setText("");
                                                            textView.append("Histograms method set on 2\n");
                                                            break;
                                                        }
                                                        case 3: {
                                                            histograms_method = 3;
                                                            button3.setVisibility(View.INVISIBLE);
                                                            editText.setText("");
                                                            textView.append("Histograms method set on 3\n");
                                                            break;
                                                        }
                                                        case 4: {
                                                            histograms_method = 4;
                                                            button3.setVisibility(View.INVISIBLE);
                                                            editText.setText("");
                                                            textView.append("Histograms method set on 4\n");
                                                            break;
                                                        }
                                                        case 5: {
                                                            histograms_method = 5;
                                                            button3.setVisibility(View.INVISIBLE);
                                                            editText.setText("");
                                                            textView.append("Histograms method set on 5\n");
                                                            break;
                                                        }
                                                        default:{
                                                            Toast.makeText(Analysis_Activity.this,"Histograms method only have 6 ways.",Toast.LENGTH_LONG).show();
                                                            break;
                                                        }
                                                    }
                                                }catch (NumberFormatException e){
                                                    Toast.makeText(getApplicationContext(),"histograms command only can use number",Toast.LENGTH_SHORT).show();
                                                }
                                                break;
                                            }
                                            default:{
                                                Toast.makeText(getApplicationContext(),"Wrong command or in this section can't use it.",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }else {
                                        textView.append("Not correct.\n");
                                    }
                                }
                            });
                            alert.show();
                            break;
                        }
                        default:{
                            Toast.makeText(getApplicationContext(),"Wrong command or in this section can't use it.",Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }
                break;
            }
        }
    }

    private Runnable scan = new Runnable() {
        Handler handler = new Handler();
        @Override
        public void run() {
            segment = 0;
            for (int i = 0;i < data_in_frame.size();i++){
                finalI = i;
                src = new Mat();
                hist1 = new Mat();

                resource1 = data_in_frame.get(finalI).clone();
                Imgproc.cvtColor(resource1,src,Imgproc.COLOR_BGR2GRAY);

                List<Mat> images1 = new ArrayList<>();
                images1.add(src);

                Mat mask1 = Mat.ones(src.size(), CvType.CV_8UC1);

                Imgproc.calcHist(images1,new MatOfInt(0),mask1,hist1,new MatOfInt(256),new MatOfFloat(0,255));

                Core.normalize(hist1,hist1,0,255,Core.NORM_MINMAX);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap1 = Bitmap.createBitmap(resource1.cols(),resource1.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(resource1,bitmap1);
                        imageView_left.setImageBitmap(bitmap1);
                        textView2.setText(String.valueOf(finalI));
                    }
                });

                if (finalI != (data_in_frame.size() - 1)){
                    src = new Mat();
                    hist2 = new Mat();

                    resource2 = data_in_frame.get(finalI + 1).clone();
                    Imgproc.cvtColor(resource2,src,Imgproc.COLOR_BGR2GRAY);

                    List<Mat> images2 = new ArrayList<>();
                    images2.add(src);

                    Mat mask2 = Mat.ones(src.size(), CvType.CV_8UC1);

                    Imgproc.calcHist(images2,new MatOfInt(0),mask2,hist2,new MatOfInt(256),new MatOfFloat(0,255));

                    Core.normalize(hist2,hist2,0,255,Core.NORM_MINMAX);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap2 = Bitmap.createBitmap(resource2.cols(),resource2.rows(),Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(resource2,bitmap2);
                            imageView_right.setImageBitmap(bitmap2);
                            textView3.setText(String.valueOf(finalI + 1));
                        }
                    });
                }

                result = Imgproc.compareHist(hist1,hist2,histograms_method);
                if (result < histogram_threshold){
                    segment++;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView_status.setImageResource(R.drawable.yellow_light);
                        }
                    });
                }else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView_status.setImageResource(R.drawable.gray_light);
                        }
                    });
                }

                if (finalI != data_in_frame.size() - 1){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.append("Index " + finalI + " with Index " + (finalI + 1) + ", there match value is " + result + "\n");
                        }
                    });
                }else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.append("Index " + finalI + " with Index " + finalI + ", there match value is " + result + "\n");
                        }
                    });
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder message = new AlertDialog.Builder(Analysis_Activity.this);
                    message.setMessage("Total have " + segment + " segments.").show();

                    button.setEnabled(true);
                    button2.setEnabled(true);
                    button3.setEnabled(true);
                }
            });
        }
    };

    private Runnable count = new Runnable() {
        Handler handler = new Handler();
        @Override
        public void run() {
            while (true){
                if (data_in_frame.isEmpty()) {//
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Analysis_Activity.this, "Finished", Toast.LENGTH_SHORT).show();
                            //textView.append("All of data are analysis accomplished.\nGet " + speed_result.size() + " travel times.\n");
                        }
                    });
                    break;
                }else {
                    for (int i = 0;i < data_in_frame.size();i++){
                        finalI = i;
                        src = new Mat();
                        hist1 = new Mat();
                        resource1 = new Mat();

                        resource1 = data_in_frame.get(finalI).clone();
                        Imgproc.cvtColor(resource1,src,Imgproc.COLOR_BGR2GRAY);

                        List<Mat> images1 = new ArrayList<>();
                        images1.add(src);

                        Mat mask1 = Mat.ones(src.size(), CvType.CV_8UC1);

                        Imgproc.calcHist(images1,new MatOfInt(0),mask1,hist1,new MatOfInt(256),new MatOfFloat(0,255));

                        Core.normalize(hist1,hist1,0,255,Core.NORM_MINMAX);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bitmap1 = Bitmap.createBitmap(resource1.cols(),resource1.rows(), Bitmap.Config.ARGB_8888);
                                Utils.matToBitmap(resource1,bitmap1);
                                imageView_left.setImageBitmap(bitmap1);
                                textView2.setText(String.valueOf(finalI));
                            }
                        });

                        if (finalI != (data_in_frame.size() - 1)){
                            src = new Mat();
                            hist2 = new Mat();
                            resource2 = new Mat();

                            resource2 = data_in_frame.get(finalI + 1).clone();
                            Imgproc.cvtColor(resource2,src,Imgproc.COLOR_BGR2GRAY);

                            List<Mat> images2 = new ArrayList<>();
                            images2.add(src);

                            Mat mask2 = Mat.ones(src.size(), CvType.CV_8UC1);

                            Imgproc.calcHist(images2,new MatOfInt(0),mask2,hist2,new MatOfInt(256),new MatOfFloat(0,255));

                            Core.normalize(hist2,hist2,0,255,Core.NORM_MINMAX);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap bitmap2 = Bitmap.createBitmap(resource2.cols(),resource2.rows(),Bitmap.Config.ARGB_8888);
                                    Utils.matToBitmap(resource2,bitmap2);
                                    imageView_right.setImageBitmap(bitmap2);
                                    textView3.setText(String.valueOf(finalI + 1));
                                }
                            });
                        }

                        result = Imgproc.compareHist(hist1,hist2,histograms_method);

                        if (result > histogram_threshold){
                            if (finalI != data_in_frame.size() - 1){
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView.append("Index " + finalI + " with Index " + (finalI + 1) + ", there match value is " + result + "\n");
                                        compare_value.add(result);
                                    }
                                });
                            }else {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView.append("Index " + finalI + " with Index " + finalI + ", there match value is " + result + "\n");
                                    }
                                });
                            }
                        }else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    textView.append("Index " + finalI + " and Index " + (finalI +1) + " are different.(" + result + ")\n");
                                    imageView_status.setImageResource(R.drawable.yellow_light);
                                }
                            });
                            break;
                        }

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    String start_time = data_in_time.get(0);
                    start_time_array = start_time.split(":");
                    String end_time = data_in_time.get(finalI);
                    end_time_array =end_time.split(":");
                    travel_time = (float)(Math.round(((Float.parseFloat(end_time_array[1]) *60 + Float.parseFloat(end_time_array[2])) - (Float.parseFloat(start_time_array[1]) * 60 + Float.parseFloat(start_time_array[2])))*1000))/1000;

                    final String first_coordinate = data_in_position_x.get(0) + "," + data_in_position_y.get(0);
                    final String last_coordinate = data_in_position_x.get(finalI) + "," + data_in_position_y.get(finalI);
                    distance = (float) (Math.round(Math.sqrt(Math.pow((data_in_position_x.get(finalI) - data_in_position_x.get(0)),2) + Math.pow((data_in_position_y.get(finalI) - data_in_position_y.get(0)),2)) * 100))/100;

                    speed = distance/travel_time;

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Double compare = 0.0;

                    for (int m = 0;m < compare_value.size();m++){
                        compare = compare + compare_value.get(m);
                    }

                    final float compare_average = (float) Math.round(compare/compare_value.size()*100)/100;

                    compare_value.clear();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView_status.setImageResource(R.drawable.gray_light);
                            textView.append("Index number: " + finalI + "\n");
                            textView.append("This car first detect time is " + start_time_array[1] + ":" + start_time_array[2] + " .\nThe last detect time is " + end_time_array[1] + ":" + end_time_array[2] + ".\n");
                            textView.append("The first coordinate is " + first_coordinate + ".\nThe last coordinate is " + last_coordinate + ".\n");
                            textView.append("Travel time: " + travel_time + "sec.\n");
                            textView.append("Distance moved: " + distance + "\n");
                            textView.append("Speed : " + speed + "\n");
                            textView.append("Compare average: " + compare_average + "\n");
                        }
                    });

                    if (Float.isNaN(speed) || speed == 0.0f){//
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView.append("Invalid information.\n");
                            }
                        });
                    }else {
                        start_time_result.add(start_time);
                        end_time_result.add(end_time);
                        start_coordinate_result.add(first_coordinate);
                        end_coordinate_result.add(last_coordinate);
                        travel_time_result.add(String.valueOf(travel_time));
                        travel_time_value.add(travel_time);
                        distance_result.add(String.valueOf(distance));
                        distance_value.add(distance);
                        speed_result.add(String.valueOf(speed));
                        speed_value.add(speed);
                        image_value.add(data_in_frame.get(0));
                        compare_result.add(String.valueOf(compare_average));
                    }

                    for (int x = 0;x <= finalI;x++){
                        data_in_time.remove(0);
                        data_in_frame.remove(0);
                        data_in_mat.remove(0);
                        data_in_position_x.remove(0);
                        data_in_position_y.remove(0);
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //textView.append("data_in_time: " + data_in_frame.size() + "\ndata_in_frame: "+ data_in_frame.size() + "\ndata_in_mat: " + data_in_mat.size() + "\ndata_in_position_x: " + data_in_position_x.size() + "\ndata_in_position_y: " + data_in_position_y.size() + "\n");
                            //textView.append("start_time_result: " + start_time_result.size() + "\nend_time_result: "+ end_time_result.size() + "\nstart_coordinate_result: " + start_coordinate_result.size() + "\nend_coordinate_result: " + end_coordinate_result.size() + "\ntravel_time_value: " + travel_time_value.size() + "\ndistance_value: " + distance_value.size() + "\nspeed_value: " + speed_value.size() + "\n");
                            textView.append("One round is finished.\nRemaining " + data_in_frame.size() + " data.\nReady to next round.\n");
                        }
                    });
                }
            }

            float travel_time_sum = 0;
            float distance_sum = 0;
            float speed_sum = 0;
            DecimalFormat decimalFormat = new DecimalFormat("##0.00");

            for (int y = 0;y < speed_value.size();y++){
                travel_time_sum = travel_time_sum + travel_time_value.get(y);
                distance_sum = distance_sum + distance_value.get(y);
                speed_sum = speed_sum + speed_value.get(y);
                image_result.add(String.valueOf(image_value.get(y).getNativeObjAddr()));
            }

            travel_time_average = (float) (Math.round((travel_time_sum/travel_time_value.size())*100))/100;
            distance_average = (float) (Math.round((distance_sum/distance_value.size())*100))/100;
            speed_average = (float)(Math.round((speed_sum/speed_value.size())*100))/100;

            if (speed_average <= traffic_threshold1){//200
                imageView_status.setImageResource(R.drawable.red_light);
            }else if (speed_average > traffic_threshold1 && speed_average <= traffic_threshold2){//200~300
                imageView_status.setImageResource(R.drawable.yellow_light);
            }else {
                imageView_status.setImageResource(R.drawable.green_light);
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    editText.setEnabled(false);
                    button3.setEnabled(true);
                    button4.setEnabled(false);

                    button3.setText("Next");

                    textView.append("Average of travel time: " + travel_time_average + "\nAverage of distance: " + distance_average + "\nAverage of speed: " + speed_average + "\nFinished\n");
                    AlertDialog.Builder message = new AlertDialog.Builder(Analysis_Activity.this);
                    message.setMessage("Finished").show();
                }
            });
        }
    };

    private void show_image(int index){
        src = new Mat();
        src = data_in_frame.get(index);

        Bitmap bitmap = Bitmap.createBitmap(src.cols(),src.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src,bitmap);
        imageView_left.setImageBitmap(bitmap);
        textView2.setText(String.valueOf(index));

        if (show_histograms){
            Mat src_gray = new Mat();
            mat = new Mat();

            Imgproc.cvtColor(src,src_gray,Imgproc.COLOR_BGR2GRAY);

            List<Mat> images = new ArrayList<>();
            images.add(src_gray);

            Mat mask = Mat.ones(src_gray.size(), CvType.CV_8UC1);
            Mat hist = new Mat();

            Imgproc.calcHist(images,new MatOfInt(0),mask,hist,new MatOfInt(256),new MatOfFloat(0,255));

            Core.normalize(hist,hist,0,255,Core.NORM_MINMAX);

            int height = hist.rows();

            mat.create(400,400,src_gray.type());
            mat.setTo(new Scalar(200,200,200));

            float[] hist_data = new float[256];
            hist.get(0,0,hist_data);

            int off_set_x = 50;
            int off_set_y = 350;

            Imgproc.line(mat,new Point(off_set_x,0),new Point(off_set_x,off_set_y),new Scalar(0,0,0));
            Imgproc.line(mat,new Point(off_set_x,off_set_y),new Point(400,off_set_y),new Scalar(0,0,0));

            for (int i = 0;i < height - 1;i++){
                int y1 = (int) hist_data[i];
                int y2 = (int) hist_data[i + 1];

                Rect rect = new Rect();

                rect.x = off_set_x + i;
                rect.y = off_set_y - y1;
                rect.width = 1;
                rect.height = y1;

                Imgproc.rectangle(mat,rect.tl(),rect.br(),new Scalar(15,15,15));
            }

            Bitmap bitmap2 = Bitmap.createBitmap(mat.cols(),mat.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat,bitmap2);

            imageView_right.setImageBitmap(bitmap2);
            textView3.setText("");
        }else {
            if (index < (data_in_frame.size() - 1)){
                src = new Mat();
                src = data_in_frame.get(index + 1);

                Bitmap bitmap2 = Bitmap.createBitmap(src.cols(),src.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(src,bitmap2);

                imageView_right.setImageBitmap(bitmap2);
                textView3.setText(String.valueOf(index + 1));
            }
        }
    }

    private void histograms_compare(int compare_index1,int compare_index2){
        src = new Mat();
        hist1 = new Mat();

        resource1 = data_in_frame.get(compare_index1);
        Imgproc.cvtColor(resource1,src,Imgproc.COLOR_BGR2GRAY);

        List<Mat> images1 = new ArrayList<>();
        images1.add(src);

        Mat mask1 = Mat.ones(src.size(), CvType.CV_8UC1);

        Imgproc.calcHist(images1,new MatOfInt(0),mask1,hist1,new MatOfInt(256),new MatOfFloat(0,255));

        Core.normalize(hist1,hist1,0,255,Core.NORM_MINMAX);

        Bitmap bitmap1 = Bitmap.createBitmap(resource1.cols(),resource1.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(resource1,bitmap1);
        imageView_left.setImageBitmap(bitmap1);
        textView2.setText(String.valueOf(compare_index1));

        if (compare_index2 < data_in_frame.size() - 1){
            src = new Mat();
            hist2 = new Mat();

            resource2 = data_in_frame.get(compare_index2);
            Imgproc.cvtColor(resource2,src,Imgproc.COLOR_BGR2GRAY);

            List<Mat> images2 = new ArrayList<>();
            images2.add(src);

            Mat mask2 = Mat.ones(src.size(), CvType.CV_8UC1);

            Imgproc.calcHist(images2,new MatOfInt(0),mask2,hist2,new MatOfInt(256),new MatOfFloat(0,255));

            Core.normalize(hist2,hist2,0,255,Core.NORM_MINMAX);

            Bitmap bitmap2 = Bitmap.createBitmap(resource2.cols(),resource2.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(resource2,bitmap2);
            imageView_right.setImageBitmap(bitmap2);
            textView3.setText(String.valueOf(compare_index2));
        }

        result = Imgproc.compareHist(hist1,hist2,histograms_method);
        textView.append("Index " + compare_index1 + " with Index " + compare_index2 + ", there match value is " + result + "\n");
        Toast.makeText(getApplicationContext(),String.valueOf(result),Toast.LENGTH_SHORT).show();
    }
}

//https://blog.csdn.net/wirelessqa/article/details/8589200
//https://stackoverflow.com/questions/29060376/how-do-i-send-opencv-mat-as-a-putextra-to-android-intent