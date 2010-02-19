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
 * Defines a factory to create {@link BxmlStreamReader} instances out of a stream of XML formatted
 * data encoded as per the OGC Binary XML specification.
 * <p>
 * {@code BxmlInputFactory} instances are stateful, and thus they're required to be <b>thread
 * safe</b> in order to support reuse by client code. Yet, the {@link BxmlStreamReader}s a factory
 * creates are NOT thread safe and it's up to the client code to handle concurrent access, though
 * concurrent access to a stream is highly discouraged.
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 * @since 1.0
 */
public interface BxmlInputFactory {

    /**
     * Returns whether this factory is configured to produce bxml readers that handle namespaces.
     * <p>
     * If not explicitly set through {@link #setNamespaceAware(boolean)}, defaults to {@code false}.
     * </p>
     * 
     * @return {@code true} if the factory produces namespace aware readers, {@code false}
     *         otherwise.
     */
    public boolean isNamespaceAware();

    /**
     * Sets whether the readers produced by this factory recognize element and attribute namespaces.
     * 
     * @param namespaceAware
     *            {@code true} for the factory to create namespace aware readers, {@code false}
     *            otherwise
     */
    public void setNamespaceAware(boolean namespaceAware);

    /**
     * Creates a {@link BxmlStreamReader} ready to parse an XML formatted document encoded as OGC
     * Binary XML specification out of a given input stream.
     * <p>
     * If this method returns successfully, the {@link BxmlStreamReader} returned is settled up to
     * parse the BXML document, meaning the factory or the stream reader implementation, at the
     * discretion of the implementor, has successfully parsed the <b>BXML header</b> AND the <b>xml
     * declaration token</b> (the equivalent to the {@code <?xml?>} processing instruction), if
     * present, and thus is able to handle the document as per the header settings (including
     * character encoding, gzip deflation, byte order, etc).
     * </p>
     * 
     * @param input
     *            the input stream from which to parse a bxml document.
     * @return a bxml reader successfully settled up to parse a bxml document out of the given input
     *         stream.
     * @throws IOException
     *             if an I/O error occurs
     * @throws IllegalArgumentException
     *             if reader's content if not a BXML file (i.e. can't obtain the header).
     */
    public BxmlStreamReader createScanner(InputStream input) throws IOException;

    /**
     * Creates a {@link BxmlStreamReader} ready to parse an XML formatted document encoded as OGC
     * Binary XML specification out of a given input stream.
     * 
     * @param bxmlFile
     * @return a bxml reader successfully settled up to parse a bxml document out of the given file.
     * @throws IOException
     *             if an I/O error occurs
     * @throws IllegalArgumentException
     *             if reader's content if not a BXML file (i.e. can't obtain the header).
     * @see #createScanner(InputStream)
     */
    public BxmlStreamReader createScanner(File bxmlFile) throws IOException;

    /**
     * Convenient method to create a {@link BxmlStreamReader} by opening a read stream from the
     * given URL.
     * 
     * @param bxmlResource
     *            an URL pointing to a binary xml encoded document
     * @return a bxml reader successfully settled up to parse a bxml document out of the given URL.
     * @throws IOException
     *             if an I/O error occurs
     * @throws IllegalArgumentException
     *             if reader's content if not a BXML file (i.e. can't obtain the header).
     * @see #createScanner(InputStream)
     */
    public BxmlStreamReader createScanner(URL bxmlResource) throws IOException;

    /**
     * Creates a {@link BxmlStreamReader} ready to parse an XML formatted document encoded as OGC
     * Binary XML specification out of a given input stream.
     * 
     * @param bxmlSource
     *            a readable Java NIO channel representing the bxml data stream
     * @return a bxml reader successfully settled up to parse a bxml document out of the given URL.
     * @throws IOException
     *             if an I/O error occurs
     * @throws IllegalArgumentException
     *             if reader's content if not a BXML file (i.e. can't obtain the header).
     * @see #createScanner(InputStream)
     */
    public BxmlStreamReader createScanner(ReadableByteChannel bxmlSource) throws IOException;
}
