package org.gvsig.bxml.stream.io;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.nio.ByteOrder;

import org.gvsig.bxml.stream.io.Header.Compression;
import org.gvsig.bxml.stream.io.Header.Flags;
import org.gvsig.bxml.stream.io.Header.Identifier;
import org.junit.Test;

public class HeaderTest {

    @Test
    public void testDefaultHeaderFormat() {
        final Header header = Header.DEFAULT;
        final Identifier identifier = header.getIdentifier();
        assertSame(Header.Identifier.INSTANCE, identifier);
        assertArrayEquals(new byte[] { 'B', 'X', 'M', 'L', 0x00 }, identifier.getName());
        assertArrayEquals(new byte[] { (byte) 0xFF, 0x0D, 0x0A }, identifier.getBinaryCheck());

        final Header.Version version = header.getVersion();
        assertSame(Header.Version.DEFAULT_VERSION, version);

        final Compression compression = header.getCompression();
        assertSame(Compression.NO_COMPRESSION, compression);

        final Flags flags = header.getFlags();
        final int flags1 = flags.getFlags1();
        int expectedFlags1 = 0x00;
        // isLittleEndian = true
        expectedFlags1 |= 0x01;
        // charsAreLittleEndian = true
        // expectedFlags1 |= 0x02;
        // hasRandomAccessInfo = false
        // expectedFlags1 |= 0x10;

        final String msg = Integer.toBinaryString(expectedFlags1) + " != "
                + Integer.toBinaryString(flags1);
        assertEquals(msg, expectedFlags1, flags1);

        final int flags2 = flags.getFlags2();
        assertEquals(0x00, flags2);
    }

    @Test
    public void testFlagsValueOf() {
        final boolean hasRandomAccessInfo = true;
        final boolean hasStrictXmlStrings = true;
        final boolean isValidated = true;
        final ByteOrder endianess = ByteOrder.LITTLE_ENDIAN;
        final ByteOrder charsEndianess = ByteOrder.BIG_ENDIAN;

        Flags flags = Flags.valueOf(endianess, charsEndianess, hasRandomAccessInfo,
                hasStrictXmlStrings, isValidated);

        assertSame(endianess, flags.getEndianess());
        assertSame(charsEndianess, flags.getCharactersEndianess());
        assertEquals(hasRandomAccessInfo, flags.hasRandomAccessInfo());
        assertEquals(hasStrictXmlStrings, flags.hasStrictXmlStrings());
        assertEquals(isValidated, flags.isValidated());
    }

}
