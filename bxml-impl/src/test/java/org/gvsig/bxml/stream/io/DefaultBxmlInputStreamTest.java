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
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.gvsig.bxml.stream.io.TestData.getHeader;
import static org.gvsig.bxml.stream.io.TestData.getReadChannel;
import static org.gvsig.bxml.stream.io.TestData.getTestInputStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

import org.gvsig.bxml.stream.io.Header.Compression;
import org.gvsig.bxml.stream.io.Header.Flags;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test suite for {@link DefaultBxmlInputStream}.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id:NioBxmlInputStreamTest.java 237 2007-12-09 19:11:15Z groldan $
 */
public class DefaultBxmlInputStreamTest {

    private Header defaultHeader;

    @Before
    public void setUp() throws Exception {
        defaultHeader = Header.DEFAULT;
    }

    @After
    public void tearDown() throws Exception {
        defaultHeader = null;
    }

    @Test
    public void testGetHeader() throws IOException {
        byte[] defaultHeader = getHeader();
        BxmlInputStream reader = getTestInputStream(defaultHeader);
        Header header = reader.getHeader();
        assertNotNull(header);
        assertEquals(Header.DEFAULT, header);
    }

    /**
     * <pre>
     * &lt;code&gt;
     *  TrailerToken {                          // last token of every file
     *     TokenType tokenType = 0x32;          // token-type code
     *     byte id[4] = { 0x01, 'T', 'R', 0x00 }; // id
     *     StringTableIndex stringIndex;        // index of string tables
     *     IndexTableIndex indexIndex;          // index of index-tables
     *     int tokenLength;                     // length of this token
     *  }
     *  StringTableIndex {      // index of string-table fragments
     *     Bool isUsed;         // flag for whether this is active
     *     Count nFragments;    // number of fragments in index
     *     StringTableIndexEntry fragments[nFragments]; // string tables
     *  }
     *  StringTableIndexEntry {     // string-table index fragment
     *     Count nStringsDefined;   // number of strings defined in frag.
     *     Count fileOffset;        // file offset to string-table token
     *  }
     *  IndexTableIndex {       // index of index tables
     *     byte isUsed;         // flag for whether this is active
     *     Count nEntries;      // number of index-tables
     *     IndexTableIndexEntry entries[nEntries]; // index-table indexes
     *  }
     *  IndexTableIndexEntry {  // entry for index
     *     String xpathExpr;    // XPath expression that is indexed
     *     Count fileOffset;    // file offset of index-table token
     *  }
     * &lt;/code&gt;
     * </pre>
     */
    @Test
    public void testReadEmptyTrailer() throws IOException {
        // record reader calls
        // TODO: re implement trailer reading test
        // expect(mockReader.getPosition()).andReturn(1001L); // ask for current position
        // mockReader.getByte(aryEq(new byte[4]), eq(0), eq(4));
        // expectLastCall().andAnswer(new IAnswer<Object>() {
        // public Object answer() throws Throwable {
        // byte[] buff = (byte[]) getCurrentArguments()[0];
        // buff[0] = 0x01;
        // buff[1] = 'T';
        // buff[2] = 'R';
        // buff[3] = 0x00;
        // return null;
        // }
        // });
        // expect(mockReader.getByte()).andReturn(0x00);
        // expect(mockReader.getByte()).andReturn(0x00);
        // expect(mockReader.getInt()).andReturn(7);
        // expect(mockReader.getPosition()).andReturn(1007L);
        // replay(mockReader);
        // DefaultBxmlInputStream stream = new DefaultBxmlInputStream(defaultHeader, mockReader);
        //
        // TrailerToken trailer = stream.readTrailer();
        //
        // verify(mockReader);
        // assertEquals(1000L, trailer.getPosition());
    }

