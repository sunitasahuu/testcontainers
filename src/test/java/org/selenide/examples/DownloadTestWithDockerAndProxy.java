package org.selenide.examples;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.proxy.SelenideProxyServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BrowserWebDriverContainer;

import java.io.File;
import java.io.FileNotFoundException;

import static com.codeborne.selenide.FileDownloadMode.HTTPGET;
import static com.codeborne.selenide.FileDownloadMode.PROXY;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.files.FileFilters.withName;
import static org.junit.Assert.assertTrue;

public class DownloadTestWithDockerAndProxy {

    private static final int proxyPort = 8864;

    private SelenideProxyServer proxyServer;

    static {
        Testcontainers.exposeHostPorts(proxyPort);
    }

    //if your baseUrl is http, you'll need to use new Proxy().setHttpProxy
    @Rule
    public BrowserWebDriverContainer chrome =
            new BrowserWebDriverContainer()
                    .withCapabilities(new ChromeOptions().setProxy(new Proxy()
                            .setSslProxy("host.testcontainers.internal:" + proxyPort))
                            .setAcceptInsecureCerts(true));

    @Before
    public void setUp() {
        Configuration.proxyEnabled = true;
        Configuration.fileDownload = PROXY;

        SelenideConfig config = new SelenideConfig()
            .proxyHost("host.testcontainers.internal")
            .proxyPort(proxyPort)
            .proxyEnabled(true);
        proxyServer = new SelenideProxyServer(config, null);
        proxyServer.start();

        RemoteWebDriver driver = chrome.getWebDriver();
        WebDriverRunner.setWebDriver(driver, proxyServer);
    }

    @Test
    public void search() throws FileNotFoundException {
        open("https://mvnrepository.com/artifact/com.codeborne/selenide/5.24.2");

        File selenideJar = $("#maincontent .grid").find(withText("jar")).download(withName("selenide-5.24.2.jar"));
        assertTrue(selenideJar.exists());
    }

    @After
    public void tearDown() {
        proxyServer.shutdown();
        WebDriverRunner.closeWebDriver();
        Configuration.fileDownload = HTTPGET;
        Configuration.proxyEnabled = false;
        Configuration.proxyHost = null;
        Configuration.proxyPort = 0;
    }
}
