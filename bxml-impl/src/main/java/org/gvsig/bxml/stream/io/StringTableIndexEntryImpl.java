package org.gvsig.bxml.stream.io;

public class StringTableIndexEntryImpl implements StringTableIndexEntry {

    private long offset;

    private long stringCount;

    public StringTableIndexEntryImpl(long offset, long stringCount) {
        this.offset = offset;
        this.stringCount = stringCount;
    }

    public long getFileOffset() {
        return offset;
    }

    public long getStringCount() {
        return stringCount;
    }

}
