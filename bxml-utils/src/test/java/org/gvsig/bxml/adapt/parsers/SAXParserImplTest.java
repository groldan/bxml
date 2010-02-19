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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class SAXParserImplTest {

    @Test
    public void testParseOriginalCubeWerxTestFile() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("test-data/cwxml-test.xml");

        // Document doc = new DocumentBuilderImpl().parse(inputStream);
        // print(doc);

        //SAXParser parser = new SAXParserImpl(true);
         SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

        TransformerFactory txFactory = TransformerFactory.newInstance();
        try {
            txFactory.setAttribute("{http://xml.apache.org/xalan}indent-number", new Integer(2));
        } catch (Exception e) {
            // some
        }

        Transformer tx = txFactory.newTransformer();
        tx.setOutputProperty(OutputKeys.METHOD, "xml");
        tx.setOutputProperty(OutputKeys.INDENT, "yes");

        InputSource inputSource = new InputSource(inputStream);
        XMLReader reader = parser.getXMLReader();
        Source transformSource = new SAXSource(reader, inputSource);

        OutputStreamWriter writer = new OutputStreamWriter(System.out, "utf-8");
        StreamResult outputTarget = new StreamResult(writer);
        tx.transform(transformSource, outputTarget);
        writer.flush();
    }

    @Test
    @Ignore
    public void testParse2() throws Exception {
        InputStream inputStream = new FileInputStream("/Users/groldan/cwxml-test.xml.bxml");

        // System.setProperty("javax.xml.parsers.SAXParserFactory",
        // "org.gvsig.bxml.adapt.sax.SAXParserFactoryImpl");
        // DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // dbf.setAttribute("javax.xml.parsers.SAXParserFactory",
        // "org.gvsig.bxml.adapt.sax.SAXParserFactoryImpl");
        // DocumentBuilder db = dbf.newDocumentBuilder();
        // Document doc = db.parse(inputStream);
        //

        Document doc = new DocumentBuilderImpl().parse(inputStream);
        print(doc);
        //
        // SAXParser parser = new SAXParserImpl(true);
        // // SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        //
        // TransformerFactory txFactory = TransformerFactory.newInstance();
        // try {
        // txFactory.setAttribute("{http://xml.apache.org/xalan}indent-number", new Integer(2));
        // } catch (Exception e) {
        // // some
        // }
        //
        // Transformer tx = txFactory.newTransformer();
        // tx.setOutputProperty(OutputKeys.METHOD, "xml");
        // tx.setOutputProperty(OutputKeys.INDENT, "yes");
        //
        // InputSource inputSource = new InputSource(inputStream);
        // XMLReader reader = parser.getXMLReader();
        // Source transformSource = new SAXSource(reader, inputSource);
        //
        // OutputStreamWriter writer = new OutputStreamWriter(System.out, "utf-8");
        // StreamResult outputTarget = new StreamResult(writer);
        // tx.transform(transformSource, outputTarget);
        // writer.flush();
    }

    protected void print(Document dom) throws Exception {
        TransformerFactory txFactory = TransformerFactory.newInstance();
        try {
            txFactory.setAttribute("{http://xml.apache.org/xalan}indent-number", new Integer(2));
        } catch (Exception e) {
            // some
        }

        Transformer tx = txFactory.newTransformer();
        tx.setOutputProperty(OutputKeys.METHOD, "xml");
        tx.setOutputProperty(OutputKeys.INDENT, "yes");

        tx.transform(new DOMSource(dom), new StreamResult(new OutputStreamWriter(System.out,
                "utf-8")));
    }

}
