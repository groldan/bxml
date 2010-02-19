package org.gvsig.bxml.stream.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

/**
 * Internal helper class to encode strings in a given charset encoding scheme
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
final class StringEncoder {
    private final BxmlOutputStream writer;

    private final Charset charset;

    private final Counts counts;

    private final CharsetEncoder encoder;

    private byte[] encodedStringBuffer;

    private ByteBuffer out;

    /**
     * Constructs a StringEncoder that encodes strings into the given {@code charset} and writes
     * them to the given {@code writer}
     * 
     * @param writer
     *            the {@link BxmlOutputStream} where to send the byte representations of the strings
     *            encoded by this string encoder
     * @param charset
     *            the character set thie encoder encodes strings into
     * @param counts
     *            the counts writer helper used to prepend the string lengths to the string literals
     */
    public StringEncoder(BxmlOutputStream writer, final Charset charset, final Counts counts) {
        this.writer = writer;
        this.charset = charset;
        this.counts = counts;
        this.encoder = charset.newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        expandBuffer(4096);
    }

    private void expandBuffer(final int length) {
        dispose();
        encodedStringBuffer = Buffers.newByteArray(length);
        out = ByteBuffer.wrap(encodedStringBuffer);
    }

    public void dispose() {
        if (encodedStringBuffer != null) {
            Buffers.returnArray(encodedStringBuffer);
        }
    }

    public Charset getCharset() {
        return charset;
    }

    public void encode(final CharBuffer in) throws IOException {
        // this is somewhat hacky, but encoding the empty string in UTF-16 is giving
        // 2 bytes and we don't need any content to be written
        if (in.length() == 0) {
            counts.writeCount(0, this.writer);
            return;
        }
        {
            final long maxPossibleLength = (long) (Math.ceil(encoder.maxBytesPerChar()) * in
                    .length());
            if (Integer.MAX_VALUE < maxPossibleLength) {
                throw new IllegalArgumentException("String too long");
            }
            if (out.capacity() < maxPossibleLength) {
                expandBuffer((int) maxPossibleLength);
            }
        }
        out.position(0);
        out.limit(out.capacity());
        encoder.encode(in, out, true);
        final int encodedStringByteLength = out.position();
        counts.writeCount(encodedStringByteLength, this.writer);

        writer.writeByte(encodedStringBuffer, 0, encodedStringByteLength);
    }
}