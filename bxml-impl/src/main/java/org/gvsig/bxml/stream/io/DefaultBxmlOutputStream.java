/* gvSIG. Sistem a de Información Geográfica de la Generalitat Valenciana
 *
 * Copyright (C) 2007 Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ibáñez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 9638 62 495
 *      gvsig@gva.es
 *      www.gvsig.gva.es
 */
package org.gvsig.bxml.stream.io;

import static org.gvsig.bxml.stream.io.ValueType.BYTE_BYTE_COUNT;
import static org.gvsig.bxml.stream.io.ValueType.DOUBLE_BYTE_COUNT;
import static org.gvsig.bxml.stream.io.ValueType.FLOAT_BYTE_COUNT;
import static org.gvsig.bxml.stream.io.ValueType.INTEGER_BYTE_COUNT;
import static org.gvsig.bxml.stream.io.ValueType.LONG_BYTE_COUNT;
import static org.gvsig.bxml.stream.io.ValueType.SHORT_BYTE_COUNT;
import static org.gvsig.bxml.stream.io.ValueType.USHORT_BYTE_COUNT;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

import org.gvsig.bxml.stream.io.Header.Compression;
import org.gvsig.bxml.stream.io.Header.Flags;
import org.gvsig.bxml.stream.io.Header.Identifier;
import org.gvsig.bxml.stream.io.Header.Version;

