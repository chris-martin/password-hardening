package seclogin;

public class Question {

    private final String question;
    private final double responseMean;

    public Question(String question, double responseMean) {
        this.question = question;
        this.responseMean = responseMean;
    }

    public String getQuestion() {
        return question;
    }

    public double getResponseMean() {
        return responseMean;
    }
}
