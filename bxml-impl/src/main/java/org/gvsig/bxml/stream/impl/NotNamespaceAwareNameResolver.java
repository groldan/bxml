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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * NamesResolver that hanldes names in a namespace aware fashion.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
class NotNamespaceAwareNameResolver implements NamesResolver {

    private Map<String, QName> cache = new HashMap<String, QName>();

    public QName resolve(final String attributeName, final boolean isAttribute) {
        QName qname = cache.get(attributeName);
        if (qname == null) {
            qname = new QName(attributeName);
            cache.put(attributeName, qname);
        }
        return qname;
    }

    /**
     * @return false
     * @see org.gvsig.bxml.stream.impl.NamesResolver#isNamespaceAware()
     */
    public boolean isNamespaceAware() {
        return false;
    }

    /**
     * Should not be called
     * 
     * @param ssPrefix
     * @param namespaceUri
     * @see org.gvsig.bxml.stream.impl.NamesResolver#declarePrefix(java.lang.String,
     *      java.lang.String)
     */
    public void declarePrefix(String ssPrefix, String namespaceUri) {
        throw new UnsupportedOperationException("Operation not supported");
    }

    public void popContext() {
        // do nothing
    }

    public void pushContext() {
        // do nothing
    }

    public void toQName(String namespaceUri, String localName, StringBuilder target) {
        throw new UnsupportedOperationException("Operation not supported");
    }

    public Map<String, String> getPrefixToNamespaceMap() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getNamespace(String defaultNsPrefix) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getPrefix(String namespaceUri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @return empty set
     * @see org.gvsig.bxml.stream.impl.NamesResolver#getPrefixes()
     */
    public Set<String> getPrefixes() {
        return Collections.emptySet();
    }

}
