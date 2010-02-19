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
package org.gvsig.bxml.stream.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.gvsig.bxml.stream.util.System;
import org.gvsig.bxml.stream.util.SystemImpl;

/**
 * {@link BxmlStreamFactory} that uses java nio.
 * 
 * @author gabriel
 */
public class DefaultStreamFactory implements BxmlStreamFactory {

    /**
     * Number of bytes to try to read at once so we don't do a lot of one-byte reads.
     */
    public static final int DEFAULT_BUFFER_SIZE = 64 * 1024;

    private static DefaultStreamFactory INSTANCE;

    private org.gvsig.bxml.stream.util.System system = new SystemImpl();

    /**
     * Convenience setter for system settings in order to help on unit testing.
     * 
     * @param system
     */
    public void setSystem(System system) {
        if (system == null) {
            throw new NullPointerException();
        }
        this.system = system;
    }

    /**
     * @see BxmlStreamFactory#createInputStream(InputStream)
     */
    public BxmlInputStream createInputStream(final InputStream in) throws IOException {
        final ReadableByteChannel inputChannel;
        inputChannel = Channels.newChannel(in);
        return createInputStream(inputChannel);
    }

    /**
     * @see BxmlStreamFactory#createInputStream(ReadableByteChannel)
     */
    public BxmlInputStream createInputStream(final ReadableByteChannel in) throws IOException {

        BxmlInputStream inputStream = new DefaultBxmlInputStream(in, DEFAULT_BUFFER_SIZE);

        if (Boolean.getBoolean("BxmlInputStream.log")) {
            Writer logTo = new OutputStreamWriter(system.stdOut());
            inputStream = new LoggingBxmlInputStream(inputStream, logTo);
        }

        return inputStream;
    }

    /**
     * @throws IOException
     * @see BxmlStreamFactory#createOutputStream(OutputStream)
     */
    public BxmlOutputStream createOutputStream(final OutputStream out) throws IOException {
        WritableByteChannel channel;
        channel = Channels.newChannel(out);
        return createOutputStream(channel);
    }

    /**
     * @throws IOException
     * @see BxmlStreamFactory#createOutputStream(WritableByteChannel)
     */
    public BxmlOutputStream createOutputStream(final WritableByteChannel out) throws IOException {
        if (out == null) {
            throw new NullPointerException("out is null");
        }
        if (!out.isOpen()) {
            throw new IllegalArgumentException("Stream is closed");
        }
        ByteOrder defaultOrder = system.nativeOrder();
        BxmlOutputStream outputStream = new DefaultBxmlOutputStream(out, DEFAULT_BUFFER_SIZE,
                defaultOrder);
        if (Boolean.getBoolean("BxmlOutputStream.log")) {
            Writer writer = new OutputStreamWriter(system.stdOut());
            outputStream = new LoggingBxmlOutputStream(outputStream, writer);
        }
        return outputStream;
    }

    public static DefaultStreamFactory instance() {
        if (INSTANCE == null) {
            INSTANCE = new DefaultStreamFactory();
        }
        return INSTANCE;
    }
}
