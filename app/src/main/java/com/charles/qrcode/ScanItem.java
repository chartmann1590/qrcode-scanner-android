package com.charles.qrcode;

public class ScanItem {
    private int id;
    private String content;
    private String format;
    private long timestamp;

    public ScanItem(int id, String content, String format, long timestamp) {
        this.id = id;
        this.content = content;
        this.format = format;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getFormat() {
        return format;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
