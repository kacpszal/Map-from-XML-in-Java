import java.util.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.*;

class Verticle {
    String ref;
    String x, y;
    public Verticle(String ref) {
        this.ref = ref;
    }
    public Verticle(String ref, String x, String y) {
        this.ref = ref;
        this.x = x;
        this.y = y;
    }
    @Override public String toString() {
        return "" + x + "   " + y;
    }
}

class Edge {
    Verticle x, y;
    double distance = 0;
    String name;
    List<Verticle> allVerticles;
    public Edge(String a, String b, String name) {
        x = new Verticle(a);
        y = new Verticle(b);
        this.name = name;
    }
    public Edge(Verticle a, Verticle b, String name) {
        x = a;
        y = b;
        this.name = name;
    }
    public Edge(List<Verticle> allVerticles, Verticle a, Verticle b, String name) {
        x = a;
        y = b;
        this.name = name;
        this.allVerticles = new ArrayList<Verticle>(allVerticles);
    }
    @Override public String toString() {
        return x + " - " + y + " ulica " + name + ", długość: " + distance + " metrów.";
    }
}

class OsmNameHandler extends DefaultHandler {
    private final Set<String> nameSet = new TreeSet<String>();
    private final Stack<String> eleStack = new Stack<String>();
    private String tmpName = "";
    private List<Verticle> verticles = new ArrayList<Verticle>();
    private String previousName = "", currentName = "";
    private List<Edge> edges = new ArrayList<Edge>();
    private List<Verticle> verticleRefs = new ArrayList<Verticle>();

    public Set<String> getNames() {
        return nameSet;
    }
    public List<Edge> getEdges() { return edges; }
    public List<Verticle> getVerticlesWithCoordinates() {
        return verticleRefs;
    }

    String refNode = "";
    String x = "";
    String y = "";

    boolean bTag = false;
    boolean bHighway = false;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {

        if (qName.equalsIgnoreCase("node")) {
            refNode = attrs.getValue("id");
            x = attrs.getValue("lat");
            y = attrs.getValue("lon");
            verticleRefs.add(new Verticle(refNode, x, y));
        }

        if("tag".equals(qName) && "way".equals(eleStack.peek()) && "highway".equals(attrs.getValue("k")))
            bHighway = true;

        /*if("tag".equals(qName) && "way".equals((eleStack.peek()))) {
            if("building".equals(attrs.getValue("k")) || "area".equals(attrs.getValue("k")) || "boundary".equals(attrs.getValue("k")) ||
                    "lit".equals(attrs.getValue("k")) || "landuse".equals(attrs.getValue("k")) || "leisure".equals(attrs.getValue("k"))
                    || "amenity".equals(attrs.getValue("k")) || "historic".equals(attrs.getValue("k"))
                    || ("type".equals(attrs.getValue("k")) && "parking_fee".equals(attrs.getValue("v"))))
                bBuilding = true;
        }*/

        if("tag".equals(qName) && "way".equals(eleStack.peek()) && bHighway) {
            if("name".equals(attrs.getValue("k"))) {
                bTag = true;
                tmpName = attrs.getValue("v");
                nameSet.add(tmpName);
                currentName = tmpName;
                if(previousName.equals(""))
                    previousName = currentName;
            }
        }

        if("nd".equals(qName) && "way".equals(eleStack.peek())) {
            //verticles.add(new Verticle(attrs.getValue("ref"))); //!!!!!!!!!!!!!!!!!!!!!
            String tmp = attrs.getValue("ref");
            for(Verticle v : verticleRefs)
                if(v.ref.equals(tmp)) {
                    verticles.add(v);
                    break;
                }
            /*if(!currentName.equals(previousName)) {
                previousName = currentName;
                if(!verticles.peekFirst().ref.equals(verticles.peekLast()))
                    edges.add(new Edge(verticles.pollFirst(), verticles.pollLast(), currentName));
                verticles.clear();
                isEdgeAdd = true;
            }*/
        }

        eleStack.push(qName);

    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equalsIgnoreCase("way") && bTag) {
            /*if(!verticles.peekFirst().ref.equals(verticles.peekLast())) {
                List<Verticle> tmpList = new ArrayList<Verticle>();
                tmpList.addAll(verticles);
                edges.add(new Edge(tmpList, verticles.pollFirst(), verticles.pollLast(), currentName));
            }*/
            /*Verticle first = verticles.get(0);
            Verticle last = verticles.get(verticles.size());
            if(!first.ref.equals(verticles.iterator(verticles.size() - 1))) {*/
                List<Verticle> tmpList = new ArrayList<Verticle>();
                tmpList.addAll(verticles);
                edges.add(new Edge(tmpList, verticles.get(0), verticles.get(verticles.size() - 1), currentName));
            //}
            verticles.clear();
        }
        eleStack.pop();
        if(qName.equalsIgnoreCase("way")) {
            bTag = false;
            verticles.clear();
            bHighway = false;
        }
    }
}

