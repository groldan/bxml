package org.gvsig.bxml.stream.io;

import java.lang.ref.SoftReference;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
class Buffers {

    private static TreeSet<SoftReference<byte[]>> cache = new TreeSet<SoftReference<byte[]>>(
            new Comparator<SoftReference<byte[]>>() {
                public int compare(SoftReference<byte[]> o1, SoftReference<byte[]> o2) {
                    byte[] b1 = o1.get();
                    byte[] b2 = o2.get();
                    if (b1 == null && b2 == null) {
                        return 0;
                    } else if (b1 == null && b2 != null) {
                        return -1;
                    } else if (b2 == null && b1 != null) {
                        return 1;
                    }
                    return b2.length - b1.length;
                }
            });

    public static byte[] newByteArray(final int minimumSize) {
        SoftReference<byte[]> ref;
        byte[] buffer = null;
        synchronized (cache) {
            cache.add(new SoftReference<byte[]>(null));

            for (Iterator<SoftReference<byte[]>> buffers = cache.iterator(); buffers.hasNext();) {
                ref = buffers.next();
                buffer = ref.get();
                if (buffer == null) {
                    buffers.remove();
                    continue;
                }
                if (buffer.length >= minimumSize) {
                    buffers.remove();
                    break;
                }
                buffer = null;
            }
            if (buffer == null) {
                buffer = new byte[minimumSize];
            }
        }

        return buffer;
    }

    public static void returnArray(final byte[] buffer) {
        if (buffer != null) {
            synchronized (cache) {
                cache.add(new SoftReference<byte[]>(buffer));
            }
        }
    }
}
