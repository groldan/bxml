package org.gvsig.bxml.adapt.stax;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

/**
 * 
 * Adapts a StAX {@link XMLStreamReader} as a {@link BxmlStreamReader}
 * <p>
 * Sample usage:
 * 
 * <pre>
 * <code>
 *  javax.xml.stream.XMLInputFactory factory = XMLInputFactory.newInstance();
 *  factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
 *  factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
 *  factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
 * 
 *  BxmlStreamReader reader = new XmlStreamReaderAdapter(factory, inputStream);
 *  reader = new BxmlStreamReader_Contract(reader);
 * </code>
 * </pre>
 * 
 * </p>
 * 
 * @author groldan
 * 
 */
public class XmlStreamReaderAdapter implements BxmlStreamReader {

    private InputStream in;

    private ReturnableStreamReader reader;

    private final XMLInputFactory factory;

    /**
     * @param factory
     * @param stream
     * @throws Exception
     */
    public XmlStreamReaderAdapter(final XMLInputFactory factory, final InputStream stream)
            throws Exception {
        this.factory = factory;
        this.in = stream;
        this.reader = new ReturnableStreamReader(factory.createXMLStreamReader(stream));
    }

    /**
     * @param factory
     *            the input factory pre configured
     * @param stream
     *            the stream of xml events to parse
     * @param encoding
     *            the name of the encoding to parse the stream with
     * @throws Exception
     */
    public XmlStreamReaderAdapter(final XMLInputFactory factory, final InputStream stream,
            final String encoding) throws Exception {
        this.factory = factory;
        this.in = stream;
        this.reader = new ReturnableStreamReader(factory.createXMLStreamReader(stream, encoding));
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#isStandalone()
     */
    public boolean isStandalone() {
        return reader.isStandalone();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#standAloneIsSet()
     */
    public boolean standAloneIsSet() {
        return reader.standaloneSet();
    }

    /**
     * @return {@code true}, though it doesn't really apply.
     * @see org.gvsig.bxml.stream.BxmlStreamReader#isLittleEndian()
     */
    public boolean isLittleEndian() {
        return true;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#isNamespaceAware()
     */
    public boolean isNamespaceAware() {
        Object property = factory.getProperty(XMLInputFactory.IS_NAMESPACE_AWARE);
        return property != null && Boolean.valueOf(property.toString()) == true;
    }

    /**
     * @return false
     * @see org.gvsig.bxml.stream.BxmlStreamReader#isValidated()
     */
    public boolean isValidated() {
        return false;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getXmlVersion()
     */
    public String getXmlVersion() {
        return reader.getVersion();
    }

    /**
     * @return the {@link XMLStreamReader#getCharacterEncodingScheme()} if non null, defaults to
     *         {@code UTF-8} otherwise.
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getCharset()
     */
    public Charset getCharset() {
        String scheme = reader.getCharacterEncodingScheme();
        if (scheme == null) {
            scheme = "UTF-8";
        }
        return Charset.forName(scheme);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#close()
     */
    public void close() throws IOException {
        if (reader != null) {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                propagate(e);
            } finally {
                reader = null;
                if (in != null) {
                    in.close();
                    in = null;
                }
            }
        }
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#isOpen()
     */
    public boolean isOpen() {
        return in != null;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        try {
            return reader.hasNext();
        } catch (XMLStreamException e) {
            propagate(e);
            return false;// will never reach here
        }
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#next()
     */
    public EventType next() throws IOException {
        try {
            int event = reader.next();
            while (event == XMLStreamConstants.ATTRIBUTE) {
                event = reader.next();
            }
            return EventMap.event(event);
        } catch (XMLStreamException e) {
            propagate(e);
            return null;// will never reach here
        }
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#nextTag()
     */
    public EventType nextTag() throws IOException {
        try {
            /*
             * Note: can't call directly reader.nextTag() cause it fails under circumstances that
             * are allowed by BxmlStreamReader.nextTag()
             */
            int event;
            while (true) {
                event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT
                        || event == XMLStreamConstants.END_ELEMENT) {
                    break;
                }
                if (event == XMLStreamConstants.END_DOCUMENT) {
                    throw new IOException("End of document reached");
                }
            }
            return EventMap.event(event);
        } catch (XMLStreamException e) {
            propagate(e);
            return null;// will never reach here
        }
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getEventType()
     */
    public EventType getEventType() {
        int event = reader.getEventType();
        return EventMap.event(event);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#require(org.gvsig.bxml.stream.EventType,
     *      java.lang.String, java.lang.String)
     */
    public void require(final EventType type, final String namespaceUri, final String localName)
            throws IllegalStateException {

        int staxType = EventMap.staxEvent(type);
        try {
            reader.require(staxType, namespaceUri, localName);
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getPrefixes()
     */
    @SuppressWarnings("unchecked")
    public Set<String> getPrefixes() {
        Set<String> prefixes = new HashSet<String>();

        final int namespaceCount = reader.getNamespaceCount();
        final NamespaceContext namespaceContext = reader.getNamespaceContext();
        for (int i = 0; i < namespaceCount; i++) {
            String namespaceURI = reader.getNamespaceURI(i);
            Iterator<String> nsp = namespaceContext.getPrefixes(namespaceURI);
            while (nsp != null && nsp.hasNext()) {
                prefixes.add(nsp.next());
            }
        }
        return prefixes;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getPrefixes(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Set<String> getPrefixes(final String uri) {
        Set<String> prefixes = new HashSet<String>();

        final NamespaceContext namespaceContext = reader.getNamespaceContext();
        Iterator<String> nsp = namespaceContext.getPrefixes(uri);
        while (nsp != null && nsp.hasNext()) {
            prefixes.add(nsp.next());
        }
        return prefixes;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getPrefix(java.lang.String)
     */
    public String getPrefix(final String namespaceURI) {
        NamespaceContext namespaceContext = reader.getNamespaceContext();
        return namespaceContext.getPrefix(namespaceURI);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(final String prefix) {
        return reader.getNamespaceURI(prefix);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getElementName()
     */
    public QName getElementName() {
        return reader.getName();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getElementPosition()
     */
    public long getElementPosition() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @return false
     * @see org.gvsig.bxml.stream.BxmlStreamReader#supportsRandomAccess()
     */
    public boolean supportsRandomAccess() throws IOException {
        return false;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#setPosition(long)
     */
    public EventType setPosition(final long position) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getAttributeCount()
     */
    public int getAttributeCount() {
        return reader.getAttributeCount();
    }

    public QName getAttributeName(final int index) {
        return reader.getAttributeName(index);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getAttributeValue(int)
     */
    public String getAttributeValue(final int index) {
        return reader.getAttributeValue(index);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getAttributeValue(java.lang.String,
     *      java.lang.String)
     */
    public String getAttributeValue(final String namespaceURI, final String localName) {
        return reader.getAttributeValue(namespaceURI, localName);
    }

    /**
     * @return {@code 1}
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValueCount()
     */
    public int getValueCount() {
        return 1;
    }

    private boolean read;

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValueReadCount()
     */
    public int getValueReadCount() {
        return read ? 1 : 0;
    }

    /**
     * Returns the coalesced value of {@link XMLStreamReader#getText()} for any successive
     * characters events
     * 
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getStringValue()
     */
    public String getStringValue() throws IOException {
        String text = reader.getText();
        int next;
        try {
            StringBuilder sb = null;
            while (true) {
                next = reader.next();
                if (next == XMLStreamConstants.CHARACTERS
                        || next == XMLStreamConstants.ENTITY_REFERENCE) {

                    if (sb == null) {
                        sb = new StringBuilder(text);
                    }
                    sb.append(reader.getText());
                } else {
                    break;
                }
            }
            reader.returnEvent(next);
            if (sb != null) {
                text = sb.toString();
            }
        } catch (XMLStreamException e) {
            propagate(e);
        }
        read = true;
        return text;
    }

    public void getValue(boolean[] dst, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    public boolean getBooleanValue() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void getValue(byte[] dst, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    public int getByteValue() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void getValue(int[] dst, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    public int getIntValue() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void getValue(long[] dst, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    public long getLongValue() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void getValue(float[] dst, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    public float getFloatValue() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void getValue(double[] dst, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    public double getDoubleValue() throws IOException {
        throw new UnsupportedOperationException();
    }

    private void propagate(XMLStreamException e) throws IOException {
        throw (IOException) new IOException(e.getMessage()).initCause(e);
    }

    private static class ReturnableStreamReader extends StreamReaderDelegate {

        private int returnedEvent;

        public ReturnableStreamReader(XMLStreamReader delegate) {
            super(delegate);
            returnedEvent = -1;
        }

        @Override
        public int next() throws XMLStreamException {
            int next;
            if (-1 != returnedEvent) {
                next = returnedEvent;
                returnedEvent = -1;
            } else {
                next = super.next();
            }
            return next;
        }

        public void returnEvent(int returnedEvent) {
            if (-1 != this.returnedEvent) {
                throw new IllegalStateException();
            }
            this.returnedEvent = returnedEvent;
        }
    }
}
