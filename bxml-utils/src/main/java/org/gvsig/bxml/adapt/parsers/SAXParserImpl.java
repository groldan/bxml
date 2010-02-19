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

import javax.xml.parsers.SAXParser;

import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderAdapter;

@SuppressWarnings("deprecation")
public class SAXParserImpl extends SAXParser {

    private XmlReaderImpl xmlReader;

    public SAXParserImpl() {
        this(true);
    }

    public SAXParserImpl(final boolean namespaceAware) {
        this.xmlReader = new XmlReaderImpl(namespaceAware);
    }

    /**
     * 
     * @see javax.xml.parsers.SAXParser#getXMLReader()
     */
    @Override
    public XMLReader getXMLReader() throws SAXException {
        return xmlReader;
    }

    /**
     * 
     * @see javax.xml.parsers.SAXParser#getParser()
     */
    @Override
    public Parser getParser() throws SAXException {
        return new XMLReaderAdapter(xmlReader);
    }

    /**
     * 
     * @see javax.xml.parsers.SAXParser#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return xmlReader.getProperty(name);
    }

    /**
     * 
     * @see javax.xml.parsers.SAXParser#isNamespaceAware()
     */
    @Override
    public boolean isNamespaceAware() {
        return xmlReader.isNamespaceAware();
    }

    @Override
    public boolean isValidating() {
        return false;
    }

    /**
     * 
     * @see javax.xml.parsers.SAXParser#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        // TODO Auto-generated method stub
    }

}
