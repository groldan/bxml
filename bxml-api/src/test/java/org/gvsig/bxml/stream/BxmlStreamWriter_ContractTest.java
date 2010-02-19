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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.gvsig.bxml.stream.EventType.ATTRIBUTE;
import static org.gvsig.bxml.stream.EventType.COMMENT;
import static org.gvsig.bxml.stream.EventType.NAMESPACE_DECL;
import static org.gvsig.bxml.stream.EventType.NONE;
import static org.gvsig.bxml.stream.EventType.START_DOCUMENT;
import static org.gvsig.bxml.stream.EventType.START_ELEMENT;
import static org.gvsig.bxml.stream.EventType.VALUE_BOOL;
import static org.gvsig.bxml.stream.EventType.VALUE_BYTE;
import static org.gvsig.bxml.stream.EventType.VALUE_CDATA;
import static org.gvsig.bxml.stream.EventType.VALUE_DOUBLE;
import static org.gvsig.bxml.stream.EventType.VALUE_FLOAT;
import static org.gvsig.bxml.stream.EventType.VALUE_INT;
import static org.gvsig.bxml.stream.EventType.VALUE_LONG;
import static org.gvsig.bxml.stream.EventType.VALUE_STRING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Array;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class BxmlStreamWriter_ContractTest {

    /**
     * Mocked up impl wrapped by the contract verifier wrapper
     */
    private BxmlStreamWriter mocks;

    /**
     * The reader that forces contract verification
     */
    private BxmlStreamWriter_Contract writer;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        createMockWriter();
    }

    private void createMockWriter() {
        mocks = createMock(BxmlStreamWriter.class);
        writer = new BxmlStreamWriter_Contract(mocks);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#getLastEvent()}.
     */
    @Test
    public void testGetLastEvent() {
        expect(mocks.getLastEvent()).andReturn(null);
        expect(mocks.getLastEvent()).andReturn(COMMENT);
        replay(mocks);
        try {
            writer.getLastEvent();
            fail("expected contract violation failure");
        } catch (PostconditionViolationException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("getLastEvent() can't return null"));
        }
        assertSame(COMMENT, writer.getLastEvent());
        verify(mocks);
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#getTagDeep()}.
     */
    @Test
    public void testGetTagDeep() {
        expect(mocks.getTagDeep()).andReturn(-1);
        replay(mocks);
        try {
            writer.getTagDeep();
            fail("expected contract violation failure");
        } catch (PostconditionViolationException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(
                    "getTagDeep: return value should be >= 0:"));
        }
        verify(mocks);
    }

    /**
     * @throws IOException
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#setDefaultNamespace(java.lang.String)} .
     */
    @Test
    public void testSetDefaultNamespace() throws IOException {
        // * @pre {defaultNamespaceUri != null}
        try {
            writer.setDefaultNamespace(null);
            fail("Expected precondition violation on null nsuri");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }

        // * @pre {getLastEvent() IN (START_DOCUMENT , START_ELEMENT, NAMESPACE_DECL)}
        final String defaultNamespaceUri = "http://www.example.com";

        // precondition violation
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(COMMENT);
        replay(mocks);

        try {
            writer.setDefaultNamespace(defaultNamespaceUri);
            fail("Expcted failure on non valid last event");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        
        // * @post {getLastEvent() == NAMESPACE_DECL}
        // postcondition violation
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(NONE);
        mocks.setDefaultNamespace(defaultNamespaceUri);
        expect(mocks.getLastEvent()).andReturn(START_DOCUMENT);
        replay(mocks);
        try {
            writer.setDefaultNamespace(defaultNamespaceUri);
            fail("Expcted failure on non valid last event for postcondition");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        // valid pre/post events 1
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(NONE);
        mocks.setDefaultNamespace(defaultNamespaceUri);
        expect(mocks.getLastEvent()).andReturn(NAMESPACE_DECL);
        replay(mocks);
        writer.setDefaultNamespace(defaultNamespaceUri);
        verify(mocks);

        // valid pre/post events 2
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(START_ELEMENT);
        mocks.setDefaultNamespace(defaultNamespaceUri);
        expect(mocks.getLastEvent()).andReturn(NAMESPACE_DECL);
        replay(mocks);
        writer.setDefaultNamespace(defaultNamespaceUri);
        verify(mocks);

        // valid pre/post events 3
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(NAMESPACE_DECL);
        mocks.setDefaultNamespace(defaultNamespaceUri);
        expect(mocks.getLastEvent()).andReturn(NAMESPACE_DECL);
        replay(mocks);
        writer.setDefaultNamespace(defaultNamespaceUri);
        verify(mocks);
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#getPrefix(java.lang.String)}.
     */
    @Test
    @Ignore
    public void testGetPrefix() {
        fail("Not yet implemented");
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#setPrefix(java.lang.String, java.lang.String)}
     *      .
     */
    @Test
    @Ignore
    public void testSetPrefix() {
        fail("Not yet implemented");
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#setSchemaLocation(java.lang.String, java.lang.String)}
     *      .
     */
    @Test
    @Ignore
    public void testSetSchemaLocation() {
        fail("Not yet implemented");
    }

    /**
     * @throws IOException
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#startArray(org.gvsig.bxml.stream.EventType, int)}
     *      .
     */
    @Test
    public void testStartArray() throws IOException {
        createMockWriter();
        expect(mocks.isArrayInProgress()).andReturn(true);
        replay(mocks);
        try {
            writer.startArray(VALUE_BYTE, 2);
            fail("expected precondition violation on isArrayInProgress() == true");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        createMockWriter();
        expect(mocks.isArrayInProgress()).andReturn(false);
        replay(mocks);
        try {
            writer.startArray(null, 2);
            fail("expected precondition violation on null type");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        createMockWriter();
        expect(mocks.isArrayInProgress()).andReturn(false);
        replay(mocks);
        try {
            writer.startArray(START_ELEMENT, 2);
            fail("expected precondition violation on type of array being not a value event");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        createMockWriter();
        expect(mocks.isArrayInProgress()).andReturn(false);
        replay(mocks);
        try {
            writer.startArray(VALUE_STRING, 2);
            fail("expected precondition violation on value_string type, string arrays are not allowed");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        createMockWriter();
        expect(mocks.isArrayInProgress()).andReturn(false);
        replay(mocks);
        try {
            writer.startArray(VALUE_INT, -1);
            fail("expected precondition violation array length < 0");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        // @post {getLastEvent() == valueType}
        createMockWriter();
        expect(mocks.isArrayInProgress()).andReturn(false);
        mocks.startArray(VALUE_BOOL, 2);
        expect(mocks.getLastEvent()).andReturn(NONE);
        replay(mocks);
        try {
            writer.startArray(VALUE_BOOL, 2);
            fail("expected postcondition violation on lastEvent != array value type");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        // @post {getWrittenValueCount() == 0}
        createMockWriter();
        expect(mocks.isArrayInProgress()).andReturn(false);
        mocks.startArray(VALUE_BOOL, 2);
        expect(mocks.getLastEvent()).andReturn(VALUE_BOOL);
        expect(mocks.getWrittenValueCount()).andReturn(1L);
        replay(mocks);
        try {
            writer.startArray(VALUE_BOOL, 2);
            fail("expected postcondition violation on written value count != 0");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        // @post {getValueLength() == arrayLength}
        createMockWriter();
        expect(mocks.isArrayInProgress()).andReturn(false);
        mocks.startArray(VALUE_BOOL, 3);
        expect(mocks.getLastEvent()).andReturn(VALUE_BOOL);
        expect(mocks.getWrittenValueCount()).andReturn(0L);
        expect(mocks.getValueLength()).andReturn(2L);
        replay(mocks);
        try {
            writer.startArray(VALUE_BOOL, 3);
            fail("expected postcondition violation on value length != declared array length");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        // @post {isArrayInProgress() == true}
        createMockWriter();
        expect(mocks.isArrayInProgress()).andReturn(false);
        mocks.startArray(VALUE_BOOL, 3);
        expect(mocks.getLastEvent()).andReturn(VALUE_BOOL);
        expect(mocks.getWrittenValueCount()).andReturn(0L);
        expect(mocks.getValueLength()).andReturn(3L);
        expect(mocks.isArrayInProgress()).andReturn(false);
        replay(mocks);
        try {
            writer.startArray(VALUE_BOOL, 3);
            fail("expected postcondition violation on isArrayInProgress == false");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);
    }

    /**
     * @throws IOException
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#endArray()}.
     */
    @Test
    public void testEndArray() throws IOException {
        // @pre {isArrayInProgress() == true}
        createMockWriter();
        expect(mocks.isArrayInProgress()).andReturn(false);
        replay(mocks);
        try {
            writer.endArray();
            fail("expected precondition violation on isArrayInProgress() == false");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        // @pre {getLastEvent().isValue() == true}
        createMockWriter();
        expect(mocks.isArrayInProgress()).andReturn(true);
        expect(mocks.getLastEvent()).andReturn(START_ELEMENT);
        replay(mocks);
        try {
            writer.endArray();
            fail("expected precondition violation on non value last event");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        // @pre {getWrittenValueCount() == getValueLength()}
        createMockWriter();
        expect(mocks.isArrayInProgress()).andReturn(true);
        expect(mocks.getLastEvent()).andReturn(VALUE_BYTE);
        expect(mocks.getWrittenValueCount()).andReturn(9L);
        expect(mocks.getValueLength()).andReturn(10L);
        replay(mocks);
        try {
            writer.endArray();
            fail("expected precondition violation on writtenValueCount != valueLength");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        // @pre {getWrittenValueCount() == getValueLength()}
        createMockWriter();
        expect(mocks.isArrayInProgress()).andReturn(true);
        expect(mocks.getLastEvent()).andReturn(VALUE_BYTE);
        expect(mocks.getWrittenValueCount()).andReturn(11L);
        expect(mocks.getValueLength()).andReturn(10L);
        replay(mocks);
        try {
            writer.endArray();
            fail("expected precondition violation on writtenValueCount != valueLength");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        // @post {getWrittenValueCount() == 0}
        // the startArray expectations
        createMockWriter();
        expect(mocks.isArrayInProgress()).andReturn(false);
        mocks.startArray(VALUE_BYTE, 10);
        expect(mocks.getLastEvent()).andReturn(VALUE_BYTE);
        expect(mocks.getWrittenValueCount()).andReturn(0L);
        expect(mocks.getValueLength()).andReturn(10L);
        expect(mocks.isArrayInProgress()).andReturn(true);
        // the endArray expectations
        expect(mocks.isArrayInProgress()).andReturn(true);
        expect(mocks.getLastEvent()).andReturn(VALUE_BYTE);
        expect(mocks.getWrittenValueCount()).andReturn(10L);
        expect(mocks.getValueLength()).andReturn(10L);
        mocks.endArray();
        expect(mocks.getValueLength()).andReturn(0L);
        expect(mocks.getWrittenValueCount()).andReturn(10L);
        replay(mocks);
        writer.startArray(VALUE_BYTE, 10);
        try {
            writer.endArray();
            fail("expected postcondition violation on getWrittenValueCount != 0");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        // @post {getValueLength() == 0}
        createMockWriter();
        // the startArray expectations
        expect(mocks.isArrayInProgress()).andReturn(false);
        mocks.startArray(VALUE_BYTE, 10);
        expect(mocks.getLastEvent()).andReturn(VALUE_BYTE);
        expect(mocks.getWrittenValueCount()).andReturn(0L);
        expect(mocks.getValueLength()).andReturn(10L);
        expect(mocks.isArrayInProgress()).andReturn(true);
        // the endArray expectations
        expect(mocks.isArrayInProgress()).andReturn(true);
        expect(mocks.getLastEvent()).andReturn(VALUE_BYTE);
        expect(mocks.getWrittenValueCount()).andReturn(10L);
        expect(mocks.getValueLength()).andReturn(10L);
        mocks.endArray();
        expect(mocks.getValueLength()).andReturn(10L);
        replay(mocks);

        writer.startArray(VALUE_BYTE, 10);
        try {
            writer.endArray();
            fail("expected postcondition violation on valueLength != 0");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        // @post {isArrayInProgress() == false}
        createMockWriter();
        // the startArray expectations
        expect(mocks.isArrayInProgress()).andReturn(false);
        mocks.startArray(VALUE_BYTE, 10);
        expect(mocks.getLastEvent()).andReturn(VALUE_BYTE);
        expect(mocks.getWrittenValueCount()).andReturn(0L);
        expect(mocks.getValueLength()).andReturn(10L);
        expect(mocks.isArrayInProgress()).andReturn(true);
        // the endArray expectations
        expect(mocks.isArrayInProgress()).andReturn(true);
        expect(mocks.getLastEvent()).andReturn(VALUE_BYTE);
        expect(mocks.getWrittenValueCount()).andReturn(10L);
        expect(mocks.getValueLength()).andReturn(10L);
        mocks.endArray();
        expect(mocks.getValueLength()).andReturn(0L);
        expect(mocks.getWrittenValueCount()).andReturn(0L);
        expect(mocks.isArrayInProgress()).andReturn(true);
        replay(mocks);

        writer.startArray(VALUE_BYTE, 10);
        try {
            writer.endArray();
            fail("expected postcondition violation on isArrayInProgress() == true");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#getValueLength()}.
     */
    @Test
    public void testGetValueLength() {
        // precon violation, last event not a value
        expect(mocks.getLastEvent()).andReturn(COMMENT);

        // postcondition violation
        expect(mocks.getLastEvent()).andReturn(VALUE_BOOL);
        expect(mocks.getValueLength()).andReturn(-1L);

        // valid cases
        expect(mocks.getLastEvent()).andReturn(VALUE_BOOL);
        expect(mocks.getValueLength()).andReturn(0L);

        expect(mocks.getLastEvent()).andReturn(VALUE_STRING);
        expect(mocks.getValueLength()).andReturn(0L);

        expect(mocks.getLastEvent()).andReturn(VALUE_LONG);
        expect(mocks.getValueLength()).andReturn(5L);

        replay(mocks);
        try {
            writer.getValueLength();
            fail("expected precondition violation on non value event");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }

        try {
            writer.getValueLength();
            fail("expected postcondition violation on value length < 0");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }

        assertEquals(0L, writer.getValueLength());
        assertEquals(0L, writer.getValueLength());
        assertEquals(5L, writer.getValueLength());
        verify(mocks);
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#getWrittenValueCount()}.
     */
    @Test
    public void testGetWrittenValueCount() {
        // precon violation, last event not a value
        expect(mocks.getLastEvent()).andReturn(COMMENT);

        // postcondition violation
        expect(mocks.getLastEvent()).andReturn(VALUE_BOOL);
        expect(mocks.getWrittenValueCount()).andReturn(-1L);

        // postcondition violation, written > value length
        expect(mocks.getLastEvent()).andReturn(VALUE_BOOL);
        expect(mocks.getWrittenValueCount()).andReturn(5L);
        expect(mocks.getValueLength()).andReturn(4L);

        // valid cases
        expect(mocks.getLastEvent()).andReturn(VALUE_DOUBLE);
        expect(mocks.getWrittenValueCount()).andReturn(5L);
        expect(mocks.getValueLength()).andReturn(5L);

        expect(mocks.getLastEvent()).andReturn(VALUE_DOUBLE);
        expect(mocks.getWrittenValueCount()).andReturn(1L);
        expect(mocks.getValueLength()).andReturn(1000L);

        replay(mocks);
        try {
            writer.getWrittenValueCount();
            fail("expected precondition violation on non value event");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }

        try {
            writer.getWrittenValueCount();
            fail("expected postcondition violation on written count < 0");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }

        try {
            writer.getWrittenValueCount();
            fail("expected postcondition violation on written count > value length");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }

        assertEquals(5L, writer.getWrittenValueCount());
        assertEquals(1L, writer.getWrittenValueCount());
        verify(mocks);
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeComment(java.lang.String)}.
     */
    @Test
    @Ignore
    public void testWriteComment() {
        fail("Not yet implemented");
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeEndAttributes()}.
     */
    @Test
    @Ignore
    public void testWriteEndAttributes() {
        fail("Not yet implemented");
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeEndDocument()}.
     */
    @Test
    @Ignore
    public void testWriteEndDocument() {
        fail("Not yet implemented");
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeEndElement()}.
     */
    @Test
    @Ignore
    public void testWriteEndElement() {
        fail("Not yet implemented");
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeStartAttribute(java.lang.String, java.lang.String)}
     *      .
     */
    @Test
    @Ignore
    public void testWriteStartAttributeStringString() {
        fail("Not yet implemented");
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeStartAttribute(javax.xml.namespace.QName)}
     *      .
     */
    @Test
    @Ignore
    public void testWriteStartAttributeQName() {
        fail("Not yet implemented");
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeStartDocument()}.
     */
    @Test
    @Ignore
    public void testWriteStartDocument() {
        fail("Not yet implemented");
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeStartElement(java.lang.String, java.lang.String)}
     *      .
     */
    @Test
    @Ignore
    public void testWriteStartElementStringString() {
        fail("Not yet implemented");
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeStartElement(javax.xml.namespace.QName)}
     *      .
     */
    @Test
    @Ignore
    public void testWriteStartElementQName() {
        fail("Not yet implemented");
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(java.lang.String)}.
     */
    @Test
    @Ignore
    public void testWriteValueString() {
        createMockWriter();
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(char[], int, int)}.
     */
    @Test
    @Ignore
    public void testWriteValueCharArray() {
        fail("Not yet implemented");
    }

    /**
     * @throws IOException
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(int)}.
     */
    @Test
    public void testWriteValueInt() throws IOException {
        testWriteSinglePrimitiveValue(VALUE_INT, Integer.valueOf(100));
    }

    /**
     * @throws IOException
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(long)}.
     */
    @Test
    public void testWriteValueLong() throws IOException {
        testWriteSinglePrimitiveValue(VALUE_LONG, Long.valueOf(100L));
    }

    /**
     * @throws IOException
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(float)}.
     */
    @Test
    public void testWriteValueFloat() throws IOException {
        testWriteSinglePrimitiveValue(VALUE_FLOAT, Float.valueOf(100F));
    }

    /**
     * @throws IOException
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(double)}.
     */
    @Test
    public void testWriteValueDouble() throws IOException {
        testWriteSinglePrimitiveValue(VALUE_DOUBLE, Double.valueOf(100D));
    }

    /**
     * @throws IOException
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(boolean)}.
     */
    @Test
    public void testWriteValueBoolean() throws IOException {
        testWriteSinglePrimitiveValue(VALUE_BOOL, Boolean.TRUE);
    }

    /**
     * 
     * @param valueType
     * @param value
     *            a Number or Boolean whose type corresponds to {@code valueType}
     * @throws IOException
     */
    private void testWriteSinglePrimitiveValue(final EventType valueType, final Object value)
            throws IOException {
        // case #1 for @pre {getLastEvent().isValue() == true || getLastEvent() IN (START_ELEMENT,
        // ATTRIBUTE)}
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(START_DOCUMENT);
        replay(mocks);
        try {
            writeTestValue(writer, valueType, value);
            fail("expected precondition violation on !lastEvent().isValue()");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        // case #2 for @pre {getLastEvent().isValue() == true || getLastEvent() IN (START_ELEMENT,
        // ATTRIBUTE)}
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(START_ELEMENT);
        expect(mocks.getValueLength()).andReturn(0L);
        expect(mocks.getWrittenValueCount()).andReturn(0L);
        writeTestValue(mocks, valueType, value);
        expect(mocks.getLastEvent()).andReturn(valueType);
        expect(mocks.getValueLength()).andReturn(1L);
        expect(mocks.getWrittenValueCount()).andReturn(1L);
        expect(mocks.isArrayInProgress()).andReturn(false);
        replay(mocks);

        writeTestValue(writer, valueType, value);

        verify(mocks);

        // case #3 for @pre {getLastEvent().isValue() == true || getLastEvent() IN (START_ELEMENT,
        // ATTRIBUTE)}
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(EventType.ATTRIBUTE);
        expect(mocks.getValueLength()).andReturn(0L);
        expect(mocks.getWrittenValueCount()).andReturn(0L);
        writeTestValue(mocks, valueType, value);
        expect(mocks.getLastEvent()).andReturn(valueType);
        expect(mocks.getValueLength()).andReturn(1L);
        expect(mocks.getWrittenValueCount()).andReturn(1L);
        expect(mocks.isArrayInProgress()).andReturn(false);
        replay(mocks);

        writeTestValue(writer, valueType, value);

        verify(mocks);

        // @post {getLastEvent() == ${valueType}}
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(EventType.ATTRIBUTE);
        expect(mocks.getValueLength()).andReturn(0L);
        expect(mocks.getWrittenValueCount()).andReturn(0L);
        writeTestValue(mocks, valueType, value);
        expect(mocks.getLastEvent()).andReturn(START_DOCUMENT);
        replay(mocks);
        try {
            writeTestValue(writer, valueType, value);
            fail("expected postcondition violation on lastEvent != " + valueType);
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        // @post {getWrittenValueCount() = 1 + $pre:getWrittenValueCount()}
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(EventType.ATTRIBUTE);
        expect(mocks.getValueLength()).andReturn(10L);
        expect(mocks.getWrittenValueCount()).andReturn(2L);
        writeTestValue(mocks, valueType, value);
        expect(mocks.getLastEvent()).andReturn(valueType);
        expect(mocks.getValueLength()).andReturn(10L);
        expect(mocks.getWrittenValueCount()).andReturn(2L);
        expect(mocks.isArrayInProgress()).andReturn(true);
        replay(mocks);
        try {
            writeTestValue(writer, valueType, value);
            fail("expected postcondition violation on final writtenValueCount != previous writtenValueCount + 1");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        // @post isArrayInProgress() == true && $pre:valueLength != $post:valueLength
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(EventType.ATTRIBUTE);
        expect(mocks.getValueLength()).andReturn(10L);
        expect(mocks.getWrittenValueCount()).andReturn(0L);
        writeTestValue(mocks, valueType, value);
        expect(mocks.getLastEvent()).andReturn(valueType);
        expect(mocks.getValueLength()).andReturn(11L);
        expect(mocks.getWrittenValueCount()).andReturn(1L);
        expect(mocks.isArrayInProgress()).andReturn(true);
        replay(mocks);
        try {
            writeTestValue(writer, valueType, value);
            fail("Expected postcondition violation if an array is in progress and the valueLength changed");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }

        verify(mocks);

        // @post isArrayInProgress() == false && $post:valueLength != expected written count
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(EventType.ATTRIBUTE);
        expect(mocks.getValueLength()).andReturn(0L);
        expect(mocks.getWrittenValueCount()).andReturn(0L);
        writeTestValue(mocks, valueType, value);
        expect(mocks.getLastEvent()).andReturn(valueType);
        expect(mocks.getValueLength()).andReturn(2L);
        expect(mocks.getWrittenValueCount()).andReturn(1L);
        expect(mocks.isArrayInProgress()).andReturn(false);
        replay(mocks);
        try {
            writeTestValue(writer, valueType, value);
            fail("Expected postcondition violation if value length is not equal to the required write count");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }

        verify(mocks);

        // @post isArrayInProgress() == false && writtenValueCount != expected written count
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(EventType.ATTRIBUTE);
        expect(mocks.getValueLength()).andReturn(0L);
        expect(mocks.getWrittenValueCount()).andReturn(0L);
        writeTestValue(mocks, valueType, value);
        expect(mocks.getLastEvent()).andReturn(valueType);
        expect(mocks.getValueLength()).andReturn(1L);
        expect(mocks.getWrittenValueCount()).andReturn(0L);
        expect(mocks.isArrayInProgress()).andReturn(false);
        replay(mocks);
        try {
            writeTestValue(writer, valueType, value);
            fail("Expected postcondition violation if written value count != expected written count");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }

        verify(mocks);

        // success case while no array is in progress
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(EventType.ATTRIBUTE);
        expect(mocks.getValueLength()).andReturn(0L);
        expect(mocks.getWrittenValueCount()).andReturn(0L);
        writeTestValue(mocks, valueType, value);
        expect(mocks.getLastEvent()).andReturn(valueType);
        expect(mocks.getValueLength()).andReturn(1L);
        expect(mocks.getWrittenValueCount()).andReturn(1L);
        expect(mocks.isArrayInProgress()).andReturn(false);
        replay(mocks);

        writeTestValue(writer, valueType, value);

        verify(mocks);

        // success case while an array _is_ in progress
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(EventType.ATTRIBUTE);
        expect(mocks.getValueLength()).andReturn(10L);
        expect(mocks.getWrittenValueCount()).andReturn(5L);
        writeTestValue(mocks, valueType, value);
        expect(mocks.getLastEvent()).andReturn(valueType);
        expect(mocks.getValueLength()).andReturn(10L);
        expect(mocks.getWrittenValueCount()).andReturn(6L);
        expect(mocks.isArrayInProgress()).andReturn(true);
        replay(mocks);

        writeTestValue(writer, valueType, value);

        verify(mocks);
    }

    private void writeTestValue(final BxmlStreamWriter whichWritter, final EventType valueType,
            final Object value) throws IOException {
        switch (valueType) {
        case VALUE_BOOL:
            whichWritter.writeValue(((Boolean) value).booleanValue());
            break;
        case VALUE_BYTE:
            whichWritter.writeValue(((Number) value).byteValue());
            break;
        case VALUE_DOUBLE:
            whichWritter.writeValue(((Number) value).doubleValue());
            break;
        case VALUE_FLOAT:
            whichWritter.writeValue(((Number) value).floatValue());
            break;
        case VALUE_INT:
            whichWritter.writeValue(((Number) value).intValue());
            break;
        case VALUE_LONG:
            whichWritter.writeValue(((Number) value).longValue());
            break;
        default:
            throw new IllegalArgumentException("Do not know what writeValue method to route "
                    + value.getClass());
        }
    }

    /**
     * @throws IOException
     * @throws IllegalArgumentException
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(boolean[], int, int)}.
     */
    @Test
    public void testWriteValueBooleanArray() throws IllegalArgumentException, IOException {
        testWritePrimitiveArray(VALUE_BOOL, new boolean[] { true, true, false, false });
    }

    private void testWritePrimitiveArray(final EventType valueType, final Object primitiveArray)
            throws IllegalArgumentException, IOException {
        // @pre {getLastEvent().isValue() == true || getLastEvent() IN (START_ELEMENT, ATTRIBUTE)}
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(START_DOCUMENT);
        replay(mocks);
        try {
            writePrimitiveArray(writer, valueType, primitiveArray);
            fail("expected precondition violation on lastEvent not a value nor START_ELEMENT or ATTRIBUTE");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        /*
         * @post {getLastEvent() == ${valueType}}
         */
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(ATTRIBUTE);
        expect(mocks.getValueLength()).andReturn(0L);
        expect(mocks.getWrittenValueCount()).andReturn(0L);
        writePrimitiveArray(mocks, valueType, primitiveArray);
        expect(mocks.getLastEvent()).andReturn(VALUE_CDATA);
        replay(mocks);
        try {
            writePrimitiveArray(writer, valueType, primitiveArray);
            fail("expected postcondition violation on lastEvent != " + valueType);
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        final int arrayLength = Array.getLength(primitiveArray);

        /*
         * case #1 for @pre {if (true == isArrayInProgress()) getValueLength() >=
         * getWrittenValueCount() + length}, where isArrayInProgress() == false
         */
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(START_ELEMENT);
        expect(mocks.getValueLength()).andReturn(0L);
        expect(mocks.getWrittenValueCount()).andReturn(0L);
        writePrimitiveArray(mocks, valueType, primitiveArray);
        expect(mocks.getLastEvent()).andReturn(valueType);
        expect(mocks.getValueLength()).andReturn(0L);
        expect(mocks.getWrittenValueCount()).andReturn(arrayLength - 1L);
        expect(mocks.isArrayInProgress()).andReturn(false);
        replay(mocks);
        try {
            writePrimitiveArray(writer, valueType, primitiveArray);
            fail("expected postcondition violation on getWrittenValueCount != " + arrayLength);
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        /*
         * case #2 for @pre {if (true == isArrayInProgress()) getValueLength() >=
         * getWrittenValueCount() + length}, where isArrayInProgress() == true
         */
        createMockWriter();
        expect(mocks.getLastEvent()).andReturn(ATTRIBUTE);
        expect(mocks.getValueLength()).andReturn(0L);
        expect(mocks.getWrittenValueCount()).andReturn(10L);
        writePrimitiveArray(mocks, valueType, primitiveArray);
        expect(mocks.getLastEvent()).andReturn(valueType);
        expect(mocks.getValueLength()).andReturn(0L);
        expect(mocks.getWrittenValueCount()).andReturn(10 + arrayLength - 1L);
        expect(mocks.isArrayInProgress()).andReturn(true);
        replay(mocks);
        try {
            writePrimitiveArray(writer, valueType, primitiveArray);
            fail("expected postcondition violation on getWrittenValueCount != previous written count "
                    + arrayLength);
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mocks);

        // @post {getValueLength() = isArrayInProgress()? $pre:getValueLength() : length}
        // @post {getWrittenValueCount == isArrayInProgress()? $pre:getWrittenValueLength() + length
        // : length}}

    }

    private void writePrimitiveArray(final BxmlStreamWriter whichWritter,
            final EventType valueType, final Object primitiveArray)
            throws IllegalArgumentException, IOException {

        switch (valueType) {
        case VALUE_BOOL:
            whichWritter.writeValue((boolean[]) primitiveArray, 0, Array.getLength(primitiveArray));
            break;
        case VALUE_BYTE:
            whichWritter.writeValue((byte[]) primitiveArray, 0, Array.getLength(primitiveArray));
            break;
        case VALUE_DOUBLE:
            whichWritter.writeValue((double[]) primitiveArray, 0, Array.getLength(primitiveArray));
            break;
        case VALUE_FLOAT:
            whichWritter.writeValue((float[]) primitiveArray, 0, Array.getLength(primitiveArray));
            break;
        case VALUE_INT:
            whichWritter.writeValue((int[]) primitiveArray, 0, Array.getLength(primitiveArray));
            break;
        case VALUE_LONG:
            whichWritter.writeValue((long[]) primitiveArray, 0, Array.getLength(primitiveArray));
            break;
        default:
            throw new IllegalArgumentException("Do not know what writeValue method to route "
                    + primitiveArray.getClass());
        }
    }

    /**
     * @throws IOException
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(byte)}.
     */
    @Test
    public void testWriteValueByte() throws IOException {
        testWriteSinglePrimitiveValue(VALUE_BYTE, Byte.valueOf((byte) 127));
    }

    /**
     * @throws IOException
     * @throws IllegalArgumentException
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(byte[], int, int)}.
     */
    @Test
    public void testWriteValueByteArray() throws IllegalArgumentException, IOException {
        testWritePrimitiveArray(VALUE_BYTE, new byte[] { 1, 2, 3, 4 });
    }

    /**
     * @throws IOException
     * @throws IllegalArgumentException
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(int[], int, int)}.
     */
    @Test
    public void testWriteValueIntArray() throws IllegalArgumentException, IOException {
        testWritePrimitiveArray(VALUE_INT, new int[] { 1, 2, 3, 4 });
    }

    /**
     * @throws IOException
     * @throws IllegalArgumentException
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(long[], int, int)}.
     */
    @Test
    public void testWriteValueLongArray() throws IllegalArgumentException, IOException {
        testWritePrimitiveArray(VALUE_LONG, new long[] { 1, 2, 3, 4 });
    }

    /**
     * @throws IOException
     * @throws IllegalArgumentException
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(float[], int, int)}.
     */
    @Test
    public void testWriteValueFloatArray() throws IllegalArgumentException, IOException {
        testWritePrimitiveArray(VALUE_FLOAT, new float[] { 1, 2, 3, 4 });
    }

    /**
     * @throws IOException
     * @throws IllegalArgumentException
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(double[], int, int)}.
     */
    @Test
    public void testWriteValueDoubleArray() throws IllegalArgumentException, IOException {
        testWritePrimitiveArray(VALUE_DOUBLE, new double[] { 1, 2, 3, 4 });
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#getStringTableReference(java.lang.CharSequence)}
     *      .
     */
    @Test
    @Ignore
    public void testGetStringTableReference() {
        fail("Not yet implemented");
    }

    /**
     * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeStringTableValue(long)}.
     */
    @Test
    @Ignore
    public void testWriteStringTableValue() {
        fail("Not yet implemented");
    }

}
