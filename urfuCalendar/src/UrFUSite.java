import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.w3c.dom.html.HTMLSelectElement;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Иван on 25.04.2015.
 */
public class UrFUSite {
    public Logger logger = Logger.getLogger(UrFUSite.class.getName());

    private WebClient webClient;
    public HtmlPage currentPage, mainPage;

    UrFUSite(){
        FileHandler fh = null;
        try {
            fh = new FileHandler("%tUrFUSite.log", true);
//            fh = new FileHandler(login+".log", true);
            fh.setFormatter(new SimpleFormatter());
        } catch (IOException e) {
            logger.severe("Exception: " + e.toString());
        }
        logger.addHandler(fh);

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
            currentPage = webClient.getPage("http://urfu.ru/ru/students/study/schedule/");
        } catch (MalformedURLException e) {
            logger.severe("Exception: " + e.toString());
        } catch (IOException e) {
            logger.severe("Exception: " + e.toString());
        }
    }

    public void getPage(){
        List<HtmlForm> a = currentPage.getForms();
        for (HtmlForm form: a){
            if (form.getAttribute("id").equals("schedule-form")){
                //System.out.println(currentPage.asText());
                HtmlButton submitInput = ((List<HtmlButton>)form.getByXPath("//*[@class='button-link f-right']")).get(0);
                HtmlSelect selectInstitute = form.getSelectByName("tx_urfuschedule_studentschedule[schedule][institute]");
                HtmlSelect selectCourse = form.getSelectByName("tx_urfuschedule_studentschedule[schedule][course]");
                HtmlSelect selectGroup = form.getSelectByName("tx_urfuschedule_studentschedule[schedule][group]");
                for (HtmlOption option: selectInstitute.getOptions()){
                    System.out.println(option.asText());
                }
                for (HtmlOption option: selectCourse.getOptions()){
                    System.out.println(option.asText());
                }
                for (HtmlOption option: selectGroup.getOptions()){
                    System.out.println(option.asText());
                }

                System.out.println(selectInstitute.getOption(0).asText());

                selectInstitute.setSelectedAttribute(selectInstitute.getOptionByValue("6"), true);
                selectCourse.setSelectedAttribute(selectCourse.getOptionByValue("3"), true);
                //System.out.print(currentPage.asText());
                //selectGroup.setSelectedAttribute(selectGroup.getOptionByValue("12"), true);
            }
        }
    }
}