class Graph {
    HashMap<Verticle, HashSet<Edge>> g = new HashMap<Verticle, HashSet<Edge>>();
    long counter = 0;
    void add(Verticle v, Edge e) {
        HashSet<Edge> setTmp = g.get(v);
        if(setTmp != null)
            setTmp.add(e);
        else {
            setTmp = new HashSet<Edge>();
            setTmp.add(e);
        }
        g.put(v, setTmp);
    }

    /*void del(Verticle v, Edge e) {
        HashSet<Edge> setTmp = g.get(v);
        g.remove(v, setTmp);
    }*/

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for(Verticle v : g.keySet()) {
            result.append("Skrzyzowanie nr " + ++counter + " o wspolrzednych: " + v + "\n");
            for (Edge e : g.get(v))
                result.append("     " + e.name + "\n");
        }
        counter = 0;
        return result.toString();
    }

    public void toFile(PrintStream out) {
        for(Verticle v : g.keySet()) {
            out.println("Skrzyzowanie nr " + ++counter + " o wspolrzednych: " + v + "\n");
            for (Edge e : g.get(v))
                out.println("     " + e.name + "\n");
        }
        counter = 0;
    }

}

public class lab1_innepodejscie {

    private static void printWayNames(Set<String> nameSet, PrintStream out) {
        out.println("ULICE:\n");
        for(String name : nameSet)
            out.println(name);
    }

    private static void printEdges(List<Edge> edges, PrintStream out) {
        out.println("\nWSPOLRZEDNE I DLUGOSC DROG:\n");
        for(Edge e : edges)
            out.println(e);
    }

    /*public static void addCoordinates(LinkedList<Edge> edges, LinkedList<Verticle> verticleRefs) {
        for(Edge e : edges) {
            for(Verticle v : verticleRefs) {
                if(e.x.ref.equals(v.ref)) {
                    e.x.x = v.x;
                    e.x.y = v.y;
                }
                if(e.y.ref.equals(v.ref)) {
                    e.y.x = v.x;
                    e.y.y = v.y;
                }
            }
        }
    }*/

    /*public static void addCoordinates2(LinkedList<Verticle> verticles, LinkedList<Verticle> verticleRefs) {
            for(Verticle v1 : verticles) {
                for(Verticle v2 : verticleRefs) {
                    if(v1.ref.equals(v2.ref)) {
                        v1.x = v2.x;
                        v1.y = v2.y;
                    }
                }
            }
    }*/

    public static void makeCrossings(List<Edge> edges, Graph graph, OsmNameHandler handler) {
        /*int counter;
        int itcounter = 0;
        for(Edge e : edges) {
            addCoordinates2(e.allVerticles, handler.getVerticlesWithCoordinates());
            ++itcounter;
            for(Verticle v : e.allVerticles) {
                counter = 0;
                Edge tmp = e;
                ListIterator<Edge> it = edges.listIterator(itcounter);
                while(it.hasNext()) {
                    tmp = it.next();
                    for(Verticle vv : tmp.allVerticles){
                        if(vv.ref.equals(v.ref)) {
                            //System.out.println(tmp + " " + v);
                            ++counter;
                            graph.add(v, tmp);
                        }
                    }
                }
                if(counter == 1)
                    graph.del(v, tmp);
            }
        }*/
        /*for(Edge e : edges)
            addCoordinates2(e.allVerticles, handler.getVerticlesWithCoordinates());*/
        for(Edge e : edges) {
            for(Edge ee : edges) {
                for(Verticle v : e.allVerticles) {
                    for(Verticle vv : ee.allVerticles) {
                        if(v.ref.equals(vv.ref) && e != ee)
                            graph.add(v, ee);
                    }
                }
            }
        }
    }

    public static void makeDistance(Collection<Edge> c) {
        double x1, x2, y1, y2;
        for(Edge e : c) {
            for(int i = 0; i < e.allVerticles.size() - 1; ++i) {
                x1 = Double.parseDouble(e.allVerticles.get(i).x);
                y1 = Double.parseDouble(e.allVerticles.get(i).y);
                x2 = Double.parseDouble(e.allVerticles.get(i + 1).x);
                y2 = Double.parseDouble(e.allVerticles.get(i + 1).y);
                e.distance += Math.sqrt((Math.pow(x2 - x1, 2)) + (Math.pow(y2 - y1, 2))) * 111196.672;
            }
        }
    }

    public static void main(String[] args) {
        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            OsmNameHandler handler = new OsmNameHandler();

            saxParser.parse("E:\\Studia\\java\\map.osm", handler);
            //addCoordinates(handler.getEdges(), handler.getVerticlesWithCoordinates());
            printWayNames(handler.getNames(), System.out);
            makeDistance(handler.getEdges());
            printEdges(handler.getEdges(), System.out);
            Graph graph = new Graph();
            makeCrossings(handler.getEdges(), graph, handler);
            System.out.println("\n" + graph);

            // zapis do pliku
            File plik = new File("result.txt");
            PrintStream save = new PrintStream(plik.getName());
            printWayNames(handler.getNames(), save);
            printEdges(handler.getEdges(), save);
            graph.toFile(save);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}