package seclogin.historyfile;

public class HistoryFileParams {

    final int maxNrOfMeasurements;
    final int nrOfFeatures;

    public HistoryFileParams(int maxNrOfMeasurements, int nrOfFeatures) {
        this.maxNrOfMeasurements = maxNrOfMeasurements;
        this.nrOfFeatures = nrOfFeatures;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoryFileParams that = (HistoryFileParams) o;

        if (maxNrOfMeasurements != that.maxNrOfMeasurements) return false;
        if (nrOfFeatures != that.nrOfFeatures) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = maxNrOfMeasurements;
        result = 31 * result + nrOfFeatures;
        return result;
    }

    @Override
    public String toString() {
        return "HistoryFileParams{" +
                "maxNrOfMeasurements=" + maxNrOfMeasurements +
                ", nrOfFeatures=" + nrOfFeatures +
                '}';
    }
}
