package pl.watchsync;

import java.nio.file.Path;

public class Shared {
    private Path path;
    private boolean to_update = false;

    Shared() {

    }

    public Path getPath() {
        return path;
    }
    public void setPath(Path path) {
        this.path = path;
    }
    public boolean isTo_update() {
        return to_update;
    }

    public void setTo_update(boolean to_update) {
        this.to_update = to_update;
    }


}
