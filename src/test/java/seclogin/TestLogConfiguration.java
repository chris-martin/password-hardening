package seclogin;

import org.slf4j.LoggerFactory;

/** Controls whether logging is enabled during tests. */
public class TestLogConfiguration {

    private static final boolean LOGGING_ENABLED = false;

    public static void configureLogging() {
        if (LOGGING_ENABLED) {
            // ensure logging initialized
            LoggerFactory.getLogger("ROOT");
        } else {
            LogConfiguration.disableLogging();
        }
    }

    private TestLogConfiguration() {}
}
