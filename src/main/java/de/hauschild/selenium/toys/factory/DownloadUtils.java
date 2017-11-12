package de.hauschild.selenium.toys.factory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.function.Consumer;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public enum DownloadUtils {

  ;

  public static String getString(final String url) {
    try (final CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
      try (final CloseableHttpResponse response = httpClient.execute(new HttpGet(url))) {
        handleHttpStatus(response);
        return EntityUtils.toString(response.getEntity()).trim();
      }
    } catch (final IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  public static void download(final String url, final Consumer<InputStream> downloadHandler) {
    try (final CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
      try (final CloseableHttpResponse response = httpClient.execute(new HttpGet(url))) {
        handleHttpStatus(response);
        try (final InputStream inputStream =
            new BufferedInputStream(response.getEntity().getContent())) {
          downloadHandler.accept(inputStream);
        }
      }
    } catch (final IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  public static void downloadZipAndExtract(final String url, final File targetDirectory) {
    download(url, inputStream -> {
      try {
        final byte[] bytes = IOUtils.toByteArray(inputStream);
        try (final ZipFile zipFile = new ZipFile(new SeekableInMemoryByteChannel(bytes))) {
          final Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
          while (entries.hasMoreElements()) {
            final ZipArchiveEntry entry = entries.nextElement();
            final File file = new File(targetDirectory, entry.getName());
            if (entry.isDirectory()) {
              file.mkdir();
              continue;
            }
            try (final OutputStream outputStream =
                new BufferedOutputStream(new FileOutputStream(file))) {
              IOUtils.copy(zipFile.getInputStream(entry), outputStream);
            }
          }
        }
      } catch (final IOException exception) {
        throw new RuntimeException(exception);
      }
    });
  }

  public static void handleHttpStatus(final CloseableHttpResponse response) throws IOException {
    final int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode != HttpStatus.SC_OK) {
      throw new IOException(String.format("Status code: %s", statusCode));
    }
  }

}
