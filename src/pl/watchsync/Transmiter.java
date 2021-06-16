package pl.watchsync;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.util.List;

public class Transmiter {
    private List<String> hosts;
    private int port;
    Transmiter(List<String> h, int port){
        this.hosts = h;
        this.port = port;

    }
    void sendObject(TransmiterData td) throws IOException {
        for (String host: hosts) {
            System.out.println("Connecting to: " + host);
            Connection conn = new Connection(host, port);
            conn.sendObject(td);
            conn.sendFile(td.getPath());
            conn.stop();
        }
    }
}

class Connection {
    private Socket connectionSocket;
    //private PrintWriter connectionOut;
    private BufferedWriter connectionOut;
    private BufferedReader connectionIn;
    private ObjectOutputStream oos;
    private BufferedInputStream connectionBif;
    private BufferedOutputStream connectionBos;


    public Connection(String host, int port) {
        try {
            InetAddress address = InetAddress.getByName(host);
            connectionSocket = new Socket(host, port);
            //connectionOut = new PrintWriter(connectionSocket.getOutputStream(), true);
            oos = new ObjectOutputStream(connectionSocket.getOutputStream());
            connectionOut = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));
            connectionIn = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            //connectionBif = new BufferedInputStream(connectionSocket.getInputStream());
            connectionBos = new BufferedOutputStream(connectionSocket.getOutputStream());

            //BufferedReader connectionIn  = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            //BufferedWriter connectionOut = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendObject(TransmiterData tdata) throws IOException {
        System.out.println("sending message: " + tdata.getFilename());
        String resp = null;
        oos.writeObject(tdata);
        oos.flush();
//        connectionOut.write(td.getFilename());
//        connectionOut.newLine();
//        connectionOut.flush();
//        connectionOut.write("exit");
//        //connectionOut.newLine();

        //connectionOut.flush();
        //System.out.println("Transmiter received: " + connectionIn.readLine());
    }

    public void sendFile(String filepath){
        byte[] bytearray = new byte[1024];
        FileInputStream fis = null;
        try {

            fis = new FileInputStream(filepath);
//            OutputStream output= socket.getOututStream();
             connectionBif = new BufferedInputStream(fis);

            int readLength = -1;
            while ((readLength = connectionBif.read(bytearray)) > 0) {
                connectionBos.write(bytearray, 0, readLength);

            }
            connectionBif.close();
            connectionBos.close();
        }
        catch(Exception ex ){

            ex.printStackTrace();
        } //Excuse the poor exception handling...
    }

    public void stop(){
        try{
            System.out.println("Zamykanie połączenia");
            connectionOut.close();
            connectionIn.close();
            connectionSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Błąd podczas zamykania połączenia");
        }
    }
}