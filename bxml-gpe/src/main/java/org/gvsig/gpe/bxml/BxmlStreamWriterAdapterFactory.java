/**
 * 
 */
package org.gvsig.gpe.bxml;

import java.io.OutputStream;

import org.gvsig.gpe.xml.stream.IXmlStreamWriter;
import org.gvsig.gpe.xml.stream.IXmlStreamWriterFactory;
import org.gvsig.gpe.xml.stream.XmlStreamException;

/**
 * @author groldan
 * 
 */
public class BxmlStreamWriterAdapterFactory implements IXmlStreamWriterFactory {

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriterFactory#canWrite(java.lang.String)
     */
    public boolean canWrite(final String mimeType) {
        return mimeType != null && mimeType.startsWith("text/x-bxml");
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamWriterFactory#createWriter(java.lang.String,
     *      java.io.OutputStream)
     */
    public IXmlStreamWriter createWriter(final String mimeType, final OutputStream out)
            throws XmlStreamException, IllegalArgumentException {
        if (!canWrite(mimeType)) {
            throw new IllegalArgumentException("MIME Type '" + mimeType + "' is not supported");
        }
        return new BxmlStreamWriterAdapter(out);
    }

}
