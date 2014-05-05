package de.sh0k.xml;

import org.junit.Test;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class XmlEventLimitGeneratorTest {

    @Test
    public void testValidXml() throws Exception {
        final StringBuffer buffer = new StringBuffer(200);
        final XMLGenerator<Document> generator = newGenerator(200, buffer);
        generator.push(Document.builder().id(1).title("Title").description("Description").build());
        generator.close();
        assertEquals("xml must match",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<documents>" +
                        "<document><id>1</id><title>Title</title><description>Description</description></document>" +
                        "</documents>",
                buffer.toString());
    }

    @Test
    public void testValidXmlForTwo() throws Exception {
        final StringBuffer buffer = new StringBuffer(250);
        final XMLGenerator<Document> generator = newGenerator(250, buffer);
        generator.push(Document.builder().id(1).title("Title").description("Description").build());
        generator.push(Document.builder().id(2).title("Title").description("Description").build());
        generator.close();
        assertEquals("xml must match",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<documents>" +
                        "<document><id>1</id><title>Title</title><description>Description</description></document>" +
                        "<document><id>2</id><title>Title</title><description>Description</description></document>" +
                        "</documents>",
                buffer.toString());

    }

    @Test
    public void testValidXmlForTwoWithLimitExceeded() throws Exception {
        final StringBuffer buffer = new StringBuffer(200);
        final XMLGenerator<Document> generator = newGenerator(200, buffer);
        generator.push(Document.builder().id(1).title("Title").description("Description").build());
        generator.push(Document.builder().id(2).title("Title").description("Description").build());
        generator.close();
        assertEquals("xml must match",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<documents>" +
                        "<document><id>1</id><title>Title</title><description>Description</description></document>" +
                        "</documents>" +
                        "" +
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<documents>" +
                        "<document><id>2</id><title>Title</title><description>Description</description></document>" +
                        "</documents>",
                buffer.toString());

    }

    @Test
    public void testLimitExceeded() throws Exception {
        final StringBuffer buffer = new StringBuffer(250);
        final XMLGenerator<Document> generator = newGenerator(250, buffer);

        generator.push(Document.builder().id(1).title("Title").description("Description").build());
        assertEquals("buffer should be empty", 0, buffer.length());
        buffer.delete(0, buffer.length());

        generator.push(Document.builder().id(2).title("Title").description("Description").build());
        assertEquals("buffer should be empty", 0, buffer.length());
        buffer.delete(0, buffer.length());

        generator.push(Document.builder().id(3).title("Title").description("Description").build());
        assertNotEquals("buffer should not be empty", 0, buffer.length());
        buffer.delete(0, buffer.length());

        generator.close();
        assertNotEquals("buffer should not be empty", 0, buffer.length());
    }

    private XMLEventLimitGenerator<Document> newGenerator(final long maxSize, final StringBuffer buffer)
            throws XMLStreamException {
        return new XMLEventLimitGenerator<Document>(maxSize, "documents",
                new XMLEventLimitGenerator.DocumentMapper<Document>() {
                    @Override
                    public void map(final XMLEventWriter writer, final XMLEventFactory xmlEventFactory,
                                    final Document document) throws XMLStreamException {
                        writer.add(xmlEventFactory.createStartElement("", "", "document"));
                        writer.add(xmlEventFactory.createStartElement("", "", "id"));
                        writer.add(xmlEventFactory.createCharacters("" + document.getId()));
                        writer.add(xmlEventFactory.createEndElement("", "", "id"));
                        writer.add(xmlEventFactory.createStartElement("", "", "title"));
                        writer.add(xmlEventFactory.createCharacters(document.getTitle()));
                        writer.add(xmlEventFactory.createEndElement("", "", "title"));
                        writer.add(xmlEventFactory.createStartElement("", "", "description"));
                        writer.add(xmlEventFactory.createCharacters(document.getDescription()));
                        writer.add(xmlEventFactory.createEndElement("", "", "description"));
                        writer.add(xmlEventFactory.createEndElement("", "", "document"));
                    }
                }, new XMLEventLimitGenerator.DocumentWriter() {
            @Override
            public void write(final byte[] xml) {
                buffer.append(new String(xml));
            }
        }
        );
    }
}
