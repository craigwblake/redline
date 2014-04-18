package org.redline_rpm;

/**
 * Simple class to pair an int and a String with each other.
 */
public class IntString {

    private int theInt = 0;
    private String theString = "";

    public IntString (int theInt, String theString) {
        this.theInt = theInt;
        this.theString = theString;
    }

    public void setInt (int theInt) {
        this.theInt = theInt;
    }

    public int getInt () {
        return this.theInt;
    }

    public void setString (String theString) {
        this.theString = theString;
    }

    public String getString () {
        return this.theString;
    }

}
