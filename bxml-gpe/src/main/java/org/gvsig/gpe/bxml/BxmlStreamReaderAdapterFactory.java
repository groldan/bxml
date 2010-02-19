package org.gvsig.gpe.bxml;

import java.io.InputStream;

import org.gvsig.gpe.xml.stream.IXmlStreamReader;
import org.gvsig.gpe.xml.stream.IXmlStreamReaderFactory;
import org.gvsig.gpe.xml.stream.XmlStreamException;

public class BxmlStreamReaderAdapterFactory implements IXmlStreamReaderFactory {

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReaderFactory#canParse(java.lang.String)
     */
    public boolean canParse(final String mimeType) {
        return mimeType != null && mimeType.startsWith("text/x-bxml");
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReaderFactory#createParser(java.lang.String,
     *      java.io.InputStream)
     */
    public IXmlStreamReader createParser(final String mimeType, final InputStream in)
            throws XmlStreamException, IllegalArgumentException {
        if (!canParse(mimeType)) {
            throw new IllegalArgumentException("MIME Type '" + mimeType + "' is not supported");
        }
        return new BxmlStreamReaderAdapter(in);
    }

}
