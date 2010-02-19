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
import org.gvsig.bxml.stream.io.BxmlInputStream;
import org.gvsig.bxml.stream.io.CommentPositionHint;

/**
 * TODO: document CommentEventWorker
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
final class CommentEventWorker extends AbstractContentEventWorker {

    /**
     * @param state
     * @return {@link EventType#COMMENT}
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#getEventType(org.gvsig.bxml.stream.impl.workers.ParseState)
     */
    @Override
    public EventType getEventType(final ParseState state) {
        return EventType.COMMENT;
    }

    /**
     * @param stream
     * @param state
     * @return
     * @throws IOException
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#initImpl(org.gvsig.bxml.stream.io.BxmlInputStream,
     *      org.gvsig.bxml.stream.impl.workers.ParseState)
     * @see ParseState#setValueLength(int)
     * @see ParseState#setValueElementsReadCount(int)
     */
    @Override
    protected EventTypeWorker initImpl(final BxmlInputStream stream, final ParseState state)
            throws IOException {
        // get the comment position hint for the comment
        final int commentPositionHintCode = stream.readByte();
        CommentPositionHint commentPostHint = CommentPositionHint.valueOf(commentPositionHintCode);
        state.setCommentPositionHint(commentPostHint);
        // set the comment value read count to zero, will be checked for at nextImpl to decide
        // whether the comment value needs to be skipped or not
        state.setValueLength(1);
        state.setValueElementsReadCount(0);
        return this;
    }

    /**
     * If the comment value were not read skips it, otherwise behaves as
     * {@link EventTypeWorker#nextImpl(BxmlInputStream, ParseState) super.nextImpl()}
     * 
     * @param stream
     * @param state
     * @return
     * @throws IOException
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#nextImpl(org.gvsig.bxml.stream.io.BxmlInputStream,
     *      org.gvsig.bxml.stream.impl.workers.ParseState)
     */
    protected EventTypeWorker nextImpl(final BxmlInputStream stream, final ParseState state)
            throws IOException {
        // discard the comment position hint
        state.setCommentPositionHint(null);

        final int readCount = state.getValueElementsReadCount();
        switch (readCount) {
        case 0:
            // comment value was not read, skip it
            stream.skipString();
            break;
        case 1:
            // comment value already read, go ahead
            state.setValueElementsReadCount(0);
            break;
        default:
            // oops, an unrecoverable workflow break, shouldn't happen!
            throw new IllegalStateException("Value element read count for comment tokens "
                    + "can be only 0 (unread) or 1 (read),  was " + readCount);
        }
        return super.nextImpl(stream, state);
    }

    /**
     * Reads the content of the comment token. No conversion is needed since comments contents
     * already are strings.
     * 
     * @see CharContentValueConverter#getValueAsString(BxmlInputStream, ParseState)
     */
    public String getValueAsString(BxmlInputStream stream, ParseState state) throws IOException {
        String value = stream.readString();
        state.notifyValueRead(1);
        return value;
    }
}
