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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.xml.sax.helpers.NamespaceSupport;

/**
 * NamesResolver that hanldes names in a non namespace aware fashion.
 * <p>
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
@SuppressWarnings("unchecked")
class NamespaceAwareNameResolver implements NamesResolver {

    private final NamespaceSupport namespaces;

    /**
     * Holder for {@link NamespaceSupport#processName(String, String[], boolean)}
     * 
     * @see #deglose(String, boolean)
     */
    private String[] namePartsHolder = new String[3];

    private Map<String, QName> resolvedCache = new HashMap<String, QName>();

    /**
     * Creates a new namespace aware qname resolver
     */
    public NamespaceAwareNameResolver() {
        namespaces = new NamespaceSupport();
        namespaces.setNamespaceDeclUris(true);
    }

    /**
     * @see org.gvsig.bxml.stream.impl.NamesResolver#resolve(String, boolean)
     */
    public QName resolve(final String prefixedName, final boolean isAttribute) {
        QName qname = resolvedCache.get(prefixedName);
        if (qname == null) {
            qname = deglose(prefixedName, isAttribute);
            resolvedCache.put(prefixedName, qname);
        }
        return qname;
    }

    private QName deglose(final String prefixedName, final boolean isAttribute) {

        // String[] namePartsHolder = namespaces.processName(prefixedName, this.namePartsHolder,
        // isAttribute);
        String[] namePartsHolder = namespaces
                .processName(prefixedName, this.namePartsHolder, false);
        if (namePartsHolder == null) {
            throw new IllegalStateException("Ubound " + (isAttribute ? "attribute" : "element")
                    + " name: " + prefixedName);
        }
        final String nsUri = namePartsHolder[0];
        final String localName = namePartsHolder[1];
        final String prefix = namespaces.getPrefix(nsUri);
        QName qname = new QName(nsUri, localName, prefix == null ? "" : prefix);
        return qname;
    }

    /**
     * @return true
     * @see org.gvsig.bxml.stream.impl.NamesResolver#isNamespaceAware()
     */
    public boolean isNamespaceAware() {
        return true;
    }

    public void declarePrefix(final String prefix, final String namespaceUri) {
        if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
            return;// ignore, "xml" prefix should not be declared
        }
        if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            return;// ignore, "xmlns" should not be declared as prefix
        }
        namespaces.declarePrefix(prefix, namespaceUri);
    }

    public void popContext() {
        namespaces.popContext();
    }

    public void pushContext() {
        namespaces.pushContext();
    }

    /**
     * Stores the qName corresponding to the given ns and localName, for the current context, in the
     * given StringBuilder target.
     * <p>
     * The target is trimmed before filling it with the qName value.
     * </p>
     * 
     * @param namespaceUri
     * @param localName
     * @return
     * @see org.gvsig.bxml.stream.impl.NamesResolver#toQName(java.lang.String, java.lang.String)
     */
    public void toQName(final String namespaceUri, final String localName, StringBuilder target) {
        String prefix;

        prefix = namespaces.getPrefix(namespaceUri);

        if (prefix == null && XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceUri)) {
            prefix = XMLConstants.XMLNS_ATTRIBUTE;
        }

        target.setLength(0);
        if (prefix != null) {
            target.append(prefix);
            target.append(':');
        }
        target.append(localName);
    }

    /**
     * Returns a map of all the prefix/namespace pairs valid for the current context, including the
     * ones for the parent contexts.
     * 
     * @see NamesResolver#getPrefixToNamespaceMap()
     */
    public Map<String, String> getPrefixToNamespaceMap() {
        final Enumeration<String> prefixes = namespaces.getPrefixes();
        Map<String, String> prefixToNamespaceMap = new HashMap<String, String>();
        while (prefixes.hasMoreElements()) {
            String prefix = prefixes.nextElement();
            String namespace = namespaces.getURI(prefix);
            prefixToNamespaceMap.put(prefix, namespace);
        }
        return prefixToNamespaceMap;
    }

    /**
     * Use the empty string for the default namespace
     * 
     * @param prefix
     * @return
     * @see org.gvsig.bxml.stream.impl.NamesResolver#getNamespace(java.lang.String)
     */
    public String getNamespace(final String prefix) {
        return namespaces.getURI(prefix);
    }

    /**
     * @see org.gvsig.bxml.stream.impl.NamesResolver#getPrefix(java.lang.String)
     */
    public String getPrefix(String namespaceUri) {
        return namespaces.getPrefix(namespaceUri);
    }

    /**
     * @see org.gvsig.bxml.stream.impl.NamesResolver#getPrefixes()
     */
    public Set<String> getPrefixes() {
        Set<String> ret = new HashSet<String>();
        Enumeration<String> prefixes = namespaces.getPrefixes();
        while (prefixes.hasMoreElements()) {
            ret.add(prefixes.nextElement());
        }
        return ret;
    }

}
