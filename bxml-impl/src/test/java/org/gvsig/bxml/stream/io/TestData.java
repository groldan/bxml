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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

import org.gvsig.bxml.stream.io.Header.Compression;
import org.gvsig.bxml.stream.io.Header.Flags;

/**
 * Helper class to assist in creating BXML data structures for unit tests.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id:TestData.java 237 2007-12-09 19:11:15Z groldan $
 */
final class TestData {

    private TestData() {
        // no-op
    }

    /**
     * Returns a default {@link Header} binary representation with:
     * <ul>
     * <li>isLittleEndian = {@link ByteOrder#nativeOrder() == ByteOrder#LITTLE_ENDIAN}
     * <li>charsAreLittleEndian = false
     * <li>hasRandomAccessInfo = false
     * <li>hasStrictXmlStrings = false
     * <li>isValidated = false
     * <li>useCompression = false
     * <li>charsEncoding = "UTF-8"
     * </ul>
     * 
     * @return a default header binary representation
     */
    public static byte[] getHeader() {
        final boolean nativeOrderIsLE = ByteOrder.LITTLE_ENDIAN == ByteOrder.nativeOrder();
        final boolean charsAreLE = false;
        return getHeader(nativeOrderIsLE, charsAreLE, false, false, false, false, "UTF-8");
    }

    public static byte[] getHeader(boolean littleEndian, boolean charsAreLE,
            boolean hasRandomAccessInfo, boolean hasStrictXmlStrings, boolean isValidated,
            boolean useCompression, String charEncoding) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            // Identifier
            out.write(0x01);
            out.write(new byte[] { 'B', 'X', 'M', 'L', 0x00 });
            out.write(new byte[] { (byte) 0xFF, 0x0D, 0x0A });
            // Version
            out.write(new byte[] { 0, 0, 8 });
            // build flags (see table in bxml spec, page 23)
            int flags1 = 0x00;
            if (littleEndian) {
                flags1 |= 0x01;
            }
            if (charsAreLE) {
                flags1 |= 0x02;
            }
            if (hasRandomAccessInfo) {
                flags1 |= 0x04;
            }
            if (hasStrictXmlStrings) {
                flags1 |= 0x08;
            }
            if (isValidated) {
                flags1 |= 0x10;
            }
            out.write(flags1);
            // second set of bit flags, not used at this version of the spec
            int flags2 = 0x00;
            out.write(flags2);

            // Compression
            int compression = useCompression ? 0x01 : 0x00;
            out.write(compression);
            // charsEncoding, a pure ACII string
            byte[] charsEncodingBytes = charEncoding.getBytes("ASCII");
            // put up front the string length. It's a Count type which in this
            // case occupies a single byte as it falls in the SmallNum category
            out.write(charEncoding.length());
            out.write(charsEncodingBytes);

            out.close();
        } catch (IOException e) {
            throw new RuntimeException("Can't happen!");
        }
        byte[] header = out.toByteArray();
        return header;
    }

    public static ReadableByteChannel getReadChannel(byte[] content) {
        return Channels.newChannel(new ByteArrayInputStream(content));
    }

    /**
     * Returns a for-test-only {@link WritableByteChannel}
     * 
     * @return
     */
    public static ByteArrayWriteChannel getWriteChannel() {
        return new ByteArrayWriteChannel();
    }

    /**
     * Test support class implementing {@link WritableByteChannel} that allows to retrieve the
     * written channel content through {@link #getWrittenContent()}.
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    public static class ByteArrayWriteChannel implements WritableByteChannel {
        private ByteArrayOutputStream out;

        private boolean open;

        public ByteArrayWriteChannel() {
            out = new ByteArrayOutputStream(1024);
            open = true;
        }

        public byte[] getWrittenContent() {
            return out.toByteArray();
        }

        public int write(ByteBuffer src) throws IOException {
            int remaining = src.remaining();
            if (remaining > 0) {
                byte[] bytes = new byte[remaining];
                src.get(bytes);
                out.write(bytes);
                return remaining;
            }
            return -1;
        }

        public void close() throws IOException {
            out.close();
            open = false;
        }

        public boolean isOpen() {
            return open;
        }
    }

    public static BxmlInputStream getTestInputStream(byte[] content, ByteOrder byteOrder)
            throws IOException {
        ByteOrder charactersEndianess = Header.DEFAULT.getFlags().getCharactersEndianess();
        boolean hasRandomAccessInfo = Header.DEFAULT.getFlags().hasRandomAccessInfo();
        boolean hasStrictXmlStrings = Header.DEFAULT.getFlags().hasStrictXmlStrings();
        boolean validated = Header.DEFAULT.getFlags().isValidated();

        Flags flags = Flags.valueOf(byteOrder, charactersEndianess, hasRandomAccessInfo,
                hasStrictXmlStrings, validated);

        Compression compression = Header.DEFAULT.getCompression();
        Charset charsEncoding = Header.DEFAULT.getCharactersEncoding();
        Header header = Header.valueOf(flags, compression, charsEncoding);
        return getTestInputStream(content, header);
    }

    /**
     * @param content
     *            byte buffer where the desired content has been written. Will be flipped in order
     *            to read the contents.
     * @param order
     * @return
     * @throws IOException
     */
    public static BxmlInputStream getTestInputStream(ByteBuffer content, ByteOrder order)
            throws IOException {
        content.flip();
        int size = content.remaining();
        byte[] bytes = new byte[size];
        content.get(bytes);
        return getTestInputStream(bytes, order);
    }

    public static BxmlInputStream getTestInputStream(byte[] content) throws IOException {
        Header header = Header.DEFAULT;
        return getTestInputStream(content, header);
    }

    public static BxmlInputStream getTestInputStream(byte[] content, Header header)
            throws IOException {
        ReadableByteChannel channel = getReadChannel(content);
        DefaultBxmlInputStream in = new DefaultBxmlInputStream(header, channel, 1024);
        return in;
    }

}
