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

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.NamespaceSupport;

public class DocumentBuilderImpl extends DocumentBuilder {

    private DocumentBuilder defaultImpl;

    public DocumentBuilderImpl() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        defaultImpl = dbf.newDocumentBuilder();
    }

    public DocumentBuilderImpl(final DocumentBuilder defaultImpl)
            throws ParserConfigurationException {
        this.defaultImpl = defaultImpl;
    }

    /**
     * @see javax.xml.parsers.DocumentBuilder#getDOMImplementation()
     */
    @Override
    public DOMImplementation getDOMImplementation() {
        return defaultImpl.getDOMImplementation();
    }

    /**
     * 
     * @see javax.xml.parsers.DocumentBuilder#isNamespaceAware()
     */
    @Override
    public boolean isNamespaceAware() {
        return defaultImpl.isNamespaceAware();
    }

    /**
     * @return false
     * @see javax.xml.parsers.DocumentBuilder#isValidating()
     */
    @Override
    public boolean isValidating() {
        return false;
    }

    /**
     * 
     * @see javax.xml.parsers.DocumentBuilder#newDocument()
     */
    @Override
    public Document newDocument() {
        return defaultImpl.newDocument();
    }

    /**
     * 
     * @see javax.xml.parsers.DocumentBuilder#setEntityResolver(org.xml.sax.EntityResolver)
     */
    @Override
    public void setEntityResolver(EntityResolver er) {
        defaultImpl.setEntityResolver(er);
    }

    /**
     * 
     * @see javax.xml.parsers.DocumentBuilder#setErrorHandler(org.xml.sax.ErrorHandler)
     */
    @Override
    public void setErrorHandler(ErrorHandler eh) {
        defaultImpl.setErrorHandler(eh);
    }

    /**
     * 
     * @see javax.xml.parsers.DocumentBuilder#parse(org.xml.sax.InputSource)
     */
    @Override
    public Document parse(InputSource is) throws SAXException, IOException {
        final Document dom = defaultImpl.newDocument();

        XmlReaderImpl xmlReaderImpl = new XmlReaderImpl(defaultImpl.isNamespaceAware());
        xmlReaderImpl.setContentHandler(new SaxToDomHandler(dom));
        xmlReaderImpl.parse(is);

        return dom;
    }

    private static final class SaxToDomHandler extends DefaultHandler2 {

        private Document dom;

        private Node currNode;

        public SaxToDomHandler(Document dom) {
            this.dom = dom;
            // we're taking ownership of the dom and it shall be empty
            assert dom.getChildNodes().getLength() == 0;
            currNode = dom;
        }

        /*
         * ContentHandler
         */

        private StringBuilder characters = new StringBuilder();

        /**
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        public void characters(char[] ch, int start, int length) throws SAXException {
            characters.setLength(0);
            characters.append(ch, start, length);

            switch (currNode.getNodeType()) {
            case Node.CDATA_SECTION_NODE:
            case Node.COMMENT_NODE:
                currNode.setNodeValue(characters.toString());
                break;
            case Node.ELEMENT_NODE:
                currNode.appendChild(dom.createTextNode(characters.toString()));
                break;
            default:
                throw new IllegalStateException("characters event on node type "
                        + currNode.getNodeType());
            }
        }

        /**
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startDocument()
         */
        public void startDocument() throws SAXException {
            // nothing to do?
        }

        /**
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endDocument()
         */
        public void endDocument() throws SAXException {
            // ok!
        }

        /**
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String,
         *      java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String uri, String localName, String qName, Attributes atts)
                throws SAXException {
            nsContext.pushContext();
            currNode = currNode.appendChild(dom.createElementNS(uri, qName));
            int nAtts = atts.getLength();
            for (int i = 0; i < nAtts; i++) {
                if (XMLConstants.DEFAULT_NS_PREFIX.equals(atts.getURI(i))) {
                    ((Element) currNode).setAttribute(atts.getLocalName(i), atts.getValue(i));
                } else {
                    ((Element) currNode).setAttributeNS(atts.getURI(i), atts.getQName(i), atts
                            .getValue(i));
                }
            }
        }

        /**
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String,
         *      java.lang.String)
         */
        public void endElement(String uri, String localName, String qName) throws SAXException {
            currNode = currNode.getParentNode();
            nsContext.popContext();
        }

        /**
         * 
         * @see org.xml.sax.helpers.DefaultHandler#ignorableWhitespace(char[], int, int)
         */
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            // we don't need this for a dom...
        }

        /**
         * 
         * @see org.xml.sax.helpers.DefaultHandler#processingInstruction(java.lang.String,
         *      java.lang.String)
         */
        public void processingInstruction(String target, String data) throws SAXException {
            // ???
        }

        /**
         * 
         * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(org.xml.sax.Locator)
         */
        public void setDocumentLocator(Locator locator) {
            // ???
        }

        /**
         * 
         * @see org.xml.sax.helpers.DefaultHandler#skippedEntity(java.lang.String)
         */
        public void skippedEntity(String name) throws SAXException {
            // TODO Auto-generated method stub

        }

        private NamespaceSupport nsContext = new NamespaceSupport();

        /**
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startPrefixMapping(java.lang.String,
         *      java.lang.String)
         */
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            // humm.. nothing to do, really?
            nsContext.declarePrefix(prefix, uri);
        }

        /**
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endPrefixMapping(java.lang.String)
         */
        public void endPrefixMapping(String prefix) throws SAXException {
            // 
        }

        /*
         * LexicalHandler
         */

        /**
         * 
         * @see org.xml.sax.ext.DefaultHandler2#startCDATA()
         */
        public void startCDATA() throws SAXException {
            currNode = currNode.appendChild(dom.createCDATASection(""));
        }

        /**
         * @see org.xml.sax.ext.DefaultHandler2#endCDATA()
         */
        public void endCDATA() throws SAXException {
            currNode = currNode.getParentNode();
        }

        /**
         * @see org.xml.sax.ext.DefaultHandler2#comment(char[], int, int)
         */
        public void comment(char ch[], int start, int length) throws SAXException {
            currNode.appendChild(dom.createComment(new String(ch, start, length)));
        }

    }

}
