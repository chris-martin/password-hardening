package seclogin.instructiontable;

import seclogin.MeasurementParams;
import seclogin.MeasurementStats;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static seclogin.instructiontable.Distinguishment.ALPHA;
import static seclogin.instructiontable.Distinguishment.BETA;

/**
 * Policy on how and whether a feature is distinguished based upon its measurements, the user's historical
 * measurements of it, and its system-wide parameters.
 */
public class DistinguishmentPolicy {

    /**
     * Determines the how each feature's measurement distinguishes it according to the system-wide
     * parameter of that feature.
     *
     * @see #measurementDistinguishment(double, seclogin.MeasurementParams)
     */
    public final Distinguishment[] measurmentDistinguishment(double[] measurements, MeasurementParams[] params) {
        checkNotNull(measurements);
        checkNotNull(params);
        checkArgument(measurements.length == params.length);

        Distinguishment[] distinguishments = new Distinguishment[measurements.length];
        for (int i = 0; i < measurements.length ; i++) {
            distinguishments[i] = measurementDistinguishment(measurements[i], params[i]);
        }
        return distinguishments;
    }

    /**
     * Determines how the given measurement of a feature distinguishes it according to the system-wide
     * parameter for that feature.
     */
    protected Distinguishment measurementDistinguishment(double measurement, MeasurementParams params) {
        return measurement < params.responseMean() ? ALPHA : BETA;
    }

    /**
     * Determines whether and how a feature is distinguishing for a particular user.
     *
     * @see #userDistinguishment(seclogin.MeasurementStats, seclogin.MeasurementParams)
     */
    public final Distinguishment[] userDistinguishment(MeasurementStats[] stats, MeasurementParams[] params) {
        checkNotNull(stats);
        checkNotNull(params);
        checkArgument(stats.length == params.length);

        Distinguishment[] distinguishments = new Distinguishment[stats.length];
        for (int i = 0; i < stats.length ; i++) {
            distinguishments[i] = userDistinguishment(stats[i], params[i]);
        }
        return distinguishments;
    }

    /**
     * Determines whether and how a feature is distinguishing for a particular user by comparing that user's
     * measurement statistics (i.e., measurement history) of that feature to the system-wide measurement
     * parameters for that feature.
     *
     * @return the distinguishment of a feature, or null if the feature is not distinguishing)
     */
    @Nullable
    protected Distinguishment userDistinguishment(@Nullable MeasurementStats stats, MeasurementParams params) {
        if (stats == null) {
            return null; // no user stats for feature, so feature is not distinguishing
        }

        if (Math.abs(stats.mean() - params.responseMean()) > (stats.stDev() * params.stDevMultiplier())) {
            return stats.mean() < params.responseMean() ? ALPHA : BETA;
        }
        return null;
    }
}
