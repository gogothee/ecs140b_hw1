// taken from the book (p192-193, Code 8.11)
//   Object-Oriented Programming with Java: An Introduction
//   David J. Barnes
//   Prentice Hall
//   2000

// methods to position or center a String within a given width.
public class StringFormatter {
    // pad out s with spaces after it.
    public static String leftAdjust(String s, int width) {
        final int stringLength = s.length();
        if (stringLength >= width) {
            // this case works when width is negative, too.
            return s;
        }
        else {
            return spaces(width-stringLength).insert(0,s).toString();
        }
    }
    // pad out s with spaces before it.
    public static String rightAdjust(String s, int width) {
        final int stringLength = s.length();
        if (stringLength >= width) {
            // this case works when width is negative, too.
            return s;
        }
        else {
            return spaces(width-stringLength).append(s).toString();
        }
    }
    public static String center(String s, int width) {
        final int stringLength = s.length();
        if (stringLength >= width) {
            // this case works when width is negative, too.
            return s;
        }
        else {
            final int numSpaces  = width - stringLength;
            final int leftSpace  = numSpaces/2,
                      rightSpace = numSpaces-leftSpace;
            return spaces(leftSpace).append(s).
                append(spaces(rightSpace)).toString();
        }
    }
    // return a StringBuffer full of spaces
    private static StringBuffer spaces(int numSpaces) {
        if (numSpaces <= 0) {
            return new StringBuffer();
        }
        else {
            StringBuffer spaces = new StringBuffer();
            for (int i = 1; i <= numSpaces; i++) {
                spaces.append(' ');
            }
            return spaces;
        }
    }
}
