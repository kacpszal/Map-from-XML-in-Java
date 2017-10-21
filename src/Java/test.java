package Java;

import java.util.*;
class X {
    static int counter = 0;
    int id = counter++;
    @Override
    public String toString() {
        return "" + id;
    }
}


public class test {

    public static void main(String[] args) {
        List<X> l = new ArrayList<X>();
        l.add(new X());
        l.add(new X());
        l.add(new X());
        List<X> l2 = new ArrayList<X>();
        System.out.println(l);
        for(int i = 0; i < l.size(); ++i) {
            l2.add(l.get(i));
        }
        System.out.println(l2);
        System.out.println(l);
        l2.remove(2);
        System.out.println(l);
        System.out.println(l2);
        System.out.println(l2.contains(l.get(0)));
    }

}
