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

import org.gvsig.bxml.stream.io.BxmlInputStream;
import org.gvsig.bxml.stream.io.TokenType;

/**
 * TODO: document EmptyAttrElementEventWorker
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
final class EmptyAttrElementEventWorker extends AbstractAttrElementEventWorker {

    /**
     * Overrides to explicitly return an {@link EndElementEventWorker} in order to force an
     * END_ELEMENT event, since this class controls the {@link TokenType#EmptyAttrElement
     * ContentElement} token, meanings its followed by one or more AttributeStart token and an
     * AttributesEnd token, but no content token, and also is not followed by a
     * {@link TokenType#ElementEnd ElementEnd} token.
     * 
     * @return the worker that maps to the ElementEnd token as per
     *         {@link EventTypeWorker#getWorker(TokenType)}
     * @throws IOException
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#nextImpl(org.gvsig.bxml.stream.io.BxmlInputStream,
     *      org.gvsig.bxml.stream.impl.workers.ParseState)
     * @see ContentAttrElementEventWorker
     * @see ContentElementEventWorker
     * @see EmptyElementEventWorker
     */
    @Override
    protected EventTypeWorker nextImpl(final BxmlInputStream stream, final ParseState state)
            throws IOException {
        final TokenType currentTokenType = state.getCurrentTokenType();
        if (TokenType.AttributeListEnd == currentTokenType) {
            state.setCurrentTokenType(TokenType.ElementEnd);
            EventTypeWorker nextWorker = EventTypeWorker.getWorker(TokenType.ElementEnd);
            nextWorker = nextWorker.init(stream, state);
            return nextWorker;
        }
        return super.nextImpl(stream, state);
    }
}
