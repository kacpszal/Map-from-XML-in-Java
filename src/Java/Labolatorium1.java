package Java;

import java.util.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

class Verticle implements Serializable {
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

class Edge implements Serializable {
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
    public List<Verticle> getVerticles() { return verticleRefs; }

    String refNode = "";
    String x = "";
    String y = "";

    boolean bTag = false;
    boolean bHighway = false;
    boolean noName = false;
    boolean bFootWay = false;

    int noNameCounter = 0;

    double frameX1, frameY1, frameX2, frameY2;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {

        if("bounds".equals(qName)) {
            frameX1 = Double.parseDouble(attrs.getValue("minlat"));
            frameY1 = Double.parseDouble(attrs.getValue("minlon"));
            frameX2 = Double.parseDouble(attrs.getValue("maxlat"));
            frameY2 = Double.parseDouble(attrs.getValue("maxlon"));
        }

        if (qName.equalsIgnoreCase("node")) {
            refNode = attrs.getValue("id");
            x = attrs.getValue("lat");
            y = attrs.getValue("lon");
            verticleRefs.add(new Verticle(refNode, x, y));
        }

        if("tag".equals(qName) && "way".equals(eleStack.peek()) && "highway".equals(attrs.getValue("k")))
            bHighway = true;

        if("tag".equals(qName) && "way".equals(eleStack.peek()) && "footway".equals(attrs.getValue("k")))
            bFootWay = true;

        if("tag".equals(qName) && "way".equals(eleStack.peek()) && bHighway) {
            if("name".equals(attrs.getValue("k"))) {
                bTag = true;
                noName = false;
                tmpName = attrs.getValue("v");
                nameSet.add(tmpName);
                currentName = tmpName;
                if(previousName.equals(""))
                    previousName = currentName;
            }
            noName = true;
        }

        if("nd".equals(qName) && "way".equals(eleStack.peek())) {
            String tmp = attrs.getValue("ref");
            for(Verticle v : verticleRefs)
                if(v.ref.equals(tmp)) {
                    verticles.add(v);
                    break;
                }
        }

        eleStack.push(qName);

    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equalsIgnoreCase("way") && bTag) {
            List<Verticle> tmpList = new ArrayList<Verticle>();
            tmpList.addAll(verticles);
            edges.add(new Edge(tmpList, verticles.get(0), verticles.get(verticles.size() - 1), currentName));
            verticles.clear();
        }
        if(qName.equalsIgnoreCase("way") && noName && !bFootWay && bHighway) {
            List<Verticle> tmpList = new ArrayList<Verticle>();
            tmpList.addAll(verticles);
            if(verticles.size() > 0)
                edges.add(new Edge(tmpList, verticles.get(0), verticles.get(verticles.size() - 1), "No Name nr " + ++noNameCounter));
            verticles.clear();
        }
        eleStack.pop();
        if(qName.equalsIgnoreCase("way")) {
            bTag = false;
            verticles.clear();
            bHighway = false;
            bFootWay = false;
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

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("\nSKRZYŻOWANIA:\n\n");
        for(Verticle v : g.keySet()) {
            result.append("Skrzyzowanie nr " + ++counter + " o wspolrzednych: " + v + "\n");
            for (Edge e : g.get(v))
                result.append("     " + e.name + "\n");
        }
        result.append("\nLiczba skrzyżowań to " + counter + ".\n");
        counter = 0;
        return result.toString();
    }

    public void toFile(PrintStream out) {
        /*out.println();
        out.println("SKRZYŻOWANIA: ");
        out.println();
        for(Verticle v : g.keySet()) {
            out.println("Skrzyzowanie nr " + ++counter + " o wspolrzednych: " + v);
            for (Edge e : g.get(v))
                out.println("     " + e.name);
        }
        out.println();
        out.println("Liczba skrzyżowań to " + counter + ".");
        counter = 0;*/

    }

}

public class Labolatorium1 {
    public static void printToFile(OsmNameHandler handler, PrintStream out) {
        out.println("#bounds");
        out.println(handler.frameX1 + ":" + handler.frameY1 + ":" + handler.frameX2 + ":" + handler.frameY2);
        out.println("#node");
        int counter = 0;
        for(Verticle v : handler.getVerticles())
            out.println("" + counter++ + " " + v.ref + ":" + v.x + ":" + v.y);
        out.println("way");
        for(Edge e : handler.getEdges()) {
            out.print(e.name + ":");
            for(Verticle v : e.allVerticles) {
                out.print(v.ref + ":");
            }
            out.println();
        }
    }

    private static void printWayNames(Set<String> nameSet, PrintStream out) {
        out.println("ULICE:");
        out.println();
        out.println("Liczba unikalnych ulic: " + nameSet.size());
        out.println();
        for(String name : nameSet)
            out.println(name);
    }

