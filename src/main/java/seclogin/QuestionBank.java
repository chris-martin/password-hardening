package seclogin;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

public class QuestionBank implements Iterable<Question> {

    private final List<Question> questions;

    public QuestionBank(List<Question> questions) {
        this.questions = ImmutableList.copyOf(questions);
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public List<MeasurementParams> measurementParams() {
        return Lists.transform(questions, new Function<Question, MeasurementParams>() {
            @Override
            public MeasurementParams apply(Question input) {
                return input.measurementParams();
            }
        });
    }

    @Override
    public Iterator<Question> iterator() {
        return questions.iterator();
    }
}
