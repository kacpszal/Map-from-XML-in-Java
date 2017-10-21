import static mylib.Print.*;
import static mylib.Range.*;

class A {
    public void f() { System.out.println("A.f()"); }
}

interface B {
    void f();
}

class C extends A implements B {
    public void f() { System.out.println("C.f()"); }
}

public class TestowyPrint {

    public static void main(String[] args) {
        C c = new C();
        c.f();
        A a = new C();
        a.f();
        B b = new C();
        b.f();
        B bb = (B)(new C());
        bb.f();
    }

}
