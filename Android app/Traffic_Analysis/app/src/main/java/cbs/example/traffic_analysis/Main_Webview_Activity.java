package cbs.example.traffic_analysis;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_PLAIN;

public class Main_Webview_Activity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, View.OnLayoutChangeListener {
    private TextView textView, textView2, textView3;
    private WebView webView;
    private ImageView imageView, imageView2, imageView3,imageView4;
    private Button button,button2,button3;
    private EditText editText;
    private Switch aSwitch,bSwitch;
    private int imageView_width, imageView_height, detect_boundary_min, detect_boundary_max;
    private int frame = 0;
    private int index = 0;
    private String video_number;
    private InputStream inputStream;
    private File cascadeDir;
    private File file;
    private CascadeClassifier carDetector;
    private Mat src, gray, gaussian_blur, picture;
    private List<Rect> car_list;
    private Scalar scalar = new Scalar(255, 0, 0, 255);
    private ArrayList<String> data_in_time = new ArrayList<>();
    private ArrayList<Mat> data_in_frame = new ArrayList<>();
    private ArrayList<String> data_in_mat = new ArrayList<>();
    private ArrayList<Integer> data_in_position_x = new ArrayList<>();
    private ArrayList<Integer> data_in_position_y = new ArrayList<>();
    private Handler handler = new Handler();
    private boolean flag1 = false;
    private boolean flag2 = true;
    private boolean lock = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_webview);

        textView = (TextView) findViewById(R.id.textView2);
        textView2 = (TextView) findViewById(R.id.textView5);
        textView3 = (TextView) findViewById(R.id.textView8);
        webView = (WebView) findViewById(R.id.WebView);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView2 = (ImageView) findViewById(R.id.imageView5);
        imageView3 = (ImageView) findViewById(R.id.imageView6);
        imageView4 = (ImageView) findViewById(R.id.imageView13);
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button7);
        editText = (EditText) findViewById(R.id.editText);
        aSwitch = (Switch) findViewById(R.id.switch1);
        bSwitch = (Switch) findViewById(R.id.switch2);

        aSwitch.setOnCheckedChangeListener(this);
        bSwitch.setOnCheckedChangeListener(start_detect);
        imageView4.setOnClickListener(this);
        webView.addOnLayoutChangeListener(this);

        bSwitch.setEnabled(true);

        video_number = getIntent().getStringExtra("video_number");

        switch (video_number) {
            case "RTSP-Tunnel": {
                webView.loadUrl("https://cctvn.freeway.gov.tw/abs2mjpg/bmjpg?camera=10040&0.9097718327235671");
                detect_boundary_min = 350;
                detect_boundary_max = 790;
                break;
            }
            case "RTSP-Outside": {
                webView.loadUrl("https://cctvs.freeway.gov.tw/live-view/mjpg/video.cgi?camera=396&0.4836240252986166&t1968=0.33029977463202065");
                detect_boundary_min = 450;
                detect_boundary_max = 790;
                break;
            }
            default: {
                webView.loadUrl("https://cctvn.freeway.gov.tw/abs2mjpg/bmjpg?camera=10040&0.9097718327235671");
                Toast.makeText(getApplicationContext(), "Somethings is not correct", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        webView.getSettings().setJavaScriptEnabled(true);
        //webView.getSettings().setUseWideViewPort(true);
        //webView.getSettings().setLoadWithOverviewMode(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                //Toast.makeText(getApplicationContext(), "start", Toast.LENGTH_SHORT).show();

                handler.post(image_identify);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                handler.removeCallbacks(image_identify);
                if (flag2){
                    webView.reload();

                    //Toast.makeText(getApplicationContext(), "re-flash", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            //Toast.makeText(getApplicationContext(),"Internal OpenCV library not found. Using OpenCV Manager for initialization",Toast.LENGTH_SHORT);
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            //Toast.makeText(getApplicationContext(),"OpenCV library found inside package. Using it!",Toast.LENGTH_SHORT);
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Fullscreen_setting fullscreenSetting = new Fullscreen_setting();
            fullscreenSetting.window = getWindow();
            fullscreenSetting.fullscreen();

            imageView3.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    /*imageView_width = imageView.getWidth();
                    imageView_height = imageView.getHeight();

                    Bitmap bitmap = Bitmap.createBitmap(imageView_width, imageView_height, Bitmap.Config.ARGB_8888);

                    Canvas canvas = new Canvas(bitmap);

                    Paint paint = new Paint();
                    paint.setStrokeWidth(8);
                    paint.setColor(Color.GREEN);
                    paint.setStyle(Paint.Style.STROKE);//fill:填滿,stroke:邊

                    if (video_number.equals("RTSP-Tunnel")) {
                        canvas.drawLine(0, imageView_height - 100, imageView_width, imageView_height - 100, paint);
                        canvas.drawLine(0, 400, imageView_width, 400, paint);
                        //canvas.drawLine(1100, 400, 940, imageView_height - 100, paint);
                    } else if (video_number.equals("RTSP-Outside")) {
                        canvas.drawLine(0, imageView_height - 100, imageView_width, imageView_height - 100, paint);
                        canvas.drawLine(0, 500, imageView_width, 500, paint);
                        canvas.drawLine(1800, 500, 700, imageView_height - 100, paint);
                        canvas.drawLine(1950, 500, 1120, imageView_height - 100, paint);
                        //detect_boundary_max = imageView_height;
                    } else {
                        canvas.drawLine(0, imageView_height - 100, imageView_width, imageView_height - 100, paint);
                        canvas.drawLine(0, 400, imageView_width, 400, paint);
                        //detect_boundary_max = imageView_height;
                    }
                    imageView3.setImageBitmap(bitmap);*/
                    return true;
                }
            });
        }
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        imageView_width = webView.getWidth();
        imageView_height = webView.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(imageView_width, imageView_height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setStrokeWidth(8);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);//fill:填滿,stroke:邊

        if (video_number.equals("RTSP-Tunnel")) {
            canvas.drawLine(0, imageView_height - 50, imageView_width, imageView_height - 50, paint);
            canvas.drawLine(0, 350, imageView_width, 350, paint);
            canvas.drawLine(610, 350, 520, imageView_height - 50, paint);
        } else if (video_number.equals("RTSP-Outside")) {
            canvas.drawLine(0, imageView_height - 50, imageView_width, imageView_height - 50, paint);
            canvas.drawLine(0, 450, imageView_width, 450, paint);
            canvas.drawLine(930, 450, 340, imageView_height - 50, paint);
            canvas.drawLine(1030, 450, 590, imageView_height - 50, paint);
            //detect_boundary_max = imageView_height;
        } else {
            canvas.drawLine(0, imageView_height - 100, imageView_width, imageView_height - 100, paint);
            canvas.drawLine(0, 400, imageView_width, 400, paint);
            //detect_boundary_max = imageView_height;
        }
        imageView3.setImageBitmap(bitmap);
        aSwitch.setEnabled(true);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "成功加载opencv！", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                    try {
                        if (video_number.equals("RTSP-Tunnel")) {
                            inputStream = getResources().openRawResource(R.raw.car_tunnel);

                            cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                            file = new File(cascadeDir.getAbsoluteFile(), "car_tunnel.xml");
                        } else if (video_number.equals("RTSP-Outside")) {
                            inputStream = getResources().openRawResource(R.raw.car_outside3);

                            cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                            file = new File(cascadeDir.getAbsoluteFile(), "car_outside3.xml");
                        } else {
                            inputStream = getResources().openRawResource(R.raw.car_outside2);

                            cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                            file = new File(cascadeDir.getAbsoluteFile(), "car_outside2.xml");
                        }
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int bytes_read = 0;
                        while ((bytes_read = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, bytes_read);
                        }

                        inputStream.close();
                        fileOutputStream.close();

                        carDetector = new CascadeClassifier(file.getAbsolutePath());
                        cascadeDir.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.onManagerConnected(status);
                    Toast toast1 = Toast.makeText(getApplicationContext(),
                            "加载失败！", Toast.LENGTH_LONG);
                    toast1.setGravity(Gravity.CENTER, 0, 0);
                    toast1.show();
                    break;
            }

        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button || v.getId() == R.id.button7){
            imageView4.setOnLongClickListener(delete_image);
            lock = true;
        }
        switch (v.getId()) {
            case R.id.button: {
                if (index == data_in_frame.size() || data_in_frame.size() == 0){
                    Toast.makeText(getApplicationContext(),"Out of the pictures",Toast.LENGTH_SHORT).show();
                    index = 0;
                }else {
                    show_image(index);

                    index++;
                }
                break;
            }
            case R.id.button7: {
                if (index == 0){
                    Toast.makeText(getApplicationContext(),"Less of the pictures",Toast.LENGTH_SHORT).show();
                    index = data_in_frame.size() - 1;
                }else {
                    --index;

                    show_image(index);
                }
                break;
            }
            case R.id.button8: {
                String[] command = editText.getText().toString().split("\\s+");
                if (command.length == 1){
                    String order = command[0];
                    switch (order){
                        case "clear": {
                            textView.setText("");
                            editText.setText("");
                            break;
                        }
                        case "restart": {
                            textView.setText("");
                            editText.setText("");
                            recreate();
                            break;
                        }
                        default: {
                            Toast.makeText(getApplicationContext(),"Wrong command or in this section can't use it.",Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }else {
                    switch (command[0]){
                        case "view":{
                            if (lock){
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
                            }else {
                                Toast.makeText(getApplicationContext(),"Please wait video play finish or view less of a picture",Toast.LENGTH_LONG).show();
                            }
                        }
                        case "area": {
                            try {
                                switch (Integer.valueOf(command[1])){
                                    case 0: {
                                        imageView3.setVisibility(View.INVISIBLE);
                                        editText.setText("");
                                        break;
                                    }
                                    case 1: {
                                        imageView3.setVisibility(View.VISIBLE);
                                        editText.setText("");
                                        break;
                                    }
                                    default:{
                                        Toast.makeText(getApplicationContext(),"0 to disable, 1 to enable",Toast.LENGTH_SHORT).show();
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
                            break;
                        }
                    }
                }
                break;
            }
            case R.id.button2: {
                if (data_in_frame.size() != 0){
                    flag2 = false;
                    Intent analysis = new Intent(getApplicationContext(),Analysis_Activity.class);
                    analysis.putStringArrayListExtra("data_in_time",data_in_time);
                    analysis.putStringArrayListExtra("data_in_mat",data_in_mat);
                    analysis.putIntegerArrayListExtra("data_in_position_x",data_in_position_x);
                    analysis.putIntegerArrayListExtra("data_in_position_y",data_in_position_y);
                    analysis.putExtra("video_number",video_number);
                    startActivity(analysis);
                }else {
                    Toast.makeText(getApplicationContext(),"Data is empty",Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.imageView13: {
                imageView4.setVisibility(View.INVISIBLE);
                break;
            }
        }
    }

    private View.OnLongClickListener delete_image = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            AlertDialog.Builder alert = new AlertDialog.Builder(Main_Webview_Activity.this);
            alert.setTitle("Delete the image data");
            alert.setMessage("Are you share you want to delete this image data?(Index: " + textView2.getText() + ")");
            alert.setNegativeButton("No",null);
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int delete_index = Integer.valueOf(textView2.getText().toString());
                    data_in_time.remove(delete_index);
                    data_in_mat.remove(delete_index);
                    data_in_frame.remove(delete_index);
                    data_in_position_x.remove(delete_index);
                    data_in_position_y.remove(delete_index);

                    index = 0;

                    textView.append("Delete image: " + delete_index + ".\nRemaining " + data_in_frame.size() + "data.\n");
                }
            });
            alert.show();
            return true;
        }
    };

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.isChecked()) {
            imageView3.setVisibility(View.VISIBLE);
        } else {
            imageView3.setVisibility(View.INVISIBLE);
        }
    }

    private CompoundButton.OnCheckedChangeListener start_detect = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.isChecked()){
                flag1 = true;
                //Toast.makeText(getApplicationContext(),"Completed and get total " + data_in_time.size() + " data.\n",Toast.LENGTH_SHORT).show();
                button.setEnabled(false);
                button2.setEnabled(false);
                button3.setEnabled(false);
                //textView.append("Width: " + imageView_width + ",Height: " + imageView_height + "\n");
                //textView.append("Completed and get total " + data_in_time.size() + " data.");
            }else {
                flag1 = false;
                Toast.makeText(getApplicationContext(),"Completed and get total " + data_in_time.size() + " data.\n",Toast.LENGTH_SHORT).show();
                button.setEnabled(true);
                button2.setEnabled(true);
                button3.setEnabled(true);
                textView.append("Width: " + imageView_width + ",Height: " + imageView_height + "\n");
                textView.append("Completed and get total " + data_in_time.size() + " data.");
            }
        }
    };

    private Runnable image_identify = new Runnable() {
        Handler handler = new Handler();
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Picture picture = view.capturePicture();
                    //or
                    Picture picture = webView.capturePicture();
                    if (picture.getHeight() != 0 && picture.getWidth() != 0) {
                        Bitmap bitmap = Bitmap.createBitmap(picture.getWidth(),picture.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        picture.draw(canvas);

                        if (flag1){
                            src = new Mat();
                            gray = new Mat();
                            gaussian_blur = new Mat();


                            Utils.bitmapToMat(bitmap,src);

                            Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY );

                            Imgproc.GaussianBlur(gray,gaussian_blur,new Size(3,3),1,1);
                            //Imgproc.blur(gray,gaussian_blur,new Size(3,3),new Point(0,0), Core.BORDER_DEFAULT);

                            MatOfRect cars = new MatOfRect();
                            if (video_number.equals("RTSP-Tunnel")){
                                carDetector.detectMultiScale(gaussian_blur,cars,1.1,17,0,new Size(50,50),new Size());
                            }else if (video_number.equals("RTSP-Outside")){
                                carDetector.detectMultiScale(gaussian_blur,cars,1.1,19,0,new Size(70,70),new Size());
                            }else {
                                carDetector.detectMultiScale(gaussian_blur,cars,1.1,70,0,new Size(70,70),new Size());
                            }

                            car_list = cars.toList();

                            if (car_list.size() > 0){
                                textView3.setText("Car: " + car_list.size());
                                if (car_list.size() > 5){
                                    imageView2.setImageResource(R.drawable.red_light);
                                }else {
                                    imageView2.setImageResource(R.drawable.green_light);
                                }
                                for (Rect rect : car_list){
                                    if (rect.y + rect.height/2 > detect_boundary_min && rect.y + rect.height/2 < detect_boundary_max){
                                        Imgproc.rectangle(src,rect.tl(),rect.br(),scalar,3,8,0);
                                        Imgproc.putText(src,"car",new Point(rect.x,rect.y - 10),FONT_HERSHEY_PLAIN, 4, new Scalar(255,0,0), 4);

                                        //Imgproc.line(src,new Point(0,imageView_height - 100),new Point(imageView_width, imageView_height-100),new Scalar(0,0,255,255),10);
                                        //Imgproc.line(src,new Point(0, 400),new Point(imageView_width, 400),new Scalar(0,0,255,255),10);

                                        String path = String.valueOf(java.time.LocalDateTime.now());
                                        textView.append(frame + ":\n" + path + "\nfind a car frame.\n");
                                        //textView.append(String.valueOf(rect.y + rect.height/2) + "\n");

                                        data_in_time.add(path);
                                        data_in_position_x.add(rect.x + rect.width/2);
                                        data_in_position_y.add(rect.y + rect.height/2);

                                        Mat mat = new Mat(src,rect);
                                        data_in_frame.add(mat);
                                        data_in_mat.add(String.valueOf(mat.getNativeObjAddr()));

                                        frame++;
                                    }
                                }
                            }else {
                                textView3.setText("Car: 0");
                                imageView2.setImageResource(R.drawable.gray_light);
                            }

                            Utils.matToBitmap(src,bitmap);
                        }
                        imageView.setImageBitmap(bitmap);

                        handler.postDelayed(image_identify,50);
                    }
                }
            });
        }
    };

    private void show_image(int index){
        picture = new Mat();
        picture = data_in_frame.get(index).clone();

        Bitmap bitmap = Bitmap.createBitmap(data_in_frame.get(index).cols(),data_in_frame.get(index).rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(picture,bitmap);
        imageView4.setImageBitmap(bitmap);
        imageView4.setVisibility(View.VISIBLE);

        textView2.setText(String.valueOf(index));
    }


}