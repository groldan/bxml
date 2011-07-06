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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.BxmlStreamReader_Contract;
import org.gvsig.bxml.stream.EventType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test suite for {@link XmlStreamReaderAdapter}
 * 
 */
public class XmlStreamReaderAdapterIntegrationTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRead() throws Exception {
        final String fileName = "/test-data/cwxml-test.xml";
        final InputStream file = getClass().getResourceAsStream(fileName);
        final BxmlStreamReader reader = createReader(file, true);

        assertEquals(EventType.START_DOCUMENT, reader.getEventType());
        assertEquals(EventType.COMMENT, reader.next());
        assertEquals(" comment1 ", reader.getStringValue());
        assertEquals(EventType.COMMENT, reader.next());
        assertEquals(" comment2 ", reader.getStringValue());
        assertEquals(EventType.START_ELEMENT, reader.next());

        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("StyledLayerDescriptor", reader.getElementName().getLocalPart());

        assertEquals("http://www.opengis.net/sld", reader.getNamespaceURI("sld"));
        assertEquals("sld", reader.getPrefix("http://www.opengis.net/sld"));

        assertEquals(1, reader.getAttributeCount());
        assertEquals("1.0.0", reader.getAttributeValue(0));
        assertEquals("1.0.0", reader.getAttributeValue("", "version"));
        assertEquals("1.0.0", reader.getAttributeValue(null, "version"));

        assertEquals(EventType.START_ELEMENT, reader.nextTag());
        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("NamedLayer", reader.getElementName().getLocalPart());
        assertEquals(0, reader.getAttributeCount());

        assertEquals(EventType.START_ELEMENT, reader.nextTag());
        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("Name", reader.getElementName().getLocalPart());

        assertEquals(EventType.VALUE_STRING, reader.next());
        // assertEquals("ALEXANDRIA:OWS-1.2> éé&eacute;x", reader.getStringValue());
        // REVISIT: I don't know how to avoid the source code mangling the tiled e whith mvn test
        assertTrue(reader.getStringValue().startsWith("ALEXANDRIA:OWS-1.2>"));
        assertEquals(EventType.END_ELEMENT, reader.nextTag());

        assertEquals(EventType.START_ELEMENT, reader.nextTag());
        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("NamedStyle", reader.getElementName().getLocalPart());

        assertEquals(EventType.START_ELEMENT, reader.nextTag());
        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("Title", reader.getElementName().getLocalPart());

        assertEquals(EventType.VALUE_STRING, reader.next());
        assertEquals(" my_style & hello ", reader.getStringValue());
        assertEquals(EventType.END_ELEMENT, reader.nextTag());
        assertEquals(EventType.START_ELEMENT, reader.nextTag());
        assertEquals("http://www.opengis.net/sld", reader.getElementName().getNamespaceURI());
        assertEquals("Name", reader.getElementName().getLocalPart());
        assertEquals(EventType.VALUE_STRING, reader.next());
        assertEquals(" my_style ", reader.getStringValue());
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

    private BxmlStreamReader createReader(final InputStream file, final boolean namespaceAware)
            throws Exception {

        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);

        BxmlStreamReader reader = new XmlStreamReaderAdapter(factory, file);
        reader = new BxmlStreamReader_Contract(reader);

        return reader;
    }

}
