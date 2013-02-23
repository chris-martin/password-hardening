package seclogin.historyfile;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import seclogin.MeasurementStats;
import seclogin.User;

import javax.annotation.Nullable;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

/*
 * A history file for a particular user that contains measurements (responses to questions)
 * from a fixed number of previously successful login attempts.
 */
public class HistoryFile {

    static final HashFunction USER_HASH_FN = Hashing.sha256();

    public final HistoryFileParams params;
    final byte[] userHash;
    final int nrOfMeasurements;
    final double[][] measurements;

    HistoryFile(HistoryFileParams params, byte[] userHash, int nrOfMeasurements, double[][] measurements) {
        this.params = params;
        this.userHash = userHash;
        this.nrOfMeasurements = nrOfMeasurements;
        this.measurements = measurements;
    }

    /** Returns an empty history file. */
    public static HistoryFile emptyHistoryFile(User user, HistoryFileParams params) {
        byte[] userHash = USER_HASH_FN.hashString(user.user).asBytes();
        return new HistoryFile(params, userHash, 0,
            new double[params.maxNrOfMeasurements][params.nrOfFeatures]);
    }

    /** Returns whether this history file's user hash matches that of the given user. */
    boolean verifyUserHash(User user) {
        return Arrays.equals(userHash, USER_HASH_FN.hashString(user.user).asBytes());
    }

    /**
     * Returns a new history file that includes the given most recent measurements
     * and excludes the least recent measurements if the file is full.
     */
    public HistoryFile withMostRecentMeasurements(double[] mostRecentMeasurements) {
        checkArgument(mostRecentMeasurements.length == params.nrOfFeatures);
        double[][] shiftedMeasurements = new double[measurements.length][];
        shiftedMeasurements[0] = mostRecentMeasurements;
        System.arraycopy(measurements, 0, shiftedMeasurements, 1, shiftedMeasurements.length - 1);
        return new HistoryFile(params, userHash, Math.min(params.maxNrOfMeasurements, nrOfMeasurements + 1), shiftedMeasurements);
    }

    /**
     * Returns statistics of the measurements in this history file, or null if the file is not yet full, i.e.,
     * if the user has not successfully logged at least as many times as measurements can fit in this history file.
     */
    @Nullable
    public MeasurementStats[] calculateStats() {
        if (nrOfMeasurements != measurements.length) {
            return null;
        }

        MeasurementStats[] stats = new MeasurementStats[params.nrOfFeatures];
        for (int i = 0; i < stats.length; i++) {
            SummaryStatistics stat = new SummaryStatistics();
            for (double[] measurement : measurements) {
                stat.addValue(measurement[i]);
            }
            stats[i] = new MeasurementStats(stat.getMean(), stat.getStandardDeviation());
        }
        return stats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoryFile that = (HistoryFile) o;

        if (nrOfMeasurements != that.nrOfMeasurements) return false;
        if (!params.equals(that.params)) return false;
        if (!Arrays.equals(userHash, that.userHash)) return false;
        if (!Arrays.deepEquals(measurements, that.measurements)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = params.hashCode();
        result = 31 * result + Arrays.hashCode(userHash);
        result = 31 * result + nrOfMeasurements;
        result = 31 * result + Arrays.deepHashCode(measurements);
        return result;
    }

    @Override
    public String toString() {
        return "HistoryFile{" +
                "params=" + params +
                ", userHash=" + Arrays.toString(userHash) +
                ", nrOfMeasurements=" + nrOfMeasurements +
                ", measurements=" + Arrays.deepToString(measurements) +
                '}';
    }
}
