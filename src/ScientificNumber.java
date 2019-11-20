import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ProgressBar;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class ScientificNumber extends BigNumber {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static void main(String[] args) throws Exception {
        new JFXPanel();
        Player player = new Player(3000), player2 = new Player(3000);
        player.setType(Player.Type.USER);
        player.setClassType(Player.ClassType.ARCHER);
        player2.setType(Player.Type.ENEMY);
        player2.setClassType(Player.ClassType.ARCHER);
        player.attack(player2);
        ProgressBar bar = player2.getHealthBar();
        ScientificNumber difference = new ScientificNumber(player2.getHealth());
        ScientificNumber minuend = new ScientificNumber(player2.getMaxHealth());
        minuend = minuend.multiply(bar.getProgress());
        difference = difference.subtract(minuend);
        System.out.println(difference);
    }

    public ScientificNumber(BigInteger val) {
        super(val);
    }

    public ScientificNumber abs() {
        return new ScientificNumber(super.abs());
    }

    public ScientificNumber(int val) {
        super(val);
    }

    public ScientificNumber(String val) {
        super(val);
    }

    public ScientificNumber(BigDecimal decimal) {
        super(decimal.toBigInteger());
    }

    public BigInteger getInteger() {
        return this;
    }

    public BigNumber getNumber() {
        return this;
    }


    @Override
    public String toString() {
        BigInteger val = getInteger();
        if (val.compareTo(new BigInteger("1000")) < 0 && val.compareTo(new BigInteger("-1000")) > 0) {
            return val.intValueExact() + "";
        }
        else {
            BigDecimal decimal = new BigDecimal(val);
            int logarithm = (int) Logarithm.get(decimal);
            decimal = decimal.divide(BigDecimal.TEN.pow(logarithm), new MathContext(3));
            return decimal.toString() + "e+" + logarithm;
        }
    }

    @Override
    public ScientificNumber add(BigNumber val) {
        BigNumber number = super.add(val);
        return new ScientificNumber(number);
    }

    @Override
    public ScientificNumber add(Number val) {
        BigNumber number = super.add(val);
        return new ScientificNumber(number);
    }

    @Override
    public ScientificNumber subtract(BigNumber val) {
        BigNumber number = super.subtract(val);
        return new ScientificNumber(number);
    }

    @Override
    public ScientificNumber subtract(Number val) {
        BigNumber number = super.subtract(val);
        return new ScientificNumber(number);
    }

    @Override
    public ScientificNumber divide(BigNumber val) {
        BigNumber number = super.divide(val);
        return new ScientificNumber(number);
    }

    @Override
    public ScientificNumber divide(Number val) {
        BigNumber number = super.divide(val);
        return new ScientificNumber(number);
    }

    @Override
    public ScientificNumber multiply(BigNumber val) {
        BigNumber number = super.multiply(val);
        return new ScientificNumber(number);
    }

    @Override
    public ScientificNumber multiply(Number val) {
        BigDecimal decimal = new BigDecimal(super.multiply(val));
        return new ScientificNumber(decimal);
    }

    @Override
    public ScientificNumber multiply(double val) {
        BigDecimal decimal = new BigDecimal(super.multiply(val));
        return new ScientificNumber(decimal);
    }

    @Override
    public ScientificNumber pow(Number val) {
        BigNumber number = super.pow(val);
        return new ScientificNumber(number);
    }
}
