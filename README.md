# TTB - The Tower Bot

A bot to execute various tedious tasks in the mobile game `The Tower`.

Hey!
You stumbled here looking for a solution to automate this tedious game.
This bot kinda works (at least at the time of writing).
It brings everything to the table that you need to fully write a functioning bot.
All it needs is some dedication to finish it. I kinda lost interest in it ;)
Are you up for the task or know someone who is?

## Vision

The vision was to create a bot with a web interface. Using [ws-scrcpy](https://github.com/Shmayro/ws-scrcpy) one can add
an interactive screen to a web interface. What is left is to create an interface to control the bot remotely via this web interface.
Using redroid the bot could be run on any server on the CPU. This wouldnt be fast but would be good enough for farming.
Therefore, one would not need to let their computer running at home.

### Notes

I began to play around with a YOLO to enable computer vision.
With the current amount of data it is very unreliable but it somehow works.
One could tune the Prediction confidence in CaptureService.java.
Using proper computer vision one could automate the rest of the game.

I used the bot for two weeks, even in tournaments. Its pretty safe to use.
If you just want to get a lot of medals and gems etc. you would probably be more efficient to cheat using a Cheat Engine.

### Current state

* Auto retry battle
* Collect Gem Ads (Running it 24/7 you can get up to 806 gems per day)
* Collect Floating Gems
* Execute a cash upgrade strategy (Configurable in CashUpgradeService.java)
* Select perks (Although it fails to read it often)

## Dependencies

* [redroid](https://github.com/remote-android/redroid-doc): The android emulator
* [redroid-script](https://github.com/ayasa520/redroid-script): To create an android redroid image with everything required
* [scrcpy](https://github.com/Genymobile/scrcpy): To get vision of the android device
* [tesseract](https://github.com/tesseract-ocr): To read text
* Appium: To control the bot using uiautomator2
* YOLO: Computer vision

## Installation

The bot has two halves that can live on different machines:

* **The controller** — the Java backend in `backend/`, plus Appium, `adb`, and tesseract. Runs anywhere Java 21 runs (macOS, Linux).
* **The Android container** — redroid. **Must run on a Linux kernel with the `binder` modules.** Docker Desktop on macOS cannot host it: its LinuxKit VM has no binder support, so `docker compose up` fails at boot. On a Mac, run redroid on a remote Linux box or inside a Linux VM and point `adb connect` at it.

### Setting up the controller

#### Arch Linux (original setup)

1. Install tesseract: `yay -S tesseract tesseract-data-eng`
2. Install appium: `npm install -g appium`
3. Install the uiautomator2 driver: `appium driver install uiautomator2`
4. Install the Android SDK platform-tools and build-tools and set `ANDROID_HOME`.

#### macOS

1. JDK 21: `brew install openjdk@21` (or use sdkman). Gradle's toolchain block requires 21 exactly.
2. Tesseract: `brew install tesseract`. tess4j loads `libtesseract` through JNA; if it reports it cannot find the library, pass `-Djna.library.path=/opt/homebrew/lib` (Apple Silicon) or `/usr/local/lib` (Intel) when running the bot. The English model is already in `tessdata/` at the repo root.
3. Android SDK: `brew install --cask android-commandlinetools`, then
   `sdkmanager "platform-tools" "build-tools;35.0.0"` and export `ANDROID_HOME` (Homebrew puts the SDK under `/opt/homebrew/share/android-commandlinetools`). Appium's uiautomator2 driver needs both `adb` and `apksigner` from these.
4. Appium: `npm install -g appium && appium driver install uiautomator2`. Find the path to Appium's `main.js` with `npm root -g` (it is `<npm root>/appium/build/lib/main.js`).
5. Optional but useful: `brew install scrcpy` to watch the device.

#### Configuring the controller

Machine-specific settings live in `backend/src/main/resources/application.properties` under `ttb.*` and can be overridden with environment variables:

| Property | Env var | Default | Meaning |
|---|---|---|---|
| `ttb.device-id` | `TTB_DEVICE_ID` | `localhost:5555` | ADB serial of the redroid container |
| `ttb.android-home` | `TTB_ANDROID_HOME` | `$ANDROID_HOME` | Android SDK root passed to Appium; `platform-tools/adb` under it is used when present |
| `ttb.appium-js-path` | `TTB_APPIUM_JS_PATH` | *(blank)* | Appium's `build/lib/main.js`; blank lets the Appium Java client find it via `$APPIUM_PATH` or `npm root -g` |

With `ANDROID_HOME` exported and Appium installed globally, no edits are needed: `cd backend && ./gradlew bootRun`. The bot fails fast at startup with a message naming the property if any of these is wrong.

### Setting up the container

#### Where to run redroid

* **Linux host** (Ubuntu 20.04+, Arch, etc.): works as-is. Check `ls /dev/binder*`; if missing, `modprobe binder_linux devices="binder,hwbinder,vndbinder"` and see the [redroid deploy notes](https://github.com/remote-android/redroid-doc/blob/master/deploy/README.md) for your distro.
* **macOS with a Linux VM**: Colima or Lima with an Ubuntu image (`colima start --vm-type vz --cpu 4 --memory 8`) gives you a Docker daemon inside a kernel that has binder. Run the compose file there and `adb connect` to the VM's forwarded port. Software rendering only — see the GPU note below.
* **macOS with a remote Linux box**: run the container there, expose port 5555 over an SSH tunnel (`ssh -L 5555:127.0.0.1:5555 host`), then `adb connect localhost:5555` locally. `docker-compose.yml` binds 5555 to `127.0.0.1` on purpose so the device is never exposed to the network.

#### docker-compose.yml flags to adjust

The compose file is written for an **x86_64 host with a host GPU**. Two groups of flags are host-specific and are commented in the file:

* `androidboot.redroid_gpu_mode=host` needs `/dev/dri` on the host. In a VM or on a headless server, change it to `guest` (software rendering). It is slower but fine for farming; the README's vision section already assumes a CPU-only server.
* The `ro.product.cpu.abilist*`, `ro.dalvik.vm.isa.*`, `ro.enable.native.bridge.exec`, `ro.dalvik.vm.native.bridge` and `ro.ndk_translation.version` lines enable **libndk_translation** so ARM-only APKs run on an x86_64 container. On an ARM64 host (Apple Silicon VM, ARM server) drop all of them and build an arm64 image; the `-n` (ndk) option in redroid-script is x86_64-only.

#### Steps

1. Clone [redroid-script](https://github.com/ayasa520/redroid-script) (into `redroid-script/` at the repo root if you like — it is git-ignored) and generate the image: `python redroid.py -a 11.0.0 -gmnw` (`-g` GApps, `-m` Magisk, `-n` ndk translation — x86_64 only, `-w` Widevine). The output tag must match the `image:` in `docker-compose.yml`.
2. Start the container: `docker compose up -d`, then `adb connect localhost:5555` and `scrcpy -s localhost:5555`.
3. Register the device with Google:
   1. Connect as root: `adb -s localhost:5555 root`
   2. Query the android_id:
      `adb -s localhost:5555 shell 'sqlite3 /data/data/com.google.android.gsf/databases/gservices.db "select * from main where name = \"android_id\";"'`
   3. Register it [at Google](https://www.google.com/android/uncertified)
4. Log in to Google Play Services.
5. Install the current version of `The Tower`.
6. Log in to your `The Tower` account.
7. Configure the system time to match your phone's, otherwise the Gem button stays hidden.

### Performance Settings

1. Check 120 FPS option
2. Disable hit texts
3. Screen Refresh rate?
4. TODO: Reduce DPI
5. TODO: Reduce Resolution?

## Game Speed and Spawn rates

* Spawn is every 125 frames
* Game Speed skips frames
* So to get to higher waves one has to choose a number with less overlaps
* So to get more spawns, one has to choose a number with exact overlaps

## Useful links

* https://github.com/Shmayro/ws-scrcpy-docker
* https://github.com/Shmayro/dockerify-android
* https://github.com/HumanSignal/label-studio