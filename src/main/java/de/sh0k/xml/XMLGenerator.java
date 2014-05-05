package de.sh0k.xml;

/**
 * XML generator interface.
 */
public interface XMLGenerator<T> {

    /**
     * Push a new document to the xml stream.
     *
     * @param document the document
     * @throws XMLException if some elements does not fit correctly to the stream.
     */
    void push(final T document) throws XMLException;

    /**
     * Close all writers and flushes the last content (if there is some).
     *
     * @throws de.sh0k.xml.XMLException if the writers cannot be closed gracefully
     */
    void close() throws XMLException;
}
