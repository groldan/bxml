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
package org.gvsig.bxml.stream.impl.workers;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.gvsig.bxml.stream.impl.NamesResolver;
import org.gvsig.bxml.stream.io.BxmlInputStream;
import org.gvsig.bxml.stream.io.CommentPositionHint;
import org.gvsig.bxml.stream.io.TokenType;
import org.gvsig.bxml.stream.io.TrailerToken;
import org.gvsig.bxml.stream.io.ValueType;

/**
 * Encapsulates the parsing state for shared access between the {@link EventTypeWorker}s in a
 * {@link DefaultBxmlStreamReader}.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public final class ParseState {

    private static class ElementInfo {
        public String name;

        public TokenType type;

        public long position;

        public String toString() {
            return type + "[" + name + ":" + position + "]";
        }
    }

    /**
     * The specific value type identifier for when currentEventType.isValue() == true. Since there
     * are less value EventTypes than low level ValueTypes (VALUE_INT maps to ValueInt, ValueShort
     * and ValueUShort), this field allows to choose which methods to call from the low level
     * {@link BxmlInputStream}
     */
    private ValueType currentValueType;

    /**
     * Holds the number of elements of a value token. Only valid when {@link #getEventType()} is one
     * of the {@code EventType.VALUE_XXX} events, and should be equal to {@code 1} except when the
     * current value type is an array of some primitive type.
     */
    private int valueLength;

    /**
     * Number of elements already read, as the aggregate count of {@link #notifyValueRead(int)}
     * notifications
     */
    private int readCount;

    // /**
    // */
    // final FastStack<ElementInfo> openElements = new FastStack<ElementInfo>(50);
    //
    // /**
    // * Pool of ElementInfo to avoid creating millions of short lived objects
    // */
    // final FastStack<ElementInfo> elementInfoPool = new FastStack<ElementInfo>(50);

    private ElementInfo currentElement;

    /**
     * Type of current token, content not yet parsed, and for which reader is positioned in order to
     * start parsing its content.
     */
    private TokenType currentTokenType;

    private StringTable stringtable = new StringTable();

    private TrailerToken trailer;

    private boolean parsingStarted;

    private String xmlVersion;

    private boolean standalone;

    private boolean standaloneIsSet;

    /**
     * Valid only during the parse of a comment token
     * 
     * @see #setCommentPositionHint(CommentPositionHint)
     */
    private CommentPositionHint commentPositionHint;

    private NamesResolver namesResolver;

    private long currentTokenPosition;

    public ParseState(final NamesResolver namesResolver) {
        this.namesResolver = namesResolver;
        // for (int i = 0; i < elementInfoPool.capacity; i++) {
        // elementInfoPool.push(new ElementInfo());
        // }
    }

    public NamesResolver getNamesResolver() {
        return namesResolver;
    }

    public StringTable getStringTable() {
        return stringtable;
    }

    /**
     * Tells this value event based worker that {@code elementCount} elements of its specific
     * element type have been read from the underlying {@link BxmlInputStream}.
     * <p>
     * This allows the BxmlStreamReader to directly read content for value events and let the worker
     * keep track of the shift between its start position and the actual position for when it needs
     * to skip content.
     * </p>
     * 
     * @param elementCount
     */
    public void notifyValueRead(final int elementCount) {
        readCount += elementCount;
        if (readCount > valueLength) {
            throw new IllegalArgumentException("Accumulated read count (" + readCount
                    + " is larger than the element count in this value event: " + valueLength);
        }
    }

    public int getValueElementsReadCount() {
        return readCount;
    }

    void setValueElementsReadCount(final int elementCount) {
        readCount = elementCount;
    }

    /**
     * Sets the value lenght.
     * <p>
     * That is, the count of a given element type (int, double, String, etc) followed by a value
     * token.
     * </p>
     * 
     * @param valueLength
     */
    void setValueLength(final int valueLength) {
        this.valueLength = valueLength;
    }

    public int getValueLength() {
        return valueLength;
    }

    /**
     * Returns the current element name.
     * 
     * @return
     * @see #pushElement(String, TokenType)
     * @see #popElement()
     */
    public String getCurrentElementName() {
        return currentElement.name;
    }

    /**
     * @see AttributesEndEventWorker#nextImpl(BxmlInputStream, ParseState)
     */
    public TokenType getCurrentElementType() {
        return currentElement.type;
    }

    public long getCurrentElementPosition() {
        return currentElement.position;
    }

    void setCurrentTokenType(TokenType currentTokenType) {
        this.currentTokenType = currentTokenType;
    }

    public TokenType getCurrentTokenType() {
        return currentTokenType;
    }

    public long getCurrentTokenPosition() {
        return currentTokenPosition;
    }

    public void setCurrentTokenPosition(final long tokenPositionInStream) {
        this.currentTokenPosition = tokenPositionInStream;
    }

    public void setTrailer(TrailerToken trailer) {
        this.trailer = trailer;
    }

    public TrailerToken getTrailer() {
        return trailer;
    }

    void setCurrentValueType(ValueType currentValueType) {
        this.currentValueType = currentValueType;
    }

    public ValueType getCurrentValueType() {
        return currentValueType;
    }

    void setParsingStarted(boolean parsingStarted) {
        this.parsingStarted = parsingStarted;
    }

    boolean isParsingStarted() {
        return parsingStarted;
    }

    void setXmlVersion(final String xmlVersion) {
        this.xmlVersion = xmlVersion;
    }

    public String getXmlVersion() {
        return xmlVersion;
    }

    void setStandalone(final boolean standalone) {
        this.standalone = standalone;
    }

    public boolean isStandalone() {
        return standalone;
    }

    void setStandaloneIsSet(final boolean standaloneIsSet) {
        this.standaloneIsSet = standaloneIsSet;
    }

    public boolean isStandaloneIsSet() {
        return standaloneIsSet;
    }

    private Stack<ElementInfo> elements = new Stack<ElementInfo>();

    /**
     * Adds an element to the stack of currently open elements and sets {@code elementName} as the
     * {@link #getCurrentElementName() current element} name.
     * 
     * @param currentElementName
     * @param currentTokenType
     * @param startElementTokenPosition
     */
    public void pushElement(final String currentElementName, final TokenType currentTokenType,
            final long startElementTokenPosition) {

        ElementInfo info = new ElementInfo();
        info.name = currentElementName;
        info.type = currentTokenType;
        info.position = startElementTokenPosition;
        this.currentElement = info;
        elements.push(info);
        // if (this.elementInfoPool.size > 0) {
        // this.currentElement = this.elementInfoPool.pop();
        // } else {
        // this.currentElement = new ElementInfo();
        // }
        //
        // ElementInfo info = this.currentElement;
        // info.name = currentElementName;
        // info.type = currentTokenType;
        // info.position = startElementTokenPosition;
        //
        // this.openElements.push(this.currentElement);

        // REVISIT: not sure if I need to set currentTokenType here at all
        this.currentTokenType = currentTokenType;
    }

    public void popElement() {
        this.currentElement = elements.pop();
        // elementInfoPool.push(this.currentElement);
        // this.currentElement = openElements.pop();
    }

    /**
     * When a comment token is found, sets the comment position hint. When a comment token is being
     * discarded, should be set to {@code null}
     * 
     * @param commentPostHint
     * @see CommentEventWorker
     */
    void setCommentPositionHint(CommentPositionHint commentPostHint) {
        this.commentPositionHint = commentPostHint;
    }

    public CommentPositionHint getCommentPositionHint() {
        return commentPositionHint;
    }

    static class Attribute {
        String name;

        String value;

        public Attribute(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    private List<Attribute> attributes = new ArrayList<Attribute>();

    private int smallNumvalue;

    public int getAttributeCount() {
        return attributes.size();
    }

    public String getAttributeName(int index) {
        return attributes.get(index).name;
    }

    public String getAttributeValue(int index) {
        return attributes.get(index).value;
    }

    ListIterator<Attribute> getPlainAttributes() {
        return this.attributes.listIterator();
    }

    /**
     * Clears the attribute list and lets the state ready to add new element attributes
     */
    void clearAttributes() {
        attributes.clear();
    }

    void addAttribute(final String stringTableName, final String coalescedValue) {
        attributes.add(new Attribute(stringTableName, coalescedValue));
    }

    void setSmallNumValue(int smallNumValue) {
        this.smallNumvalue = smallNumValue;
    }

    public int getSmallNumValue() {
        return smallNumvalue;
    }

    /**
     * A fast stack like class
     * 
     * @author Gabriel Roldan
     * 
     * @param <T>
     *            the type of stored object
     */
    @SuppressWarnings("unchecked")
    private static final class FastStack<T> {

        private T[] buff;

        private int capacity;

        private int size;

        public FastStack(final int initialSize) {
            this.capacity = initialSize;
            this.size = 0;
            this.buff = (T[]) new Object[initialSize];
        }

        public void push(T o) {
            if (size == capacity) {
                T[] tmp = (T[]) new Object[(int) Math.ceil(capacity * 1.5)];
                System.arraycopy(buff, 0, tmp, 0, capacity);
                capacity = tmp.length;
                buff = tmp;
            }
            buff[size] = o;
            size++;
        }

        public T pop() {
            size--;
            T ret = buff[size];
            buff[size] = null;
            return ret;
        }
    }

    /**
     * Intended to be called when random access is detected (ie,
     * DefaultBxmlStreamReader.setPosition) in order to clear the open elements stack and avoid OOM
     * errors. This means I'm now waiting for the nasty side effects.. yet I'm being able of parsing
     * gigabyte GML files with no problems, but I didn't foresee other use cases really. This method
     * was introduced as a band-aid when detected a heavy use of random access to parse only the
     * geometries in large GML files were causing the stack to grow abusively. For this same reason
     * I'm not pushing/poping context in the {@link NamesResolver} anymore (see
     * {@link StartElementTypeWorker} and {@link EndElementEventWorker}).
     */
    public void invalidate() {
        elements.clear();
    }

}
