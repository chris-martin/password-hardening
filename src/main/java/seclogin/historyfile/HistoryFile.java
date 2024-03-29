package seclogin.historyfile;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seclogin.MeasurementStats;
import seclogin.User;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A history file for a particular user that contains measurements (responses to questions)
 * from a fixed number of previously successful login attempts.
 */
public class HistoryFile {

    private static final Logger log = LoggerFactory.getLogger(HistoryFile.class);

    static final HashFunction USER_HASH_FN = Hashing.sha256();

    final byte[] userHash;
    final double[][] measurements;

    HistoryFile(byte[] userHash, double[][] measurements) {
        checkNotNull(userHash);
        checkArgument(userHash.length == USER_HASH_FN.bits() / Byte.SIZE);
        checkNotNull(measurements);
        checkArgument(measurements.length > 0);

        this.userHash = userHash;
        this.measurements = measurements;
    }

    /** Returns an empty history file. */
    public static HistoryFile emptyHistoryFile(User user, HistoryFileParams params) {
        checkNotNull(user);
        checkArgument(params.maxNrOfMeasurements > 0);

        byte[] userHash = USER_HASH_FN.hashString(user.user).asBytes();
        double[][] measurements = new double[params.maxNrOfMeasurements][params.nrOfFeatures];
        for (double[] measurement : measurements) {
            Arrays.fill(measurement, Double.NaN);
        }
        return new HistoryFile(userHash, measurements);
    }

    /** Returns whether this history file's user hash matches that of the given user. */
    boolean verifyUserHash(User user) {
        checkNotNull(user);

        return Arrays.equals(userHash, USER_HASH_FN.hashString(user.user).asBytes());
    }

    /**
     * Returns a new history file that includes the given most recent measurements
     * and excludes the least recent measurements if the file is full.
     */
    public HistoryFile withMostRecentMeasurements(double[] mostRecentMeasurements) {
        checkNotNull(mostRecentMeasurements);
        checkArgument(mostRecentMeasurements.length == measurements[0].length);

        double[][] shiftedMeasurements = new double[measurements.length][];
        shiftedMeasurements[0] = mostRecentMeasurements;
        System.arraycopy(measurements, 0, shiftedMeasurements, 1, shiftedMeasurements.length - 1);
        return new HistoryFile(userHash, shiftedMeasurements);
    }

    /**
     * Returns statistics of the measurements in this history file. The stats for features for which there aren't
     * enough entries in the table will be null. If the file is not yet full, i.e., if the user has not successfully
     * logged in at least as many times as measurements can fit in this history file, then all features will have
     * null stats. If the file is full, but for a particular feature the user did not supply a measurement on
     * all of the entries in the history file, the stats for that feature will be null.
     */
    public MeasurementStats[] calculateStats() {
        checkState(measurements.length > 0);

        int nrOfFeatures = measurements[0].length;
        MeasurementStats[] stats = new MeasurementStats[nrOfFeatures];

        log.debug("Calculating historical measurement stats");
        for (int i = 0; i < stats.length; i++) {
            log.debug("Feature i = {}", i);
            SummaryStatistics stat = new SummaryStatistics();
            for (double[] measurement : measurements) {
                log.debug("Historical measurements = {}", Arrays.toString(measurement));
                checkState(measurement.length == nrOfFeatures);
                double featureMeasurement = measurement[i];
                if (Double.isNaN(featureMeasurement)) {
                    break; // the history file isn't full for this feature, so go no further
                }
                stat.addValue(featureMeasurement);
            }
            checkState(stat.getN() <= measurements.length);
            if (stat.getN() == measurements.length) {
                stats[i] = new MeasurementStats(stat.getMean(), stat.getStandardDeviation());
            }
            log.debug("Calculated stats = {}", stats[i]);
        }
        return stats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoryFile that = (HistoryFile) o;

        if (!Arrays.equals(userHash, that.userHash)) return false;
        if (!Arrays.deepEquals(measurements, that.measurements)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(userHash);
        result = 31 * result + Arrays.deepHashCode(measurements);
        return result;
    }

    @Override
    public String toString() {
        return "HistoryFile{" +
                "userHash=" + BaseEncoding.base16().lowerCase().encode(userHash) +
                ", measurements=" + Arrays.deepToString(measurements) +
                '}';
    }
}
