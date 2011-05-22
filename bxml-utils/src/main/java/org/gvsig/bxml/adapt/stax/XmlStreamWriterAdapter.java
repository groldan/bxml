package org.gvsig.bxml.adapt.stax;

import static org.gvsig.bxml.stream.EventType.ATTRIBUTE;
import static org.gvsig.bxml.stream.EventType.ATTRIBUTES_END;
import static org.gvsig.bxml.stream.EventType.COMMENT;
import static org.gvsig.bxml.stream.EventType.END_DOCUMENT;
import static org.gvsig.bxml.stream.EventType.END_ELEMENT;
import static org.gvsig.bxml.stream.EventType.NAMESPACE_DECL;
import static org.gvsig.bxml.stream.EventType.NONE;
import static org.gvsig.bxml.stream.EventType.START_DOCUMENT;
import static org.gvsig.bxml.stream.EventType.START_ELEMENT;
import static org.gvsig.bxml.stream.EventType.VALUE_BOOL;
import static org.gvsig.bxml.stream.EventType.VALUE_BYTE;
import static org.gvsig.bxml.stream.EventType.VALUE_DOUBLE;
import static org.gvsig.bxml.stream.EventType.VALUE_FLOAT;
import static org.gvsig.bxml.stream.EventType.VALUE_INT;
import static org.gvsig.bxml.stream.EventType.VALUE_LONG;
import static org.gvsig.bxml.stream.EventType.VALUE_STRING;
import static org.gvsig.bxml.stream.io.TokenType.EmptyElement;
import static org.gvsig.bxml.stream.io.ValueType.ArrayCode;
import static org.gvsig.bxml.stream.io.ValueType.BoolCode;
import static org.gvsig.bxml.stream.io.ValueType.ByteCode;
import static org.gvsig.bxml.stream.io.ValueType.DoubleCode;
import static org.gvsig.bxml.stream.io.ValueType.FloatCode;
import static org.gvsig.bxml.stream.io.ValueType.IntCode;
import static org.gvsig.bxml.stream.io.ValueType.LongCode;
import static org.gvsig.bxml.stream.io.ValueType.StringCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.gvsig.bxml.stream.EncodingOptions;
import org.gvsig.bxml.stream.EventType;
import org.gvsig.bxml.stream.io.TokenType;
import org.gvsig.bxml.stream.io.ValueType;

public class XmlStreamWriterAdapter implements BxmlStreamWriter {

    private final EncodingOptions encodingOptions;

    private final XMLStreamWriter staxWriter;

    private final ElementStack openElements;

    private boolean open;

    private QName currAttName;

    private StringBuilder currAttValue = new StringBuilder();

    private boolean arrayInProgress;

    private ValueType currentValueType;

    private long writtenValueLength;

    private long valueLength;

    private EventType lastEvent;

    private EventType lastTagEvent;

    private final Map<String, String> schemaLocations;

    private final Map<String, String> pendingNamespaces;

    private boolean processingAttributes;

