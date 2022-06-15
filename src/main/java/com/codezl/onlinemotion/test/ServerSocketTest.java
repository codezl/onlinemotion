package com.codezl.onlinemotion.test;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: code-zl
 * @Date: 2022/06/15/11:19
 * @Description:
 */
public class ServerSocketTest implements Runnable {
    private Socket ssocket;

    public ServerSocketTest(Socket socket) {
        this.ssocket = socket;
    }

    public static void main(String[] args) throws IOException {
        ServerSocket socket = new ServerSocket(1234);
        System.out.print("listening...");
        while (true) {
            Socket accept = socket.accept();
            System.out.print("connected...");
            new Thread(new ServerSocketTest(accept)).start();
        }
    }

    @Override
    public void run() {
        System.out.print("\nrun...\n");
        try {
            PrintStream printStream = new PrintStream(ssocket.getOutputStream());
            for (int i=100;i >= 0;i--) {
                printStream.print(i + "wall");
            }
            printStream.close();
            ssocket.close();
        } catch (IOException e) {
            System.out.print("\n错误\n");
            e.printStackTrace();
        }
    }
}
