package pl.watchsync;
import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        Properties prop = new Properties();
        String fileName = "watchsync.config";
        InputStream is = null;
        try {
            is = new FileInputStream(fileName);
        } catch (FileNotFoundException ex) {
            System.out.println("File not found");
        }
        try {
            prop.load(is);
        } catch (IOException ex) {
            System.out.println("IO Error");
        }
        System.out.println("Working dir" + prop.getProperty("dir"));
        int port = Integer.parseInt(prop.getProperty("port"));

        Shared sh = new Shared();
        List<String> strings = new ArrayList<String>();
        strings.add(prop.getProperty("hosts_propagation"));
        TransmiterData td = new TransmiterData();
        Transmiter snd = new Transmiter(strings,port);


        FileWatcher Fw = new FileWatcher(prop.getProperty("dir"), sh, snd, td);

        Fw.start();

        String ip_range = prop.getProperty("hosts_allowed");

        MultiServer Ms = new MultiServer(port, sh, ip_range);

        Ms.start();




    }
}