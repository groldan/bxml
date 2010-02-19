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
package org.gvsig.bxml.stream.impl;

import java.io.IOException;

import org.gvsig.bxml.stream.io.BxmlInputStream;

/**
 * Helper class to assist in creating BXML data structures for unit tests.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id:TestData.java 237 2007-12-09 19:11:15Z groldan $
 */
final class TestData {

    private TestData() {
        // no-op
    }

    public static BxmlInputStream getBxmlInputStream(byte[] content) throws IOException {
        BxmlInputStream bxmlIn = new MockBxmlInputStream(content);
        return bxmlIn;
    }

    /**
     * Creates and returns a {@link DefaultBxmlStreamReader} set up with a mockup
     * {@link MockBxmlInputStream} which content is the {@code content} byte array passed as
     * argument.
     * 
     * @param header
     * @return
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static DefaultBxmlStreamReader getDefaultBxmlScanner(final byte[] content)
            throws IllegalArgumentException, IOException {
        final BxmlInputStream in = new MockBxmlInputStream(content);
        DefaultBxmlStreamReader scanner = new DefaultBxmlStreamReader(in);
        return scanner;
    }
}
