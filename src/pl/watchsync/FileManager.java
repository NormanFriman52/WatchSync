package pl.watchsync;

import java.io.*;

public class FileManager {

    private String oldSum;
    private TransmiterData td;

    FileManager(TransmiterData tdata) {
        td = tdata;
    }

    public void ControlSum() {

    }

    public void CreateNewFile() {

    }

    public boolean checkIgnore(String path, String filename) {
        String pathWithoutFile = path.substring(0, path.lastIndexOf("/") - 1);
        File tempFile = new File(pathWithoutFile + "/.watchsync");
        boolean exists = tempFile.exists();
        if (exists) {
            System.out.println(exists);
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

    public void writeSumData() {

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
