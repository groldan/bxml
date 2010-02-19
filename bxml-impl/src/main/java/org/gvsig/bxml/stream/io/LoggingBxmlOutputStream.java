package org.gvsig.bxml.stream.io;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class LoggingBxmlOutputStream extends BxmlOutputStreamWrapper {

    private final Writer writer;

    public LoggingBxmlOutputStream(final BxmlOutputStream wrapped, final Writer writer) {
        super(wrapped);
        this.writer = writer;
    }

    private void log(String event, Object o) {
        try {
            writer.write(event);
            writer.write(':');
            writer.write(String.valueOf(o));
            writer.write('\n');
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(String event) {
        try {
            writer.write(event);
            writer.write('\n');
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        log("<< close() >>");
        wrapped.close();
    }

    public void flush() throws IOException {
        log("<< flush() >>");
        wrapped.flush();
    }

    public long getPosition() {
        long position = wrapped.getPosition();
        log("getPosition", position);
        return position;
    }

    public void setPosition(long newPosition) throws IOException {
        log("setPosition", newPosition);
        wrapped.setPosition(newPosition);
    }

    public int getCachedSize() {
        int cachedSize = wrapped.getCachedSize();
        log("getCachedSize", cachedSize);
        return cachedSize;
    }

    public void setAutoFlushing(boolean autoFlush) {
        log("setAutoFlushing", autoFlush);
        wrapped.setAutoFlushing(autoFlush);
    }

    public void setCharactersEncoding(Charset charset) {
        log("setCharactersEncoding", charset);
        wrapped.setCharactersEncoding(charset);
    }

    public void setEndianess(ByteOrder byteOrder) {
        log("setEndianess", byteOrder);
        wrapped.setEndianess(byteOrder);
    }

    public void writeHeader(Header header) throws IOException {
        log("header", header);
        wrapped.writeHeader(header);
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStreamWrapper#writeTokenType(org.gvsig.bxml.stream.io.TokenType)
     */
    public void writeTokenType(TokenType tokenType) throws IOException {
        log("Token [at pos " + wrapped.getPosition() + "]", tokenType);
        wrapped.writeTokenType(tokenType);
    }

    public void writeString(CharSequence string) throws IOException {
        log("writeString", string);
        wrapped.writeString(string);
    }

    public void writeString(char[] buffer, int offset, int length) throws IOException {
        log("writeString(char[], int,int)", new String(buffer, offset, length));
        wrapped.writeString(buffer, offset, length);
    }
}
