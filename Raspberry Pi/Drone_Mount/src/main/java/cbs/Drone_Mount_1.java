package cbs;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Drone_Mount_1 {
    private static Runtime status = Runtime.getRuntime();
    private static String network_device;
    private static String device_check = "";
    private static String network_check = "";
    private static boolean internet_check = true;
    private static String vpn_check = "";
    private static boolean VPN_check = true;
    private static String wifi_check = "";

    private static ServerSocket serverSocket;
    private static Socket socket;
    private static BufferedReader bufferedReader;
    private static InputStreamReader inputStreamReader;
    private static int command;
    protected static boolean type;

    protected static Runtime camera_servo1 = Runtime.getRuntime();
    private static int servo1_max_angle = 700;
    private static int servo1_minimum_angle = 90;
    private static int servo1_center_angle = 395;

    public static void main(String[] arg) {
        try{
            status.exec("gpio mode 0 out");
            status.exec("gpio write 0 1");

            Process process = Runtime.getRuntime().exec ("ifconfig wwan0");
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            LineNumberReader lineNumberReader = new LineNumberReader (inputStreamReader);
            while ((network_device = lineNumberReader.readLine ()) != null){
                device_check = device_check + network_device;
                System.out.println(network_device);
            }
        }
        catch (java.io.IOException e){
            System.err.println ("IOException " + e.getMessage());
        }

        if(device_check.equals("")) {
            System.out.println("No device found\nUse Wi-Fi for communication");
            try {
                Runtime.getRuntime().exec("ifconfig tun0 down");

                Process process_4 = Runtime.getRuntime().exec("ifconfig wlan0");
                InputStreamReader inputStreamReader_4 = new InputStreamReader(process_4.getInputStream());
                LineNumberReader lineNumberReader_4 = new LineNumberReader(inputStreamReader_4);
                while ((network_device = lineNumberReader_4.readLine ()) != null){
                    wifi_check = wifi_check + network_device;
                    System.out.println(network_device);
                }

                if (wifi_check.contains("inet")){
                    System.out.println("Wi-Fi is connect");
                    type = false;
                }else {
                    System.out.println("Wi-Fi AP is unavailable");

                    try {
                        for (int i = 5;i > 0;i--){
                            System.out.println("System will reboot in " + i);
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Runtime.getRuntime().exec("reboot");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("Device found\nUse mobile net for communication");

            try {
                Runtime.getRuntime().exec("sudo ifconfig wlan0 down");
                Runtime.getRuntime().exec("sudo pon 4GLTE");

                System.out.println("Network connecting...");
            } catch (IOException e) {
                e.printStackTrace();
            }

            do {
                try{
                    Process process_2 = Runtime.getRuntime().exec ("ifconfig ppp0");
                    InputStreamReader inputStreamReader_2 = new InputStreamReader(process_2.getInputStream());
                    LineNumberReader lineNumberReader_2 = new LineNumberReader (inputStreamReader_2);
                    while ((network_device = lineNumberReader_2.readLine ()) != null){
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

            System.out.println("Internet is connected\nVPN connecting...");

            do {
                try{
                    Thread.sleep(3000);
                    Process process_3 = Runtime.getRuntime().exec ("ifconfig tun0");
                    InputStreamReader inputStreamReader_3 = new InputStreamReader(process_3.getInputStream());
                    LineNumberReader lineNumberReader_3 = new LineNumberReader (inputStreamReader_3);
                    while ((network_device = lineNumberReader_3.readLine ()) != null){
                        vpn_check = vpn_check + network_device;
                        System.out.println(network_device);
                    }
                    if(vpn_check.equals("")) {
                        VPN_check = true;
                    }else {
                        VPN_check = false;
                    }
                }
                catch (IOException | InterruptedException e){
                    System.err.println ("IOException " + e.getMessage());

                }
            }while(VPN_check);

            System.out.println("VPN is Connected");
            type = true;
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

        status status = new status();
        Thread thread_status = new Thread(status);
        thread_status.start();

        try {
            camera_servo1.exec("gpio mode 1 pwm");
            camera_servo1.exec("gpio pwm-ms");
            camera_servo1.exec("gpio pwmc 192");
            camera_servo1.exec("gpio pwmr 2000");
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                    client(12);
                    break;
                }
                case 13: {
                    //initialization
                    servo1_angle = servo1_initialization();
                    break;
                }
                case 19: {
                    network_check(12);
                    break;
                }
                case 31: {//camera up
                    if (servo1_angle <= servo1_max_angle){
                        try {
                            servo1_angle = servo1_angle + 1;
                            camera_servo1.exec("gpio pwm 1 " + servo1_angle);
                            System.out.println("Servo 1 : " + servo1_angle);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else {
                        System.out.println("Servo 1 is out of range");
                        client(32);
                    }
                    break;
                }
                case 36: {//camera down
                    if (servo1_angle >= servo1_minimum_angle){
                        try {
                            servo1_angle = servo1_angle - 1;
                            camera_servo1.exec("gpio pwm 1 " + servo1_angle);
                            System.out.println("Servo 1 : " + servo1_angle);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else {
                        System.out.println("Servo 1 is out of range");
                        client(37);
                    }
                    break;
                }
                case 41: {
                    //camera left
                    break;
                }
                case 46: {
                    //camera right
                    break;
                }
                default: {
                    System.out.println("Unknown command");
                }
            }
        }

    }

    private static void client(int message){
        Socket client_socket;
        DataOutputStream dos;
        try {
            if (type){
                client_socket = new Socket("10.8.0.2",9700);
            }else {
                client_socket = new Socket("192.168.2.102",9700);
            }

            dos = new DataOutputStream(client_socket.getOutputStream());
            dos.write(message);

            dos.close();
            client_socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void network_check(int message){
        Socket client_socket;
        DataOutputStream dos;
        try {
            if (type){
                client_socket = new Socket("10.8.0.2",9600);
            }else {
                client_socket = new Socket("192.168.2.102",9600);
            }

            dos = new DataOutputStream(client_socket.getOutputStream());
            dos.write(message);

            dos.close();
            client_socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int servo1_initialization(){
        try {
            for (int i = servo1_minimum_angle;i<servo1_max_angle;i=i+2){
                camera_servo1.exec("gpio pwm 1 " + i);
                System.out.println(i);
                Thread.sleep(25);
            }

            for (int i = servo1_max_angle;i>servo1_minimum_angle;i=i-2){
                camera_servo1.exec("gpio pwm 1 " + i);
                System.out.println(i);
                Thread.sleep(25);
            }

            for (int i = servo1_minimum_angle;i<servo1_center_angle;i=i+2){
                camera_servo1.exec("gpio pwm 1 " + i);
                System.out.println(i);
                Thread.sleep(25);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("servo1 is initialization");
        client(14);

        return servo1_center_angle;
    }
}

class status implements Runnable{
    private static Runtime status = Runtime.getRuntime();
    @Override
    public void run() {
        try {
            status.exec("gpio mode 0 out");

            while (true){
                status.exec("gpio write 0 1");

                Thread.sleep(1000);

                status.exec("gpio write 0 0");

                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
