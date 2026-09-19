package de.ttd.ttb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TtbPropertiesTest {
  @TempDir Path sdk;

  @Test
  void validatesWithSdkDirAndNoAppiumPath() {
    new TtbProperties("localhost:5555", sdk.toString(), "").validate();
    new TtbProperties("localhost:5555", sdk.toString(), null).validate();
  }

  @Test
  void rejectsBlankDeviceId() {
    final var ex =
        assertThrows(
            IllegalStateException.class,
            () -> new TtbProperties(" ", sdk.toString(), "").validate());
    assertTrue(ex.getMessage().contains("ttb.device-id"));
  }

  @Test
  void rejectsMissingAndroidHome() {
    final var blank =
        assertThrows(
            IllegalStateException.class,
            () -> new TtbProperties("localhost:5555", "", "").validate());
    assertTrue(blank.getMessage().contains("ANDROID_HOME"));

    final var missing =
        assertThrows(
            IllegalStateException.class,
            () ->
                new TtbProperties("localhost:5555", sdk.resolve("nope").toString(), "")
                    .validate());
    assertTrue(missing.getMessage().contains("does not point at a directory"));
  }

  @Test
  void rejectsAppiumPathThatIsNotAFile() {
    final var ex =
        assertThrows(
            IllegalStateException.class,
            () ->
                new TtbProperties("localhost:5555", sdk.toString(), sdk.toString()).validate());
    assertTrue(ex.getMessage().contains("ttb.appium-js-path"));
  }

  @Test
  void appiumJsIsEmptyWhenBlankAndPresentWhenSet() throws IOException {
    final var mainJs = Files.createFile(sdk.resolve("main.js"));
    assertTrue(new TtbProperties("localhost:5555", sdk.toString(), "").appiumJs().isEmpty());
    assertEquals(
        mainJs.toFile(),
        new TtbProperties("localhost:5555", sdk.toString(), mainJs.toString())
            .appiumJs()
            .orElseThrow());
  }

  @Test
  void adbPathPrefersSdkPlatformToolsAndFallsBackToPath() throws IOException {
    assertEquals("adb", new TtbProperties("localhost:5555", sdk.toString(), "").adbPath());

    final var adb = Files.createDirectories(sdk.resolve("platform-tools")).resolve("adb");
    Files.createFile(adb);
    assertTrue(adb.toFile().setExecutable(true));
    assertEquals(
        new File(sdk.toFile(), "platform-tools/adb").getAbsolutePath(),
        new TtbProperties("localhost:5555", sdk.toString(), "").adbPath());
  }
}