    public XmlStreamWriterAdapter(final EncodingOptions encodingOptions,
            final XMLStreamWriter staxWriter) {
        this.encodingOptions = encodingOptions;
        this.staxWriter = staxWriter;
        openElements = new ElementStack();
        open = true;
        schemaLocations = new HashMap<String, String>();
        pendingNamespaces = new HashMap<String, String>();
        lastEvent = NONE;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getEncodingOptions()
     */
    public EncodingOptions getEncodingOptions() {
        return encodingOptions.clone();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getLastEvent()
     */
    public EventType getLastEvent() {
        return lastEvent;
    }

    public EventType getLastTagEvent() {
        return lastTagEvent;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getCurrentElementName()
     */
    public String getCurrentElementName() {
        return openElements.getCurrentElement().localName;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getCurrentElementNamespace()
     */
    public String getCurrentElementNamespace() {
        return openElements.getCurrentElement().namespaceUri;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getTagDeep()
     */
    public int getTagDeep() {
        final int size = openElements.size();
        return size;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#close()
     */
    public void close() throws IOException {
        try {
            staxWriter.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        } finally {
            open = false;
        }
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#isOpen()
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#flush()
     */
    public void flush() throws IOException {
        try {
            staxWriter.flush();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#require(org.gvsig.bxml.stream.EventType,
     *      java.lang.String, java.lang.String)
     */
    public void require(EventType type, String namespaceUri, String localName)
            throws IllegalStateException {
        if (type != null && type != getLastEvent()) {
            throw new IllegalStateException("expected current event to be " + type + " but is "
                    + getLastEvent());
        }
        if (namespaceUri != null && !namespaceUri.equals(getCurrentElementNamespace())) {
            throw new IllegalStateException("expected namespace URI to be " + namespaceUri
                    + " but is " + getCurrentElementNamespace());
        }
        if (localName != null && !localName.equals(getCurrentElementName())) {
            throw new IllegalStateException("expected element's local name to be " + localName
                    + " but is " + getCurrentElementName());
        }
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#setSchemaLocation(java.lang.String,
     *      java.lang.String)
     */
    public void setSchemaLocation(String namespaceUri, String schemaLocationUri) {
        schemaLocations.put(namespaceUri, schemaLocationUri);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartDocument()
     */
    public void writeStartDocument() throws IOException {
        try {
            staxWriter.writeStartDocument();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
        lastEvent = START_DOCUMENT;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeEndDocument()
     */
    public void writeEndDocument() throws IOException {
        try {
            staxWriter.writeEndDocument();
            staxWriter.flush();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
        lastEvent = END_DOCUMENT;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartElement(java.lang.String,
     *      java.lang.String)
     */
    public void writeStartElement(String namespaceUri, String localName) throws IOException {
        // start a new current element
        {
            openElements.newElement(EmptyElement, namespaceUri, localName);
        }
        try {
            final String prefix = staxWriter.getPrefix(namespaceUri);
            if (prefix == null) {
                // we got a non prefix mapped namespace for this element
                // lets declare the element namespace inline
                // staxWriter.writeDefaultNamespace(namespaceUri);
                staxWriter.setDefaultNamespace(namespaceUri);
            }
            staxWriter.writeStartElement(namespaceUri, localName);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }

        lastEvent = lastTagEvent = START_ELEMENT;
        if (openElements.size() == 1) {
            declareSchemaLocations();
        }
        lastEvent = lastTagEvent = START_ELEMENT;
        this.writtenValueLength = 0;
        this.valueLength = 0;
        this.currentValueType = null;
    }

    private void declareSchemaLocations() throws IOException {
        if (schemaLocations.size() > 0) {
            StringBuilder sl = new StringBuilder();

            for (Iterator<Map.Entry<String, String>> it = schemaLocations.entrySet().iterator(); it
                    .hasNext();) {

                Map.Entry<String, String> entry = it.next();
                String nsUri = entry.getKey();
                String schemaLocationUri = entry.getValue();

                sl.append(nsUri).append(' ').append(schemaLocationUri);
                if (it.hasNext()) {
                    sl.append(' ');
                }
            }

            try {
                staxWriter.setPrefix("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
                staxWriter.writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
                staxWriter.writeAttribute(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                        "schemaLocation", sl.toString());
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }

            schemaLocations.clear();
        }
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartElement(javax.xml.namespace.QName)
     */
    public void writeStartElement(QName qname) throws IOException {
        writeStartElement(qname.getNamespaceURI(), qname.getLocalPart());
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeEndElement()
     */
    public void writeEndElement() throws IOException {
        // call writePendingNamespaces before popping the current element in case it was a direct
        // start/end element call without value or attributes
        // writePendingNamespaces();

        // if (this.pendingNamespacesJustWritten) {
        // writeEndNamespaces();
        // }

        try {
            staxWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }

        openElements.popCurrentElement();
        lastEvent = lastTagEvent = END_ELEMENT;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartAttribute(java.lang.String,
     *      java.lang.String)
     */
    public void writeStartAttribute(String namespaceUri, String localName) throws IOException {
        writeStartAttribute(new QName(namespaceUri, localName));
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartAttribute(javax.xml.namespace.QName)
     */
    public void writeStartAttribute(QName qname) throws IOException {
        flushAttribute();
        this.currAttName = qname;
        this.processingAttributes = true;
        // no more need to check for auto closing the attribute list, the user is now responsible
        this.writtenValueLength = 0;
        this.valueLength = 0;
        lastEvent = ATTRIBUTE;
    }

    private void flushAttribute() throws IOException {
        if (currAttName != null) {
            try {
                String namespaceURI = currAttName.getNamespaceURI();
                String localPart = currAttName.getLocalPart();
                String value = currAttValue.toString();
                String prefix = staxWriter.getPrefix(namespaceURI);
                if (null == prefix) {
                    staxWriter.setDefaultNamespace(namespaceURI);
                }
                staxWriter.writeAttribute(namespaceURI, localPart, value);
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
            currAttName = null;
            currAttValue.setLength(0);
        }
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeEndAttributes()
     */
    public void writeEndAttributes() throws IOException {
        flushAttribute();
        // migrates from EmptyElement to EmptyAttrElement
        if (processingAttributes) {// no attributes may have been written
            processingAttributes = false;
            this.writtenValueLength = 0;
            this.valueLength = 0;
        }
        lastEvent = ATTRIBUTES_END;
        flushAttribute();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(java.lang.String)
     */
    public void writeValue(String value) throws IOException {
        startValue(StringCode);
        writeValueInternal(value);
        writtenValueLength++;
        lastEvent = VALUE_STRING;
    }

    private void writeValueInternal(String value) throws IOException {
        if (processingAttributes) {
            if (writtenValueLength > 0) {
                // this is an array of some primitive type
                currAttValue.append(' ');
            }
            currAttValue.append(value);
        } else {
            try {
                if (writtenValueLength > 0) {
                    // this is an array of some primitive type
                    staxWriter.writeCharacters(" ");
                }
                staxWriter.writeCharacters(value);
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(char[], int, int)
     */
    public void writeValue(char[] chars, int offset, int length) throws IOException {
        startValue(StringCode);
        writeValueInternal(new String(chars, offset, length));
        writtenValueLength++;
        lastEvent = VALUE_STRING;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(int)
     */
    public void writeValue(int value) throws IOException {
        startValue(IntCode);
        writeValueInternal(String.valueOf(value));
        writtenValueLength++;
        lastEvent = VALUE_INT;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(long)
     */
    public void writeValue(long value) throws IOException {
        startValue(LongCode);
        writeValueInternal(String.valueOf(value));
        writtenValueLength++;
        lastEvent = VALUE_LONG;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(float)
     */
    public void writeValue(float value) throws IOException {
        startValue(FloatCode);
        writeValueInternal(String.valueOf(value));
        writtenValueLength++;
        lastEvent = VALUE_FLOAT;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(double)
     */
    public void writeValue(double value) throws IOException {
        startValue(DoubleCode);
        writeValueInternal(String.valueOf(value));
        writtenValueLength++;
        lastEvent = VALUE_DOUBLE;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(boolean)
     */
    public void writeValue(boolean value) throws IOException {
        startValue(BoolCode);
        writeValueInternal(String.valueOf(value));
        writtenValueLength++;
        lastEvent = VALUE_BOOL;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(boolean[], int, int)
     */
    public void writeValue(boolean[] value, int offset, int length) throws IOException {
        final boolean arrayAlreadyInProgress = isArrayInProgress();
        if (!arrayAlreadyInProgress) {
            startArray(VALUE_BOOL, length);
        }
        for (int i = 0; i < length; i++) {
            writeValueInternal(String.valueOf(value[i + offset]));
        }
        if (!arrayAlreadyInProgress) {
            endArray();
            valueLength = length;
        }
        lastEvent = VALUE_BOOL;
        writtenValueLength += length;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(byte)
     */
    public void writeValue(byte value) throws IOException {
        startValue(ByteCode);
        writeValueInternal(String.valueOf(value));
        writtenValueLength++;
        lastEvent = VALUE_BYTE;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(byte[], int, int)
     */
    public void writeValue(byte[] value, int offset, int length) throws IOException {
        final boolean arrayAlreadyInProgress = isArrayInProgress();
        if (!arrayAlreadyInProgress) {
            startArray(VALUE_BYTE, length);
        }
        for (int i = 0; i < length; i++) {
            writeValueInternal(String.valueOf(value[i + offset]));
        }
        if (!arrayAlreadyInProgress) {
            endArray();
            valueLength = length;
        }
        lastEvent = VALUE_BYTE;
        writtenValueLength += length;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(int[], int, int)
     */
    public void writeValue(int[] value, int offset, int length) throws IOException {
        final boolean arrayAlreadyInProgress = isArrayInProgress();
        if (!arrayAlreadyInProgress) {
            startArray(VALUE_INT, length);
        }
        for (int i = 0; i < length; i++) {
            writeValueInternal(String.valueOf(value[i + offset]));
        }
        if (!arrayAlreadyInProgress) {
            endArray();
            valueLength = length;
        }
        lastEvent = VALUE_INT;
        writtenValueLength += length;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(long[], int, int)
     */
    public void writeValue(long[] value, int offset, int length) throws IOException {
        final boolean arrayAlreadyInProgress = isArrayInProgress();
        if (!arrayAlreadyInProgress) {
            startArray(VALUE_LONG, length);
        }
        for (int i = 0; i < length; i++) {
            writeValueInternal(String.valueOf(value[i + offset]));
        }
        if (!arrayAlreadyInProgress) {
            endArray();
            valueLength = length;
        }
        lastEvent = VALUE_LONG;
        writtenValueLength += length;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(float[], int, int)
     */
    public void writeValue(float[] value, int offset, int length) throws IOException {
        final boolean arrayAlreadyInProgress = isArrayInProgress();
        if (!arrayAlreadyInProgress) {
            startArray(VALUE_FLOAT, length);
        }
        for (int i = 0; i < length; i++) {
            writeValueInternal(String.valueOf(value[i + offset]));
        }
        if (!arrayAlreadyInProgress) {
            endArray();
            valueLength = length;
        }
        lastEvent = VALUE_FLOAT;
        writtenValueLength += length;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(double[], int, int)
     */
    public void writeValue(double[] value, int offset, int length) throws IOException {
        final boolean arrayAlreadyInProgress = isArrayInProgress();
        if (!arrayAlreadyInProgress) {
            startArray(VALUE_DOUBLE, length);
        }
        for (int i = 0; i < length; i++) {
            writeValueInternal(String.valueOf(value[i + offset]));
        }
        if (!arrayAlreadyInProgress) {
            endArray();
            valueLength = length;
        }
        lastEvent = VALUE_DOUBLE;
        writtenValueLength += length;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeComment(java.lang.String)
     */
    public void writeComment(String commentContent) throws IOException {
        try {
            staxWriter.writeComment(commentContent);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
        lastEvent = COMMENT;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#startArray(org.gvsig.bxml.stream.EventType, int)
     */
    public void startArray(EventType valueType, int arrayLength) throws IOException {
        ValueType arrayElementType;
        switch (valueType) {
        case VALUE_BOOL:
            arrayElementType = ValueType.BoolCode;
            break;
        case VALUE_BYTE:
            arrayElementType = ValueType.ByteCode;
            break;
        case VALUE_INT:
            arrayElementType = ValueType.IntCode;
            break;
        case VALUE_LONG:
            arrayElementType = ValueType.LongCode;
            break;
        case VALUE_FLOAT:
            arrayElementType = ValueType.FloatCode;
            break;
        case VALUE_DOUBLE:
            arrayElementType = ValueType.DoubleCode;
            break;
        default:
            throw new IllegalArgumentException("Not a numeric value type: " + valueType);
        }
        startArrayValue(arrayElementType, arrayLength);
        this.arrayInProgress = true;
        lastEvent = valueType;
        valueLength = arrayLength;
        writtenValueLength = 0;
    }

    private void startArrayValue(final ValueType arrayElementType, final int length)
            throws IOException {
        startValue(ArrayCode);
    }

    private void startValue(ValueType valueTypeCode) throws IOException {
        if (currentValueType == ValueType.ArrayCode) {
            // ignore, being called from a writeValue(primitiveType) while encoding an array
            return;
        }
        currentValueType = valueTypeCode;
        valueLength = 1;
        writtenValueLength = 0;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#isArrayInProgress()
     */
    public boolean isArrayInProgress() {
        return arrayInProgress;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#endArray()
     */
    public void endArray() throws IOException {
        this.arrayInProgress = false;
        this.currentValueType = null;
        this.writtenValueLength = 0;
        this.valueLength = 0;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#setDefaultNamespace(java.lang.String)
     */
    public void setDefaultNamespace(String defaultNamespaceUri) throws IOException {
        try {
            staxWriter.setDefaultNamespace(defaultNamespaceUri);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        lastEvent = NAMESPACE_DECL;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getPrefix(java.lang.String)
     */
    public String getPrefix(String uri) {
        try {
            return staxWriter.getPrefix(uri);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeNamespace(java.lang.String,
     *      java.lang.String)
     */
    public void writeNamespace(String prefix, String namespaceUri) throws IOException {
        try {
            staxWriter.setPrefix(prefix, namespaceUri);
            staxWriter.writeNamespace(prefix, namespaceUri);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
        // this.namesResolver.declarePrefix(prefix, namespaceUri);
        // this.pendingNamespaces.put(prefix, namespaceUri);
        // writePendingNamespaces();
        lastEvent = NAMESPACE_DECL;
    }

    /**
     * @return {@code false}
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#supportsStringTableValues()
     */
    public boolean supportsStringTableValues() {
        return false;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStringTableValue(long)
     */
    public void writeStringTableValue(long stringTableEntryId) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getStringTableReference(java.lang.CharSequence)
     */
    public long getStringTableReference(CharSequence stringValue) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getValueLength()
     */
    public long getValueLength() {
        return valueLength;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#getWrittenValueCount()
     */
    public long getWrittenValueCount() {
        return writtenValueLength;
    }

    /**
     * Calling this method makes no effect as the underlying xml writer is not bxml
     * 
     * @see org.gvsig.bxml.stream.BxmlStreamWriter#setWriteAttributeValueAsStringTable(java.lang.String)
     */
    public void setWriteAttributeValueAsStringTable(String qName) {
        // ignored
    }

    /**
     * Represents a stack of open elements.
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static final class ElementStack {
        private static class OpenElement {
            TokenType elementType;

            String namespaceUri, localName;

            @Override
            public String toString() {
                return "[" + elementType + ":" + namespaceUri + "#" + localName + "]";
            }
        }

        /**
         * Use an arraylist to simulate the open elements stack because additions/removals perform
         * much better then Stack and LinkedList. The current element is the last one in the list.
         */
        private List<OpenElement> openElements = new ArrayList<OpenElement>();

        /**
         * Pool of openelements to avoid creating thousands of objects
         */
        private List<OpenElement> openElementPool = new ArrayList<OpenElement>();

        private OpenElement currentElement;

        public OpenElement getCurrentElement() {
            return currentElement;
        }

        public TokenType getCurrentElementType() {
            return currentElement.elementType;
        }

        /**
         * Returns a new current element
         * 
         * @param elementType
         * @param fileOffset
         * @param localName
         * @param namespaceUri
         */
        public void newElement(final TokenType elementType, final String namespaceUri,
                final String localName) {
            OpenElement element;
            if (openElementPool.size() > 0) {
                element = openElementPool.remove(openElementPool.size() - 1);
            } else {
                element = new OpenElement();
            }

            element.elementType = EmptyElement;
            element.namespaceUri = namespaceUri;
            element.localName = localName;

            openElements.add(element);
            currentElement = element;
        }

        public int size() {
            return openElements.size();
        }

        public void setCurrentElementType(TokenType elementType) {
            currentElement.elementType = elementType;
        }

        public void popCurrentElement() {
            // remove currentElement from openElements and return it to the pool
            final int currentElementIndex = openElements.size() - 1;
            openElements.remove(currentElementIndex);
            openElementPool.add(currentElement);

            // establish the new current element
            if (currentElementIndex > 0) {
                currentElement = openElements.get(currentElementIndex - 1);
            } else {
                currentElement = null;
            }
        }
    }
}