    @Test
    public void testReadTokenType() throws IOException {
        // the BxmlInputStream knows nothing regarding the correct structure
        // of a bxml document, it just knows how to read "primitives".
        // So there's no difference for it if the content is a sequence of
        // token type identifiers or whatever, as long as the header is well formed.
        // So we're going to use a sequence of token types to test them all at once...
        // even if the data stream makes no sense

        final TokenType[] tokenTypes = TokenType.values();
        final int tokenTypeCount = tokenTypes.length;
        final byte[] tokenTypeCodes = new byte[tokenTypeCount];
        for (int i = 0; i < tokenTypeCount; i++) {
            TokenType tokenType = tokenTypes[i];
            tokenTypeCodes[i] = (byte) tokenType.getCode();
        }

        BxmlInputStream bxmlInputStream = getTestInputStream(tokenTypeCodes);

        // stream created, header read, positioned at first token
        for (TokenType tokenType : tokenTypes) {
            TokenType readTokenType = bxmlInputStream.readTokenType();
            Assert.assertSame(tokenType, readTokenType);
        }
    }

    /**
     * Assert getString behaves as expected and respects the current {@link Charset}
     */
    @Test
    public void testReadString() throws IOException {
        testReadString("US-ASCII", "�Ma�ana?");
        testReadString("US-ASCII", "�Ahora!");

        testReadString("UTF-8", "�Ma�ana?");
        testReadString("UTF-8", "�Ahora!");

        testReadString("UTF-16", "�Ma�ana?");
        testReadString("UTF-16", "�Ahora!");

        testReadString("UTF-16LE", "�Ma�ana?");
        testReadString("UTF-16LE", "�Ahora!");

        testReadString("UTF-16BE", "�Ma�ana?");
        testReadString("UTF-16BE", "�Ahora!");

        testReadString("ISO-8859-1", "�Ma�ana?");
        testReadString("ISO-8859-1", "�Ahora!");
    }

    private void testReadString(final String charsetName, final String string) throws IOException {
        if (!Charset.isSupported(charsetName)) {
            // charset not supported, can't test
            return;
        }
        final Charset charset = Charset.forName(charsetName);

        // create a header with the given charset
        final Flags flags = Flags.valueOf(ByteOrder.nativeOrder(), ByteOrder.nativeOrder(), false,
                false, false);
        final Header header = Header.valueOf(flags, Compression.NO_COMPRESSION, charset);

        final CharsetEncoder encoder = charset.newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        ByteBuffer encoded = encoder.encode(CharBuffer.wrap(string));
        final byte[] string1Bytes = new byte[encoded.limit()];
        encoded.get(string1Bytes);
        final int lengthString1 = string1Bytes.length;
        byte[] content = new byte[1 + lengthString1];
        content[0] = (byte) lengthString1;
        System.arraycopy(string1Bytes, 0, content, 1, lengthString1);

        CharsetDecoder decoder = charset.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPLACE);
        decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

        final String expected1 = decoder.decode(ByteBuffer.wrap(string1Bytes)).toString();

        final ReadableByteChannel channel = getReadChannel(content);
        DefaultBxmlInputStream streamReader = new DefaultBxmlInputStream(header, channel, 1024);
        final String readString1 = streamReader.readString();

