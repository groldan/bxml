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

import org.gvsig.bxml.stream.EventType;
import org.gvsig.bxml.stream.impl.NamesResolver;
import org.gvsig.bxml.stream.io.BxmlInputStream;
import org.gvsig.bxml.stream.io.TokenType;

/**
 * Base abstract EventTypeWorker for workers that produce START_ELEMENT events based on the
 * different element tokens ( {@link TokenType#EmptyAttrElement}, {@link TokenType#EmptyElement},
 * {@link TokenType#ContentElement} and {@link TokenType#ContentAttrElement} ).
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
abstract class StartElementTypeWorker extends EventTypeWorker {

    @Override
    public final EventType getEventType(final ParseState state) {
        return EventType.START_ELEMENT;
    }

    /**
     * Reads the StringTable reference for the element name from the {@code stream} and sets the
     * current element name in {@code sharedState}.
     * 
     * @param stream
     * @param sharedState
     * @return
     * @throws IOException
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#initImpl(org.gvsig.bxml.stream.io.BxmlInputStream,
     *      org.gvsig.bxml.stream.impl.workers.ParseState)
     */
    @Override
    protected EventTypeWorker initImpl(final BxmlInputStream stream, final ParseState sharedState)
            throws IOException {
        // and read all attributes
        sharedState.clearAttributes();

        final long elemNameStrRef = stream.readCount();
        final String currentElementName = sharedState.getStringTable().get(elemNameStrRef);
        final TokenType currentTokenType = sharedState.getCurrentTokenType();
        final long currentTokenPosition = sharedState.getCurrentTokenPosition();
        // do the check in the order that short circuit evaluation may take less steps to evaluate..
        if (TokenType.ContentAttrElement == currentTokenType
                || TokenType.ContentElement == currentTokenType
                || TokenType.EmptyAttrElement == currentTokenType
                || TokenType.EmptyElement == currentTokenType) {

            sharedState.pushElement(currentElementName, currentTokenType, currentTokenPosition);
            /*
             * NOTE I'm not pushing/poping context anymore when parsing because the introduction of
             * random access support may lead to opening lots (thousands) of elements without
             * getting to close them and ultimately going OOM due to the ever increasing element
             * stak.
             */

            // ////final NamesResolver namesResolver = sharedState.getNamesResolver();
            // ////namesResolver.pushContext();
            return this;
        }

        throw new IllegalStateException("Current token type shall be one of ContentAttrElement,"
                + " ContentElement, EmptyAttrElement or EmptyElement: " + currentTokenType);
    }

}
