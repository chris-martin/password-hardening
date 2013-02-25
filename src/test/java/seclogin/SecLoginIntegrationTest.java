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

    @Test
    public void test_with_no_questions() throws Exception {

        QuestionBank questions = new QuestionBank(Arrays.<Question>asList());
        SecLogin secLogin = new SecLogin(userInterface, userStatePersistence, random, questions);

        passwordIs("password");
        userIs("steve");

        secLogin.addUser("steve");
        expectPasswordPrompt();
        expectNothing();

        secLogin.prompt();
        expectUserPrompt();
        expectPasswordPrompt();
        expectSuccess();
        expectNothing();

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
        expectPasswordPrompt();
        expectNothing();

        secLogin.prompt();
        expectUserPrompt();
        expectPasswordPrompt();
        expectQuestionPrompt(question);
        expectSuccess();
        expectNothing();

    }

}
