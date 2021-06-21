package pl.watchsync;
import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
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

        List<Shared> sh = new ArrayList<>();
        List<String> strings = new ArrayList<>();
        strings.add(prop.getProperty("hosts_propagation"));
        TransmiterData td = new TransmiterData();
        int syncdelay = Integer.parseInt(prop.getProperty("syncdelay"));
        Transmiter snd = new Transmiter(strings,port, syncdelay);

        boolean allow_delete = Boolean.parseBoolean(prop.getProperty("allow_delete"));


        FileWatcher Fw = new FileWatcher(prop.getProperty("dir"), (ArrayList<Shared>) sh, snd, td);

        FileManager fl = new FileManager(td, allow_delete);

        Fw.start();

        String ip_range = prop.getProperty("hosts_allowed");

        MultiServer Ms = new MultiServer(port, (ArrayList<Shared>) sh, ip_range, fl);

        Ms.start();




    }
}