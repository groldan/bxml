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

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.gvsig.bxml.stream.EventType.COMMENT;
import static org.gvsig.bxml.stream.EventType.END_DOCUMENT;
import static org.gvsig.bxml.stream.EventType.END_ELEMENT;
import static org.gvsig.bxml.stream.EventType.NONE;
import static org.gvsig.bxml.stream.EventType.SPACE;
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
import static org.gvsig.bxml.stream.io.TokenType.AttributeListEnd;
import static org.gvsig.bxml.stream.io.TokenType.AttributeStart;
import static org.gvsig.bxml.stream.io.TokenType.CharContent;
import static org.gvsig.bxml.stream.io.TokenType.Comment;
import static org.gvsig.bxml.stream.io.TokenType.ContentAttrElement;
import static org.gvsig.bxml.stream.io.TokenType.ContentElement;
import static org.gvsig.bxml.stream.io.TokenType.ElementEnd;
import static org.gvsig.bxml.stream.io.TokenType.EmptyAttrElement;
import static org.gvsig.bxml.stream.io.TokenType.StringTable;
import static org.gvsig.bxml.stream.io.TokenType.Trailer;
import static org.gvsig.bxml.stream.io.TokenType.XmlDeclaration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.ByteOrder;

import javax.xml.namespace.QName;

