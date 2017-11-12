package de.hauschild.selenium.toys.factory.phantomjs;

import java.io.File;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import de.hauschild.selenium.toys.factory.AbstractWebDriverFactory;
import de.hauschild.selenium.toys.factory.DownloadUtils;

public class PhantomJSWebDriverFactory extends AbstractWebDriverFactory {

  private static boolean INITIALIZED = false;

  private static void initialize() {
    if (INITIALIZED) {
      return;
    }

    final File tempDirectory = new File(System.getProperty("java.io.tmpdir"));
    final File driverDirectory = new File(tempDirectory, "phantomJs");
    driverDirectory.mkdir();
    DownloadUtils.downloadZipAndExtract(
        "https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-windows.zip",
        driverDirectory);

    final File driverExecutable = new File(
        new File(new File(driverDirectory, "phantomjs-2.1.1-windows"), "bin"), "phantomjs.exe");
    System.setProperty("phantomjs.binary.path", driverExecutable.getAbsolutePath());
    INITIALIZED = true;
  }

  @Override
  protected WebDriver create(final Class<?> testClass,
      final de.hauschild.selenium.toys.WebDriver webDriverAnnotation,
      final Map<String, String> options) {
    initialize();
    return new PhantomJSDriver();
  }

}
