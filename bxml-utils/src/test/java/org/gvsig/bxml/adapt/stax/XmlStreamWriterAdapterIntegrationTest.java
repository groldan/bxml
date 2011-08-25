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
package org.gvsig.bxml.adapt.stax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.gvsig.bxml.stream.BxmlStreamWriter_Contract;
import org.gvsig.bxml.stream.EncodingOptions;
import org.gvsig.bxml.stream.EventType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Integration test suite for {@link XmlStreamWriterAdapter}
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class XmlStreamWriterAdapterIntegrationTest {

    private EncodingOptions encodingOptions;

    private ByteArrayOutputStream output;

    private BxmlStreamWriter writer;

    @Before
    public void setUp() throws Exception {
        encodingOptions = new EncodingOptions();
        createWriter();
    }

    @After
    public void tearDown() throws Exception {
        encodingOptions = null;
        writer = null;
    }

    private void createWriter() throws Exception {
        output = new ByteArrayOutputStream();
        XMLOutputFactory staxFactory = XMLOutputFactory.newInstance();

        XMLStreamWriter staxWriter = staxFactory.createXMLStreamWriter(output);
        BxmlStreamWriter impl = new XmlStreamWriterAdapter(encodingOptions, staxWriter);
        BxmlStreamWriter_Contract c = new BxmlStreamWriter_Contract(impl);
        this.writer = c;
    }

    @Test
    public void testGetLastEvent() throws IOException {
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
        encodingOptions.setNamespaceAware(false);
        createWriter();

        final String sldNamespace = "http://www.opengis.net/sld";
        writer.writeStartDocument();
        writer.writeComment(" comment1 ");
        writer.writeComment(" comment2 ");
        writer.writeStartElement(sldNamespace, "StyledLayerDescriptor");

        writer.writeStartAttribute("", "xmlns");
        writer.writeValue(sldNamespace);
        writer.writeStartAttribute("", "xmlns:sld");
        writer.writeValue(sldNamespace);
        writer.writeStartAttribute("", "version");
        writer.writeValue("1.0.0");
        writer.writeEndAttributes();

        writer.writeStartElement("", "NamedLayer");
        writer.writeStartElement("", "Name");
        writer.writeValue("ALEXANDRIA:OWS-1.2");
        writer.writeValue("&gt;");
        writer.writeValue("&#x20;");
        writer.writeValue("é");
        writer.writeValue("&#233;");
        writer.writeValue("&eacute;");
        writer.writeValue("x");
        writer.writeEndElement();// Name

        writer.writeStartElement("", "NamedStyle");
        writer.writeStartElement("", "Title");
        writer.writeValue(" my_style ");
        writer.writeValue("&amp;");
        writer.writeValue(" hello ");
        writer.writeComment("x"); // /comment

        writer.writeValue(" there ");
        writer.writeComment("y");
        writer.writeEndElement();// Title

        writer.writeStartElement("", "Name");
        writer.writeValue("my_style");
        writer.writeEndElement();// Name

        writer.writeEndElement();// NamedStyle

        writer.writeEndElement();// NamedLayer

        writer.writeStartElement("", "EmptyElement");
        writer.writeStartAttribute("", "bob");
        writer.writeValue("jim &amp; me");
        writer.writeEndAttributes();
        writer.writeEndElement();// EmptyElement
        writer.writeEndElement();// StyledLayerDescriptor

        writer.writeEndDocument();
        writer.flush();
        writer.close();

        print();
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
        BxmlStreamWriter serializer = this.writer;
        final String sldNamespace = "http://www.opengis.net/sld";
        final String exampleNamespace = "http://www.example.com/test";

        serializer.writeStartDocument();

        serializer.setSchemaLocation(sldNamespace,
                "http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd");
        serializer.setSchemaLocation(exampleNamespace, "http://www.example.com/test/example.xsd");

        serializer.setPrefix("sld", sldNamespace);

        serializer.writeComment(" comment1 ");
        serializer.writeComment(" comment2 ");
        serializer.writeStartElement(sldNamespace, "StyledLayerDescriptor");

        serializer.writeDefaultNamespace(sldNamespace);
        serializer.writeNamespace("sld", sldNamespace);

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

        print();
    }

    @Test
    public void testWriteAutoReferenceableAttributeValues() throws Exception {
        BxmlStreamWriter serializer = this.writer;
        serializer.setWriteAttributeValueAsStringTable("gml:srsName");

        final String gmlNs = "http://www.opengis.net/gml";
        serializer.writeStartDocument();
        serializer.writeStartElement("", "Test");
        serializer.writeNamespace("gml", gmlNs);

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
        // TODO: check the gml:srsName value was written only once as a StringTable reference
        print();
    }

    private void printPlain() throws Exception {
        byte[] byteArray = output.toByteArray();
        InputStreamReader r = new InputStreamReader(new ByteArrayInputStream(byteArray));
        int c;
        while ((c = r.read()) != -1) {
            System.err.print((char) c);
        }
        System.err.print('\n');
    }

    private void print() throws Exception {
        printPlain();
        TransformerFactory txFactory = TransformerFactory.newInstance();
        try {
            txFactory.setAttribute("{http://xml.apache.org/xalan}indent-number", new Integer(2));
        } catch (Exception e) {
            // some
        }

        Transformer tx = txFactory.newTransformer();
        tx.setOutputProperty(OutputKeys.METHOD, "xml");
        tx.setOutputProperty(OutputKeys.INDENT, "yes");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document dom = builder.parse(new ByteArrayInputStream(output.toByteArray()));

        tx.transform(new DOMSource(dom), new StreamResult(new OutputStreamWriter(System.out,
                "utf-8")));
    }

}
