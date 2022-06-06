package cbs;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Drone_Mount_2 {
    private static Runtime input = Runtime.getRuntime();
    private static Runtime status = Runtime.getRuntime();
    private static Runtime work = Runtime.getRuntime();
    private static Runtime alert = Runtime.getRuntime();
    private static String ans = "";
    private static String network_device = "";
    private static String device_check = "";
    private static String wifi_check = "";
    private static String network_check = "";
    private static boolean internet_check = true;
    private static String vpn_check = "";
    private static boolean VPN_check = false;

    private static ServerSocket serverSocket;
    private static Socket socket;
    private static BufferedReader bufferedReader;
    private static InputStreamReader inputStreamReader;
    private static int command;
    protected static boolean type;

    protected static Runtime camera_servo1 = Runtime.getRuntime();
    protected static Runtime camera_servo2 = Runtime.getRuntime();
    private static int servo1_max_angle = 670;
    private static int servo1_minimum_angle = 370;
    private static int servo1_center_angle = 520;
    private static String servo2_right_angle = "380";
    private static String servo2_left_angle = "460";
    private static String servo2_stop_angle = "420";
    public static void main(String[] arg) {
        try {
            work.exec("gpio mode 3 out");
            alert.exec("gpio mode 2 out");

            status.exec("gpio mode 4 out");
            work.exec("gpio write 4 1");
            Runtime.getRuntime().exec("sudo pon 4GLTE");

            input.exec("gpio mode 0 in");
            Process process = input.exec("gpio read 0");
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            LineNumberReader lineNumberReader = new LineNumberReader(inputStreamReader);
            ans = lineNumberReader.readLine();
            System.out.println(ans);

            Process process2 = Runtime.getRuntime().exec ("ifconfig wwan0");
            InputStreamReader inputStreamReader2 = new InputStreamReader(process2.getInputStream());
            LineNumberReader lineNumberReader2 = new LineNumberReader (inputStreamReader2);
            while ((network_device = lineNumberReader2.readLine ()) != null){
                device_check = device_check + network_device;
                System.out.println(network_device);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(device_check.equals("")) {
            try {
                alert.exec("gpio write 2 1");
                work.exec("gpio write 4 0");
                Runtime.getRuntime().exec("exit");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            try {
                Runtime.getRuntime().exec("sudo pon 4GLTE");
                System.out.println("Network connecting...");

                do {
                    try{
                        Process process3 = Runtime.getRuntime().exec ("ifconfig ppp0");
                        InputStreamReader inputStreamReader3 = new InputStreamReader(process3.getInputStream());
                        LineNumberReader lineNumberReader3 = new LineNumberReader (inputStreamReader3);
                        while ((network_device = lineNumberReader3.readLine ()) != null){
                            network_check = network_check + network_device;
                            System.out.println(network_device);
                        }

                        if (network_check.equals("")){
                            internet_check = true;
                        }else {
                            internet_check = false;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }while (internet_check);

                System.out.println("Internet is connected");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("VPN connecting...");

        try {
            do {
                Thread.sleep(3000);
                Process process5 = Runtime.getRuntime().exec ("sudo ifconfig tun0");
                InputStreamReader inputStreamReader5 = new InputStreamReader(process5.getInputStream());
                LineNumberReader lineNumberReader5 = new LineNumberReader (inputStreamReader5);
                while ((network_device = lineNumberReader5.readLine ()) != null){
                    vpn_check = vpn_check + network_device;
                    System.out.println(network_device);
                }
                if(vpn_check.equals("")) {
                    VPN_check = true;
                }else {
                    VPN_check = false;
                }
            }while(VPN_check);

            System.out.println("VPN is Connected");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        if (ans.equals("1")){
            System.out.println("Wi-Fi Mod");
            try{
                Runtime.getRuntime().exec("ifconfig tun0 down");
                //Runtime.getRuntime().exec("sudo systemctl disable openvpn");

                Process process4 = Runtime.getRuntime().exec("ifconfig wlan0");
                InputStreamReader inputStreamReader4 = new InputStreamReader(process4.getInputStream());
                LineNumberReader lineNumberReader4 = new LineNumberReader(inputStreamReader4);
                while ((network_device = lineNumberReader4.readLine ()) != null){
                    wifi_check = wifi_check + network_device;
                    System.out.println(network_device);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            if (wifi_check.contains("inet")){
                System.out.println("Wi-Fi AP is on-line");
                type = true;

                try {
                    work.exec("gpio write 4 0");
                    status.exec("gpio write 3 1");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                System.out.println("Wi-Fi AP is unavailable");

                try {
                    alert.exec("gpio write 2 1");
                    work.exec("gpio write 4 0");
                    Runtime.getRuntime().exec("exit");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            System.out.println("4G LTE Mod");
            type = false;
            try {
                //Runtime.getRuntime().exec("sudo systemctl enable openvpn");
                Runtime.getRuntime().exec("ifconfig tun0 up");

                do {
                    Thread.sleep(3000);
                    Process process5 = Runtime.getRuntime().exec ("sudo ifconfig tun0");
                    InputStreamReader inputStreamReader5 = new InputStreamReader(process5.getInputStream());
                    LineNumberReader lineNumberReader5 = new LineNumberReader (inputStreamReader5);
                    while ((network_device = lineNumberReader5.readLine ()) != null){
                        vpn_check = vpn_check + network_device;
                        System.out.println(network_device);
                    }
                    if(vpn_check.equals("")) {
                        VPN_check = true;
                    }else {
                        VPN_check = false;
                    }
                }while(VPN_check);

                System.out.println("VPN is Connected");
                work.exec("gpio write 4 0");
                status.exec("gpio write 3 1");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Starting RTSP server...");
        try {
            Thread.sleep(1000);
            Runtime.getRuntime().exec("lxterminal -e sudo sh /home/pi/Traffic_Analysis/rtsp.sh");
            Thread.sleep(1000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Loading Base program...");
        try {
            Thread.sleep(1000);
            Runtime.getRuntime().exec("lxterminal -e sudo java /home/pi/Traffic_Analysis/Base.java");
            Thread.sleep(1000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Loading Main program...");

        try {
            camera_servo1.exec("gpio mode 23 pwm");
            camera_servo1.exec("gpio pwm-ms");
            camera_servo1.exec("gpio pwmc 192");
            camera_servo1.exec("gpio pwmr 2000");

            camera_servo2.exec("gpio mode 1 pwm");
            camera_servo2.exec("gpio pwm-ms");
            camera_servo2.exec("gpio pwmc 192");
            camera_servo2.exec("gpio pwmr 2000");
        } catch (IOException e) {
            e.printStackTrace();
        }

        onWorking onWorking = new onWorking();
        Thread thread_status = new Thread(onWorking);
        thread_status.start();

        int servo1_angle = servo1_minimum_angle;

        while(true) {
            try {
                System.out.println("Waiting command");
                serverSocket = new ServerSocket(6000);
                socket = serverSocket.accept();

                inputStreamReader = new InputStreamReader(socket.getInputStream());
                bufferedReader = new BufferedReader(inputStreamReader);
                command = bufferedReader.read();

                System.out.println("message received from client: " + command);

                inputStreamReader.close();
                bufferedReader.close();
                serverSocket.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            switch (command){
                case 11: {
                    client(12,9500);
                    break;
                }
                case 13: {
                    servo1_angle = servo1_initialization();
                    break;
                }
                case 15: {
                    servo2_initialization();
                    break;
                }
                case 31: {//camera up
                    if (servo1_angle <= servo1_max_angle){
                        try {
                            servo1_angle = servo1_angle + 1;
                            camera_servo1.exec("gpio pwm 23 " + servo1_angle);
                            System.out.println("Servo 1 : " + servo1_angle);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else {
                        System.out.println("Servo 1 is out of range");
                        client(32,9700);
                    }
                    break;
                }
                case 36: {//camera down
                    if (servo1_angle >= servo1_minimum_angle){
                        try {
                            servo1_angle = servo1_angle - 1;
                            camera_servo1.exec("gpio pwm 23 " + servo1_angle);
                            System.out.println("Servo 1 : " + servo1_angle);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else {
                        System.out.println("Servo 1 is out of range");
                        client(37,9700);
                    }
                    break;
                }
                case 41: {//camera right
                    try {
                        camera_servo2.exec("gpio pwm 1 " + servo2_right_angle);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case 42: {//camera stop
                    try {
                        camera_servo2.exec("gpio pwm 1 " + servo2_stop_angle);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case 46: {//camera left
                    try {
                        camera_servo2.exec("gpio pwm 1 " + servo2_left_angle);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case 47: {//camera stop
                    try {
                        camera_servo2.exec("gpio pwm 1 " + servo2_stop_angle);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                default: {
                    System.out.println("Unknown command");
                }
            }
        }
    }

    private static int servo1_initialization(){
        try {
            for (int i = servo1_minimum_angle;i<servo1_max_angle;i=i+2){
                camera_servo1.exec("gpio pwm 23 " + i);
                System.out.println(i);
                Thread.sleep(25);
            }

            for (int i = servo1_max_angle;i>servo1_minimum_angle;i=i-2){
                camera_servo1.exec("gpio pwm 23 " + i);
                System.out.println(i);
                Thread.sleep(25);
            }

            /*for (int i = servo1_minimum_angle;i<servo1_center_angle;i=i+2){
                camera_servo1.exec("gpio pwm 23 " + i);
                System.out.println(i);
                Thread.sleep(25);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("servo1 is initialization");
        client(14,9500);

        return servo1_minimum_angle;
    }

    private static void  servo2_initialization(){
        try {
            camera_servo2.exec("gpio pwm 1 " + servo2_right_angle);
            Thread.sleep(1000);

            camera_servo2.exec("gpio pwm 1 " + servo2_stop_angle);
            Thread.sleep(1000);

            camera_servo2.exec("gpio pwm 1 " + servo2_left_angle);
            Thread.sleep(1000);

            camera_servo2.exec("gpio pwm 1 " + servo2_stop_angle);
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("servo2 is initialization");
        client(16,9500);
    }

    private static void client(int message,int port){
        Socket client_socket;
        DataOutputStream dos;
        try {
            if (type){
                client_socket = new Socket("192.168.4.8",port);
            }else {
                client_socket = new Socket("10.8.0.2",port);
            }

            dos = new DataOutputStream(client_socket.getOutputStream());
            dos.write(message);

            dos.close();
            client_socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class onWorking implements Runnable{
    private static Runtime status = Runtime.getRuntime();
    @Override
    public void run() {
        try {
            status.exec("gpio mode 3 out");

            while (true){
                status.exec("gpio write 3 1");

                Thread.sleep(1000);

                status.exec("gpio write 3 0");

                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
