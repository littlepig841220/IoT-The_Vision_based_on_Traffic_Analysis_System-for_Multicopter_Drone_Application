package cbs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Base {
    protected static ServerSocket serverSocket;
    protected static Socket socket;
    protected static BufferedReader bufferedReader;
    protected static InputStreamReader inputStreamReader;
    protected static int command;
    public static void main(String[] arg) {
        System.out.println("Base program is started...");

        while(true) {
            try {
                System.out.println("Waiting command");
                serverSocket = new ServerSocket(5000);
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
                case 61: {
                    try {
                        Thread.sleep(5000);
                        Runtime.getRuntime().exec("sudo reboot");
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                    //System.exit(0);
                    break;
                }
                case 62: {
                    try {
                        Thread.sleep(5000);
                        Runtime.getRuntime().exec("sudo shutdown now");
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                    //System.exit(0);
                    break;
                }
                default: {
                    System.out.println("Unknown command");
                }
            }
        }
    }
}
