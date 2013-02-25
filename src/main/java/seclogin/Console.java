package seclogin;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.tools.jline.console.ConsoleReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Random;

public class Console {

    private static final Logger log = LoggerFactory.getLogger(Console.class);

    private Console() {}

    public static void main(String[] args) {
        Namespace ns = parseArguments(args);
        boolean verbose = ns.getBoolean("verbose");

        try {
            main(ns);
        } catch (RuntimeException e) {
            if (verbose) {
                e.printStackTrace();
            } else {
                System.err.println(e.getMessage());
            }
            throw exit(1);
        }
    }

    private static Namespace parseArguments(String[] args) {
        ArgumentParser parser = ArgumentParsers
                .newArgumentParser(SecLogin.class.getSimpleName())
                .defaultHelp(true)
                .description("Password hardening proof-of-concept");

        parser.addArgument("-a", "--add")
                .help("Add specified user");

        parser.addArgument("-v", "--verbose")
                .action(Arguments.storeTrue());

        parser.addArgument("--historyfilesize")
                .help("Change the history file size. Warning: Altering the history file size" +
                        "with existing user state will likely result in unexpected behavior.")
                .type(Integer.class);

        Namespace ns;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            throw exit(1);
        }
        return ns;
    }

    public static void main(Namespace args) {
        if (!args.getBoolean("verbose")) {
            LogConfiguration.disableLogging();
        }

        Config config = loadAndUpdateConfig(args);
        int historyFileSize = config.getHistoryFileSize();
        log.debug("Using history file size = {}", historyFileSize);

        Random random = new SecureRandom();
        SecLogin secLogin = new SecLogin(
            new ConsoleUI(),
            new UserStateFilesystemPersistence(persistentStateDir()),
            random,
            QuestionBank.createDefault(),
            historyFileSize
        );

        String usernameToAdd = args.getString("add");
        if (usernameToAdd != null) {
            secLogin.addUser(usernameToAdd);
            System.out.printf("Added user %s.\n", usernameToAdd);
            throw exit(0);
        }

        secLogin.prompt();
    }

    private static Config loadAndUpdateConfig(Namespace args) {
        File configFile = new File(persistentStateDir(), "seclogin.properties");
        Config config;
        try {
            config = new Config(configFile);

            Integer historyFileSize = args.getInt("historyfilesize");
            if (historyFileSize != null) {
                config.setHistoryFileSize(historyFileSize);
                config.save();
                System.out.println("Saved updated config.");
                throw exit(0);
            }
        } catch (IOException e) {
            System.err.println("Could not read/write config file " + configFile.getAbsolutePath());
            throw exit(1);
        }
        return config;
    }

    /** The directory in which to store configuration and user history files and instruction tables. */
    private static File persistentStateDir() {
        File file = new File(".seclogin");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new RuntimeException("Could not create directory " + file.getAbsolutePath());
            }
        }
        return file;
    }

    static RuntimeException exit(int exitCode) {
        System.exit(exitCode);
        return null;
    }

    static class ConsoleUI implements UserInterface {

        private final ConsoleReader console;
        private final Thread shutdownHook;

        ConsoleUI(final ConsoleReader console) {
            this.console = console;
            shutdownHook = new Thread(new Runnable() {
                @Override
                public void run() {
                    restoreTerminal();
                }
            });
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }

        @Override
        protected void finalize() throws Throwable {
            restoreTerminal();
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            super.finalize();
        }

        private void restoreTerminal() {
            try {
                console.getTerminal().restore();
            } catch (Throwable ignored) {
            }
        }

        ConsoleUI() {
            this(newConsoleReader());
        }

        static ConsoleReader newConsoleReader() {
            try {
                return new ConsoleReader();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private String ask(String prompt, Character mask) {
            try {
                return console.readLine(prompt + " ", mask);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public String ask(String prompt) {
            return ask(prompt, null);
        }

        public String askSecret(String prompt) {
            return ask(prompt, ConsoleReader.NULL_MASK);
        }

        public void tell(String message) {
            try {
                console.println(message);
                console.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    static class Config {

        final File file;
        final Properties props = new Properties();

        Config(File file) throws IOException {
            this.file = file;
            if (!file.exists()) {
                save();
            }
            FileReader reader = new FileReader(file);
            try {
                log.debug("Loading configuration from {}", file);
                props.load(reader);
            } finally {
                reader.close();
            }
        }

        void save() throws IOException {
            FileWriter writer = new FileWriter(file);
            try {
                log.debug("Saving configuration to %s", file);
                props.store(writer, null);
            } finally {
                writer.close();
            }
        }

        String historyFileSizeKey = "historyFileSize";
        int getHistoryFileSize() {
            try {
                return Integer.parseInt(props.getProperty(historyFileSizeKey, "2"));
            } catch (NumberFormatException e) {
                throw new RuntimeException(historyFileSizeKey + " must be an integer");
            }
        }
        void setHistoryFileSize(int historyFileSize) {
            props.setProperty(historyFileSizeKey, String.valueOf(historyFileSize));
        }
    }

}
