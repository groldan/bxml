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
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;

/**
 * Defines a factory to create {@link BxmlStreamWriter} instances settled up with the factory's
 * {@link EncodingOptions encoding options}.
 * <p>
 * A {@code BxmlStreamWriter} can be created out of an ordinary {@code OutputStream} or a
 * destination {@code File}.
 * </p>
 * <p>
 * {@code BxmlOutputFactory} instances are stateful. Its sole state is meant to be an instance of
 * {@link EncodingOptions}, which determines the encoding preferences the {@link BxmlStreamWriter}s
 * created by the factory will operate with. Yet externally modifying the state of the {@code
 * EncodingOptions} object used to alter this factory state shall not actually alter the factory
 * state. EncodingOptions are mutable for the sake of simplicity using this API, though
 * implementations shall use a safe copy internally or other way of ensuring information hiding.
 * </p>
 * <p>
 * {@code BxmlOutputFactory} instances are required to be <b>thread safe</b> in order to support
 * reuse by client code. Yet, the {@link BxmlStreamWriter}s a factory creates are NOT thread safe
 * and it's up to the client code to handle concurrent access, though concurrent access to a stream
 * is highly discouraged.
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 * @since 1.0
 */
public interface BxmlOutputFactory {

    /**
     * Returns a safe copy of this factory's encoding options.
     * <p>
     * External modifications of the returned object state does not affect the factory state. To
     * change the factory encoding settings use {@link #setEncodingOptions(EncodingOptions)}.
     * </p>
     * 
     * @return a safe copy of this factory's encoding options;
     * @see #setEncodingOptions(EncodingOptions)
     */
    public EncodingOptions getEncodingOptions();

    /**
     * Sets this factory encoding options to {@code encodingOptions}.
     * <p>
     * Further external modifications to the {@code encodingOptions} object shall not affect this
     * factory instance. The factory encoding options will be the ones the {@code encodingOptions}
     * object had at the time this method was last called.
     * </p>
     * 
     * @param encodingOptions
     *            parameter object encapsulating the encoding preferencies writers created with this
     *            factory shall comply to.
     */
    public void setEncodingOptions(EncodingOptions encodingOptions);

    /**
     * Creates a {@link BxmlStreamWriter} settled up to create documents using the factory's
     * {@link #getEncodingOptions() encoding options}, which will be written to the given target
     * file.
     * <p>
     * Note writing the binary xml file is a serial process and thus if the {@code outputFile}
     * already exists it will be completely replaced by the new contents. Yet, a
     * {@link BxmlStreamWriter} implementation is required not to truncate the contents of the
     * existing file until the first write operation is made (that is,
     * {@link BxmlStreamWriter#writeStartDocument()} is called.
     * </p>
     * 
     * @param outputFile
     *            the destination file where to write the BXML document.
     * @return a writer for the given {@code outputFile} and the factory's
     *         {@link #getEncodingOptions() encoding options}
     * @throws IOException
     *             if an error occurs setting up the writer
     */
    public BxmlStreamWriter createSerializer(final File outputFile) throws IOException;

    /**
     * Creates a {@link BxmlStreamWriter} settled up to create documents using the factory's
     * {@link #getEncodingOptions() encoding options}, which will be written to the given target
     * output stream.
     * 
     * @param output
     *            the destination stream where to write the BXML document.
     * @return a writer for the given {@code output} and the factory's {@link #getEncodingOptions()
     *         encoding options}
     * @throws IOException
     *             if an error occurs setting up the writer
     */
    public BxmlStreamWriter createSerializer(final OutputStream output) throws IOException;

    /**
     * Creates a {@link BxmlStreamWriter} settled up to create documents using the factory's
     * {@link #getEncodingOptions() encoding options}, which will be written to the given target
     * output stream.
     * 
     * @param output
     *            the destination stream where to write the BXML document.
     * @return a writer for the given {@code output} and the factory's {@link #getEncodingOptions()
     *         encoding options}
     * @throws IOException
     *             if an error occurs setting up the writer
     */
    public BxmlStreamWriter createSerializer(final WritableByteChannel output) throws IOException;
}
