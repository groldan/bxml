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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.gvsig.bxml.stream.io.TokenType.XmlDeclaration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import org.gvsig.bxml.stream.BxmlFactoryFinder;
import org.gvsig.bxml.stream.BxmlOutputFactory;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.gvsig.bxml.stream.EncodingOptions;
import org.gvsig.bxml.stream.EventType;
import org.gvsig.bxml.stream.io.BxmlInputStream;
import org.gvsig.bxml.stream.io.BxmlOutputStream;
import org.gvsig.bxml.stream.io.DefaultStreamFactory;
import org.gvsig.bxml.stream.io.Header;
import org.gvsig.bxml.stream.io.TokenType;
import org.gvsig.bxml.stream.io.ValueType;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test suite for {@link DefaultBxmlStreamWriter}
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class DefaultBxmlStreamWriterTest {

    private EncodingOptions encodingOptions;

    private BxmlOutputStream mockOutputStream;

    private DefaultBxmlStreamWriter writer;

    @Before
    public void setUp() throws Exception {
        createWriter();
    }

    @After
    public void tearDown() throws Exception {
        encodingOptions = null;
        mockOutputStream = null;
        writer = null;
    }

    private void createWriter() throws IOException {
        encodingOptions = new EncodingOptions();
        mockOutputStream = createMock(BxmlOutputStream.class);
        // record constructor calls
        // mockOutputStream.setEndianess(eq(encodingOptions.getByteOrder()));
        // mockOutputStream.setCharactersEncoding(eq(encodingOptions.getCharactersEncoding()));

        // no writing is done at the writer's constructor, so its safe to create it here
        // although the expected mockWriter calls hasn't been declared
        writer = new DefaultBxmlStreamWriter(encodingOptions, mockOutputStream);
    }

    @Test
    public void testWriteStartDocumentDefaultOptions() throws IOException {
        testWriteStartDocument(encodingOptions);
    }

    @Test
    public void testGetLastEvent() throws IOException {
        BxmlOutputStream outputStream = new DefaultStreamFactory()
                .createOutputStream(new ByteArrayOutputStream());
        BxmlStreamWriter writer = new DefaultBxmlStreamWriter(new EncodingOptions(), outputStream);
        assertSame(EventType.NONE, writer.getLastEvent());

        writer.writeStartDocument();
        assertEquals(EventType.START_DOCUMENT, writer.getLastEvent());

        writer.writeComment("comment");
        assertEquals(EventType.COMMENT, writer.getLastEvent());

        writer.writeStartElement("", "localName");
        assertEquals(EventType.START_ELEMENT, writer.getLastEvent());

        writer.writeStartAttribute("", "attLocalName");
        assertEquals(EventType.ATTRIBUTE, writer.getLastEvent());

        writer.writeValue("stringVal");
        assertEquals(EventType.VALUE_STRING, writer.getLastEvent());

        writer.writeEndAttributes();
        assertEquals(EventType.ATTRIBUTES_END, writer.getLastEvent());

        writer.writeValue(true);
        assertEquals(EventType.VALUE_BOOL, writer.getLastEvent());

        writer.writeValue(1D);
        assertEquals(EventType.VALUE_DOUBLE, writer.getLastEvent());

        writer.writeValue(1F);
        assertEquals(EventType.VALUE_FLOAT, writer.getLastEvent());

        writer.writeValue(1);
        assertEquals(EventType.VALUE_INT, writer.getLastEvent());

        writer.writeValue(1L);
        assertEquals(EventType.VALUE_LONG, writer.getLastEvent());

        writer.writeValue("string");
        assertEquals(EventType.VALUE_STRING, writer.getLastEvent());

        writer.writeValue(new boolean[1], 0, 1);
        assertEquals(EventType.VALUE_BOOL, writer.getLastEvent());

        writer.writeValue(new char[1], 0, 1);
        assertEquals(EventType.VALUE_STRING, writer.getLastEvent());

        writer.writeValue(new double[1], 0, 1);
        assertEquals(EventType.VALUE_DOUBLE, writer.getLastEvent());

        writer.writeValue(new float[1], 0, 1);
        assertEquals(EventType.VALUE_FLOAT, writer.getLastEvent());

        writer.writeValue(new int[1], 0, 1);
        assertEquals(EventType.VALUE_INT, writer.getLastEvent());

        writer.writeValue(new long[1], 0, 1);
        assertEquals(EventType.VALUE_LONG, writer.getLastEvent());

        writer.writeEndElement();
        assertEquals(EventType.END_ELEMENT, writer.getLastEvent());

        writer.writeEndDocument();
        assertEquals(EventType.END_DOCUMENT, writer.getLastEvent());
    }

    @Test
    public void testWriteStartDocumentCustomOptions() throws IOException {
        // use inverse settings than default options
        final ByteOrder byteOrder = encodingOptions.getByteOrder() == ByteOrder.BIG_ENDIAN ? ByteOrder.LITTLE_ENDIAN
                : ByteOrder.BIG_ENDIAN;
        final Charset charsEncoding = Charset.forName("US-ASCII");
        final boolean useCompression = !encodingOptions.isUseCompression();
        final boolean useStrictXmlStrings = !encodingOptions.isUseStrictXmlStrings();
        final boolean isValidated = !encodingOptions.isValidated();
        final String xmlVersion = "1.1";
        final Boolean standalone = Boolean.FALSE;
        encodingOptions = new EncodingOptions();
        encodingOptions.setByteOrder(byteOrder);
        encodingOptions.setCharactersEncoding(charsEncoding);
        encodingOptions.setUseCompression(useCompression);
        encodingOptions.setUseStrictXmlStrings(useStrictXmlStrings);
        encodingOptions.setValidated(isValidated);
        encodingOptions.setXmlVersion(xmlVersion);
        encodingOptions.setStandalone(standalone);

        testWriteStartDocument(encodingOptions);
    }

    private void testWriteStartDocument(EncodingOptions encodingOptions) throws IOException {
        // record expected writer calls
        Header header = DefaultBxmlStreamWriter.toHeader(encodingOptions);
        mockOutputStream.writeHeader(eq(header));
        mockOutputStream.writeTokenType(eq(XmlDeclaration));
        mockOutputStream.writeString(eq(encodingOptions.getXmlVersion()));
        final boolean standalone = encodingOptions.isStandalone() == null ? true : encodingOptions
                .isStandalone().booleanValue();
        final boolean standaloneIsSet = encodingOptions.isStandalone() != null;
        mockOutputStream.writeBoolean(eq(standalone));
        mockOutputStream.writeBoolean(eq(standaloneIsSet));
        replay(mockOutputStream);

        writer = new DefaultBxmlStreamWriter(encodingOptions, mockOutputStream);
        writer.writeStartDocument();
        verify(mockOutputStream);
    }

    @Test
    public void testClose() throws IOException {
        expect(mockOutputStream.isOpen()).andReturn(true);
        expect(mockOutputStream.isAutoFlushing()).andReturn(true);
        mockOutputStream.flush();

        replay(mockOutputStream);
        writer.close();
        verify(mockOutputStream);
    }

    @Test
    public void testCloseNotAutoFlush() throws IOException {
        expect(mockOutputStream.isOpen()).andReturn(true);
        expect(mockOutputStream.isAutoFlushing()).andReturn(false);
        mockOutputStream.setAutoFlushing(eq(true));
        mockOutputStream.flush();

        replay(mockOutputStream);
        writer.close();
        verify(mockOutputStream);
    }

    @Test
    public void testCloseAlreadyClosed() throws IOException {
        expect(mockOutputStream.isOpen()).andReturn(false);
        replay(mockOutputStream);
        writer.close();
        verify(mockOutputStream);
    }

    @Test
    @Ignore
    public void testWriteEndDocument() throws IOException {
        expect(mockOutputStream.getPosition()).andReturn(1000L);
        mockOutputStream.writeTokenType(TokenType.Trailer);
        final byte[] id = { 0x01, 'T', 'R', 0x00 };
        mockOutputStream.writeByte(id, 0, 4);

        final boolean stringTableIndexIsUsed = false;
        final boolean indexTableIsUsed = false;
        mockOutputStream.writeBoolean(stringTableIndexIsUsed);
        mockOutputStream.writeBoolean(indexTableIsUsed);

        expect(mockOutputStream.getPosition()).andReturn(10050L);
        final int trailerLength = 50;
        final int trailerLengthIncludingLengthMark = ValueType.INTEGER_BYTE_COUNT + trailerLength;
        mockOutputStream.writeInt(trailerLengthIncludingLengthMark);
        mockOutputStream.flush();

        replay(mockOutputStream);
        writer.writeEndDocument();
        verify(mockOutputStream);
    }

    @Test
    public void testFlush() throws IOException {
        mockOutputStream.flush();
        replay(mockOutputStream);
        writer.flush();
        verify(mockOutputStream);
    }

    @Test
    @Ignore
    public void testWriteStartAttribute() throws IOException {
        // record calls to mock stream...
        {
            mockOutputStream.writeTokenType(TokenType.StringTable);
            mockOutputStream.writeCount(1L);// string table element index
            mockOutputStream.writeString("attName");// resolved attribute name
        }

        String uri = "http://junit.org";
        // no startElement called, bound prefix to the root context
        // writer.setPrefix("junit", uri);

        replay(mockOutputStream);

        writer.writeStartAttribute(uri, "attName");

        writer.getPrefix(uri);

        verify(mockOutputStream);
    }

    @Test
    @Ignore
    public void testWriteStartElement() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testWriteEndElement() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testWriteValueString() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testWriteValueInt() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testWriteValueLong() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testWriteValueFloat() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testWriteValueDouble() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testWriteValueBoolean() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testWriteValueStringArray() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testWriteValueIntArray() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testWriteValueLongArray() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testWriteValueFloatArray() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testWriteValueDoubleArray() {
        fail("Not yet implemented");
    }

    /**
     * <pre>
     * &lt;code&gt;
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;ISO-8859-1&quot; ?&gt;
     *     &lt;!-- comment1 --&gt;
     *     &lt;!-- comment2 --&gt;
     *     &lt;StyledLayerDescriptor xmlns=&quot;http://www.opengis.net/sld&quot;
     *     xmlns:sld=&quot;http://www.opengis.net/sld&quot; version='1.0.0'&gt;
     *     &lt;sld:NamedLayer&gt;
     *     &lt;Name&gt;ALEXANDRIA:OWS-1.2&gt; ��&amp;eacutex&lt;/Name&gt;
     *     &lt;NamedStyle&gt;
     *       &lt;Title&gt; my_style &amp; hello &lt;!--x--&gt; there &lt;!--y--&gt; &lt;/Title&gt;
     *       &lt;Name&gt; my_style &lt;/Name&gt;
     *     &lt;/NamedStyle&gt;
     *     &lt;/sld:NamedLayer&gt;
     *     &lt;EmptyElement bob='jim &amp; me '/&gt;
     *     &lt;/StyledLayerDescriptor&gt;
     * &lt;/code&gt;
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testWriteTestDocumentNoNameSpaces() throws Exception {
        final File file = new File("target/testWriteTestDocumentNoNameSpaces.bxml");
        OutputStream out = new FileOutputStream(file);

        DefaultBxmlOutputFactory defaultBxmlFactory = new DefaultBxmlOutputFactory();
        encodingOptions.setCharactersEncoding(Charset.forName("UTF-8"));
        defaultBxmlFactory.setEncodingOptions(encodingOptions);

        BxmlStreamWriter serializer = defaultBxmlFactory.createSerializer(out);

        final String sldNamespace = "http://www.opengis.net/sld";
        serializer.writeStartDocument();
        serializer.writeComment(" comment1 ");
        serializer.writeComment(" comment2 ");
        serializer.writeStartElement(sldNamespace, "StyledLayerDescriptor");

        serializer.writeStartAttribute("", "xmlns");
        serializer.writeValue(sldNamespace);
        serializer.writeStartAttribute("", "xmlns:sld");
        serializer.writeValue(sldNamespace);
        serializer.writeStartAttribute("", "version");
        serializer.writeValue("1.0.0");
        serializer.writeEndAttributes();

        serializer.writeStartElement("", "NamedLayer");
        serializer.writeStartElement("", "Name");
        serializer.writeValue("ALEXANDRIA:OWS-1.2");
        serializer.writeValue("&gt;");
        serializer.writeValue("&#x20;");
        serializer.writeValue("�");
        serializer.writeValue("&#233;");
        serializer.writeValue("&eacute;");
        serializer.writeValue("x");
        serializer.writeEndElement();// Name

        serializer.writeStartElement("", "NamedStyle");
        serializer.writeStartElement("", "Title");
        serializer.writeValue(" my_style ");
        serializer.writeValue("&amp;");
        serializer.writeValue(" hello ");
        serializer.writeComment("x"); // /comment

        serializer.writeValue(" there ");
        serializer.writeComment("y");
        serializer.writeEndElement();// Title

        serializer.writeStartElement("", "Name");
        serializer.writeValue("my_style");
        serializer.writeEndElement();// Name

        serializer.writeEndElement();// NamedStyle

        serializer.writeEndElement();// NamedLayer

        serializer.writeStartElement("", "EmptyElement");
        serializer.writeStartAttribute("", "bob");
        serializer.writeValue("jim &amp; me");
        serializer.writeEndAttributes();
        serializer.writeEndElement();// EmptyElement
        serializer.writeEndElement();// StyledLayerDescriptor

        serializer.writeEndDocument();
        serializer.flush();
        serializer.close();

        // @TODO: can't use an integration test from inside a unit test!
        // ReadableByteChannel channel = new FileInputStream(file).getChannel();
        // BxmlScannerIntegrationTest bxmlScannerIntegrationTest = new BxmlScannerIntegrationTest();
        // bxmlScannerIntegrationTest.setUp();
        // bxmlScannerIntegrationTest.testTraverseBxml(channel);
    }

    /**
     * Write the test document in a namespace aware fashion
     * 
     * <pre>
     * &lt;code&gt;
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;ISO-8859-1&quot; ?&gt;
     *     &lt;!-- comment1 --&gt;
     *     &lt;!-- comment2 --&gt;
     *     &lt;StyledLayerDescriptor xmlns=&quot;http://www.opengis.net/sld&quot;
     *     xmlns:sld=&quot;http://www.opengis.net/sld&quot; version='1.0.0'&gt;
     *     &lt;sld:NamedLayer&gt;
     *     &lt;Name&gt;ALEXANDRIA:OWS-1.2&gt; ��&amp;eacutex&lt;/Name&gt;
     *     &lt;NamedStyle&gt;
     *       &lt;Title&gt; my_style &amp; hello &lt;!--x--&gt; there &lt;!--y--&gt; &lt;/Title&gt;
     *       &lt;Name&gt; my_style &lt;/Name&gt;
     *     &lt;/NamedStyle&gt;
     *     &lt;/sld:NamedLayer&gt;
     *     &lt;EmptyElement bob='jim &amp; me '/&gt;
     *     &lt;/StyledLayerDescriptor&gt;
     * &lt;/code&gt;
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testWriteTestDocumentWithNameSpaces() throws Exception {
        final BxmlStreamWriter serializer;
        final File file = new File("target/testWriteTestDocumentNameSpaces.bxml");
        final OutputStream out = new FileOutputStream(file);
        {
            encodingOptions.setCharactersEncoding(Charset.forName("UTF-8"));
            final BxmlOutputFactory defaultBxmlFactory = BxmlFactoryFinder.newOutputFactory();
            defaultBxmlFactory.setEncodingOptions(encodingOptions);
            serializer = defaultBxmlFactory.createSerializer(out);
        }

        final String sldNamespace = "http://www.opengis.net/sld";
        final String exampleNamespace = "http://www.example.com/test";

        serializer.writeStartDocument();

        serializer.setSchemaLocation(sldNamespace,
                "http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd");
        serializer.setSchemaLocation(exampleNamespace, "http://www.example.com/test/example.xsd");

        serializer.setDefaultNamespace(sldNamespace);

        serializer.writeNamespace("sld", sldNamespace);

        serializer.writeComment(" comment1 ");
        serializer.writeComment(" comment2 ");
        serializer.writeStartElement(sldNamespace, "StyledLayerDescriptor");

        serializer.writeStartAttribute(sldNamespace, "version");
        serializer.writeValue("1.0.0");
        serializer.writeEndAttributes();

        serializer.writeStartElement(sldNamespace, "NamedLayer");
        serializer.writeStartElement(sldNamespace, "Name");
        serializer.writeValue("ALEXANDRIA:OWS-1.2");
        serializer.writeValue("&gt;");
        serializer.writeValue("&#x20;");
        serializer.writeValue("�");
        serializer.writeValue("&#233;");
        serializer.writeValue("&eacute;");
        serializer.writeValue("x");
        serializer.writeEndElement();// Name

        serializer.writeStartElement(sldNamespace, "NamedStyle");
        serializer.writeStartElement(sldNamespace, "Title");
        serializer.writeValue(" my_style ");
        serializer.writeValue("&amp;");
        serializer.writeValue(" hello ");
        serializer.writeComment("x"); // /comment

        serializer.writeValue(" there ");
        serializer.writeComment("y");
        serializer.writeEndElement();// Title

        serializer.writeStartElement(sldNamespace, "Name");
        serializer.writeValue("my_style");
        serializer.writeEndElement();// Name

        serializer.writeEndElement();// NamedStyle

        serializer.writeEndElement();// NamedLayer

        serializer.writeStartElement(exampleNamespace, "EmptyElement");
        serializer.writeStartAttribute("", "bob");
        serializer.writeValue("jim &amp; me");
        serializer.writeEndAttributes();
        serializer.writeEndElement();// EmptyElement

        serializer.writeStartElement(exampleNamespace, "EmptyElement2");
        serializer.writeEndElement();// EmptyElement2

        serializer.writeStartElement(exampleNamespace, "EmptyElement3");
        serializer.writeStartElement("", "EmptyElement");
        serializer.writeEndElement();// EmptyElement
        serializer.writeEndElement();// EmptyElement3

        serializer.writeEndElement();// StyledLayerDescriptor

        serializer.writeEndDocument();
        serializer.flush();
        serializer.close();
        out.close();

        BxmlStreamReader parser = BxmlFactoryFinder.newInputFactory().createScanner(file);
        while (parser.hasNext()) {
            parser.next();
        }
    }

    @Test
    public void testWriteAutoReferenceableAttributeValues() throws Exception {
        final File file = new File("target/testWriteAutoReferenceableAttributeValues.bxml");
        OutputStream out = new FileOutputStream(file);

        DefaultBxmlOutputFactory defaultBxmlFactory = new DefaultBxmlOutputFactory();
        encodingOptions.setCharactersEncoding(Charset.forName("ISO-8859-1"));
        defaultBxmlFactory.setEncodingOptions(encodingOptions);

        BxmlStreamWriter serializer = defaultBxmlFactory.createSerializer(out);
        serializer.setWriteAttributeValueAsStringTable("gml:srsName");

        final String gmlNs = "http://www.opengis.net/gml";
        serializer.writeNamespace("gml", gmlNs);

        serializer.writeStartDocument();
        serializer.writeStartElement("", "Test");

        serializer.writeStartElement(gmlNs, "Envelope");

        serializer.writeStartAttribute(gmlNs, "srsName");
        serializer.writeValue("http://www.opengis.net/gml/srs/epsg.xml#4326");

        serializer.writeStartAttribute("", "srsName");
        serializer.writeValue("nonReferencedValue");
        serializer.writeEndAttributes();

        serializer.writeEndElement();// Envelope

        serializer.writeStartElement(gmlNs, "Envelope");

        serializer.writeStartAttribute(gmlNs, "srsName");
        serializer.writeValue("http://www.opengis.net/gml/srs/epsg.xml#4326");

        serializer.writeStartAttribute("", "srsName");
        serializer.writeValue("nonReferencedValue");
        serializer.writeEndAttributes();

        serializer.writeEndElement();// Envelope

        serializer.writeEndElement();// Test

        serializer.writeEndDocument();
        serializer.flush();
        serializer.close();
        //TODO: check the gml:srsName value was written only once as a StringTable reference
    }
}
