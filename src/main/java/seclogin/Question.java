package seclogin;

class Question {

    private final String question;

    private final MeasurementParams measurementParams;

    public Question(String question, MeasurementParams measurementParams) {
        this.question = question;
        this.measurementParams = measurementParams;
    }

    public String question() {
        return question;
    }

    public MeasurementParams measurementParams() {
        return measurementParams;
    }

}
