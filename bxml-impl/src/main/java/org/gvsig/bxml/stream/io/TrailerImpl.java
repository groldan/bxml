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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class TrailerImpl implements TrailerToken {

    private Set<StringTableIndexEntry> stringTableIndex;

    private Set<IndexTableIndexEntry> indexTableIndex;

    private long position;

    public TrailerImpl(long position) {
        this.position = position;
    }

    /**
     * @see org.gvsig.bxml.stream.io.TrailerToken#getPosition()
     */
    public long getPosition() {
        return position;
    }

    /**
     * @see org.gvsig.bxml.stream.io.TrailerToken#getIndexTableIndex()
     */
    public Set<IndexTableIndexEntry> getIndexTableIndex() {
        Set<IndexTableIndexEntry> table = null;
        if (indexTableIndex != null) {
            table = Collections.unmodifiableSet(indexTableIndex);
        }
        return table;
    }

    void addIndexTableIndexEntry(final IndexTableIndexEntry entry) {
        if (indexTableIndex == null) {
            indexTableIndex = new LinkedHashSet<IndexTableIndexEntry>();
        }
        indexTableIndex.add(entry);
    }

    /**
     * @see org.gvsig.bxml.stream.io.TrailerToken#getStringTableIndex()
     */
    public Set<StringTableIndexEntry> getStringTableIndex() {
        Set<StringTableIndexEntry> table = null;
        if (stringTableIndex != null) {
            table = Collections.unmodifiableSet(stringTableIndex);
        }
        return table;
    }

    void addStringTableIndexEntry(final StringTableIndexEntry entry) {
        if (stringTableIndex == null) {
            stringTableIndex = new LinkedHashSet<StringTableIndexEntry>();
        }
        stringTableIndex.add(entry);
    }
}
