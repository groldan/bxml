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

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test suite for the {@code BxmlStreamReader} contract enforcement wrapper,
 * {@link BxmlStreamReader_Contract}.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id:BxmlStreamReader_ContractTest.java 454 2008-03-31 15:22:02Z groldan $
 */
public class BxmlStreamReader_ContractTest {

    /**
     * Mocked up impl wrapped by the contract verifier wrapper
     */
    BxmlStreamReader mockImpl;

    /**
     * The reader that forces contract verification
     */
    private BxmlStreamReader reader;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        createReader();
    }

    private void createReader() {
        mockImpl = createMock(BxmlStreamReader.class);
        reader = new BxmlStreamReader_Contract(mockImpl);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getAttributeCount()}.
     */
    @Test
    public void testGetAttributeCount() {
        // * @pre {getEventType() == START_ELEMENT}
        try {
            expect(mockImpl.getEventType()).andReturn(EventType.SPACE);
            replay(mockImpl);
            reader.getAttributeCount();
            fail("expected precondition violation, event type is not START_ELEMENT");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        // * @post {$return >= 0}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        expect(mockImpl.getAttributeCount()).andReturn(-1);
        replay(mockImpl);
        try {
            reader.getAttributeCount();
            fail("expected postcondition violation, getAttributeCount returned < 0");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        expect(mockImpl.getAttributeCount()).andReturn(2);
        replay(mockImpl);
        assertEquals(2, reader.getAttributeCount());
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getAttributeValue(int)}.
     */
    @Test
    public void testGetAttributeValue() {
        // * @pre {getEventType() == START_ELEMENT}
        try {
            expect(mockImpl.getEventType()).andReturn(EventType.SPACE);
            replay(mockImpl);
            reader.getAttributeValue(1);
            fail("expected precondition violation @pre {getEventType() == START_ELEMENT}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {index >= 0}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        replay(mockImpl);
        try {
            reader.getAttributeValue(-1);
            fail("expected precondition violation @pre {index >= 0}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {index < getAttributeCount()}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        expect(mockImpl.getAttributeCount()).andReturn(1);
        replay(mockImpl);
        try {
            reader.getAttributeValue(1);
            fail("expected precondition violation @pre {index < getAttributeCount()}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @post {$return != null}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        expect(mockImpl.getAttributeCount()).andReturn(2);
        expect(mockImpl.getAttributeValue(1)).andReturn(null);
        replay(mockImpl);
        try {
            reader.getAttributeValue(1);
            fail("expected precondition violation @post {$return != null}");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        expect(mockImpl.getAttributeCount()).andReturn(2);
        expect(mockImpl.getAttributeValue(1)).andReturn("value");
        replay(mockImpl);
        assertEquals("value", reader.getAttributeValue(1));
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getAttributeValue(String, String)}.
     */
    @Test
    public void testGetAttributeValueNsLocalName() {
        // * @pre {getEventType() == START_ELEMENT}
        try {
            expect(mockImpl.getEventType()).andReturn(EventType.SPACE);
            replay(mockImpl);
            reader.getAttributeValue("", "localName");
            fail("expected precondition violation @pre {getEventType() == START_ELEMENT}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {localName != null}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        replay(mockImpl);
        try {
            reader.getAttributeValue("", null);
            fail("expected precondition violation @pre {localName != null}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        expect(mockImpl.getAttributeValue("", "localName")).andReturn("value");
        replay(mockImpl);
        assertEquals("value", reader.getAttributeValue("", "localName"));
        verify(mockImpl);
    }

    /**
     * Test method for {@link BxmlStreamReader#getAttributeName(int)}.
     */
    @Test
    public void testGetAttributeName() {
        // * @pre {getEventType() == START_ELEMENT}
        expect(mockImpl.getEventType()).andReturn(EventType.SPACE);
        replay(mockImpl);
        try {
            reader.getAttributeName(1);
            fail("expected precondition violation, event type is not START_ELEMENT");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {index >= 0}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        replay(mockImpl);
        try {
            reader.getAttributeName(-1);
            fail("expected precondition violation, index < 0");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {index < getAttributeCount()}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        expect(mockImpl.getAttributeCount()).andReturn(2);
        replay(mockImpl);
        try {
            reader.getAttributeName(2);
            fail("expected precondition violation: @pre {index < getAttributeCount()}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @post {$return != null}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        expect(mockImpl.getAttributeCount()).andReturn(2);
        expect(mockImpl.getAttributeName(1)).andReturn(null);
        replay(mockImpl);
        try {
            assertEquals(new QName("name"), reader.getAttributeName(1));
            fail("expected postcondition violation: @post {$return != null}");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met..
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        expect(mockImpl.getAttributeCount()).andReturn(2);
        expect(mockImpl.getAttributeName(1)).andReturn(new QName("name"));
        replay(mockImpl);
        assertEquals(new QName("name"), reader.getAttributeName(1));
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getElementName()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetElementName() throws IOException {
        // * @pre {getEventType().isTag() == true}
        expect(mockImpl.getEventType()).andReturn(EventType.SPACE);
        replay(mockImpl);
        try {
            reader.getElementName();
            fail("expected precondition violation @pre {getEventType().isTag() == true}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @post {$return != null}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        expect(mockImpl.getElementName()).andReturn(null);
        replay(mockImpl);
        try {
            reader.getElementName();
            fail("expected postcondition violation @post {$return != null}");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        expect(mockImpl.getElementName()).andReturn(new QName("name"));
        expect(mockImpl.getEventType()).andReturn(EventType.END_ELEMENT);
        expect(mockImpl.getElementName()).andReturn(new QName("name"));
        replay(mockImpl);

        assertEquals(new QName("name"), reader.getElementName());
        assertEquals(new QName("name"), reader.getElementName());
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getBooleanValue()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetBooleanValue() throws IOException {
        // * @pre {getEventType() == VALUE_BOOL}
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BYTE);
        replay(mockImpl);
        try {
            reader.getBooleanValue();
            fail("expected precondition violation @pre {getEventType() == VALUE_BOOL}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {getValueReadCount() < getValueCount()}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BOOL);
        expect(mockImpl.getValueCount()).andReturn(10);
        expect(mockImpl.getValueReadCount()).andReturn(10);
        replay(mockImpl);
        try {
            reader.getBooleanValue();
            fail("expected precondition violation @pre {getValueReadCount() < getValueCount()}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BOOL);
        expect(mockImpl.getValueCount()).andReturn(10);
        expect(mockImpl.getValueReadCount()).andReturn(9);
        expect(mockImpl.getBooleanValue()).andReturn(true);
        replay(mockImpl);
        assertEquals(Boolean.TRUE, reader.getBooleanValue());
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getByteValue()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetByteValue() throws IOException {
        // * @pre {getEventType() == VALUE_BYTE}
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BOOL);
        replay(mockImpl);
        try {
            reader.getByteValue();
            fail("expected precondition violation @pre {getEventType() == VALUE_BYTE}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {getValueReadCount() < getValueCount()}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BYTE);
        expect(mockImpl.getValueCount()).andReturn(10);
        expect(mockImpl.getValueReadCount()).andReturn(10);
        replay(mockImpl);
        try {
            reader.getByteValue();
            fail("expected precondition violation @pre {getValueReadCount() < getValueCount()}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BYTE);
        expect(mockImpl.getValueCount()).andReturn(10);
        expect(mockImpl.getValueReadCount()).andReturn(9);
        expect(mockImpl.getByteValue()).andReturn(1);
        replay(mockImpl);
        assertEquals(1, reader.getByteValue());
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getDoubleValue()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetDoubleValue() throws IOException {
        // * @pre {getEventType() == VALUE_DOUBLE}
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BOOL);
        replay(mockImpl);
        try {
            reader.getDoubleValue();
            fail("expected precondition violation @pre {getEventType() == VALUE_DOUBLE}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {getValueReadCount() < getValueCount()}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_DOUBLE);
        expect(mockImpl.getValueCount()).andReturn(10);
        expect(mockImpl.getValueReadCount()).andReturn(10);
        replay(mockImpl);
        try {
            reader.getDoubleValue();
            fail("expected precondition violation @pre {getValueReadCount() < getValueCount()}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_DOUBLE);
        expect(mockImpl.getValueCount()).andReturn(10);
        expect(mockImpl.getValueReadCount()).andReturn(9);
        expect(mockImpl.getDoubleValue()).andReturn(1D);
        replay(mockImpl);
        assertEquals(1D, reader.getDoubleValue(), 0);
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getFloatValue()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetFloatValue() throws IOException {
        // * @pre {getEventType() == VALUE_FLOAT}
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BOOL);
        replay(mockImpl);
        try {
            reader.getFloatValue();
            fail("expected precondition violation @pre {getEventType() == VALUE_FLOAT}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {getValueReadCount() < getValueCount()}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_FLOAT);
        expect(mockImpl.getValueCount()).andReturn(10);
        expect(mockImpl.getValueReadCount()).andReturn(10);
        replay(mockImpl);
        try {
            reader.getFloatValue();
            fail("expected precondition violation @pre {getValueReadCount() < getValueCount()}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_FLOAT);
        expect(mockImpl.getValueCount()).andReturn(10);
        expect(mockImpl.getValueReadCount()).andReturn(9);
        expect(mockImpl.getFloatValue()).andReturn(5F);
        replay(mockImpl);
        assertEquals(5F, reader.getFloatValue(), 0);
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getIntValue()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetIntValue() throws IOException {
        // * @pre {getEventType() == VALUE_INT}
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BOOL);
        replay(mockImpl);
        try {
            reader.getIntValue();
            fail("expected precondition violation @pre {getEventType() == VALUE_INT}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {getValueReadCount() < getValueCount()}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_INT);
        expect(mockImpl.getValueCount()).andReturn(10);
        expect(mockImpl.getValueReadCount()).andReturn(10);
        replay(mockImpl);
        try {
            reader.getIntValue();
            fail("expected precondition violation @pre {getValueReadCount() < getValueCount()}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_INT);
        expect(mockImpl.getValueCount()).andReturn(10);
        expect(mockImpl.getValueReadCount()).andReturn(9);
        expect(mockImpl.getIntValue()).andReturn(1);
        replay(mockImpl);
        assertEquals(1, reader.getIntValue());
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getLongValue()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetLongValue() throws IOException {
        // * @pre {getEventType() == VALUE_LONG}
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BOOL);
        replay(mockImpl);
        try {
            reader.getLongValue();
            fail("expected precondition violation @pre {getEventType() == VALUE_LONG}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {getValueReadCount() < getValueCount()}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_LONG);
        expect(mockImpl.getValueCount()).andReturn(10);
        expect(mockImpl.getValueReadCount()).andReturn(10);
        replay(mockImpl);
        try {
            reader.getLongValue();
            fail("expected precondition violation @pre {getValueReadCount() < getValueCount()}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_LONG);
        expect(mockImpl.getValueCount()).andReturn(10);
        expect(mockImpl.getValueReadCount()).andReturn(9);
        expect(mockImpl.getLongValue()).andReturn(1L);
        replay(mockImpl);
        assertEquals(1, reader.getLongValue());
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getStringValue()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetStringValue() throws IOException {
        // * @pre {getEventType().isValue() == true || getEventType() == {@link EventType#COMMENT}}
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        replay(mockImpl);
        try {
            reader.getStringValue();
            fail("expected precondition violation, event type is not a value or comment");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @post {$return != null}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_LONG);
        expect(mockImpl.getStringValue()).andReturn(null);
        replay(mockImpl);
        try {
            reader.getStringValue();
            fail("expected postcondition violation @post {$return != null}");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BYTE);
        expect(mockImpl.getStringValue()).andReturn("1");
        replay(mockImpl);
        assertEquals("1", reader.getStringValue());

        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.COMMENT);
        expect(mockImpl.getStringValue()).andReturn("comment");
        replay(mockImpl);
        assertEquals("comment", reader.getStringValue());
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getValue(boolean[], int, int)}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetValueBooleanArray() throws IOException {
        // * @pre {getEventType() == VALUE_BOOL}
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BYTE);
        replay(mockImpl);
        try {
            reader.getValue(new boolean[3], 0, 3);
            fail("expected precondition violation @pre {getEventType() == VALUE_BOOL}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {dst != null}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BOOL);
        replay(mockImpl);
        try {
            reader.getValue((boolean[]) null, 1, 3);
            fail("expected precondition violation @pre {dst != null}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {length <= (dst.length - offset)}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BOOL);
        replay(mockImpl);
        try {
            reader.getValue(new boolean[3], 1, 3);
            fail("expected precondition violation @pre {length <= (dst.length - offset)}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {getValueCount() - getValueReadCount() >= length}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BOOL);
        expect(mockImpl.getValueCount()).andReturn(3);
        expect(mockImpl.getValueReadCount()).andReturn(3);
        replay(mockImpl);
        try {
            reader.getValue(new boolean[3], 0, 3);
            fail("expected precondition violation @pre {getValueCount() - getValueReadCount() >= length}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BOOL);
        expect(mockImpl.getValueCount()).andReturn(3);
        expect(mockImpl.getValueReadCount()).andReturn(2);
        mockImpl.getValue(EasyMock.aryEq(new boolean[3]), anyInt(), anyInt());
        replay(mockImpl);
        reader.getValue(new boolean[3], 2, 1);
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getValue(byte[], int, int)}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetValueByteArray() throws IOException {
        // * @pre {getEventType() == VALUE_BYTE}
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BOOL);
        replay(mockImpl);
        try {
            reader.getValue(new byte[3], 0, 3);
            fail("expected precondition violation @pre {getEventType() == VALUE_BYTE}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {dst != null}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BYTE);
        replay(mockImpl);
        try {
            reader.getValue((byte[]) null, 1, 3);
            fail("expected precondition violation @pre {dst != null}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {length <= (dst.length - offset)}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BYTE);
        replay(mockImpl);
        try {
            reader.getValue(new byte[3], 1, 3);
            fail("expected precondition violation @pre {length <= (dst.length - offset)}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {getValueCount() - getValueReadCount() >= length}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BYTE);
        expect(mockImpl.getValueCount()).andReturn(3);
        expect(mockImpl.getValueReadCount()).andReturn(3);
        replay(mockImpl);
        try {
            reader.getValue(new byte[3], 0, 3);
            fail("expected precondition violation @pre {getValueCount() - getValueReadCount() >= length}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BYTE);
        expect(mockImpl.getValueCount()).andReturn(3);
        expect(mockImpl.getValueReadCount()).andReturn(2);
        mockImpl.getValue(EasyMock.aryEq(new byte[3]), anyInt(), anyInt());
        replay(mockImpl);
        reader.getValue(new byte[3], 2, 1);
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getValue(int[], int, int)}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetValueIntArray() throws IOException {
        // * @pre {getEventType() == VALUE_INT}
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BYTE);
        replay(mockImpl);
        try {
            reader.getValue(new int[3], 0, 3);
            fail("expected precondition violation @pre {getEventType() == VALUE_INT}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {dst != null}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_INT);
        replay(mockImpl);
        try {
            reader.getValue((boolean[]) null, 1, 3);
            fail("expected precondition violation @pre {dst != null}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {length <= (dst.length - offset)}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_INT);
        replay(mockImpl);
        try {
            reader.getValue(new int[3], 1, 3);
            fail("expected precondition violation @pre {length <= (dst.length - offset)}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {getValueCount() - getValueReadCount() >= length}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_INT);
        expect(mockImpl.getValueCount()).andReturn(3);
        expect(mockImpl.getValueReadCount()).andReturn(3);
        replay(mockImpl);
        try {
            reader.getValue(new int[3], 0, 3);
            fail("expected precondition violation @pre {getValueCount() - getValueReadCount() >= length}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_INT);
        expect(mockImpl.getValueCount()).andReturn(3);
        expect(mockImpl.getValueReadCount()).andReturn(2);
        mockImpl.getValue(EasyMock.aryEq(new int[3]), anyInt(), anyInt());
        replay(mockImpl);
        reader.getValue(new int[3], 2, 1);
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getValue(long[], int, int)}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetValueLongArray() throws IOException {
        // * @pre {getEventType() == VALUE_LONG}
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BYTE);
        replay(mockImpl);
        try {
            reader.getValue(new long[3], 0, 3);
            fail("expected precondition violation @pre {getEventType() == VALUE_LONG}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {dst != null}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_LONG);
        replay(mockImpl);
        try {
            reader.getValue((boolean[]) null, 1, 3);
            fail("expected precondition violation @pre {dst != null}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {length <= (dst.length - offset)}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_LONG);
        replay(mockImpl);
        try {
            reader.getValue(new long[3], 1, 3);
            fail("expected precondition violation @pre {length <= (dst.length - offset)}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {getValueCount() - getValueReadCount() >= length}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_LONG);
        expect(mockImpl.getValueCount()).andReturn(3);
        expect(mockImpl.getValueReadCount()).andReturn(3);
        replay(mockImpl);
        try {
            reader.getValue(new long[3], 0, 3);
            fail("expected precondition violation @pre {getValueCount() - getValueReadCount() >= length}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_LONG);
        expect(mockImpl.getValueCount()).andReturn(3);
        expect(mockImpl.getValueReadCount()).andReturn(2);
        mockImpl.getValue(EasyMock.aryEq(new long[3]), anyInt(), anyInt());
        replay(mockImpl);
        reader.getValue(new long[3], 2, 1);
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getValue(float[], int, int)}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetValueFloatArray() throws IOException {
        // * @pre {getEventType() == VALUE_FLOAT}
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BYTE);
        replay(mockImpl);
        try {
            reader.getValue(new float[3], 0, 3);
            fail("expected precondition violation @pre {getEventType() == VALUE_FLOAT}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {dst != null}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_FLOAT);
        replay(mockImpl);
        try {
            reader.getValue((boolean[]) null, 1, 3);
            fail("expected precondition violation @pre {dst != null}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {length <= (dst.length - offset)}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_FLOAT);
        replay(mockImpl);
        try {
            reader.getValue(new float[3], 1, 3);
            fail("expected precondition violation @pre {length <= (dst.length - offset)}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {getValueCount() - getValueReadCount() >= length}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_FLOAT);
        expect(mockImpl.getValueCount()).andReturn(3);
        expect(mockImpl.getValueReadCount()).andReturn(3);
        replay(mockImpl);
        try {
            reader.getValue(new float[3], 0, 3);
            fail("expected precondition violation @pre {getValueCount() - getValueReadCount() >= length}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_FLOAT);
        expect(mockImpl.getValueCount()).andReturn(3);
        expect(mockImpl.getValueReadCount()).andReturn(2);
        mockImpl.getValue(EasyMock.aryEq(new float[3]), anyInt(), anyInt());
        replay(mockImpl);
        reader.getValue(new float[3], 2, 1);
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getValue(double[], int, int)}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetValueDoubleArray() throws IOException {
        // * @pre {getEventType() == VALUE_DOUBLE}
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_BYTE);
        replay(mockImpl);
        try {
            reader.getValue(new double[3], 0, 3);
            fail("expected precondition violation @pre {getEventType() == VALUE_DOUBLE}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {dst != null}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_DOUBLE);
        replay(mockImpl);
        try {
            reader.getValue((boolean[]) null, 1, 3);
            fail("expected precondition violation @pre {dst != null}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {length <= (dst.length - offset)}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_DOUBLE);
        replay(mockImpl);
        try {
            reader.getValue(new double[3], 1, 3);
            fail("expected precondition violation @pre {length <= (dst.length - offset)}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @pre {getValueCount() - getValueReadCount() >= length}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_DOUBLE);
        expect(mockImpl.getValueCount()).andReturn(3);
        expect(mockImpl.getValueReadCount()).andReturn(3);
        replay(mockImpl);
        try {
            reader.getValue(new double[3], 0, 3);
            fail("expected precondition violation @pre {getValueCount() - getValueReadCount() >= length}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_DOUBLE);
        expect(mockImpl.getValueCount()).andReturn(3);
        expect(mockImpl.getValueReadCount()).andReturn(2);
        mockImpl.getValue(EasyMock.aryEq(new double[3]), anyInt(), anyInt());
        replay(mockImpl);
        reader.getValue(new double[3], 2, 1);
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getValueCount()}.
     */
    @Test
    public void testGetValueCount() {
        // * @pre {getEventType().isValue() == true}
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        replay(mockImpl);
        try {
            reader.getValueCount();
            fail("expected precondition violation @pre {getEventType().isValue() == true}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @post {$return >= 0}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_INT);
        expect(mockImpl.getValueCount()).andReturn(-1);
        replay(mockImpl);
        try {
            reader.getValueCount();
            fail("expected postcondition violation @pre {$return >= 0}");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_INT);
        expect(mockImpl.getValueCount()).andReturn(50);
        replay(mockImpl);
        assertEquals(50, reader.getValueCount());
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#next()}.
     * 
     * @throws IOException
     */
    @Test
    public void testNext() throws IOException {
        // * @pre {hasNext() == true}
        expect(mockImpl.hasNext()).andReturn(false);
        replay(mockImpl);
        try {
            reader.next();
            fail("Expected precondition violation @pre {hasNext() == true}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @post {$return != null}
        createReader();
        expect(mockImpl.hasNext()).andReturn(true);
        expect(mockImpl.next()).andReturn(null);
        replay(mockImpl);
        try {
            reader.next();
            fail("Expected postcondition violation @pre {$return != null}");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @post {$return == getEventType()}
        createReader();
        expect(mockImpl.hasNext()).andReturn(true);
        expect(mockImpl.next()).andReturn(EventType.COMMENT);
        expect(mockImpl.getEventType()).andReturn(EventType.START_ELEMENT);
        replay(mockImpl);
        try {
            reader.next();
            fail("Expected postcondition violation @pre {$return == getEventType()}");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.hasNext()).andReturn(true);
        expect(mockImpl.next()).andReturn(EventType.COMMENT);
        expect(mockImpl.getEventType()).andReturn(EventType.COMMENT);
        replay(mockImpl);
        assertEquals(EventType.COMMENT, reader.next());
        verify(mockImpl);

    }

    /**
     * Test method for contract of {@link BxmlStreamReader#nextTag()}.
     * 
     * @throws IOException
     */
    @Test
    public void testNextTag() throws IOException {
        // * @pre {hasNext() == true}
        expect(mockImpl.hasNext()).andReturn(false);
        replay(mockImpl);
        try {
            reader.nextTag();
            fail("Expected precondition violation @pre {hasNext() == true}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @post {$return != null}
        createReader();
        expect(mockImpl.hasNext()).andReturn(true);
        expect(mockImpl.nextTag()).andReturn(null);
        replay(mockImpl);
        try {
            reader.nextTag();
            fail("Expected postcondition violation @post {$return != null}");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @post {$return == getEventType()}
        createReader();
        expect(mockImpl.hasNext()).andReturn(true);
        expect(mockImpl.nextTag()).andReturn(EventType.START_ELEMENT);
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_DOUBLE);
        replay(mockImpl);
        try {
            reader.nextTag();
            fail("Expected postcondition violation @post {$return == getEventType()}");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @post {getEventType().isTag() == true}
        createReader();
        expect(mockImpl.hasNext()).andReturn(true);
        expect(mockImpl.nextTag()).andReturn(EventType.VALUE_DOUBLE);
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_DOUBLE);
        replay(mockImpl);
        try {
            reader.nextTag();
            fail("Expected postcondition violation @post {getEventType().isTag() == true}");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);
    }

    /**
     * Test method for contract of {@link BxmlStreamReader#getValueReadCount()}.
     * 
     * @throws IOException
     */
    @Test
    public void testValueReadCount() {
        // * @pre {getEventType().isValue() == true}
        expect(mockImpl.getEventType()).andReturn(EventType.NONE);
        replay(mockImpl);
        try {
            reader.getValueReadCount();
            fail("Expected precondition violation @pre {getEventType().isValue() == true}");
        } catch (PreconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // * @post {$return <= getValueCount()}
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_DOUBLE);
        expect(mockImpl.getValueReadCount()).andReturn(5);
        expect(mockImpl.getValueCount()).andReturn(4);
        replay(mockImpl);
        try {
            reader.getValueReadCount();
            fail("Expected violation of @post {$return <= getValueCount()}");
        } catch (PostconditionViolationException e) {
            assertTrue(true);
        }
        verify(mockImpl);

        // all conditions met...
        createReader();
        expect(mockImpl.getEventType()).andReturn(EventType.VALUE_DOUBLE);
        expect(mockImpl.getValueReadCount()).andReturn(5);
        expect(mockImpl.getValueCount()).andReturn(14);
        replay(mockImpl);
        assertEquals(5, reader.getValueReadCount());
        verify(mockImpl);
    }
}
