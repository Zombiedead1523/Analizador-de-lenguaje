
package analizador;


public class fail {
    private int line;
    private String msg;

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public fail(int line, String msg) {
        this.line = line;
        this.msg = msg;
    }
}
