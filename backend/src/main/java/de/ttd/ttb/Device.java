package de.ttd.ttb;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.NullOutputReceiver;
import de.ttd.ttb.capture.Pixel;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Device {
  private static final String ANDROID_HOME = "/home/shino/Android/Sdk";
  private static final String DEVICE_ID = "localhost:5555";
  private static final String APPIUM_JS_PATH =
      "/home/shino/.nvm/versions/node/v22.14.0/lib/node_modules/appium/build/lib/main.js";

  private IDevice device;

  private Process process;
  private BufferedWriter writer;
  private BufferedReader reader;

  AndroidDriver androidDriver;
  AppiumDriverLocalService appiumDriverLocalService;

  private boolean initializing = false;

  public void captureTestData() {
    try {
      final var image = this.capture();
      final var now = LocalDateTime.now();
      final var outputfile =
          new File(
              "test_data/"
                  + now.getYear()
                  + "_"
                  + now.getHour()
                  + "_"
                  + now.getMinute()
                  + "_"
                  + now.getSecond()
                  + "_"
                  + now.getNano()
                  + ".png");
      ImageIO.write(image, "png", outputfile);
    } catch (Exception e) {

    }
  }

  public void tap(int x, int y) {
    if (this.initializing) {
      throw new IllegalStateException("Device is initializing");
    }

    final var finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    final var tap = new Sequence(finger, 1);
    tap.addAction(
        finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), x, y));
    tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
    androidDriver.perform(List.of(tap));
  }

  public void tap(Pixel pixel) {
    this.tap(pixel.x(), pixel.y());
  }

  // Precise swiping is hard
  public void swipe(int x, int y, int amount) throws Exception {
    if (this.initializing) {
      throw new IllegalStateException("Device is initializing");
    }

    final var finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    final var tap = new Sequence(finger, 1);
    tap.addAction(
        finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), x, y));
    tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    tap.addAction(
        finger.createPointerMove(
            Duration.ofMillis(400), PointerInput.Origin.viewport(), x, y + amount));
    tap.addAction(
        finger.createPointerMove(
            Duration.ofMillis(200), PointerInput.Origin.viewport(), x, y + amount));
    tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
    androidDriver.perform(List.of(tap));
  }

  public void startApp() {
    this.executeCommand(
        "monkey --pct-syskeys 0 -p com.TechTreeGames.TheTower -c android.intent.category.LAUNCHER 1");
  }

  private LocalDateTime lastCheck = LocalDateTime.now();

  public boolean isAppRunning() {
    if (lastCheck.isBefore(LocalDateTime.now())) {
      final var result = this.getRunningPackage();
      lastCheck = LocalDateTime.now().plusSeconds(5);
      return result != null && result.contains("com.TechTreeGames.TheTower");
    }
    return true;
  }

  private String getRunningPackage() {
    final var result = new StringBuffer();

    try {
      device.executeShellCommand(
          "dumpsys activity activities | grep mResumedActivity | cut -d \"{\" -f2 | cut -d ' ' -f3 | cut -d \"/\" -f1",
          new IShellOutputReceiver() {

            @Override
            public void addOutput(byte[] data, int offset, int length) {
              result.append(new String(data, offset, length));
            }

            @Override
            public void flush() {}

            @Override
            public boolean isCancelled() {
              return false;
            }
          });
      return result.toString();

    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  public BufferedImage capture() throws Exception {
    if (this.initializing) {
      throw new IllegalStateException("Device is initializing");
    }

    return ImageIO.read(new ByteArrayInputStream(androidDriver.getScreenshotAs(OutputType.BYTES)));
  }

  private void executeCommand(String command) {
    try {
      device.executeShellCommand(command, new NullOutputReceiver());
    } catch (Exception e) {
      // TODO: Handle
      e.printStackTrace();
    }
  }

  private void executeCommandViaShell(String command) {
    try {
      this.writer.write(String.format("timeout 5s %s; echo 'COMMAND DONE'\n", command));
      this.writer.flush();

      String line;
      while ((line = this.reader.readLine()) != null) {
        if (line.equals("COMMAND DONE")) {
          break;
        }
      }
    } catch (Exception e) {
    }
  }

  @PostConstruct
  private void init() throws Exception {
    this.initializing = true;

    // 3 ways to control the android device
    // Using ADB via the bridge
    AndroidDebugBridge.init(false);
    AndroidDebugBridge adb = AndroidDebugBridge.createBridge(5L, TimeUnit.SECONDS);
    this.waitFor(adb::isConnected);
    this.waitFor(adb::hasInitialDeviceList);

    device =
        Arrays.stream(adb.getDevices())
            .filter(it -> it.getSerialNumber().equals(DEVICE_ID))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Could not find device"));

    // Using an open ADB Shell
    ProcessBuilder builder = new ProcessBuilder("adb", "-s", DEVICE_ID, "shell");
    this.process = builder.start();
    this.writer =
        new BufferedWriter(new java.io.OutputStreamWriter(this.process.getOutputStream()));
    this.reader = new BufferedReader(new java.io.InputStreamReader(this.process.getInputStream()));

    // Using Appium
    this.initAppium();

    this.initializing = false;
  }

  private void initAppium() throws Exception {
    appiumDriverLocalService =
        AppiumDriverLocalService.buildService(
            new AppiumServiceBuilder()
                .withArgument(() -> "--log-level", "error")
                .withAppiumJS(new File(APPIUM_JS_PATH))
                .withEnvironment(
                    new HashMap<>() {
                      {
                        put("ANDROID_HOME", ANDROID_HOME);
                      }
                    }));
    appiumDriverLocalService.start();
    UiAutomator2Options options = new UiAutomator2Options().setUdid(DEVICE_ID);
    androidDriver = new AndroidDriver(new URI("http://127.0.0.1:4723").toURL(), options);
  }

  @PreDestroy
  private void cleanup() throws Exception {
    AndroidDebugBridge.disconnectBridge(5L, TimeUnit.SECONDS);

    // Direct Shell
    this.writer.close();
    this.reader.close();
    this.process.destroy();

    this.cleanupAppium();
  }

  private void cleanupAppium() throws Exception {
    androidDriver.quit();
    appiumDriverLocalService.stop();
  }

  private boolean skipFirstRestart = true;

  @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
  private void restart() throws Exception {
    if (this.skipFirstRestart) {
      this.skipFirstRestart = false;
      return;
    }

    // Appium is a little unstable, so it needs to be restarted regularly
    System.out.println("RESTARTING");
    this.initializing = true;
    this.cleanupAppium();
    this.initAppium();
    this.initializing = false;
    System.out.println("RESTART DONE!");
  }

  private void waitFor(Supplier<Boolean> until) {
    int trials = 5;
    while (trials > 0) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (until.get()) {
        break;
      }
      trials--;
    }

    if (trials == 0) {
      throw new RuntimeException("Waiting failed!");
    }
  }
}
