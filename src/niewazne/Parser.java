package niewazne;

import java.util.*;

import java.io.File;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Parser {
    public static void main(String[] args){

        try {
            File inputFile = new File("E:\\Studia\\java\\map.osm");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            UserHandler userhandler = new UserHandler();
            saxParser.parse(inputFile, userhandler);
            userhandler.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class UserHandler extends DefaultHandler {

    public static Graph g = new Graph(); //?

    boolean bTag = false;
    boolean bWay = false;
    boolean hWay = false;
    boolean bBuild = false;
    boolean bAddr = false;

    String v = new String();
    String k = new String();
    String lat = new String();
    String lon = new String();
    String id = new String();
    String ref = new String();

    List<Node> points = new ArrayList<Node>();
    List<String> refs = new ArrayList<String>();

    @Override
    public void startElement(String uri,
                             String localName, String qName, Attributes attributes)
            throws SAXException {
        if (qName.equalsIgnoreCase("node")) {
            id = attributes.getValue("id");
            lat = attributes.getValue("lat");
            lon = attributes.getValue("lon");
            Node tmp = new Node(id, lat, lon);
            points.add(tmp);
        }
        if (qName.equalsIgnoreCase("way")){
            bWay = true;
        }
        if (qName.equalsIgnoreCase("nd") && bWay){
            ref = attributes.getValue("ref");
            refs.add(ref);
        }
        if (qName.equalsIgnoreCase("tag")) {
            bTag = true;
            k = attributes.getValue("k");
            v = attributes.getValue("v");
            if(k.equals("highway")) hWay = true;
            if(k.equals("building")) bBuild = true;
            if((k.length() > 5) && (k.substring(0, 5).equals("addr:"))) bAddr = true;
        }
    }

    @Override
    public void endElement(String uri,
                           String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("way")){
            bWay = false;
            bBuild = false;
            bAddr = false;
            hWay = false;
            refs.clear();
        }
    }

    @Override
    public void characters(char ch[],
                           int start, int length) throws SAXException {
        if(bTag && bWay && hWay && !bAddr && k.equals("name")) {

            g.addStreet(v);
            {
                List<Node> forStrNodes = new ArrayList<Node>();
                for (String r : refs) {
                    for (Node n : points) {
                        if (n.getId().equals(r)) {
                            Node tmpNode = new Node(n.getId(), n.getX(), n.getY());
                            forStrNodes.add(tmpNode);
                            break;
                        }
                    }
                }

                double nextDist = dist(forStrNodes);

                for (Street s : g.streets) {
                    if (s.getName().equals(v)) {
                        for (Node n :
                                forStrNodes) {
                            s.addN(n);
                        }
                        s.dist += nextDist;
                        break;
                    }
                }
            }


            bTag = false;
        }
    }

    public static double dist(List<Node> LN) {
        double dist = 0;
        for(int i = 0; i < LN.size() - 1; i++) {
            double val10, val11, val20, val21;

            val10 = Double.parseDouble(LN.get(i).getX());
            val11 = Double.parseDouble(LN.get(i+1).getX());
            val20 = Double.parseDouble(LN.get(i).getY());
            val21 = Double.parseDouble(LN.get(i+1).getY());

            dist += Math.sqrt((Math.pow((val10 - val11), 2)) + (Math.pow((val20 - val21), 2)));
        }
        dist = dist * 111196.672;
        return dist;
    }

    public void show() {

        System.out.println("Ile ulic: " + g.streetCounter);
        int j = 1;
        for (Street s:
                g.streets) {
            if(j < 10) System.out.print("0");
            System.out.print(j + ".: ul. " + s.getName() + ", długość w metrach: ");
            System.out.format("%.2f%n", s.getDist());
            for (Node n: s.nodes) {System.out.println("\t" + n.getX() + ", " + n.getY());}
            System.out.println();
            j++;
        }
        System.out.println();

        g.giveCrossing();

        System.out.println("Ile skrzyżowań: " + g.crossingCounter);
        int i = 1;
        for (Cross n:
                g.crossings) {
            if(i < 10) System.out.print("0");
            System.out.println(i + ".: " + n.getX() + ", " + n.getY() + ": ul. " + n.getS1() + " i ul. " + n.getS2());
            i++;
        }
        System.out.println(); System.out.println();
    }
}

////////////////////////////////////Graph/////////////////////////////////////////

class Node
{
    protected String id = new String(), x = new String(), y = new String();

    public Node(){
        id = "";
        x = "";
        y = "";
    }
    public Node(String id, String x, String y){
        this.id = id;
        this.x = x;
        this.y = y;
    }
    public String getX(){return this.x;}
    public String getY(){return this.y;}
    public String getId(){return this.id;}
}

class Cross extends Node{
    private String s1 = new String(), s2 = new String();
    public Cross(String id, String x, String y, String s1, String s2){
        this.id = id;
        this.x = x;
        this.y = y;
        this.s1 = s1;
        this.s2 = s2;
    }
    public String getS1(){return this.s1;}
    public String getS2(){return this.s2;}
}

class Street
{
    private String name = new String();
    public double dist;
    public List<Node> nodes = new ArrayList<Node>();

    public Street(String n){
        name = n;
    }
    public String getName(){
        return this.name;
    }
    public double getDist() { return this.dist; }

    public boolean addN(Node n) {
        for (Node v: nodes) {
            if(v.getX().equals(n.getX()) && v.getY().equals(n.getY())) return false;
        }
        nodes.add(n);
        return true;
    }
}

class Graph
{
    public int crossingCounter;
    public int streetCounter;
    public List<Cross> crossings = new ArrayList<Cross>();
    public List<Street> streets = new ArrayList<Street>();

    public Graph() {
        crossingCounter = 0;
        streetCounter = 0;
    }
    public boolean addCross(Cross c){
        for (Cross x:
                crossings) {
            if(c.getX().equals(x.getX()) && c.getY().equals(x.getY())) return false;
        }
        crossings.add(c);
        crossingCounter++;
        return true;
    }
    public boolean addStreet(String n){
        for (Street s:
                streets) {
            if(s.getName().equals(n)) return false;
        }
        Street newStreet = new Street(n);
        streets.add(newStreet);
        streetCounter++;
        return true;
    }

    public void giveCrossing() {
        for (Street s1: streets) {
            for (Street s2: streets) {
                for (Node n1: s1.nodes) {
                    for(Node n2: s2.nodes) {
                        if(n1.getX().equals(n2.getX()) && n1.getY().equals(n2.getY()) && s1 != s2) {
                            Cross crossing = new Cross(n1.getId(), n1.getX(), n1.getY(), s1.getName(), s2.getName());
                            addCross(crossing);
                            break;
                        }
                    }
                }
            }
        }
    }

}