    private static void printEdges(List<Edge> edges, PrintStream out) {
        out.println();
        out.println("WSPÓŁRZĘDNE I DŁUGOŚCI DRÓG:");
        out.println();
        out.println("Liczba wszystkich dróg (niektóre ulice są podzielone na części): " + edges.size());
        out.println();
        for(Edge e : edges)
            out.println(e);
    }

    public static void makeCrossings(List<Edge> edges, Graph graph, OsmNameHandler handler) {
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

            //saxParser.parse("11listopad.osm", handler);
            saxParser.parse("krakow_centrum.osm", handler);
            //printWayNames(handler.getNames(), System.out);
            makeDistance(handler.getEdges());
            //printEdges(handler.getEdges(), System.out);
            Graph graph = new Graph();
            makeCrossings(handler.getEdges(), graph, handler);
            //System.out.println("\n" + graph);



            // zapis do pliku
            File plik = new File("result.txt");
            PrintStream save = new PrintStream(plik.getName());
            //printWayNames(handler.getNames(), save);
            //printEdges(handler.getEdges(), save);
            //graph.toFile(save);
            printToFile(handler, save);

            // Serializacja

            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("serializacjaGraf.bin"))) {
                outputStream.writeObject(handler.frameX1);
                outputStream.writeObject(handler.frameY1);
                outputStream.writeObject(handler.frameX2);
                outputStream.writeObject(handler.frameY2);
                outputStream.writeObject(handler.getVerticles());
                outputStream.writeObject(handler.getEdges());
            }

            // end of Serializacja

            MyFrame m = new MyFrame(handler.frameX1, handler.frameY1, handler.frameX2, handler.frameY2, handler.getEdges(), graph, handler);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //Dijkstra

    public static double dist(Verticle v1, Verticle v2) {
        return Math.sqrt((Math.pow(Double.parseDouble(v2.x) - Double.parseDouble(v1.x), 2)) + (Math.pow(Double.parseDouble(v2.y) - Double.parseDouble(v1.y), 2))) * 111196.672;
    }

    public static List<Verticle> dijkstra(Graph g, Verticle start, Verticle end, OsmNameHandler handler) {
        List<Verticle> result = new ArrayList<Verticle>();
        double[] d = new double[handler.getVerticles().size()];
        Verticle[] predecessor = new Verticle[handler.getVerticles().size()];
        Queue<Verticle> q = new LinkedList<Verticle>();
        for(int i = 0; i < handler.getVerticles().size(); ++i) {
            d[i] = Double.POSITIVE_INFINITY;
            predecessor[i] = null;
        }
        d[handler.getVerticles().indexOf(start)] = 0;
        q.offer(start);
        while(!q.isEmpty()) {
            Verticle v = q.poll();
            for(Edge e : handler.getEdges()) {
                int position = e.allVerticles.indexOf(v);
                int posV = handler.getVerticles().indexOf(v);
                if(position > 0) {
                    int posMinusOne = handler.getVerticles().indexOf(e.allVerticles.get(position - 1));
                    Verticle posMinusOne2 = e.allVerticles.get(position - 1);
                    if(d[posMinusOne] > d[posV] +
                            dist(posMinusOne2, v)) {
                        d[posMinusOne] = d[posV] + dist(posMinusOne2, v);
                        predecessor[posMinusOne] = v;
                        q.offer(posMinusOne2);
                    }
                }
                if(position < e.allVerticles.size() - 1 && position >= 0) {
                    int posPlusOne = handler.getVerticles().indexOf(e.allVerticles.get(position + 1));
                    Verticle posPlusOne2 = e.allVerticles.get(position + 1);
                    if(d[posPlusOne] > d[posV] +
                            dist(posPlusOne2, v)) {
                        d[posPlusOne] = d[posV] + dist(posPlusOne2, v);
                        predecessor[posPlusOne] = v;
                        q.offer(posPlusOne2);
                    }
                }
            }
            System.out.println(q.size());

        }
        System.out.println("\nDroga z pkt. A do B to: " + d[handler.getVerticles().indexOf(end)] + "\n");
        Verticle tmp = end;
        System.out.println(tmp);
        result.add(tmp);
        if(d[handler.getVerticles().indexOf(end)] != Double.POSITIVE_INFINITY)
            while(tmp != start) {
                tmp = predecessor[handler.getVerticles().indexOf(tmp)];
                System.out.println(tmp);
                result.add(tmp);
            }

        return result;

    }

