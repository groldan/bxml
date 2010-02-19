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
import java.nio.charset.CharsetDecoder;

/**
 * Abstract wrapper for a {@link BxmlInputStream} that redirects all methods to the wrapped one so
 * subclasses may override the ones of interest.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public abstract class BxmlInputStreamWrapper implements BxmlInputStream {

    protected BxmlInputStream wrapped;

    public BxmlInputStreamWrapper(final BxmlInputStream wrapped) {
        this.wrapped = wrapped;
    }

    public void close() throws IOException {
        wrapped.close();
    }

    public boolean isOpen() {
        return wrapped.isOpen();
    }

    public Header getHeader() {
        return wrapped.getHeader();
    }

    public boolean readBoolean() throws IOException {
        return wrapped.readBoolean();
    }

    public void readBoolean(boolean[] buffer, int offset, int count) throws IOException {
        wrapped.readBoolean(buffer, offset, count);
    }

    public int readByte() throws IOException {
        return wrapped.readByte();
    }

    public void readByte(byte[] buffer, int offset, int count) throws IOException {
        wrapped.readByte(buffer, offset, count);
    }

    public long readCount() throws IOException {
        return wrapped.readCount();
    }

    public double readDouble() throws IOException {
        return wrapped.readDouble();
    }

    public void readDouble(double[] buffer, int offset, int count) throws IOException {
        wrapped.readDouble(buffer, offset, count);
    }

    public float readFloat() throws IOException {
        return wrapped.readFloat();
    }

    public void readFloat(float[] buffer, int offset, int count) throws IOException {
        wrapped.readFloat(buffer, offset, count);
    }

    public int readInt() throws IOException {
        return wrapped.readInt();
    }

    public void readInt(int[] buffer, int offset, int count) throws IOException {
        wrapped.readInt(buffer, offset, count);
    }

    public long readLong() throws IOException {
        return wrapped.readLong();
    }

    public void readLong(long[] buffer, int offset, int count) throws IOException {
        wrapped.readLong(buffer, offset, count);
    }

    public short readShort() throws IOException {
        return wrapped.readShort();
    }

    public void readShort(int[] buffer, int offset, int count) throws IOException {
        wrapped.readShort(buffer, offset, count);
    }

    public String readString() throws IOException {
        return wrapped.readString();
    }

    // public void readString(char[] dst, int offset) throws IOException {
    // wrapped.readString(dst, offset);
    // }

    public void decode(CharsetDecoder charsetDecoder, CharBuffer charBuffer, int length)
            throws IOException {
        wrapped.decode(charsetDecoder, charBuffer, length);
    }

    public TokenType readTokenType() throws IOException {
        return wrapped.readTokenType();
    }

    public int readUShort() throws IOException {
        return wrapped.readUShort();
    }

    public void readUShort(int[] buffer, int offset, int count) throws IOException {
        wrapped.readUShort(buffer, offset, count);
    }

    public long getPosition() {
        return wrapped.getPosition();
    }

    public void skip(int byteCount) throws IOException {
        wrapped.skip(byteCount);
    }

    public void skipString() throws IOException {
        wrapped.skipString();
    }

    public TrailerToken readTrailer() throws IOException {
        return wrapped.readTrailer();
    }

    public long getSize() throws IOException, UnsupportedOperationException {
        return wrapped.getSize();
    }

    public void setPosition(long position) throws IOException {
        wrapped.setPosition(position);
    }

    public boolean supportsRandomAccess() {
        return wrapped.supportsRandomAccess();
    }
}