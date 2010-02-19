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

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.gvsig.bxml.stream.io.ValueType.DOUBLE_BYTE_COUNT;
import static org.gvsig.bxml.stream.io.ValueType.FLOAT_BYTE_COUNT;
import static org.gvsig.bxml.stream.io.ValueType.INTEGER_BYTE_COUNT;
import static org.gvsig.bxml.stream.io.ValueType.LONG_BYTE_COUNT;
import static org.gvsig.bxml.stream.io.ValueType.SHORT_BYTE_COUNT;
import static org.gvsig.bxml.stream.io.ValueType.USHORT_BYTE_COUNT;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

import org.gvsig.bxml.stream.io.Header.Compression;
import org.gvsig.bxml.stream.io.Header.Flags;
import org.gvsig.bxml.stream.io.TestData.ByteArrayWriteChannel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test suite for {@link DefaultBxmlOutputStream}
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class DefaultBxmlOutputStreamTest {

    private DefaultBxmlOutputStream writer;

    /**
     * Channel where writes are sent, and allows to retrieve the written content.
     * 
     * @see TestData.ByteArrayWriteChannel
     */
    private ByteArrayWriteChannel channel;

    /**
     * Write buffer size used for tests
     */
    private static final int DEFAULT_TEST_BUFFER_SIZE = 20;

    /**
     * By default perform writes in platform's byte order
     */
    private static final ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();

    public DefaultBxmlOutputStreamTest() {
        // no-op
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        channel = TestData.getWriteChannel();
        writer = new DefaultBxmlOutputStream(channel, DEFAULT_TEST_BUFFER_SIZE, NATIVE_ORDER);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        channel = null;
        writer = null;
    }

    /**
     * Test method for {@link DefaultBxmlOutputStream#close()}.
     * 
     * @throws IOException
     */
    @Test
    public void testClose() throws IOException {
        assertTrue(channel.isOpen());
        assertTrue(writer.isOpen());
        writer.close();
        assertFalse(writer.isOpen());
        assertTrue(channel.isOpen());
    }

    /**
     * Test method for
     * {@link DefaultBxmlOutputStream#setCharactersEncoding(java.nio.charset.Charset)}.
     */
    @Test
    public void testSetCharactersEncoding() {
        assertEquals(Charset.forName("UTF-8"), writer.getCharactersEncoding());
        Charset defaultCharset = Charset.defaultCharset();
        writer.setCharactersEncoding(defaultCharset);
        assertSame(defaultCharset, writer.getCharactersEncoding());
        Charset utf16 = Charset.forName("UTF-16");
        writer.setCharactersEncoding(utf16);
        assertSame(utf16, writer.getCharactersEncoding());
    }

    /**
     * Test method for {@link DefaultBxmlOutputStream#setEndianess(java.nio.ByteOrder)}.
     * 
     * @throws IOException
     */
    @Test
    public void testSetEndianess() throws IOException {
        assertSame(NATIVE_ORDER, writer.getEndianess());
        writer.setEndianess(ByteOrder.BIG_ENDIAN);
        assertSame(ByteOrder.BIG_ENDIAN, writer.getEndianess());
        writer.setEndianess(ByteOrder.LITTLE_ENDIAN);
        assertSame(ByteOrder.LITTLE_ENDIAN, writer.getEndianess());

        writer.close();
        try {
            writer.setEndianess(ByteOrder.BIG_ENDIAN);
            fail("Expected ISE as the stream is closed");
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    /**
     * Test method for {@link DefaultBxmlOutputStream#writeString(java.lang.String)}.
     * 
     * @throws IOException
     */
    @Test
    public void testWriteString() throws IOException {
        Charset utf16LE = Charset.forName("UTF-16LE");
        Charset utf16BE = Charset.forName("UTF-16BE");
        Charset utf16 = Charset.forName("UTF-16");
        Charset utf8 = Charset.forName("UTF-8");
        Charset ascii = Charset.forName("US-ASCII");
        Charset latin1 = Charset.forName("ISO-8859-1");
        testWriteString(utf16LE);
        testWriteString(utf16BE);
        testWriteString(utf16);
        testWriteString(utf8);
        testWriteString(ascii);
        testWriteString(latin1);
    }

    private void testWriteString(final Charset charset) throws IOException {
        final String smallString = "�Ahora?... �Ma�ana!";
        testEncodeString(charset, "");
        testEncodeString(charset, smallString);
        StringBuffer sb = new StringBuffer();
        while (sb.length() < 100) {
            sb.append(smallString);
        }
        final String largeString = sb.toString();
        testEncodeString(charset, largeString);
    }

    private void testEncodeString(final Charset charset, final String string) throws IOException {
        final CharsetEncoder charencoder = charset.newEncoder();
        charencoder.onMalformedInput(CodingErrorAction.REPLACE);
        charencoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        ByteBuffer encoded = charencoder.encode(CharBuffer.wrap(string));
        final byte[] encodedContent = new byte[encoded.limit()];
        encoded.get(encodedContent);

        final ByteArrayWriteChannel writeChannel = TestData.getWriteChannel();
        final int bufferSize = 30;
        writer = new DefaultBxmlOutputStream(writeChannel, bufferSize, NATIVE_ORDER);

        writer.setCharactersEncoding(charset);
        writer.writeString(string);
        writer.close();

        // sure the count is a SmallNum
        byte[] expected = new byte[1 + encodedContent.length];
        expected[0] = (byte) encodedContent.length;
        System.arraycopy(encodedContent, 0, expected, 1, encodedContent.length);
        byte[] writtenContent = writeChannel.getWrittenContent();
        assertArrayEquals(expected, writtenContent);
    }

    /**
     * Test method for {@link DefaultBxmlOutputStream#writeString(char[], int, int)}.
     * 
     * @throws IOException
     */
    @Test
    public void testWriteStringCharArray() throws IOException {
        Charset utf16LE = Charset.forName("UTF-16LE");
        Charset utf16BE = Charset.forName("UTF-16BE");
        Charset utf16 = Charset.forName("UTF-16");
        Charset utf8 = Charset.forName("UTF-8");
        Charset ascii = Charset.forName("US-ASCII");
        Charset latin1 = Charset.forName("ISO-8859-1");
        testWriteStringCharArray(utf16LE);
        testWriteStringCharArray(utf16BE);
        testWriteStringCharArray(utf16);
        testWriteStringCharArray(utf8);
        testWriteStringCharArray(ascii);
        testWriteStringCharArray(latin1);
    }

    private void testWriteStringCharArray(final Charset charset) throws IOException {
        final String smallString = "�Ahora?... �Ma�ana!";
        testWriteStringCharArray(charset, "".toCharArray());
        testWriteStringCharArray(charset, smallString.toCharArray());
        StringBuffer sb = new StringBuffer();
        while (sb.length() < 100) {
            sb.append(smallString);
        }
        final String largeString = sb.toString();
        testWriteStringCharArray(charset, largeString.toCharArray());
    }

    private void testWriteStringCharArray(final Charset charset, final char[] string)
            throws IOException {
        final CharsetEncoder charencoder = charset.newEncoder();
        charencoder.onMalformedInput(CodingErrorAction.REPLACE);
        charencoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        ByteBuffer encoded = charencoder.encode(CharBuffer.wrap(string));
        final byte[] encodedContent = new byte[encoded.limit()];
        encoded.get(encodedContent);

        final ByteArrayWriteChannel writeChannel = TestData.getWriteChannel();
        final int bufferSize = 30;
        writer = new DefaultBxmlOutputStream(writeChannel, bufferSize, NATIVE_ORDER);

        writer.setCharactersEncoding(charset);
        writer.writeString(string, 0, string.length);
        writer.close();

        // sure the count is a SmallNum
        byte[] expected = new byte[1 + encodedContent.length];
        expected[0] = (byte) encodedContent.length;
        System.arraycopy(encodedContent, 0, expected, 1, encodedContent.length);
        byte[] writtenContent = writeChannel.getWrittenContent();
        assertArrayEquals(expected, writtenContent);
    }

    /**
     * Test method for {@link DefaultBxmlOutputStream#writeHeader(org.gvsig.bxml.stream.io.Header)}.
     */
    @Test
    public void testWriteHeader() throws IOException {
        final Header defaultHeader = Header.DEFAULT;
        testWriteHeader(defaultHeader);

        Flags flags = Flags
                .valueOf(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN, true, true, true);
        Charset charsEncoding = Charset.forName("UTF-16LE");
        Header header = Header.valueOf(flags, Compression.GZIP, charsEncoding);
        testWriteHeader(header);
    }

    private void testWriteHeader(final Header defaultHeader) throws IOException {
        final ByteArrayWriteChannel writeChannel = TestData.getWriteChannel();
        final int bufferSize = 30;
        writer = new DefaultBxmlOutputStream(writeChannel, bufferSize, NATIVE_ORDER);

        writer.writeHeader(defaultHeader);
        writer.close();

        final byte[] encodedHeader = writeChannel.getWrittenContent();
        BxmlInputStream reader = TestData.getTestInputStream(encodedHeader);

        ParsingUtils parsingUtils = new ParsingUtils(Charset.defaultCharset(), reader);

        final Header parsedHeader = parsingUtils.parseHeader();

        assertEquals(defaultHeader, parsedHeader);
    }

    @Test
    public void testWriteByte() throws IOException {
        for (int value = 0; value < 256; value++) {
            writer.writeByte(value);
        }
        writer.close();
        byte[] writtenContent = channel.getWrittenContent();
        assertEquals(256, writtenContent.length);
        for (int expected = 0; expected < 256; expected++) {
            assertEquals(expected, writtenContent[expected] & 0xFF);
        }
    }

    @Test
    public void testWriteByteArray() throws IOException {
        final byte[] pageWrite = new byte[256];
        for (int value = 0; value < 256; value++) {
            pageWrite[value] = (byte) (value & 0xFF);
        }
        writer.writeByte(pageWrite, 0, 50);
        assertEquals(50, writer.getPosition());

        writer.writeByte(pageWrite, 50, 50);
        assertEquals(100, writer.getPosition());

        writer.writeByte(pageWrite, 100, 156);
        assertEquals(256, writer.getPosition());

        writer.close();
        byte[] writtenContent = channel.getWrittenContent();
        Assert.assertArrayEquals(pageWrite, writtenContent);
    }

    @Test
    public void testWriteDoubleLE() throws IOException {
        testPutDouble(LITTLE_ENDIAN);
    }

    @Test
    public void testWriteDoubleBE() throws IOException {
        testPutDouble(BIG_ENDIAN);
    }

    private void testPutDouble(final ByteOrder order) throws IOException {
        final int writeCount = 100;
        final byte[] expected = new byte[ValueType.DOUBLE_BYTE_COUNT * writeCount];

        ByteBuffer wrapper = ByteBuffer.wrap(expected);
        writer.setEndianess(order);
        wrapper.order(order);

        for (double value = 0; value < writeCount; value++) {
            writer.writeDouble(value);
            wrapper.putDouble(value);
        }
        writer.close();

        byte[] writtenContent = channel.getWrittenContent();
        Assert.assertArrayEquals(expected, writtenContent);
    }

    @Test
    public void testWriteDoubleArrayLE() throws IOException {
        testPutDoubleArray(LITTLE_ENDIAN);
    }

    @Test
    public void testWriteDoubleArrayBE() throws IOException {
        testPutDoubleArray(BIG_ENDIAN);
    }

    private void testPutDoubleArray(ByteOrder order) throws IOException {
        final byte[] expected = new byte[ValueType.DOUBLE_BYTE_COUNT * 256];
        final double[] pageWrite = new double[256];
        final ByteBuffer wrapper = ByteBuffer.wrap(expected);
        wrapper.order(order);
        writer.setEndianess(order);

        for (int value = 0; value < 256; value++) {
            wrapper.putDouble(value);
            pageWrite[value] = value;
        }

        writer.writeDouble(pageWrite, 0, 50);
        assertEquals(50 * DOUBLE_BYTE_COUNT, writer.getPosition());

        writer.writeDouble(pageWrite, 50, 50);
        assertEquals(100 * DOUBLE_BYTE_COUNT, writer.getPosition());

        writer.writeDouble(pageWrite, 100, 156);
        assertEquals(256 * DOUBLE_BYTE_COUNT, writer.getPosition());

        writer.close();
        byte[] writtenContent = channel.getWrittenContent();
        Assert.assertArrayEquals(expected, writtenContent);
    }

    @Test
    public void testWriteFloatLE() throws IOException {
        testPutFloat(LITTLE_ENDIAN);
    }

    @Test
    public void testWriteFloatBE() throws IOException {
        testPutFloat(BIG_ENDIAN);
    }

    private void testPutFloat(final ByteOrder order) throws IOException {
        final int writeCount = 100;
        final byte[] expected = new byte[ValueType.FLOAT_BYTE_COUNT * writeCount];

        ByteBuffer wrapper = ByteBuffer.wrap(expected);
        writer.setEndianess(order);
        wrapper.order(order);

        for (float value = 0; value < writeCount; value++) {
            writer.writeFloat(value);
            wrapper.putFloat(value);
        }
        writer.close();

        byte[] writtenContent = channel.getWrittenContent();
        Assert.assertArrayEquals(expected, writtenContent);
    }

    @Test
    public void testWriteFloatArrayLE() throws IOException {
        testPutFloatArray(LITTLE_ENDIAN);
    }

    @Test
    public void testWriteFloatArrayBE() throws IOException {
        testPutFloatArray(BIG_ENDIAN);
    }

    private void testPutFloatArray(ByteOrder order) throws IOException {
        final byte[] expected = new byte[FLOAT_BYTE_COUNT * 256];
        final float[] pageWrite = new float[256];
        final ByteBuffer wrapper = ByteBuffer.wrap(expected);
        wrapper.order(order);
        writer.setEndianess(order);

        for (int value = 0; value < 256; value++) {
            wrapper.putFloat(value);
            pageWrite[value] = value;
        }

        writer.writeFloat(pageWrite, 0, 50);
        assertEquals(50 * FLOAT_BYTE_COUNT, writer.getPosition());

        writer.writeFloat(pageWrite, 50, 50);
        assertEquals(100 * FLOAT_BYTE_COUNT, writer.getPosition());

        writer.writeFloat(pageWrite, 100, 156);
        assertEquals(256 * FLOAT_BYTE_COUNT, writer.getPosition());

        writer.close();
        byte[] writtenContent = channel.getWrittenContent();
        Assert.assertArrayEquals(expected, writtenContent);
    }

    @Test
    public void testWriteIntIntLE() throws IOException {
        testPutInt(LITTLE_ENDIAN);
    }

    @Test
    public void testWriteIntIntBE() throws IOException {
        testPutInt(BIG_ENDIAN);
    }

    private void testPutInt(final ByteOrder order) throws IOException {
        final int writeCount = 100;
        final byte[] expected = new byte[ValueType.INTEGER_BYTE_COUNT * writeCount];

        ByteBuffer wrapper = ByteBuffer.wrap(expected);
        writer.setEndianess(order);
        wrapper.order(order);

        for (int value = 0; value < writeCount; value++) {
            writer.writeInt(value);
            wrapper.putInt(value);
        }
        writer.close();

        byte[] writtenContent = channel.getWrittenContent();
        Assert.assertArrayEquals(expected, writtenContent);
    }

    @Test
    public void testWriteIntArrayLE() throws IOException {
        testPutIntArray(LITTLE_ENDIAN);
    }

    @Test
    public void testWriteIntArrayBE() throws IOException {
        testPutIntArray(BIG_ENDIAN);
    }

    private void testPutIntArray(ByteOrder order) throws IOException {
        final byte[] expected = new byte[INTEGER_BYTE_COUNT * 256];
        final int[] pageWrite = new int[256];
        final ByteBuffer wrapper = ByteBuffer.wrap(expected);
        wrapper.order(order);
        writer.setEndianess(order);

        for (int value = 0; value < 256; value++) {
            wrapper.putInt(value);
            pageWrite[value] = value;
        }

        writer.writeInt(pageWrite, 0, 50);
        assertEquals(50 * INTEGER_BYTE_COUNT, writer.getPosition());

        writer.writeInt(pageWrite, 50, 50);
        assertEquals(100 * INTEGER_BYTE_COUNT, writer.getPosition());

        writer.writeInt(pageWrite, 100, 156);
        assertEquals(256 * INTEGER_BYTE_COUNT, writer.getPosition());

        writer.close();
        byte[] writtenContent = channel.getWrittenContent();
        Assert.assertArrayEquals(expected, writtenContent);
    }

    @Test
    public void testWriteLongLE() throws IOException {
        testPutLong(LITTLE_ENDIAN);
    }

    @Test
    public void testWriteLongBE() throws IOException {
        testPutLong(BIG_ENDIAN);
    }

    private void testPutLong(final ByteOrder order) throws IOException {
        final int writeCount = 100;
        final byte[] expected = new byte[ValueType.LONG_BYTE_COUNT * writeCount];

        ByteBuffer wrapper = ByteBuffer.wrap(expected);
        writer.setEndianess(order);
        wrapper.order(order);

        for (long value = 0; value < writeCount; value++) {
            writer.writeLong(value);
            wrapper.putLong(value);
        }
        writer.close();

        byte[] writtenContent = channel.getWrittenContent();
        Assert.assertArrayEquals(expected, writtenContent);
    }

    @Test
    public void testWriteLongArrayLE() throws IOException {
        testPutLongArray(LITTLE_ENDIAN);
    }

    @Test
    public void testWriteLongArrayBE() throws IOException {
        testPutLongArray(BIG_ENDIAN);
    }

    private void testPutLongArray(ByteOrder order) throws IOException {
        final byte[] expected = new byte[LONG_BYTE_COUNT * 256];
        final long[] pageWrite = new long[256];
        final ByteBuffer wrapper = ByteBuffer.wrap(expected);
        wrapper.order(order);
        writer.setEndianess(order);

        for (int value = 0; value < 256; value++) {
            wrapper.putLong(value);
            pageWrite[value] = value;
        }

        writer.writeLong(pageWrite, 0, 50);
        assertEquals(50 * LONG_BYTE_COUNT, writer.getPosition());

        writer.writeLong(pageWrite, 50, 50);
        assertEquals(100 * LONG_BYTE_COUNT, writer.getPosition());

        writer.writeLong(pageWrite, 100, 156);
        assertEquals(256 * LONG_BYTE_COUNT, writer.getPosition());

        writer.close();
        byte[] writtenContent = channel.getWrittenContent();
        Assert.assertArrayEquals(expected, writtenContent);
    }

    @Test
    public void testWriteShortLE() throws IOException {
        testPutShort(LITTLE_ENDIAN);
    }

    @Test
    public void testWriteShortBE() throws IOException {
        testPutShort(BIG_ENDIAN);
    }

    private void testPutShort(final ByteOrder order) throws IOException {
        final int writeCount = Short.MAX_VALUE - Short.MIN_VALUE;
        final byte[] expected = new byte[ValueType.SHORT_BYTE_COUNT * writeCount];

        ByteBuffer wrapper = ByteBuffer.wrap(expected);
        writer.setEndianess(order);
        wrapper.order(order);

        for (short value = Short.MIN_VALUE; value < Short.MAX_VALUE; value++) {
            writer.writeShort(value);
            wrapper.putShort(value);
        }
        writer.close();

        byte[] writtenContent = channel.getWrittenContent();
        Assert.assertArrayEquals(expected, writtenContent);
    }

    @Test
    public void testWriteShorttArrayLE() throws IOException {
        testPutShorttArray(LITTLE_ENDIAN);
    }

    @Test
    public void testWriteShorttArrayBE() throws IOException {
        testPutShorttArray(BIG_ENDIAN);
    }

    private void testPutShorttArray(ByteOrder bo) throws IOException {
        final byte[] expected = new byte[SHORT_BYTE_COUNT * 256];
        final short[] pageWrite = new short[256];
        final ByteBuffer wrapper = ByteBuffer.wrap(expected);
        wrapper.order(bo);
        writer.setEndianess(bo);

        for (short value = 0; value < 256; value++) {
            wrapper.putShort(value);
            pageWrite[value] = value;
        }

        writer.writeShort(pageWrite, 0, 50);
        assertEquals(50 * SHORT_BYTE_COUNT, writer.getPosition());

        writer.writeShort(pageWrite, 50, 50);
        assertEquals(100 * SHORT_BYTE_COUNT, writer.getPosition());

        writer.writeShort(pageWrite, 100, 156);
        assertEquals(256 * SHORT_BYTE_COUNT, writer.getPosition());

        writer.close();
        byte[] writtenContent = channel.getWrittenContent();
        Assert.assertArrayEquals(expected, writtenContent);
    }

    @Test
    public void testWriteUShortLE() throws IOException {
        testPutUShort(LITTLE_ENDIAN);
    }

    @Test
    public void testWriteUShortBE() throws IOException {
        testPutUShort(BIG_ENDIAN);
    }

    private void testPutUShort(final ByteOrder order) throws IOException {
        final int ushortMaxValue = ValueType.UShortCode.getUpperLimit().intValue();
        final int writeCount = 1 + ushortMaxValue;
        final byte[] expected = new byte[ValueType.USHORT_BYTE_COUNT * writeCount];

        ByteBuffer wrapper = ByteBuffer.wrap(expected);
        writer.setEndianess(order);
        wrapper.order(order);

        for (int value = 0; value <= ushortMaxValue; value++) {
            writer.writeUShort(value);
            wrapper.putShort((short) value);
        }
        writer.close();

        byte[] writtenContent = channel.getWrittenContent();
        Assert.assertArrayEquals(expected, writtenContent);
    }

    @Test
    public void testWriteUShortArrayLE() throws IOException {
        testPutUShortArray(LITTLE_ENDIAN);
    }

    @Test
    public void testWriteUShortArrayBE() throws IOException {
        testPutUShortArray(BIG_ENDIAN);
    }

    private void testPutUShortArray(ByteOrder bo) throws IOException {
        final byte[] expected = new byte[USHORT_BYTE_COUNT * 256];
        final int[] pageWrite = new int[256];
        final ByteBuffer wrapper = ByteBuffer.wrap(expected);
        wrapper.order(bo);
        writer.setEndianess(bo);

        for (int value = 0; value < 256; value++) {
            wrapper.putShort((short) value);
            pageWrite[value] = value;
        }

        writer.writeUShort(pageWrite, 0, 50);
        assertEquals(50 * USHORT_BYTE_COUNT, writer.getPosition());

        writer.writeUShort(pageWrite, 50, 50);
        assertEquals(100 * USHORT_BYTE_COUNT, writer.getPosition());

        writer.writeUShort(pageWrite, 100, 156);
        assertEquals(256 * USHORT_BYTE_COUNT, writer.getPosition());

        writer.close();
        byte[] writtenContent = channel.getWrittenContent();
        Assert.assertArrayEquals(expected, writtenContent);
    }

}
