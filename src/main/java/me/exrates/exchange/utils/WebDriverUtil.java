package me.exrates.exchange.utils;

import groovy.util.AntBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.io.File;
import java.util.HashMap;

import static java.util.Objects.requireNonNull;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class WebDriverUtil {
    
    private static final String DELIMETER = "/";

    private static final String PHANTOM_JS = "phantomjs";
    private static final String CHROME = "chrome";

    public static WebDriver getPhantomJSDriver(String driverVersion) {
        requireNonNull(driverVersion, "Driver version must be provided");

        String platform;
        String archiveExtension;
        String execFilePath;

        final Platform currentPlatform = Platform.getCurrent();

        if (currentPlatform.is(Platform.WINDOWS)) {
            execFilePath = "/phantomjs.exe";
            platform = "windows";
            archiveExtension = "zip";
        } else if (currentPlatform.is(Platform.MAC)) {
            execFilePath = "/bin/phantomjs";
            platform = "macosx";
            archiveExtension = "zip";
        } else if (currentPlatform.is(Platform.LINUX)) {
            execFilePath = "/bin/phantomjs";
            platform = "linux-x86_64";
            archiveExtension = "tar.bz2";
        } else {
            throw new RuntimeException("Unsupported operating system [${Platform.current}]");
        }

        String phantomJSExecPath = String.join(StringUtils.EMPTY, "phantomjs", "-", driverVersion, "-", platform, execFilePath);

        String phantomJSFullDownloadPath = String.join(StringUtils.EMPTY, "https://bitbucket.org/ariya/phantomjs/downloads/phantomjs", "-", driverVersion, "-", platform, ".", archiveExtension);

        File phantomJSDriverLocalFile = downloadDriver(phantomJSFullDownloadPath, phantomJSExecPath, archiveExtension, PHANTOM_JS);

        System.setProperty("phantomjs.binary.path", phantomJSDriverLocalFile.getAbsolutePath());

        return new PhantomJSDriver();
    }

    public static WebDriver getChromeDriver(String driverVersion) {
        requireNonNull(driverVersion, "Driver version must be provided");

        String platform;
        String archiveExtension;
        String execFilePath;

        final Platform currentPlatform = Platform.getCurrent();

        execFilePath = "/chromedriver";
        archiveExtension = "zip";
        if (currentPlatform.is(Platform.WINDOWS)) {
            platform = "win64";
        } else if (currentPlatform.is(Platform.MAC)) {
            platform = "mac64";
        } else if (currentPlatform.is(Platform.LINUX)) {
            platform = "linux64";
        } else {
            throw new RuntimeException("Unsupported operating system [${Platform.current}]");
        }

        String chromeExecPath = String.join(StringUtils.EMPTY, "chromedriver", "-", driverVersion, "-", platform, execFilePath);

        String chromeFullDownloadPath = String.join(StringUtils.EMPTY, "https://chromedriver.storage.googleapis.com", "/", driverVersion, "/chromedriver", "_", platform, ".", archiveExtension);

        File chromeDriverLocalFile = downloadDriver(chromeFullDownloadPath, chromeExecPath, archiveExtension, CHROME);

        System.setProperty("webdriver.chrome.driver", chromeDriverLocalFile.getAbsolutePath());

        return new ChromeDriver();
    }

    private static File downloadDriver(String driverDownloadFullPath, String driverFilePath, String archiveFileExtension, String browser) {
        final String folderName = driverFilePath.split(DELIMETER)[0];
        final String fileName = driverFilePath.split(DELIMETER)[1];
        
        String driversPath = browser.equals(CHROME) ? "target/drivers/" + folderName : "target/drivers";

        File destinationDirectory = new File(driversPath);
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs();
        }
        File driverFile = new File(String.join("/", destinationDirectory.getAbsolutePath(), browser.equals(CHROME) ? fileName : driverFilePath));

        String localArchivePath = String.join(".", "target/driver", archiveFileExtension);

        if (!driverFile.exists()) {
            AntBuilder antBuilder = new AntBuilder();
            antBuilder.invokeMethod("get", new HashMap() {{
                put("src", driverDownloadFullPath);
                put("dest", localArchivePath);
            }});

            if (archiveFileExtension.equals("zip")) {
                antBuilder.invokeMethod("unzip", new HashMap() {{
                    put("src", localArchivePath);
                    put("dest", destinationDirectory);
                }});
            } else {
                antBuilder.invokeMethod("untar", new HashMap() {{
                    put("src", localArchivePath);
                    put("dest", destinationDirectory);
                    put("compression", "bzip2");
                }});
            }

            antBuilder.invokeMethod("delete", new HashMap() {{
                put("file", localArchivePath);
            }});
            antBuilder.invokeMethod("chmod", new HashMap() {{
                put("file", driverFile);
                put("perm", "700");
            }});
        }
        return driverFile;
    }
}