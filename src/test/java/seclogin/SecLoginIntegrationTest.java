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

        Question question = new Question("Question A", new MeasurementParams(50, 2));
        QuestionBank questions = new QuestionBank(Arrays.<Question>asList(question));
        SecLogin secLogin = new SecLogin(userInterface, userStatePersistence, random, questions, 2);

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



}
