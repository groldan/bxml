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

import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.CharsetDecoder;

import org.gvsig.bxml.stream.BxmlStreamReader;

/**
 * Represents an incoming stream of data formatted as per the BXML spec.
 * <p>
 * Implementations are required to parse the BXML header into a {@link Header} instance at
 * initialization time in order to guarantee the availability of the header through the
 * {@link #getHeader()} method at any time.
 * </p>
 * <p>
 * This interface defines read methods to obtain Java language primitives and String objects easily
 * from a BXML-formatted data source. Thus, its expected that most implementations are actually
 * decorators over another kind of input stream like {@link InputStream} or
 * {@link ReadableByteChannel}.
 * </p>
 * <p>
 * This interface does not impose any threading consideration and hence client code shall consider
 * the implementations acquired through a {@link BxmlStreamFactory} to be not thread safe.
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public interface BxmlInputStream {

    /**
     * Returns the {@link Header} parsed at this input stream initialization.
     * <p>
     * The header will contain format metadata such as endianess, character encoding and
     * compression. Once the header is parsed it'll be retained by the BxmlInputStream instance so
     * successive calls to this method return the cached Header instance, as well as to use its
     * metadata to properly treat the input data, if needed.
     * </p>
     * <p>
     * This method does not throws any exception and returns a properly formed Header instance. This
     * means that the BxmlInputStream has to fail fast at its initialization stage to ensure the
     * header is available from the moment it is ready to start being used by client code.
     * </p>
     * 
     * @return the Header object
     * @pos {$return != null}
     */
    public Header getHeader();

    /**
     * Closes this stream and releases any system resources associated with it.
     * <p>
     * If the stream is already closed then invoking this method has no effect.
     * </p>
     * <p>
     * After a bxml inputstream is closed, any further attempt to invoke I/O operations upon it will
     * cause an {@link IOException} to be thrown.
     * </p>
     * 
     * @throws IOException
     *             if an I/O error occurs
     */
    public void close() throws IOException;

    /**
     * Returns whether this reader and the underlying input source is open and thus it can be
     * required to read content.
     * 
     * @return {@code true} if the stream is open, {@code false} otherwise
     */
    public boolean isOpen();

    public TokenType readTokenType() throws IOException;

    /**
     * Reads a {@code Count} integral quantity and returns a possibly widened primitive value.
     * <p>
     * The {@code Count} type is defined as
     * 
     * <pre>
     * &lt;code&gt;
     * union Count { // numeric value type for counts, offsets
     *     SmallNum, // negative numbers are invalid
     *     UShortNum,
     *     IntNum,
     *     LongNum
     * }
     * &lt;/code&gt;
     * </pre>
     * 
     * The type used is identified by the first byte, and small numbers will only require a single
     * byte. Having a single {@code readCount()} simplifies the client code by avoiding it to
     * implement logic to read the specific integral quantity based on its type identifier.
     * </p>
     * 
     * @return a long number read from the underlying input stream using the readers #
     * @throws IOException
     */
    public long readCount() throws IOException;

    /**
     * Convenience method to read a char array directly into a string, should be avoided as much as
     * possible for potentially large strings.
     * 
     * @param size
     * @return
     * @throws IOException
     */
    public String readString() throws IOException;

    /**
     * Reads as much characters as possible and stores them in {@code dst} starting at index {@code
     * offset}, and returns the number of characters stored or {@code -1} if there are no more
     * characters for the current string token.
     * 
     * @param dst
     * @param offset
     * @return
     */
    // public void readString(char[] dst, int offset, int length) throws IOException;
    /**
     * Uses the following {@code byteCount} bytes from the underlying input stream to decode them as
     * a String using the provided {@code charsetDecoder} and store the decoded characters into the
     * {@code destination} character buffer.
     * <p>
     * The {@code destination} CharBuffer shall contain free space enough to hold all the characters
     * decoded from the {@code byteCount} bytes. How many characters will be decoded, though, is
     * dependant on the character encoding scheme being used and will be <= byteCount.
     * </p>
     * <p>
     * NOTE: if the client code is reusing the {@code charsetDecoder} being passed as argument, make
     * sure it calls {@code CharsetDecoder#reset() charsetDecoder.reset()} before calling this
     * method to avoid some previous state to make the decoding fail. If the destination buffer has
     * no enough space to hold the decoded characters, a runtime exception will be thrown when its
     * exhausted.
     * </p>
     * 
     * @param charsetDecoder
     * @param destination
     *            the character buffer where to store the decoded string characters
     * @param byteCount
     *            number of bytes, starting at current position + 1, to decode into a String with
     *            the given CharsetDecoder
     * @throws IOException
     */
    public void decode(CharsetDecoder charsetDecoder, CharBuffer destination, int byteCount)
            throws IOException;

    public boolean readBoolean() throws IOException;

    public void readBoolean(boolean[] buffer, int offset, int count) throws IOException;

    public int readByte() throws IOException;

    public void readByte(byte[] buffer, int offset, int count) throws IOException;

    public short readShort() throws IOException;

    /**
     * @param buffer
     *            the buffer where to store the shorts read. A int[] is used instead of a short[] to
     *            avoid double buffering when mapping a high level
     *            {@link BxmlStreamReader#getValue(int[], int, int)} to an array of short numbers.
     * @param offset
     * @param count
     * @throws IOException
     */
    public void readShort(int[] buffer, int offset, int count) throws IOException;

    public int readUShort() throws IOException;

    public void readUShort(int[] buffer, int offset, int count) throws IOException;

    public int readInt() throws IOException;

    public void readInt(int[] buffer, int offset, int count) throws IOException;

    public long readLong() throws IOException;

    public void readLong(long[] buffer, int offset, int count) throws IOException;

    public float readFloat() throws IOException;

    public void readFloat(float[] buffer, int offset, int count) throws IOException;

    public double readDouble() throws IOException;

    public void readDouble(double[] buffer, int offset, int count) throws IOException;

    public long getPosition();

    /**
     * Skips a String value; that is, the amount of bytes given by reading a count value and the
     * byte length of that count value.
     * <p>
     * This method assumes the calling code knows the cursor is set at the start of a String value,
     * there's no way for an implementation to check that since BxmlInputStream does not have any
     * knowledge on the structure of the contents it reads.
     * </p>
     * <p>
     * This is actually a convenience method to skip a String, which is equivallent to do the
     * following:
     * 
     * <pre>
     * &lt;code&gt;
     * BxmlInputStream stream = ...
     * ....
     * long length = stream.readCount();
     * stream.skip((int) length);
     * &lt;/code&gt;
     * </pre>
     * 
     * </p>
     * 
     * @throws IOException
     * @see #skip(int)
     */
    public void skipString() throws IOException;

    /**
     * Skips {@code byteCount} bytes from the underlying input stream and positions the cursor at
     * {@code getPosition() + byteCount}.
     * 
     * @param byteCount
     * @throws IOException
     */
    public void skip(int byteCount) throws IOException;

    public TrailerToken readTrailer() throws IOException;

    /**
     * @return whether an arbitrary position can be set
     */
    public boolean supportsRandomAccess();

    public void setPosition(long position) throws IOException;

    /**
     * @return the file size
     * @throws UnsupportedOperationException
     *             if {@code #supportsRandomAccess() == false}
     */
    public long getSize() throws IOException, UnsupportedOperationException;
}
