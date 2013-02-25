package seclogin;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.mockito.Mockito.*;
import static seclogin.SecLoginIntegrationTest.Expectation.*;

public class SecLoginIntegrationTest {

    UserInterface userInterface;

    InOrder inOrder;

    UserStatePersistence userStatePersistence;

    Random random;

    @Before
    public void setUp() throws Exception {

        userInterface = Mockito.mock(UserInterface.class);

        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws Throwable {
                System.out.println(invocation.getArguments()[0].toString().trim());
                return null;
            }
        }).when(userInterface).tell(Mockito.<String>any());

        userStatePersistence = new UserStatePersistence() {

            final Map<User, UserState> map = new HashMap<User, UserState>();

            public UserState read(User user) {
                return map.get(user);
            }

            public void write(UserState userState) {
                map.put(userState.user, userState);
            }

        };

        random = TestRandom.random();

        inOrder = inOrder(userInterface);

    }

    void userIs(String user) {
        when(userInterface.ask(UserInterface.UserPrompt)).thenReturn(user);
    }

    void passwordIs(String password) {
        when(userInterface.askSecret(UserInterface.PasswordPrompt)).thenReturn(password);
    }

    void answerIs(Question question, String answer) {
        when(userInterface.ask(question.question())).thenReturn(answer);
    }

    void expectNothing() {
        inOrder.verifyNoMoreInteractions();
    }

    void expectUserPrompt() {
        inOrder.verify(userInterface).ask(UserInterface.UserPrompt);
    }

    void expectPasswordPrompt() {
        inOrder.verify(userInterface).askSecret(UserInterface.PasswordPrompt);
    }

    void expectQuestionPrompt(Question question) {
        inOrder.verify(userInterface).ask(question.question());
    }

    void expectSuccess() {
        inOrder.verify(userInterface).tell(UserInterface.Success);
    }

    void expectFailure() {
        inOrder.verify(userInterface).tell(UserInterface.Failure);
    }

    public enum Expectation {
        UserPrompt, PasswordPrompt, Success, Failure, Done
    }

    void expect(Object o) {
        if (o instanceof Expectation) {
            switch ((Expectation) o) {
                case UserPrompt: expectUserPrompt(); break;
                case PasswordPrompt: expectPasswordPrompt(); break;
                case Success: expectSuccess(); break;
                case Failure: expectFailure(); break;
                case Done: expectNothing();
            }
        } else if (o instanceof Question) {
            expectQuestionPrompt((Question) o);
        } else if (o instanceof QuestionBank) {
            for (Question q : ((QuestionBank) o)) {
                expectQuestionPrompt(q);
            }
        }
    }

    void expect(Object ... os) {
        for (Object o : os) expect(o);
    }

    /**
     * Demonstrates that a correct password is required to log in.
     */
    @Test
    public void test_password_correctness() throws Exception {

        System.out.println("test_password_correctness");

        Question question = new Question("Question A", new MeasurementParams(50, 2));
        QuestionBank questions = new QuestionBank(Arrays.<Question>asList(question));
        SecLogin secLogin = new SecLogin(userInterface, userStatePersistence, random, questions, 1, .99);

        passwordIs("password");
        userIs("steve");
        answerIs(question, "20");

        // Add one user, and the system prompts for a password.
        secLogin.addUser("steve");
        expect(PasswordPrompt, Done);

        // Log in with correct password
        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, questions, Success, Done);

        // Try to log in with a different password, and fail.
        passwordIs("psosarwd");
        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, questions, Failure, Done);

    }

    /**
     * Demonstrates that a distinguished feature must be correct for login.
     */
    @Test
    public void test_feature_correctness() throws Exception {

        System.out.println("test_feature_correctness");

        Question question = new Question("Question A", new MeasurementParams(50, 2));
        QuestionBank questions = new QuestionBank(Arrays.<Question>asList(question));
        SecLogin secLogin = new SecLogin(userInterface, userStatePersistence, random, questions, 1, .99);

        passwordIs("password");
        userIs("steve");
        answerIs(question, "20");

        // Add one user, and the system prompts for a password.
        secLogin.addUser("steve");
        expect(PasswordPrompt, Done);

        // Log in with correct password twice, establishing the feature as distinguished
        for (int i = 0; i < 2; i++) {
            secLogin.prompt();
            expect(UserPrompt, PasswordPrompt, questions, Success, Done);
        }

        // Using a slightly different answer should be okay, and the feature is still distinguished.
        answerIs(question, "21.2");
        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, questions, Success, Done);

        // Using a radically different answer should fail.
        answerIs(question, "51");
        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, questions, Failure, Done);

    }

    /**
     * Demonstrates that a distinguished feature may become an undistinguished feature.
     */
    @Test
    public void test_feature_undistinguishment() throws Exception {

        System.out.println("test_feature_undistinguishment");

        Question question = new Question("Question A", new MeasurementParams(50, 2));
        QuestionBank questions = new QuestionBank(Arrays.<Question>asList(question));
        SecLogin secLogin = new SecLogin(userInterface, userStatePersistence, random, questions, 2, .99);

        passwordIs("password");
        userIs("steve");
        answerIs(question, "20");

        // Add one user, and the system prompts for a password.
        secLogin.addUser("steve");
        expect(PasswordPrompt, Done);

        // Log in with correct password twice
        for (int i = 0; i < 2; i++) {
            secLogin.prompt();
            expect(UserPrompt, PasswordPrompt, questions, Success, Done);
        }

        // This answer is rather different from the first two, but succeeds because it is
        // on the correct side of 50.
        answerIs(question, "49");
        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, questions, Success, Done);

        // The previous login radically increases the user's standard deviation, so now the
        // feature is non-distinguishing, and any value works.
        answerIs(question, "99");
        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, questions, Success, Done);

    }

    @Test
    public void test_multiple_questions() throws Exception {

        System.out.println("test_multiple_questions");

        Question questionA = new Question("Question A", new MeasurementParams(50, 2));
        Question questionB = new Question("Question B", new MeasurementParams(500, 2));
        Question questionC = new Question("Question C", new MeasurementParams(5000, 2));
        QuestionBank questions = new QuestionBank(Arrays.<Question>asList(questionA, questionB, questionC));
        SecLogin secLogin = new SecLogin(userInterface, userStatePersistence, random, questions, 2, .49);

        passwordIs("password");
        userIs("steve");
        answerIs(questionA, "20"); // below mean
        answerIs(questionB, "800"); // above mean
        answerIs(questionC, "2000"); // below mean

        // Add one user, and the system prompts for a password.
        secLogin.addUser("steve");
        expect(PasswordPrompt, Done);

        // Log in with correct password thrice. The first two fill the history table,
        // and the third checks against the previous results.
        for (int i = 0; i < 3; i++) {
            secLogin.prompt();
            expect(UserPrompt, PasswordPrompt, questions, Success, Done);
        }

        // Screw up question C
        answerIs(questionC, "7000");
        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, questions, Failure, Done);

        // Don't answer question C
        answerIs(questionC, "");
        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, questions, Failure, Done);

    }

    /**
     * Uses a 2-entry history file that allows a feature to be distinguishing with only one entry.
     */
    @Test
    public void test_unanswered_question() throws Exception {

        System.out.println("test_unanswered_question");

        Question question = new Question("Question A", new MeasurementParams(50, 2));
        QuestionBank questions = new QuestionBank(Arrays.<Question>asList(question));
        SecLogin secLogin = new SecLogin(userInterface, userStatePersistence, random, questions, 2, .51);

        passwordIs("password");
        userIs("steve");

        // Add one user, and the system prompts for a password.
        secLogin.addUser("steve");
        expect(PasswordPrompt, Done);

        // Don't answer the first time
        answerIs(question, "");
        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, questions, Success, Done);

        // Answer this time, distinguishing below
        answerIs(question, "4");
        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, questions, Success, Done);

        // Answer above, which fails because this configuration allows distinguishment
        // even with only a single response.
        answerIs(question, "51");
        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, questions, Failure, Done);

    }

    @Test
    public void test_close_to_system_mean() throws Exception {

        System.out.println("test_close_to_system_mean");

        Question question = new Question("Question A", new MeasurementParams(50, 2));
        QuestionBank questions = new QuestionBank(Arrays.<Question>asList(question));
        SecLogin secLogin = new SecLogin(userInterface, userStatePersistence, random, questions, 2, .51);

        passwordIs("password");
        userIs("steve");

        // Add one user, and the system prompts for a password.
        secLogin.addUser("steve");
        expect(PasswordPrompt, Done);

        answerIs(question, "49.8");
        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, questions, Success, Done);

        answerIs(question, "47");
        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, questions, Success, Done);

        // Answer above, which succeeds because the previous answers below were not
        // far enough below to be distinguishing.
        answerIs(question, "99");
        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, questions, Success, Done);

    }

}
