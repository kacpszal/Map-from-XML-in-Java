import java.util.*;

class Punkt {
    private double masa;

    public double getMasa() {
        return masa;
    }

    public Punkt() {
        masa = 0;
    }

    public Punkt(double masa) {
        this.masa = masa;
    }

    @Override public String toString() {
        return "Punkt materialny";
    }

    public double obliczGlownyMomentBezwladnosci() {
        return 0;
    }

    public double obliczSteiner(double d) {
        return (obliczGlownyMomentBezwladnosci() + (masa * d * d));
    }

    public void setMasa(double masa) {
        this.masa = masa;
    }
}

public class Zad1 {

    static void wypisz(Punkt p, double d) {
        System.out.println(p + ": masa = " + p.getMasa() + ", główny moment bezwładności = " + p.obliczGlownyMomentBezwladnosci());
        System.out.println("Moment bezwładności względem osi d = " + d + ": " + p.obliczSteiner(d));
    }

    static void wypiszBryle(BrylaSztywna bs) {
        System.out.println(bs + ", główny moment bezwładności = " + bs.obliczGlownyMomentBezwladnosci());
        System.out.println("Moment bezwładności względem osi d = 15.02: " + bs.obliczSteiner(15.02));
    }

    public static void main(String[] args) {
        Punkt p1 = new Punkt();
        Punkt p2 = new Punkt(2.16);
        wypisz(p1, 4.78);
        wypisz(p2, 19.23);
        p1.setMasa(35);
        wypisz(p1, 4.78);
        Punkt[] pTab = new Punkt[5];
        Random rand = new Random();
        for(int i = 0; i < pTab.length; ++i)
            pTab[i] = new Punkt(rand.nextDouble());
        for(Punkt p : pTab) {
            wypisz(p, rand.nextDouble());
        }

        // na bdb

        BrylaSztywna brylaSztywna1 = new BrylaSztywna();
        BrylaSztywna brylaSztywna2 = new BrylaSztywna(new Punkt(), new Punkt(10), new Punkt(54.22), new Punkt());
        System.out.println("NA BDB: ");
        wypiszBryle(brylaSztywna1);
        wypiszBryle(brylaSztywna2);

    }

}

//Pytania:
// 1. domyslnym dostępem bez nadania zadnego atrybutu dostępu jest dostęp pakietowy, pole lub metoda sa dostępne w calym pakiecie, w którym sie znajduje
// public - pole lub metoda dostepne dla kazdego
// protected - pole lub metoda ma dostęp pakietowy, a w dodatku dostęp do pola lub metody ma klasa pochodna dziedzicząca po klasie bazowej, w której znajduje się to pole lub metoda
// private - pole lub metoda jest dostępna tylko w klasie, w której występuje
// 2. akcesory, inaczej gettery są tworzone w celu zwrócenia wartości pola prywatnego; mutatory inaczej settery są tworzone w celu nadania innej wartości polu prywatnemu
// 3. konstruktory służą do prawidłowego zainicjowania nowo powstałego obiektu, rodzaje konstuktorów: domyślny, postaci NazwaKlasy() {} i sparametryzowany postaci NazwaKlasy(argumenty) {}
// 4. metody to wyodrębniony fragment kodu, zawierający instrukcje, zapisywany raz w programie, a wywoływany często wielokrotnie f(), f(int a, double b), f(float x, Object... objs), f(int[] tab)

//na bdb

class BrylaSztywna extends Punkt {
    ArrayList<Punkt> punkty = new ArrayList<>();

    BrylaSztywna() {}
    BrylaSztywna(Punkt... ps) {
        for(Punkt p : ps)
            punkty.add(p);
    }

    @Override public double obliczGlownyMomentBezwladnosci() {
        double result = 0;
        Random rand = new Random();
        for(Punkt p : punkty) {
            double tmp = rand.nextDouble();
            result += p.getMasa() * tmp * tmp; // tu powinna być jakaś prawdziwa odległość punktu do środka bryły sztywnej, ale ja losuje bo nie znam odległości
        }
        return result;
    }

    @Override public double obliczSteiner(double d) {
        double result = 0;
        double m = 0;
        for(Punkt p : punkty)
            m += p.getMasa();
        for(Punkt p : punkty)
            result += obliczGlownyMomentBezwladnosci() + (m * d * d);
        return result;
    }

    @Override public String toString() {
        return "Bryla sztywna";
    }
}