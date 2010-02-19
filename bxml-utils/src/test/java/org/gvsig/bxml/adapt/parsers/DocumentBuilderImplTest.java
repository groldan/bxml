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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DocumentBuilderImplTest {

    @Test
    @Ignore
    public void testParse() throws Exception {
        final String bxmlFile = "test-data/cwxml-test.bxml";
        InputStream inputStream = getClass().getResourceAsStream(bxmlFile);
        if (inputStream == null) {
            throw new FileNotFoundException(bxmlFile);
        }

        DocumentBuilderImpl docBuilder = new DocumentBuilderImpl();
        Document doc = docBuilder.parse(inputStream);

        assertNotNull(doc);

        print(doc);
        
        assertEquals(Node.COMMENT_NODE, doc.getFirstChild().getNodeType());
        assertEquals(Node.COMMENT_NODE, doc.getFirstChild().getNextSibling().getNodeType());
        assertEquals("comment1", doc.getFirstChild().getNodeValue());
        assertEquals("comment2", doc.getFirstChild().getNextSibling().getNodeValue());

        Element root = doc.getDocumentElement();
        assertNotNull(root);
        final String sldPrefix = "sld";
        final String sldNsUri = "http://www.opengis.net/sld";
        assertEquals(sldNsUri, root.getNamespaceURI());
        assertEquals("StyledLayerDescriptor", root.getLocalName());
        assertEquals(sldPrefix, root.getPrefix());
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
