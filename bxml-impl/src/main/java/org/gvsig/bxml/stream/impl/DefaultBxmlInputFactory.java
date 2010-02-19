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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.gvsig.bxml.stream.BxmlInputFactory;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.io.BxmlInputStream;
import org.gvsig.bxml.stream.io.BxmlStreamFactory;
import org.gvsig.bxml.stream.io.DefaultStreamFactory;

/**
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 * @see BxmlInputFactory
 */
public class DefaultBxmlInputFactory implements BxmlInputFactory {

    private static BxmlStreamFactory defaultStreamFactory;

    private BxmlStreamFactory streamFactory;

    private boolean namespaceAware;

    public DefaultBxmlInputFactory() {
        synchronized (DefaultBxmlInputFactory.class) {
            if (defaultStreamFactory == null) {
                defaultStreamFactory = new DefaultStreamFactory();
            }
        }
        this.streamFactory = defaultStreamFactory;
    }

    /**
     * @param reader
     *            the low level reader for the {@link BxmlStreamReader} implementation to return
     * @return a default implementation of BxmlStreamReader wrapped by a contract enforcement
     *         wrapper
     * @throws IOException
     *             if thrown by the implementation constructor
     * @see BxmlStreamReader_Contract
     */
    public synchronized BxmlStreamReader createScanner(final BxmlInputStream reader)
            throws IOException {
        if (reader == null) {
            throw new NullPointerException("reader");
        }

        final NamesResolver namesResolver;
        if (namespaceAware) {
            namesResolver = new NamespaceAwareNameResolver();
        } else {
            namesResolver = new NotNamespaceAwareNameResolver();
        }

        final DefaultBxmlStreamReader implementation;
        implementation = new DefaultBxmlStreamReader(reader, namesResolver);

        return implementation;
    }

    /**
     * @param input
     * @return
     * @throws IOException
     * @see org.gvsig.bxml.stream.BxmlInputFactory#createScanner(java.io.InputStream)
     * @see #createScanner(BxmlInputStream)
     */
    public BxmlStreamReader createScanner(InputStream input) throws IOException {
        ReadableByteChannel channel = Channels.newChannel(input);
        return createScanner(channel);
    }

    /**
     * @param bxmlFile
     * @return
     * @throws IOException
     * @see org.gvsig.bxml.stream.BxmlInputFactory#createScanner(java.io.File)
     * @see #createScanner(BxmlInputStream)
     */
    public BxmlStreamReader createScanner(final File bxmlFile) throws IOException {
        FileChannel channel = new FileInputStream(bxmlFile).getChannel();
        return createScanner(channel);
    }

    /**
     * @param bxmlResource
     * @return
     * @throws IOException
     * @see org.gvsig.bxml.stream.BxmlInputFactory#createScanner(java.net.URL)
     * @see DefaultBxmlStreamReader
     */
    public BxmlStreamReader createScanner(final URL bxmlResource) throws IOException {
        InputStream stream = bxmlResource.openStream();
        stream = new BufferedInputStream(stream);
        ReadableByteChannel channel = Channels.newChannel(stream);
        return createScanner(channel);
    }

    public BxmlStreamReader createScanner(final ReadableByteChannel in) throws IOException {
        BxmlInputStream reader = streamFactory.createInputStream(in);
        return createScanner(reader);
    }

    public void setStreamFactory(BxmlStreamFactory streamFactory) {
        if (streamFactory == null) {
            throw new NullPointerException("streamFactory");
        }
        this.streamFactory = streamFactory;
    }

    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlInputFactory#setNamespaceAware(boolean)
     */
    public synchronized void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }
}
