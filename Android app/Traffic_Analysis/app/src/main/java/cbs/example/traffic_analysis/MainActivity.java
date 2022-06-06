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
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
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
import org.opencv.core.Core;
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

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, MediaPlayer.OnVideoSizeChangedListener, View.OnClickListener , CompoundButton.OnCheckedChangeListener {
    private MediaPlayer mediaPlayer;
    private TextureView textureView;
    private ImageView imageView,imageView2,imageView3;
    private TextView textView,textView2,textView3;
    private Button button,button2,button3;
    private EditText editText;
    private Switch aSwitch;
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
    private String video_number;
    private InputStream inputStream;
    private File cascadeDir;
    private File file;

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
        editText = (EditText) findViewById(R.id.editText);
        aSwitch = (Switch) findViewById(R.id.switch1);

        textureView.setSurfaceTextureListener(this);
        aSwitch.setOnCheckedChangeListener(this);

        mediaPlayer = new MediaPlayer();

        video_number = getIntent().getStringExtra("video_number");
        System.out.println("test");

        try {
            if (video_number.equals("tunnel01") || video_number.equals("tunnel02")){
                detect_boundary_min = 400;
                detect_boundary_max = 948;
                if (video_number.equals("tunnel01")){
                    assetFileDescriptor = getAssets().openFd("tunnel01.mp4");
                }else {
                    assetFileDescriptor = getAssets().openFd("tunnel02.mp4");
                }
            }else if (video_number.equals("outside01") || video_number.equals("outside02")){
                detect_boundary_min = 400;
                detect_boundary_max = 948;
                if (video_number.equals("outside01")){
                    assetFileDescriptor = getAssets().openFd("outside01.mp4");
                }else {
                    assetFileDescriptor = getAssets().openFd("outside02.mp4");
                }
            }else if (video_number.equals("outside03")){
                detect_boundary_min = 500;
                detect_boundary_max = 948;

                assetFileDescriptor = getAssets().openFd("outside04.mp4");
            }else{
                detect_boundary_min = 400;
                detect_boundary_max = 948;
                assetFileDescriptor = getAssets().openFd("failed01.mp4");
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        imageView3.setVisibility(View.INVISIBLE);
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

                    if (video_number.equals("tunnel01") || video_number.equals("tunnel02")){
                        canvas.drawLine(0, imageView_height-100,imageView_width, imageView_height-100,paint);
                        canvas.drawLine(0, 400,imageView_width, 400,paint);
                        canvas.drawLine(1150,400,950,imageView_height-100,paint);
                    }else if (video_number.equals("outside01") || video_number.equals("outside02")){
                        canvas.drawLine(0, imageView_height-100,imageView_width, imageView_height-100,paint);
                        canvas.drawLine(0, 400,imageView_width, 400,paint);
                        canvas.drawLine(1300,400,400,imageView_height-100,paint);
                        canvas.drawLine(1150,400,0,imageView_height-100,paint);
                        //detect_boundary_max = imageView_height;
                    }else if (video_number.equals("outside03")){
                        canvas.drawLine(0, imageView_height-100,imageView_width, imageView_height-100,paint);
                        canvas.drawLine(0, 500,imageView_width, 500,paint);
                        canvas.drawLine(1950,500,1115,imageView_height-100,paint);
                        canvas.drawLine(1800,500,690,imageView_height-100,paint);
                        //detect_boundary_max = imageView_height;
                    }
                    else {
                        canvas.drawLine(0, imageView_height-100,imageView_width, imageView_height-100,paint);
                        canvas.drawLine(0, 400,imageView_width, 400,paint);
                        //detect_boundary_max = imageView_height;
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
                            "成功加载opencv！", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                    try {
                        if (video_number.equals("tunnel01") || video_number.equals("tunnel02")){
                            inputStream = getResources().openRawResource(R.raw.car_tunnel);

                            cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                            file = new File(cascadeDir.getAbsoluteFile(),"car_tunnel.xml");
                        }else if (video_number.equals("outside01") || video_number.equals("outside02")){
                            inputStream = getResources().openRawResource(R.raw.car_outside3);

                            cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                            file = new File(cascadeDir.getAbsoluteFile(),"car_outside3.xml");
                        }else if (video_number.equals("outside03")){
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
                            "加载失败！", Toast.LENGTH_LONG);
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
                mediaPlayer.setDataSource(assetFileDescriptor);
                mediaPlayer.prepareAsync();
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(1.0f));
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.start();
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
        src = new Mat();
        gray = new Mat();
        gaussian_blur = new Mat();

        Bitmap bitmap = textureView.getBitmap();
        Utils.bitmapToMat(bitmap,src);

        Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY );

        Imgproc.GaussianBlur(gray,gaussian_blur,new Size(3,3),1,1);
        //Imgproc.blur(gray,gaussian_blur,new Size(3,3),new Point(0,0), Core.BORDER_DEFAULT);

        MatOfRect cars = new MatOfRect();
        if (video_number.equals("tunnel01") || video_number.equals("tunnel02")){
            carDetector.detectMultiScale(gaussian_blur,cars,1.1,17,0,new Size(50,50),new Size());
        }else if (video_number.equals("outside01") || video_number.equals("outside02")){
            carDetector.detectMultiScale(gaussian_blur,cars,1.1,19,0,new Size(70,70),new Size());
        }else if (video_number.equals("outside03")){
            carDetector.detectMultiScale(gaussian_blur,cars,1.1,70,0,new Size(90,90),new Size());
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
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button || v.getId() == R.id.button7){
            imageView.setOnLongClickListener(delete_image);
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
                analysis.putExtra("video_number",video_number);
                startActivity(analysis);
                break;
            }
        }
    }

    private View.OnLongClickListener delete_image = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
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

    private void show_image(int index){
        picture = new Mat();
        picture = data_in_frame.get(index).clone();

        Bitmap bitmap = Bitmap.createBitmap(data_in_frame.get(index).cols(),data_in_frame.get(index).rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(picture,bitmap);
        imageView.setImageBitmap(bitmap);

        textView2.setText(String.valueOf(index));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.isChecked()){
            imageView3.setVisibility(View.VISIBLE);
        }else {
            imageView3.setVisibility(View.INVISIBLE);
        }
    }
}
