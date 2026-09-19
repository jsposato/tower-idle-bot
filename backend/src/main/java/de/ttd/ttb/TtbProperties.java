package de.ttd.ttb;

import java.io.File;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Machine-specific settings, bound from {@code ttb.*} in application.properties or the
 * corresponding {@code TTB_*} environment variables.
 *
 * @param deviceId ADB serial of the emulator, e.g. {@code localhost:5555}
 * @param androidHome Android SDK root handed to Appium; defaults to {@code $ANDROID_HOME}
 * @param appiumJsPath Appium's {@code build/lib/main.js}; blank lets Appium's Java client locate it
 *     via {@code APPIUM_PATH} or {@code npm root -g}
 */
@ConfigurationProperties(prefix = "ttb")
public record TtbProperties(String deviceId, String androidHome, String appiumJsPath) {

  /**
   * Fails fast with a readable message instead of an opaque Appium/ADB error. Called by {@link
   * Device} before it touches hardware, not at bind time, so contexts without a device can still
   * load these properties.
   */
  public void validate() {
    if (deviceId == null || deviceId.isBlank()) {
      throw new IllegalStateException("ttb.device-id must be set (e.g. localhost:5555)");
    }
    if (androidHome == null || androidHome.isBlank()) {
      throw new IllegalStateException(
          "ttb.android-home must be set, or export ANDROID_HOME, so Appium can find the Android SDK");
    }
    if (!new File(androidHome).isDirectory()) {
      throw new IllegalStateException(
          "ttb.android-home does not point at a directory: " + androidHome);
    }
    if (appiumJs().filter(it -> !it.isFile()).isPresent()) {
      throw new IllegalStateException(
          "ttb.appium-js-path does not point at a file: " + appiumJsPath);
    }
  }

  /** Appium main.js when explicitly configured; empty means let the Java client find it. */
  public Optional<File> appiumJs() {
    return appiumJsPath == null || appiumJsPath.isBlank()
        ? Optional.empty()
        : Optional.of(new File(appiumJsPath));
  }

  /** {@code $ANDROID_HOME/platform-tools/adb} if it exists, otherwise {@code adb} from PATH. */
  public String adbPath() {
    final var bundled = new File(androidHome, "platform-tools/adb");
    return bundled.canExecute() ? bundled.getAbsolutePath() : "adb";
  }
}
