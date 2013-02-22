package seclogin.math;

import java.math.BigInteger;

/**
 * An ''(x, y)'' pair contained by some function ''f(x) = y''.
 */
public class Point {

    public final BigInteger x;
    public final BigInteger y;

    public Point(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
    }

    public Point(int x, int y) {
        this(BigInteger.valueOf(x), BigInteger.valueOf(y));
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (x != null ? !x.equals(point.x) : point.x != null) return false;
        if (y != null ? !y.equals(point.y) : point.y != null) return false;

        return true;
    }

    public int hashCode() {
        int result = x != null ? x.hashCode() : 0;
        result = 31 * result + (y != null ? y.hashCode() : 0);
        return result;
    }

}
