package org.gvsig.bxml.util;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.gvsig.bxml.adapt.parsers.XmlReaderImpl;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class BxmlDecoder {

    private static final int DEFAULT_INDENT_SIZE = 2;

    private int indentSpaces = DEFAULT_INDENT_SIZE;

    /**
     * Established how many space characters to use for indentation, where a value <= 0 means to
     * perform no indentation.
     * 
     * @param indentSpaces
     */
    public void setIndentSize(final int indentSpaces) {
        this.indentSpaces = indentSpaces;
    }

    public int getIndentSize() {
        return indentSpaces;
    }

    public void decode(final ReadableByteChannel source, final WritableByteChannel target,
            final ProgressListener listener) throws IOException {

        final TransformerFactory txFactory = TransformerFactory.newInstance();
        if (indentSpaces > 0) {
            try {
                txFactory.setAttribute("{http://xml.apache.org/xalan}indent-number", Integer
                        .valueOf(indentSpaces));
            } catch (Exception e) {
                // some
            }
        }

        Transformer tx;
        try {
            tx = txFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        }
        tx.setOutputProperty(OutputKeys.METHOD, "xml");
        tx.setOutputProperty(OutputKeys.INDENT, "yes");

        StreamResult streamResult = new StreamResult();
        {
            // Charset contentEncoding = reader.getCharset();
            // String csName = contentEncoding.name();
            // Writer outputWriter = Channels.newWriter(target, csName);
            // streamResult.setWriter(outputWriter);
            streamResult.setOutputStream(Channels.newOutputStream(target));
        }
        Source xmlSource;
        {
            boolean namespaceAware = true;
            XMLReader xmlReader = new XmlReaderImpl(namespaceAware, listener);
            InputSource inputSource = new InputSource(Channels.newInputStream(source));
            xmlSource = new SAXSource(xmlReader, inputSource);
        }

        try {
            tx.transform(xmlSource, streamResult);
        } catch (TransformerException e) {
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        }
    }
}
