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
}
