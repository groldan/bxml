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

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.Set;

import org.gvsig.bxml.stream.io.Header.Flags;

/**
 * A {@link BxmlInputStream} implementation that uses the {@code java.nio} API to gather the bxml
 * data from.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
final class DefaultBxmlInputStream implements BxmlInputStream {

    /**
     * Empty buffer used to replace {@link #buffer} at {@link #dispose()} in order to actually
     * release any system resource (probably a mapped memory region) used by buffer.
     */
    protected static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);

    /**
     * Lazily initialized by {@link #getHeader()}
     */
    private Header header;

    /**
     * Lazily initialized by {@link #getParseUtils()}
     */
    private ParsingUtils parseUtils;

    private ByteBuffer buffer;

    private ReadableByteChannel readChannel;

    /**
     * holds the "cursor" byte position
     */
    private long position;

    private final boolean supportsRandomAccess;

    /**
     * Whether channel is a FileChannel and hence the buffer can be memory mapped
     */
    private final boolean bufferIsMappeable;

    /**
     * The file size if known, -1 if unknown (hence bufferIsMappeable == false)
     */
    private final long fileSize;

    /**
     * Initializes this bxml input stream by reading the header.
     * 
     * @pre {channel != null}
     * @pre {channel.isOpen() == true}
     * @pre {pageSize > 0}
     * @param channel
     *            input channel from where to read the bxml stream
     * @pageSize the size in bytes for the internal buffer used to do bulk reads from the channel.
     * @throws IOException
     *             if an I/O exception occurs reading the input channel
     * @throws IllegalArgumentException
     *             if the header not well formed
     */
    public DefaultBxmlInputStream(ReadableByteChannel channel, int pageSize) throws IOException {
        this(null, channel, pageSize);
    }

    /**
     * Package visible constructor <b>only</b> to easy unit testing by providing a Header so it does
     * not have to be parsed.
     * 
     * @param header
     *            the parsed file header, may be {@code null}, in which case it'll be parsed from
     *            the {@code channel} content in order to initialize the stream
     * @param channel
     * @param pageSize
     * @throws IOException
     */
    DefaultBxmlInputStream(Header header, ReadableByteChannel channel, int pageSize)
            throws IOException {
        this.readChannel = channel;
        this.supportsRandomAccess = channel instanceof FileChannel;
        this.bufferIsMappeable = channel instanceof FileChannel;

        ByteBuffer buffer;
        if (bufferIsMappeable) {
            final FileChannel fileChannel = (FileChannel) channel;
            final long size = fileChannel.size();
            final long mapSize = Math.min(size, pageSize);
            this.fileSize = size;
            buffer = fileChannel.map(MapMode.READ_ONLY, 0, mapSize);
        } else {
            this.fileSize = -1L;
            buffer = ByteBuffer.allocateDirect(pageSize);
            // start empty
            buffer.position(0);
            buffer.limit(0);
        }
        this.buffer = buffer;
        Header theHeader = header;
        if (theHeader == null) {
            theHeader = parseHeader();
        }
        this.header = theHeader;
        final Flags flags = this.header.getFlags();
        buffer.order(flags.getEndianess());
    }

    private Header parseHeader() throws IllegalArgumentException, IOException {
        final Charset ascii = Charset.forName("US-ASCII");
        final ParsingUtils parser = new ParsingUtils(ascii, this);
        Header header = parser.parseHeader();
        return header;
    }

    protected static final int fill(ByteBuffer buffer, ReadableByteChannel channel)
            throws IOException {
        int r = buffer.remaining();
        // channel reads return -1 when EOF or other error
        // because they a non-blocking reads, 0 is a valid return value!!
        while (buffer.remaining() > 0 && r != -1) {
            r = channel.read(buffer);
        }
        if (r == -1) {
            buffer.limit(buffer.position());
        }
        return r;
    }

    /**
     * @see BxmlInputStream#isOpen()
     */
    public final boolean isOpen() {
        return readChannel != null;
    }

    /**
     * Ensures there're at least {@code length} bytes to be read in {@link #buffer}
     * <p>
     * This method is intended to be called as the first statement of every readXXX public method
     * and acts as a safety gate for buffer underflows.
     * </p>
     * <p>
     * If ther're not enough remaining bytes on the buffer to serve {@code length} bytes, it will be
     * {@link ByteBuffer#compact() compacted} and filled with enough data to at least contain
     * {@code length} remaining bytes, and its position will be set to {@code 0}.
     * </p>
     * <p>
     * NOTE: it is the responsibility of the calling code not to request a length greater than the
     * buffer's capacity
     * </p>
     * 
     * @param length
     *            minimum number of bytes needed to be available in the buffer.
     * @throws IOException
     *             if the buffer needs to be populated and an IOException occurs while reading from
     *             the channel.
     * @throws EOFException
     *             if the buffer needs to be populated and there are not enough data on the channel
     */
    protected final void ensureCapacity(final int length) throws IOException {
        // if (buffer.isReadOnly()) {
        // return;
        // }
        if (buffer.remaining() >= length) {
            return;
        }
        if (bufferIsMappeable) {
            long mapSize = Math.max(buffer.capacity(), length);
            long mapFrom = Math.min(position, fileSize - mapSize);
            buffer = ((FileChannel) readChannel).map(MapMode.READ_ONLY, mapFrom, mapSize);
            final Flags flags = getHeader().getFlags();
            buffer.order(flags.getEndianess());
            long newBufferPosition = position - mapFrom;
            buffer.position((int) newBufferPosition);
        } else {
            buffer.compact();
            fill(buffer, readChannel);
            buffer.position(0);
        }
        if (buffer.remaining() < length) {
            throw new EOFException("premature end of file encountered, curr position: " + position
                    + " requested length: " + length);
        }
    }

    /**
     * @see ReadStrategy#getPosition()
     */
    public final long getPosition() {
        return position;
    }

    /**
     * @param charsetDecoder
     * @param charBuffer
     * @param length
     * @throws IOException
     * @see org.gvsig.bxml.stream.io.BxmlInputStream#decode(java.nio.charset.CharsetDecoder,
     *      java.nio.CharBuffer, int)
     */
    public final void decode(final CharsetDecoder charsetDecoder, final CharBuffer charBuffer,
            final int length) throws IOException {
        // collect byte read count until reaching the required length
        int readCount = 0;
        while (readCount < length) {
            final int remainingReadCount = length - readCount;
            final int fillCount = Math.min(buffer.capacity(), remainingReadCount);
            ensureCapacity(fillCount);
            final int useCount = Math.min(buffer.remaining(), remainingReadCount);
            readCount += useCount;
            int oldLimit = buffer.limit();
            buffer.limit(buffer.position() + useCount);
            boolean endOfInput = readCount == length;
            charsetDecoder.decode(this.buffer, charBuffer, endOfInput);
            position += useCount;
            buffer.limit(oldLimit);
        }
    }

    /**
     * @see BxmlInputStream#getHeader()
     */
    public Header getHeader() {
        return header;
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlInputStream#supportsRandomAccess()
     */
    public boolean supportsRandomAccess() {
        return this.supportsRandomAccess;
    }

    public long getSize() throws UnsupportedOperationException, IOException {
        if (!supportsRandomAccess) {
            throw new UnsupportedOperationException(
                    "Random access is only supported for FileChannel");
        }
        return fileSize;
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlInputStream#setPosition(long)
     */
    public void setPosition(final long newPosition) throws IOException {
        if (!supportsRandomAccess) {
            throw new UnsupportedOperationException(
                    "Random access is only supported for FileChannel");
        }

        final long delta = newPosition - position;
        if (delta < 0) {
            if (buffer.position() > Math.abs(delta)) {
                // rewind
                buffer.position((int) (buffer.position() - Math.abs(delta)));
                position = newPosition;
                return;
            }
        } else if (buffer.remaining() > delta) {
            buffer.position((int) (buffer.position() + delta));
            position = newPosition;
            return;
        }

        // skip directly on the channel, but preserve what might already be available in the
        // buffer to prevent multiple unnecessary subsequent reads
        FileChannel fileChannel = (FileChannel) readChannel;
        if (newPosition >= fileSize) {
            throw new IllegalArgumentException("Can't set position to " + newPosition
                    + ", file size is " + fileSize);
        }
        try {
            fileChannel.position(newPosition);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Setting fileChannel position to " + newPosition, e);
        }
        buffer.position(0).limit(0);
        this.position = newPosition;
    }

    /**
     * Closes this channel.
     * <p>
     * After a channel is closed, any further attempt to invoke I/O operations upon it will cause a
     * {@link IOException} to be thrown.
     * <p>
     * If this channel is already closed then invoking this method has no effect.
     * <p>
     * This method may be invoked at any time. If some other thread has already invoked it, however,
     * then another invocation will block until the first invocation is complete, after which it
     * will return without effect.
     * </p>
     * 
     * @throws IOException
     *             If an I/O error occurs
     * @see BxmlInputStream#close()
     */
    public void close() throws IOException {
        if (readChannel != null) {
            readChannel.close();
        }
        position = -1;
        readChannel = null;
        buffer = EMPTY_BUFFER;
    }

    /**
     * @see BxmlInputStream#readTokenType()
     */
    public TokenType readTokenType() throws IOException {
        final int tokenIdentifier = readByte();
        TokenType tokenType;
        try {
            tokenType = TokenType.valueOf(tokenIdentifier);
        } catch (IllegalArgumentException iae) {
            throw new IllegalStateException("Byte at current position (" + getPosition()
                    + ") does not correspond to a token identifier: 0x"
                    + Integer.toHexString(tokenIdentifier), iae);
        }
        return tokenType;
    }

    /**
     * @see BxmlInputStream#readBoolean()
     */
    public boolean readBoolean() throws IOException {
        int booleanByteCode = readByte();
        return 0x00 == booleanByteCode ? false : true;
    }

    /**
     * @see BxmlInputStream#readBoolean(boolean[], int, int)
     */
    public void readBoolean(boolean[] dest, final int offset, final int count) throws IOException {
        for (int i = 0; i < count; i++) {
            dest[offset + i] = readBoolean();
        }
    }

    /**
     * @see BxmlInputStream#readByte()
     */
    public int readByte() throws IOException {
        ensureCapacity(BYTE_BYTE_COUNT);
        position += BYTE_BYTE_COUNT;
        return buffer.get() & 0xFF;
    }

    /**
     * @see BxmlInputStream#readByte(byte[], int, int)
     */
    public void readByte(byte[] dest, final int offset, final int count) throws IOException {
        // collect byte read count until reaching the required length
        int readCount = 0;
        while (readCount < count) {
            final int remainingReadCount = count - readCount;
            final int fillCount = Math.min(buffer.capacity(), remainingReadCount);
            ensureCapacity(fillCount);
            final int useCount = Math.min(buffer.remaining(), remainingReadCount);
            int oldLimit = buffer.limit();
            buffer.limit(buffer.position() + useCount);
            buffer.get(dest, offset + readCount, useCount);
            buffer.limit(oldLimit);
            readCount += useCount;
        }
        position += count;
    }

    /**
     * @see BxmlInputStream#readCount()
     * @see Counts#readCount()
     */
    public long readCount() throws IOException {
        final ParsingUtils parseUtils = getParseUtils();
        return parseUtils.readCount();
    }

    private ParsingUtils getParseUtils() {
        if (this.parseUtils == null) {
            final Charset charactersEncoding = header.getCharactersEncoding();
            ParsingUtils parseUtils = new ParsingUtils(charactersEncoding, this);
            this.parseUtils = parseUtils;
        }
        return this.parseUtils;
    }

    /**
     * @see BxmlInputStream#readDouble()
     */
    public double readDouble() throws IOException {
        ensureCapacity(DOUBLE_BYTE_COUNT);
        position += DOUBLE_BYTE_COUNT;
        return buffer.getDouble();
    }

    /**
     * @see BxmlInputStream#readDouble(double[], int, int)
     */
    public void readDouble(double[] dest, final int offset, final int count) throws IOException {
        for (int i = 0; i < count; i++) {
            dest[offset + i] = readDouble();
        }
    }

    /**
     * @see BxmlInputStream#readFloat()
     */
    public float readFloat() throws IOException {
        ensureCapacity(FLOAT_BYTE_COUNT);
        position += FLOAT_BYTE_COUNT;
        return buffer.getFloat();
    }

    /**
     * @see BxmlInputStream#readFloat(float[], int, int)
     */
    public void readFloat(float[] dest, final int offset, final int count) throws IOException {
        for (int i = 0; i < count; i++) {
            dest[offset + i] = readFloat();
        }
    }

    /**
     * @see BxmlInputStream#readInt()
     */
    public int readInt() throws IOException {
        ensureCapacity(INTEGER_BYTE_COUNT);
        position += INTEGER_BYTE_COUNT;
        return buffer.getInt();
    }

    /**
     * @see BxmlInputStream#readInt(int[], int, int)
     */
    public void readInt(int[] dest, final int offset, final int count) throws IOException {
        for (int i = 0; i < count; i++) {
            dest[offset + i] = readInt();
        }
    }

    /**
     * @see BxmlInputStream#readLong()
     */
    public long readLong() throws IOException {
        ensureCapacity(LONG_BYTE_COUNT);
        position += LONG_BYTE_COUNT;
        return buffer.getLong();
    }

    /**
     * @see BxmlInputStream#readLong(long[], int, int)
     */
    public void readLong(long[] dest, final int offset, final int count) throws IOException {
        for (int i = 0; i < count; i++) {
            dest[offset + i] = readLong();
        }
    }

    /**
     * @see BxmlInputStream#readShort()
     */
    public short readShort() throws IOException {
        ensureCapacity(SHORT_BYTE_COUNT);
        position += SHORT_BYTE_COUNT;
        return buffer.getShort();
    }

    /**
     * @see BxmlInputStream#readShort(int[], int, int)
     */
    public void readShort(int[] dest, final int offset, final int count) throws IOException {
        for (int i = 0; i < count; i++) {
            dest[offset + i] = readShort();
        }
    }

    /**
     * @see BxmlInputStream#readUShort()
     */
    public int readUShort() throws IOException {
        ensureCapacity(USHORT_BYTE_COUNT);
        position += USHORT_BYTE_COUNT;
        return buffer.getShort() & 0xFFFF;
    }

    /**
     * @see BxmlInputStream#readUShort(int[], int, int)
     */
    public void readUShort(int[] dest, final int offset, final int count) throws IOException {
        for (int i = 0; i < count; i++) {
            dest[offset + i] = readUShort();
        }
    }

    /**
     * @see BxmlInputStream#readString()
     * @TODO This has to be improved for performance, we're _only_ hunting correctness right now
     */
    public final String readString() throws IOException {
        final ParsingUtils parseUtils = getParseUtils();
        return parseUtils.parseString().toString();
    }

    /**
     * @see BxmlInputStream#readString(char[], int)
     * @see ParsingUtils#parseString()
     */
    // public void readString(char[] dst, int offset) throws IOException {
    // final ParsingUtils parseUtils = getParseUtils();
    // final CharBuffer parsed = parseUtils.parseString();
    // int buffRemaining = dst.length - offset;
    // int strRemaining = parsed.length();
    // parsed.get(dst, offset, Math.min(buffRemaining, strRemaining));
    // }

    public void skip(final int byteCount) throws IOException {
        if (buffer.remaining() >= byteCount) {
            buffer.position(buffer.position() + byteCount);
            position += byteCount;
            return;
        }

        final long finalPosition = position + byteCount;

        if (supportsRandomAccess) {
            setPosition(finalPosition);
        } else {

            while (position < finalPosition) {
                buffer.compact();
                fill(buffer, readChannel);
                buffer.position(0);
                int advanceCount = buffer.remaining();
                if (advanceCount == 0) {
                    throw new EOFException("Can't advance to position " + finalPosition
                            + ", got EOF at " + position);
                }
                if (position + advanceCount > finalPosition) {
                    advanceCount = (int) (finalPosition - position);
                }
                buffer.position(buffer.position() + advanceCount);
                position += advanceCount;
            }
        }
        assert position == finalPosition;

        // if (buffer.isDirect()) {
        // ensureCapacity(byteCount);
        // buffer.position(buffer.position() + byteCount);
        // } else {
        // throw new UnsupportedOperationException("Not yet implemented for non direct buffers");
        // }
        // position += byteCount;
    }

    /**
     * @see BxmlInputStream#skip(int)
     */
    public void skipOld(int byteCount) throws IOException {
        if (buffer.remaining() >= byteCount) {
            buffer.position(buffer.position() + byteCount);
            position += byteCount;
            return;
        }

        final long finalPosition = position + byteCount;
        while (position < finalPosition) {
            buffer.compact();
            fill(buffer, readChannel);
            buffer.position(0);
            int advanceCount = buffer.remaining();
            if (advanceCount == 0) {
                throw new EOFException("Can't advance to position " + finalPosition
                        + ", got EOF at " + position);
            }
            if (position + advanceCount > finalPosition) {
                advanceCount = (int) (finalPosition - position);
            }
            buffer.position(buffer.position() + advanceCount);
            position += advanceCount;
        }
        assert position == finalPosition;

        // if (buffer.isDirect()) {
        // ensureCapacity(byteCount);
        // buffer.position(buffer.position() + byteCount);
        // } else {
        // throw new UnsupportedOperationException("Not yet implemented for non direct buffers");
        // }
        // position += byteCount;
    }

    /**
     * @see BxmlInputStream#skipString()
     */
    public void skipString() throws IOException {
        final long length = readCount();
        skip((int) length);
    }

    /**
     * Assumes {@link #readTokenType()} has yet been called and returned {@link TokenType#Trailer}
     * 
     * @see BxmlInputStream#readTrailer()
     */
    public TrailerToken readTrailer() throws IOException {
        final long startPosition = getPosition() - 1;
        final byte[] expected = { 0x01, 'T', 'R', 0x00 };
        byte[] id = new byte[4];
        readByte(id, 0, 4);
        if (!Arrays.equals(expected, id)) {
            throw new IllegalStateException("malformed trailer token " + Arrays.toString(id));
        }
        TrailerImpl trailer = new TrailerImpl(startPosition);
        readStringTableIndex(trailer);
        readIndexTableIndex(trailer);
        final int trailerTokenLength = readInt();
        final long currPosition = getPosition();
        // @todo REVISIT: not sure the following assert is correct
        assert currPosition - startPosition == trailerTokenLength;
        return trailer;
    }

    private void readStringTableIndex(TrailerImpl trailer) throws IOException {
        final boolean isUsed = readBoolean();
        long nFragments = 0;
        if (isUsed) {
            nFragments = readCount();
            for (long i = 0; i < nFragments; i++) {
                readStringTableIndexEntry(trailer);
            }
        }
        Set<StringTableIndexEntry> table = trailer.getStringTableIndex();
        assert nFragments == 0 ? table == null : table.size() == nFragments;
        assert isUsed == nFragments > 0;
    }

    private void readStringTableIndexEntry(TrailerImpl trailer) throws IOException {
        final long nStringsDefined = readCount();
        final long fileOffset = readCount();
        StringTableIndexEntryImpl entry = new StringTableIndexEntryImpl(fileOffset, nStringsDefined);
        trailer.addStringTableIndexEntry(entry);
    }

    private void readIndexTableIndex(TrailerImpl trailer) throws IOException {
        final boolean isUsed = readBoolean();
        long nEntries = 0;
        if (isUsed) {
            nEntries = readCount();
            for (long i = 0; i < nEntries; i++) {
                readIndexTableIndexEntry(trailer);
            }
        }
        Set<IndexTableIndexEntry> table = trailer.getIndexTableIndex();
        assert nEntries == 0 ? table == null : nEntries == table.size();
        assert isUsed == nEntries > 0;
    }

    private void readIndexTableIndexEntry(TrailerImpl trailer) throws IOException {
        final String xpathExpression = readString();
        final long fileOffset = readCount();
        IndexTableIndexEntryImpl entry;
        entry = new IndexTableIndexEntryImpl(xpathExpression, fileOffset);
        trailer.addIndexTableIndexEntry(entry);
    }

}
