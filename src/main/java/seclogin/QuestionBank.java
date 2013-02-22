package seclogin;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.List;

/** Bank of questions to which responses will constitute measurements used to hardened the password. */
public class QuestionBank implements Iterable<Question> {

    private final List<Question> questions;

    public QuestionBank(List<Question> questions) {
        this.questions = ImmutableList.copyOf(questions);
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public MeasurementParams[] measurementParams() {
        MeasurementParams[] params = new MeasurementParams[questions.size()];
        for (int i = 0; i < params.length; i++) {
            params[i] = questions.get(i).measurementParams();
        }
        return params;
    }

    @Override
    public Iterator<Question> iterator() {
        return questions.iterator();
    }

    public static QuestionBank createDefault() {
        return new QuestionBank(ImmutableList.of(
            new Question(
                "How far (in miles) are you from the Georgia Tech campus?",
                new MeasurementParams(1, 2)
            ),
            new Question(
                "How long (in minutes) do you anticipate being logged in during this session?",
                new MeasurementParams(20, 2)
            ),
            new Question(
                "How many emails will you send during this session?",
                new MeasurementParams(2, 2)
            )
        ));
    }

}
