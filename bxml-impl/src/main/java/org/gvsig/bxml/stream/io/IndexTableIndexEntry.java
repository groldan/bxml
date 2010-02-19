package org.gvsig.bxml.stream.io;

/**
 * <pre>
 * &lt;code&gt;
 *  IndexTableIndexEntry {  // entry for index
 *     String xpathExpr;    // XPath expression that is indexed
 *     Count fileOffset;    // file offset of index-table token
 *  }
 * &lt;/code&gt;
 * </pre>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public interface IndexTableIndexEntry {

    public String getXpathExpression();

    public long getFileOffset();
}