    public static List<Verticle> aStar(Graph g, Verticle start, Verticle end, OsmNameHandler handler) {
        double[] fScore = new double[handler.getVerticles().size()];
        for(int i = 0; i < fScore.length; ++i)
            fScore[i] = Double.POSITIVE_INFINITY;
        double[] gScore = new double[handler.getVerticles().size()];
        double[] hScore = new double[handler.getVerticles().size()];
        double tentativeGScore;
        boolean tentativeIsBetter;
        int[] cameFrom = new int[handler.getVerticles().size()];
        for(int i = 0; i < cameFrom.length; ++i)
            cameFrom[i] = -1;
        List<Verticle> closedSet = new LinkedList<Verticle>();
        List<Verticle> openSet = new ArrayList<Verticle>();
        boolean[] openBoolean = new boolean[handler.getVerticles().size()];
        openSet.add(start);
        openBoolean[handler.getVerticles().indexOf(start)] = true;
        gScore[handler.getVerticles().indexOf(start)] = 0;
        while (!openSet.isEmpty()) {
            double minF = -1;
            int minFIndex = -1;
            for(int i = 0; i < fScore.length; ++i) {
                if(openBoolean[i]) {
                    minF = fScore[i];
                    minFIndex = i;
                    break;
                }
            }
            for (int i = 0; i < fScore.length; ++i) {
                if (fScore[i] < minF && openBoolean[i]) {
                    minF = fScore[i];
                    minFIndex = i;
                }
            }
            if (minFIndex == handler.getVerticles().indexOf(end))
                return reconstructPath(cameFrom, handler.getVerticles().indexOf(end), handler);
            Verticle v = handler.getVerticles().get(minFIndex);
            openSet.remove(v);
            closedSet.add(v);
            openBoolean[minFIndex] = false;

            for (Edge e : handler.getEdges()) {
                int position = e.allVerticles.indexOf(v);
                int posV = handler.getVerticles().indexOf(v);
                if (position > 0) {
                    int posMinusOne = handler.getVerticles().indexOf(e.allVerticles.get(position - 1));
                    Verticle posMinusOne2 = e.allVerticles.get(position - 1);/////
                    if (closedSet.contains(posMinusOne2)) {

                    } else {
                        tentativeGScore = gScore[posV] + dist(v, posMinusOne2);
                        tentativeIsBetter = false;
                        if (!openSet.contains(posMinusOne2)) {
                            openSet.add(posMinusOne2);
                            openBoolean[posMinusOne] = true;
                            hScore[posMinusOne] = dist(posMinusOne2, end);
                            tentativeIsBetter = true;
                        } else if (tentativeGScore < gScore[posMinusOne])
                            tentativeIsBetter = true;
                        if (tentativeIsBetter) {
                            cameFrom[posMinusOne] = posV;
                            gScore[posMinusOne] = tentativeGScore;
                            fScore[posMinusOne] = gScore[posMinusOne] + hScore[posMinusOne];
                        }
                    }
                }
                if (position < e.allVerticles.size() - 1 && position >= 0) {
                    int posPlusOne = handler.getVerticles().indexOf(e.allVerticles.get(position + 1));
                    Verticle posPlusOne2 = e.allVerticles.get(position + 1);//////
                    if (closedSet.contains(posPlusOne2)) {

                    } else {
                        tentativeGScore = gScore[posV] + dist(v, posPlusOne2);
                        tentativeIsBetter = false;
                        if (!openSet.contains(posPlusOne2)) {
                            openSet.add(posPlusOne2);
                            openBoolean[posPlusOne] = true;
                            hScore[posPlusOne] = dist(posPlusOne2, end);
                            tentativeIsBetter = true;
                        } else if (tentativeGScore < gScore[posPlusOne])
                            tentativeIsBetter = true;
                        if (tentativeIsBetter) {
                            cameFrom[posPlusOne] = posV;
                            gScore[posPlusOne] = tentativeGScore;
                            fScore[posPlusOne] = gScore[posPlusOne] + hScore[posPlusOne];
                        }
                    }
                }

            }
        }
        return null;
    }

    public static List<Verticle> reconstructPath(int[] cameFrom, int end, OsmNameHandler handler) {
        List<Verticle> result = new ArrayList<Verticle>();
        if(cameFrom.length > 0) {
            result.add(handler.getVerticles().get(end));
            while(true) {
                if(cameFrom[end] == -1)
                    break;
                result.add(handler.getVerticles().get(cameFrom[end]));
                end = cameFrom[end];
            }
        }
        else
            System.out.println("Brak sciezki.");
        return result;
    }
}

