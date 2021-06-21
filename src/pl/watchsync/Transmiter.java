package pl.watchsync;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


//this thread class manages all outbound connections
//check if list is not empty then transfers files and object to defined hosts
public class Transmiter extends Thread{
    private final List<String> hosts;
    private final int port;
    private final List<TransmiterData> tdl = new ArrayList<>();
    private final int syncdelay;
    Transmiter(List<String> h, int port, int syncdelay){
        this.hosts = h;
        this.port = port;
        this.syncdelay = syncdelay;
        start();

    }
    void warpObject(TransmiterData td){
        try{
            int flag = 0;
            if(!td.getEvent_type().equals("ENTRY_DELETE")){
                for (TransmiterData tdata: tdl){
                    if(tdata.getSum().equals(td.getSum())){
                        flag = 1;
                        break;
                    }
                }
            }
            if (flag == 0) tdl.add(td);
        }
        catch (Exception e){
            System.out.println("problem with adding item " + e);
        }

    }

    void sendObject(List<TransmiterData> tdl) throws IOException {
        for (String host: hosts) {
            System.out.println("Connecting to: " + host);

            for (TransmiterData item : tdl) {
                Connection conn = new Connection(host, port);
                conn.sendObject(item);
                if (!item.getEvent_type().equals("ENTRY_DELETE")) conn.sendFile(item.getPath());
                conn.stop();
            }
        }
    }

    @Override
    public void run(){
        super.run();
        Instant start = Instant.now();
        Instant end = Instant.now();
        Duration timeElapsed;

        while(true){
            timeElapsed = Duration.between(start, end);
            if(timeElapsed.toSeconds() > this.syncdelay + 5){
                start =  Instant.now();
                if(!tdl.isEmpty()){
                    try {
                        sendObject(tdl);
                        tdl.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            end =  Instant.now();
        }
    }
}

class Connection {
    private Socket connectionSocket;
    private BufferedWriter connectionOut;
    private BufferedReader connectionIn;
    private ObjectOutputStream oos;
    private BufferedOutputStream connectionBos;


    public Connection(String host, int port) {
        try {
            InetAddress address = InetAddress.getByName(host);
            connectionSocket = new Socket(host, port);
            oos = new ObjectOutputStream(connectionSocket.getOutputStream());
            connectionOut = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));
            connectionIn = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            connectionBos = new BufferedOutputStream(connectionSocket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendObject(TransmiterData tdata){
        try{
            System.out.println("sending message: " + tdata.getFilename());
            oos.writeObject(tdata);
            oos.flush();
        }
        catch (Exception e){
            System.out.println("sendObject " + e);
        }
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
        FileInputStream fis;
        try {

            fis = new FileInputStream(filepath);
            BufferedInputStream connectionBif = new BufferedInputStream(fis);

            int readLength;
            while ((readLength = connectionBif.read(bytearray)) > 0) {
                connectionBos.write(bytearray, 0, readLength);
            }
            connectionBif.close();
            fis.close();
            connectionBos.flush();


        }
        catch(Exception ex ){

            ex.printStackTrace();
        } //Excuse the poor exception handling...
    }

    public void stop(){
        try{
            System.out.println("Zamykanie połączenia");
            connectionOut.write("end");
            connectionOut.flush();
            connectionOut.close();
            connectionIn.close();
            connectionSocket.close();
            connectionBos.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Błąd podczas zamykania połączenia");
        }
    }
}