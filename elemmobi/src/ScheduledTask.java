import java.util.TimerTask;

public class ScheduledTask extends TimerTask {
    private String Login, Password;
    MastersOfElemental game = new MastersOfElemental();
    public ScheduledTask(String login, String password){
        super();
        Login = login;
        Password = password;
    }

    @Override
    public void run() {
        try {
            game.login(Login, Password);
            game.GetCurrentState();
            game.logger.info("Top users: \n" + game.GetTopUsers());

            while (game.GetCurrentNumberDuels() > 0) {
                game.HoldDuel();
                game.GetCurrentState();
            }
            game.logout();
        }
        catch (Exception e){
            game.logger.severe("Exception: "+e.toString());
        }
    }
}
