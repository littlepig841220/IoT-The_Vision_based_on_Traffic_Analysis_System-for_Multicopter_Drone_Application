package cbs.example.traffic_analysis;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewTreeObserver;
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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_PLAIN;

public class Main_Mount_Activity extends AppCompatActivity implements TextureView.SurfaceTextureListener, MediaPlayer.OnVideoSizeChangedListener, View.OnClickListener , CompoundButton.OnCheckedChangeListener, View.OnLongClickListener, View.OnTouchListener{
    private MediaPlayer mediaPlayer;
    private TextureView textureView;
    private ImageView imageView,imageView2,imageView3;
    private TextView textView,textView2,textView3;
    private Button button,button2,button3,button4,button5,button6,button7;
    private EditText editText;
    private Switch aSwitch,bSwitch;
    private AssetFileDescriptor assetFileDescriptor;
    private Mat src,gray,gaussian_blur,picture;
    private CascadeClassifier carDetector;
    private Scalar scalar = new Scalar(255,0,0,255);
    private int imageView_width,imageView_height,detect_boundary_min,detect_boundary_max;
    private ArrayList<String> data_in_time = new ArrayList<>();
    private ArrayList<Mat> data_in_frame = new ArrayList<>();
    private ArrayList<String> data_in_mat = new ArrayList<>();
    private ArrayList<Integer> data_in_position_x = new ArrayList<>();
    private ArrayList<Integer> data_in_position_y = new ArrayList<>();
    private List<Rect> car_list;
    private int frame = 0;
    private int index = 0;
    private boolean lock = false;
    private String module,ip,port;
    private InputStream inputStream;
    private File cascadeDir;
    private File file;
    private Handler servo1_up_handler = new Handler();
    private Handler servo1_down_handler = new Handler();
    private boolean flag1 = false;
    protected boolean out_of_range_up = true;
    protected boolean out_of_range_down = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = (TextureView) findViewById(R.id.textureView);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView2 = (ImageView) findViewById(R.id.imageView5);
        imageView3 = (ImageView) findViewById(R.id.imageView6);
        textView = (TextView) findViewById(R.id.textView2);
        textView2 = (TextView) findViewById(R.id.textView5);
        textView3 = (TextView) findViewById(R.id.textView8);
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button7);
        button4 = (Button) findViewById(R.id.button17);
        button5 = (Button) findViewById(R.id.button18);
        button6 = (Button) findViewById(R.id.button19);
        button7 = (Button) findViewById(R.id.button20);
        editText = (EditText) findViewById(R.id.editText);
        aSwitch = (Switch) findViewById(R.id.switch1);
        bSwitch = (Switch) findViewById(R.id.switch2);

        textureView.setSurfaceTextureListener(this);
        aSwitch.setOnCheckedChangeListener(this);
        bSwitch.setOnCheckedChangeListener(start_detect);

        mediaPlayer = new MediaPlayer();

        module = getIntent().getStringExtra("Module");
        ip = getIntent().getStringExtra("IP");
        port = getIntent().getStringExtra("Port");

        if (module.equals("tunnel")){
            detect_boundary_min = 400;
            detect_boundary_max = 948;
        }else if (module.equals("outside")){
            detect_boundary_min = 500;
            detect_boundary_max = 948;
        }else {
            detect_boundary_min = 400;
            detect_boundary_max = 948;
        }

        button4.setOnTouchListener(this);
        button5.setOnTouchListener(this);
        button6.setOnTouchListener(this);
        button7.setOnTouchListener(this);

        imageView3.setVisibility(View.INVISIBLE);

        Thread thread2 = new Thread(new Server());
        thread2.start();
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Fullscreen_setting fullscreenSetting = new Fullscreen_setting();
            fullscreenSetting.window = getWindow();
            fullscreenSetting.fullscreen();

            imageView3.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    imageView_width = imageView.getWidth();
                    imageView_height = imageView.getHeight();
                    Bitmap bitmap = Bitmap.createBitmap(imageView_width,imageView_height,Bitmap.Config.ARGB_8888);

                    Canvas canvas = new Canvas(bitmap);

                    Paint paint = new Paint();
                    paint.setStrokeWidth(8);
                    paint.setColor(Color.GREEN);
                    paint.setStyle(Paint.Style.STROKE);//fill:填滿,stroke:邊

                    if (module.equals("tunnel")){
                        canvas.drawLine(0, imageView_height-100,imageView_width, imageView_height-100,paint);
                        canvas.drawLine(0, 400,imageView_width, 400,paint);
                        canvas.drawLine(1150,400,950,imageView_height-100,paint);
                    }else if (module.equals("outside")){
                        canvas.drawLine(0, imageView_height-100,imageView_width, imageView_height-100,paint);
                        canvas.drawLine(0, 500,imageView_width, 500,paint);
                        canvas.drawLine(1950,500,1115,imageView_height-100,paint);
                        canvas.drawLine(1800,500,690,imageView_height-100,paint);
                    }
                    else {
                        canvas.drawLine(0, imageView_height-100,imageView_width, imageView_height-100,paint);
                        canvas.drawLine(0, 400,imageView_width, 400,paint);
                    }
                    imageView3.setImageBitmap(bitmap);
                    return true;
                }
            });
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "succeed to load opencv!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                    try {
                        if (module.equals("tunnel")){
                            inputStream = getResources().openRawResource(R.raw.car_tunnel);

                            cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                            file = new File(cascadeDir.getAbsoluteFile(),"car_tunnel.xml");
                        }else if (module.equals("outside")){
                            inputStream = getResources().openRawResource(R.raw.car_outside4);

                            cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                            file = new File(cascadeDir.getAbsoluteFile(),"car_outside4.xml");
                        }else {
                            inputStream = getResources().openRawResource(R.raw.car_outside2);

                            cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                            file = new File(cascadeDir.getAbsoluteFile(),"car_outside2.xml");
                        }
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int bytes_read = 0;
                        while ((bytes_read = inputStream.read(buffer)) != -1){
                            fileOutputStream.write(buffer,0,bytes_read);
                        }

                        inputStream.close();
                        fileOutputStream.close();

                        carDetector = new CascadeClassifier(file.getAbsolutePath());
                        cascadeDir.delete();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.onManagerConnected(status);
                    Toast toast1 = Toast.makeText(getApplicationContext(),
                            "Failed to load opencv!", Toast.LENGTH_LONG);
                    toast1.setGravity(Gravity.CENTER, 0, 0);
                    toast1.show();
                    break;
            }

        }
    };

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Surface surface = new Surface(surfaceTexture);
        try {
            mediaPlayer.setSurface(surface);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                String URL = "rtsp://" + ip + ":8555/unicast";
                Uri uri = Uri.parse(URL);

                mediaPlayer.setDataSource(getApplicationContext(),uri);
                mediaPlayer.prepareAsync();
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(1.0f));
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.start();
                        button4.setEnabled(true);
                        button5.setEnabled(true);
                        button6.setEnabled(true);
                        button7.setEnabled(true);
                        bSwitch.setEnabled(true);
                    }
                });

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Toast.makeText(getApplicationContext(),"Completed and get total " + data_in_time.size() + " data.\n",Toast.LENGTH_LONG).show();
                        button.setEnabled(true);
                        button2.setEnabled(true);
                        button3.setEnabled(true);
                        textView.append("Width: " + imageView_width + ",Height: " + imageView_height + "\n");
                        textView.append("Completed and get total " + data_in_time.size() + " data.");
                        textureView.setVisibility(View.INVISIBLE);
                        //textView.append("\nCompleted and get total " + data_in_time.size() + "data.");
                    }
                });
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        Bitmap bitmap = textureView.getBitmap();
        if (flag1){
            src = new Mat();
            gray = new Mat();
            gaussian_blur = new Mat();

            Utils.bitmapToMat(bitmap,src);

            Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY );

            Imgproc.GaussianBlur(gray,gaussian_blur,new Size(3,3),1,1);

            MatOfRect cars = new MatOfRect();

            if (module.equals("tunnel")){
                carDetector.detectMultiScale(gaussian_blur,cars,1.1,17,0,new Size(50,50),new Size());
            }else if (module.equals("outside")){
                carDetector.detectMultiScale(gaussian_blur,cars,1.1,51,0,new Size(90,90),new Size());
            }else {
                carDetector.detectMultiScale(gaussian_blur,cars,1.1,65,0,new Size(70,70),new Size());
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
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button || v.getId() == R.id.button7){
            imageView.setOnLongClickListener(this);
            lock = true;
        }
        switch (v.getId()){
            case R.id.button: {
                if (index == data_in_frame.size()){
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
                Intent analysis = new Intent(getApplicationContext(),Analysis_Activity.class);
                analysis.putStringArrayListExtra("data_in_time",data_in_time);
                analysis.putStringArrayListExtra("data_in_mat",data_in_mat);
                analysis.putIntegerArrayListExtra("data_in_position_x",data_in_position_x);
                analysis.putIntegerArrayListExtra("data_in_position_y",data_in_position_y);
                analysis.putExtra("video_number",module);
                startActivity(analysis);
                break;
            }
        }

    }

    @Override
    public boolean onLongClick(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(Main_Mount_Activity.this);
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.isChecked()){
            imageView3.setVisibility(View.VISIBLE);
        }else {
            imageView3.setVisibility(View.INVISIBLE);
        }
    }

    private CompoundButton.OnCheckedChangeListener start_detect = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.isChecked()){
                flag1 = true;
                button.setEnabled(false);
                button2.setEnabled(false);
                button3.setEnabled(false);
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

    private void show_image(int index){
        picture = new Mat();
        picture = data_in_frame.get(index).clone();

        Bitmap bitmap = Bitmap.createBitmap(data_in_frame.get(index).cols(),data_in_frame.get(index).rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(picture,bitmap);
        imageView.setImageBitmap(bitmap);

        textView2.setText(String.valueOf(index));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            switch (v.getId()){
                case R.id.button17: {
                    servo1_up_handler.post(camera_up_runnable);
                    break;
                }
                case R.id.button18: {
                    servo1_down_handler.post(camera_down_runnable);
                    break;
                }
                case R.id.button19: {
                    BackgroundTask action = new BackgroundTask();
                    action.execute(ip,port,"41");
                    break;
                }
                case R.id.button20: {
                    BackgroundTask action = new BackgroundTask();
                    action.execute(ip,port,"46");
                    break;
                }
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP){
            switch (v.getId()){
                case R.id.button17: {
                    servo1_up_handler.removeCallbacks(camera_up_runnable);
                    break;
                }
                case R.id.button18: {
                    servo1_down_handler.removeCallbacks(camera_down_runnable);
                    break;
                }
                case R.id.button19: {
                    BackgroundTask action = new BackgroundTask();
                    action.execute(ip,port,"42");
                    break;
                }
                case R.id.button20: {
                    BackgroundTask action = new BackgroundTask();
                    action.execute(ip,port,"47");
                    break;
                }
            }
        }
        return true;
    }

    private Runnable camera_up_runnable = new Runnable() {
        @Override
        public void run() {
            if (out_of_range_up){
                BackgroundTask action = new BackgroundTask();
                action.execute(ip,port,"31");
                servo1_up_handler.postDelayed(camera_up_runnable,25);
            }
        }
    };

    private Runnable camera_down_runnable = new Runnable() {
        @Override
        public void run() {
            if (out_of_range_down){
                BackgroundTask action = new BackgroundTask();
                action.execute(ip,port,"36");
                servo1_down_handler.postDelayed(camera_down_runnable,25);
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
                ss = new ServerSocket(9700);
                while (true){
                    socket = ss.accept();
                    dis = new DataInputStream(socket.getInputStream());
                    message = dis.read();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(getApplicationContext(),String.valueOf(message),Toast.LENGTH_SHORT).show();
                            switch (message){
                                case 32 : {
                                    try {
                                        out_of_range_up = false;
                                        Toast.makeText(getApplicationContext(),"Rolling camera up is out of range",Toast.LENGTH_SHORT).show();
                                        Thread.sleep(1000);
                                        out_of_range_up = true;
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                                case 37 : {
                                    try {
                                        out_of_range_down = false;
                                        Toast.makeText(getApplicationContext(),"Rolling camera down is out of range",Toast.LENGTH_SHORT).show();
                                        Thread.sleep(1000);
                                        out_of_range_down = true;
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    break;
                                }
                                /*case 16 : {
                                    flag3 = false;
                                    imageView3.setImageResource(R.drawable.green_light);
                                    break;
                                }*/
                            }
                        }
                    });
                    socket.close();
                    dis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}