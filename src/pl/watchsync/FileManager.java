package pl.watchsync;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

//This class manages incoming files and directories
public class FileManager extends Thread{

    private String oldSum;
    //private TransmiterData td;
    private boolean allow_delete;
    private int syncdelay;
    private final List<TransmiterData> tdl = new ArrayList<>();

    FileManager(Boolean allow_delete, int syncdelay) {
        this.allow_delete = allow_delete;
        this.syncdelay = syncdelay;
        start();
    }

    @Override
    public void run(){
        super.run();
        Instant start = Instant.now();
        Instant end = Instant.now();
        Duration timeElapsed;

        while(true){
            timeElapsed = Duration.between(start, end);
            if(timeElapsed.toSeconds() > this.syncdelay){
                start =  Instant.now();
                if(!tdl.isEmpty()){
                    try {
                        for(TransmiterData td: tdl){
                            td.getEvent_type();
                            if (td.getEvent_type().equals("ENTRY_CREATE")){
                                createEntry(td);
                            }
                            if (td.getEvent_type().equals("ENTRY_MODIFY")){
                                modifyEntry(td);
                            }
                            if (td.getEvent_type().equals("ENTRY_DELETE") & this.allow_delete & td.getType().equals("file")){
                                deleteFile(td.getPath(), td.getFilename());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            end =  Instant.now();
        }
    }

    private void createEntry(TransmiterData td) {
        try{
            File file = new File(td.getPath());// Check if the file exists
            boolean exists = file.exists();
            if(!exists){
                Files.move(Paths.get(td.getTempPath()), Paths.get(td.getPath()), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (Exception e){
            System.out.println("error, file already exists:" + e);
        }
    }

    private void modifyEntry(TransmiterData td) {
        try{
            if (!checkIgnore(td.getPath(),td.getFilename())) {
                File file = new File(td.getPath());// Check if the file exists
                boolean exists = file.exists();
                if (exists) {
                    String sum1 = MD5Checksum.getMD5Checksum(td.getPath());
                    String sum2 = MD5Checksum.getMD5Checksum(td.getTempPath());
                    if (!sum1.equals(sum2))
                        Files.move(Paths.get(td.getTempPath()), Paths.get(td.getPath()), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        catch (Exception e){
            System.out.println("error moving files:" + e);
        }
    }

    public void addObject(TransmiterData td){
        tdl.add(td);
    }

    public boolean checkIgnore(String path, String filename) {
        String pathWithoutFile = path.substring(0, path.lastIndexOf("/") - 1);
        File tempFile = new File(pathWithoutFile + "/.watchsync");
        boolean exists = tempFile.exists();
        if (exists) {
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(pathWithoutFile + "/.watchsync"));
                String line = reader.readLine();
                while (line != null) {
                    line = reader.readLine();
                    if (line.contains(filename)){
                        reader.close();
                        return true;
                    }
                }
                reader.close();
                return false;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void deleteFile(String path, String filename) {
        if (!checkIgnore(path,filename)){
            File file = new File(path);

            if(file.delete())
            {
                System.out.println("File deleted successfully");
            }
            else
            {
                System.out.println("Failed to delete the file");
            }
        }
    }
    public void deleteDir(String path){

    }
}
