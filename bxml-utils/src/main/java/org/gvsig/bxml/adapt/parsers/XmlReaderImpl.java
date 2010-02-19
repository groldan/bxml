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
package org.gvsig.bxml.adapt.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlFactoryFinder;
import org.gvsig.bxml.stream.BxmlInputFactory;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.gvsig.bxml.util.ProgressListener;
import org.gvsig.bxml.util.ProgressListenerAdapter;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * 
 * @author Gabriel Roldan
 */
public class XmlReaderImpl implements XMLReader {

    private ContentHandler contentHandler;

    private LexicalHandler lexicalHandler;

    private DTDHandler dtdHandler;

    private EntityResolver entityResolver;

    private ErrorHandler errorHandler;

    private boolean namespaceAware;

    private final ProgressListener listener;

    public XmlReaderImpl(final boolean namespaceAware) {
        this(namespaceAware, new ProgressListenerAdapter());
    }

    public XmlReaderImpl(final boolean namespaceAware, final ProgressListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        this.namespaceAware = namespaceAware;
        this.listener = listener;
    }

    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    /**
     * @see org.xml.sax.XMLReader#getContentHandler()
     */
    public ContentHandler getContentHandler() {
        return this.contentHandler;
    }

    /**
     * @see org.xml.sax.XMLReader#setContentHandler(org.xml.sax.ContentHandler)
     */
    public void setContentHandler(final ContentHandler handler) {
        this.contentHandler = handler;
        if (handler instanceof LexicalHandler) {
            this.lexicalHandler = (LexicalHandler) handler;
        }
    }