class MyFrame extends JFrame {
    public MyFrame(double x1, double y1, double x2, double y2, List<Edge> edges, Graph g, OsmNameHandler handler) {
        super("Kacper Szalwa - Mapa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        java.awt.Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        JPanel panel = new MyPanel(x1, y1, x2, y2, edges, d.width, d.height, g, handler);
        add(panel);
        setVisible(true);
        setSize(d.width, d.height);
    }
}

class MyPanel extends JPanel {
    double x1, y1, x2, y2, width, height;
    boolean dijkstra = false;
    boolean star = false;
    List<Edge> edges;
    Graph graph;
    OsmNameHandler handler;
    public MyPanel(double x1, double y1, double x2, double y2, List<Edge> edges, double width, double height, Graph g, OsmNameHandler handler) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.edges = edges;
        this.width = width;
        this.height = height;
        setPreferredSize(new java.awt.Dimension(400, 400));
        graph = g;
        this.handler = handler;
    }
    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        final double scale = width / (y2 - y1);
        final double scale1 = height / (x2 - x1);
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
        for(Edge e : edges) {
            List<Verticle> verticleList = new ArrayList<Verticle>(e.allVerticles);
            Verticle v1 = null;
            Verticle v2 = null;
            int counter = 0;
            for(Verticle v : e.allVerticles) {
                if(counter == 0) {

                }
                else {
                    v2 = v1;
                }
                v1 = verticleList.get(counter++);
                if(v1 != null && v2 != null) {
                    java.awt.geom.Line2D.Double line =
                            new java.awt.geom.Line2D.Double((Double.parseDouble(v1.y) - y1) * scale, height - (Double.parseDouble(v1.x) - x1) * scale1,
                                    (Double.parseDouble(v2.y) - y1) * scale, height - (Double.parseDouble(v2.x) - x1) * scale1);
                    g2d.draw(line);
                }
                /*java.awt.geom.Ellipse2D.Double p = new java.awt.geom.Ellipse2D.Double((Double.parseDouble(v.y) - y1) * scale - 2.5,
                        height - (Double.parseDouble(v.x) - x1) * scale1 - 2.5, 5.0, 5.0);
                g2d.fill(p);*/ // to do kropek na skrzyzowaniach i laczeniach drog
            }
        }
        /*if(!dijkstra) {
            List<Verticle> result = Labolatorium1.dijkstra(graph, handler.getVerticles().get(10), handler.getVerticles().get(30), handler); // dijkstra!!!
            System.out.println("" + handler.getVerticles().get(10) + " " + handler.getVerticles().get(30));
            dijkstra = true;
            Verticle v1 = null;
            Verticle v2 = null;
            int counter = 0;
            for(Verticle v : result) {
                if(counter == 0) {

                }
                else {
                    v2 = v1;
                }
                v1 = result.get(counter++);
                if(v1 != null && v2 != null) {
                    java.awt.geom.Line2D.Double line =
                            new java.awt.geom.Line2D.Double((Double.parseDouble(v1.y) - y1) * scale, height - (Double.parseDouble(v1.x) - x1) * scale1,
                                    (Double.parseDouble(v2.y) - y1) * scale, height - (Double.parseDouble(v2.x) - x1) * scale1);
                    g.setColor(java.awt.Color.GREEN);
                    g2d.draw(line);
                }
                g.setColor(java.awt.Color.GREEN);
                java.awt.geom.Ellipse2D.Double p = new java.awt.geom.Ellipse2D.Double((Double.parseDouble(v.y) - y1) * scale - 2.5,
                        height - (Double.parseDouble(v.x) - x1) * scale1 - 2.5, 5.0, 5.0);
                g2d.fill(p);
            }
        }*/
        /*if(!star) {
            List<Verticle> result = Labolatorium1.aStar(graph, handler.getVerticles().get(10), handler.getVerticles().get(30), handler); // aStar !!!!!!
            System.out.println("" + handler.getVerticles().get(10) + " " + handler.getVerticles().get(30));
            star = true;
            Verticle v1 = null;
            Verticle v2 = null;
            int counter = 0;
            for(Verticle v : result) {
                if(counter == 0) {

                }
                else {
                    v2 = v1;
                }
                v1 = result.get(counter++);
                if(v1 != null && v2 != null) {
                    java.awt.geom.Line2D.Double line =
                            new java.awt.geom.Line2D.Double((Double.parseDouble(v1.y) - y1) * scale, height - (Double.parseDouble(v1.x) - x1) * scale1,
                                    (Double.parseDouble(v2.y) - y1) * scale, height - (Double.parseDouble(v2.x) - x1) * scale1);
                    g.setColor(java.awt.Color.RED);
                    g2d.draw(line);
                }
                g.setColor(java.awt.Color.RED);
                java.awt.geom.Ellipse2D.Double p = new java.awt.geom.Ellipse2D.Double((Double.parseDouble(v.y) - y1) * scale - 2.5,
                        height - (Double.parseDouble(v.x) - x1) * scale1 - 2.5, 5.0, 5.0);
                g2d.fill(p);
            }
        }*/
    }
}

























