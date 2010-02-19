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
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Abstract wrapper for a {@link BxmlOutputStream} that redirects all methods to the wrapped one so
 * subclasses may override the ones of interest.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public abstract class BxmlOutputStreamWrapper implements BxmlOutputStream {

    protected final BxmlOutputStream wrapped;

    public BxmlOutputStreamWrapper(final BxmlOutputStream wrapped) {
        this.wrapped = wrapped;
    }

    public void close() throws IOException {
        wrapped.close();
    }

    public void flush() throws IOException {
        wrapped.flush();
    }

    public int getCachedSize() {
        return wrapped.getCachedSize();
    }

    public ByteOrder getEndianess() {
        return wrapped.getEndianess();
    }

    public long getPosition() {
        return wrapped.getPosition();
    }

    public boolean isAutoFlushing() {
        return wrapped.isAutoFlushing();
    }

    public boolean isOpen() {
        return wrapped.isOpen();
    }

    public void setAutoFlushing(boolean autoFlush) {
        wrapped.setAutoFlushing(autoFlush);
    }

    public void setCharactersEncoding(Charset charset) {
        wrapped.setCharactersEncoding(charset);
    }

    public void setEndianess(ByteOrder byteOrder) {
        wrapped.setEndianess(byteOrder);
    }

    public void setPosition(long newPosition) throws IOException {
        wrapped.setPosition(newPosition);
    }

    public void writeHeader(Header header) throws IOException {
        wrapped.writeHeader(header);
    }

    /**
     * @see org.gvsig.bxml.stream.io.BxmlOutputStream#writeTokenType(org.gvsig.bxml.stream.io.TokenType)
     */
    public void writeTokenType(TokenType tokenType) throws IOException {
        wrapped.writeTokenType(tokenType);
    }

    public void writeBoolean(boolean b) throws IOException {
        wrapped.writeBoolean(b);
    }

    public void writeBoolean(boolean[] buffer, int offset, int length) throws IOException {
        wrapped.writeBoolean(buffer, offset, length);
    }

    public void writeByte(int _byte) throws IOException {
        wrapped.writeByte(_byte);
    }

    public void writeByte(byte[] buffer, int offset, int length) throws IOException {
        wrapped.writeByte(buffer, offset, length);
    }

    public void writeCount(long count) throws IOException {
        wrapped.writeCount(count);
    }

    public void writeDouble(double _double) throws IOException {
        wrapped.writeDouble(_double);
    }

    public void writeDouble(double[] buffer, int offset, int length) throws IOException {
        wrapped.writeDouble(buffer, offset, length);
    }

    public void writeFloat(float _float) throws IOException {
        wrapped.writeFloat(_float);
    }

    public void writeFloat(float[] buffer, int offset, int length) throws IOException {
        wrapped.writeFloat(buffer, offset, length);
    }

    public void writeInt(int _int) throws IOException {
        wrapped.writeInt(_int);
    }

    public void writeInt(int[] buffer, int offset, int length) throws IOException {
        wrapped.writeInt(buffer, offset, length);
    }

    public void writeLong(long _long) throws IOException {
        wrapped.writeLong(_long);
    }

    public void writeLong(long[] buffer, int offset, int length) throws IOException {
        wrapped.writeLong(buffer, offset, length);
    }

    public void writeShort(short _short) throws IOException {
        wrapped.writeShort(_short);
    }

    public void writeShort(short[] buffer, int offset, int length) throws IOException {
        wrapped.writeShort(buffer, offset, length);
    }

    public void writeString(CharSequence string) throws IOException {
        wrapped.writeString(string);
    }

    public void writeString(char[] buffer, int offset, int length) throws IOException {
        wrapped.writeString(buffer, offset, length);
    }

    public void writeUShort(int ushort) throws IOException {
        wrapped.writeUShort(ushort);
    }

    public void writeUShort(int[] buffer, int offset, int length) throws IOException {
        wrapped.writeUShort(buffer, offset, length);
    }

}
