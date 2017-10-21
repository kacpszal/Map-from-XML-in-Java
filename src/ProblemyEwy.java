interface Value {
    int getValue();
}

public class ProblemyEwy {

    public static void main(String[] args) {
        Value v = new Value() {
            @Override
            public int getValue() {
                return 42;
            }
        };
        System.out.println(v.getValue());
    }

}