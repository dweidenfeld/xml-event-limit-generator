# XMLEventLimitGenerator

## Description
The XmlEventLimitGenerator is a Java library to generate xml strings. You push documents (or objects, like you want) to
the xml generator and the generator proceeds the pushed document. While the push sequence it automatically generates an
average size of all documents. The benefit of this library is, that you can generate xml strings with a predefined max
size. This max size relates to the average document size, that is calculated on the fly, so there is no 100% guarantee,
that no xml string will pass the max size, but there is a factor that can be specified as buffer.

## Usage
Simply look at the test cases.

    final SomeWriter writer = ...;
    final XMLEventLimitGenerator<Document> generator = new XmlEventLimitGenerator<Document>(250, "documents", ..., writer);
    generator.push(Document.builder().id(1).title("Title").description("Description").build());
    generator.close();
    System.out.println(writer.toString());

will result in

    <?xml version="1.0" encoding="UTF-8"?>
    <documents>
        <document>
            <id>1</id>
            <title>Title</title>
            <description>Description</description>
        </document>
    </documents>

## License
The MIT License (MIT)

Copyright (c) 2014 Dominik Weidenfeld

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
