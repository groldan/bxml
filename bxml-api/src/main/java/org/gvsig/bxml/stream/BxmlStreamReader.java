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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * The BxmlStreamReader interface defines forward, read-only API to parse XML data structures
 * encoded as per the OGC Binary XML specification, in a "pull" fashion.
 * <p>
 * BxmlStreamReader is designed to iterate over BXML using <code>next()</code> and
 * <code>hasNext()</code>. This interface defines a cursor like API to iterate over XML structured
 * documents encoded as per the OGC Binary XML implementation specification. The API is inspired by
 * the <i>StAX</i> XML pull API and provides for querying element and attribute contents stored as
 * primitive or arrays of primitive data types.
 * </p>
 * <p>
 * <h2>Query methods</h2>
 * Some document metadata derived from the BXML header token and the XML declaration token, as well
 * as some state control information may be safely queried at any time. For instance, the following
 * methods return such information:
 * <ul>
 * <li>{@link #getCharset()} which character set the string content is encoded with
 * <li>{@link #getXmlVersion()} the xml version declared in the document's xml declaration token
 * <li>{@link #isStandalone()} whether the document is standalone as per the xml specification
 * <li>{@link #isLittleEndian()} whether the binary contents are little endian
 * <li>{@link #isNamespaceAware()} whether the reader handles namespaces
 * <li>{@link #getEventType()} which is the current parsing event
 * <li>{@link #isOpen()} whether this reader is open and this able to operate
 * <li>{@link #isValidated()} whether the bxml content is marked as already validated
 * <li>{@link #supportsRandomAccess()} does the stream support random access?
 * </ul>
 * </p>
 * <p>
 * <h2>Data access</h2>
 * The data being parsed can be accessed using methods such as <code>next()</code>,
 * <code>nextTag()</code>, <code>getElementName()</code>, <code>getValueCount()</code> and the
 * various <code>getValue()</code> methods.
 * </p>
 * <p>
 * The {@link #next()} method causes the reader to read the next parse event.
 * </p>
 * <p>
 * The next() method returns an integer which identifies the type of event just read. The event type
 * can be determined using getEventType()
 * </p>
 * <p>
 * <b>How to get the content of an element</b>: The process of getting at the value of elements is
 * two-fold. First, the last call to {@code next()} and thus the current {@link #getEventType()
 * event type} shall returned one of {@link EventType#VALUE_BOOL VALUE_BOOL},
 * {@link EventType#VALUE_BYTE VALUE_BYTE}, {@link EventType#VALUE_INT VALUE_INT},
 * {@link EventType#VALUE_LONG VALUE_LONG}, {@link EventType#VALUE_FLOAT VALUE_FLOAT},
 * {@link EventType#VALUE_DOUBLE VALUE_DOUBLE}, {@link EventType#VALUE_STRING VALUE_STRING}. Or,
 * what is the same, {@code getEventType().isValue == true}.
 * </p>
 * <p>
 * Then, the count of elements of that kind needs to be requested through {@link #getValueCount()}.
 * With that information at hand, the client code may use the appropriate {@code getValue(type[],
 * int, int)}, or <b>a String representation</b> of the value may be asked directly through
 * {@link #getStringValue()}. By using the {@code getValue()} methods client code can be under
 * control of memory resources. It is allowed to make multiple calls to a {@code getValue()} method
 * as long as the total requested length between the multiple calls does not exceed the value count
 * returned by {@code getValueCount()}. Also, it is allowed to call {@code next()} before having
 * consumed all the values in a value array. The reader shall just skip to the next token. The same
 * applies to the {@link #nextTag()} method, which can be called at any time as long as the end of
 * document has not been reached, to advance to the next start element or end element event,
 * whatever is found first.
 * </p>
 * <p>
 * Note the BXML specification only supports arrays of primitive types.
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 * @since 1.0
 */
public interface BxmlStreamReader {

    /**
     * Derived property from the BXML file header, indicates whether the BXML document is marked as
     * standalone, as per the XML standalone definition.
     * 
     * @return {@code true} if the document is standalone, {@code false} otherwise
     */
    public boolean isStandalone();

    /**
     * Derived property from the BXML file header, indicates whether the BXML document standalone
     * mark is set at all.
     * 
     * @return {@code true} if the document standalone mark is explicitly set, {@code false}
     *         otherwise
     */
    public boolean standAloneIsSet();

    /**
     * Returns whether the multi-byte primitive values (doubles, longs, etc) in the document are
     * encoded in little endian or big endian byte order.
     * 
     * @return {@code true} if the document is little endian, {@code false} if its big endian.
     */
    public boolean isLittleEndian();

    /**
     * Returns whether this reader is namespace aware or not.
     * 
     * @return {@code true} if this reader handles namespaces, {@code false otherwise}
     */
    public boolean isNamespaceAware();

    /**
     * Returns a flag set in the bxml document header that indicates if the document structure and
     * contents is already validated, with the aim of easying the task of parsers.
     * 
     * @return {@code true} is the document is marked to be validated, {@code false} otherwise
     */
    public boolean isValidated();

    /**
     * Get the xml version declared on the xml declaration token, or {@code null} if none was
     * declared.
     * 
     * @return the xml version or {@code null} if not declared.
     */
    public String getXmlVersion();

    /**
     * Derived property from the BXML file header, identifies the character-set encoding that is
     * used for all of the Strings in the file.
     * 
     * @post {$return != null}
     * @return the charset used by the document
     */
    public Charset getCharset();

    /**
     * Frees any resource associated with this reader, including closing the underlying input stream
     * this reader fetches data from.
     * <p>
     * The first call to this method closes and releases resources. Subsequent calls shall take no
     * effect and return quitely. After the first call to this method any non query method (defined
     * as metadata retrieval methods in the class' javadoc) shall fail with an io exception.
     * </p>
     * 
     * @throws IOException
     *             if an I/O error occurs closing this reader or the underlying input stream
     */
    public void close() throws IOException;

    /**
     * Returns whether this stream reader and its underlying input source are open and able to
     * receive more parsing calls.
     * <p>
     * A {@code BxmlStreamReader} should be open since its creation until {@link #close()} was
     * explicitly called by client code, regardless of whether there are or not more parsing events
     * to process.
     * </p>
     * 
     * @return {@code true} if this stream is still open, {@code false} otherwise
     */
    public boolean isOpen();

    /**
     * Returns whether there are more parsing events to be processed.
     * 
     * @pre {isOpen() == true}
     * @post {$return == (getEventType() == END_DOCUMENT? false : true)}
     * @return {@code true} if there are more parsing events and {@code false} if there are no more
     *         events (i.e. the trailer token has been reached, which is the same than the current
     *         event being END_DOCUMENT).
     * @throws IOException
     *             if an I/O error occurs checking whether there are more parsing events to process
     */
    public boolean hasNext() throws IOException;

    /**
     * Advances the cursor to the next BXML token that has an {@link EventType} mapping and returns
     * the corresponding event type.
     * <p>
     * The first call to {@code next()} shall always return {@link EventType#START_DOCUMENT}.
     * </p>
     * 
     * @pre {hasNext() == true}
     * @post {$return != null}
     * @post {$return == getEventType()}
     * @return the event corresponding to the parsing event the cursor advanced to
     * @throws IOException
     *             if an I/O error occurs checking whether there are more parsing events to process
     * @see #getEventType()
     */
    public EventType next() throws IOException;

    /**
     * Advances the cursor skipping any content until the first start or end element token is found.
     * 
     * @pre {hasNext() == true}
     * @post {$return != null}
     * @post {$return == getEventType()}
     * @post {getEventType().isTag() == true}
     * @return the event type corresponding to the first START_ELEMENT or END_ELEMENT parse event
     *         found
     * @throws IOException
     *             if an I/O exception is found while reading through the contents looking for a tag
     *             token
     */
    public EventType nextTag() throws IOException;

    /**
     * Returns the event type for the current position of the BXML reader cursor.
     * <p>
     * When a {@code BxmlStreamReader} is first created, the current event type is
     * {@link EventType#NONE NONE}. It is after the first call to {@code next()} when
     * {@code getEventType()} will return {@link EventType#START_DOCUMENT START_DOCUMENT}.
     * </p>
     * 
     * @return the {@link EventType} returned by the last {@link #next()} call, or
     *         {@link EventType#NONE} if the document did not started being parssed yet.
     */
    public EventType getEventType();

    /**
     * Test if the current event is of the given {@code type} and if the {@code namespaceUri} and
     * {@code localName} match the current namespace and name of the current event.
     * <p>
     * If either of the three arguments is {@code null} it is not tested.
     * </p>
     * <p>
     * If either {@code namespaceUri} or {@code localName} is <strong>not</strong> {@code null} the
     * event {@code type} shall be either {@link EventType#START_ELEMENT} or
     * {@link EventType#END_ELEMENT}, as {@link #getElementName()} is only valid on that context.
     * </p>
     * <p>
     * If a test does not pass, an unchecked Exception is thrown to indicate there's a programming
     * error on the client code.
     * </p>
     * <p>
     * This method is a convenience utility for client code to make sure the parser is in a given
     * state before proceeding.
     * </p>
     * 
     * @pre {if( namespaceUri != null || localName != null ) getEventType().isTag() == true}
     * @param type
     *            {@link EventType} to test it's the {@link #getEventType() current parsing event},
     *            or {@code null} if not to be tested
     * @param namespaceUri
     *            the namespace URI to test againt the current start or end element event, or
     *            {@code null} if not to be tested
     * @param localName
     *            the current element's local name at a start or end element event, or {@code null}
     *            if not to be tested
     * @throws IllegalStateException
     */
    public void require(EventType type, String namespaceUri, String localName)
            throws IllegalStateException;

    /**
     * Get all prefixes bound to a Namespace URI in the current scope.
     */
    public Set<String> getPrefixes();

    /**
     * Get all prefixes bound to the given Namespace URI in the current scope.
     */
    public Set<String> getPrefixes(String uri);

    /**
     * <p>
     * Get prefix bound to Namespace URI in the current scope.
     * </p>
     * 
     * <p>
     * To get all prefixes bound to a Namespace URI in the current scope, use
     * {@link #getPrefixes(String namespaceURI)}.
     * </p>
     * 
     * <p>
     * When requesting a prefix by Namespace URI, the following table describes the returned prefix
     * value for all Namespace URI values:
     * </p>
     * 
     * <table border="2" rules="all" cellpadding="4">
     * <thead>
     * <tr>
     * <th align="center" colspan="2">
     * <code>getPrefix(namespaceURI)</code> return value for specified Namespace URIs</th>
     * </tr>
     * <tr>
     * <th>Namespace URI parameter</th>
     * <th>prefix value returned</th>
     * </tr>
     * </thead> <tbody>
     * <tr>
     * <td>&lt;default Namespace URI&gt;</td>
     * <td><code>XMLConstants.DEFAULT_NS_PREFIX</code> ("")</td>
     * </tr>
     * <tr>
     * <td>bound Namespace URI</td>
     * <td>prefix bound to Namespace URI in the current scope, if multiple prefixes are bound to the
     * Namespace URI in the current scope, a single arbitrary prefix, whose choice is implementation
     * dependent, is returned</td>
     * </tr>
     * <tr>
     * <td>unbound Namespace URI</td>
     * <td><code>null</code></td>
     * </tr>
     * <tr>
     * <td><code>XMLConstants.XML_NS_URI</code> ("http://www.w3.org/XML/1998/namespace")</td>
     * <td><code>XMLConstants.XML_NS_PREFIX</code> ("xml")</td>
     * </tr>
     * <tr>
     * <td><code>XMLConstants.XMLNS_ATTRIBUTE_NS_URI</code> ("http://www.w3.org/2000/xmlns/")</td>
     * <td><code>XMLConstants.XMLNS_ATTRIBUTE</code> ("xmlns")</td>
     * </tr>
     * <tr>
     * <td><code>null</code></td>
     * <td><code>IllegalArgumentException</code> is thrown</td>
     * </tr>
     * </tbody>
     * </table>
     * 
     * @pre {namespaceURI != null}
     * 
     * @param namespaceURI
     *            URI of Namespace to lookup
     * 
     * @return prefix bound to Namespace URI in current context
     * 
     */
    public String getPrefix(String namespaceURI);

    /**
     * Get Namespace URI bound to a prefix in the current scope.
     * 
     * <p>
     * When requesting a Namespace URI by prefix, the following table describes the returned
     * Namespace URI value for all possible prefix values:
     * </p>
     * 
     * <table border="2" rules="all" cellpadding="4">
     * <thead>
     * <tr>
     * <td align="center" colspan="2">
     * <code>getNamespaceURI(prefix)</code> return value for specified prefixes</td>
     * </tr>
     * <tr>
     * <td>prefix parameter</td>
     * <td>Namespace URI return value</td>
     * </tr>
     * </thead> <tbody>
     * <tr>
     * <td><code>DEFAULT_NS_PREFIX</code> ("")</td>
     * <td>default Namespace URI in the current scope or <code>{@link
     *         javax.xml.XMLConstants#NULL_NS_URI XMLConstants.NULL_NS_URI("")}
     *         </code> when there is no default Namespace URI in the current scope</td>
     * </tr>
     * <tr>
     * <td>bound prefix</td>
     * <td>Namespace URI bound to prefix in current scope</td>
     * </tr>
     * <tr>
     * <td>unbound prefix</td>
     * <td>
     * <code>{@link
     *         javax.xml.XMLConstants#NULL_NS_URI XMLConstants.NULL_NS_URI("")}
     *         </code></td>
     * </tr>
     * <tr>
     * <td><code>XMLConstants.XML_NS_PREFIX</code> ("xml")</td>
     * <td><code>XMLConstants.XML_NS_URI</code> ("http://www.w3.org/XML/1998/namespace")</td>
     * </tr>
     * <tr>
     * <td><code>XMLConstants.XMLNS_ATTRIBUTE</code> ("xmlns")</td>
     * <td><code>XMLConstants.XMLNS_ATTRIBUTE_NS_URI</code> ("http://www.w3.org/2000/xmlns/")</td>
     * </tr>
     * <tr>
     * <td><code>null</code></td>
     * <td><code>IllegalArgumentException</code> is thrown</td>
     * </tr>
     * </tbody>
     * </table>
     * 
     * @pre {prefix != null}
     * 
     * @param prefix
     *            prefix to look up
     * 
     * @return Namespace URI bound to prefix in the current scope
     * 
     */
    public String getNamespaceURI(String prefix);

    /**
     * Returns the qualified name of the current element, being at a START_ELEMENT or END_ELEMENT
     * event
     * <p>
     * If this bxml reader is {@link #isNamespaceAware() namespace aware}, the returned name
     * contains both the attribute namespace uri, as well as the local name. The prefix shall be
     * included only if its present in the element name as it is in the file being parsed, to better
     * reflect the actual file structure.
     * </p>
     * <p>
     * If this bxml reader is <b>not</b> namespace aware, the namespace and prefix in the returned
     * name are the {@link XMLConstants#NULL_NS_URI null namespace URI} and
     * {@link XMLConstants#DEFAULT_NS_PREFIX default namespace prefix}, as defined in the XML 1.0
     * spec, and the {@code localName} field contains the element name as it is in the file being
     * parsed.
     * </p>
     * 
     * @pre {getEventType().isTag() == true}
     * @post {$return != null}
     * @return the name of the current element
     */
    public QName getElementName();

    /**
     * @pre {supportsRandomAccess() == true}
     * @pre {getEventType() == START_ELEMENT}
     * @return the position of the current start element event in the stream
     */
    public long getElementPosition() throws IOException;

    /**
     * Indicates whether this parser instance supports random access to allow positioning the parser
     * at the start of an xml element
     * 
     * @return {@code true} if {@link #getElementPosition()} and {@link #setPosition(long)} are
     *         supported, {@code false} otherwise
     * @throws IOException
     */
    public boolean supportsRandomAccess() throws IOException;

    /**
     * Allows to set the parser position the the position of a start element tag.
     * 
     * @pre {supportsRandomAccess() == true}
     * @post {getEventType() == START_ELEMENT}
     * @param position
     * @return the START_ELEMENT event for the position set
     * @throws IOException
     */
    public EventType setPosition(long position) throws IOException;

    /**
     * Returns the count of attributes on this {@code START_ELEMENT}
     * <p>
     * This count excludes namespace definitions <b>if</b> this parser is
     * {@link #isNamespaceAware() namespace aware}. If the parser does not handle namespaces,
     * namespace definitions are returned as normal attributes.
     * </p>
     * <p>
     * Attribute indices are zero-based.
     * </p>
     * 
     * @pre {getEventType() == START_ELEMENT}
     * @post {$return >= 0}
     * @return the number of attributes for the current start element tag or zero if there are no
     *         declared attributes.
     */
    public int getAttributeCount();

    /**
     * Returns the qualified name of the attribute at the provided {@code index}, for the current
     * {@code START_ELEMENT} event
     * <p>
     * If this bxml reader is {@link #isNamespaceAware() namespace aware}, the returned name
     * contains both the attribute namespace uri, as well as the local name. The prefix shall be
     * included only if its present in the attribute name as it is in the file being parsed, to
     * better reflect the actual file format.
     * </p>
     * <p>
     * If this bxml reader is <b>not</b> namespace aware, the namespace and prefix in the returned
     * name are the {@link XMLConstants#NULL_NS_URI null namespace URI} and
     * {@link XMLConstants#DEFAULT_NS_PREFIX default namespace prefix}, as defined in the XML 1.0
     * spec, and the {@code localName} field contains the element name as it is in the file being
     * parsed.
     * </p>
     * <p>
     * The attribute order reported by the {@code BxmlStreamReader} implementation shall be self
     * consistent between {@code getAttributeName} and {@code getAttributeValue}, though it might
     * not match the order in the bxml file being parsed.
     * </p>
     * 
     * @pre {getEventType() == START_ELEMENT}
     * @pre {index >= 0}
     * @pre {index < getAttributeCount()}
     * @post {$return != null}
     * @param index
     *            zero based index for which to return the qualified name
     * @return the name of the attribute at index {@code index}
     * @see #getAttributeCount()
     */
    public QName getAttributeName(int index);

    /**
     * Returns the coalesced value for the attribute at index {@code index}
     * <p>
     * NOTE in the binary xml file being parsed, the {@code AttributeStart} token from which an
     * attribute is parsed, may be followed by one or more value tokens. In case it is not followed
     * by any value token, the returned value for the attribute will be the empty string, not
     * {@code null}. In case it is followed by more than one value token, the returned attribute
     * value will be the concatenated string value from all the value tokens composing the attribute
     * value. The rules to transform a non string value to String are the same than for the
     * {@link #getStringValue()} method.
     * </p>
     * 
     * @pre {getEventType() == START_ELEMENT}
     * @pre {index >= 0}
     * @pre {index < getAttributeCount()}
     * @post {$return != null}
     * @param index
     * @return the coalesced String representation of the attribute value, or the empty string if
     *         the attribute has no value.
     */
    public String getAttributeValue(int index);

    /**
     * Returns the normalized attribute value of the attribute with the namespace and localName If
     * the namespaceURI is null the namespace is not checked for equality
     * 
     * @pre {getEventType() == START_ELEMENT}
     * @pre {localName != null}
     * @param namespaceURI
     *            the namespace of the attribute, if {@code null} the namespace will not be checked
     *            for equality
     * @param localName
     *            the local name of the attribute, cannot be null
     * @return returns the value of the attribute , or {@code null} if not found
     */
    public String getAttributeValue(String namespaceURI, String localName);

    /**
     * Returns the number of elements of the given value type that are directly available when a
     * value token was found.
     * <p>
     * If the current value type refers to a StringValue, {@code getValueCount} will return always
     * {@code 1}, since the spec does not define the array of strings as a value element. Otherwise,
     * if the current value type is of a single primitive element, returns {@code 1}, and if it is
     * of an array of some primitive value type, returns the array length, which might be any
     * integer including zero.
     * </p>
     * 
     * @pre {getEventType().isValue() == true}
     * @post {$return >= 0}
     * @return {@code 1} in case {@code getEventType() == VALUE_STRING}, as there are no arrays of
     *         Strings, otherwise the number of elements of the primitive data type corresponding to
     *         the current {@code VALUE_*} event type that are stored in the document for that value
     *         token.
     * @see #getValueReadCount()
     */
    public int getValueCount();

    /**
     * Returns the number of elements of the current value event type that was already read.
     * <p>
     * This is useful to control the required value read count does not exceed the actual value
     * element count.
     * </p>
     * 
     * @pre {getEventType().isValue() == true}
     * @post {$return <= getValueCount()}
     * @return the number of elements already read for the current value event
     * @see #getValueCount()
     */
    public int getValueReadCount();

    /**
     * Returns the String representation of the current value or comment token.
     * <p>
     * When the current value token represents an array of some primitive value, the content is
     * translated to String using the standard xml list type encoding. For example, the
     * <code>double[]{1.0, 1.1, 2.2}</code> array will be converted to the {@code "1.0 1.1 2.2"}
     * String before returning it.
     * </p>
     * <p>
     * NOTE calling this method implies parsing all the remaining elements for the current value
     * event, so it is not allowed to call other {@code getValue} or {@code getXXXValue} method
     * after this method returns, since the whole value event was consumed.
     * </p>
     * 
     * @pre {getEventType().isValue() == true || getEventType() == {@link EventType#COMMENT}
     * @post {$return != null}
     * @return the current value object, whether it is a single element or an array of some
     *         primitive types, as a String.
     * @throws IOException
     *             if an I/O error occurs while reading the data
     */
    public String getStringValue() throws IOException;

    /**
     * Being at a {@link EventType#VALUE_BOOL boolean value} event, reads {@code length} booleans
     * out of the underlying input stream and stores them in {@code dst}, starting at array index
     * {@code offset}.
     * <p>
     * This operation is semantically equivalent to calling {@link #getBooleanValue()}
     * {@code length} times, though its recommended for bulk reads as the implementation may perform
     * better.
     * </p>
     * 
     * @pre {getEventType() == VALUE_BOOL}
     * @pre {dst != null}
     * @pre {offset >= 0}
     * @pre {length <= (dst.length - offset)}
     * @pre {getValueCount() - getValueReadCount() >= length}
     * @param dst
     *            the client supplied buffer where to store the read content
     * @param offset
     *            the zero based index at which to start storing the read content in {@code dst}
     * @param length
     *            the amount of elements to store in {@code dst}
     * @throws IOException
     *             if an I/O error occurs while reading the data
     * @see #getValueCount()
     */
    public void getValue(boolean[] dst, int offset, int length) throws IOException;

    /**
     * Reads a single boolean value out of the underlying input stream.
     * <p>
     * Calls to this method may be interleaved with calls to {@link #getValue(boolean[], int, int)}
     * for the same VALUE_BOOL event, as long as the accumulated number of elements read does not
     * exceed the current {@link #getValueCount() value count}.
     * </p>
     * 
     * @pre {getEventType() == VALUE_BOOL}
     * @pre {getValueReadCount() < getValueCount()}
     * @return the value read
     * @throws IOException
     *             if an I/O error occurs while reading the data
     * @see #getValueCount()
     * @see #getValueReadCount()
     * @see #getValue(boolean[], int, int)
     */
    public boolean getBooleanValue() throws IOException;

    /**
     * Being at a {@link EventType#VALUE_BYTE byte value} event, reads {@code length} bytes out of
     * the underlying input stream and stores them in {@code dst}, starting at array index
     * {@code offset}.
     * <p>
     * This operation is semantically equivalent to calling {@link #getByteValue()} {@code length}
     * times, though its recommended for bulk reads as the implementation may perform better.
     * </p>
     * 
     * @pre {getEventType() == VALUE_BYTE}
     * @pre {dst != null}
     * @pre {offset >= 0}
     * @pre {length <= (dst.length - offset)}
     * @pre {getValueCount() - getValueReadCount() >= length}
     * @param dst
     *            the client supplied buffer where to store the read content
     * @param offset
     *            the zero based index at which to start storing the read content in {@code dst}
     * @param length
     *            the amount of elements to store in {@code dst}
     * @throws IOException
     *             if an I/O error occurs while reading the data
     * @throws IllegalArgumentException
     *             if the accumulated read length for the current value event exceeds the actual
     *             value length
     * @see #getValueCount()
     * @see #getValueReadCount()
     * @see #getBooleanValue()
     */
    public void getValue(byte[] dst, int offset, int length) throws IOException;

    /**
     * Reads a single byte value out of the underlying input stream.
     * <p>
     * Calls to this method may be interleaved with calls to {@link #getValue(byte[], int, int)} for
     * the same VALUE_BYTE event, as long as the accumulated number of elements read does not exceed
     * the current {@link #getValueCount() value count}.
     * </p>
     * 
     * @pre {getEventType() == VALUE_BYTE}
     * @pre {getValueReadCount() < getValueCount()}
     * @return the value read
     * @throws IOException
     *             if an I/O error occurs while reading the data
     * @see #getValueCount()
     * @see #getValueReadCount()
     * @see #getValue(byte[], int, int)
     */
    public int getByteValue() throws IOException;

    /**
     * Being at a {@link EventType#VALUE_INT int value} event, reads {@code length} integers out of
     * the underlying input stream and stores them in {@code dst}, starting at array index
     * {@code offset}.
     * <p>
     * This operation is semantically equivalent to calling {@link #getIntValue()} {@code length}
     * times, though its recommended for bulk reads as the implementation may perform better.
     * </p>
     * 
     * @pre {getEventType() == VALUE_INT}
     * @pre {dst != null}
     * @pre {offset >= 0}
     * @pre {length <= (dst.length - offset)}
     * @pre {getValueCount() - getValueReadCount() >= length}
     * @param dst
     *            the client supplied buffer where to store the read content
     * @param offset
     *            the zero based index at which to start storing the read content in {@code dst}
     * @param length
     *            the amount of elements to store in {@code dst}
     * @throws IOException
     *             if an I/O error occurs while reading the data
     * @throws IllegalArgumentException
     *             if the accumulated read length for the current value event exceeds the actual
     *             value length
     * @see #getValueCount()
     * @see #getValueReadCount()
     * @see #getIntValue()
     */
    public void getValue(int[] dst, int offset, int length) throws IOException;

    /**
     * Reads a single integer value out of the underlying input stream.
     * <p>
     * Calls to this method may be interleaved with calls to {@link #getValue(int[], int, int)} for
     * the same VALUE_INT event, as long as the accumulated number of elements read does not exceed
     * the current {@link #getValueCount() value count}.
     * </p>
     * 
     * @pre {getEventType() == VALUE_INT}
     * @pre {getValueReadCount() < getValueCount()}
     * @return the value read
     * @throws IOException
     *             if an I/O error occurs while reading the data
     * @see #getValueCount()
     * @see #getValueReadCount()
     * @see #getValue(int[], int, int)
     */
    public int getIntValue() throws IOException;

    /**
     * Being at a {@link EventType#VALUE_LONG long value} event, reads {@code length} longs out of
     * the underlying input stream and stores them in {@code dst}, starting at array index
     * {@code offset}.
     * <p>
     * This operation is semantically equivalent to calling {@link #getLongValue()} {@code length}
     * times, though its recommended for bulk reads as the implementation may perform better.
     * </p>
     * 
     * @pre {getEventType() == VALUE_LONG}
     * @pre {dst != null}
     * @pre {offset >= 0}
     * @pre {length <= (dst.length - offset)}
     * @pre {getValueCount() - getValueReadCount() >= length}
     * @param dst
     *            the client supplied buffer where to store the read content
     * @param offset
     *            the zero based index at which to start storing the read content in {@code dst}
     * @param length
     *            the amount of elements to store in {@code dst}
     * @throws IOException
     *             if an I/O error occurs while reading the data
     * @throws IllegalArgumentException
     *             if the accumulated read length for the current value event exceeds the actual
     *             value length
     * @see #getValueCount()
     * @see #getValueReadCount()
     * @see #getLongValue()
     */
    public void getValue(long[] dst, int offset, int length) throws IOException;

    /**
     * Reads a single long value out of the underlying input stream.
     * <p>
     * Calls to this method may be interleaved with calls to {@link #getValue(long[], int, int)} for
     * the same VALUE_LONG event, as long as the accumulated number of elements read does not exceed
     * the current {@link #getValueCount() value count}.
     * </p>
     * 
     * @pre {getEventType() == VALUE_LONG}
     * @pre {getValueReadCount() < getValueCount()}
     * @return the value read
     * @throws IOException
     *             if an I/O error occurs while reading the data
     * @see #getValueCount()
     * @see #getValueReadCount()
     * @see #getValue(long[], int, int)
     */
    public long getLongValue() throws IOException;

    /**
     * Being at a {@link EventType#VALUE_FLOAT float value} event, reads {@code length} floats out
     * of the underlying input stream and stores them in {@code dst}, starting at array index
     * {@code offset}.
     * <p>
     * This operation is semantically equivalent to calling {@link #getFloatValue()} {@code length}
     * times, though its recommended for bulk reads as the implementation may perform better.
     * </p>
     * 
     * @pre {getEventType() == VALUE_FLOAT}
     * @pre {dst != null}
     * @pre {offset >= 0}
     * @pre {length <= (dst.length - offset)}
     * @pre {getValueCount() - getValueReadCount() >= length}
     * @param dst
     *            the client supplied buffer where to store the read content
     * @param offset
     *            the zero based index at which to start storing the read content in {@code dst}
     * @param length
     *            the amount of elements to store in {@code dst}
     * @throws IOException
     *             if an I/O error occurs while reading the data
     * @throws IllegalArgumentException
     *             if the accumulated read length for the current value event exceeds the actual
     *             value length
     * @see #getValueCount()
     * @see #getValueReadCount()
     * @see #getFloatValue()
     */
    public void getValue(float[] dst, int offset, int length) throws IOException;

    /**
     * Reads a single float value out of the underlying input stream.
     * <p>
     * Calls to this method may be interleaved with calls to {@link #getValue(float[], int, int)}
     * for the same VALUE_FLOAT event, as long as the accumulated number of elements read does not
     * exceed the current {@link #getValueCount() value count}.
     * </p>
     * 
     * @pre {getEventType() == VALUE_FLOAT}
     * @pre {getValueReadCount() < getValueCount()}
     * @return the value read
     * @throws IOException
     *             if an I/O error occurs while reading the data
     * @see #getValueCount()
     * @see #getValueReadCount()
     * @see #getValue(float[], int, int)
     */
    public float getFloatValue() throws IOException;

    /**
     * Being at a {@link EventType#VALUE_DOUBLE double value} event, reads {@code length} doubles
     * out of the underlying input stream and stores them in {@code dst}, starting at array index
     * {@code offset}.
     * <p>
     * This operation is semantically equivalent to calling {@link #getDoubleValue()} {@code length}
     * times, though its recommended for bulk reads as the implementation may perform better.
     * </p>
     * 
     * @pre {getEventType() == VALUE_DOUBLE}
     * @pre {dst != null}
     * @pre {offset >= 0}
     * @pre {length <= (dst.length - offset)}
     * @pre {getValueCount() - getValueReadCount() >= length}
     * @param dst
     *            the client supplied buffer where to store the read content
     * @param offset
     *            the zero based index at which to start storing the read content in {@code dst}
     * @param length
     *            the amount of elements to store in {@code dst}
     * @throws IOException
     *             if an I/O error occurs while reading the data
     * @throws IllegalArgumentException
     *             if the accumulated read length for the current value event exceeds the actual
     *             value length
     * @see #getValueCount()
     * @see #getValueReadCount()
     * @see #getDoubleValue()
     */
    public void getValue(double[] dst, int offset, int length) throws IOException;

    /**
     * Reads a single double value out of the underlying input stream.
     * <p>
     * Calls to this method may be interleaved with calls to {@link #getValue(double[], int, int)}
     * for the same VALUE_DOUBLE event, as long as the accumulated number of elements read does not
     * exceed the current {@link #getValueCount() value count}.
     * </p>
     * 
     * @pre {getEventType() == VALUE_DOUBLE}
     * @pre {getValueReadCount() < getValueCount()}
     * @return the value read
     * @throws IOException
     *             if an I/O error occurs while reading the data
     * @see #getValueCount()
     * @see #getValueReadCount()
     * @see #getValue(double[], int, int)
     */
    public double getDoubleValue() throws IOException;

    // public void getValue(char[] dst, int offset, int length) throws IOException;
}
