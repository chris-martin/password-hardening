package seclogin.historyfile;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import seclogin.MeasurementStats;
import seclogin.SecurityParameters;
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

    static final HashFunction USER_HASH_FN = Hashing.sha256();

    final byte[] userHash;
    final int nrOfMeasurements;
    final double[][] measurements;

    HistoryFile(byte[] userHash, int nrOfMeasurements, double[][] measurements) {
        checkNotNull(userHash);
        checkArgument(userHash.length == USER_HASH_FN.bits()/Byte.SIZE);
        checkNotNull(nrOfMeasurements);
        checkNotNull(measurements);
        checkArgument(measurements.length > 0);
        checkArgument(nrOfMeasurements <= measurements.length);

        this.userHash = userHash;
        this.nrOfMeasurements = nrOfMeasurements;
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
        return new HistoryFile(userHash, 0, measurements);
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
        return new HistoryFile(userHash, Math.min(measurements.length, nrOfMeasurements + 1), shiftedMeasurements);
    }

    /**
     * Returns statistics of the measurements in this history file. The stats for features for which there aren't
     * enough entries in the table will be null. If the file is not yet full, i.e., if the user has not successfully
     * logged in at least as many times as measurements can fit in this history file, then all features will have
     * null stats. If the file is full, but for a particular feature the user did not supply a measurement on
     * more than half of the entries in the history file, the stats for that feature will be null.
     *
     * @param declinedMeasurementNonDistinguishmentThreshold
     * Lower bound (exclusive) on the percentage of measurements for a particular feature declined by the user
     * at which that feature will be considered non-distinguishing.
     */
    public MeasurementStats[] calculateStats(double declinedMeasurementNonDistinguishmentThreshold) {
        checkState(measurements.length > 0);

        int nrOfFeatures = measurements[0].length;
        MeasurementStats[] stats = new MeasurementStats[nrOfFeatures];

        if (nrOfMeasurements != measurements.length) {
            return stats; // the history file isn't full yet
        }

        for (int i = 0; i < stats.length; i++) {
            SummaryStatistics stat = new SummaryStatistics();
            for (double[] measurement : measurements) {
                checkState(measurement.length == nrOfFeatures);
                double featureMeasurement = measurement[i];
                if (!Double.isNaN(featureMeasurement)) {
                    stat.addValue(featureMeasurement);
                }
            }
            boolean notEnoughMeasurements = stat.getN() / (double) measurements.length <
                    declinedMeasurementNonDistinguishmentThreshold;
            stats[i] = notEnoughMeasurements ? null : new MeasurementStats(stat.getMean(), stat.getStandardDeviation());
        }
        return stats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoryFile that = (HistoryFile) o;

        if (nrOfMeasurements != that.nrOfMeasurements) return false;
        if (!Arrays.equals(userHash, that.userHash)) return false;
        if (!Arrays.deepEquals(measurements, that.measurements)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(userHash);
        result = 31 * result + nrOfMeasurements;
        result = 31 * result + Arrays.deepHashCode(measurements);
        return result;
    }

    @Override
    public String toString() {
        return "HistoryFile{" +
                "userHash=" + BaseEncoding.base16().lowerCase().encode(userHash) +
                ", nrOfMeasurements=" + nrOfMeasurements +
                ", measurements=" + Arrays.deepToString(measurements) +
                '}';
    }
}
