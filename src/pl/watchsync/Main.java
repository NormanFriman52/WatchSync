package pl.watchsync;
import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {

        //collect properties from config file
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        Properties prop = new Properties(); // read properties
        String fileName = "watchsync.config";
        InputStream is = null;
        try {
            is = new FileInputStream(fileName);
        } catch (FileNotFoundException ex) {
            System.out.println("Config File not found, if not exists please create it first");
        }
        try {
            prop.load(is);
        } catch (IOException ex) {
            System.out.println("IO Error");
        }
        System.out.println("Working dir" + prop.getProperty("dir"));
        int port = Integer.parseInt(prop.getProperty("port"));
        String receivers = prop.getProperty("hosts_propagation");
        List<String> strings;
        strings = Arrays.asList(receivers.split("\\s*,\\s*"));
        int syncdelay = Integer.parseInt(prop.getProperty("syncdelay"));
        boolean allow_delete = Boolean.parseBoolean(prop.getProperty("allow_delete"));
        String working_dir = prop.getProperty("dir");
        String ip_range = prop.getProperty("hosts_allowed");

        //Threads Initialisation
        TransmiterData td = new TransmiterData();
        Transmiter snd = new Transmiter(strings,port, syncdelay);
        FileWatcher Fw = new FileWatcher(working_dir,  snd, td);
        FileManager fm = new FileManager(allow_delete, syncdelay);
        Fw.start();
        MultiServer Ms = new MultiServer(port, ip_range, fm);
        Ms.start();




    }
}