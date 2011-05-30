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
package org.gvsig.bxml.adapt.sax;

import java.io.IOException;

import javax.xml.parsers.SAXParser;

import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX {@link ContentHandler} that can be used with a {@link SAXParser} to encode an XML stream
 * into BXML encoding format.
 * <p>
 * Sample usage:
 * 
 * <pre>
 * &lt;code&gt;
 * BxmlFactory factory = ...
 * EncodingOptions encodingOptions = ...
 * File outputBxmlFile = ...
 * BxmlStreamWriter encoder = factory.createSerializer(outputBxmlFile, encodingOptions);
 * 
 * XmlToBxmlContentHandler bxmlEncodingHandler = new XmlToBxmlContentHandler(encoder);
 * 
 * SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
 * SAXParser saxParser = saxParserFactory.newSAXParser();
 * try {
 *      saxParser.parse(xmlFile, bxmlEncodingHandler);
 * } finally {
 *      bxmlSerializer.close();
 * }
 * &lt;/code&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class XmlToBxmlContentHandler extends DefaultHandler {

    /**
     * Accumulates content from successive calls to {@link #characters(char[], int, int)} for a
     * single element
     */
    protected StringBuffer characters = new StringBuffer();

    protected BxmlStreamWriter out;

    public XmlToBxmlContentHandler(final BxmlStreamWriter bxmlSerializer) {
        this.out = bxmlSerializer;
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        try {
            out.writeNamespace(prefix, uri);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        // out.setPrefix(prefix, null);
    }

    public void startDocument() throws SAXException {
        try {
            out.writeStartDocument();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void endDocument() throws SAXException {
        try {
            out.writeEndDocument();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        characters.setLength(0);
        try {
            String elemName = localName;// "".equals(localName) ? qName : localName;
            out.writeStartElement(uri, elemName);
            final int length = attributes.getLength();
            String nsUri;
            String attName;
            String value;
            for (int i = 0; i < length; i++) {
                nsUri = attributes.getURI(i);
                attName = attributes.getLocalName(i);
                if ("".equals(attName)) {
                    attName = attributes.getQName(i);
                }
                value = attributes.getValue(i);

                startAttribute(nsUri, attName, value);
            }
            endAttributes();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void startAttribute(String nsUri, String attName, String value) throws IOException {
        out.writeStartAttribute(nsUri, attName);
        out.writeValue(value);
    }

    public void endAttributes() throws IOException {
        out.writeEndAttributes();
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            String value = characters.toString();
            out.writeValue(value);
            characters.setLength(0);
            out.writeEndElement();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        collectValue(ch, start, length);
    }

    private void collectValue(char[] ch, int start, int length) {
        characters.append(ch, start, length);
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
        java.lang.System.out.println("whitespace");
    }
}