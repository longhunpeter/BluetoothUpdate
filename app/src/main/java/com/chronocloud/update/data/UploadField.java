package com.chronocloud.update.data;

/**
 * Created by lxl on 14-3-12.
 */
public enum UploadField implements Field {
    _id("INTEGER NOT NULL primary key"),
    dimensionalCode("TEXT"),
    LocalModifyTime("INTEGER");
    private String type;

    private UploadField() {
        this("TEXT");
    }

    private UploadField(String type) {
        this.type = type;
    }

    @Override
    public int order() {
        return this.ordinal();
    }

    @Override
    public String type() {
        return type;
    }
}
