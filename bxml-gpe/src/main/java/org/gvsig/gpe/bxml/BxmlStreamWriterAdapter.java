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
package org.gvsig.gpe.bxml;

import static org.gvsig.gpe.bxml.BxmlStreamReaderAdapter.bxmlToGpeEventMappings;
import static org.gvsig.gpe.bxml.BxmlStreamReaderAdapter.gpeToBxmlEventMappings;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlFactoryFinder;
import org.gvsig.bxml.stream.BxmlOutputFactory;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.gvsig.gpe.xml.stream.EventType;
import org.gvsig.gpe.xml.stream.IXmlStreamWriter;
import org.gvsig.gpe.xml.stream.XmlStreamException;

/**
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class BxmlStreamWriterAdapter implements IXmlStreamWriter {

    private BxmlStreamWriter w;

    public BxmlStreamWriterAdapter(final OutputStream out) throws XmlStreamException {
        final BxmlOutputFactory fac = BxmlFactoryFinder.newOutputFactory();
        try {
            this.w = fac.createSerializer(out);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * Tries to set a writer property.
     * <p>
     * The following are the only properties supported by thie binary xml writer:
     * <ul>
     * <li><strong>{@code org.gvsig.bxml.stream.BxmlStreamWriter.attribueValueReference}</strong>:
     * accumulative (can be set more than onece). Qualified name of an xml attribute whose value is
     * to be written as a StringTable reference (hence written only once for the whole document, or
     * rather from the point where this encoding hint is set onwards). For example {@code
     * writer.setProperty("org.gvsig.bxml.stream.BxmlStreamWriter.attribueValueReference",
     * "gml:srsName")}
     * </ul>
     * </p>
     * 
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#setProperty(java.lang.String,
     *      java.lang.String)
     */
    public boolean setProperty(final String propertyName, final String propertyValue) {
        if ("org.gvsig.bxml.stream.BxmlStreamWriter.attribueValueReference".equals(propertyName)) {
            w.setWriteAttributeValueAsStringTable(propertyValue);
            return true;
        }
        return false;
    }

    /**
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#close()
     */
    public void close() throws XmlStreamException {
        try {
            w.close();
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#endArray()
     */
    public void endArray() throws XmlStreamException {
        try {
            w.endArray();
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#flush()
     */
    public void flush() throws XmlStreamException {
        try {
            w.flush();
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    private EventType toGpeEventType(org.gvsig.bxml.stream.EventType lastEvent) {
        EventType gpeEvent = (EventType) bxmlToGpeEventMappings.get(lastEvent);
        return gpeEvent;
    }

    private org.gvsig.bxml.stream.EventType toBxmlEventType(EventType valueType) {
        org.gvsig.bxml.stream.EventType bxmlEvent = gpeToBxmlEventMappings.get(valueType);
        if (bxmlEvent == null) {
            throw new IllegalArgumentException("No mapping for bxml event type " + valueType
                    + " found");
        }
        return bxmlEvent;
    }

    /**
     * @return
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#getLastEvent()
     */
    public EventType getLastEvent() {
        return toGpeEventType(w.getLastEvent());
    }

    /**
     * @return
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#getLastTagEvent()
     */
    public EventType getLastTagEvent() {
        EventType tagEvent = toGpeEventType(w.getLastEvent());
        return tagEvent;
    }

    /**
     * @return
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#getTagDeep()
     */
    public int getTagDeep() {
        return w.getTagDeep();
    }

    /**
     * @return
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#getValueLength()
     */
    public long getValueLength() {
        return w.getValueLength();
    }

    /**
     * @return
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#getWrittenValueCount()
     */
    public long getWrittenValueCount() {
        return w.getWrittenValueCount();
    }

    /**
     * @return
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#isOpen()
     */
    public boolean isOpen() {
        return w.isOpen();
    }

    /**
     * @param defaultNamespaceUri
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#setDefaultNamespace(java.lang.String)
     */
    public void setDefaultNamespace(String defaultNamespaceUri) {
        try {
            w.setDefaultNamespace(defaultNamespaceUri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeNamespace(java.lang.String,
     *      java.lang.String)
     */
    public void writeNamespace(String prefix, String namespaceUri) throws XmlStreamException {
        try {
            w.writeNamespace(prefix, namespaceUri);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#getCurrentElementName()
     */
    public String getCurrentElementName() {
        return w.getCurrentElementName();
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#getCurrentElementNamespace()
     */
    public String getCurrentElementNamespace() {
        return w.getCurrentElementNamespace();
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#require(org.gvsig.gpe.xml.stream.EventType,
     *      java.lang.String, java.lang.String)
     */
    public void require(final EventType event, final String namespaceUri, final String localName)
            throws IllegalStateException {
        org.gvsig.bxml.stream.EventType type = event == null ? null : toBxmlEventType(event);
        w.require(type, namespaceUri, localName);

    }

    /**
     * @param namespaceUri
     * @param schemaLocationUri
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#setSchemaLocation(java.lang.String,
     *      java.lang.String)
     */
    public void setSchemaLocation(String namespaceUri, String schemaLocationUri) {
        w.setSchemaLocation(namespaceUri, schemaLocationUri);
    }

    /**
     * @param valueType
     * @param arrayLength
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#startArray(org.gvsig.gpe.xml.stream.EventType,
     *      int)
     */
    public void startArray(EventType valueType, int arrayLength) throws XmlStreamException {
        try {
            w.startArray(toBxmlEventType(valueType), arrayLength);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param commentContent
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeComment(java.lang.String)
     */
    public void writeComment(String commentContent) throws XmlStreamException {
        try {
            w.writeComment(commentContent);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeEndAttributes()
     */
    public void writeEndAttributes() throws XmlStreamException {
        try {
            w.writeEndAttributes();
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeEndDocument()
     */
    public void writeEndDocument() throws XmlStreamException {
        try {
            w.writeEndDocument();
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeEndElement()
     */
    public void writeEndElement() throws XmlStreamException {
        try {
            w.writeEndElement();
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param namespaceUri
     * @param localName
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeStartAttribute(java.lang.String,
     *      java.lang.String)
     */
    public void writeStartAttribute(String namespaceUri, String localName)
            throws XmlStreamException {
        try {
            w.writeStartAttribute(namespaceUri, localName);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param qname
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeStartAttribute(javax.xml.namespace.QName)
     */
    public void writeStartAttribute(QName qname) throws XmlStreamException {
        try {
            w.writeStartAttribute(qname);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeStartDocument()
     */
    public void writeStartDocument() throws XmlStreamException {
        try {
            w.writeStartDocument();
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param namespaceUri
     * @param localName
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeStartElement(java.lang.String,
     *      java.lang.String)
     */
    public void writeStartElement(String namespaceUri, String localName) throws XmlStreamException {
        try {
            w.writeStartElement(namespaceUri, localName);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param qname
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeStartElement(javax.xml.namespace.QName)
     */
    public void writeStartElement(QName qname) throws XmlStreamException {
        try {
            w.writeStartElement(qname);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param value
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeValue(java.lang.String)
     */
    public void writeValue(String value) throws XmlStreamException {
        try {
            w.writeValue(value);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param chars
     * @param offset
     * @param length
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeValue(char[], int, int)
     */
    public void writeValue(char[] chars, int offset, int length) throws XmlStreamException {
        try {
            w.writeValue(chars, offset, length);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeValue(byte[], int, int)
     */
    public void writeValue(byte[] bytes, int offset, int length) throws XmlStreamException {
        try {
            w.writeValue(bytes, offset, length);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param value
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeValue(int)
     */
    public void writeValue(int value) throws XmlStreamException {
        try {
            w.writeValue(value);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param value
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeValue(long)
     */
    public void writeValue(long value) throws XmlStreamException {
        try {
            w.writeValue(value);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param value
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeValue(float)
     */
    public void writeValue(float value) throws XmlStreamException {
        try {
            w.writeValue(value);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param value
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeValue(double)
     */
    public void writeValue(double value) throws XmlStreamException {
        try {
            w.writeValue(value);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param value
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeValue(boolean)
     */
    public void writeValue(boolean value) throws XmlStreamException {
        try {
            w.writeValue(value);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param value
     * @param offset
     * @param length
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeValue(boolean[], int, int)
     */
    public void writeValue(boolean[] value, int offset, int length) throws XmlStreamException {
        try {
            w.writeValue(value, offset, length);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param value
     * @param offset
     * @param length
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeValue(int[], int, int)
     */
    public void writeValue(int[] value, int offset, int length) throws XmlStreamException {
        try {
            w.writeValue(value, offset, length);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param value
     * @param offset
     * @param length
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeValue(long[], int, int)
     */
    public void writeValue(long[] value, int offset, int length) throws XmlStreamException {
        try {
            w.writeValue(value, offset, length);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param value
     * @param offset
     * @param length
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeValue(float[], int, int)
     */
    public void writeValue(float[] value, int offset, int length) throws XmlStreamException {
        try {
            w.writeValue(value, offset, length);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @param value
     * @param offset
     * @param length
     * @throws XmlStreamException
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriter#writeValue(double[], int, int)
     */
    public void writeValue(double[] value, int offset, int length) throws XmlStreamException {
        try {
            w.writeValue(value, offset, length);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

}
