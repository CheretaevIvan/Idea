import org.apache.commons.cli.*;

import java.util.Timer;

public class Main {
    public static void main(String[] args) {
        Option Login = new Option("l", "login", true, "имя пользователя для входа в игру");
        Option Password = new Option("p", "password", true, "пароль для входа в игру");
        Option Help = new Option("h", "help", false, "подсказка для незнающих");
        Options options = new Options();
        options.addOption(Login);
        options.addOption(Password);
        options.addOption(Help);
        GnuParser parser = new GnuParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String login = "";
        String password = "";
        if (line.hasOption("l")) {
            login = line.getOptionValue("l");
        }
        if (line.hasOption("p")) {
            password = line.getOptionValue("p");
        }
        if (line.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Masters Of Elemental", options);
        }

        if (!login.isEmpty() && !password.isEmpty()) {
            Timer timerChert = new Timer();
            ScheduledTask scheduledTaskChert = new ScheduledTask(login, password);
            timerChert.schedule(scheduledTaskChert, 0, 1000 * 60 * 40);
        }
        else {
            if (!line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("Masters of Elemental", options);
            }
        }
    }
}
