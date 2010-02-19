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
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;

import org.gvsig.bxml.stream.io.Header.Compression;
import org.gvsig.bxml.stream.io.Header.Flags;
import org.gvsig.bxml.stream.io.Header.Version;

/**
 * Utility class to parse out common BXML object structures out of a {@link BxmlInputStream}.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
class ParsingUtils {

    private final Counts counts;

    private final Charset charset;

    private final CharsetDecoder charsetDecoder;

    private BxmlInputStream reader;

    CharBuffer charBuffer;

    /**
     * Private constructor to force the use of this class as a pure utility class
     * 
     * @param charsetsEncoding
     *            the character scheme used to decode strings with
     * @param reader
     *            the low level reader where to get bytes from for this utility class parse methods
     */
    public ParsingUtils(final Charset charsetsEncoding, final BxmlInputStream reader) {
        this.charset = charsetsEncoding;
        charsetDecoder = charset.newDecoder();
        charsetDecoder.onMalformedInput(CodingErrorAction.REPORT);
        charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        this.counts = new Counts();
        this.reader = reader;
        this.charBuffer = CharBuffer.allocate(1024);
    }

    /**
     * Parses a String from the {@link BxmlInputStream} and returns the CharBuffer into which the
     * string was decoded.
     * <p>
     * This method is intended to be called by {@link DefaultBxmlInputStream}. Care shall be taken
     * since the returned CharBuffer constitutes this class internal state.
     * </p>
     * 
     * @return the internal charbuffer where the string was decoded
     * @throws IOException
     */
    public final CharBuffer parseString() throws IOException {
        // hope nobody sends a String larger than Integer.MAX_VALUE, that's a long string btw..
        final long count = counts.readCount(this.reader);
        assert count <= Integer.MAX_VALUE : "Can't create a string larger than Integer.MAX_VALUE";
        final int byteLength = (int) count;
        if (charBuffer.capacity() < byteLength) {
            // charBuffer = ByteBuffer.allocateDirect(4*byteLength).asCharBuffer();
            charBuffer = CharBuffer.allocate(Math.round(1.3f * byteLength));
        }
        charBuffer.position(0);
        charBuffer.limit(charBuffer.capacity());
        // we need to call reset to avoid a potential previous state to influence the current
        // decoding
        charsetDecoder.reset();
        reader.decode(charsetDecoder, charBuffer, byteLength);
        charBuffer.flip();

        return charBuffer;
    }

    /**
     * Parses a {@link Header} out of a {@link ReadStrategy}.
     * <p>
     * After this method successfully returns, the header object is parsed and the BufferedReader is
     * positioned to start reading content at the first byte after the end of the header.
     * </p>
     * 
     * @return
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public final Header parseHeader() throws IllegalArgumentException, IOException {
        // Identifier
        final int nonTextMark = reader.readByte();
        if (0x01 != nonTextMark) {
            throw new IllegalArgumentException(
                    "Provided input stream does not starts with the non text byte marker");
        }
        final byte[] name = new byte[5];
        reader.readByte(name, 0, 5);

        if (!Arrays.equals(new byte[] { 'B', 'X', 'M', 'L', 0x00 }, name)) {
            throw new IllegalArgumentException(
                    "Input stream does not contains the BXML format identifier");
        }
        final byte[] binaryCheck = new byte[3];
        reader.readByte(binaryCheck, 0, 3);

        if (!Arrays.equals(new byte[] { (byte) 0xFF, 0x0D, 0x0A }, binaryCheck)) {
            throw new IllegalArgumentException(
                    "Input stream does not contains the BXML binary check mark");
        }

        // Version
        int major = reader.readByte();
        int minor = reader.readByte();
        int point = reader.readByte();
        final Version version = Version.valueOf(major, minor, point);
        if (!Version.DEFAULT_VERSION.equals(version)) {
            throw new IllegalArgumentException("Unsupported version: " + version);
        }

        // flags
        final int flags1 = reader.readByte();
        final int flags2 = reader.readByte();
        if (0x00 != flags2) {
            throw new IllegalArgumentException(
                    "The flags2 field is unused at this version of the spec and shall have the value 0x00");
        }
        final Flags flags = Flags.valueOf(flags1);

        // Compression
        final int compressionCode = reader.readByte();
        Compression compression = Compression.valueOf(compressionCode);

        // requires this instance's charset to be US-ASCII
        final String charEncoding = parseString().toString();
        final Charset charset;
        try {
            charset = Charset.forName(charEncoding);
        } catch (UnsupportedCharsetException uce) {
            throw new IllegalArgumentException("Charset defined in header not supported: "
                    + charEncoding, uce);
        }

        Header header = Header.valueOf(version, flags, compression, charset);
        return header;
    }

    public final long readCount() throws IOException {
        return counts.readCount(this.reader);
    }

}
