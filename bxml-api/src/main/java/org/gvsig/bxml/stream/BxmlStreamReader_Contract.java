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

import static org.gvsig.bxml.stream.Contract.assertPost;
import static org.gvsig.bxml.stream.Contract.assertPre;
import static org.gvsig.bxml.stream.EventType.COMMENT;
import static org.gvsig.bxml.stream.EventType.START_ELEMENT;
import static org.gvsig.bxml.stream.EventType.VALUE_BOOL;
import static org.gvsig.bxml.stream.EventType.VALUE_BYTE;
import static org.gvsig.bxml.stream.EventType.VALUE_DOUBLE;
import static org.gvsig.bxml.stream.EventType.VALUE_FLOAT;
import static org.gvsig.bxml.stream.EventType.VALUE_INT;
import static org.gvsig.bxml.stream.EventType.VALUE_LONG;

import java.io.IOException;
import java.lang.reflect.Array;

import javax.xml.namespace.QName;

/**
 * Contract enforcement wrapper for {@link BxmlStreamReader} implementations.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
final class BxmlStreamReader_Contract extends BxmlStreamReaderAdapter implements BxmlStreamReader {

    /**
     * Creates a contract enforcement wrapper for the given {@link BxmlStreamReader}
     * 
     * @param impl
     *            the implementation to wrap with this contract enforcement wrapper
     */
    public BxmlStreamReader_Contract(final BxmlStreamReader impl) {
        super(impl);
    }

    private void assertEventType(final EventType expectedCurrentEvent) {
        final EventType eventType = getEventType();
        assertPre(eventType == expectedCurrentEvent, "Event type shall be ", expectedCurrentEvent,
                " but was ", eventType);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getAttributeCount()
     */
    public int getAttributeCount() {
        assertEventType(START_ELEMENT);
        final int attributeCount = impl.getAttributeCount();
        assertPost(attributeCount > -1, "Attribute count shall be >= 0: ", attributeCount);
        return attributeCount;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getAttributeValue(java.lang.String,
     *      java.lang.String)
     */
    public String getAttributeValue(String namespaceURI, String localName) {
        assertEventType(START_ELEMENT);
        assertPre(localName != null, "localName can't be null");
        return impl.getAttributeValue(namespaceURI, localName);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getAttributeValue(int)
     */
    public String getAttributeValue(int index) {
        // * @pre {getEventType() == START_ELEMENT}
        // * @pre {index >= 0}
        // * @pre {index < getAttributeCount()}
        // * @post {$return != null}
        assertEventType(START_ELEMENT);
        assertPre(index > -1, "Attribute index shall be >= 0");
        assertPre(index < impl.getAttributeCount(),
                "Attribute index shall be < getAttributeCount()");
        final String attributeValue = impl.getAttributeValue(index);
        assertPost(attributeValue != null, "Attribute value is null");
        return attributeValue;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getAttributeName(int)
     */
    public QName getAttributeName(final int index) {
        // * @pre {getEventType() == START_ELEMENT}
        // * @pre {index >= 0 && index < getAttributeCount()}
        // * @post {$return != null}
        assertEventType(START_ELEMENT);
        assertPre(index > -1, "Attribute index shall be > 0");
        assertPre(index < impl.getAttributeCount(),
                "Attribute index shall be < getAttributeCount()");

        final QName attributeName = impl.getAttributeName(index);
        assertPost(attributeName != null, "Attribute name is null");
        return attributeName;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getElementName()
     */
    public QName getElementName() {
        final EventType eventType = getEventType();
        assertPre(eventType.isTag(), "Event type shall be either START_ELEMENT or END_ELEMENT: ",
                eventType);
        final QName elementName = impl.getElementName();
        assertPost(elementName != null, "returned element name can't be null");
        return elementName;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getElementPosition()
     */
    public long getElementPosition() throws IOException{
        assertPre(impl.supportsRandomAccess(),
                "This BxmlStreamReader does not support random access");
        assertPre(impl.getEventType() == START_ELEMENT,
                "getElementPosition() can only be called at a START_ELEMENT event");
        return impl.getElementPosition();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#setPosition(long)
     */
    public EventType setPosition(final long position) throws IOException {
        // @pre {supportsRandomAccess() == true}
        assertPre(impl.supportsRandomAccess(),
                "This BxmlStreamReader does not support random access");
        EventType eventType = impl.setPosition(position);
        // @post {getEventType() == START_ELEMENT}
        assertPost(EventType.START_ELEMENT == eventType,
                "setPosition: return value shall be START_ELEMENT");
        return eventType;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#require(org.gvsig.bxml.stream.EventType,
     *      java.lang.String, java.lang.String)
     */
    public void require(EventType type, String namespaceUri, String localName)
            throws IllegalStateException {
        if (namespaceUri != null || localName != null) {
            final EventType eventType = getEventType();
            assertPre(eventType.isTag(), "Event type shall be either START_ELEMENT or END_ELEMENT"
                    + " when testing the element name or namespace: ", eventType);
        }
        impl.require(type, namespaceUri, localName);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getBooleanValue()
     */
    public boolean getBooleanValue() throws IOException {
        assertGetSingleValue(VALUE_BOOL, "getBooleanValue()");
        return impl.getBooleanValue();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getByteValue()
     */
    public int getByteValue() throws IOException, IllegalArgumentException {
        assertGetSingleValue(VALUE_BYTE, "getByteValue()");
        return impl.getByteValue();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getStringValue()
     */
    public String getStringValue() throws IOException {
        final EventType eventType = getEventType();
        assertPre(eventType.isValue() || COMMENT == eventType,
                "Current event type is not a VALUE event nor a comment: ", eventType);
        final String stringValue = impl.getStringValue();
        assertPost(stringValue != null, "getStringValue() returns null");
        return stringValue;
    }

    /**
     * Runs the precondition checks for the various getValue(xxx[], int, int) methods
     */
    private void assertGetValueArray(Object dst, int offset, int length, EventType eventType,
            String methodName) {
        assertEventType(eventType);

        // * @pre {dst != null}
        assertPre(dst != null, methodName, ": destination array is null");

        // * @pre {offset >= 0}
        assertPre(offset > -1, methodName, ": offset shall be >= 0");

        // * @pre {length <= (dst.length - offset)}
        assertPre(length <= (Array.getLength(dst) - offset), methodName,
                ": length shall be < (dst.length - offset)");
        // * @pre {getValueCount() - getValueReadCount() >= length}
        final int valueCount = impl.getValueCount();
        final int valueReadCount = impl.getValueReadCount();
        final int remaining = (valueCount - valueReadCount);
        assertPre(remaining >= length, methodName,
                ": getValueCount() - getValueReadCount() < length");
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(boolean[], int, int)
     */
    public void getValue(boolean[] dst, int offset, int length) throws IOException {
        assertGetValueArray(dst, offset, length, VALUE_BOOL, "getValue(boolean[], int, int)");
        impl.getValue(dst, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(byte[], int, int)
     */
    public void getValue(byte[] dst, int offset, int length) throws IOException {
        assertGetValueArray(dst, offset, length, VALUE_BYTE, "getValue(byte[], int, int)");
        impl.getValue(dst, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(int[], int, int)
     */
    public void getValue(int[] dst, int offset, int length) throws IOException {
        assertGetValueArray(dst, offset, length, VALUE_INT, "getValue(int[], int, int)");
        impl.getValue(dst, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(long[], int, int)
     */
    public void getValue(long[] dst, int offset, int length) throws IOException {
        assertGetValueArray(dst, offset, length, VALUE_LONG, "getValue(long[], int, int)");
        impl.getValue(dst, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(float[], int, int)
     */
    public void getValue(float[] dst, int offset, int length) throws IOException {
        assertGetValueArray(dst, offset, length, VALUE_FLOAT, "getValue(float[], int, int)");
        impl.getValue(dst, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(double[], int, int)
     */
    public void getValue(double[] dst, int offset, int length) throws IOException {
        assertGetValueArray(dst, offset, length, VALUE_DOUBLE, "getValue(double[], int, int)");
        impl.getValue(dst, offset, length);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValueReadCount()
     */
    public int getValueReadCount() {
        assertPre(getEventType().isValue(),
                "getReadCount(): current event type is not a VALUE event");
        final int valueReadCount = impl.getValueReadCount();
        assertPost(valueReadCount <= impl.getValueCount(),
                "getValueReadCount() shall be <= than getValueCount()");
        return valueReadCount;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValueCount()
     */
    public int getValueCount() throws IllegalStateException {
        final EventType eventType = getEventType();
        assertPre(eventType.isValue(), "getValueCount(): current event type is not VALUE event: ",
                eventType);
        final int valueCount = impl.getValueCount();
        assertPost(valueCount > -1, "getValueCount(): return value shall be >= 0");
        return valueCount;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#next()
     */
    public EventType next() throws IOException {
        assertPre(hasNext(), "End of document already reached");
        final EventType next = impl.next();
        assertPost(next != null, "next() returned null");
        assertPost(impl.getEventType() == next,
                "next(): return value shall be same than getEventType()");
        return next;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#nextTag()
     */
    public EventType nextTag() throws IOException {
        assertPre(impl.hasNext(), "nextTag(): hasNext() returned false");

        final EventType nextTag = impl.nextTag();

        assertPost(nextTag != null, "nextTag() returned null");
        assertPost(nextTag == impl.getEventType(),
                "nextTag(): returned EventType does not match getEventType()");
        assertPost(nextTag.isTag(), "nexTag() did not return a tag element: ", nextTag);

        return nextTag;
    }

    /**
     * Runs the precondition checks for the getXXXValue() methods
     */
    private void assertGetSingleValue(final EventType eventType, final String methodName) {
        assertEventType(eventType);
        assertPre(impl.getValueReadCount() < impl.getValueCount(), methodName,
                ": getValueReadCount() shall be < than getValueCount()");
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getDoubleValue()
     */
    public double getDoubleValue() throws IOException {
        assertGetSingleValue(VALUE_DOUBLE, "getDoubleValue()");
        return impl.getDoubleValue();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getFloatValue()
     */
    public float getFloatValue() throws IOException {
        assertGetSingleValue(VALUE_FLOAT, "getFloatValue()");
        return impl.getFloatValue();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getIntValue()
     */
    public int getIntValue() throws IOException {
        assertGetSingleValue(VALUE_INT, "getIntValue()");
        return impl.getIntValue();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getLongValue()
     */
    public long getLongValue() throws IOException {
        assertGetSingleValue(VALUE_LONG, "getLongValue()");
        return impl.getLongValue();
    }
}
