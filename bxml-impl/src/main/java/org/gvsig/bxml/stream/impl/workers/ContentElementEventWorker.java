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
 * TODO: document ContentElementEventWorker
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
final class ContentElementEventWorker extends StartElementTypeWorker {

    /**
     * Explicitly delegates to super.nextImpl() since this class controls the
     * {@link TokenType#ContentElement ContentElement} token, meaning its followed by zero or more
     * content tokens and finalized by one {@link TokenType#ElementEnd ElementEnd} token, and thus
     * we don't need to mimic the END_ELEMENT event.
     * 
     * @return the worker for the next token
     * @throws IOException
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#nextImpl(org.gvsig.bxml.stream.io.BxmlInputStream,
     *      org.gvsig.bxml.stream.impl.workers.ParseState)
     * @see ContentAttrElementEventWorker
     * @see EmptyElementEventWorker
     * @see EmptyAttrElementEventWorker
     */
    @Override
    protected EventTypeWorker nextImpl(final BxmlInputStream stream, final ParseState state)
            throws IOException {
        return super.nextImpl(stream, state);
    }
}
