package seclogin.instructiontable;

import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seclogin.HardenedPassword;
import seclogin.Password;
import seclogin.SecurityParameters;
import seclogin.instructiontable.InstructionTable.Entry;
import seclogin.math.Interpolation;
import seclogin.math.Mod;
import seclogin.math.PasswordBasedPRF;
import seclogin.math.Point;
import seclogin.math.Polynomial;
import seclogin.math.RandomBigIntModQ;
import seclogin.math.RandomPolynomial;
import seclogin.math.SparsePRP;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static seclogin.instructiontable.Distinguishment.ALPHA;
import static seclogin.instructiontable.Distinguishment.BETA;

/** Generates instruction tables and interpolates corresponding hardened passwords mod q. */
public class InstructionTableModQ {

    private static final Logger log = LoggerFactory.getLogger(InstructionTableModQ.class);

    private final Mod q;
    private final Random random;
    private final RandomBigIntModQ randomBigIntModQ;
    private final RandomPolynomial randomPolynomial;

    public InstructionTableModQ(Mod q, Random random) {
        checkNotNull(q);
        checkNotNull(random);

        this.q = q;
        this.random = random;
        randomBigIntModQ = new RandomBigIntModQ(random, q);
        randomPolynomial = new RandomPolynomial(random, q);
    }

    /**
     * Interpolates the hardened password using the (x,y) pairs recovered from the given table
     * using the given regular password and measurements.
     */
    public HardenedPassword interpolateHpwd(InstructionTable table, Password pwd, Distinguishment[] distinguishments) {
        checkNotNull(table);
        checkNotNull(pwd);
        checkNotNull(distinguishments);

        List<Point> points = table.points(q, pwd, distinguishments);
        return new HardenedPassword(new Interpolation(points, q).yIntercept());
    }

    /**
     * Generates a hardened password mod q.
     */
    public HardenedPassword generateHardenedPassword() {
        return new HardenedPassword(randomBigIntModQ.nextBigIntModQ());
    }

    /**
     * Generates an instruction table using the given corresponding hardened password,
     * the given regular password, and the given feature distinguishments for the user.
     */
    public InstructionTable generate(HardenedPassword hpwd, Password pwd, Distinguishment[] distinguishments) {
        checkNotNull(hpwd);
        checkArgument(q.contains(hpwd.hpwd));
        checkNotNull(pwd);
        checkNotNull(distinguishments);

        BigInteger f0 = hpwd.hpwd;
        Polynomial f = randomPolynomial.nextPolynomial(f0, distinguishments.length);

        byte[] r = new byte[SecurityParameters.R_LEN/Byte.SIZE];
        random.nextBytes(r);

        log.debug("Generating instruction table");
        log.debug("r = {}", BaseEncoding.base16().lowerCase().encode(r));
        log.debug("hpwd = {}", hpwd);

        PasswordBasedPRF g = PasswordBasedPRF.forSaltedPassword(r, pwd, q);
        SparsePRP p = new SparsePRP(r, q);

        Entry[] table = new Entry[distinguishments.length];
        for (int i = 0; i < table.length; i++) {
            Distinguishment distinguishment = distinguishments[i];
            log.debug("Feature i = {}", i);
            log.debug("distinguishment = {}", distinguishment);

            BigInteger y0 = f.apply(p.apply(2*i));
            BigInteger alpha = y0.add(g.of(2*i)).mod(q.q);
            log.debug("alpha = {}", alpha.toString(16));
            if (distinguishment == BETA) {
                log.debug("Polluting alpha");
                alpha = randomBigIntModQ.nextBigIntegerModQNotEqualTo(alpha);
                log.debug("alpha = {}", alpha.toString(16));
            }

            BigInteger y1 = f.apply(p.apply((2*i)+1));
            BigInteger beta = y1.add(g.of((2*i)+1)).mod(q.q);
            log.debug("beta = {}", beta.toString(16));
            if (distinguishment == ALPHA) {
                log.debug("Polluting beta");
                beta = randomBigIntModQ.nextBigIntegerModQNotEqualTo(beta);
                log.debug("beta = {}", beta.toString(16));
            }

            table[i] = new Entry(alpha, beta);
        }
        return new InstructionTable(r, table);
    }
}
