package UmbrellaPackage;

import java.io.*;
import java.net.*;

/**
 * @authors Team 19
 */

public class SocketSender {

    public static void sendEnvelope(Envelope e, String ipAddress) throws IOException {
        Socket socket = new Socket(ipAddress, 5002);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(e);
        outputStream.close();
        socket.close();
    }
}



/* intended socket implementation
    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;

    public static void main(String[] args) {
        try(Socket socket = new Socket("localhost",5000)) {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            sendFile("path/to/file1.pdf");
            sendFile("path/to/file2.pdf");
            
            dataInputStream.close();
            dataInputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void sendFile(String path) throws Exception{
        int bytes = 0;
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        
        // send file size
        dataOutputStream.writeLong(file.length());  
        // break file into chunks
        byte[] buffer = new byte[4*1024];
        while ((bytes=fileInputStream.read(buffer))!=-1){
            dataOutputStream.write(buffer,0,bytes);
            dataOutputStream.flush();
        }
        fileInputStream.close();
    }
    
    public static void sendEnvelope(Envelope e) {
        
    }
 */