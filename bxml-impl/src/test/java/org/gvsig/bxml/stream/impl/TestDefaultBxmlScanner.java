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
 * A {@link DefaultBxmlStreamReader} subclass that aims to assist unit tests by exposing some
 * private fields.
 * <p>
 * This is a commonly used, non intrusive strategy, that allows to test a class by its interface and
 * yet provide some level of mocking up over internal behavior in order to better isolate a method
 * under test.
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
class TestDefaultBxmlScanner extends DefaultBxmlStreamReader {

    public TestDefaultBxmlScanner(BxmlInputStream reader) throws IOException {
        super(reader);
    }

    /**
     * Exposes the string table so test cases can populate it prior to call a method under test.
     */
    public void addToStringTable(final String s) {
        super.getStringTable().add(s, getStringTable().size() * 10);
    }
}
