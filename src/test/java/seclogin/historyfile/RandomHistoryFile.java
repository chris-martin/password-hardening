package seclogin.historyfile;

import seclogin.User;

import java.util.Random;

class RandomHistoryFile {

    private final HistoryFileParams params;
    private final Random random;

    private RandomHistoryFile(HistoryFileParams params, Random random) {
        this.params = params;
        this.random = random;
    }

    static RandomHistoryFile random(Random random) {
        return random(new HistoryFileParams(2, 3), random);
    }

    static RandomHistoryFile random(HistoryFileParams params, Random random) {
        return new RandomHistoryFile(params, random);
    }

    HistoryFile nextHistoryFile() {
        return nextHistoryFile(new User("asdf"));
    }

    HistoryFile nextHistoryFile(User user) {
        HistoryFile file = HistoryFile.emptyHistoryFile(user, params);
        for (int i = 0; i < params.maxNrOfMeasurements; ++i) {
            file = file.withMostRecentMeasurements(randomMeasurements());
        }
        return file;
    }

    private double[] randomMeasurements() {
        double[] measurements = new double[params.nrOfFeatures];
        for (int i = 0; i < measurements.length; i++) {
            measurements[i] = randomMeasurement();
        }
        return measurements;
    }

    private double randomMeasurement() {
        return random.nextInt(20);
    }
}
