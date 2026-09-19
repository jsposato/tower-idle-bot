# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

TTB ("The Tower Bot") automates the mobile idle game *The Tower* running inside a redroid Android container. A Spring Boot app (`backend/`) screenshots the device, runs a YOLO11n object-detection model (`ttb.onnx`) plus Tesseract OCR over the frame, and taps/swipes via Appium. The original author abandoned it ("kinda works"); treat it as a working prototype, not a finished product. The `yolo/` directory is the separate Python training pipeline that produces `ttb.onnx`.

There is no CI, no lint config, and only one test (`contextLoads`). The `.idea/` config enables google-java-format (2-space indent) — match it.

## Commands

All Java work happens in `backend/`:

```sh
cd backend
./gradlew build          # compile + test
./gradlew test           # tests only
./gradlew test --tests 'de.ttd.ttb.TtbApplicationTests'   # single test class
./gradlew bootRun        # run the bot — requires a live device (see below)
```

`./gradlew bootRun` and `TtbApplicationTests` (a `@SpringBootTest`) both need a real environment: an ADB device at `ttb.device-id` (default `localhost:5555`), `ANDROID_HOME` set, a running Appium install, `ttb.onnx` and `tessdata/` resolvable from the **working directory** (the paths are relative: `./ttb.onnx`, `tessdata`), and native OpenCV. Without these the Spring context fails in `Device.init()` / `YoloPrediction`'s static initializer. Expect the test to fail on a dev machine with no device; that is the pre-existing state, not a regression.

Device/emulator setup (redroid image, Google registration, Appium install) is documented step-by-step in `README.md`; `docker-compose.yml` runs the redroid container at 720x1280 @ 320dpi, 120fps — every hardcoded tap coordinate in the code assumes that resolution.

YOLO training (`yolo/`, Python, ultralytics):

```sh
cd yolo
./startLabeler.sh        # Label Studio in Docker on :8654 (data in yolo/label-data)
python train.py          # trains yolo11n.pt on data_yolo/ → runs/detect/yolo_ttb/weights/best.pt
python to_onnx.py        # exports best.pt → ONNX; copy to repo-root ttb.onnx
python detect.py         # pre-labels images in yolo/data/ into yolo/data_labeled/
```

The Python scripts hardcode `sys.path` entries for the original author's pipx venvs (`/home/shino/...`); fix those before running.

## Machine-specific configuration

Device settings (`ttb.device-id`, `ttb.android-home`, `ttb.appium-js-path`) are bound into the `TtbProperties` record from `application.properties` / `TTB_*` env vars; `Device.init()` calls `TtbProperties.validate()` before touching ADB so a bad path fails with a named property rather than an Appium stack trace. Keep hardware-touching validation there, not at bind time, so a device-less test context can still load the properties.

Still hardcoded to the original author's Linux box: the `sys.path.append(...)` lines in `yolo/*.py`.

## Architecture

### Control loop

`Bot` (a `CommandLineRunner`) spins forever: ensure the game process is running, capture a frame at most once per second, then ask each `GameState` in order — `OverviewGS`, `BattleGS`, `GemAdGS` — whether it `isVisible()` and call `next()` on the first that is. All exceptions are swallowed silently (the catch block is empty), so a broken step shows up as the bot doing nothing, not as a stack trace.

### Perception: one frame, many questions

`CaptureService` holds the **current frame** and its **current YOLO predictions**. Everything else queries that cached frame; nothing captures on its own. A state calls `captureService.captureNext()` explicitly after an action it expects to change the screen (typically after `Thread.sleep(...)` to let animations settle). If you add an action and forget the `captureNext()`, the next decision is made against a stale frame.

Three perception mechanisms coexist, in decreasing order of use:

1. **YOLO** (`YoloPrediction`) — runs `ttb.onnx` via OpenCV DNN on a 640x640 letterbox, rescales boxes to 720x1280. Classes are the `Prediction` enum, whose **ordinal order must match `yolo/ttb.yaml` `names` order exactly** — the model outputs class indices, and `NUM_CLASSES` is hardcoded to 33. Retraining with a new class means updating `ttb.yaml`, `Prediction`, and `NUM_CLASSES` together.
2. **Tesseract OCR** (`ImageReader`) — reads text out of fixed pixel boxes (current wave in `BattleMetadata`, perk names in `BattleGS.selectPerk`). Unreliable; the README says perk reading "fails often".
3. **Pixel colour probes** (`CaptureService.isColorAt`) — used by `CashUpgradeService.updateCashUpgrade` to tell whether an upgrade button is maxed (grey) or too expensive (dark blue). `ImageMatcher` histogram comparison exists but is effectively unused.

### State detection by class-name prefix

`Prediction` names are prefixed `B_` (battle), `O_` (overview/home), `GA_` (gem ad). `CaptureService.isStateVisible(StatePredictionPrefix)` decides which screen is showing by checking whether *any* prediction with that prefix is present. So the set of trained classes is also the state machine's routing table: a new detectable element belongs to whatever screen its prefix says.

Per-class confidence thresholds live in `CaptureService.CONFIDENCE_THRESHOLDS`; unlisted classes fall back to `YoloPrediction.CONFIDENCE_THRESHOLD` (0.9). Tune there when a class is flapping.

### Battle logic (`battle/`)

`BattleGS.next()` is a priority ladder: game over → retry; ad-failed dialog; coin-bonus popup; perk selection; open perk menu; collect gem ad; otherwise read the wave, run up to 20 cash upgrades, tap any floating gem.

The cash-upgrade strategy is a static list in `CashUpgradeService` (the README calls this the configuration point). Structure:

- `CashUpgradeOption` — a record of `(label, tab, slot, row)` describing where an upgrade lives in the in-game shop grid (three tabs, two columns, N rows). The `getOptionPixel` / `scrollTo` math in `CashUpgradeService` turns that into tap coordinates and swipe distances (`SCROLL_SWIPE_OFFSET = 151` px per row).
- `BuyUntilTactic` — buy an option until wave N or until maxed.
- `RoundRobinTactic` — cycle through a list of `BuyUntilTactic`s, skipping invalid ones.
- `CashUpgradeStrategy` — wraps one round-robin; strategies are tried in order, advancing when every tactic in the current one is exhausted.

Scroll position is tracked in memory per tab (`currentScrollOffset`), which is why `BattleGS` resets `initialized` whenever the battle screen disappears (e.g. after a gem ad) — the shop must be re-scrolled to a known origin (`init()` swipes each tab to the top three times) before offsets are trustworthy again.

### Device (`Device.java`)

Wraps three channels to the emulator: ddmlib `AndroidDebugBridge` (shell commands: launch app, check foreground package), a raw `adb shell` process (mostly unused), and Appium/UiAutomator2 (screenshots, taps, swipes). Appium is restarted every 30 minutes via `@Scheduled` because it is unstable; during restart `initializing` is true and `tap`/`capture` throw — those throws are what the `Bot` loop's empty catch is absorbing.
