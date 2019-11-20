import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * <b>Class</b> - Logarithm
 * <blockquote>{@code public class Logarithm}</blockquote>
 *
 * Primarily an extension of the Math.log() method
 * into the BigInteger and the BigDecimal.
 *
 */
public class Logarithm {

    private Logarithm() {}

    public static double get(BigInteger integer) {
        BigInteger val = new BigInteger(integer.toByteArray());
        if (val.bitLength() > 200000)
            throw new OutOfMemoryError("The bit length of your input " + val.bitLength() + " is greater than 200000!");
        int residue = 0;
        while (val.bitLength() > 1000) {
            val = val.divide(new BigInteger("1000").pow(20));
            residue++;
        }
        return Math.log10(val.doubleValue()) + residue * 60;
    }

    public static double get(BigDecimal decimal) throws OutOfMemoryError {
        BigInteger val = decimal.toBigInteger();
        int residue = 0;
        if (val.bitLength() > 200000)
            throw new OutOfMemoryError("The bit length of your input, " + val.bitLength() + " is greater than 200000!");
        while (val.bitLength() > 1000) {
            val = val.divide(new BigDecimal("1000").pow(20).toBigInteger());
            residue++;
        }
        return Math.log10(val.doubleValue()) + residue * 60;
    }
}