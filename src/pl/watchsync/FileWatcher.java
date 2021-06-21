//https://docs.oracle.com/javase/tutorial/essential/io/notification.html
package pl.watchsync;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;


public class FileWatcher extends Thread{
    private WatchService watcher;
    private Path dir;
    private List<Shared> shared;
    private Transmiter transmiter;
    private TransmiterData tdata;
    FileWatcher(String path, ArrayList<Shared> sh, Transmiter snd, TransmiterData td){

        try {
            shared = sh;
            transmiter = snd;
            tdata = td;
            watcher = FileSystems.getDefault().newWatchService();
            dir = Path.of(path);
            WatchKey key = dir.register(watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

        }
        catch (IOException x) {
            System.err.println(x);
        }
    }

    public void triggered_action(String event, String name, String full_path){
        System.out.println("event: " + event +  " of file " + name + " of path: " + full_path);
        //if(!shared.isTo_update()){
            //shared.setPath(name);
            //shared.setTo_update(true);
            if (!event.equals("ENTRY_DELETE")) {

                File file = new File(full_path);
                boolean exists = file.exists();      // Check if the file exists
                boolean isDirectory = file.isDirectory(); // Check if it's a directory
                boolean isFile = file.isFile();      // Check if it's a regular file
                tdata.setEvent_type(event);
                if (isDirectory) tdata.setType("dir");
                if (isFile) tdata.setType("file");
                tdata.setPath(full_path);
                tdata.setFilename(name);

                try {
                    tdata.setSum(MD5Checksum.getMD5Checksum(full_path));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("file sum: " + tdata.getSum());
                transmiter.warpObject(tdata);

                //System.out.println(shared.isTo_update());
            }
            if (event.equals("ENTRY_DELETE")) {
                tdata.setFilename(name);
                tdata.setEvent_type(event);
                tdata.setPath(full_path);
                transmiter.warpObject(tdata);
            }
        //}
    }

    @Override
    public void run(){
        super.run();
        for (;;) {

            // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                System.out.println(x);
                return;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // This key is registered only
                // for ENTRY_CREATE events,
                // but an OVERFLOW event can
                // occur regardless if events
                // are lost or discarded.
                if (kind == OVERFLOW) {
                    continue;
                }

                // The filename is the
                // context of the event.
                WatchEvent<Path> ev = (WatchEvent<Path>)event;
                Path dir = (Path)key.watchable();
                Path fullPath = dir.resolve(ev.context());
                triggered_action(event.kind().toString(), ev.context().toString(), fullPath.toString());

                //Details left to reader....
            }

            // Reset the key -- this step is critical if you want to
            // receive further watch events.  If the key is no longer valid,
            // the directory is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }
}
