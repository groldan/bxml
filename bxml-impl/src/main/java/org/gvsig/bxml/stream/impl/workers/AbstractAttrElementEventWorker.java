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

import java.io.IOException;
import java.util.ListIterator;

import javax.xml.XMLConstants;

import org.gvsig.bxml.stream.EventType;
import org.gvsig.bxml.stream.impl.NamesResolver;
import org.gvsig.bxml.stream.io.BxmlInputStream;

/**
 * TODO: document AbstractAttrElementEventWorker
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
abstract class AbstractAttrElementEventWorker extends StartElementTypeWorker {

    private static final String NSPREFIX_DECLARATION = XMLConstants.XMLNS_ATTRIBUTE + ":";

    /**
     * Overrides to extend the {@link StartElementTypeWorker} behaviour by reading the element
     * attributes.
     * 
     * @param stream
     * @param sharedState
     * @return
     * @throws IOException
     * @see org.gvsig.bxml.stream.impl.workers.StartElementTypeWorker#initImpl(org.gvsig.bxml.stream.io.BxmlInputStream,
     *      org.gvsig.bxml.stream.impl.workers.ParseState)
     */
    @Override
    protected final EventTypeWorker initImpl(final BxmlInputStream stream,
            final ParseState sharedState) throws IOException {
        // parse the start element tag
        super.initImpl(stream, sharedState);

        EventTypeWorker nextWorker = super.nextImpl(stream, sharedState);
        while (EventType.ATTRIBUTES_END != nextWorker.getEventType(sharedState)) {
            nextWorker = super.next(stream, sharedState);
        }

        final NamesResolver namesResolver = sharedState.getNamesResolver();

        if (namesResolver.isNamespaceAware()) {

            ParseState.Attribute plainAttribute;
            String attQname;
            String attValue;

            ListIterator<ParseState.Attribute> plainAtts;
            for (plainAtts = sharedState.getPlainAttributes(); plainAtts.hasNext();) {
                plainAttribute = plainAtts.next();
                attQname = plainAttribute.name;
                attValue = plainAttribute.value;

                if (XMLConstants.XMLNS_ATTRIBUTE.equals(attQname)) {
                    namesResolver.declarePrefix(XMLConstants.DEFAULT_NS_PREFIX, attValue);
                    plainAtts.remove();
                } else if (attQname.startsWith(NSPREFIX_DECLARATION)) {
                    final int colonIndex = attQname.indexOf(':');
                    final String prefix = attQname.substring(colonIndex + 1);
                    namesResolver.declarePrefix(prefix, attValue);
                    plainAtts.remove();
                }
            }
        }
        return this;
    }
}
