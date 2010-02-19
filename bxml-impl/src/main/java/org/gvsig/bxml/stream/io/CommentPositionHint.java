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
package org.gvsig.bxml.stream.io;

/**
 * Allowable values for the {@code positioningHint} field in a {@link TokenType#Comment} token.
 * <p>
 * It is provided to give a hint of what line position in textual XML a comment should be
 * regenerated. Comments will normally be indented on a fresh line, but options are provided to
 * suggest that they be generated at the start of a fresh line in case the comment has full-width
 * visual formatting, or at the end of the previous content/markup object, in the case that the
 * comment describes that object.
 * </p>
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @see TokenType#Comment
 */
public enum CommentPositionHint {
    /** Hint to position a comment on a fresh line but indented */
    INDENTED(0x00),
    /** Hint to position a comment on a fresh line */
    START_OF_LINE(0x01),
    /** Hint to position a comment after previous content */
    END_OF_LINE(0x02);

    private final int typeCode;

    CommentPositionHint(final int typeCode) {
        this.typeCode = typeCode;
    }

    public int getCode() {
        return typeCode;
    }

    public static final CommentPositionHint valueOf(final int code) {
        switch (code) {
        case 0x00:
            return INDENTED;
        case 0x01:
            return START_OF_LINE;
        case 0x02:
            return END_OF_LINE;
        default:
            throw new IllegalArgumentException(code + " is not a valid CommentPositionHint");
        }
    }
}
