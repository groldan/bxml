package org.gvsig.gpe.bxml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlFactoryFinder;
import org.gvsig.bxml.stream.BxmlInputFactory;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.gvsig.gpe.xml.stream.IXmlStreamReader;
import org.gvsig.gpe.xml.stream.XmlStreamException;

/**
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
class BxmlStreamReaderAdapter implements IXmlStreamReader {

    public static final Map<EventType, org.gvsig.gpe.xml.stream.EventType> bxmlToGpeEventMappings;

    public static final Map<org.gvsig.gpe.xml.stream.EventType, EventType> gpeToBxmlEventMappings;
    static {
        HashMap<EventType, org.gvsig.gpe.xml.stream.EventType> mappings = new HashMap<EventType, org.gvsig.gpe.xml.stream.EventType>();
        // eventTypeMapping.put(EventType.ATTRIBUTE, IXmlStreamReader.ATTRIBUTE);
        mappings.put(EventType.START_DOCUMENT, org.gvsig.gpe.xml.stream.EventType.START_DOCUMENT);
        mappings.put(EventType.COMMENT, org.gvsig.gpe.xml.stream.EventType.COMMENT);
        mappings.put(EventType.END_DOCUMENT, org.gvsig.gpe.xml.stream.EventType.END_DOCUMENT);
        mappings.put(EventType.END_ELEMENT, org.gvsig.gpe.xml.stream.EventType.END_ELEMENT);
        mappings.put(EventType.SPACE, org.gvsig.gpe.xml.stream.EventType.SPACE);
        mappings.put(EventType.START_ELEMENT, org.gvsig.gpe.xml.stream.EventType.START_ELEMENT);
        mappings.put(EventType.VALUE_BOOL, org.gvsig.gpe.xml.stream.EventType.VALUE_BOOL);
        mappings.put(EventType.VALUE_BYTE, org.gvsig.gpe.xml.stream.EventType.VALUE_BYTE);
        mappings.put(EventType.VALUE_DOUBLE, org.gvsig.gpe.xml.stream.EventType.VALUE_DOUBLE);
        mappings.put(EventType.VALUE_FLOAT, org.gvsig.gpe.xml.stream.EventType.VALUE_FLOAT);
        mappings.put(EventType.VALUE_INT, org.gvsig.gpe.xml.stream.EventType.VALUE_INT);
        mappings.put(EventType.VALUE_LONG, org.gvsig.gpe.xml.stream.EventType.VALUE_LONG);
        mappings.put(EventType.VALUE_STRING, org.gvsig.gpe.xml.stream.EventType.VALUE_STRING);

        mappings.put(EventType.ATTRIBUTE, org.gvsig.gpe.xml.stream.EventType.ATTRIBUTE);
        mappings.put(EventType.ATTRIBUTES_END, org.gvsig.gpe.xml.stream.EventType.ATTRIBUTES_END);
        // TODO: no DTD mapping yet
        // mappings.put(EventType., org.gvsig.gpe.xml.stream.EventType.DTD);
        // TODO: no entityReference mapping yet
        // mappings.put(EventType., org.gvsig.gpe.xml.stream.EventType.ENTITY_REFERENCE);
        mappings.put(EventType.NAMESPACE_DECL, org.gvsig.gpe.xml.stream.EventType.NAMESPACE);
        mappings.put(EventType.NONE, org.gvsig.gpe.xml.stream.EventType.NONE);
        // TODO: no processingInstruction mapping yet
        // mappings.put(EventType., org.gvsig.gpe.xml.stream.EventType.PROCESSING_INSTRUCTION);
        mappings.put(EventType.VALUE_CDATA, org.gvsig.gpe.xml.stream.EventType.VALUE_CDATA);

        bxmlToGpeEventMappings = Collections.unmodifiableMap(mappings);

        HashMap<org.gvsig.gpe.xml.stream.EventType, EventType> reverse = new HashMap<org.gvsig.gpe.xml.stream.EventType, EventType>();
        for (Map.Entry<EventType, org.gvsig.gpe.xml.stream.EventType> entry : mappings.entrySet()) {
            reverse.put(entry.getValue(), entry.getKey());
        }
        gpeToBxmlEventMappings = Collections.unmodifiableMap(reverse);
    }

    private BxmlStreamReader reader;

    /**
     * Creates a new bxml to IXmlStreamReader adapter
     * 
     * @param in
     * @throws XmlStreamException
     */
    public BxmlStreamReaderAdapter(InputStream in) throws XmlStreamException {
        setInput(in);
    }

    private void setInput(final InputStream inputStream) throws XmlStreamException {
        if (reader != null) {
            throw new IllegalStateException("inputStream already set");
        }
        final BxmlInputFactory inputFactory = BxmlFactoryFinder.newInputFactory();
        inputFactory.setNamespaceAware(true);
        try {
            this.reader = inputFactory.createScanner(inputStream);
            EventType current = reader.next();
            assert current != EventType.NONE;
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#require(org.gvsig.gpe.xml.stream.EventType,
     *      javax.xml.namespace.QName)
     */
    public void require(org.gvsig.gpe.xml.stream.EventType type, QName name)
            throws IllegalStateException {
        String nsUri = name == null ? null : name.getNamespaceURI();
        String localName = name == null ? null : name.getLocalPart();
        require(type, nsUri, localName);
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#require(org.gvsig.gpe.xml.stream.EventType,
     *      String, String)
     */
    public void require(final org.gvsig.gpe.xml.stream.EventType eventType,
            final String namespaceURI, final String localName) throws IllegalStateException {
        final org.gvsig.gpe.xml.stream.EventType currentEvent;
        try {
            currentEvent = getEventType();
        } catch (XmlStreamException e) {
            throw new IllegalStateException(e);
        }
        if (eventType != null && currentEvent != eventType) {
            throw new IllegalStateException("Client code expected event type " + eventType
                    + " but current event is " + currentEvent);
        }
        if (namespaceURI != null || localName != null) {
            QName currentName;
            try {
                currentName = getElementName();
            } catch (XmlStreamException e) {
                throw new IllegalStateException(e);
            }
            if (namespaceURI != null) {
                if (currentName == null) {
                    throw new IllegalStateException("The current event (" + currentEvent
                            + ") does not have a Name, but client code required namespace "
                            + namespaceURI);

                }
                if (!namespaceURI.equals(currentName.getNamespaceURI())) {
                    throw new IllegalStateException("Client code expected namespace to be '"
                            + namespaceURI + "' but current namespace is '"
                            + currentName.getNamespaceURI() + "' (for element '"
                            + currentName.getLocalPart() + "')");
                }
            }
            if (localName != null) {
                if (currentName == null) {
                    throw new IllegalStateException("The current event (" + currentEvent
                            + ") does not have a Name, but client code required localName "
                            + localName);

                }
                if (!localName.equals(currentName.getLocalPart())) {
                    throw new IllegalStateException("Client code expected localName to be '"
                            + localName + "' but current localName is '"
                            + currentName.getLocalPart() + "' (for namespace '"
                            + currentName.getNamespaceURI() + "')");
                }
            }
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getAttributeCount()
     */
    public int getAttributeCount() throws XmlStreamException {
        return reader.getAttributeCount();
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getAttributeName(int)
     */
    public QName getAttributeName(int i) throws XmlStreamException {
        return reader.getAttributeName(i);
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getAttributeValue(int)
     */
    public String getAttributeValue(int i) throws XmlStreamException {
        return reader.getAttributeValue(i);
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getElementName()
     */
    public QName getElementName() throws XmlStreamException {
        return reader.getElementName();
    }

    /**
     * @return <code>false</code>
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#supportsRandomAccess()
     */
    public boolean supportsRandomAccess() {
        try {
            return reader.supportsRandomAccess();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getElementPosition()
     */
    public long getElementPosition() {
        try {
            return reader.getElementPosition();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#setPosition(long)
     */
    public org.gvsig.gpe.xml.stream.EventType setPosition(final long position) throws IOException {
        EventType bxmlEvent = reader.setPosition(position);
        return bxmlToGpeEventMappings.get(bxmlEvent);
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getEventType()
     */
    public org.gvsig.gpe.xml.stream.EventType getEventType() throws XmlStreamException {
        final EventType bxmlEventType = reader.getEventType();
        final org.gvsig.gpe.xml.stream.EventType eventType = bxmlToGpeEventMappings
                .get(bxmlEventType);
        return eventType;
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getName()
     */
    public QName getName() throws XmlStreamException {
        EventType eventType = reader.getEventType();
        if (EventType.START_ELEMENT == eventType || EventType.END_ELEMENT == eventType) {
            return reader.getElementName();
        }
        return null;
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getText()
     */
    public String getText() throws XmlStreamException {
        try {
            String stringValue = reader.getStringValue();
            return stringValue;
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#isWhitespace()
     */
    public boolean isWhitespace() throws XmlStreamException {
        return EventType.SPACE == reader.getEventType();
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#next()
     */
    public org.gvsig.gpe.xml.stream.EventType next() throws XmlStreamException {
        try {
            reader.next();
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
        return getEventType();
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#nextTag()
     */
    public org.gvsig.gpe.xml.stream.EventType nextTag() throws XmlStreamException {
        try {
            reader.nextTag();
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
        return getEventType();
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getBooleanValue()
     */
    public boolean getBooleanValue() throws XmlStreamException {
        try {
            return reader.getBooleanValue();
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getByteValue()
     */
    public int getByteValue() throws XmlStreamException {
        try {
            return reader.getByteValue();
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getDoubleValue()
     */
    public double getDoubleValue() throws XmlStreamException {
        try {
            return reader.getDoubleValue();
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getFloatValue()
     */
    public float getFloatValue() throws XmlStreamException {
        try {
            return reader.getFloatValue();
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getIntValue()
     */
    public int getIntValue() throws XmlStreamException {
        try {
            return reader.getIntValue();
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getLongValue()
     */
    public long getLongValue() throws XmlStreamException {
        try {
            return reader.getLongValue();
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getValue(boolean[], int, int)
     */
    public void getValue(boolean[] dst, int offset, int length) throws XmlStreamException {
        try {
            reader.getValue(dst, offset, length);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getValue(byte[], int, int)
     */
    public void getValue(byte[] dst, int offset, int length) throws XmlStreamException {
        try {
            reader.getValue(dst, offset, length);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getValue(int[], int, int)
     */
    public void getValue(int[] dst, int offset, int length) throws XmlStreamException {
        try {
            reader.getValue(dst, offset, length);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getValue(long[], int, int)
     */
    public void getValue(long[] dst, int offset, int length) throws XmlStreamException {
        try {
            reader.getValue(dst, offset, length);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getValue(float[], int, int)
     */
    public void getValue(float[] dst, int offset, int length) throws XmlStreamException {
        try {
            reader.getValue(dst, offset, length);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getValue(double[], int, int)
     */
    public void getValue(double[] dst, int offset, int length) throws XmlStreamException {
        try {
            reader.getValue(dst, offset, length);
        } catch (IOException e) {
            throw new XmlStreamException(e);
        }
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getValueCount()
     */
    public int getValueCount() {
        return reader.getValueCount();
    }

    /**
     * @see org.gvsig.gpe.xml.stream.IXmlStreamReader#getValueReadCount()
     */
    public int getValueReadCount() {
        return reader.getValueReadCount();
    }

}
