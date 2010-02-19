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
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public interface BxmlOutputStream {

    /**
     * Sets the byte order to use in writing multi-byte primitives
     * <p>
     * This method may be called multiple times, the new byteOrder has effect starting on the first
     * {@code writeXXX} call after the byte order is set.
     * </p>
     * 
     * @param byteOrder
     *            the endianess to use to encode multi-byte primitives
     */
    public void setEndianess(ByteOrder byteOrder);

    /**
     * Returns the byte order this writer uses to encode multi-byte primitive values. If a byte
     * order was not set through {@link #setEndianess(ByteOrder)} but {@link #writeHeader(Header)}
     * was already called, it shall be the byte order specified by the header, otherwise the
     * platform's default endianess.
     * 
     * @return the endianess used to encode multi-byte primitives
     */
    public ByteOrder getEndianess();

    /**
     * Allows the client code to set the charset scheme this output stream shall use to encode
     * strings into when calling {@link #writeString(String)} or
     * {@link #writeString(char[], int, int)}. If not explicitly set, such character squeme is
     * assumed to be the one provided by the header when {@link #writeHeader(Header)} was called.
     * 
     * @param charset
     */
    public void setCharactersEncoding(Charset charset);

    /**
     * Closes this stream and releases any system resources associated with it, but does not close
     * the underlying output stream/channel.
     * <p>
     * If the stream is already closed then invoking this method has no effect.
     * </p>
     * <p>
     * After this stream is closed, any further attempt to invoke I/O operations upon it will cause
     * unpredictable results
     * </p>
     * 
     * @throws IOException
     *             if an I/O error occurs
     */
    public void close() throws IOException;

    /**
     * @return whether this stream writer is closed and thus no {@code writeXXX} operations shall be
     *         called
     */
    public boolean isOpen();

    /**
     * Returns the current byte position, which is the same than the amount of byte written so far.
     * 
     * @return
     */
    public long getPosition();

    /**
     * Allows to rewind to a previous file offset, as long as the new position lays inside the
     * cached data.
     * 
     * @pre {newPosition > ( getPosition() - getCachedSize() )}
     * @param newPosition
     *            absolute file offset where to set the write pointer to.
     */
    public void setPosition(final long newPosition) throws IOException;

    /**
     * Returns the number of bytes currently cached
     * 
     * @return
     */
    public int getCachedSize();

    /**
     * Sets whether the output stream is allowed to auto flush content or not.
     * <p>
     * Given the streamed nature of the writing process, this state control is useful for situations
     * were the calling code may need to temporarily go back to a certain position in the output
     * stream, hence ensuring first the content for that position will not be auto flushed. Shall be
     * used with caution though, trying to avoid auto flushing as much as possible, or rather to
     * force buffering as least as possible.
     * </p>
     * <p>
     * The common case for setting autoFlushing off is when writing a start element token, which can
     * be either a {@link TokenType#EmptyElement}, {@link TokenType#EmptyAttrElement},
     * {@link TokenType#ContentElement}, or {@link TokenType#ContentAttrElement}, and which one
     * might not be known in advance.
     *</p>
     * 
     * @param autoFlush
     *            {@code true} for this output stream to decide when to flush content, {@code false}
     *            to prevent it from flushing content at all until {@code autoFlush} is set to
     *            {@code true} again.
     */
    public void setAutoFlushing(boolean autoFlush);

    /**
     * 
     * @return
     * @see #setAutoFlushing(boolean)
     */
    public boolean isAutoFlushing();

    /**
     * Writes the remaining bytes in the internal buffer to the underlying output stream and rewinds
     * the buffer.
     * <p>
     * Calling this method <strong>forces</strong> flushing the cached data regardless of the state
     * of {@link #isAutoFlushing()}. The stream's {@link #getPosition() position} doesn't change,
     * but going back to a previous {@link #setPosition(long) position} will not be possible.
     * </p>
     */
    public void flush() throws IOException;

    /**
     * Writes out the provided header and configures the writer for the header provided encoding
     * options.
     * <p>
     * The header provided encoding options this writer will be configured to use after writing the
     * provided header are:
     * <ul>
     * <li>The byte order (endianess) to use for the multi-byte primitives
     * <li>The Character Set encoding to use for string writing
     * <li>Whether to use or not GZIP compression
     * </ul>
     * </p>
     * 
     * @param header
     * @throws IOException
     */
    public void writeHeader(Header header) throws IOException;

    /**
     * Writes a given token identifier
     * 
     * @param tokenType
     * @throws IOException
     * @see TokenType#getCode()
     */
    public void writeTokenType(TokenType tokenType) throws IOException;

    /**
     * Writes a "count" using the least count package size possible for its value.
     * <p>
     * As per the bxml spec, counts are integer values used for a number of purposes, such as to
     * prepend the lenght of a string to the string itself, etc. Most of the times, counts are
     * pretty small values, and thus there exist a number of Count tokens: SmallNum, UShort, Int and
     * Long. This method,thus, allows to use a single value type to encode any count type.
     * Implementations shall write down the count type whose value range fits for the given {@code
     * count}.
     * </p>
     * 
     * @param count
     *            a positive integer or zero
     * @throws IOException
     * @see ValueType#SmallNum
     * @see ValueType#UShortCode
     * @see ValueType#IntCode
     * @see ValueType#LongCode
     */
    public void writeCount(long count) throws IOException;

    /**
     * Writes the provided string encoded in the character set established by the last call to
     * {@link #setCharactersEncoding(Charset)}
     * 
     * @param string
     *            non null string to encode
     * @throws IOException
     */
    public void writeString(CharSequence string) throws IOException;

    /**
     * Writes the string literal defined by the provided {@code buffer}, starting at array index
     * {@code offset} and using {@code length} elements from the array.
     * 
     * @param buffer
     *            the source of data to write down to the underlying output stream
     * @param offset
     *            the first index, inclusive, to start writing array elements at
     * @param length
     *            the ammount of array elements to write down
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeString(char[] buffer, int offset, int length) throws IOException;

    /**
     * Writes a single byte down to the underlying output stream.
     * 
     * @param _byte
     *            the value to write
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeByte(int _byte) throws IOException;

    /**
     * Writes {@code length} bytes from the provided {@code buffer}, starting at array index {@code
     * offset}.
     * 
     * @param buffer
     *            the source of data to write down to the underlying output stream
     * @param offset
     *            the first index, inclusive, to start writing array elements at
     * @param length
     *            the ammount of array elements to write down
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeByte(byte[] buffer, int offset, int length) throws IOException;

    /**
     * Writes a single short value down to the underlying output stream using this writer's byte
     * order.
     * 
     * @param _short
     *            the value to write
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeShort(short _short) throws IOException;

    /**
     * Writes {@code length} shorts from the provided {@code buffer}, starting at array index
     * {@code offset} and using this writer's byte order.
     * 
     * @param buffer
     *            the source of data to write down to the underlying output stream
     * @param offset
     *            the first index, inclusive, to start writing array elements at
     * @param length
     *            the ammount of array elements to write down
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeShort(short[] buffer, int offset, int length) throws IOException;

    /**
     * Writes a single unsigned short value down to the underlying output stream using this writer's
     * byte order.
     * 
     * @param ushort
     *            the value to write
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeUShort(int ushort) throws IOException;

    /**
     * Writes {@code length} unsigned shorts from the provided {@code buffer}, starting at array
     * index {@code offset} and using this writer's byte order.
     * 
     * @param buffer
     *            the source of data to write down to the underlying output stream
     * @param offset
     *            the first index, inclusive, to start writing array elements at
     * @param length
     *            the ammount of array elements to write down
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeUShort(int[] buffer, int offset, int length) throws IOException;

    /**
     * Writes a single int value down to the underlying output stream using this writer's byte
     * order.
     * 
     * @param _int
     *            the value to write
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeInt(int _int) throws IOException;

    /**
     * Writes {@code length} integers the provided {@code buffer}, starting at array index {@code
     * offset} and using this writer's byte order.
     * 
     * @param buffer
     *            the source of data to write down to the underlying output stream
     * @param offset
     *            the first index, inclusive, to start writing array elements at
     * @param length
     *            the ammount of array elements to write down
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeInt(int[] buffer, int offset, int length) throws IOException;

    /**
     * Writes a single long value down to the underlying output stream using this writer's byte
     * order.
     * 
     * @param _long
     *            the value to write
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeLong(long _long) throws IOException;

    /**
     * Writes {@code length} longs the provided {@code buffer}, starting at array index {@code
     * offset} and using this writer's byte order.
     * 
     * @param buffer
     *            the source of data to write down to the underlying output stream
     * @param offset
     *            the first index, inclusive, to start writing array elements at
     * @param length
     *            the ammount of array elements to write down
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeLong(long[] buffer, int offset, int length) throws IOException;

    /**
     * Writes a single float value down to the underlying output stream using this writer's byte
     * order.
     * 
     * @param _float
     *            the value to write
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeFloat(float _float) throws IOException;

    /**
     * Writes {@code length} floats from the provided {@code buffer}, starting at array index
     * {@code offset} and using this writer's byte order.
     * 
     * @param buffer
     *            the source of data to write down to the underlying output stream
     * @param offset
     *            the first index, inclusive, to start writing array elements at
     * @param length
     *            the ammount of array elements to write down
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeFloat(float[] buffer, int offset, int length) throws IOException;

    /**
     * Writes a single double value down to the underlying output stream using this writer's byte
     * order.
     * 
     * @param _double
     *            the value to write
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeDouble(double _double) throws IOException;

    /**
     * Writes {@code length} doubles from the provided {@code buffer}, starting at array index
     * {@code offset} and using this writer's byte order.
     * 
     * @param buffer
     *            the source of data to write down to the underlying output stream
     * @param offset
     *            the first index, inclusive, to start writing array elements at
     * @param length
     *            the ammount of array elements to write down
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeDouble(double[] buffer, int offset, int length) throws IOException;

    /**
     * Writes a single boolean value down to the underlying output stream.
     * 
     * @param b
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeBoolean(boolean b) throws IOException;

    /**
     * Writes {@code length} booleans from the provided {@code buffer}, starting at array index
     * {@code offset}.
     * 
     * @param buffer
     *            the source of data to write down to the underlying output stream
     * @param offset
     *            the first index, inclusive, to start writing array elements at
     * @param length
     *            the ammount of array elements to write down
     * @throws IOException
     *             if an input/output exception occurs while writing data down to the underlying
     *             output stream
     */
    public void writeBoolean(boolean[] buffer, int offset, int length) throws IOException;

}
