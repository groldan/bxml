package org.gvsig.bxml.stream.io;

/**
 * Models an index table entry for the trailer token.
 * <p>
 * The encoding structure of such an entry goes like this:
 * 
 * <pre>
 * &lt;code&gt;
 *  StringTableIndexEntry {     // string-table index fragment
 *     Count nStringsDefined;   // number of strings defined in frag.
 *     Count fileOffset;        // file offset to string-table token
 *  }
 * &lt;/code&gt;
 * </pre>
 * 
 * </p>
 * <p>
 * NOTE: The stringCount property seems redundant: at reading time once a StringTable fragment is
 * located through the fileOffset, the string count is just the next bit of information after the
 * StringTableToken, and since its length is variable depending on the count type, you have to
 * either skip it by calculating the count byte-length from the stringCount property or do a
 * straight read of the string count from the stream. Yet, we're including this property for the
 * interface in order to collect that information during writing and being able to comply with the
 * encoding format.
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public interface StringTableIndexEntry {

    public long getFileOffset();

    public long getStringCount();
}
