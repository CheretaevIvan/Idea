package elemmobi;

import org.apache.commons.cli.*;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Timer;

public class Main {
    public static void main(String[] args) {
        Option Login = new Option("l", "login", true, "имя пользователя для входа в игру");
        Option Password = new Option("p", "password", true, "пароль для входа в игру");
        Option GoTop = new Option("t", "top", false, "искать только топ пользователей");
        Option Help = new Option("h", "help", false, "подсказка для незнающих");
        Options options = new Options();
        options.addOption(Login);
        options.addOption(Password);
        options.addOption(Help);
		options.addOption(GoTop);
        GnuParser parser = new GnuParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String login = "";
        String password = "";
        Boolean goTop = false;
        if (line.hasOption("l")) {
            login = line.getOptionValue("l");
        }
        if (line.hasOption("p")) {
            password = line.getOptionValue("p");
        }
        if (line.hasOption("t")){
            goTop = true;
        }
        if (line.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Masters Of Elemental", options);
        }

        if (!login.isEmpty() && !password.isEmpty()) {
            Timer timerChert = new Timer();
            ScheduledTask scheduledTaskChert = new ScheduledTask(login, password, goTop);
            int minutes = 40;
            try {
                File base = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
                File configFile = new File(base, "config.properties");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            timerChert.schedule(scheduledTaskChert, 0, 1000 * 60 * minutes);
        } else {
            if (!line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("Masters of Elemental", options);
            }
        }
    }
}
