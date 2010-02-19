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

import java.util.EnumSet;
import java.util.Set;

/**
 * The EventType enum defines the possible parse events a {@link BxmlStreamReader} may return during
 * each call to {@code next()}.
 * <p>
 * These are high level events and may not map one to one to the low level Binary XML format tokens.
 * This is so to better reflect the problem domain for this high level API, letting the low level
 * treatment of specific bxml tokens to the implementation criteria.
 * </p>
 * <p>
 * In general lines, it could be said that the following EventType to TokenType mappings are
 * defined:
 * <table>
 * <tr>
 * <th>EventType</th>
 * <th>TokenType</th>
 * </tr>
 * <tr>
 * <td>NONE</td>
 * <td>Header</td>
 * </tr>
 * <tr>
 * <td>START_DOCUMENT</td>
 * <td>XmlDeclaration, if present, forced otherwise in order to always return a START_DOCUMENT event
 * </td>
 * </tr>
 * <tr>
 * <td>END_DOCUMENT</td>
 * <td>Trailer</td>
 * </tr>
 * <tr>
 * <td>START_ELEMENT</td>
 * <td>EmptyElement, EmptyAttrElement, ContentElement, ContentAttrElement</td>
 * </tr>
 * <tr>
 * <td>END_ELEMENT</td>
 * <td>ElementEnd, or forced when EmptyElement or EmptyAttrElement</td>
 * </tr>
 * <tr>
 * <td>ATTRIBUTE</td>
 * <td>AttributeStart, only used for writing</td>
 * </tr>
 * <tr>
 * <td>ATTRIBUTES_END</td>
 * <td>AttributeListEnd, only used for writing</td>
 * </tr>
 * <tr>
 * <td>COMMENT</td>
 * <td>Comment</td>
 * </tr>
 * <tr>
 * <td>SPACE</td>
 * <td>WhiteSpace</td>
 * </tr>
 * <tr>
 * <td>VALUE_STRING, VALUE_BOOL, VALUE_BYTE, VALUE_INT, VALUE_LONG, VALUE_FLOAT, VALUE_DOUBLE</td>
 * <td>CharContent token, followed by the specific value type</td>
 * </tr>
 * </table>
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 * @since 1.0
 */
public enum EventType {

    /**
     * Null object to indicate either a document has not started being parsed yet, or a document has
     * not started being written yet.
     */
    NONE,

    /**
     * Indicates the parser is positioned at the start of the document. The next call to
     * {@link BxmlStreamReader#next()} shall return either {@link #START_ELEMENT}, {@link #COMMENT}
     * or {@link #END_DOCUMENT} if the document is empty.
     */
    START_DOCUMENT,

    /**
     * Indicates the parser has reached the end of the bxml document
     */
    END_DOCUMENT,

    /**
     * Signals the opening of an element tag.
     */
    START_ELEMENT,

    /**
     * Signals the end of the current element. No content elements such as {@code <element/>} and
     * {@code <element att="attval"/>} will be notified by an START_ELEMENT and END_ELEMENT event
     * with no value event in between, to preserve semantic equivalence with the {@code
     * <element></element>} tag sequence.
     */
    END_ELEMENT,

    /**
     * Content event that indicates the parser is at a value token whose content is either a Java
     * boolean or an array of booleans.
     */
    VALUE_BOOL,

    /**
     * Content event that indicates the parser is at a value token whose content is either a Java
     * byte or an array of bytes.
     */
    VALUE_BYTE,

    /**
     * Content event that indicates the parser is at a value token whose content is either a Java
     * integer or an array of integers. For the sake of simplicity, the low level {@code SmallNum},
     * {@code Short}, {@code UShort}, and {@code Int} value types are mapped to this event type.
     */
    VALUE_INT,

    /**
     * Content event that indicates the parser is at a value token whose content is either a Java
     * long or an array of longs.
     */
    VALUE_LONG,

    /**
     * Content event that indicates the parser is at a value token whose content is either a Java
     * float or an array of floats.
     */
    VALUE_FLOAT,

    /**
     * Content event that indicates the parser is at a value token whose content is either a Java
     * double or an array of double.
     */
    VALUE_DOUBLE,

    /**
     * Content event that indicates the parser is at a value token whose content is a Java String.
     * There are no such a structure as an array of Strings in the BXML spec.
     */
    VALUE_STRING,

    /**
     * This is event equivalent to the {@code "<![CDATA[content]]>"} structure in textual XML,
     * signaled by the CDataSectionToken. This token is essentially equivalent to the
     * CharContentToken, except that its use may be regarded as a hint to a translator to regenerate
     * a CDATA section in textual XML.
     */
    VALUE_CDATA,

    /**
     * Content event that signals the presence of a WhiteSpace token. That is, a potentially
     * insignificant sequence of white space or newline characters
     */
    SPACE,

    /**
     * Indicates the parser is at a XML comment token
     */
    COMMENT,

    /**
     * Used only for writing attributes.
     * 
     * @see BxmlStreamWriter#writeStartAttribute(String, String)
     */
    ATTRIBUTE,

    /**
     * Used only for writing attributes.
     * 
     * @see BxmlStreamWriter#writeEndAttributes()
     */
    ATTRIBUTES_END,
    
    /**
     * Indicates the presence of a namespace declaration
     */
    NAMESPACE_DECL;

    /**
     * Cached set of enum values that represent a value token
     */
    private static Set<EventType> valueEvents = EnumSet.range(VALUE_BOOL, SPACE);

    /**
     * Convenience method that returns whether this EventType represents a value token
     * 
     * @return {@code true} if {@code eventType} is one of {@code VALUE_BOOL, VALUE_BYTE, VALUE_INT,
     *         VALUE_LONG, VALUE_FLOAT, VALUE_DOUBLE, VALUE_STRING, VALUE_CDATA, SPACE}
     */
    public boolean isValue() {
        return valueEvents.contains(this);
    }

    /**
     * Convenience method that returns whether this EventType represents a tag element
     * 
     * @return {@code true} if {@code eventType} is one of {@code START_ELEMENT, END_ELEMENT}
     */
    public boolean isTag() {
        return START_ELEMENT == this || END_ELEMENT == this;
    }
}