/**
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
final class DefaultBxmlOutputStream implements BxmlOutputStream {
    /**
     * Empty buffer used to replace {@link #buffer} at {@link #dispose()} in order to actually
     * release any system resource (probably a mapped memory region) used by buffer.
     */
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);

    private final Counts counts;

    private StringEncoder stringEncoder;

    private ByteBuffer buffer;

    protected WritableByteChannel writeChannel;

    private long position;

    private boolean autoflush;

    /**
     * Creates a DefaultBxmlOutputStream that writes to the given write {@code channel} in the
     * provided {@code byteOrder} using the provided {@code bulkWriteSize} as the buffer capacity.
     * <p>
     * </p>
     * 
     * @param channel
     * @param bulkWriteSize
     * @param defaultByteOrder
     */
    public DefaultBxmlOutputStream(final WritableByteChannel channel, final int bulkWriteSize,
            final ByteOrder defaultByteOrder) {
        this.writeChannel = channel;
        expandBuffer(bulkWriteSize);
        setEndianess(defaultByteOrder);
        this.autoflush = true;
        this.counts = new Counts();
        setCharactersEncoding(Charset.forName("UTF-8"));
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#close()
     */
    public void close() throws IOException {
        if (writeChannel != null) {
            flush();
            // do not close the channel, its up to the client code to do so...
            // writeChannel.close();
            stringEncoder.dispose();
        }
        position = -1;
        writeChannel = null;
        buffer = EMPTY_BUFFER;
    }

    /**
     * @return true if the underlying write channel is not null and is open.
     * @see BxmlOutputStream#isOpen()
     */
    public boolean isOpen() {
        return writeChannel != null && writeChannel.isOpen();
    }

    public long getPosition() {
        return position;
    }

    /**
     * Writes the remaining bytes in the internal buffer to the underlying output stream and rewinds
     * the buffer.
     * <p>
     * Calling this method forces flushing the cached data regardless of the state of
     * {@link #isAutoFlushing()}
     * </p>
     * 
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#flush()
     */
    public void flush() throws IOException {
        if (buffer.position() == 0) {
            return;
        }
        buffer.limit(buffer.position());
        buffer.position(0);

        while (buffer.remaining() > 0) {
            writeChannel.write(buffer);
        }
        buffer.position(0);
        buffer.limit(buffer.capacity());
    }

    public int getCachedSize() {
        return buffer.position();
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#isAutoFlushing()
     */
    public boolean isAutoFlushing() {
        return autoflush;
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#setAutoFlushing(boolean)
     */
    public void setAutoFlushing(boolean autoFlush) {
        this.autoflush = autoFlush;
    }

    public void setPosition(long newPosition) throws IOException {
        long currentPosition = getPosition();
        int cachedSize = getCachedSize();
        final long writtenLimit = currentPosition - cachedSize;
        if (newPosition < writtenLimit) {
            throw new IllegalArgumentException(newPosition + " is bellow the buffered threshold "
                    + writtenLimit);
        }
        long newCachedPosition = newPosition - writtenLimit;
        if (newCachedPosition > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("newposition exceeds cacheable size");
        }
        buffer.position((int) newCachedPosition);
        this.position = newPosition;
    }

    /**
     * Makes sure the buffer can hold {@code byteCount} more bytes and sets the buffer limit to the
     * current position plus byteCount.
     * <p>
     * If there's not enough free space to hold {@code byteCount} bytes more, this method will
     * either expand the buffer size or flush the current unwritten bytes, depending on whether
     * {@link #isAutoFlushing()} is set or not, respectively.
     * </p>
     * 
     * @param byteCount
     * @throws IOException
     */
    private void ensureWriteCapacity(final int byteCount) throws IOException {
        if (buffer.capacity() - buffer.position() < byteCount) {
            if (autoflush) {
                flush();
                // calling code shall ensure they don't ask for more than the write page size
                if (byteCount > buffer.capacity()) {
                    throw new IllegalArgumentException("Can't allocate more than read page size: "
                            + buffer.capacity());
                }
                // not needed, flush rewinds the buffer buffer.position(0);
            } else {
                // can't flush, expand the buffer by a multiple of 1Kb
                int kMultiplier = 1 + (byteCount / 1024);
                expandBuffer(buffer.capacity() + (1024 * kMultiplier));
            }
        }
    }

    private void expandBuffer(final int newBufferSize) {
        final ByteBuffer previousBuffer = this.buffer;

        buffer = ByteBuffer.allocateDirect(newBufferSize);
        buffer.position(0);
        buffer.limit(buffer.capacity());

        if (previousBuffer != null) {
            buffer.order(previousBuffer.order());
            // transfer existing content to new buffer
            previousBuffer.limit(previousBuffer.position());
            previousBuffer.position(0);
            buffer.put(previousBuffer);
        }
        buffer.limit(buffer.capacity());
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#setEndianess(java.nio.ByteOrder)
     */
    public void setEndianess(final ByteOrder byteOrder) {
        if (!isOpen()) {
            throw new IllegalStateException("stream is closed");
        }
        buffer.order(byteOrder);
    }

    public ByteOrder getEndianess() {
        if (!isOpen()) {
            throw new IllegalStateException("stream is closed");
        }
        return buffer.order();
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#setCharactersEncoding(java.nio.charset.Charset)
     */
    public void setCharactersEncoding(final Charset charset) {
        if (stringEncoder != null) {
            stringEncoder.dispose();
        }
        this.stringEncoder = new StringEncoder(this, charset, counts);
    }

    /**
     * This method is not API, its here only to facilitate unit testing
     * 
     * @return {@code null} if {@link #writeHeader(Header)} nor
     *         {@link #setCharactersEncoding(Charset)} were not yet called, the charset encoding
     *         this writer encodes strings into otherwise.
     */
    Charset getCharactersEncoding() {
        return stringEncoder == null ? null : stringEncoder.getCharset();
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeString(java.lang.String)
     */
    public void writeString(final CharSequence string) throws IOException {
        this.stringEncoder.encode(CharBuffer.wrap(string));
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeString(char[], int, int)
     */
    public void writeString(final char[] buffer, final int offset, final int length)
            throws IOException {
        this.stringEncoder.encode(CharBuffer.wrap(buffer, offset, length));
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeByte(int)
     */
    public void writeByte(final int _byte) throws IOException {
        ensureWriteCapacity(BYTE_BYTE_COUNT);
        position += BYTE_BYTE_COUNT;
        buffer.put((byte) _byte);
    }

    /**
     * @throws IOException
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeByte(byte[], int, int)
     */
    public void writeByte(final byte[] src, final int offset, final int length) throws IOException {
        if (length < this.buffer.capacity()) {
            ensureWriteCapacity(length);
            this.buffer.put(src, offset, length);
        } else {
            final int bulkSize = Math.min(length, this.buffer.capacity());
            int writtenCount = 0;
            while (writtenCount < length) {
                int remaining = length - writtenCount;
                int toWrite = Math.min(remaining, bulkSize);
                ensureWriteCapacity(toWrite);
                // write toWrite bytes starting at index offset + writtenCount
                this.buffer.put(src, offset + writtenCount, toWrite);
                writtenCount += toWrite;
            }
            assert writtenCount == length;
        }
        position += BYTE_BYTE_COUNT * length;
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeCount(long)
     */
    public void writeCount(final long count) throws IOException {
        counts.writeCount(count, this);
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeDouble(double)
     */
    public void writeDouble(final double _double) throws IOException {
        ensureWriteCapacity(DOUBLE_BYTE_COUNT);
        buffer.putDouble(_double);
        position += DOUBLE_BYTE_COUNT;
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeDouble(double[], int, int)
     */
    public void writeDouble(final double[] src, final int offset, final int length)
            throws IOException {
        for (int i = 0; i < length; i++) {
            writeDouble(src[offset + i]);
        }
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeFloat(float)
     */
    public void writeFloat(final float _float) throws IOException {
        ensureWriteCapacity(FLOAT_BYTE_COUNT);
        buffer.putFloat(_float);
        position += FLOAT_BYTE_COUNT;
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeFloat(float[], int, int)
     */
    public void writeFloat(final float[] src, final int offset, final int length)
            throws IOException {
        for (int i = 0; i < length; i++) {
            writeFloat(src[offset + i]);
        }
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeInt(int)
     */
    public void writeInt(final int _int) throws IOException {
        ensureWriteCapacity(INTEGER_BYTE_COUNT);
        buffer.putInt(_int);
        position += INTEGER_BYTE_COUNT;
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeInt(int[], int, int)
     */
    public void writeInt(final int[] src, final int offset, final int length) throws IOException {
        for (int i = 0; i < length; i++) {
            writeInt(src[offset + i]);
        }
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeLong(long)
     */
    public void writeLong(final long _long) throws IOException {
        ensureWriteCapacity(LONG_BYTE_COUNT);
        buffer.putLong(_long);
        position += LONG_BYTE_COUNT;
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeLong(long[], int, int)
     */
    public void writeLong(final long[] src, final int offset, final int length) throws IOException {
        for (int i = 0; i < length; i++) {
            writeLong(src[offset + i]);
        }
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeShort(short)
     */
    public void writeShort(final short _short) throws IOException {
        ensureWriteCapacity(SHORT_BYTE_COUNT);
        buffer.putShort(_short);
        position += SHORT_BYTE_COUNT;
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeShort(short[], int, int)
     */
    public void writeShort(final short[] src, final int offset, final int length)
            throws IOException {
        for (int i = 0; i < length; i++) {
            writeShort(src[offset + i]);
        }
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeTokenType(org.gvsig.bxml.stream.io.TokenType)
     */
    public void writeTokenType(final TokenType tokenType) throws IOException {
        writeByte(tokenType.getCode());
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeUShort(int)
     */
    public void writeUShort(final int ushort) throws IOException {
        ensureWriteCapacity(USHORT_BYTE_COUNT);
        buffer.putShort((short) ushort);
        position += USHORT_BYTE_COUNT;
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeUShort(int[], int, int)
     */
    public void writeUShort(final int[] src, final int offset, final int length) throws IOException {
        for (int i = 0; i < length; i++) {
            writeUShort(src[offset + i]);
        }
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeBoolean(boolean)
     */
    public void writeBoolean(final boolean b) throws IOException {
        writeByte(b ? 0xFF : 0x00);
    }

    public void writeBoolean(boolean[] value, int offset, int length) throws IOException {
        final int finalIndex = length - offset;
        for (int i = offset; i < finalIndex; i++) {
            writeBoolean(value[i]);
        }
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeHeader(Header)
     */
    public void writeHeader(final Header header) throws IOException {
        final Identifier identifier = header.getIdentifier();
        writeByte(identifier.getNonTextMarker());
        // BXML
        byte[] name = identifier.getName();
        writeByte(name, 0, name.length);
        // binary check
        byte[] binaryCheck = identifier.getBinaryCheck();
        writeByte(binaryCheck, 0, binaryCheck.length);

        // version
        Version version = header.getVersion();
        writeByte(version.getMajor());
        writeByte(version.getMinor());
        writeByte(version.getPoint());

        // flags bitmasks
        Flags flags = header.getFlags();
        writeByte(flags.getFlags1());
        writeByte(flags.getFlags2());

        // compression
        Compression compression = header.getCompression();
        writeByte(compression.compressionCode());

        // character content charset encoding
        Charset charactersEncoding = header.getCharactersEncoding();
        String charsetInAciiFormat = charactersEncoding.name();
        // int valueCode = ValueType.StringCode.getCode();
        // asume len is lower than 239 and thus a smallnum value type
        int len = charsetInAciiFormat.length();
        counts.writeCount(len, this);
        char[] charArray = charsetInAciiFormat.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            writeByte(charArray[i]);
        }
        setCharactersEncoding(charactersEncoding);
        setEndianess(flags.getEndianess());
        // TODO: configure GZIP encoding
    }
}
