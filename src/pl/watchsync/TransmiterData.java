package pl.watchsync;

import java.io.Serializable;


//this is shared class
//defines all transferred file data and allows and contains event types
public class TransmiterData implements Serializable {
    private String path;
    private String filename;
    private String event_type;
    private String type;
    private String sum;
    private String tempPath;

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }
//    private String oldSum;

    TransmiterData() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;

    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }
}
