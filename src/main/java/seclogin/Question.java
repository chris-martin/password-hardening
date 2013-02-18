package seclogin;

public class Question {

    private final String question;
    private final double averageResponse;

    public Question(String question, double averageResponse) {
        this.question = question;
        this.averageResponse = averageResponse;
    }

    public String getQuestion() {
        return question;
    }

    public double getAverageResponse() {
        return averageResponse;
    }
}
