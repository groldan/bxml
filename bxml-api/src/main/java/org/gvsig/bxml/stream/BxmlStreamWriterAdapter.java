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

import javax.xml.namespace.QName;

/**
 * An convenient wrapper class subclassing {@link BxmlStreamWriter} by overriding only the needed
 * methods.
 * <p>
 * The methods in this class delegate to the ones in the wrapped implementation.
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class BxmlStreamWriterAdapter implements BxmlStreamWriter {

    protected final BxmlStreamWriter impl;

    public BxmlStreamWriterAdapter(final BxmlStreamWriter impl) {
        this.impl = impl;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#close()
     */
    public void close() throws IOException {
        impl.close();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#flush()
     */
    public void flush() throws IOException {
        impl.flush();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getCurrentElementName()
     */
    public String getCurrentElementName() {
        return impl.getCurrentElementName();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getCurrentElementNamespace()
     */
    public String getCurrentElementNamespace() {
        return impl.getCurrentElementNamespace();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#require(org.gvsig.bxml.stream.EventType,
     *      java.lang.String, java.lang.String)
     */
    public void require(EventType type, String namespaceUri, String localName)
            throws IllegalStateException {
        impl.require(type, namespaceUri, localName);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getEncodingOptions()
     */
    public EncodingOptions getEncodingOptions() {
        return impl.getEncodingOptions();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getLastEvent()
     */
    public EventType getLastEvent() {
        return impl.getLastEvent();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getTagDeep()
     */
    public int getTagDeep() {
        return impl.getTagDeep();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getLastTagEvent()
     */
    public EventType getLastTagEvent() {
        return impl.getLastTagEvent();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#isOpen()
     */
    public boolean isOpen() {
        return impl.isOpen();
    }

    /**
     * @throws IOException
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#setDefaultNamespace(java.lang.String)
     */
    public void setDefaultNamespace(String defaultNamespaceUri) throws IOException {
        impl.setDefaultNamespace(defaultNamespaceUri);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getPrefix(java.lang.String)
     */
    public String getPrefix(String uri) {
        return impl.getPrefix(uri);
    }

    /**
     * @throws IOException
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeNamespace(String, String)
     */
    public void writeNamespace(String prefix, String namespaceUri) throws IOException {
        impl.writeNamespace(prefix, namespaceUri);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#setSchemaLocation(java.lang.String,
     *      java.lang.String)
     */
    public void setSchemaLocation(String namespaceUri, String schemaLocationUri) {
        impl.setSchemaLocation(namespaceUri, schemaLocationUri);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#startArray(org.gvsig.bxml.stream.EventType, int)
     */
    public void startArray(EventType valueType, int arrayLength) throws IOException {
        impl.startArray(valueType, arrayLength);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#endArray()
     */
    public void endArray() throws IOException {
        impl.endArray();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#isArrayInProgress()
     */
    public boolean isArrayInProgress() {
        return impl.isArrayInProgress();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getValueLength()
     */
    public long getValueLength() {
        return impl.getValueLength();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getWrittenValueCount()
     */
    public long getWrittenValueCount() {
        return impl.getWrittenValueCount();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeComment(java.lang.String)
     */
    public void writeComment(String commentContent) throws IOException {
        impl.writeComment(commentContent);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeEndAttributes()
     */
    public void writeEndAttributes() throws IOException {
        impl.writeEndAttributes();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeEndDocument()
     */
    public void writeEndDocument() throws IOException {
        impl.writeEndDocument();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeEndElement()
     */
    public void writeEndElement() throws IOException {
        impl.writeEndElement();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartAttribute(java.lang.String,
     *      java.lang.String)
     */
    public void writeStartAttribute(String namespaceUri, String localName) throws IOException {
        impl.writeStartAttribute(namespaceUri, localName);
    }

    /**
     * @param qname
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartAttribute(javax.xml.namespace.QName)
     */
    public void writeStartAttribute(QName qname) throws IOException {
        impl.writeStartAttribute(qname);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartDocument()
     */
    public void writeStartDocument() throws IOException {
        impl.writeStartDocument();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartElement(java.lang.String,
     *      java.lang.String)
     */
    public void writeStartElement(String namespaceUri, String localName) throws IOException {
        impl.writeStartElement(namespaceUri, localName);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartElement(javax.xml.namespace.QName)
     */
    public void writeStartElement(QName qname) throws IOException {
        impl.writeStartElement(qname);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(java.lang.String)
     */
    public void writeValue(String value) throws IOException {
        impl.writeValue(value);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(char[], int, int)
     */
    public void writeValue(char[] chars, int offset, int length) throws IOException {
        impl.writeValue(chars, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(int)
     */
    public void writeValue(int value) throws IOException {
        impl.writeValue(value);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(long)
     */
    public void writeValue(long value) throws IOException {
        impl.writeValue(value);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(float)
     */
    public void writeValue(float value) throws IOException {
        impl.writeValue(value);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(double)
     */
    public void writeValue(double value) throws IOException {
        impl.writeValue(value);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(boolean)
     */
    public void writeValue(boolean value) throws IOException {
        impl.writeValue(value);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(boolean[], int, int)
     */
    public void writeValue(boolean[] value, int offset, int length) throws IOException {
        impl.writeValue(value, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(byte)
     */
    public void writeValue(byte value) throws IOException {
        impl.writeValue(value);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(byte[], int, int)
     */
    public void writeValue(final byte[] value, final int offset, final int length)
            throws IOException {
        impl.writeValue(value, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(int[], int, int)
     */
    public void writeValue(int[] value, int offset, int length) throws IOException {
        impl.writeValue(value, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(long[], int, int)
     */
    public void writeValue(long[] value, int offset, int length) throws IOException {
        impl.writeValue(value, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(float[], int, int)
     */
    public void writeValue(float[] value, int offset, int length) throws IOException {
        impl.writeValue(value, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(double[], int, int)
     */
    public void writeValue(double[] value, int offset, int length) throws IOException {
        impl.writeValue(value, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getStringTableReference(java.lang.CharSequence)
     */
    public long getStringTableReference(final CharSequence stringValue) throws IOException {
        return impl.getStringTableReference(stringValue);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStringTableValue(long)
     */
    public void writeStringTableValue(long longValue) throws IOException {
        impl.writeStringTableValue(longValue);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#setWriteAttributeValueAsStringTable(java.lang.String)
     */
    public void setWriteAttributeValueAsStringTable(String qName) {
        impl.setWriteAttributeValueAsStringTable(qName);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#supportsStringTableValues()
     */
    public boolean supportsStringTableValues() {
        return impl.supportsStringTableValues();
    }

}
