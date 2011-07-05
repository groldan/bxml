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
package org.gvsig.bxml.stream;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * An convenient wrapper class subclassing {@link BxmlStreamReader} by overriding only the needed
 * methods.
 * <p>
 * The methods in this class delegate to the ones in the wrapped implementation.
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class BxmlStreamReaderAdapter implements BxmlStreamReader {

    protected final BxmlStreamReader impl;

    /**
     * 
     */
    public BxmlStreamReaderAdapter(final BxmlStreamReader wrapped) {
        this.impl = wrapped;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#close()
     */
    public void close() throws IOException {
        impl.close();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#standAloneIsSet()
     */
    public boolean standAloneIsSet() {
        return impl.standAloneIsSet();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#isLittleEndian()
     */
    public boolean isLittleEndian() {
        return impl.isLittleEndian();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#isNamespaceAware()
     */
    public boolean isNamespaceAware() {
        return impl.isNamespaceAware();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#isValidated()
     */
    public boolean isValidated() {
        return impl.isValidated();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#isOpen()
     */
    public boolean isOpen() {
        return impl.isOpen();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getCharset()
     */
    public Charset getCharset() {
        return impl.getCharset();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getEventType()
     */
    public EventType getEventType() {
        return impl.getEventType();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getXmlVersion()
     */
    public String getXmlVersion() {
        return impl.getXmlVersion();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#require(org.gvsig.bxml.stream.EventType,
     *      java.lang.String, java.lang.String)
     */
    public void require(EventType type, String namespaceUri, String localName)
            throws IllegalStateException {
        impl.require(type, namespaceUri, localName);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        return impl.hasNext();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#isStandalone()
     */
    public boolean isStandalone() throws IllegalStateException {
        return impl.isStandalone();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getPrefixes()
     */
    public Set<String> getPrefixes() {
        return impl.getPrefixes();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getPrefix(java.lang.String)
     */
    public String getPrefix(String namespaceURI) {
        return impl.getPrefix(namespaceURI);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(String prefix) {
        return impl.getNamespaceURI(prefix);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getAttributeCount()
     */
    public int getAttributeCount() {
        return impl.getAttributeCount();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getAttributeValue(java.lang.String,
     *      java.lang.String)
     */
    public String getAttributeValue(String namespaceURI, String localName) {
        return impl.getAttributeValue(namespaceURI, localName);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getAttributeValue(int)
     */
    public String getAttributeValue(int index) {
        return impl.getAttributeValue(index);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getAttributeName(int)
     */
    public QName getAttributeName(final int index) {
        return impl.getAttributeName(index);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getElementName()
     */
    public QName getElementName() {
        return impl.getElementName();
    }

    /**
     * @throws IOException
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getElementPosition()
     */
    public long getElementPosition() throws IOException {
        return impl.getElementPosition();
    }

    /**
     * @throws IOException
     * @see org.gvsig.bxml.stream.BxmlStreamReader#supportsRandomAccess()
     */
    public boolean supportsRandomAccess() throws IOException {
        return impl.supportsRandomAccess();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#setPosition(long)
     */
    public EventType setPosition(final long position) throws IOException {
        return impl.setPosition(position);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getBooleanValue()
     */
    public boolean getBooleanValue() throws IOException {
        return impl.getBooleanValue();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getByteValue()
     */
    public int getByteValue() throws IOException, IllegalArgumentException {
        return impl.getByteValue();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getStringValue()
     */
    public String getStringValue() throws IOException {
        return impl.getStringValue();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(boolean[], int, int)
     */
    public void getValue(boolean[] dst, int offset, int length) throws IOException {
        impl.getValue(dst, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(byte[], int, int)
     */
    public void getValue(byte[] dst, int offset, int length) throws IOException {
        impl.getValue(dst, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(int[], int, int)
     */
    public void getValue(int[] dst, int offset, int length) throws IOException {
        impl.getValue(dst, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(long[], int, int)
     */
    public void getValue(long[] dst, int offset, int length) throws IOException {
        impl.getValue(dst, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(float[], int, int)
     */
    public void getValue(float[] dst, int offset, int length) throws IOException {
        impl.getValue(dst, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(double[], int, int)
     */
    public void getValue(double[] dst, int offset, int length) throws IOException {
        impl.getValue(dst, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValueReadCount()
     */
    public int getValueReadCount() {
        return impl.getValueReadCount();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValueCount()
     */
    public int getValueCount() throws IllegalStateException {
        return impl.getValueCount();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#next()
     */
    public EventType next() throws IOException {
        return impl.next();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#nextTag()
     */
    public EventType nextTag() throws IOException {
        return impl.nextTag();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getDoubleValue()
     */
    public double getDoubleValue() throws IOException {
        return impl.getDoubleValue();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getFloatValue()
     */
    public float getFloatValue() throws IOException {
        return impl.getFloatValue();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getIntValue()
     */
    public int getIntValue() throws IOException {
        return impl.getIntValue();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getLongValue()
     */
    public long getLongValue() throws IOException {
        return impl.getLongValue();
    }

}
