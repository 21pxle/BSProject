import java.math.BigDecimal;
import java.math.BigInteger;

public class BigNumber extends BigInteger {

    //Don't you have something else to do besides staring at my code?

    public static final BigNumber ZERO = new BigNumber(0);
    public static final BigNumber ONE = new BigNumber(1);
    public static final BigNumber TEN = new BigNumber(10);
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for the BigNumber class.
     * @return A BigNumber class containing the number 0.
     */
    public BigNumber() {
        this(0);
    }
    /**
     * Converts an integer to a number of the BigNumber class.
     * @param val The integer to convert to a number of the BigNumber class.
     */
    public BigNumber(int val) {
        super("" + val);
    }

    /**
     * Converts a string to a number of the BigNumber class.
     * @param val
     */
    public BigNumber(String val) {
        super(val);
    }
    /**
     * Converts a double to a number of the BigNumber class.
     * @param val
     */
    public BigNumber(double val) {
        super(String.valueOf(new BigDecimal(val).toBigInteger()));
    }

    /**
     *
     */
    public BigNumber(Number val) {
        this(val.doubleValue());
    }
    /**
     * Converts a BigInteger to a number of the BigNumber class.
     * @param val
     */
    public BigNumber(BigInteger val) {
        super(val.toByteArray());
    }

    /**
     * Copy constructor.
     * @param val The BigNumber to copy.
     */
    public BigNumber(BigNumber val) {
        this(val.toByteArray());
    }

    /**
     * A constructor that uses a byte array.
     * @param byteArray The byte array to use for the BigNumber class.
     */
    public BigNumber(byte[] byteArray) {
        super(byteArray);
    }

    /**
     * The add() method
     * @param val The BigNumber to add.
     * @return The result of adding the two numbers.
     */
    public BigNumber add(BigNumber val) {
        return new BigNumber(super.add(val.toBigInteger()));
    }

    /**
     * The add() method
     * @param val The number to add.
     * @return The result of adding the two numbers.
     */
    public BigNumber add(Number val) {
        return new BigNumber(super.add(new BigInteger((int) val + "")));
    }

    /**
     * The subtract() method
     * @param val The BigNumber to add.
     * @return The difference between the two numbers.
     */
    public BigNumber subtract(BigNumber val) {
        return new BigNumber(super.subtract(val.toBigInteger()));
    }

    /**
     * The subtract() method
     * @param val The number to subtract.
     * @return The difference between of the two numbers, rounded down to the nearest integer.
     */
    public BigNumber subtract(Number val) {
        return new BigNumber(super.subtract(new BigInteger((int) val.doubleValue() + "")));
    }

    /**
     * The multiply() method
     * @param val The BigNumber to multiply
     * @return The product of the two numbers
     */
    public BigNumber multiply(BigNumber val) {
        return new BigNumber(super.multiply(val.toBigInteger()));
    }

    /**
     * The multiply() method
     * @param val The number to multiply
     * @return The product of the two numbers
     */
    public BigNumber multiply(Number val) {
        return new BigNumber(new BigDecimal(this.toBigInteger()).multiply(new BigDecimal(val + "")));
    }

    /**
     * The multiply() method
     * @param val The number to multiply
     * @return The product of the two numbers
     */
    public BigNumber multiply(double val) {
        return new BigNumber(new BigDecimal(this.toBigInteger()).multiply(new BigDecimal(val + "")));
    }

    /**
     *
     * @param val The power to be raised to.
     * @return <code>this<sup>val</sup></code>, rounded down to the nearest integer.
     * @throws OutOfMemoryError when the bit length multiplied by the parameter is greater than 600000.
     */
    public BigNumber pow(Number val) throws OutOfMemoryError {
        if (super.bitLength() * val.doubleValue() > 600000)
            throw new OutOfMemoryError("The input value is too high!");
        BigNumber number = this;
        BigInteger integer = number.pow((int) Math.floor((double) val));
        BigDecimal decimal1 = new BigDecimal(integer);
        double d = Logarithm.get(number.toBigInteger());
        d *= val.doubleValue() % 1;
        BigDecimal decimal2 = BigDecimal.TEN.pow((int) Math.floor((double) val)) //Exponent
                .multiply(new BigDecimal(Math.pow(10, d % 1))); //Mantissa
        BigDecimal product = decimal1.multiply(decimal2);
        return new BigNumber(product.toBigInteger());
    }

    /**
     * The divide() method
     * @param val The BigNumber to divide
     * @return The quotient of the two numbers.
     */
    public BigNumber divide(BigNumber val) {
        return new BigNumber(super.divide(val.toBigInteger()));
    }

    /**
     * The divide() method
     * @param val The number to divide.
     * @return The quotient rounded down to the nearest integer.
     */
    public BigNumber divide(Number val) {
        return new BigNumber(super.divide(new BigInteger((int) val + "")));
    }

    /**
     * The toBigInteger() method
     * @return A number of the BigInteger class that will have the same value as the caller.
     */
    public BigInteger toBigInteger() {
        return new BigInteger(toByteArray());
    }
    /**
     * The abs() method
     * @return The absolute value of the number.
     */
    @Override
    public BigNumber abs() {
        return new BigNumber(super.abs());
    }
}