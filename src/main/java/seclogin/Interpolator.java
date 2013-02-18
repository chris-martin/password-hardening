package seclogin;

import java.math.BigInteger;

public interface Interpolator {

    BigInteger interpolateYIntercept(BigInteger[] xyPairs, BigInteger mod);
}
