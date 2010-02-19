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
 * Enumeration that defines the possible encoding types for the content of a
 * {@link TokenType#BlobSection} token.
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @see TokenType#BlobSection
 */
public enum TextBlobType {
    NONE(0x00), HEXCODED(0x01), BASE64(0x02), BYTEARRAY(0x03);

    private final int typeCode;

    TextBlobType(final int typeCode) {
        this.typeCode = typeCode;
    }

    public int getCode() {
        return typeCode;
    }
}