import org.easymock.EasyMock;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.gvsig.bxml.stream.io.BxmlInputStream;
import org.gvsig.bxml.stream.io.CommentPositionHint;
import org.gvsig.bxml.stream.io.Header;
import org.gvsig.bxml.stream.io.TokenType;
import org.gvsig.bxml.stream.io.ValueType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test suite for {@link DefaultBxmlStreamReader}
 * <p>
 * NOTE: some {@code testNextXXXToken()} test cases javadocs define the allowed structure that
 * follows the given token type. The syntax used is as follows:
 * <ul>
 * <li>{@code [XXXToken]}: a single instance of XXXToken is expected
 * <li>{@code [XXXToken]+}: one or more instance of XXXToken may occur
 * <li>{@code <XXXToken>}: zero or one instance of XXXToken may occur
 * <li>{@code <XXXToken>*}: zero or more instances of XXXToken may occur
 * </ul>
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class DefaultBxmlStreamReaderTest {

    private TestDefaultBxmlScanner scanner;

    private BxmlInputStream mockInput;

    @Before
    public void setUp() throws Exception {
        // create mock object
        mockInput = createMock(BxmlInputStream.class);
        // record expected behavior (constructor calls getHeader())
        expect(mockInput.getHeader()).andReturn(Header.DEFAULT);
        recordMockXmlDeclaration("1.0", false, false);
    }

    private void recordMockXmlDeclaration(String version, boolean standalone,
            boolean standaloneIsSet) throws IOException {
        expect(mockInput.readTokenType()).andReturn(XmlDeclaration);
        // version
        expect(mockInput.readString()).andReturn(version);
        // standalone
        expect(mockInput.readBoolean()).andReturn(standalone);
        // standaloneIsSet
        expect(mockInput.readBoolean()).andReturn(standaloneIsSet);
    }

    @After
    public void tearDown() throws Exception {
        mockInput = null;
    }

    /**
     * Is {@link BxmlInputStream#getHeader()} called at {@link DefaultBxmlInputStream} construction
     * time?
     * 
     * @throws IOException
     */
    @Test
    public void testGetHeader() throws IOException {
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        verify(mockInput);
    }

    /**
     * Check the query methods get the information needed from the header
     * 
     * @throws IOException
     */
    @Test
    public void testGetHeaderMetadata() throws IOException {
        replay(mockInput);
        // use default header settings
        final Header header = Header.DEFAULT;
        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(header.getCharactersEncoding(), scanner.getCharset());
        assertEquals("1.0", scanner.getXmlVersion());
        assertFalse(scanner.isStandalone());
        assertFalse(scanner.standAloneIsSet());
        boolean isLittleEndian = header.getFlags().getEndianess() == ByteOrder.LITTLE_ENDIAN;
        assertEquals(isLittleEndian, scanner.isLittleEndian());
        assertFalse(scanner.isValidated());
    }

    /**
     * Are the xmlVersion, standalone and standaloneIsSet derived properties from the
     * XmlDeclarationToken correctly acquired, and the defaults used if there are no
     * XmlDeclarationToken right after the header?
     * 
     * @throws IOException
     */
    @Test
    public void testXmlDeclarationDerivedProperties() throws IOException {
        // create mock object
        mockInput = createMock(BxmlInputStream.class);
        // record expected behavior (constructor calls getHeader())
        expect(mockInput.getHeader()).andReturn(Header.DEFAULT);
        recordMockXmlDeclaration("1.0", false, false);
        replay(mockInput);
        scanner = new TestDefaultBxmlScanner(mockInput);
        assertEquals("1.0", scanner.getXmlVersion());
        assertFalse(scanner.isStandalone());
        assertFalse(scanner.standAloneIsSet());
        verify(mockInput);

        mockInput = createMock(BxmlInputStream.class);
        expect(mockInput.getHeader()).andReturn(Header.DEFAULT);
        recordMockXmlDeclaration("1.1", true, true);
        replay(mockInput);
        scanner = new TestDefaultBxmlScanner(mockInput);
        assertEquals("1.1", scanner.getXmlVersion());
        assertTrue(scanner.isStandalone());
        assertTrue(scanner.standAloneIsSet());
        verify(mockInput);

        // now with no XmlDeclarationToken defined
        mockInput = createMock(BxmlInputStream.class);
        expect(mockInput.getHeader()).andReturn(Header.DEFAULT);
        // insted of xmldecl, first token is an element start
        expect(mockInput.readTokenType()).andReturn(ContentElement);
        replay(mockInput);
        scanner = new TestDefaultBxmlScanner(mockInput);
        assertEquals("1.0", scanner.getXmlVersion());
        assertTrue(scanner.isStandalone());
        assertFalse(scanner.standAloneIsSet());
        verify(mockInput);
    }

    /**
     * Does {@link DefaultBxmlStreamReader#close()} actually calls {@link BxmlInputStream#close()}?
     * 
     * @throws IOException
     */
    @Test
    public void testClose() throws IOException {
        expect(mockInput.isOpen()).andReturn(true);
        mockInput.close();
        expect(mockInput.isOpen()).andReturn(false);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertTrue(scanner.isOpen());
        scanner.close();
        assertFalse(scanner.isOpen());

        verify(mockInput);
    }

    /**
     * After {@link BxmlStreamReader} is created, next() shall be called at least once so
     * {@link BxmlStreamReader#getEventType()} does not throws {@link IllegalStateException}.
     * 
     * @throws IOException
     */
    @Test
    public void testNextNotCalled() throws IOException {
        replay(mockInput);
        scanner = new TestDefaultBxmlScanner(mockInput);
        EventType eventType = scanner.getEventType();
        assertSame(NONE, eventType);
    }

    /**
     * Is {@link EventType#START_DOCUMENT} the first event if the XmlDeclaration token is not set?
     * 
     * @throws IOException
     */
    @Test
    public void testNextStartDocumentNoXmlDeclaration() throws IOException {
        // create the mock again to override the one created on setUp()
        // which already contains the XmlDeclaration token
        mockInput = createMock(BxmlInputStream.class);
        // record expected behavior (constructor calls getHeader())
        expect(mockInput.getHeader()).andReturn(Header.DEFAULT);

        expect(mockInput.readTokenType()).andReturn(EmptyAttrElement);

        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);

        assertTrue(scanner.hasNext());
        EventType next = scanner.next();
        Assert.assertSame(EventType.START_DOCUMENT, next);
        verify(mockInput);
    }

    /**
     * If the XmlDeclarationToken is specified, the first event also is START_DOCUMENT
     * 
     * @throws IOException
     */
    @Test
    public void testNextStartDocumentXmlDeclaration() throws IOException {
        replay(mockInput);
        scanner = new TestDefaultBxmlScanner(mockInput);

        assertTrue(scanner.hasNext());
        EventType next = scanner.next();
        Assert.assertSame(EventType.START_DOCUMENT, next);
        verify(mockInput);
    }

    /**
     * First event after start document may be a comment instead of start element
     * 
     * @throws IOException
     */
    @Test
    public void testNextStartDocumentThenComment() throws IOException {
        expect(mockInput.readTokenType()).andReturn(Comment);
        expect(mockInput.readByte()).andReturn(CommentPositionHint.INDENTED.getCode());
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        EventType next = scanner.next();
        Assert.assertSame(EventType.START_DOCUMENT, next);

        next = scanner.next();
        Assert.assertSame(EventType.COMMENT, next);
        verify(mockInput);
    }

    /**
     * Check behavior when next() is called and an {@link TokenType#EmptyAttrElement} token is
     * found.
     * <p>
     * {@code next()} has to return {@link EventType#START_ELEMENT}, another call to {@code next()}
     * shall return {@link EventType#ATTRIBUTE}, and a last call {@link EventType#END_ELEMENT}.
     * </p>
     * <p>
     * {@code [EmptyAttrElementToken],<StringTableToken>,[AttributeStartToken]+,[AttributeListEndToken] }
     * </p>
     * 
     * @throws IOException
     */
    @Test
    public void testNextEmptyAttrElementToken() throws IOException {

        // record this: <element attr1="value1"/>
        expect(mockInput.getPosition()).andReturn(50L);
        expect(mockInput.readTokenType()).andReturn(EmptyAttrElement);
        // count index for element name
        expect(mockInput.readCount()).andReturn(0L);

        expect(mockInput.readTokenType()).andReturn(AttributeStart);
        expect(mockInput.readCount()).andReturn(0L);
        expect(mockInput.getPosition()).andReturn(55L);
        expect(mockInput.readTokenType()).andReturn(AttributeListEnd);

        // expect(mockInput.readTokenType()).andReturn(AttributeStart);
        // count index for element name
        // expect(mockInput.readCount()).andReturn(1L);

        // set mock to replay mode
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        // pre-populate string table as we're not providing it
        scanner.addToStringTable("attr1");

        Assert.assertSame(EventType.START_DOCUMENT, scanner.next());
        Assert.assertSame(EventType.START_ELEMENT, scanner.next());
        Assert.assertSame(EventType.END_ELEMENT, scanner.next());

        verify(mockInput);
    }

    /**
     * Asserts nextTag() produces the skipping of content until a tag token is found
     * 
     * @throws IOException
     */
    @Test
    public void testNextTag() throws IOException {
        mockStringTable(new String[] { "prefix:ElemName" });
        mockContentElem(0);

        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();

        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        // expected to skip the WhiteSpace token content
        expect(mockInput.readCount()).andReturn(5L);// nBlankLines field
        mockInput.skipString();
        // another non tag token to skip...
        expect(mockInput.readTokenType()).andReturn(TokenType.Comment);
        // skipping the comment content...
        expect(mockInput.readByte()).andReturn(CommentPositionHint.END_OF_LINE.getCode());
        mockInput.skipString();
        // now got a tag token
        expect(mockInput.readTokenType()).andReturn(TokenType.ElementEnd);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        final EventType startTag = scanner.nextTag();
        assertSame(START_ELEMENT, startTag);

        final EventType nextTag = scanner.nextTag();
        assertSame(END_ELEMENT, nextTag);

        verify(mockInput);
    }

    /**
     * An attribute content may be empty, in which case it is valid for it not to be followed by any
     * content token, but another AttributeStartToken or AttributeListEnd token may follow it.
     * 
     * @throws IOException
     */
    @Test
    public void testEmptyAttributeEvent() throws IOException {
        mockEmptyAttrElement(0);
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();

        // first attribute with no value
        expect(mockInput.readTokenType()).andReturn(AttributeStart);
        expect(mockInput.readCount()).andReturn(1L);

        // second attribute with no value
        expect(mockInput.readTokenType()).andReturn(AttributeStart);
        expect(mockInput.readCount()).andReturn(2L);

        mockAttributeListEnd();

        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        // pre-populate string table as we're not providing it
        scanner.addToStringTable("elementName");
        scanner.addToStringTable("attr1");
        scanner.addToStringTable("attr2");

        assertSame(START_DOCUMENT, scanner.next());
        assertSame(START_ELEMENT, scanner.next());
        assertEquals("attr2", scanner.getAttributeName(0).getLocalPart());
        assertEquals("attr1", scanner.getAttributeName(1).getLocalPart());
        verify(mockInput);
    }

    /**
     * Assert {@code getAttributeName()} is correctly handled when {@code getEventType() ==
     * ATTRIBUTE} and throws an {@code IllegalStateException} otherwise.
     * 
     * @throws IOException
     */
    @Test
    public void testGetAttributeName() throws IOException {
        mockEmptyAttrElement(0);
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();

        recordMockAttribute(mockInput, "attr1", null, 1L);
        mockAttributeListEnd();

        // set mock to replay mode
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        // pre-populate string table as we're not providing it
        scanner.addToStringTable("elementName");
        scanner.addToStringTable("attr1");

        // note the mock document content is not valid, yet the scanner does not do any well
        // formedness check
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(START_ELEMENT, scanner.next());

        assertEquals("attr1", scanner.getAttributeName(0).getLocalPart());
        // more calls should not result in more calls to the reader...
        assertEquals("attr1", scanner.getAttributeName(0).getLocalPart());
        assertEquals("attr1", scanner.getAttributeName(0).getLocalPart());

        verify(mockInput);
    }

    /**
     * Assert an attribute value is correctly handled when an ATTRIBUTE event is followed by a
     * VALUE_XXX event and throws an {@code IllegalStateException} otherwise.
     * 
     * @throws IOException
     */
    @Test
    public void testGetAttributeValue() throws IOException {
        mockEmptyAttrElement(0);
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();

        recordMockAttribute(mockInput, "attr1", "attr1Value", 1L);
        mockAttributeListEnd();
        // set mock to replay mode
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        // pre-populate string table as we're not providing it
        scanner.addToStringTable("elementName1");
        scanner.addToStringTable("attr1");

        Assert.assertSame(EventType.START_DOCUMENT, scanner.next());
        Assert.assertSame(EventType.START_ELEMENT, scanner.next());
        Assert.assertEquals("attr1Value", scanner.getAttributeValue(0));
        Assert.assertSame(EventType.END_ELEMENT, scanner.next());

        verify(mockInput);
    }

    /**
     * Check that when calling getStringValue and the current event type is a primitive value array,
     * the content is correctly encoded and returned as a String
     * 
     * @throws IOException
     * @see {@link DefaultBxmlStreamReader#getStringValue()}
     */
    @Test
    public void testGetStringValue() throws IOException {
        // starts the call to next()
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        // content is an array
        expect(mockInput.readByte()).andReturn(ValueType.ArrayCode.getCode());
        // of type double
        expect(mockInput.readByte()).andReturn(ValueType.DoubleCode.getCode());
        // with 5 elements
        expect(mockInput.readCount()).andReturn(5L);
        // call to next() finished

        // now the call to getStringValue lays down to 5 getDoubleValue calls
        expect(mockInput.readDouble()).andReturn(0.1D);
        expect(mockInput.readDouble()).andReturn(0.2D);
        expect(mockInput.readDouble()).andReturn(0.3D);
        expect(mockInput.readDouble()).andReturn(0.4D);
        expect(mockInput.readDouble()).andReturn(0.5D);

        replay(mockInput);
        scanner = new TestDefaultBxmlScanner(mockInput);

        Assert.assertSame(EventType.START_DOCUMENT, scanner.next());

        // here starts what we recorded in this method
        Assert.assertSame(EventType.VALUE_DOUBLE, scanner.next());
        Assert.assertSame(EventType.VALUE_DOUBLE, scanner.getEventType());
        Assert.assertSame(5, scanner.getValueCount());

        final String expectedStringValue = 0.1 + " " + 0.2 + " " + 0.3 + " " + 0.4 + " " + 0.5;
        String stringValue = scanner.getStringValue();
        assertEquals(expectedStringValue, stringValue);

        verify(mockInput);
    }

    /**
     * As first event is START_DOCUMENT, if next() was not yet called, hasNext() always returns
     * true, since at least the END_DOCUMENT event is remaining.
     * 
     * @throws IOException
     */
    @Test
    public void testHasNextJustCreated() throws IOException {
        // header set and xmldecl read on constructor
        replay(mockInput);
        scanner = new TestDefaultBxmlScanner(mockInput);
        assertTrue(scanner.hasNext());
        verify(mockInput);
    }

    /**
     * An empty document generates only START_DOCUMENT and END_DOCUMENT events, so hasNext() shall
     * return true before the first call to next() and false after that.
     * 
     * @throws IOException
     */
    @Test
    public void testHasNextEmptyDocument() throws IOException {
        expect(mockInput.readTokenType()).andReturn(Trailer);
        // header set and xmldecl read on constructor
        replay(mockInput);
        scanner = new TestDefaultBxmlScanner(mockInput) {
            // @Override
            // protected void readTrailer() {
            // // do nothing
            // }
        };

        assertTrue(scanner.hasNext());
        // START_DOCUMENT
        scanner.next();
        assertTrue(scanner.hasNext());

        // END_DOCUMENT
        scanner.next();
        assertFalse(scanner.hasNext());
        verify(mockInput);
    }

    /**
     * Tests consistency between getValueCount and getValueReadCount while getting a value in
     * chunks.
     * 
     * @throws IOException
     */
    @Test
    public void testGetValueReadCount() throws IOException {
        mockStringTable(new String[] { "prefix:ElemName" });
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();

        mockContentElem(0);
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        // content is an array
        expect(mockInput.readByte()).andReturn(ValueType.ArrayCode.getCode());
        // of type bool
        expect(mockInput.readByte()).andReturn(ValueType.BoolCode.getCode());
        // with 50 elements
        expect(mockInput.readCount()).andReturn(6L);
        // record the sequence of reads we'll use bellow
        expect(mockInput.readBoolean()).andReturn(true);
        mockInput.readBoolean(aryEq(new boolean[10]), anyInt(), anyInt());
        expect(mockInput.readBoolean()).andReturn(false);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(START_ELEMENT, scanner.next());
        assertSame(VALUE_BOOL, scanner.next());

        assertEquals(6, scanner.getValueCount());
        assertEquals(0, scanner.getValueReadCount());

        scanner.getBooleanValue();
        assertEquals(1, scanner.getValueReadCount());
        // get the 4 of the 5 values remaining, this time in bulk,
        scanner.getValue(new boolean[10], 2, 4);
        // get the last element in the array as a single element
        scanner.getBooleanValue();

        // we don't really need to check this, we can just rely on the contract enforcement provided
        // by the factory. Yet the ParseState.notifyReadCount(int) method is doing a bounds check
        try {
            scanner.getBooleanValue();
            fail("Expected exception while asking for more values than available");
        } catch (RuntimeException e) {
            assertTrue(true);
        }
    }

    /**
     * Check parsing of a {@link TokenType#CharContentRef} token as a string value event
     */
    @Test
    public void testCharContentRefValue() throws IOException {
        mockStringTable(new String[] { "string_0", "string_1" });
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();

        expect(mockInput.readTokenType()).andReturn(TokenType.CharContentRef);
        expect(mockInput.readCount()).andReturn(1L);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertEquals(START_DOCUMENT, scanner.next());
        assertEquals(VALUE_STRING, scanner.next());
        assertEquals("string_1", scanner.getStringValue());
        verify(mockInput);
    }

    /**
     * Check parsing of a {@link TokenType#CharEntityRef} token as a string value event
     */
    // @Test
    public void testCharEntityRefValue() throws IOException {
        fail("implement");
    }

    /**
     * Check parsing of a {@link TokenType#EntityRef} token as a string value event
     */
    // @Test
    public void testEntityRefValue() throws IOException {
        fail("implement");
    }

    /**
     * Check parsing of a {@link TokenType#CDataSection} token as a CDATA value event
     */
    @Test
    public void testCDataValue() throws IOException {
        // record a CData section whose value is an array of 5 ints
        expect(mockInput.readTokenType()).andReturn(TokenType.CDataSection);
        expect(mockInput.readByte()).andReturn(ValueType.ArrayCode.getCode());
        expect(mockInput.readByte()).andReturn(ValueType.IntCode.getCode());
        expect(mockInput.readCount()).andReturn(5L);
        // to get the value as a string, expect 5 readInt() calls
        expect(mockInput.readInt()).andReturn(1);
        expect(mockInput.readInt()).andReturn(2);
        expect(mockInput.readInt()).andReturn(3);
        expect(mockInput.readInt()).andReturn(4);
        expect(mockInput.readInt()).andReturn(5);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertEquals(START_DOCUMENT, scanner.next());
        assertEquals(VALUE_CDATA, scanner.next());
        assertEquals("1 2 3 4 5", scanner.getStringValue());
        verify(mockInput);
    }

    /**
     * Check a CDATA value event skips content
     */
    @Test
    public void testCDataValueSkip() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();

        // record a CData section whose value is an array of 5 ints
        expect(mockInput.readTokenType()).andReturn(TokenType.CDataSection);
        expect(mockInput.readByte()).andReturn(ValueType.ArrayCode.getCode());
        expect(mockInput.readByte()).andReturn(ValueType.IntCode.getCode());
        expect(mockInput.readCount()).andReturn(5L);
        // calling next without having read the value should lead to this skip call
        mockInput.skip(5 * ValueType.INTEGER_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertEquals(START_DOCUMENT, scanner.next());
        assertEquals(VALUE_CDATA, scanner.next());
        // calling next() again skips the CData content
        assertEquals(SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Check parsing of a {@link TokenType#Whitespace} token as a SPACE value event
     */
    @Test
    public void testWhiteSpaceValue() throws IOException {
        // record a WhiteSpace content token with 3 newlines and 5 space characters
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        // nBlankLines count
        expect(mockInput.readCount()).andReturn(3L);
        // whitespace content
        expect(mockInput.readString()).andReturn("     ");// 5 spaces
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertEquals(START_DOCUMENT, scanner.next());
        assertEquals(SPACE, scanner.next());
        StringBuffer sb = new StringBuffer();
        sb.append('\n');
        sb.append('\n');
        sb.append('\n');
        sb.append("     ");
        final String expected = sb.toString();
        assertEquals(expected, scanner.getStringValue());
        verify(mockInput);
    }

    /**
     * Check {@link TokenType#Whitespace} token is correctly skipped
     */
    @Test
    public void testWhiteSpaceValueSkip() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();

        // record a WhiteSpace content token with 3 newlines and 5 space characters
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        // nBlankLines count, read as part of the skip process
        expect(mockInput.readCount()).andReturn(3L);
        // but the string content is not read but skipped
        mockInput.skipString();

        expect(mockInput.readTokenType()).andReturn(TokenType.Comment);
        expect(mockInput.readByte()).andReturn(CommentPositionHint.END_OF_LINE.getCode());
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertEquals(START_DOCUMENT, scanner.next());
        assertEquals(SPACE, scanner.next());
        // calling next() without having read the contents produces the skip
        assertEquals(COMMENT, scanner.next());
        verify(mockInput);
    }

    /**
     * Check parsing of a {@link TokenType#StringTable} token
     */
    @Test
    public void testStringTableTokenParsing() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();

        expect(mockInput.readTokenType()).andReturn(TokenType.StringTable);
        expect(mockInput.readCount()).andReturn(2L);
        expect(mockInput.readString()).andReturn("entry1");
        expect(mockInput.readString()).andReturn("entry2");
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);

        // this one is produced by the mocking up in setUp()
        assertSame(EventType.START_DOCUMENT, scanner.next());
        // this one is the space event returned after the string table was parsed and continued with
        // the filter chain, since a StringTable token does not map to any EventType
        assertSame(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Test method for {@link DefaultBxmlStreamReader#getBooleanValue()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetBooleanValue() throws IOException {
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.BoolCode.getCode());
        expect(mockInput.readBoolean()).andReturn(true);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_BOOL, scanner.next());
        assertEquals(true, scanner.getBooleanValue());
        verify(mockInput);
    }

    /**
     * Is a boolean value skipped if not consumed? (ie, next() called without reading the value)
     * 
     * @throws IOException
     */
    @Test
    public void testGetBooleanValueSkip() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();

        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.BoolCode.getCode());
        mockInput.skip(ValueType.BOOLEAN_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_BOOL, scanner.next());
        assertEquals(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Test method for {@link DefaultBxmlStreamReader#getByteValue()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetByteValue() throws IOException {
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.ByteCode.getCode());
        expect(mockInput.readByte()).andReturn(1);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_BYTE, scanner.next());
        assertEquals(1, scanner.getByteValue());
        verify(mockInput);
    }

    /**
     * Is a byte value skipped if not consumed? (ie, next() called without reading the value)
     * 
     * @throws IOException
     */
    @Test
    public void testGetByteValueSkip() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.ByteCode.getCode());
        mockInput.skip(ValueType.BYTE_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_BYTE, scanner.next());
        assertEquals(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Test method for {@link DefaultBxmlStreamReader#getIntValue()} when the actual value type is
     * {@link ValueType#IntCode}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetIntValue() throws IOException {
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.IntCode.getCode());
        expect(mockInput.readInt()).andReturn(1000);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_INT, scanner.next());
        assertEquals(1000, scanner.getIntValue());
        verify(mockInput);
    }

    /**
     * Is an int value skipped if not consumed? (ie, next() called without reading the value), for
     * the case where the int value maps a {@link ValueType#IntCode int} value
     * 
     * @throws IOException
     */
    @Test
    public void testGetIntValueSkip() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.IntCode.getCode());
        mockInput.skip(ValueType.INTEGER_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_INT, scanner.next());
        assertEquals(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Test method for {@link DefaultBxmlStreamReader#getIntValue()} when the actual value type is
     * {@link ValueType#SmallNum}.
     * <p>
     * SmallNum (a special purpose value ranging from 0 to 239, that does not needs a value type
     * identifier preceeding it, is mapped to a VALUE_INT event). SmallNum is not defined as a valid
     * array elements though, since the array header already has a single byte identifying the
     * content type, using smallnum as array values wouldn't get any benefit over an array of bytes.
     * </p>
     * 
     * @throws IOException
     */
    @Test
    public void testGetIntValueFromSmallNum() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        // this is the minimum SmallNum value
        expect(mockInput.readByte()).andReturn(0);
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        // this is the maximum SmallNum value
        expect(mockInput.readByte()).andReturn(239);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_INT, scanner.next());
        assertEquals(0, scanner.getIntValue());
        assertSame(VALUE_INT, scanner.next());
        assertEquals(239, scanner.getIntValue());
        verify(mockInput);
    }

    /**
     * Is an int value skipped if not consumed? (ie, next() called without reading the value), for
     * the case where the int value maps a {@link ValueType#SmallNum small num} value
     * 
     * @throws IOException
     */
    @Test
    public void testGetIntValueFromSmallNumSkip() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        // this is the max SmallNum value
        expect(mockInput.readByte()).andReturn(239);
        // no need to call skip()..., its a SmallNum
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_INT, scanner.next());
        assertEquals(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Test method for {@link DefaultBxmlStreamReader#getIntValue()} when the actual value type is
     * {@link ValueType#UShortCode}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetIntValueFromUShort() throws IOException {
        final int expectedValue = ValueType.UShortCode.getUpperLimit().intValue();
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.UShortCode.getCode());
        expect(mockInput.readUShort()).andReturn(expectedValue);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_INT, scanner.next());
        assertEquals(expectedValue, scanner.getIntValue());
        verify(mockInput);
    }

    /**
     * Is an int value skipped if not consumed? (ie, next() called without reading the value), for
     * the case where the int value maps a {@link ValueType#UShortCode ushort} value
     * 
     * @throws IOException
     */
    @Test
    public void testGetIntValueFromUShortSkip() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.UShortCode.getCode());
        mockInput.skip(ValueType.USHORT_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_INT, scanner.next());
        assertEquals(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Test method for {@link DefaultBxmlStreamReader#getIntValue()} when the actual value type is
     * {@link ValueType#ShortCode}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetIntValueFromShort() throws IOException {
        final short expectedValue = ValueType.ShortCode.getUpperLimit().shortValue();
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.ShortCode.getCode());
        expect(mockInput.readShort()).andReturn(expectedValue);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_INT, scanner.next());
        assertEquals(expectedValue, scanner.getIntValue());
        verify(mockInput);
    }

    /**
     * Is an int value skipped if not consumed? (ie, next() called without reading the value), for
     * the case where the int value maps a {@link ValueType#ShortCode short} value
     * 
     * @throws IOException
     */
    @Test
    public void testGetIntValueFromShortSkip() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.ShortCode.getCode());
        mockInput.skip(ValueType.SHORT_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_INT, scanner.next());
        assertEquals(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Test method for {@link DefaultBxmlStreamReader#getFloatValue()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetFloatValue() throws IOException {
        final float expectedValue = ValueType.FloatCode.getUpperLimit().floatValue();
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.FloatCode.getCode());
        expect(mockInput.readFloat()).andReturn(expectedValue);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_FLOAT, scanner.next());
        assertEquals(expectedValue, scanner.getFloatValue(), 0);
        verify(mockInput);
    }

    /**
     * Is a float value skipped if not consumed? (ie, next() called without reading the value)
     * 
     * @throws IOException
     */
    @Test
    public void testGetFloatValueSkip() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.FloatCode.getCode());
        mockInput.skip(ValueType.FLOAT_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_FLOAT, scanner.next());
        assertEquals(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Test method for {@link DefaultBxmlStreamReader#getLongValue()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetLongValue() throws IOException {
        final long expectedValue = ValueType.LongCode.getUpperLimit().longValue();
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.LongCode.getCode());
        expect(mockInput.readLong()).andReturn(expectedValue);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_LONG, scanner.next());
        assertEquals(expectedValue, scanner.getLongValue());
        verify(mockInput);
    }

    /**
     * Is a long value skipped if not consumed? (ie, next() called without reading the value)
     * 
     * @throws IOException
     */
    @Test
    public void testGetLongValueSkip() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.LongCode.getCode());
        mockInput.skip(ValueType.LONG_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_LONG, scanner.next());
        assertEquals(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Test method for {@link DefaultBxmlStreamReader#getDoubleValue()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetDoubleValue() throws IOException {
        final double expectedValue = ValueType.DoubleCode.getUpperLimit().doubleValue();
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.DoubleCode.getCode());
        expect(mockInput.readDouble()).andReturn(expectedValue);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_DOUBLE, scanner.next());
        assertEquals(expectedValue, scanner.getDoubleValue(), 0);
        verify(mockInput);
    }

    /**
     * Is a double value skipped if not consumed? (ie, next() called without reading the value)
     * 
     * @throws IOException
     */
    @Test
    public void testGetDoubleValueSkip() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // read
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.DoubleCode.getCode());
        mockInput.skip(ValueType.DOUBLE_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_DOUBLE, scanner.next());
        assertEquals(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Test method for {@link DefaultBxmlStreamReader#getValue(boolean[], int, int)}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetValueBooleanArray() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // record a 10 element array
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.ArrayCode.getCode());
        expect(mockInput.readByte()).andReturn(ValueType.BoolCode.getCode());
        expect(mockInput.readCount()).andReturn(10L);
        mockInput.readBoolean(aryEq(new boolean[10]), EasyMock.eq(4), EasyMock.eq(5));
        mockInput.skip(5 * ValueType.BOOLEAN_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_BOOL, scanner.next());
        assertEquals(10, scanner.getValueCount());
        assertEquals(0, scanner.getValueReadCount());
        // read the first 5 elements ...
        scanner.getValue(new boolean[10], 4, 5);
        assertEquals(5, scanner.getValueReadCount());
        // skip the last 5 elements...
        assertSame(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Test method for {@link DefaultBxmlStreamReader#getValue(byte[], int, int)}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetValueByteArray() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // record a 10 element array
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.ArrayCode.getCode());
        expect(mockInput.readByte()).andReturn(ValueType.ByteCode.getCode());
        expect(mockInput.readCount()).andReturn(10L);
        mockInput.readByte(aryEq(new byte[10]), EasyMock.eq(4), EasyMock.eq(5));
        mockInput.skip(5 * ValueType.BYTE_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_BYTE, scanner.next());
        assertEquals(10, scanner.getValueCount());
        assertEquals(0, scanner.getValueReadCount());
        // read the first 5 elements ...
        scanner.getValue(new byte[10], 4, 5);
        assertEquals(5, scanner.getValueReadCount());
        // skip the last 5 elements...
        assertSame(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Test method for {@link DefaultBxmlStreamReader#getValue(int[], int, int)}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetValueIntArray() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // record a 10 element array
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.ArrayCode.getCode());
        expect(mockInput.readByte()).andReturn(ValueType.IntCode.getCode());
        expect(mockInput.readCount()).andReturn(10L);
        mockInput.readInt(aryEq(new int[10]), EasyMock.eq(4), EasyMock.eq(5));
        mockInput.skip(5 * ValueType.INTEGER_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_INT, scanner.next());
        assertEquals(10, scanner.getValueCount());
        assertEquals(0, scanner.getValueReadCount());
        // read the first 5 elements ...
        scanner.getValue(new int[10], 4, 5);
        assertEquals(5, scanner.getValueReadCount());
        // skip the last 5 elements...
        assertSame(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    @Test
    public void testGetValueIntArrayFromShort() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // record a 10 element array
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.ArrayCode.getCode());
        expect(mockInput.readByte()).andReturn(ValueType.ShortCode.getCode());
        expect(mockInput.readCount()).andReturn(10L);
        mockInput.readShort(aryEq(new int[10]), EasyMock.eq(4), EasyMock.eq(5));
        mockInput.skip(5 * ValueType.SHORT_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_INT, scanner.next());
        assertEquals(10, scanner.getValueCount());
        assertEquals(0, scanner.getValueReadCount());
        // read the first 5 elements ...
        scanner.getValue(new int[10], 4, 5);
        assertEquals(5, scanner.getValueReadCount());
        // skip the last 5 elements...
        assertSame(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    @Test
    public void testGetValueIntArrayFromUShort() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // record a 10 element array
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.ArrayCode.getCode());
        expect(mockInput.readByte()).andReturn(ValueType.UShortCode.getCode());
        expect(mockInput.readCount()).andReturn(10L);
        mockInput.readUShort(aryEq(new int[10]), EasyMock.eq(4), EasyMock.eq(5));
        mockInput.skip(5 * ValueType.SHORT_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_INT, scanner.next());
        assertEquals(10, scanner.getValueCount());
        assertEquals(0, scanner.getValueReadCount());
        // read the first 5 elements ...
        scanner.getValue(new int[10], 4, 5);
        assertEquals(5, scanner.getValueReadCount());
        // skip the last 5 elements...
        assertSame(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Test method for {@link DefaultBxmlStreamReader#getValue(long[], int, int)}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetValueLongArray() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // record a 10 element array
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.ArrayCode.getCode());
        expect(mockInput.readByte()).andReturn(ValueType.LongCode.getCode());
        expect(mockInput.readCount()).andReturn(10L);
        mockInput.readLong(aryEq(new long[10]), EasyMock.eq(4), EasyMock.eq(5));
        mockInput.skip(5 * ValueType.LONG_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_LONG, scanner.next());
        assertEquals(10, scanner.getValueCount());
        assertEquals(0, scanner.getValueReadCount());
        // read the first 5 elements ...
        scanner.getValue(new long[10], 4, 5);
        assertEquals(5, scanner.getValueReadCount());
        // skip the last 5 elements...
        assertSame(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Test method for {@link DefaultBxmlStreamReader#getValue(float[], int, int)}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetValueFloatArray() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // record a 10 element array
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.ArrayCode.getCode());
        expect(mockInput.readByte()).andReturn(ValueType.FloatCode.getCode());
        expect(mockInput.readCount()).andReturn(10L);
        mockInput.readFloat(aryEq(new float[10]), EasyMock.eq(4), EasyMock.eq(5));
        mockInput.skip(5 * ValueType.FLOAT_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_FLOAT, scanner.next());
        assertEquals(10, scanner.getValueCount());
        assertEquals(0, scanner.getValueReadCount());
        // read the first 5 elements ...
        scanner.getValue(new float[10], 4, 5);
        assertEquals(5, scanner.getValueReadCount());
        // skip the last 5 elements...
        assertSame(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Test method for {@link DefaultBxmlStreamReader#getValue(double[], int, int)}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetValueDoubleArray() throws IOException {
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        // record a 10 element array
        expect(mockInput.readTokenType()).andReturn(TokenType.CharContent);
        expect(mockInput.readByte()).andReturn(ValueType.ArrayCode.getCode());
        expect(mockInput.readByte()).andReturn(ValueType.DoubleCode.getCode());
        expect(mockInput.readCount()).andReturn(10L);
        mockInput.readDouble(aryEq(new double[10]), EasyMock.eq(4), EasyMock.eq(5));
        mockInput.skip(5 * ValueType.DOUBLE_BYTE_COUNT);
        expect(mockInput.readTokenType()).andReturn(TokenType.Whitespace);
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput);
        assertSame(START_DOCUMENT, scanner.next());
        assertSame(VALUE_DOUBLE, scanner.next());
        assertEquals(10, scanner.getValueCount());
        assertEquals(0, scanner.getValueReadCount());
        // read the first 5 elements ...
        scanner.getValue(new double[10], 4, 5);
        assertEquals(5, scanner.getValueReadCount());
        // skip the last 5 elements...
        assertSame(EventType.SPACE, scanner.next());
        verify(mockInput);
    }

    /**
     * Asserts the correct sequence of events is read through an entire xml test document, which is
     * completely mocked up by {@link #recordMockTestFile()}.
     * <p>
     * This test uses a non namespace aware parser
     * </p>
     * 
     * @throws IOException
     */
    @Test
    public void testParseSampleDocument() throws IOException {
        recordMockTestFile();
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        replay(mockInput);

        scanner = new TestDefaultBxmlScanner(mockInput) {
            // @Override
            // protected void readTrailer() {
            // // do nothing
            // }
        };

        assertEquals(START_DOCUMENT, scanner.next());
        assertEquals(COMMENT, scanner.next());
        assertEquals("comment1", scanner.getStringValue());
        assertEquals(COMMENT, scanner.next());
        assertEquals("comment2", scanner.getStringValue());
        assertEquals(START_ELEMENT, scanner.next());
        QName elementName = scanner.getElementName();
        assertEquals("StyledLayerDescriptor", elementName.getLocalPart());

        // this is a non namespace aware parser, so xmlns and xmlns: attributes are reported as
        // normal attributes
        assertEquals(3, scanner.getAttributeCount());
        assertEquals("version", scanner.getAttributeName(0).getLocalPart());
        assertEquals("1.0.0", scanner.getAttributeValue(0));

        assertEquals("xmlns:sld", scanner.getAttributeName(1).getLocalPart());
        assertEquals("http://www.opengis.net/sld", scanner.getAttributeValue(1));

        assertEquals("xmlns", scanner.getAttributeName(2).getLocalPart());
        assertEquals("http://www.opengis.net/sld", scanner.getAttributeValue(2));

        assertEquals(START_ELEMENT, scanner.next());

        elementName = scanner.getElementName();
        assertEquals("sld:NamedLayer", elementName.getLocalPart());

        assertEquals(START_ELEMENT, scanner.next());
        elementName = scanner.getElementName();
        assertEquals("Name", elementName.getLocalPart());
        assertEquals(VALUE_STRING, scanner.next());
        String stringValue = scanner.getStringValue();
        assertEquals("ALEXANDRIA:OWS-1.2&gt; &#x20;�&#233;&eacute;x", stringValue);
        assertEquals(END_ELEMENT, scanner.next());
        elementName = scanner.getElementName();
        assertEquals("Name", elementName.getLocalPart());

        assertEquals(START_ELEMENT, scanner.next());
        elementName = scanner.getElementName();
        assertEquals("NamedStyle", elementName.getLocalPart());

        assertEquals(START_ELEMENT, scanner.next());
        elementName = scanner.getElementName();
        assertEquals("Title", elementName.getLocalPart());

        assertEquals(VALUE_STRING, scanner.next());
        stringValue = scanner.getStringValue();
        assertEquals(" my_style & hello <!--x--> there <!--y--> ", stringValue);

        assertEquals(END_ELEMENT, scanner.next());
        elementName = scanner.getElementName();
        assertEquals("Title", elementName.getLocalPart());

        assertEquals(START_ELEMENT, scanner.next());
        elementName = scanner.getElementName();
        assertEquals("Name", elementName.getLocalPart());

        assertEquals(VALUE_STRING, scanner.next());
        stringValue = scanner.getStringValue();
        assertEquals(" my_style ", stringValue);

        assertEquals(END_ELEMENT, scanner.next());
        elementName = scanner.getElementName();
        assertEquals("Name", elementName.getLocalPart());

        assertEquals(END_ELEMENT, scanner.next());
        elementName = scanner.getElementName();
        assertEquals("NamedStyle", elementName.getLocalPart());

        assertEquals(END_ELEMENT, scanner.next());
        elementName = scanner.getElementName();
        assertEquals("sld:NamedLayer", elementName.getLocalPart());

        assertEquals(START_ELEMENT, scanner.next());
        elementName = scanner.getElementName();
        assertEquals("EmptyElement", elementName.getLocalPart());
        assertEquals(2, scanner.getAttributeCount());
        assertEquals("xmlns", scanner.getAttributeName(0).getLocalPart());
        assertEquals("bob", scanner.getAttributeName(1).getLocalPart());
        assertEquals(END_ELEMENT, scanner.next());
        elementName = scanner.getElementName();
        assertEquals("EmptyElement", elementName.getLocalPart());

        assertEquals(END_ELEMENT, scanner.next());
        elementName = scanner.getElementName();
        assertEquals("StyledLayerDescriptor", elementName.getLocalPart());

        assertEquals(END_DOCUMENT, scanner.next());
        verify(mockInput);
    }

    /**
     * Asserts the element and attribute names from the {@link #recordMockTestFile() mocked up test
     * file} are correctly parsed with a namespace aware {@link BxmlStreamReader}
     * 
     * @throws IOException
     */
    @Test
    public void testParseSampleDocumentWithNamespaces() throws IOException {
        recordMockTestFile();
        // every token read produces a getPosition call, ignore them
        expect(mockInput.getPosition()).andReturn(100L).anyTimes();
        replay(mockInput);

        final NamesResolver namespaceResolver = new NamespaceAwareNameResolver();
        final BxmlStreamReader scanner = new DefaultBxmlStreamReader(mockInput, namespaceResolver);

        assertEquals(START_DOCUMENT, scanner.next());
        assertEquals(COMMENT, scanner.next());
        assertEquals("comment1", scanner.getStringValue());
        assertEquals(COMMENT, scanner.next());
        assertEquals("comment2", scanner.getStringValue());
        assertEquals(START_ELEMENT, scanner.next());
        QName expectedName;
        QName actualName;

        expectedName = new QName("http://www.opengis.net/sld", "StyledLayerDescriptor");
        actualName = scanner.getElementName();
        assertEquals(expectedName, actualName);

        // this is a namespace aware parser, so xmlns and xmlns: attributes are not reported as
        // attributes
        assertEquals(1, scanner.getAttributeCount());

        expectedName = new QName("http://www.opengis.net/sld", "version");
        actualName = scanner.getAttributeName(0);
        assertEquals(expectedName, actualName);
        assertEquals("1.0.0", scanner.getAttributeValue(0));

        assertEquals(START_ELEMENT, scanner.next());

        expectedName = new QName("http://www.opengis.net/sld", "NamedLayer");
        actualName = scanner.getElementName();
        assertEquals(expectedName, actualName);

        assertEquals(START_ELEMENT, scanner.next());

        expectedName = new QName("http://www.opengis.net/sld", "Name");
        actualName = scanner.getElementName();
        assertEquals(expectedName, actualName);

        assertEquals(VALUE_STRING, scanner.next());
        String stringValue = scanner.getStringValue();
        assertEquals("ALEXANDRIA:OWS-1.2&gt; &#x20;�&#233;&eacute;x", stringValue);

        assertEquals(END_ELEMENT, scanner.next());
        expectedName = new QName("http://www.opengis.net/sld", "Name");
        actualName = scanner.getElementName();
        assertEquals(expectedName, actualName);

        assertEquals(START_ELEMENT, scanner.next());
        expectedName = new QName("http://www.opengis.net/sld", "NamedStyle");
        actualName = scanner.getElementName();
        assertEquals(expectedName, actualName);

        assertEquals(START_ELEMENT, scanner.next());

        expectedName = new QName("http://www.opengis.net/sld", "Title");
        actualName = scanner.getElementName();
        assertEquals(expectedName, actualName);

        assertEquals(VALUE_STRING, scanner.next());
        stringValue = scanner.getStringValue();
        assertEquals(" my_style & hello <!--x--> there <!--y--> ", stringValue);

        assertEquals(END_ELEMENT, scanner.next());
        expectedName = new QName("http://www.opengis.net/sld", "Title");
        actualName = scanner.getElementName();
        assertEquals(expectedName, actualName);

        assertEquals(START_ELEMENT, scanner.next());
        expectedName = new QName("http://www.opengis.net/sld", "Name");
        actualName = scanner.getElementName();
        assertEquals(expectedName, actualName);

        assertEquals(VALUE_STRING, scanner.next());
        stringValue = scanner.getStringValue();
        assertEquals(" my_style ", stringValue);

        assertEquals(END_ELEMENT, scanner.next());
        expectedName = new QName("http://www.opengis.net/sld", "Name");
        actualName = scanner.getElementName();
        assertEquals(expectedName, actualName);

        assertEquals(END_ELEMENT, scanner.next());
        expectedName = new QName("http://www.opengis.net/sld", "NamedStyle");
        actualName = scanner.getElementName();
        assertEquals(expectedName, actualName);

        assertEquals(END_ELEMENT, scanner.next());
        expectedName = new QName("http://www.opengis.net/sld", "NamedLayer");
        actualName = scanner.getElementName();
        assertEquals(expectedName, actualName);

        assertEquals(START_ELEMENT, scanner.next());
        expectedName = new QName("http://www.example.com", "EmptyElement");
        actualName = scanner.getElementName();
        assertEquals(expectedName, actualName);

        assertEquals(1, scanner.getAttributeCount());
        expectedName = new QName("http://www.example.com", "bob");
        actualName = scanner.getAttributeName(0);
        assertEquals(expectedName, actualName);

        assertEquals(END_ELEMENT, scanner.next());
        expectedName = new QName("http://www.example.com", "EmptyElement");
        actualName = scanner.getElementName();
        assertEquals(expectedName, actualName);

        assertEquals(END_ELEMENT, scanner.next());
        expectedName = new QName("http://www.opengis.net/sld", "StyledLayerDescriptor");
        actualName = scanner.getElementName();
        assertEquals(expectedName, actualName);

        assertEquals(END_DOCUMENT, scanner.next());
        verify(mockInput);
    }

    /**
     * Records the full reading of the following test file:
     * 
     * <pre>
     * &lt;code&gt;
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;ISO-8859-1&quot; ?&gt;
     * &lt;!-- comment1 --&gt;
     * &lt;!-- comment2 --&gt;
     * &lt;StyledLayerDescriptor xmlns=&quot;http://www.opengis.net/sld&quot;
     *   xmlns:sld=&quot;http://www.opengis.net/sld&quot; version='1.0.0'&gt;
     *   &lt;sld:NamedLayer&gt;
     *     &lt;Name&gt;ALEXANDRIA:OWS-1.2&amp;gt; &amp;#x20;�&amp;#233;&amp;eacute;x&lt;/Name&gt;
     *     &lt;NamedStyle&gt;
     *       &lt;Title&gt; my_style &amp; hello &lt;!--x--&gt; there &lt;!--y--&gt; &lt;/Title&gt;
     *       &lt;Name&gt; my_style &lt;/Name&gt;
     *     &lt;/NamedStyle&gt;
     *   &lt;/sld:NamedLayer&gt;
     *   &lt;EmptyElement bob='jim &amp; me '/&gt;
     * &lt;/StyledLayerDescriptor&gt;
     * &lt;/code&gt;
     * </pre>
     * 
     * as the following sequence of tokens:
     * 
     * <pre>
     * &lt;code&gt;
     * XmlDeclaration
     * Comment/StartOfLine/&quot;comment1&quot;
     * Comment/StartOfLine/&quot;comment2&quot;
     * StringTable/1/&quot;StyledLayerDescriptor&quot;
     * ContentAttrElement/0
     *      StringTable/3/&quot;xmlns&quot;,&quot;xmlns:sld&quot;,&quot;version&quot;
     *      AttributeStart/1/CharContent/&quot;http://www.opengis.net/sld&quot;
     *      AttributeStart/2/CharContent/&quot;http://www.opengis.net/sld&quot;
     *      AttributeStart/3/CharContent/&quot;1.0.0&quot;
     *      AttributeListEnd
     *      StringTable/1/&quot;NamedLayer&quot;
     *      ContentElement/4
     *          StringTable/1/&quot;Name&quot;
     *          ContentElement/5
     *              CharContent/&quot;ALEXANDRIA:OWS-1.2&gt;  ��&amp;eacutex&quot;
     *          ElementEnd
     *          StringTable/1/&quot;NamedStyle&quot;
     *          ContentElement/6
     *              StringTable/1/&quot;Title&quot;
     *              ContentElement/7
     *                  CharContent/&quot; my_style &amp; hello &lt;!--x--&gt; there &lt;!--y--&gt;&quot;
     *              ElementEnd
     *              Contentelement/5
     *                  CharContent/&quot; my_style &quot;
     *              ElementEnd
     *          ElementEnd
     *      ElementEnd
     *      StringTable/2/&quot;EmptyElement&quot;,&quot;bob&quot;
     *      EmptyAttrElement/8
     *          AttributeStart/9
     *              CharContent/&quot;jim &amp; me &quot;
     *          AttributeListEnd
     *      ElementEnd
     * ElementEnd
     * &lt;/code&gt;
     * </pre>
     * 
     * @throws IOException
     */
    private void recordMockTestFile() throws IOException {
        mockInput = createMock(BxmlInputStream.class);
        // record expected behavior (constructor calls getHeader())
        expect(mockInput.getHeader()).andReturn(Header.DEFAULT);

        recordMockXmlDeclaration("1.0", true, false);

        mockComment(CommentPositionHint.START_OF_LINE, "comment1");
        mockComment(CommentPositionHint.START_OF_LINE, "comment2");

        mockStringTable(new String[] { "StyledLayerDescriptor" });
        mockContentAttrElem(0);

        final int xmlnsAttIndex = 1;
        mockStringTable(new String[] { "xmlns", "xmlns:sld", "version" });
        mockAttribute(xmlnsAttIndex, "http://www.opengis.net/sld");
        mockAttribute(2, "http://www.opengis.net/sld");
        mockAttribute(3, "1.0.0");
        mockAttributeListEnd();

        mockStringTable(new String[] { "sld:NamedLayer" });
        mockContentElem(4);

        mockStringTable(new String[] { "Name" });
        mockContentElem(5);
        mockCharContent("ALEXANDRIA:OWS-1.2&gt; &#x20;�&#233;&eacute;x");
        mockElementEnd();// Name

        mockStringTable(new String[] { "NamedStyle" });
        mockContentElem(6);

        mockStringTable(new String[] { "Title" });
        mockContentElem(7);
        mockCharContent(" my_style & hello <!--x--> there <!--y--> ");
        mockElementEnd();// Title

        mockContentElem(5); // <Name>
        mockCharContent(" my_style ");
        mockElementEnd();// </Name>

        mockElementEnd();// NamedStyle

        mockElementEnd();// NamedLayer

        mockStringTable(new String[] { "EmptyElement", "bob" });
        mockEmptyAttrElement(8);
        mockAttribute(9, "jim & me ");
        mockAttribute(xmlnsAttIndex, "http://www.example.com");
        mockAttributeListEnd();

        mockElementEnd(); // StyledLayerDescriptor

        expect(mockInput.readTokenType()).andReturn(Trailer);
    }

    /**
     * Records the tokens needed to acquire an xml attribute named {@code attrName} with value
     * {@code attrValue} into a mocked up instance of {@link BxmlInputStream}.
     * 
     * @param input
     *            a mocked up {@link BxmlInputStream} to record the reading of the xml attribute
     *            into
     * @param attrName
     *            the name of the attribute to record
     * @param attrValue
     *            the value of the attribute to record, use a value != null for the attribute value
     *            to be actually recorded.
     * @throws IOException
     *             shouldn't happen
     */
    private void recordMockAttribute(BxmlInputStream input, String attrName, String attrValue,
            long attrNameStringTableIndex) throws IOException {
        // record the AttributeStart token
        expect(input.readTokenType()).andReturn(AttributeStart);
        // AttributeStartToken is followed by a count for its index in the string table
        expect(input.readCount()).andReturn(attrNameStringTableIndex);

        // now follows the attribute value...
        if (attrValue != null) {
            expect(input.readTokenType()).andReturn(CharContent);
            expect(input.readByte()).andReturn(ValueType.StringCode.getCode());
            expect(input.readString()).andReturn(attrValue);
        }
    }

    private void mockEmptyAttrElement(long elemNameStringTableIndex) throws IOException {
        expect(mockInput.readTokenType()).andReturn(EmptyAttrElement);
        expect(mockInput.readCount()).andReturn(elemNameStringTableIndex);
    }

    private void mockCharContent(final String content) throws IOException {
        // attribute content token type
        expect(mockInput.readTokenType()).andReturn(CharContent);
        // attribute content value type
        expect(mockInput.readByte()).andReturn(ValueType.StringCode.getCode());
        // attribute content
        expect(mockInput.readString()).andReturn(content);
    }

    private void mockContentElem(long elemNameStringTableIndex) throws IOException {
        expect(mockInput.readTokenType()).andReturn(ContentElement);
        expect(mockInput.readCount()).andReturn(elemNameStringTableIndex);
    }

    private void mockElementEnd() throws IOException {
        expect(mockInput.readTokenType()).andReturn(ElementEnd);
    }

    private void mockAttributeListEnd() throws IOException {
        expect(mockInput.readTokenType()).andReturn(AttributeListEnd);
    }

    private void mockAttribute(final long stringTableIndex, final String content)
            throws IOException {
        // AttributeStart Token
        expect(mockInput.readTokenType()).andReturn(AttributeStart);
        // attribute name string table index
        expect(mockInput.readCount()).andReturn(stringTableIndex);

        mockCharContent(content);
    }

    private void mockComment(final CommentPositionHint posHint, final String commentContent)
            throws IOException {
        expect(mockInput.readTokenType()).andReturn(Comment);
        expect(mockInput.readByte()).andReturn(posHint.getCode());
        expect(mockInput.readString()).andReturn(commentContent);
    }

    private void mockStringTable(String[] strings) throws IOException {
        expect(mockInput.readTokenType()).andReturn(StringTable);
        expect(mockInput.readCount()).andReturn((long) strings.length);
        for (String toAdd : strings) {
            expect(mockInput.readString()).andReturn(toAdd);
        }
    }

    private void mockContentAttrElem(long elemNameStringTableIndex) throws IOException {
        expect(mockInput.readTokenType()).andReturn(ContentAttrElement);
        expect(mockInput.readCount()).andReturn(elemNameStringTableIndex);
    }

}
