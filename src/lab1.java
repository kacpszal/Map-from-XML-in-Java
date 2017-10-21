import java.util.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.*;

public class lab1 {

    private static class OsmNameHandler extends DefaultHandler {
        private final Set<String> nameSet = new TreeSet<String>();
        private final Stack<String> eleStack = new Stack<String>();

        public Set<String> getNames() {
            return nameSet;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
            if("tag".equals(qName) && "way".equals(eleStack.peek())) {
                if("name".equals(attrs.getValue("k"))) {
                    String tmpName = attrs.getValue("v");
                    nameSet.add(tmpName);
                }
            }
            if("nd".equals(qName) && "way".equals(eleStack.peek())) {

            }
            eleStack.push(qName);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            eleStack.pop();
        }
    }

    private static void printWayNames(Set<String> nameSet, PrintStream out) {
        for(String name : nameSet) {
            out.println(name);
        }
    }

    public static void main(String[] args) {
        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            OsmNameHandler handler = new OsmNameHandler();

            saxParser.parse("E:\\Studia\\java\\map_mielec.osm", handler);
            printWayNames(handler.getNames(), System.out);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
