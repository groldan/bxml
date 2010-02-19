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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public final class StringTable {

    /**
     * A comparator for charsequences.
     */
    private static final Comparator<? super CharSequence> CHARSEQUENCE_COMPARATOR = new Comparator<CharSequence>() {
        public int compare(CharSequence o1, CharSequence o2) {
            if (o1 == o2) {
                return 0;
            }

            final int len1 = o1.length();
            final int len2 = o2.length();

            if (len1 == len2) {
                char c1;
                char c2;
                int lim = Math.min(len1, len2);
                int k = 0;
                while (k < lim) {
                    c1 = o1.charAt(k);
                    c2 = o2.charAt(k);
                    if (c1 != c2) {
                        return c1 - c2;
                    }
                    k++;
                }
            }
            return len1 - len2;
        }
    };

    private final List<String> strings;

    private final List<Long> offsets;

    /**
     * Map string entry/index for faster look ups of string index by name. As charsequence equals is
     * not consistent across implementations we use a special comparator.
     */
    private TreeMap<CharSequence, Long> indexMap = new TreeMap<CharSequence, Long>(
            CHARSEQUENCE_COMPARATOR);

    public StringTable() {
        strings = new ArrayList<String>(100);
        offsets = new ArrayList<Long>(100);
    }

    /**
     * Adds a string to the string table, if not already present, and returns the string table entry
     * index.
     * 
     * @param string
     * @param position
     *            the position in the stream where the string table entry for this string is stored
     * @return the index, >= 0, corresponding to the given string
     */
    public long add(final CharSequence string, final long position) {
        long index = get(string);
        if (index == -1) {
            index = Integer.valueOf(strings.size());

            // ensure we store a COPY of the charsequence
            final String entry = string.toString();
            strings.add(entry);
            offsets.add(Long.valueOf(position));
            indexMap.put(entry, Long.valueOf(index));
        }
        return index;
    }

    public String get(final long index) throws IndexOutOfBoundsException {
        String string = strings.get((int) index);
        return string;
    }

    public long getOffset(final int index) {
        Long offset = offsets.get(index);
        return offset.longValue();
    }

    /**
     * Returns the index of the given string or {@code -1}
     * 
     * @param stringToHandle
     * @return the index of the given string in the StringTable, or {@code -1} if there are no entry
     *         for the string
     */
    public long get(final CharSequence stringToHandle) {
        Long index = indexMap.get(stringToHandle);
        return index == null ? -1 : index.longValue();
    }

    public int size() {
        return strings.size();
    }
}