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

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines the encoding options a {@link BxmlStreamWriter} is created with.
 * <p>
 * The default settings are as follow:
 * <ul>
 * <li>byteOrder: defaults to the platform's default "endianess"; that is, the byte order used by
 * the running JVM, acquired through {@link ByteOrder#nativeOrder()};
 * <li>charactersEncoding: {@code UTF-8}
 * <li>useCompression: false;
 * <li>useStrictXmlStrings: false;
 * <li>isValidated: false;
 * <li>isNamespaceAware: false.
 * </ul>
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 * @since 1.0
 */
public class EncodingOptions implements Cloneable {
    public static final String DEFAULT_XML_VERSION = "1.0";

    private boolean namespaceAware;

    private ByteOrder byteOrder;

    private Charset charactersEncoding;

    private boolean useCompression;

    private boolean useStrictXmlStrings;

    private boolean isValidated;

    private String xmlVersion;

    private Boolean isStandalone;

    private boolean indexStringTableEntries;

    private Set<String> indexTableXpathExpressions = Collections.emptySet();

    /**
     * Creates a new EncodingOptionsImpl with default values.
     */
    public EncodingOptions() {
        this.namespaceAware = true;
        this.byteOrder = ByteOrder.nativeOrder();
        this.charactersEncoding = Charset.forName("UTF-8");
        this.xmlVersion = DEFAULT_XML_VERSION;
        this.useCompression = false;
        this.useStrictXmlStrings = false;
        this.isValidated = false;
        this.isStandalone = null;
        this.indexStringTableEntries = false;
    }

    /**
     * Specifies whether the {@link BxmlStreamWriter} to be created for this encoding options shall
     * be namespace aware.
     * <p>
     * </p>
     * 
     * @return {@code true} if the {@code BxmlStreamWriter} shall be namespace aware, {@code false}
     *         otherwise. Defaults to {@code true}
     */
    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    /**
     * @param namespaceAware
     *            whether the {@code BxmlStreamWriter} shall be namespace aware
     */
    public final void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    /**
     * Returns the byte order (so called "endianess") the {@link BxmlStreamWriter}s created with
     * this encoding options shall use to encode multi-byte primitive values such as {@code double,
     * float, long, etc.}.
     * <p>
     * The default value is dependant on the endianess for the hardware architecture the Java
     * Virtual Machine executing the code is running on, and is obtained through
     * {@link ByteOrder#nativeOrder()}.
     * </p>
     * 
     * @return
     * @see ByteOrder
     */
    public final ByteOrder getByteOrder() {
        return byteOrder;
    }

    /**
     * Sets the byte order (so called "endianess") the {@link BxmlStreamWriter}s created with this
     * encoding options shall use to encode multi-byte primitive values such as {@code double,
     * float, long, etc.}.
     * 
     * @return
     * @see ByteOrder
     * @see #getByteOrder()
     */
    public final void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    /**
     * Returns the charset encoding the {@link BxmlStreamWriter}s created with this encoding options
     * shall use to encode string literals into.
     * <p>
     * Defaults to "UTF-8"
     * <p>
     * 
     * @return the charset set to encode string literals in the target binary xml document, or
     *         {@code UTF-8} if not explicitly set.
     */
    public final Charset getCharactersEncoding() {
        return charactersEncoding;
    }

    /**
     * Sets the charset encoding the {@link BxmlStreamWriter}s created with this encoding options
     * shall use to encode string literals into (that is, both xml tag names, attributes, string
     * attribute and element values, etc).
     * 
     * @param charactersEncoding
     *            the charset set to encode string literals in the target binary xml document, or
     *            {@code UTF-8} if not explicitly set.
     */
    public final void setCharactersEncoding(Charset charactersEncoding) {
        this.charactersEncoding = charactersEncoding;
    }

    /**
     * Returns whether {@link BxmlStreamWriter}s created with this encoding options shall compress
     * the document's contents using GZIP encoding.
     * <p>
     * Defaults to {@code false}
     * </p>
     * 
     * @return {@code true} if GZIP compression shall be used, {@code false otherwise}
     */
    public final boolean isUseCompression() {
        return useCompression;
    }

    /**
     * Sets whether {@link BxmlStreamWriter}s created with this encoding options shall compress the
     * document's contents using GZIP encoding.
     * 
     * @param useCompression
     *            whether to use GZIP encoding for value elements or not.
     */
    public final void setUseCompression(boolean useCompression) {
        this.useCompression = useCompression;
    }

    /**
     * TODO: describe
     * <p>
     * Defaults to {@code false}
     * </p>
     * 
     * @return
     */
    public final boolean isUseStrictXmlStrings() {
        return useStrictXmlStrings;
    }

    /**
     * TODO: describe
     * 
     * @param useStrictXmlStrings
     */
    public final void setUseStrictXmlStrings(boolean useStrictXmlStrings) {
        this.useStrictXmlStrings = useStrictXmlStrings;
    }

    /**
     * Returns a flag indicating whether the document to be written should be considered valid
     * against its schema.
     * <p>
     * Defaults to {@code false}
     * </p>
     * 
     * @return
     */
    public final boolean isValidated() {
        return isValidated;
    }

    /**
     * Sets a flag indicating whether the document to be written should be considered valid against
     * its schema.
     * 
     * @param isValidated
     *            a flag for code reading the document to be written that indicates the document is
     *            already validated, to easy the task of parsers.
     */
    public final void setValidated(boolean isValidated) {
        this.isValidated = isValidated;
    }

    /**
     * Returns the xml version that is to be stated in the documents xml declaration token (i.e. the
     * {@code "1.0"} in {@code <?xml version="1.0"?>}.
     * <p>
     * Defaults to {@link #DEFAULT_XML_VERSION "1.0"}
     * </p>
     * 
     * @return the xml version for the xml declaration token in the bxml documents encoded with this
     *         options.
     */
    public String getXmlVersion() {
        return this.xmlVersion;
    }

    /**
     * Sets the xml version that is to be stated in the documents xml declaration token (i.e. the
     * {@code "1.0"} in {@code <?xml version="1.0"?>}.
     * 
     * @param xmlVersion
     *            the xml version for the xml declaration token in the bxml documents encoded with
     *            this options.
     */
    public final void setXmlVersion(String xmlVersion) {
        if (xmlVersion == null) {
            throw new NullPointerException();
        }
        this.xmlVersion = xmlVersion;
    }

    /**
     * Indicates whether the document to be written is standalone as defined in the <a
     * href="http://www.w3.org/TR/2006/REC-xml-20060816/#sec-rmd">xml spec</a>
     * <p>
     * If this property is not set (i.e. returns {@code null} , the {@code standalone} flag in the
     * BXML document defaults to {@code true} and the {@code standaloneIsSet} flag defaults to
     * {@code false}.
     * </p>
     * 
     * @return the standalone flag, or {@code null} if not set
     */
    public Boolean isStandalone() {
        return this.isStandalone;
    }

    /**
     * @param isStandalone
     *            {@code null} to indicate the standalone flag is not set, otherwise
     *            {@link Boolean#TRUE} or {@link Boolean#FALSE} to explicitly indicate the xml
     *            document standalone flag.
     */
    public final void setStandalone(Boolean isStandalone) {
        this.isStandalone = isStandalone;
    }

    /**
     * Returns a (possibly empty) set of XPath expressions to create an index for, if the
     * Implementation supports it.
     * <p>
     * The set of XPath expressions returned is just a HINT on what location paths to store and
     * index entry for at the trailer token. A {@link BxmlStreamWriter} implementation is free to
     * ignore it if it can't handle XPath indexing. If it does not ignore it, though, its mandated
     * to correctly index all the XPath expressions.
     * </p>
     * <p>
     * The allowable XPath expressions are a subset of the XPath specification as defined in the OGC
     * Filter encoding specification. TODO: specify more the
     * </p>
     * 
     * @return
     */
    public Set<String> getIndexableXpathExpressionsHint() {
        return indexTableXpathExpressions;
    }

    /**
     * @param indexTableXpathExpressions
     */
    public final void setIndexTableXpathExpressions(Set<String> indexTableXpathExpressions) {
        this.indexTableXpathExpressions = indexTableXpathExpressions;
    }

    /**
     * Hint to indicate a {@link BxmlStreamWriter} to write out an index table at the trailer token
     * with the file offsets of the StringTable tokens spread throughout the document.
     * 
     * @return
     */
    public boolean isIndexStringTableEntriesHint() {
        return indexStringTableEntries;
    }

    /**
     * 
     */
    public final void setIndexStringTableEntriesHint(boolean indexStringTableEntries) {
        this.indexStringTableEntries = indexStringTableEntries;
    }

    /**
     * Clones this encoding options
     */
    @Override
    public EncodingOptions clone() {
        try {
            EncodingOptions clone = (EncodingOptions) super.clone();
            if (clone.indexTableXpathExpressions != Collections.EMPTY_SET) {
                clone.setIndexTableXpathExpressions(new HashSet<String>(
                        clone.indexTableXpathExpressions));
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Can't happen!", e);
        }
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EncodingOptions)) {
            return false;
        }
        final EncodingOptions other = (EncodingOptions) o;
        if (namespaceAware != other.namespaceAware)
            return false;
        if (byteOrder != other.byteOrder)
            return false;
        if (charactersEncoding != other.charactersEncoding)
            return false;
        if (useCompression != other.useCompression)
            return false;
        if (useStrictXmlStrings != other.useStrictXmlStrings)
            return false;
        if (isValidated != other.isValidated)
            return false;
        if (!xmlVersion.equals(other.xmlVersion))
            return false;

        return isStandalone == null ? other.isStandalone == null : isStandalone
                .equals(other.isStandalone);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 37 ^ (namespaceAware ? 3 : 5);
        hash *= ByteOrder.BIG_ENDIAN == byteOrder ? 7 : 9;
        hash *= charactersEncoding.hashCode();
        hash *= useCompression ? 1 : 2;
        hash *= useStrictXmlStrings ? 11 : 13;
        hash *= isValidated ? 17 : 21;
        hash *= xmlVersion.hashCode();
        hash *= isStandalone == null ? 1 : (isStandalone.booleanValue() ? 2 : 1);
        return hash;
    }
}