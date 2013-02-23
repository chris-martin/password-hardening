package seclogin;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecLoginIntegrationTest {

    UserInterface userInterface;
    UserStatePersistence userStatePersistence;
    Random random;

    @Before
    public void setUp() throws Exception {

        userInterface = Mockito.mock(UserInterface.class);

        userStatePersistence = new UserStatePersistence() {

            final Map<User, UserState> map = new HashMap<User, UserState>();

            public UserState read(User user, MeasurementParams[] measurementParams) {
                return map.get(user);
            }

            public void write(UserState userState) {
                map.put(userState.user, userState);
            }

        };

        random = TestRandom.random();

    }

    @Test
    public void test_with_no_questions() throws Exception {

        QuestionBank questions = new QuestionBank(Arrays.<Question>asList());
        SecLogin secLogin = new SecLogin(userInterface, userStatePersistence, random, questions);

        when(userInterface.askSecret(Mockito.<String>any())).thenReturn("password");
        secLogin.addUser("steve");

    }

    @Test
    public void test_with_one_question() throws Exception {

        QuestionBank questions = new QuestionBank(Arrays.<Question>asList(
            new Question("Question A", new MeasurementParams(50, 2))
        ));
        SecLogin secLogin = new SecLogin(userInterface, userStatePersistence, random, questions);

        when(userInterface.askSecret(UserInterface.PasswordPrompt)).thenReturn("password");

        secLogin.addUser("steve");

        when(userInterface.ask(UserInterface.UserPrompt)).thenReturn("steve");
        when(userInterface.ask("Question A")).thenReturn("20");

        secLogin.prompt();

        verify(userInterface).tell(UserInterface.Success);

    }

}
