package seclogin;

public class HistoryFileParams {

    private final int maxNrOfEntries;

    private final int nrOfFeatures;

    public HistoryFileParams(int maxNrOfEntries, int nrOfFeatures) {
        this.maxNrOfEntries = maxNrOfEntries;
        this.nrOfFeatures = nrOfFeatures;
    }

    public int maxNrOfEntries() {
        return maxNrOfEntries;
    }

    public int nrOfFeatures() {
        return nrOfFeatures;
    }

}
