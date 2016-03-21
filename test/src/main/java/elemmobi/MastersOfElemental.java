package elemmobi;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.Console;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class MastersOfElemental {
    public Logger logger = Logger.getLogger(MastersOfElemental.class.getName());
    public HtmlPage currentPage, mainPage;
    int Win, Defeat;
    private WebClient webClient;
    private boolean IsLogin;
    private int Strength;
    private boolean GoTop;

    MastersOfElemental(boolean goTop) {
        IsLogin = false;
        Strength = 0;
        Win = Defeat = 0;
        GoTop = goTop;

        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getCookieManager().setCookiesEnabled(true);
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);
        try {
            currentPage = webClient.getPage("http://elem.mobi");
        } catch (MalformedURLException e) {
            logger.severe("Exception: " + e.toString());
        } catch (IOException e) {
            logger.severe("Exception: " + e.toString());
        }
    }

    public String GetCurrentState() {
        if (!IsLogin)
            return "";
        try {
            mainPage = (HtmlPage) mainPage.refresh();
        } catch (IOException e) {
            logger.severe("Exception: " + e.toString());
        }
        HtmlElement htmlSilver = (HtmlElement) mainPage.getByXPath("//*[@class='c_silver']").get(0);
        String silver = htmlSilver.asText();
        HtmlElement htmlGold = (HtmlElement) mainPage.getByXPath("//*[@class='c_gold']").get(0);
        String gold = htmlGold.asText();
        String result = String.format("You have: %s gold, %s silver, %d strength and %d duels\n For the current session was %d wins and %d loss",
                gold, silver, Strength, GetCurrentNumberDuels(), Win, Defeat);
        logger.info(result);
        return result;
    }

    public List<String> GetTopUsers() {
        List<String> result = new ArrayList<String>();
        try {
            HtmlPage oldpage = currentPage;
            mainPage = (HtmlPage) mainPage.refresh();
            currentPage = mainPage.getAnchorByText("Лучшие").click();
            for (DomNode node : (List<DomNode>) currentPage.getByXPath("//*[@class='user']")) {
                //System.out.println(node.asText());
                result.add(node.asText());
            }
            currentPage = oldpage;
        } catch (IOException e) {
            logger.severe("Exception: " + e.toString());
        }
        return result;
    }

    public int GetCurrentNumberDuels() {
        if (!IsLogin)
            return 0;
        try {
            mainPage = (HtmlPage) mainPage.refresh();
        } catch (IOException e) {
            logger.severe("Exception: " + e.toString());
        }
        mainPage.getAnchorByHref("/duel/").asText();
        for (DomNode node : mainPage.getAnchorByHref("/duel/").getChildren()) {
            if (node.getNodeName().equals("#text")
                    && !node.getNodeValue().trim().isEmpty()
                    && node.getNodeValue().trim().split(" ")[node.getNodeValue().trim().split(" ").length - 1].startsWith("бо"))
                return Integer.parseInt(node.getTextContent().trim().split(" ")[0]);
        }
        return 0;
    }

    public boolean HoldDuel() {
        try {
            currentPage = mainPage.getAnchorByHref("/duel/").click();
            HtmlElement htmlStrengthEnemy = (HtmlElement) currentPage.getByXPath("//*[contains(@class, 'c_da mt5 mr5')]").get(0);
            int strengthEnemy = Integer.parseInt(htmlStrengthEnemy.asText().replaceAll(" ", ""));
            HtmlElement htmlNameEnemy = (HtmlElement) currentPage.getByXPath("//*[@class='c_rose nwr mb5']").get(0);
            String nameEnemy = htmlNameEnemy.asText();


            if (GoTop) {
                // Бить только топ пользователей
                while (!GetTopUsers().contains(nameEnemy)) {
                    currentPage = currentPage.getAnchorByText("Искать еще").click();
                    htmlStrengthEnemy = (HtmlElement) currentPage.getByXPath("//*[contains(@class, 'c_da mt5 mr5')]").get(0);
                    strengthEnemy = Integer.parseInt(htmlStrengthEnemy.asText().replaceAll(" ", ""));
                    htmlNameEnemy = (HtmlElement) currentPage.getByXPath("//*[@class='c_rose nwr mb5']").get(0);
                    nameEnemy = htmlNameEnemy.asText();
                }
            }
            else {
                // Бить только "слабых" пользователей
                while (strengthEnemy >= Strength) {
                    currentPage = currentPage.getAnchorByText("Искать еще").click();
                    htmlStrengthEnemy = (HtmlElement) currentPage.getByXPath("//*[contains(@class, 'c_da mt5 mr5')]").get(0);
                    strengthEnemy = Integer.parseInt(htmlStrengthEnemy.asText().replaceAll(" ", ""));
                    htmlNameEnemy = (HtmlElement) currentPage.getByXPath("//*[@class='c_rose nwr mb5']").get(0);
                    nameEnemy = htmlNameEnemy.asText();
                }
            }

            logger.info("Selected opponent " + nameEnemy + " with the force " + strengthEnemy);
            currentPage = currentPage.getAnchorByText("Напасть").click();

            while (currentPage.getByXPath("//*[@class='msg2 gld85 mt5']").size() == 0) {
                double maxDamage = Integer.MIN_VALUE;
                HtmlAnchor cardSelection = (HtmlAnchor) currentPage.getByXPath("//*[contains(@href, '/duel/')]").get(0);
                for (DomElement element : (List<DomElement>) currentPage.getByXPath("//*[contains(@class, 'w3card inbl ptb5')]")) {
                    Iterator<DomElement> iterator = element.getChildElements().iterator();
                    DomElement enemyCard = iterator.next();
                    DomElement interaction = iterator.next();
                    DomElement playerCard = iterator.next();
                    int enemyCardStrength = Integer.parseInt(enemyCard.asText());
                    int playerCardStrength = Integer.parseInt(playerCard.asText());
                    double powerFactor = Double.parseDouble(interaction.asText().split(" ")[1]);
                    double deltaPlayerHealth = enemyCardStrength * (2 - powerFactor);
                    double deltaEnemyHealth = playerCardStrength * powerFactor;
                    if (deltaEnemyHealth - deltaPlayerHealth > maxDamage) {
                        maxDamage = deltaEnemyHealth - deltaPlayerHealth;
                        cardSelection = (HtmlAnchor) playerCard;
                    }
                }
                currentPage = cardSelection.click();
            }

            String resultDuel = ((HtmlElement) currentPage.getFirstByXPath("//*[@class='msg2 gld85 mt5']")).asText();
            resultDuel = new String(resultDuel.getBytes(), "Cp1251");
            if (resultDuel.contains("Победа"))
                Win++;
            if (resultDuel.contains("Поражение"))
                Defeat++;
            logger.info(resultDuel);
            currentPage = (HtmlPage) mainPage.refresh();
        } catch (Exception e) {
            logger.severe("Exception: " + e.toString());
        }
        return false;
    }

    public HtmlPage login(String login, String password) {
        if (IsLogin)
            logout();

        FileHandler fh = null;
        try {
            fh = new FileHandler("%t" + login + ".log", true);
//            fh = new FileHandler(login+".log", true);
            fh.setFormatter(new SimpleFormatter());
        } catch (IOException e) {
            logger.severe("Exception: " + e.toString());
        }
        logger.addHandler(fh);
        HtmlAnchor entranceForPlayers = currentPage.getAnchorByHref("/login/");//.getAnchorByText("Вход для игроков");
//        for (HtmlAnchor htmlAnchor: currentPage.getAnchors()){
//            if (htmlAnchor.getHrefAttribute().startsWith("/login/"))
//                entranceForPlayers = htmlAnchor;
//        }
        try {
            HtmlPage loginPage = entranceForPlayers.click();

            HtmlForm formLogin = loginPage.getForms().get(0);
            HtmlSubmitInput button = formLogin.getInputByValue("Вход");
            HtmlTextInput inputLogin = formLogin.getInputByName("plogin");
            HtmlPasswordInput inputPassword = formLogin.getInputByName("ppass");
            inputLogin.setValueAttribute(login);
            inputPassword.setValueAttribute(password);
            currentPage = button.dblClick();
        } catch (IOException e) {
            logger.severe("Exception: " + e.toString());
            return null;
        }
        if (!currentPage.getBody().getTextContent().contains("Указан неверный логин или пароль")) {
            IsLogin = true;
            HtmlElement htmlStrength = (HtmlElement) currentPage.getByXPath("//*[@class='c_da']").get(0);
            Strength = Integer.parseInt(htmlStrength.asText().replaceAll(" ", ""));
            logger.info("Login game  as " + login);
        }
        else {
            logger.severe("Can't login gems as " + login + ". Please check login and password");
            return null;
        }
        mainPage = currentPage;
        return mainPage;
    }

    public boolean logout() {
        try {
            currentPage = (HtmlPage) mainPage.refresh();
            HtmlAnchor exit = currentPage.getAnchorByText("Выход");
            currentPage = exit.click();
            logger.info("Logout of the game");
            IsLogin = false;
        } catch (Exception e) {
            logger.severe("Exception: " + e.toString());
        }
        return currentPage.getUrl().toString().equals("http://elem.mobi/start");
    }
}