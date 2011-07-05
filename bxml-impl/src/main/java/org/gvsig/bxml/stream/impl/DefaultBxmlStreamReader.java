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

import static org.gvsig.bxml.stream.io.TokenType.XmlDeclaration;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Set;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.gvsig.bxml.stream.impl.workers.CharContentValueConverter;
import org.gvsig.bxml.stream.impl.workers.EventTypeWorker;
import org.gvsig.bxml.stream.impl.workers.ParseState;
import org.gvsig.bxml.stream.impl.workers.StringTable;
import org.gvsig.bxml.stream.io.BxmlInputStream;
import org.gvsig.bxml.stream.io.Header;
import org.gvsig.bxml.stream.io.Header.Flags;
import org.gvsig.bxml.stream.io.StringTableIndexEntry;
import org.gvsig.bxml.stream.io.TokenType;
import org.gvsig.bxml.stream.io.TrailerToken;
import org.gvsig.bxml.stream.io.ValueType;

/**
 * TODO: describe.
 * <p>
 * Note all public methods that implement the {@link BxmlStreamReader} interface expect the contract
 * enforcement check to be done by the platform, so they do not contain explicit argument and state
 * consistency checks. Instead, assume the method preconditions have been met. They should ensure
 * though to establish the correct state any post condition should check. Again, the contract
 * enforcement provided by the platform will take care of checking the post conditions are met.
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
class DefaultBxmlStreamReader implements BxmlStreamReader {

    private final BxmlInputStream reader;

    private final Header header;

    private final ParseState parseState;

    /**
     * Handles resolving element and attribute names from the StringTable
     */
    private final NamesResolver namesResolver;

    private EventTypeWorker worker;

    /**
     * null == not yet set, true == the {@link BxmlInputStream} supports random access AND the
     * trailer token contains the string table index
     * 
     * @see #supportsRandomAccess()
     */
    private Boolean supportsRandomAccess;

    private TrailerToken trailer;

    /**
     * Creates a new, non namespace aware, DefaultBxmlStreamReader.
     * 
     * @param reader
     * @throws IOException
     */
    public DefaultBxmlStreamReader(final BxmlInputStream reader) throws IOException {
        this(reader, new NotNamespaceAwareNameResolver());
    }

    /**
     * Creates a new, DefaultBxmlStreamReader with the given nameResolver that determines whether
     * the parsing is namespace aware or not.
     * 
     * @param reader
     * @param namesResolver
     * @throws IOException
     */
    public DefaultBxmlStreamReader(final BxmlInputStream reader, final NamesResolver namesResolver)
            throws IOException {
        this.reader = reader;
        this.namesResolver = namesResolver;
        this.parseState = new ParseState(namesResolver);

        // // this.processingState = new ProcessingState();
        this.header = reader.getHeader();

        // read the XML declaration token, if present
        final EventTypeWorker parseInitWorker = EventTypeWorker.getWorker(XmlDeclaration);
        this.worker = parseInitWorker.init(reader, parseState);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getCharset()
     */
    public Charset getCharset() {
        return header.getCharactersEncoding();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#isLittleEndian()
     */
    public boolean isLittleEndian() {
        final Flags flags = header.getFlags();
        final ByteOrder endianess = flags.getEndianess();
        return ByteOrder.LITTLE_ENDIAN == endianess;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#isNamespaceAware()
     */
    public boolean isNamespaceAware() {
        return this.namesResolver.isNamespaceAware();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#isValidated()
     */
    public boolean isValidated() {
        final Flags flags = this.header.getFlags();
        return flags.isValidated();
    }

    /**
     * @return
     * @see org.gvsig.bxml.stream.BxmlStreamReader#isOpen()
     */
    public boolean isOpen() {
        return this.reader.isOpen();
    }

    /**
     * @see BxmlStreamReader#isStandalone()
     */
    public final boolean isStandalone() {
        return parseState.isStandalone();
    }

    /**
     * @see BxmlStreamReader#standAloneIsSet()
     */
    public final boolean standAloneIsSet() {
        return parseState.isStandaloneIsSet();
    }

    /**
     * @see BxmlStreamReader#getXmlVersion()
     */
    public final String getXmlVersion() {
        return parseState.getXmlVersion();
    }

    /**
     * @see BxmlStreamReader#close()
     */
    public void close() throws IOException {
        reader.close();
    }

    /**
     * @see BxmlStreamReader#getEventType()
     */
    public EventType getEventType() {
        return worker.getEventType(parseState);
    }

    /**
     * @see BxmlStreamReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        TokenType currentTokenType = parseState.getCurrentTokenType();
        return currentTokenType != TokenType.Trailer;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#next()
     */
    public EventType next() throws IOException {
        this.worker = worker.next(reader, parseState);
        return worker.getEventType(parseState);
    }

    /**
     * @see BxmlStreamReader#nextTag()
     */
    public EventType nextTag() throws IOException {
        EventType next;
        while (true) {
            next = next();
            if (next.isTag()) {
                break;
            }
        }
        return next;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#require(org.gvsig.bxml.stream.EventType,
     *      java.lang.String, java.lang.String)
     */
    public void require(final EventType type, final String namespaceUri, final String localName)
            throws IllegalStateException {
        if (type != null && type != getEventType()) {
            throw new IllegalStateException("expected current event to be " + type + " but is "
                    + getEventType());
        }
        QName elementName = getElementName();
        if (namespaceUri != null && !namespaceUri.equals(elementName.getNamespaceURI())) {
            throw new IllegalStateException("expected namespace URI to be " + namespaceUri
                    + " but is " + elementName.getNamespaceURI());
        }
        if (localName != null && !localName.equals(elementName.getLocalPart())) {
            throw new IllegalStateException("expected element's local name to be " + localName
                    + " but is " + elementName.getLocalPart());
        }
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getPrefixes()
     */
    public Set<String> getPrefixes() {
        return namesResolver.getPrefixes();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getPrefixes(java.lang.String)
     */
    public Set<String> getPrefixes(String uri) {
        return namesResolver.getPrefixes(uri);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getPrefix(java.lang.String)
     */
    public String getPrefix(final String namespaceURI) {
        return namesResolver.getPrefix(namespaceURI);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(final String prefix) {
        return namesResolver.getNamespace(prefix);
    }

    /**
     * @pre {getEventType() == START_ELEMENT}
     * @pos {$return >= 0}
     * @see BxmlStreamReader#getAttributeCount()
     */
    public int getAttributeCount() {
        return parseState.getAttributeCount();
    }

    /**
     * Returns the coalesced value for the attribute at index {@code index}
     * 
     * @pre {getEventType() == START_ELEMENT}
     * @param index
     * @see BxmlStreamReader#getAttributeValue(int)
     */
    public String getAttributeValue(int index) {
        return parseState.getAttributeValue(index);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getAttributeValue(java.lang.String,
     *      java.lang.String)
     */
    public String getAttributeValue(final String namespaceURI, final String localName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @pre {getEventType() == START_ELEMENT}
     * @pos {$return != null}
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getAttributeName(int)
     */
    public QName getAttributeName(final int index) {
        final String currentAttributeName = parseState.getAttributeName(index);
        return this.namesResolver.resolve(currentAttributeName, true);
    }

    /**
     * @see BxmlStreamReader#getElementName()
     */
    public QName getElementName() {
        final String currentElementName = parseState.getCurrentElementName();
        return this.namesResolver.resolve(currentElementName, false);
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getElementPosition()
     */
    public long getElementPosition() {
        return this.parseState.getCurrentElementPosition();
    }

    /**
     * Returns whether random access is supported.
     * <p>
     * This implementation supports random access if both the underlying
     * {@link BxmlInputStream#supportsRandomAccess() stream} supports random access AND the BXML
     * document's {@link TokenType#Trailer Trailer} token contains the {@code StringTableIndex}.
     * Otherwise, interrupting the sequential parsing through {@link #setPosition(long)} may lead to
     * non determined string table references.
     * </p>
     * 
     * @throws IOException
     * 
     * @see org.gvsig.bxml.stream.BxmlStreamReader#supportsRandomAccess()
     */
    public boolean supportsRandomAccess() throws IOException {
        if (this.supportsRandomAccess == null) {
            this.supportsRandomAccess = Boolean.FALSE;
            this.trailer = readTrailer();
            if (this.trailer != null) {
                Set<StringTableIndexEntry> stringTableIndex = trailer.getStringTableIndex();
                if (stringTableIndex == null) {
                    this.supportsRandomAccess = Boolean.TRUE;
                } else {
                    int tableIndexSize = stringTableIndex.size();
                    this.supportsRandomAccess = Boolean.valueOf(tableIndexSize > 0);
                }
            }
        }
        return this.supportsRandomAccess.booleanValue();
    }

    private TrailerToken readTrailer() throws IOException {
        if (!reader.supportsRandomAccess()) {
            return null;
        }
        final long currPosition = reader.getPosition();
        final long fileSize = reader.getSize();

        TrailerToken trailer = null;
        try {
            reader.setPosition(fileSize - 4);

            final int trailerSize = reader.readInt();

            final long trailerTokenPosition = fileSize - trailerSize;

            reader.setPosition(trailerTokenPosition);

            TokenType trailerToken = reader.readTokenType();
            if (TokenType.Trailer != trailerToken) {
                throw new IllegalStateException("Expected TrailerToken at position "
                        + trailerTokenPosition + " but found " + trailerToken);
            }

            trailer = reader.readTrailer();

            Set<StringTableIndexEntry> stringTableIndex = trailer.getStringTableIndex();
            if (stringTableIndex != null) {
                fillStringTable(stringTableIndex);
            }
        } finally {
            reader.setPosition(currPosition);
        }
        return trailer;
    }

    private void fillStringTable(Set<StringTableIndexEntry> stringTableIndex) throws IOException {

        final StringTable stringTable = getStringTable();

        long fileOffset;
        long nIndexStrings;
        long nStrings;
        TokenType tokenType;
        String string;

        for (StringTableIndexEntry entry : stringTableIndex) {

            nIndexStrings = entry.getStringCount();
            fileOffset = entry.getFileOffset();

            reader.setPosition(fileOffset);
            tokenType = reader.readTokenType();

            if (TokenType.StringTable != tokenType) {
                throw new IllegalStateException("Expected StringTable token at position "
                        + fileOffset + " but found " + tokenType);
            }

            nStrings = reader.readCount();
            if (nStrings != nIndexStrings) {
                throw new IllegalStateException(
                        "Number of strings defined in trailer index and at the actual "
                                + "StringTable entry do not match. File offset: " + fileOffset
                                + ". Strings declared in index: " + nIndexStrings
                                + ". Strings declared in StringTable token: " + nStrings);
            }

            for (int i = 0; i < nStrings; i++) {
                string = reader.readString();
                stringTable.add(string, fileOffset);
            }
        }
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#setPosition(long)
     */
    public EventType setPosition(final long position) throws IOException {
        if (this.trailer == null) {
            this.trailer = readTrailer();
        }

        // did the parsing start at all?
        if (XmlDeclaration == parseState.getCurrentTokenType()) {
            nextTag();
        }

        /*
         * Problem: setting a random position in the stream breaks the element stack. I.e. the
         * push/pop context calls on NamesResolver gets unbalancer. By now I can only think of
         * invalidating the context altogether, otherwise we may be quickly going OOM due to the
         * amount of open context/elements that get not closed
         */
        parseState.invalidate();

        this.reader.setPosition(position);
        return nextTag();
    }

    protected StringTable getStringTable() {
        return parseState.getStringTable();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getBooleanValue()
     */
    public boolean getBooleanValue() throws IOException {
        parseState.notifyValueRead(1);
        final boolean readBoolean = reader.readBoolean();
        return readBoolean;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getByteValue()
     */
    public int getByteValue() throws IOException, IllegalArgumentException {
        parseState.notifyValueRead(1);
        final int readByte = reader.readByte();
        return readByte;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getDoubleValue()
     */
    public double getDoubleValue() throws IOException {
        parseState.notifyValueRead(1);
        final double readDouble = reader.readDouble();
        return readDouble;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getFloatValue()
     */
    public float getFloatValue() throws IOException {
        parseState.notifyValueRead(1);
        final float readFloat = reader.readFloat();
        return readFloat;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getIntValue()
     */
    public int getIntValue() throws IOException {
        parseState.notifyValueRead(1);
        final ValueType currentValueType = parseState.getCurrentValueType();
        final int readInt;
        if (ValueType.SmallNum == currentValueType) {
            readInt = parseState.getSmallNumValue();
        } else if (ValueType.IntCode == currentValueType) {
            readInt = reader.readInt();
        } else if (ValueType.ShortCode == currentValueType) {
            readInt = reader.readShort();
        } else if (ValueType.UShortCode == currentValueType) {
            readInt = reader.readUShort();
        } else {
            throw new IllegalStateException(currentValueType + " does not map to a VALUE_INT event");
        }
        return readInt;
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getLongValue()
     */
    public long getLongValue() throws IOException {
        parseState.notifyValueRead(1);
        final long readLong = reader.readLong();
        return readLong;
    }

    /**
     * Reads and returns .... and marks the current element content as being read
     * 
     * @see BxmlStreamReader#getStringValue()
     */
    public String getStringValue() throws IOException {
        // for this to work the worker shall be able to return its value as a String
        if (!(this.worker instanceof CharContentValueConverter)) {
            throw new IllegalStateException(
                    "Current event handler cannot convert a value to String. " + " token: "
                            + parseState.getCurrentTokenType() + ", worker: "
                            + worker.getClass().getSimpleName() + ", event: "
                            + worker.getEventType(parseState));
        }
        CharContentValueConverter converter = (CharContentValueConverter) worker;
        String value = converter.getValueAsString(reader, parseState);
        return value;
    }

    /**
     * Delegates to the internal {@link BxmlInputStream#readBoolean(boolean[], int, int)} method
     * <p>
     * Relies on precondition check enforced by the platform
     * </p>
     * 
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(boolean[], int, int)
     */
    public void getValue(boolean[] dst, final int offset, final int length) throws IOException,
            IllegalArgumentException {
        parseState.notifyValueRead(length);
        reader.readBoolean(dst, offset, length);
    }

    /**
     * Delegates to the internal {@link BxmlInputStream#readByte(byte[], int, int)} method
     * <p>
     * Relies on precondition check enforced by the platform
     * </p>
     * 
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(byte[], int, int)
     */
    public void getValue(byte[] dst, final int offset, final int length) throws IOException {
        parseState.notifyValueRead(length);
        reader.readByte(dst, offset, length);
    }

    /**
     * Delegates to the internal BxmlInputStream {@link BxmlInputStream#readInt(int[], int, int)
     * readInt}, {@link BxmlInputStream#readShort(int[], int, int) readShort},
     * {@link BxmlInputStream#readUShort() readUShort} method as appropriate for the current token
     * type.
     * <p>
     * Relies on precondition check enforced by the platform
     * </p>
     * 
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(int[], int, int)
     */
    public void getValue(int[] dst, final int offset, final int length) throws IOException {
        final ValueType valueType = parseState.getCurrentValueType();
        if (ValueType.UShortCode == valueType) {
            parseState.notifyValueRead(length);
            reader.readUShort(dst, offset, length);
        } else if (ValueType.ShortCode == valueType) {
            parseState.notifyValueRead(length);
            reader.readShort(dst, offset, length);
        } else if (ValueType.IntCode == valueType) {
            parseState.notifyValueRead(length);
            reader.readInt(dst, offset, length);
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Delegates to the internal {@link BxmlInputStream#readLong(long[], int, int)} method
     * <p>
     * Relies on precondition check enforced by the platform
     * </p>
     * 
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(long[], int, int)
     */
    public void getValue(long[] dst, final int offset, final int length) throws IOException {
        parseState.notifyValueRead(length);
        reader.readLong(dst, offset, length);
    }

    /**
     * Delegates to the internal {@link BxmlInputStream#readFloat(float[], int, int)} method
     * <p>
     * Relies on precondition check enforced by the platform
     * </p>
     * 
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(float[], int, int)
     */
    public void getValue(float[] dst, final int offset, final int length) throws IOException {
        parseState.notifyValueRead(length);
        reader.readFloat(dst, offset, length);
    }

    /**
     * Delegates to the internal {@link BxmlInputStream#readDouble(double[], int, int)} method
     * <p>
     * Relies on precondition check enforced by the platform
     * </p>
     * 
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(double[], int, int)
     */
    public void getValue(double[] dst, final int offset, final int length) throws IOException {
        parseState.notifyValueRead(length);
        reader.readDouble(dst, offset, length);
    }

    /**
     * Delegates to the internal {@link BxmlInputStream#read} method
     * <p>
     * Relies on precondition check enforced by the platform
     * </p>
     * 
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValue(char[], int, int)
     */
    // public void getValue(char[] dst, final int offset, final int length) throws IOException {
    // }
    /**
     * @see BxmlStreamReader#getValueCount()
     */
    public int getValueCount() throws IllegalStateException {
        return parseState.getValueLength();
    }

    /**
     * @see org.gvsig.bxml.stream.BxmlStreamReader#getValueReadCount()
     */
    public int getValueReadCount() {
        return parseState.getValueElementsReadCount();
    }
}
