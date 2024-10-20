package analizador;


public class pila {
    private int line;
    private String start;

    public pila(int line, String start) {
        this.line = line;
        this.start = start;
    }

    

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }
}
