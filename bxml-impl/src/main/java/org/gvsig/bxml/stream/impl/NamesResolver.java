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

import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.xml.sax.helpers.NamespaceSupport;

/**
 * Controls conversion of element and attribute names as they come from the StringTable into
 * namespace aware or not element names.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public interface NamesResolver {

    public QName resolve(final String attributeName, final boolean isAttribute);

    public abstract boolean isNamespaceAware();

    /**
     * Starts a new Namespace context (same as {@link NamespaceSupport#pushContext}).
     * 
     * <pre>
     * &lt;borrowed&gt;
     * </pre>
     * 
     * The new context will automatically inherit the declarations of its parent context, but it
     * will also keep track of which declarations were made within this context.
     * 
     * <p>
     * Event callback code should start a new context once per element. This means being ready to
     * call this in either of two places. For elements that don't include namespace declarations,
     * the <em>ContentHandler.startElement()</em> callback is the right place. For elements with
     * such a declaration, it'd done in the first <em>ContentHandler.startPrefixMapping()</em>
     * callback. A boolean flag can be used to track whether a context has been started yet. When
     * either of those methods is called, it checks the flag to see if a new context needs to be
     * started. If so, it starts the context and sets the flag. After
     * <em>ContentHandler.startElement()</em> does that, it always clears the flag.
     * 
     * <p>
     * Normally, SAX drivers would push a new context at the beginning of each XML element. Then
     * they perform a first pass over the attributes to process all namespace declarations, making
     * <em>ContentHandler.startPrefixMapping()</em> callbacks. Then a second pass is made, to
     * determine the namespace-qualified names for all attributes and for the element name. Finally
     * all the information for the <em>ContentHandler.startElement()</em> callback is available, so
     * it can then be made.
     * 
     * <p>
     * The Namespace support object always starts with a base context already in force: in this
     * context, only the "xml" prefix is declared.
     * </p>
     * 
     * <pre>
     * &lt;/borrowed&gt;
     * </pre>
     * 
     */
    public void pushContext();

    /**
     * Revert to the previous Namespace context (same as {@link NamespaceSupport#popContext()}).
     * <p>
     * 
     * <pre>
     * &lt;borrowed&gt;
     * </pre>
     * 
     * Normally, you should pop the context at the end of each XML element. After popping the
     * context, all Namespace prefix mappings that were previously in force are restored.
     * </p>
     * 
     * <p>
     * You must not attempt to declare additional Namespace prefixes after popping a context, unless
     * you push another context first.
     * 
     * <pre>
     * &lt;/borrowed&gt;
     * </pre>
     * 
     * </p>
     * 
     * @see #pushContext
     */
    public void popContext();

    public void declarePrefix(final String prefix, final String namespaceUri);

    /**
     * Returns the (potentially prefixed) qname for the given full name.
     * 
     * @param namespaceUri
     *            the
     * @param localName
     * @return
     */
    public void toQName(String namespaceUri, String localName, StringBuilder target);

    public Map<String, String> getPrefixToNamespaceMap();

    public String getNamespace(String defaultNsPrefix);

    /**
     * Returns one of the prefixes mapped to a Namespace URI.
     * 
     * @param namespaceUri
     *            the namespace URI to return one of the prefixes bound to
     * @return a prefix bound to {@code namespaceUri}, or {@code null} if there's none
     */
    public String getPrefix(String namespaceUri);

    /**
     * Returns an enumeration of all prefixes whose declarations are active in the current context.
     * <p>
     * This includes declarations from parent contexts that have not been overridden.
     * </p>
     */
    public Set<String> getPrefixes();

}
