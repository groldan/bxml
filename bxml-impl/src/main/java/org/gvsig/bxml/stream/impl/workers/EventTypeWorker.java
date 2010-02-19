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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gvsig.bxml.stream.EventType;
import org.gvsig.bxml.stream.io.BxmlInputStream;
import org.gvsig.bxml.stream.io.TokenType;

/**
 * An EventTypeWorker handles the parsing workflow for a specific {@link EventType}, allowing to
 * implement the optimizations allowed for each event type and still keep the code clean
 * <p>
 * A single instance of each specific EventTypeWorker will be maintained for each
 * DefaultBxmlStreamReader. This instances contain its own state.
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public abstract class EventTypeWorker {

    /** Package logger */
    private static final Logger LOGGER = Logger.getLogger("org.gvsig.bxml.stream.impl.workers");

    private static final Map<TokenType, EventTypeWorker> workerMappings;
    static {
        HashMap<TokenType, EventTypeWorker> workers = new HashMap<TokenType, EventTypeWorker>();
        workers.put(TokenType.AttributeStart, new AttributeEventWorker());
        workers.put(TokenType.AttributeListEnd, new AttributesEndEventWorker());
        workers.put(TokenType.Comment, new CommentEventWorker());
        workers.put(TokenType.Trailer, new EndDocumentEventWorker());
        workers.put(TokenType.ElementEnd, new EndElementEventWorker());
        workers.put(TokenType.Whitespace, new SpaceEventWorker());
        workers.put(TokenType.XmlDeclaration, new StartDocumentEventWorker());

        workers.put(TokenType.ContentElement, new ContentElementEventWorker());
        workers.put(TokenType.ContentAttrElement, new ContentAttrElementEventWorker());
        workers.put(TokenType.EmptyElement, new EmptyElementEventWorker());
        workers.put(TokenType.EmptyAttrElement, new EmptyAttrElementEventWorker());
        workers.put(TokenType.ElementEnd, new EndElementEventWorker());

        workers.put(TokenType.CDataSection, new CDataSectionEventWorker());
        workers.put(TokenType.CharContent, new CharContentEventWorker());
        workers.put(TokenType.CharContentRef, new CharContentRefEventWorker());
        workers.put(TokenType.CharEntityRef, new CharEntityRefEventWorker());
        workers.put(TokenType.Comment, new CommentEventWorker());
        workers.put(TokenType.EntityRef, new EntityRefEventWorker());
        workers.put(TokenType.ProcessingInstr, new ProcessingInstructionEventWorker());

        workers.put(TokenType.StringTable, new StringTableWorker());
        workerMappings = workers;
    }

    /**
     * Returns the EventType corresponding to the {@link TokenType token} the stream reader is
     * positioned at.
     * 
     * @return
     */
    public abstract EventType getEventType(final ParseState state);

    /**
     * An EventTypeWorker calls this method over the worker about to be returned when
     * {@link #next(BxmlInputStream, ParseState)} is called, so the worker to be returned
     * initializes its state as needed.
     */
    public final EventTypeWorker init(final BxmlInputStream stream, final ParseState sharedState)
            throws IOException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Init EventTypeWorker " + this.getClass().getSimpleName() + " for token "
                    + sharedState.getCurrentTokenType());
        }

        final EventTypeWorker initializedWorker = initImpl(stream, sharedState);

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Returned initialized EventTypeWorker "
                    + initializedWorker.getClass().getSimpleName() + " for token "
                    + sharedState.getCurrentTokenType());
        }

        return initializedWorker;
    }

    /**
     * Implementations that need initialization shall override this template method.
     * 
     * @param stream
     * @param sharedState
     * @return
     * @throws IOException
     */
    protected EventTypeWorker initImpl(final BxmlInputStream stream, final ParseState sharedState)
            throws IOException {
        return this;
    }

    /**
     * Advances to the next event type and returns its corresponding worker.
     * <p>
     * This is where optimization can take place when an event (eg, VALUE_*) hadn't been consumed
     * and the next event is required. Implementations may just "skip" on the {@code stream}
     * avoiding the parse overhead.
     * </p>
     * <p>
     * This default implementation reads a {@link TokenType} from the {@code stream} and returns its
     * mapping {@link EventTypeWorker}. Subclasses may override as needed to perform other
     * operations as long as they respect the general contract of this method.
     * </p>
     * 
     * @param stream
     * @param state
     * @return
     * @throws IOException
     *             if an IO error occurs while reading reading over the {@code stream}
     */
    public final EventTypeWorker next(final BxmlInputStream stream, final ParseState state)
            throws IOException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Getting next event worker from " + this.getClass().getSimpleName()
                    + " for token " + state.getCurrentTokenType());
        }

        final EventTypeWorker nextWorker = nextImpl(stream, state);

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Got EventTypeWorker " + nextWorker.getClass().getSimpleName());
        }

        return nextWorker;
    }

    /**
     * Implementations needing to do something else than reading the next token type and getting its
     * mapping EventTypeWorker shall override this template method.
     * 
     * @param stream
     * @param state
     * @return
     * @throws IOException
     */
    protected EventTypeWorker nextImpl(final BxmlInputStream stream, ParseState state)
            throws IOException {
        final long tokenPosition = stream.getPosition();
        final TokenType tokenType = stream.readTokenType();        
        
        EventTypeWorker nextWorker = getWorker(tokenType);
        state.setCurrentTokenType(tokenType);
        state.setCurrentTokenPosition(tokenPosition);
        nextWorker = nextWorker.init(stream, state);
        return nextWorker;
    }

    /**
     * Looks up for the EventTypeWorker that maps to the given tokenType.
     * 
     * @param tokenType
     *            the TokenType for which to find a mapping EventTypeWorker
     * @return the EventTypeWorker that maps to the given tokenType
     * @throws IllegalArgumentException
     *             if there's no EventTypeWorker that maps to the given tokenType
     */
    public static final EventTypeWorker getWorker(final TokenType tokenType) {
        final EventTypeWorker worker = workerMappings.get(tokenType);
        if (worker == null) {
            throw new IllegalArgumentException("There's no mapping worker for token " + tokenType);
        }
        return worker;
    }
}
