package me.exrates.exchange.configurations;

import me.exrates.exchange.utils.WebDriverUtil;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
public class WebDriverConfiguration {

    @Value("${phantom-js.driver.version}")
    private String phantomJSDriverVersion;

    @Value("${chrome.driver.version}")
    private String chromeDriverVersion;

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Bean
    public WebDriver phantomJSDriver() {
        return WebDriverUtil.getPhantomJSDriver(phantomJSDriverVersion);
    }

//    @Order(Ordered.HIGHEST_PRECEDENCE)
//    @Bean
//    public WebDriver chromeDriver() {
//        return WebDriverUtil.getChromeDriver(chromeDriverVersion);
//    }
}