        assertEquals(charsetName, expected1, readString1);
    }

    /**
     * Decode a String whose length in bytes is greater than the internal ByteBuffer capacity
     * 
     * @throws IOException
     */
    @Test
    public void testDecodeStringBufferOverFlow() throws IOException {
        String testString = "01234567890123456789";
        final int bufferSize = 5;
        CharsetDecoder charsetDecoder;
        byte[] content;
        {
            Charset charset = Charset.forName("US-ASCII");
            charsetDecoder = charset.newDecoder();
            ByteBuffer encoded = charset.encode(testString);
            content = new byte[20];
            encoded.get(content);
        }
        ReadableByteChannel channel = getReadChannel(content);
        BxmlInputStream reader = new DefaultBxmlInputStream(defaultHeader, channel, bufferSize);
        CharBuffer charBuffer = CharBuffer.allocate(100);
        reader.decode(charsetDecoder, charBuffer, 20);
        charBuffer.flip();

        String decodedString = charBuffer.toString();
        assertEquals(testString, decodedString);
    }

    /**
     * Decode a String whose length in bytes is lower than the internal ByteBuffer capacity
     * 
     * @throws IOException
     */
    @Test
    public void testDecodeString() throws IOException {
        final String testString = "01234567890123456789";
        final int bufferSize = 20;
        CharsetDecoder charsetDecoder;
        byte[] content = new byte[bufferSize];
        {
            Charset charset = Charset.forName("US-ASCII");
            charsetDecoder = charset.newDecoder();
            ByteBuffer encoded = charset.encode(testString);
            encoded.get(content, 0, 20);
        }
        ReadableByteChannel channel = getReadChannel(content);
        DefaultBxmlInputStream reader = new DefaultBxmlInputStream(defaultHeader, channel,
                bufferSize);
        CharBuffer charBuffer = CharBuffer.allocate(100);
        reader.decode(charsetDecoder, charBuffer, 5);
        charBuffer.flip();

        String decodedString = charBuffer.toString();
        assertEquals("01234", decodedString);
    }

    /**
     * Verify setting the byte order makes reading multi-byte primitives correctly
     * 
     * @throws IOException
     */
    @Test
    public void testByteOrder() throws IOException {
        byte[] bigEndianContent = new byte[1024];
        ByteBuffer buff = ByteBuffer.wrap(bigEndianContent);
        buff.order(BIG_ENDIAN);
        buff.putLong(1000);
        buff.putDouble(0.01);

        BxmlInputStream reader = getTestInputStream(bigEndianContent, BIG_ENDIAN);
        assertEquals(1000, reader.readLong());
        assertEquals(0.01, reader.readDouble(), 0.0001);

        byte[] littleEndianContent = new byte[1024];
        ByteBuffer leBuff = ByteBuffer.wrap(littleEndianContent);
        leBuff.order(LITTLE_ENDIAN);
        leBuff.putLong(1000);
        leBuff.putDouble(0.01);

        reader = getTestInputStream(littleEndianContent, LITTLE_ENDIAN);
        assertEquals(1000, reader.readLong());
        assertEquals(0.01, reader.readDouble(), 0.0001);
    }

    /**
     * Ensure dispose() closes the underlying channel and subsequent io operations throw IOException
     * 
     * @throws IOException
     */
    @Test
    public void testClose() throws IOException {
        ReadableByteChannel mockReadChannel = createMock(ReadableByteChannel.class);
        // expect channel.close() to be called
        mockReadChannel.close();
        replay(mockReadChannel);
        DefaultBxmlInputStream reader = new DefaultBxmlInputStream(defaultHeader, mockReadChannel,
                1024);
        assertTrue(reader.isOpen());
        reader.close();

        assertFalse(reader.isOpen());
        // try an io operation when disposed
        try {
            reader.readByte();
            fail("Expected EOFexception when calling a read method and the channel was closed");
        } catch (EOFException e) {
            assertTrue(true); // OK
        }
        verify(mockReadChannel);
    }

    @Test
    public void testReadByte() throws IOException {
        byte[] content = { 0x00, 0x01, 0x02, 0x03, (byte) 0xff };
        BxmlInputStream reader = getTestInputStream(content);
        assertEquals(0x00, reader.readByte());
        assertEquals(0x01, reader.readByte());
        assertEquals(0x02, reader.readByte());
        assertEquals(0x03, reader.readByte());
        int i = reader.readByte();
        assertEquals(255, i);
    }

    @Test
    public void testReadByteArray() throws IOException {
        byte[] content = { 0x00, 0x01, 0x02, 0x03, (byte) 0xff };
        BxmlInputStream reader = getTestInputStream(content);
        byte[] dst = new byte[4];
        dst[3] = 127;

        reader.readByte(dst, 0, 3);
        assertEquals(0x00, dst[0]);
        assertEquals(0x01, dst[1]);
        assertEquals(0x02, dst[2]);
        assertEquals(127, dst[3]);
        try {
            reader.readByte(dst, 0, 6);
            fail("expected EOFException, requested more bytes than available");
        } catch (EOFException e) {
            assertTrue(true);
        }
    }

    /**
     * Ensure readByte(byte[]...) works as expected even when the requested length is greater than
     * the internal buffer size
     * 
     * @throws IOException
     */
    @Test
    public void testReadByteArrayBufferOverflow() throws IOException {
        byte[] content = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 };

        ReadableByteChannel channel = getReadChannel(content);
        final int bufferSize = 4;
        DefaultBxmlInputStream reader = new DefaultBxmlInputStream(Header.DEFAULT, channel,
                bufferSize);
        byte[] dst = new byte[content.length];

        reader.readByte(dst, 0, dst.length);
        Assert.assertArrayEquals(content, dst);
        // check post-condition
        assertEquals(dst.length, reader.getPosition());
    }

    @Test
    public void testReadShortLE() throws IOException {
        testReadShort(LITTLE_ENDIAN);
    }

    @Test
    public void testReadShortBE() throws IOException {
        testReadShort(BIG_ENDIAN);
    }

    private void testReadShort(final ByteOrder order) throws IOException {
        ByteBuffer content = ByteBuffer.allocate(1024);
        content.order(order);
        content.putShort(Short.MIN_VALUE);
        content.putShort((short) 0);
        content.putShort(Short.MAX_VALUE);
        BxmlInputStream reader = getTestInputStream(content, order);
        assertEquals(Short.MIN_VALUE, reader.readShort());
        assertEquals(0, reader.readShort());
        assertEquals(Short.MAX_VALUE, reader.readShort());
    }

    @Test
    public void testReadIntLE() throws IOException {
        testGetInt(LITTLE_ENDIAN);
    }

    @Test
    public void testReadIntBE() throws IOException {
        testGetInt(BIG_ENDIAN);
    }

    private void testGetInt(ByteOrder order) throws IOException {
        ByteBuffer content = ByteBuffer.allocate(1024);
        content.order(order);
        content.putInt(Integer.MIN_VALUE);
        content.putInt(0);
        content.putInt(Integer.MAX_VALUE);
        BxmlInputStream reader = getTestInputStream(content, order);
        assertEquals(Integer.MIN_VALUE, reader.readInt());
        assertEquals(0, reader.readInt());
        assertEquals(Integer.MAX_VALUE, reader.readInt());
    }

    @Test
    public void testReadLongLE() throws IOException {
        testGetLong(LITTLE_ENDIAN);
    }

    @Test
    public void testReadLongBE() throws IOException {
        testGetLong(BIG_ENDIAN);
    }

    private void testGetLong(ByteOrder bo) throws IOException {
        ByteBuffer content = ByteBuffer.allocate(1024);
        content.order(bo);
        content.putLong(Long.MIN_VALUE);
        content.putLong(0);
        content.putLong(Long.MAX_VALUE);
        BxmlInputStream reader = getTestInputStream(content, bo);
        assertEquals(Long.MIN_VALUE, reader.readLong());
        assertEquals(0, reader.readLong());
        assertEquals(Long.MAX_VALUE, reader.readLong());
    }

    @Test
    public void testReadDoubleLE() throws IOException {
        testGetDouble(LITTLE_ENDIAN);
    }

    @Test
    public void testReadDoubleBE() throws IOException {
        testGetDouble(BIG_ENDIAN);
    }

    private void testGetDouble(ByteOrder bo) throws IOException {
        ByteBuffer content = ByteBuffer.allocate(1024);
        content.order(bo);
        content.putDouble(Double.MIN_VALUE);
        content.putDouble(0);
        content.putDouble(Double.MAX_VALUE);
        BxmlInputStream reader = getTestInputStream(content, bo);
        assertEquals(Double.MIN_VALUE, reader.readDouble(), 1E-6);
        assertEquals(0, reader.readDouble(), 1E-6);
        assertEquals(Double.MAX_VALUE, reader.readDouble(), 1E-6);
    }

    @Test
    public void testReadFloatLE() throws IOException {
        testGetFloat(LITTLE_ENDIAN);
    }

    @Test
    public void testReadFloatBE() throws IOException {
        testGetFloat(BIG_ENDIAN);
    }

    private void testGetFloat(ByteOrder bo) throws IOException {
        ByteBuffer content = ByteBuffer.allocate(1024);
        content.order(bo);
        content.putFloat(Float.MIN_VALUE);
        content.putFloat(0);
        content.putFloat(Float.MAX_VALUE);
        BxmlInputStream reader = getTestInputStream(content, bo);
        assertEquals(Float.MIN_VALUE, reader.readFloat(), 1E-6);
        assertEquals(0, reader.readFloat(), 1E-6);
        assertEquals(Float.MAX_VALUE, reader.readFloat(), 1E-6);
    }

    @Test
    public void testReadUShortLE() throws IOException {
        testGetUShort(LITTLE_ENDIAN);
    }

    @Test
    public void testReadUShortBE() throws IOException {
        testGetUShort(BIG_ENDIAN);
    }

    private void testGetUShort(ByteOrder bo) throws IOException {
        ByteBuffer content = ByteBuffer.allocate(1024);
        content.order(bo);
        content.putShort((short) 0);
        content.put((byte) 0xFF);
        content.put((byte) 0xFF);
        BxmlInputStream reader = getTestInputStream(content, bo);
        assertEquals(0, reader.readUShort());
        assertEquals(65535, reader.readUShort());
    }

    @Test
    public void testReadDoubleArrayBE() throws IOException {
        testReadDoubleArray(BIG_ENDIAN);
    }

    @Test
    public void testReadDoubleArrayLE() throws IOException {
        testReadDoubleArray(LITTLE_ENDIAN);
    }

    public void testReadDoubleArray(final ByteOrder byteOrder) throws IOException {
        ByteBuffer content = ByteBuffer.allocate(1024);
        content.order(byteOrder);

        content.putDouble(Double.MIN_VALUE);
        content.putDouble(0);
        content.putDouble(Double.MAX_VALUE);

        BxmlInputStream reader = getTestInputStream(content, byteOrder);
        double[] dest = new double[5];
        reader.readDouble(dest, 1, 3);
        assertEquals(Double.MIN_VALUE, dest[1], 1E-6);
        assertEquals(0, dest[2], 1E-6);
        assertEquals(Double.MAX_VALUE, dest[3], 1E-6);
        try {
            reader.readDouble(dest, 0, 1);
            fail("Expected EOF exception");
        } catch (EOFException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testReadFloatArrayLE() throws IOException {
        testGetFloatArray(LITTLE_ENDIAN);
    }

    @Test
    public void testReadFloatArrayBE() throws IOException {
        testGetFloatArray(BIG_ENDIAN);
    }

    private void testGetFloatArray(ByteOrder bo) throws IOException {
        ByteBuffer content = ByteBuffer.allocate(1024);
        content.order(bo);
        content.putFloat(Float.MIN_VALUE);
        content.putFloat(0);
        content.putFloat(Float.MAX_VALUE);

        BxmlInputStream reader = getTestInputStream(content, bo);
        float[] dest = new float[5];
        reader.readFloat(dest, 1, 3);
        assertEquals(Float.MIN_VALUE, dest[1], 1E-6);
        assertEquals(0, dest[2], 1E-6);
        assertEquals(Float.MAX_VALUE, dest[3], 1E-6);
        try {
            reader.readFloat(dest, 0, 1);
            fail("Expected EOF exception");
        } catch (EOFException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testReadIntArrayLE() throws IOException {
        testGetIntArray(LITTLE_ENDIAN);
    }

    @Test
    public void testReadIntArrayBE() throws IOException {
        testGetIntArray(BIG_ENDIAN);
    }

    private void testGetIntArray(ByteOrder bo) throws IOException {
        ByteBuffer content = ByteBuffer.allocate(1024);
        content.order(bo);
        content.putInt(Integer.MIN_VALUE);
        content.putInt(0);
        content.putInt(Integer.MAX_VALUE);

        BxmlInputStream reader = getTestInputStream(content, bo);
        int[] dest = new int[5];
        reader.readInt(dest, 1, 3);
        assertEquals(Integer.MIN_VALUE, dest[1]);
        assertEquals(0, dest[2]);
        assertEquals(Integer.MAX_VALUE, dest[3]);
        try {
            reader.readInt(dest, 0, 1);
            fail("Expected EOF exception");
        } catch (EOFException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testReadLongArrayLE() throws IOException {
        testGetLongArray(LITTLE_ENDIAN);
    }

    @Test
    public void testReadLongArrayBE() throws IOException {
        testGetLongArray(BIG_ENDIAN);
    }

    private void testGetLongArray(ByteOrder bo) throws IOException {
        ByteBuffer content = ByteBuffer.allocate(1024);
        content.order(bo);
        content.putLong(Long.MIN_VALUE);
        content.putLong(0);
        content.putLong(Long.MAX_VALUE);

        BxmlInputStream reader = getTestInputStream(content, bo);
        long[] dest = new long[5];
        reader.readLong(dest, 1, 3);
        assertEquals(Long.MIN_VALUE, dest[1]);
        assertEquals(0, dest[2]);
        assertEquals(Long.MAX_VALUE, dest[3]);
        try {
            reader.readLong(dest, 0, 1);
            fail("Expected EOF exception");
        } catch (EOFException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testReadShortArrayLE() throws IOException {
        testGetShortArray(LITTLE_ENDIAN);
    }

    @Test
    public void testReadShortArrayBE() throws IOException {
        testGetShortArray(BIG_ENDIAN);
    }

    private void testGetShortArray(ByteOrder bo) throws IOException {
        ByteBuffer content = ByteBuffer.allocate(1024);
        content.order(bo);
        content.putShort(Short.MIN_VALUE);
        content.putShort((short) 0);
        content.putShort(Short.MAX_VALUE);

        BxmlInputStream reader = getTestInputStream(content, bo);
        int[] dest = new int[5];
        reader.readShort(dest, 1, 3);
        assertEquals(Short.MIN_VALUE, dest[1]);
        assertEquals(0, dest[2]);
        assertEquals(Short.MAX_VALUE, dest[3]);
        try {
            reader.readShort(dest, 0, 1);
            fail("Expected EOF exception");
        } catch (EOFException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testReadUShortArrayLE() throws IOException {
        testGetUShortArray(LITTLE_ENDIAN);
    }

    @Test
    public void testReadUShortArrayBE() throws IOException {
        testGetUShortArray(BIG_ENDIAN);
    }

    private void testGetUShortArray(ByteOrder bo) throws IOException {
        ByteBuffer content = ByteBuffer.allocate(1024);
        content.order(bo);
        content.putShort((short) 0);
        content.put((byte) 0xFF);
        content.put((byte) 0xFF);

        BxmlInputStream reader = getTestInputStream(content, bo);
        int[] dest = new int[5];
        reader.readUShort(dest, 1, 2);
        assertEquals(0, dest[1]);
        assertEquals(65535, dest[2]);
        try {
            reader.readUShort(dest, 0, 5);
            fail("Expected EOF exception");
        } catch (EOFException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testGetPosition() throws IOException {
        byte[] content = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        ReadableByteChannel channel = getReadChannel(content);
        // tell newInstance to use a very small read buffer, just 3 bytes
        DefaultBxmlInputStream reader = new DefaultBxmlInputStream(Header.DEFAULT, channel, 4);
        assertEquals(0, reader.getPosition());
        reader.readShort();
        assertEquals(2, reader.getPosition());
        reader.readInt();
        assertEquals(6, reader.getPosition());
        reader.readByte();
        assertEquals(7, reader.getPosition());
        reader.close();
        assertEquals(-1, reader.getPosition());
    }

    @Test
    public void testSkip() throws IOException {
        byte[] content = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        ReadableByteChannel channel = getReadChannel(content);
        // tell newInstance to use a very small read buffer, just 3 bytes
        DefaultBxmlInputStream reader = new DefaultBxmlInputStream(Header.DEFAULT, channel, 3);
        reader.skip(2);
        assertEquals(0x02, reader.readByte());
        reader.skip(1);
        reader.skip(1);
        // skip more than the buffer capacity, it should work
        reader.skip(4);
        assertEquals(9, reader.getPosition());
        try {
            reader.skip(2);
            fail("Expected EOF exception trying to skip beyond the content length");
        } catch (EOFException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testSkipString() throws IOException {
        // first byte is the string length
        byte[] content = { 0x03, 'A', 'B', 'C', 0x04, 0x05 };
        ReadableByteChannel channel = getReadChannel(content);
        // tell newInstance to use a very small read buffer, just 3 bytes
        DefaultBxmlInputStream reader = new DefaultBxmlInputStream(Header.DEFAULT, channel, 3);
        reader.skipString();
        assertEquals(0x04, reader.readByte());
    }
}
