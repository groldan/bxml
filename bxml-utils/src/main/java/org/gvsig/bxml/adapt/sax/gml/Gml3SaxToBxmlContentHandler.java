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
package org.gvsig.bxml.adapt.sax.gml;

import java.io.IOException;

import org.gvsig.bxml.adapt.sax.XmlToBxmlContentHandler;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.xml.sax.SAXException;

/**
 * A simple SAX to BXML writing SAX content handler that encodes {@code gml:postList} coordinates as
 * a {@code double[]}.
 * <p>
 * The approach is quite naive, though it serves well to the purpose while a better approach is
 * defined.
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class Gml3SaxToBxmlContentHandler extends XmlToBxmlContentHandler {
    double[] doubleBuffer = new double[100];

    public Gml3SaxToBxmlContentHandler(final BxmlStreamWriter bxmlSerializer) {
        super(bxmlSerializer);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if (characters.length() > 0) {
                String value = characters.toString();
                if ("gml:posList".equals(qName)) {
                    value = value.replaceAll("\\s+", " ");
                    String[] split = value.split(" ");
                    final int arrayLength = split.length;
                    if (arrayLength > doubleBuffer.length) {
                        doubleBuffer = new double[arrayLength];
                    }
                    for (int i = 0; i < arrayLength; i++) {
                        String s = split[i];
                        double d = Double.valueOf(s);
                        doubleBuffer[i] = d;
                    }
                    out.writeValue(doubleBuffer, 0, arrayLength);
                } else {
                    out.writeValue(value);
                }
            }
            characters.setLength(0);
            out.writeEndElement();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

}
