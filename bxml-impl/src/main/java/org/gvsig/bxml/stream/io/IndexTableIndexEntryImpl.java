package org.gvsig.bxml.stream.io;

public class IndexTableIndexEntryImpl implements IndexTableIndexEntry {

    private String xpathExpression;

    private long offset;

    public IndexTableIndexEntryImpl(final String xpathExpression, final long offset) {
        this.xpathExpression = xpathExpression;
        this.offset = offset;
    }

    public long getFileOffset() {
        return offset;
    }

    public String getXpathExpression() {
        return xpathExpression;
    }

}
