package net.sf.xapp.examples.school.model;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class Hat {
    private HatType type;
    private Colour colour;

    public HatType getType() {
        return type;
    }

    public void setType(HatType type) {
        this.type = type;
    }

    public Colour getColour() {
        return colour;
    }

    public void setColour(Colour colour) {
        this.colour = colour;
    }

    @Override
    public String toString() {
        return colour + " "+ type;
    }
}
