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
package org.gvsig.bxml.stream;

/**
 * A runtime exception used by contract enforcement wrappers to signal a precondition was not met.
 * <p>
 * This exception shall not be declared in method signatures nor client code should catch them. The
 * raise of such an exception means a serious error and after its thrown the supplier code can't
 * ensure a correct behaviour.
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
class PreconditionViolationException extends RuntimeException {

    private static final long serialVersionUID = -6650026227362788386L;

    /**
     * @param msg
     *            precondition violation explanation
     */
    public PreconditionViolationException(String msg) {
        super(msg);
    }
}
