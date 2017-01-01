package ml.myll.mengyinnotifier;

/**
 * Created by will on 1/1/2017.
 */

public class MEvent {
    private String name;
    private int color;

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MEvent (String n, int c) {
        name = n;
        color = c;
    }
}