    /**
     * @see org.xml.sax.XMLReader#getDTDHandler()
     */
    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }

    /**
     * @see org.xml.sax.XMLReader#setDTDHandler(org.xml.sax.DTDHandler)
     */
    public void setDTDHandler(final DTDHandler handler) {
        this.dtdHandler = handler;
    }

    /**
     * @see org.xml.sax.XMLReader#getEntityResolver()
     */
    public EntityResolver getEntityResolver() {
        return this.entityResolver;
    }

    /**
     * @see org.xml.sax.XMLReader#setEntityResolver(org.xml.sax.EntityResolver)
     */
    public void setEntityResolver(EntityResolver resolver) {
        this.entityResolver = resolver;
    }

    /**
     * @see org.xml.sax.XMLReader#getErrorHandler()
     */
    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    /**
     * @see org.xml.sax.XMLReader#setErrorHandler(org.xml.sax.ErrorHandler)
     */
    public void setErrorHandler(final ErrorHandler handler) {
        this.errorHandler = handler;
    }

    /**
     * @see org.xml.sax.XMLReader#getFeature(java.lang.String)
     */
    public boolean getFeature(final String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return false;
    }

    /**
     * @see org.xml.sax.XMLReader#setFeature(java.lang.String, boolean)
     */
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.xml.sax.XMLReader#getProperty(java.lang.String)
     */
    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.xml.sax.XMLReader#setProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(String name, Object value) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.xml.sax.XMLReader#parse(java.lang.String)
     */
    public void parse(String systemId) throws IOException, SAXException {
        throw new UnsupportedOperationException(
                "I don't know yet how to implement resolving the systemId");
    }

    /**
     * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
     */
    public void parse(final InputSource input) throws IOException, SAXException {

        final BxmlStreamReader reader = newReader(input);

        final LinkedList<QName> elementNameStack = new LinkedList<QName>();

        EventType event;
        while ((event = reader.next()) != EventType.END_DOCUMENT) {
            if (listener.isCancelled()) {
                return;
            }
            listener.event(event);
            switch (event) {
            case START_DOCUMENT: {
                contentHandler.startDocument();
                // System.out.println("startDocument");
            }
                break;
            case END_DOCUMENT: {
                contentHandler.endDocument();
                // System.out.println("endDocument");
            }
                break;
            case START_ELEMENT: {
                QName name = reader.getElementName();
                // System.out.println("startElement: " + name);
                elementNameStack.addFirst(name);
                String prefix = name.getPrefix();
                String uri = name.getNamespaceURI();
                String localName = name.getLocalPart();
                String qName = toQName(name);
                boolean doPrefixMapping = prefix.length() > 0 && uri.length() > 0;
                if (doPrefixMapping) {
                    contentHandler.startPrefixMapping(prefix, uri);
                }
                Attributes atts = readAttributes(reader);
                contentHandler.startElement(uri, localName, qName, atts);
            }
                break;
            case END_ELEMENT: {
                QName name = elementNameStack.removeFirst();
                // System.out.println("endElement: " + name);
                contentHandler.endElement(name.getNamespaceURI(), name.getLocalPart(),
                        toQName(name));
                boolean doPrefixMapping = name.getPrefix().length() > 0
                        && name.getNamespaceURI().length() > 0;
                if (doPrefixMapping) {
                    contentHandler.endPrefixMapping(name.getPrefix());
                }
            }
                break;

            case VALUE_BOOL:
            case VALUE_BYTE:
            case VALUE_INT:
            case VALUE_LONG:
            case VALUE_FLOAT:
            case VALUE_DOUBLE:
            case VALUE_STRING:
                // System.out.println("value: " + event);
                String stringValue = reader.getStringValue();
                char[] chars = stringValue.toCharArray();
                contentHandler.characters(chars, 0, chars.length);
                break;
            case VALUE_CDATA: {
                // System.out.println("value: " + event);
                if (lexicalHandler != null) {
                    lexicalHandler.startCDATA();
                }
                /**
                 * The bxml cdata token is defined as having a single value so no need to call next
                 * while the token type is of a value type: <code>
                 * <pre>
                 *  CDataSectionToken {         // &lt;![CDATA[content]]&gt;
                 *     TokenType type = 0x12;   // token-type code
                 *     Value content;           // single content value
                 *  }
                 *  </pre>
                 *  </code>
                 */
                chars = reader.getStringValue().toCharArray();
                contentHandler.characters(chars, 0, chars.length);
                if (lexicalHandler != null) {
                    lexicalHandler.endCDATA();
                }
            }
                break;

            case SPACE: {
                String spaceValue = reader.getStringValue();
                // System.out.println("space: '" + spaceValue + "'");
                chars = spaceValue.toCharArray();
                contentHandler.ignorableWhitespace(chars, 0, chars.length);
            }
                break;

            case COMMENT: {
                if (lexicalHandler != null) {
                    String commentValue = reader.getStringValue();
                    // System.out.println("comment: '" + commentValue + "'");
                    chars = commentValue.toCharArray();
                    lexicalHandler.comment(chars, 0, chars.length);
                }
            }
                break;

            case ATTRIBUTE: {
                throw new IllegalStateException("Shouldn't get an ATTRIBUTE event as the "
                        + "attributes should have been consumed at START_ELEMENT");
            }
            case ATTRIBUTES_END: {
                throw new IllegalStateException("Shouldn't get an ATTRIBUTES_END event as the "
                        + "attributes should have been consumed at START_ELEMENT");
            }
            }
        }
    }

    private BxmlStreamReader newReader(final InputSource input) throws IOException {
        final InputStream byteStream = input.getByteStream();
        final BxmlInputFactory inputFactory = BxmlFactoryFinder.newInputFactory();
        inputFactory.setNamespaceAware(namespaceAware);

        final BxmlStreamReader reader = inputFactory.createScanner(byteStream);
        return reader;
    }

    private Attributes readAttributes(final BxmlStreamReader reader) {
        AttributesImpl atts = new AttributesImpl();
        final int attributeCount = reader.getAttributeCount();
        QName name;
        String qName, value;
        for (int i = 0; i < attributeCount; i++) {
            name = reader.getAttributeName(i);
            value = reader.getAttributeValue(i);
            qName = toQName(name);
            atts.addAttribute(name.getNamespaceURI(), name.getLocalPart(), qName, null, value);
            listener.event(EventType.ATTRIBUTE);
        }
        if (attributeCount > 0) {
            listener.event(EventType.ATTRIBUTES_END);
        }
        return atts;
    }

    private String toQName(final QName name) {
        String prefix = name.getPrefix();
        return prefix == null || "".equals(prefix) ? name.getLocalPart()
                : new StringBuilder(prefix).append(':').append(name.getLocalPart()).toString();
    }

}
