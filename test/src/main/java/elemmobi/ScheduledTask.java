package elemmobi;

import java.util.TimerTask;

public class ScheduledTask extends TimerTask {
    MastersOfElemental game;
    private String Login, Password;

    public ScheduledTask(String login, String password, boolean goTop) {
        super();
        Login = login;
        Password = password;
        game = new MastersOfElemental(goTop);
    }

    @Override
    public void run() {
        try {
            if (game.login(Login, Password) != null) {
                game.GetCurrentState();
                game.logger.info("Top users: \n" + game.GetTopUsers());

                while (game.GetCurrentNumberDuels() > 0) {
                    game.HoldDuel();
                    game.GetCurrentState();
                }
                game.logout();
            }
        } catch (Exception e) {
            game.logger.severe("Exception: " + e.toString());
        }
    }
}
