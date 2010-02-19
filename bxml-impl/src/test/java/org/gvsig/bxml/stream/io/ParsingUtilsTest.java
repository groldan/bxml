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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import org.gvsig.bxml.stream.io.Header.Compression;
import org.gvsig.bxml.stream.io.Header.Flags;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test suite for {@link ParseUtils} utility class.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class ParsingUtilsTest {

    private static final Charset ascii = Charset.forName("US-ASCII");

    @Before
    public void setUp() throws Exception {
        // do nothing
    }

    @After
    public void tearDown() throws Exception {
        // do nothing
    }

    /**
     * Parse a "default" header.
     * 
     * @throws IOException
     */
    @Test
    public void testParseHeaderDefault() throws IOException {
        final byte[] headerBytes = TestData.getHeader();
        final BxmlInputStream reader = TestData.getTestInputStream(headerBytes);
        final ParsingUtils ParsingUtils = new ParsingUtils(ascii, reader);
        Header parsedHeader = ParsingUtils.parseHeader();
        assertNotNull(parsedHeader);
        assertSame(Header.Identifier.INSTANCE, parsedHeader.getIdentifier());
        assertEquals(Header.Version.DEFAULT_VERSION, parsedHeader.getVersion());

        Flags flags = parsedHeader.getFlags();

        final ByteOrder defaultByteOrder = ByteOrder.nativeOrder();
        assertEquals(defaultByteOrder, flags.getEndianess());
        assertEquals(ByteOrder.BIG_ENDIAN, flags.getCharactersEndianess());
        assertEquals(false, flags.hasRandomAccessInfo());
        assertEquals(false, flags.hasStrictXmlStrings());
        assertEquals(false, flags.isValidated());

        assertEquals(Compression.NO_COMPRESSION, parsedHeader.getCompression());

        Charset expectedCharset = Charset.forName("UTF-8");
        Charset charactersEncoding = parsedHeader.getCharactersEncoding();
        assertEquals(expectedCharset, charactersEncoding);
    }

    @Test
    public void testParseHeaderEndianess() throws IOException {
        byte[] header;
        boolean isLittleEndian = true;
        header = TestData.getHeader(isLittleEndian, false, false, false, false, false, "UTF-8");
        BxmlInputStream reader = TestData.getTestInputStream(header);
        ParsingUtils ParsingUtils = new ParsingUtils(ascii, reader);

        Header parsedHeader = ParsingUtils.parseHeader();

        Assert.assertSame(ByteOrder.LITTLE_ENDIAN, parsedHeader.getFlags().getEndianess());

        isLittleEndian = false;
        header = TestData.getHeader(isLittleEndian, false, false, false, false, false, "UTF-8");
        reader = TestData.getTestInputStream(header);
        ParsingUtils = new ParsingUtils(ascii, reader);

        parsedHeader = ParsingUtils.parseHeader();

        Assert.assertSame(ByteOrder.BIG_ENDIAN, parsedHeader.getFlags().getEndianess());
    }

    @Test
    public void testParseHeaderCharsEndianess() throws IOException {
        byte[] header;
        boolean charsAreLittleEndian = true;
        header = TestData.getHeader(false, charsAreLittleEndian, false, false, false, false,
                "UTF-8");
        BxmlInputStream reader = TestData.getTestInputStream(header);
        ParsingUtils ParsingUtils = new ParsingUtils(ascii, reader);

        Header parsedHeader = ParsingUtils.parseHeader();

        ByteOrder charactersEndianess = parsedHeader.getFlags().getCharactersEndianess();
        Assert.assertSame(ByteOrder.LITTLE_ENDIAN, charactersEndianess);

        charsAreLittleEndian = false;
        header = TestData.getHeader(false, charsAreLittleEndian, false, false, false, false,
                "UTF-8");
        reader = TestData.getTestInputStream(header);
        ParsingUtils = new ParsingUtils(ascii, reader);

        parsedHeader = ParsingUtils.parseHeader();
        charactersEndianess = parsedHeader.getFlags().getCharactersEndianess();
        Assert.assertSame(ByteOrder.BIG_ENDIAN, charactersEndianess);
    }

    @Test
    public void testParseHeaderHasRandomAccessInfo() throws IOException {
        byte[] header;

        boolean hasRandomAccessInfo = true;
        header = TestData
                .getHeader(false, false, hasRandomAccessInfo, false, false, false, "UTF-8");
        BxmlInputStream reader = TestData.getTestInputStream(header);
        ParsingUtils ParsingUtils = new ParsingUtils(ascii, reader);

        Header parsedHeader = ParsingUtils.parseHeader();
        boolean parsedValue = parsedHeader.getFlags().hasRandomAccessInfo();
        Assert.assertEquals(hasRandomAccessInfo, parsedValue);

        hasRandomAccessInfo = false;
        header = TestData
                .getHeader(false, false, hasRandomAccessInfo, false, false, false, "UTF-8");
        reader = TestData.getTestInputStream(header);
        ParsingUtils = new ParsingUtils(ascii, reader);

        parsedHeader = ParsingUtils.parseHeader();
        parsedValue = parsedHeader.getFlags().hasRandomAccessInfo();
        Assert.assertEquals(hasRandomAccessInfo, parsedValue);
    }

    @Test
    public void testParseHeaderHasStrictXmlStrings() throws IOException {
        byte[] header;
        boolean hasStrictXmlStrings = true;
        header = TestData
                .getHeader(false, false, false, hasStrictXmlStrings, false, false, "UTF-8");
        BxmlInputStream readBuffer = TestData.getTestInputStream(header);
        ParsingUtils ParsingUtils = new ParsingUtils(ascii, readBuffer);

        Header parsedHeader = ParsingUtils.parseHeader();
        boolean parsedValue = parsedHeader.getFlags().hasStrictXmlStrings();
        Assert.assertEquals(hasStrictXmlStrings, parsedValue);

        hasStrictXmlStrings = false;
        header = TestData
                .getHeader(false, false, false, hasStrictXmlStrings, false, false, "UTF-8");
        readBuffer = TestData.getTestInputStream(header);
        ParsingUtils = new ParsingUtils(ascii, readBuffer);

        parsedHeader = ParsingUtils.parseHeader();
        parsedValue = parsedHeader.getFlags().hasStrictXmlStrings();
        Assert.assertEquals(hasStrictXmlStrings, parsedValue);
    }

    @Test
    public void testParseHeaderIsValidated() throws IOException {
        byte[] header;
        boolean isValidated = true;
        header = TestData.getHeader(false, false, false, false, isValidated, false, "UTF-8");
        BxmlInputStream reader = TestData.getTestInputStream(header);
        ParsingUtils ParsingUtils = new ParsingUtils(ascii, reader);

        Header parsedHeader = ParsingUtils.parseHeader();
        boolean parsedValue = parsedHeader.getFlags().isValidated();
        Assert.assertEquals(isValidated, parsedValue);

        isValidated = false;
        header = TestData.getHeader(false, false, false, false, isValidated, false, "UTF-8");
        reader = TestData.getTestInputStream(header);
        ParsingUtils = new ParsingUtils(ascii, reader);

        parsedHeader = ParsingUtils.parseHeader();
        parsedValue = parsedHeader.getFlags().isValidated();
        Assert.assertEquals(isValidated, parsedValue);
    }

    @Test
    public void testParseHeaderHasCompression() throws IOException {
        byte[] header;
        boolean compressed = true;
        header = TestData.getHeader(false, false, false, false, false, compressed, "UTF-8");
        BxmlInputStream reader = TestData.getTestInputStream(header);
        ParsingUtils ParsingUtils = new ParsingUtils(ascii, reader);

        Header parsedHeader = ParsingUtils.parseHeader();
        Compression parsedValue = parsedHeader.getCompression();
        Assert.assertEquals(Compression.GZIP, parsedValue);

        compressed = false;
        header = TestData.getHeader(false, false, false, false, false, compressed, "UTF-8");
        reader = TestData.getTestInputStream(header);
        ParsingUtils = new ParsingUtils(ascii, reader);

        parsedHeader = ParsingUtils.parseHeader();
        parsedValue = parsedHeader.getCompression();
        Assert.assertEquals(Compression.NO_COMPRESSION, parsedValue);
    }

    @Test
    public void testParseHeaderCharsEncoding() throws IOException {
        byte[] header;
        Charset expected = Charset.forName("UTF-8");
        header = TestData.getHeader(false, false, false, false, false, false, "UTF-8");
        BxmlInputStream reader = TestData.getTestInputStream(header);
        ParsingUtils ParsingUtils = new ParsingUtils(ascii, reader);

        Header parsedHeader = ParsingUtils.parseHeader();
        Charset parsedValue = parsedHeader.getCharactersEncoding();
        Assert.assertEquals(expected, parsedValue);

        expected = Charset.forName("UTF-16");
        header = TestData.getHeader(false, false, false, false, false, false, "UTF-16");

        reader = TestData.getTestInputStream(header);
        ParsingUtils = new ParsingUtils(ascii, reader);
        parsedHeader = ParsingUtils.parseHeader();

        parsedValue = parsedHeader.getCharactersEncoding();
        Assert.assertEquals(expected, parsedValue);

        expected = Charset.forName("UTF-16BE");
        header = TestData.getHeader(false, false, false, false, false, false, "UTF-16BE");

        reader = TestData.getTestInputStream(header);
        ParsingUtils = new ParsingUtils(ascii, reader);
        parsedHeader = ParsingUtils.parseHeader();

        parsedValue = parsedHeader.getCharactersEncoding();
        Assert.assertEquals(expected, parsedValue);

        expected = Charset.forName("UTF-16LE");

        header = TestData.getHeader(false, false, false, false, false, false, "UTF-16LE");

        reader = TestData.getTestInputStream(header);
        ParsingUtils = new ParsingUtils(ascii, reader);
        parsedHeader = ParsingUtils.parseHeader();

        parsedValue = parsedHeader.getCharactersEncoding();
        Assert.assertEquals(expected, parsedValue);

        String illegalCharsetName = "NOT_A_VALID_CHARSET";
        header = TestData.getHeader(false, false, false, false, false, false, illegalCharsetName);
        reader = TestData.getTestInputStream(header);
        ParsingUtils = new ParsingUtils(ascii, reader);
        try {
            ParsingUtils.parseHeader();
            fail("Obtaining a BXML scanner should have failed with an unsupported charset");
        } catch (IllegalArgumentException e) {
            // ok, that's expected
            Assert.assertTrue(true);
        }
    }

    /**
     * Assert the header parsing fails if the header is invalid
     * <p>
     * That is:
     * <ul>
     * <li>first byte is not the nonText mark (0x01)</li>
     * <li>bytes [1-5] does not equal to <code>{ 'B', 'X', 'M', 'L', 0x00 }</code></li>
     * <li>bytes [6-8] does not equal <code> {0xFF, 0x0D, 0x0A }</code></li>
     * <li>version is not equal to 0.0.8 (bytes [9-11] != <code> {0, 0, 8}}</code></li>
     * <li>byte 13 (flags2) is not equal to {@code 0x00}</li>
     * <li>byte 15 (character set length count) is {@code > 239} or {@code < 1}</li>
     * <li>charset name (bytes 16 - (16 + character set count) is not the name of the VM supported
     * {@link Charset}s</li>
     * </ul>
     * </p>
     */
    @Test
    public void testParseHeaderPreconditions() throws IOException {
        // try an incomplete header first.. less data than needed for a full header
        byte[] incompleteHeader;
        {
            final byte[] header = TestData.getHeader();
            incompleteHeader = new byte[14];
            System.arraycopy(header, 0, incompleteHeader, 0, 14);
        }
        BxmlInputStream reader = TestData.getTestInputStream(incompleteHeader);
        ParsingUtils ParsingUtils = new ParsingUtils(ascii, reader);
        try {
            ParsingUtils.parseHeader();
            fail("Expected EOFException");
        } catch (EOFException e) {
            Assert.assertTrue(true);
        }

        testHeaderParsingError(0, 0x00, "Expected IAE when non text marker is supplied");

        // "BCML" instead of "BXML"
        testHeaderParsingError(2, 'C',
                "Expected IAE when format name is not { 'B', 'X', 'M', 'L', 0x00 }");

        // 0x0A by 0x0B
        testHeaderParsingError(8, 0x0B, "Expected IAE when binaryCheck is not {0xFF, 0x0D, 0x0A }");

        // version 1.0.8 instead of 0.0.8
        testHeaderParsingError(9, 1, "Expected IAE when version is not 0.0.8");

        // flags2 != 0x00
        testHeaderParsingError(13, 0x01, "Expected IAE when flags2 is not 0x00");

        // "ZTF-8" instead of "UTF-8"
        testHeaderParsingError(16, 'Z',
                "Expected IAE when the charset name is not a supported VM charset");
    }

    private void testHeaderParsingError(final int byteIndexToReplace, final int replaceBy,
            final String failureMessage) throws IOException {
        final byte[] header = TestData.getHeader();
        header[byteIndexToReplace] = (byte) replaceBy; // "ZTF-8" instead of "UTF-8"
        BxmlInputStream reader = TestData.getTestInputStream(header);
        ParsingUtils ParsingUtils = new ParsingUtils(ascii, reader);
        try {
            ParsingUtils.parseHeader();
            fail(failureMessage);
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }
}
