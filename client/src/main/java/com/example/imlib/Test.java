package com.example.imlib;

import com.example.imlib.utils.L;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Test {
    public static void main(String[] args) {

        Socket socket= null;
        try {
            socket = new Socket("127.0.0.1",8099);
            //通过客户端的套接字对象Socket方法，获取字节输出流，将数据写向服务器
            OutputStream out=socket.getOutputStream();
            out.write("服务器你好！".getBytes());

            //读取服务器发回的数据，使用socket套接字对象中的字节输入流
            InputStream in=socket.getInputStream();
            byte[] data=new byte[1024];
            int len=in.read(data);
            System.out.println(new String(data,0,5));

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
