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
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
abstract class Contract {

    protected static void assertPre(final boolean preconditionMet, final Object msg1)
            throws PreconditionViolationException {
        assertPre(preconditionMet, msg1, null, null, null, null, null);
    }

    protected static void assertPre(final boolean preconditionMet, final Object msg1,
            final Object msg2) throws PreconditionViolationException {
        assertPre(preconditionMet, msg1, msg2, null, null, null, null);
    }

    protected static void assertPre(final boolean preconditionMet, final Object msg1,
            final Object msg2, final Object msg3) throws PreconditionViolationException {
        assertPre(preconditionMet, msg1, msg2, msg3, null, null, null);
    }

    protected static void assertPre(final boolean preconditionMet, final Object msg1,
            final Object msg2, final Object msg3, final Object msg4)
            throws PreconditionViolationException {
        assertPre(preconditionMet, msg1, msg2, msg3, msg4, null, null);
    }

    protected static void assertPre(final boolean preconditionMet, final Object msg1,
            final Object msg2, final Object msg3, final Object msg4, final Object msg5)
            throws PreconditionViolationException {
        assertPre(preconditionMet, msg1, msg2, msg3, msg4, msg5, null);
    }

    protected static void assertPre(final boolean preconditionMet, final Object msg1,
            final Object msg2, final Object msg3, final Object msg4, final Object msg5,
            final Object msg6) throws PreconditionViolationException {
        if (preconditionMet) {
            return;
        }
        StringBuffer sb = new StringBuffer("Precondition violation: ");
        appendMessages(msg1, msg2, msg3, msg4, msg5, msg6, sb);
        final String msg = sb.toString();
        throw new PreconditionViolationException(msg);
    }

    private static void appendMessages(final Object msg1, final Object msg2, final Object msg3,
            final Object msg4, final Object msg5, final Object msg6, StringBuffer sb) {
        if (msg1 != null) {
            sb.append(String.valueOf(msg1));
        }
        if (msg2 != null) {
            sb.append(String.valueOf(msg2));
        }
        if (msg3 != null) {
            sb.append(String.valueOf(msg3));
        }
        if (msg4 != null) {
            sb.append(String.valueOf(msg4));
        }
        if (msg5 != null) {
            sb.append(String.valueOf(msg5));
        }
        if (msg6 != null) {
            sb.append(String.valueOf(msg6));
        }
    }

    protected static void assertPost(final boolean postconditionMet, final Object msg1)
            throws PostconditionViolationException {
        assertPost(postconditionMet, msg1, null, null, null, null, null);
    }

    protected static void assertPost(final boolean postconditionMet, final Object msg1,
            final Object msg2) throws PostconditionViolationException {
        assertPost(postconditionMet, msg1, msg2, null, null, null, null);
    }

    protected static void assertPost(final boolean postconditionMet, final Object msg1,
            final Object msg2, final Object msg3) throws PostconditionViolationException {
        assertPost(postconditionMet, msg1, msg2, msg3, null, null, null);
    }

    protected static void assertPost(final boolean postconditionMet, final Object msg1,
            final Object msg2, final Object msg3, final Object msg4)
            throws PostconditionViolationException {
        assertPost(postconditionMet, msg1, msg2, msg3, msg4, null, null);
    }

    protected static void assertPost(final boolean postconditionMet, final Object msg1,
            final Object msg2, final Object msg3, final Object msg4, final Object msg5)
            throws PostconditionViolationException {
        assertPost(postconditionMet, msg1, msg2, msg3, msg4, msg5, null);
    }

    protected static void assertPost(final boolean postconditionMet, final Object msg1,
            final Object msg2, final Object msg3, final Object msg4, final Object msg5,
            final Object msg6) throws PostconditionViolationException {
        if (postconditionMet) {
            return;
        }
        StringBuffer sb = new StringBuffer("Postcondition violation: ");
        appendMessages(msg1, msg2, msg3, msg4, msg5, msg6, sb);
        final String msg = sb.toString();
        throw new PostconditionViolationException(msg);

    }
}