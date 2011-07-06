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
package org.gvsig.bxml.stream.impl.test.integration;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.geotools.test.TestData;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.gvsig.bxml.stream.EventType;
import org.gvsig.bxml.stream.impl.DefaultBxmlInputFactory;
import org.gvsig.bxml.stream.impl.DefaultBxmlOutputFactory;
import org.gvsig.bxml.stream.io.BxmlInputStream;
import org.gvsig.bxml.stream.io.DefaultStreamFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BxmlStreamReaderIntegrationTest {

    private DefaultBxmlInputFactory bxmlFactory;

    private DefaultStreamFactory streamFactory;

    @Before
    public void setUp() throws Exception {
        bxmlFactory = new DefaultBxmlInputFactory();
        streamFactory = new DefaultStreamFactory();
    }

    @After
    public void tearDown() throws Exception {
        bxmlFactory = null;
        streamFactory = null;
    }

    @Test
    public void testRead() throws Exception {
        final String fileName = "cwxml-test.bxml";
        final File file = TestData.file(this, fileName);
        final BxmlStreamReader reader = createFileReader(file, true);

        assertEquals(EventType.NONE, reader.getEventType());
        reader.next();
        assertEquals(EventType.START_DOCUMENT, reader.getEventType());
        assertEquals(EventType.COMMENT, reader.next());
        assertEquals("comment1", reader.getStringValue());
        assertEquals(EventType.COMMENT, reader.next());
        assertEquals("comment2", reader.getStringValue());
        assertEquals(EventType.START_ELEMENT, reader.next());

        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("StyledLayerDescriptor", reader.getElementName().getLocalPart());

        assertEquals("http://www.opengis.net/sld", reader.getNamespaceURI("sld"));
        assertEquals("sld", reader.getPrefix("http://www.opengis.net/sld"));

        assertEquals(1, reader.getAttributeCount());
        assertEquals("1.0.0", reader.getAttributeValue(0));
        // assertEquals("1.0.0", reader.getAttributeValue("", "version"));
        assertEquals("1.0.0", reader.getAttributeValue(null, "version"));

        assertEquals(EventType.START_ELEMENT, reader.nextTag());
        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("NamedLayer", reader.getElementName().getLocalPart());
        assertEquals(0, reader.getAttributeCount());

        assertEquals(EventType.START_ELEMENT, reader.nextTag());
        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("Name", reader.getElementName().getLocalPart());

        assertEquals(EventType.VALUE_STRING, reader.next());
        assertEquals("ALEXANDRIA:OWS-1.2", reader.getStringValue());
        assertEquals(EventType.VALUE_STRING, reader.next());
        assertEquals(">", reader.getStringValue());
        assertEquals(EventType.VALUE_STRING, reader.next());
        assertEquals(" ", reader.getStringValue());
        assertEquals(EventType.VALUE_STRING, reader.next());
        assertEquals("é", reader.getStringValue());
        assertEquals(EventType.VALUE_STRING, reader.next());
        assertEquals("é", reader.getStringValue());
        assertEquals(EventType.VALUE_STRING, reader.next());
        assertEquals("é", reader.getStringValue());
        assertEquals(EventType.VALUE_STRING, reader.next());
        assertEquals("x", reader.getStringValue());

        assertEquals(EventType.END_ELEMENT, reader.next());

        assertEquals(EventType.START_ELEMENT, reader.nextTag());
        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("NamedStyle", reader.getElementName().getLocalPart());

        assertEquals(EventType.START_ELEMENT, reader.nextTag());
        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("Title", reader.getElementName().getLocalPart());

        assertEquals(EventType.VALUE_STRING, reader.next());
        assertEquals("my_style ", reader.getStringValue());
        assertEquals(EventType.VALUE_STRING, reader.next());
        assertEquals("&", reader.getStringValue());
        assertEquals(EventType.VALUE_STRING, reader.next());
        assertEquals(" hello", reader.getStringValue());

        assertEquals(EventType.COMMENT, reader.next());
        assertEquals("x", reader.getStringValue());
        assertEquals(EventType.VALUE_STRING, reader.next());
        assertEquals(" there", reader.getStringValue());
        assertEquals(EventType.COMMENT, reader.next());
        assertEquals("y", reader.getStringValue());
        assertEquals(EventType.END_ELEMENT, reader.next());

        assertEquals(EventType.START_ELEMENT, reader.nextTag());
        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("Name", reader.getElementName().getLocalPart());
        assertEquals(EventType.VALUE_STRING, reader.next());
        assertEquals("my_style", reader.getStringValue());
        assertEquals(EventType.END_ELEMENT, reader.nextTag());
        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("Name", reader.getElementName().getLocalPart());

        assertEquals(EventType.END_ELEMENT, reader.nextTag());
        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("NamedStyle", reader.getElementName().getLocalPart());

        assertEquals(EventType.END_ELEMENT, reader.nextTag());
        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("NamedLayer", reader.getElementName().getLocalPart());

        assertEquals(EventType.START_ELEMENT, reader.nextTag());
        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("EmptyElement", reader.getElementName().getLocalPart());
        assertEquals("jim & me ", reader.getAttributeValue(0));
        assertEquals("jim & me ", reader.getAttributeValue(null, "bob"));
        assertEquals(EventType.END_ELEMENT, reader.nextTag());
        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("EmptyElement", reader.getElementName().getLocalPart());

        assertEquals(EventType.END_ELEMENT, reader.nextTag());
        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("StyledLayerDescriptor", reader.getElementName().getLocalPart());

        assertEquals(EventType.END_DOCUMENT, reader.next());
    }

    /**
     * If the underlying channel supports random access and there's no IndexTableIndex in the
     * Trailer, random access should be supported
     * 
     * @throws Exception
     */
    @Test
    public void testSupportsRandomAccessNoStringTableIndex() throws Exception {
        final String fileName = "cwxml-test-data/test.bxml";
        final File file = TestData.file(this, fileName);
        final BxmlStreamReader reader = createFileReader(file, true);

        assertTrue("reader should support random access", reader.supportsRandomAccess());
    }

    /**
     * If the underlying channel supports random access and there *is* IndexTableIndex in the
     * Trailer, random access should be supported
     * 
     * @throws Exception
     */
    @Test
    public void testSuportsRandomAccessWithStringTableIndex() throws Exception {
        final String fileName = "target/testWithStringTableIndex.bxml";
        File file = createTestFile(fileName);

        final BxmlStreamReader reader = createFileReader(file, true);

        assertTrue("reader should support random access", reader.supportsRandomAccess());

    }

    @Test
    public void testRandomAccess() throws Exception {
        final String fileName = "target/testWithStringTableIndex.bxml";
        File file = createTestFile(fileName);

        BxmlStreamReader reader = createFileReader(file, true);

        reader.nextTag();
        QName rootName = reader.getElementName();
        long rootPosition = reader.getElementPosition();

        reader.nextTag();
        QName childName = reader.getElementName();
        long childPosition = reader.getElementPosition();

        reader = createFileReader(file, true);
        // reader.nextTag();

        EventType positionEvent;

        positionEvent = reader.setPosition(childPosition);
        assertSame(EventType.START_ELEMENT, positionEvent);
        assertEquals(childName, reader.getElementName());
        assertEquals(childPosition, reader.getElementPosition());

        positionEvent = reader.setPosition(rootPosition);
        assertSame(EventType.START_ELEMENT, positionEvent);
        assertEquals(rootName, reader.getElementName());
        assertEquals(rootPosition, reader.getElementPosition());
    }

    private File createTestFile(final String fileName) throws IOException, FileNotFoundException {
        File file = new File(fileName);
        file.createNewFile();

        WritableByteChannel outChannel = new FileOutputStream(file).getChannel();
        BxmlStreamWriter w = new DefaultBxmlOutputFactory().createSerializer(outChannel);
        w.writeStartDocument();
        String nsUri = "http://www.gvsig.org/bxml";
        w.writeNamespace("bxml", nsUri);

        w.writeStartElement(nsUri, "root");

        w.writeStartAttribute("", "noNsAtt");
        w.writeValue(true);

        w.writeStartAttribute(nsUri, "nsAtt");
        long reference = w.getStringTableReference("referencedValue");
        w.writeStringTableValue(reference);

        w.writeEndAttributes();

        w.writeStartElement(nsUri, "valueElement");
        w.writeValue(1000D);
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();

        outChannel.close();
        return file;
    }

    private BxmlStreamReader createFileReader(final File file, final boolean namespaceAware)
            throws IOException {
        final FileChannel channel = new FileInputStream(file).getChannel();
        final BxmlInputStream bxmlIn = streamFactory.createInputStream(channel);

        bxmlFactory.setNamespaceAware(namespaceAware);

        final BxmlStreamReader reader = bxmlFactory.createScanner(bxmlIn);
        return reader;
    }

    @Test
    @Ignore
    public void testDefaultBxmlScannerParseStat_id_1000_SAX() throws Exception {
        final String fileName = "stat_id_1000.xml";
        testParseSAX(TestData.file(this, fileName));
    }

    @Test
    public void testDefaultBxmlScannerParseStat_id_1000() throws IOException {
        final String fileName = "stat_id_1000.bxml";
        testTraverseBxml(TestData.openChannel(this, fileName));
    }

    @Test
    @Ignore
    public void testParseLarge_Polygons_SAX() throws Exception {
        final String fileName = "large_polygons.gml";
        testParseSAX(TestData.file(this, fileName));
    }

    @Test
    @Ignore
    public void testDefaultBxmlScannerParseLarge_Polygons() throws Exception {
        final String fileName = "large_polygons.gml.bxml";
        double ellapsed = testTraverseBxml(TestData.openChannel(this, fileName));
        System.out.println(fileName + ": " + ellapsed);
    }

    // @Test
    public void testParseLargeGML2_SAX() throws Exception {
        final String fileName = "/usr/local/data/gis/testdata/gshhs_land_noindent.gml2";
        File file = new File(fileName);
        testParseSAX(file);
    }

    // @Test
    public void testParseLargeGML2() throws Exception {
        final String fileName = "/usr/local/data/gis/testdata/gshhs_land_noindent.gml2.bxml";
        ReadableByteChannel channel = new FileInputStream(fileName).getChannel();
        testTraverseBxml(channel);
    }

    public void plainReadLargeGML2() throws IOException {
        plainRead(new File("/usr/local/data/gis/testdata/gshhs_land_noindent.gml2.bxml"));
    }

    public void plainReadLargePolygons() throws IOException {
        plainRead(TestData.file(this, "large_polygons.gml"));
    }

    private void plainRead(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        final byte[] buff = new byte[1024 * 1024];
        try {
            while (in.read(buff) > -1) {
                // do nothing
            }
        } finally {
            in.close();
        }
    }

    private long testParseSAX(final File file) throws Exception {
        SAXParserFactory saxFac = SAXParserFactory.newInstance();
        SAXParser saxParser = saxFac.newSAXParser();

        DefaultHandler handler;

        // use a sax handler that builds up strings from characters event as we do...
        handler = new DefaultHandler() {
            StringBuilder sb = new StringBuilder();

            @Override
            public void startElement(String uri, String localName, String qName,
                    Attributes attributes) throws SAXException {
                sb.setLength(0);
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                String s = sb.toString();
                assert s != null;
            }

            @Override
            public void characters(char ch[], int start, int length) throws SAXException {
                sb.append(ch, start, length);
            }
        };

        long start = System.currentTimeMillis();
        saxParser.parse(file, handler);
        final long end = System.currentTimeMillis();
        final long ellapsed = (end - start);
        return ellapsed;
    }

    public double testTraverseBxml(final ReadableByteChannel channel) throws IOException {
        if (channel == null) {
            throw new NullPointerException("null channel provided, check the test data");
        }
        final BxmlInputStream bxmlIn = streamFactory.createInputStream(channel);
        final BxmlStreamReader scanner = bxmlFactory.createScanner(bxmlIn);

        double start = System.nanoTime();

        try {
            traverse(scanner);
        } finally {
            scanner.close();
        }

        final double end = System.nanoTime();
        final double ellapsed = (end - start);
        return ellapsed / 1E6;
    }

    // private boolean reportTraverse = false;

    private static void traverse(final BxmlStreamReader scanner) throws IOException {
        QName attributeName = null;
        Object attributeValue = null;
        QName elementName = null;
        Object elementValue = null;

        while (scanner.hasNext()) {
            EventType event = scanner.next();
            // if(reportTraverse)System.err.print(event);
            // if(reportTraverse)System.err.print("\t\t");
            switch (event) {
            case START_DOCUMENT:
                break;
            case END_DOCUMENT:
                break;
            case START_ELEMENT:
                elementName = scanner.getElementName();
                assert elementName != null;
                // if(reportTraverse)System.err.print("<" + elementName + ">");
                final int attCount = scanner.getAttributeCount();
                for (int i = 0; i < attCount; i++) {
                    attributeName = scanner.getAttributeName(i);
                    attributeValue = scanner.getAttributeValue(i);
                    // System.err.print(attributeName + "=\"" + attributeValue + "\"");
                }

                break;
            case END_ELEMENT:
                elementName = scanner.getElementName();
                // if(reportTraverse)System.err.print("</" + elementName + ">");
                break;
            case VALUE_BOOL:
            case VALUE_BYTE:
            case VALUE_DOUBLE:
            case VALUE_FLOAT:
            case VALUE_INT:
            case VALUE_LONG:
            case VALUE_STRING:
                elementValue = scanner.getStringValue();
                assert elementValue != null;
                // if(reportTraverse)System.err.print(String.valueOf(elementValue).substring(0,
                // Math.min(100, ((String)(elementValue)).length())));
                break;
            case COMMENT:
                elementValue = scanner.getStringValue();
                // if(reportTraverse)System.err.print("<!-- " + elementValue + " -->");
                break;
            case SPACE:
                break;
            default:
                throw new IllegalStateException("Unknown event: " + event);
            }
            // if(reportTraverse)System.err.print("\n");
        }
    }

    public static void main(String[] argv) {
        try {
            BxmlStreamReaderIntegrationTest bxmlScannerIntegrationTest = new BxmlStreamReaderIntegrationTest();
            for (int i = 0; i < 10; i++) {
                bxmlScannerIntegrationTest.testParseLargeGML2();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
