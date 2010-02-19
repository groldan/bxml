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
package org.gvsig.bxml.stream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;

/**
 * A fake implementation of {@link BxmlInputFactory} registered for the service provider factory
 * system to only be reached at unit test scope (see /src/test/resources/META-INF/services).
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class FakeBxmlInputFacotry implements BxmlInputFactory {

    public BxmlStreamReader createScanner(InputStream input) throws IOException {
        throw new UnsupportedOperationException("This is a fake factory to test the factory finder");
    }

    public BxmlStreamReader createScanner(ReadableByteChannel input) throws IOException {
        throw new UnsupportedOperationException("This is a fake factory to test the factory finder");
    }

    public BxmlStreamReader createScanner(File bxmlFile) throws IOException {
        throw new UnsupportedOperationException("This is a fake factory to test the factory finder");
    }

    public BxmlStreamReader createScanner(URL bxmlResource) throws IOException {
        throw new UnsupportedOperationException("This is a fake factory to test the factory finder");
    }

    public boolean isNamespaceAware() {
        throw new UnsupportedOperationException("This is a fake factory to test the factory finder");
    }

    public void setNamespaceAware(boolean namespaceAware) {
        throw new UnsupportedOperationException("This is a fake factory to test the factory finder");
    }
}
