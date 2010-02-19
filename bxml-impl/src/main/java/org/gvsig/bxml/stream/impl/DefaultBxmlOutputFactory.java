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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.gvsig.bxml.stream.BxmlOutputFactory;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.gvsig.bxml.stream.EncodingOptions;
import org.gvsig.bxml.stream.io.BxmlOutputStream;
import org.gvsig.bxml.stream.io.BxmlStreamFactory;
import org.gvsig.bxml.stream.io.DefaultStreamFactory;

public class DefaultBxmlOutputFactory implements BxmlOutputFactory {

    private static BxmlStreamFactory defaultStreamFactory;

    private EncodingOptions encodingOptions;

    private BxmlStreamFactory streamFactory;

    public DefaultBxmlOutputFactory() {
        this.encodingOptions = new EncodingOptions();
        synchronized (DefaultBxmlOutputFactory.class) {
            if (defaultStreamFactory == null) {
                defaultStreamFactory = new DefaultStreamFactory();
            }
        }
        this.streamFactory = defaultStreamFactory;
    }

    /**
     * @see BxmlOutputFactory#getEncodingOptions()
     */
    public synchronized EncodingOptions getEncodingOptions() {
        return encodingOptions.clone();
    }

    /**
     * @see BxmlOutputFactory#setEncodingOptions(EncodingOptions)
     */
    public synchronized void setEncodingOptions(final EncodingOptions encodingOptions) {
        if (encodingOptions == null) {
            throw new NullPointerException("encoding options");
        }
        this.encodingOptions = encodingOptions.clone();
    }

    /**
     * @see BxmlOutputFactory#createSerializer(File)
     */
    public synchronized BxmlStreamWriter createSerializer(File outputFile) throws IOException {
        if (outputFile == null) {
            throw new NullPointerException("outputFile");
        }
        if (outputFile.exists() && !outputFile.canWrite()) {
            throw new IOException(outputFile.getAbsolutePath() + " is not writable");
        }
        WritableByteChannel channel = new FileOutputStream(outputFile).getChannel();
        final BxmlOutputStream streamWriter = streamFactory.createOutputStream(channel);
        return createSerializer(streamWriter);
    }

    /**
     * @see BxmlOutputFactory#createSerializer(OutputStream)
     */
    public synchronized BxmlStreamWriter createSerializer(OutputStream output) throws IOException {
        if (output == null) {
            throw new NullPointerException("output");
        }
        WritableByteChannel channel = Channels.newChannel(output);
        return createSerializer(channel);
    }

    /**
     * @see BxmlOutputFactory#createSerializer(WritableByteChannel)
     */
    public BxmlStreamWriter createSerializer(WritableByteChannel output) throws IOException {
        if (output == null) {
            throw new NullPointerException("output");
        }
        final BxmlOutputStream streamWriter = streamFactory.createOutputStream(output);
        return createSerializer(streamWriter);
    }

    private BxmlStreamWriter createSerializer(BxmlOutputStream streamWriter) throws IOException {
        final BxmlStreamWriter writer = new DefaultBxmlStreamWriter(encodingOptions, streamWriter);
        return writer;
    }

    public synchronized void setOutputStreamFactory(BxmlStreamFactory streamFactory) {
        if (streamFactory == null) {
            throw new NullPointerException("streamFactory");
        }
        this.streamFactory = streamFactory;
    }

}
