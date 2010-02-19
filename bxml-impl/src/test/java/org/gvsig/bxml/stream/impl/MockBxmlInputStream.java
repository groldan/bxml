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
package org.gvsig.bxml.stream.impl;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

import org.gvsig.bxml.stream.io.BxmlInputStream;
import org.gvsig.bxml.stream.io.Header;
import org.gvsig.bxml.stream.io.TokenType;
import org.gvsig.bxml.stream.io.TrailerToken;

class MockBxmlInputStream implements BxmlInputStream {

    private DataInputStream reader;

    public MockBxmlInputStream(final byte[] content) {
        ByteArrayInputStream in = new ByteArrayInputStream(content);
        DataInputStream dataInput = new DataInputStream(in);
        this.reader = dataInput;
    }

    public void close() throws IOException {
        reader.close();
        reader = null;
    }

    public boolean isOpen() {
        return reader == null;
    }

    public int readByte() throws IOException {
        return reader.read();
    }

    public void readByte(byte[] buffer, int offset, int count) throws IOException {
        reader.readFully(buffer, offset, count);
    }

    // unimplemented methods follow

    public Header getHeader() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public long getPosition() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean readBoolean() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void readBoolean(boolean[] buffer, int offset, int count) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public long readCount() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public double readDouble() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void readDouble(double[] buffer, int offset, int count) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public float readFloat() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void readFloat(float[] buffer, int offset, int count) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int readInt() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void readInt(int[] buffer, int offset, int count) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public long readLong() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void readLong(long[] buffer, int offset, int count) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public short readShort() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void readShort(int[] buffer, int offset, int count) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String readString() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // public void readString(char[] dst, int offset) throws IOException {
    // throw new UnsupportedOperationException("Not yet implemented");
    // }

    public TokenType readTokenType() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public TrailerToken readTrailer() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int readUShort() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void readUShort(int[] buffer, int offset, int count) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void skip(int byteCount) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void skipString() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void decode(CharsetDecoder charsetDecoder, CharBuffer charBuffer, int length)
            throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void setPosition(long position) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean supportsRandomAccess() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public long getSize() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}