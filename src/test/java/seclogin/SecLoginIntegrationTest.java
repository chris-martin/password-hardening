package seclogin;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

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
        }
    }

    void expect(Object ... os) {
        for (Object o : os) expect(o);
    }

    /**
     * Demonstrates how a setup without any questions behaves just like a normal password
     * system with no hardening.
     */
    @Test
    public void test_with_no_questions() throws Exception {

        QuestionBank questions = new QuestionBank(Arrays.<Question>asList());
        SecLogin secLogin = new SecLogin(userInterface, userStatePersistence, random, questions);

        passwordIs("password");
        userIs("steve");

        // We add one user, and the system prompts for a password.
        secLogin.addUser("steve");
        expect(PasswordPrompt, Done);

        // Log in with that password successfully.
        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, Success, Done);

        // Try to log in with a different password, and fail.
        passwordIs("psosarwd");
        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, Failure, Done);

    }

    @Test
    public void test_with_one_question() throws Exception {

        Question question = new Question("Question A", new MeasurementParams(50, 2));
        QuestionBank questions = new QuestionBank(Arrays.<Question>asList(question));
        SecLogin secLogin = new SecLogin(userInterface, userStatePersistence, random, questions);

        passwordIs("password");
        userIs("steve");
        answerIs(question, "20");

        secLogin.addUser("steve");
        expect(PasswordPrompt, Done);

        secLogin.prompt();
        expect(UserPrompt, PasswordPrompt, question, Success, Done);

    }

}
