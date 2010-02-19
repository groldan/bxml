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
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

public class LoggingBxmlInputStream extends BxmlInputStreamWrapper {

    private final Writer writer;

    public LoggingBxmlInputStream(final BxmlInputStream wrapped, final Writer logTo) {
        super(wrapped);
        this.writer = logTo;
    }

    private void log(String event) {
        try {
            writer.write(event);
            writer.write('\n');
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private <T> T log(String event, T o) {
        try {
            writer.write(event);
            writer.write(':');
            writer.write(String.valueOf(o));
            writer.write('\n');
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return o;
    }

    @Override
    public Header getHeader() {
        return log("header", wrapped.getHeader());
    }

    @Override
    public TokenType readTokenType() throws IOException {
        return log("Token [at pos " + wrapped.getPosition() + "]", wrapped.readTokenType());
    }

    @Override
    public String readString() throws IOException {
        return log("readString", wrapped.readString());
    }

    @Override
    public void decode(CharsetDecoder charsetDecoder, CharBuffer charBuffer, int length)
            throws IOException {
        wrapped.decode(charsetDecoder, charBuffer, length);
        log("decode(CharsetDecoder, CharBuffer, int [" + length + "])", charBuffer.toString());
    }

    @Override
    public void skipString() throws IOException {
        long currPos = wrapped.getPosition();
        wrapped.skipString();
        log("skipString from " + currPos + " to " + wrapped.getPosition());
    }

    @Override
    public TrailerToken readTrailer() throws IOException {
        return log("Trailer", wrapped.readTrailer());
    }

    public void setPosition(long position) throws IOException {
        wrapped.setPosition(position);
        log("Setting position to " + position + ": result position: " + wrapped.getPosition());
    }

    public boolean supportsRandomAccess() {
        return wrapped.supportsRandomAccess();
    }

    public long getSize() throws IOException, UnsupportedOperationException {
        return wrapped.getSize();
    }
}
