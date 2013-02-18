package seclogin;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class QuestionBank implements Iterable<Question> {

    private final List<Question> questions;

    public QuestionBank(List<Question> questions) {
        this.questions = ImmutableList.copyOf(questions);
    }

    public List<Question> getQuestions() {
        return questions;
    }

    @Override
    public Iterator<Question> iterator() {
        return questions.iterator();
    }
}
