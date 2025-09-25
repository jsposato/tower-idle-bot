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

### Setting up the backend system

1. Install tesseract: `yay -S tesseract tesseract-data-eng`
2. Install appium: `npm install -g appium`
3. Install appium drive: `npm install -g appium-uiatomator2-driver` && `appium driver install uiautomator2`
4. Install Android Sdk and plattform tools, it references my system paths in Device.java, you have to change these

### Setting up the container

1. Generate the redroid image: `python redroid.py -a 11.0.0 -gmnw`
2. Start the container and scrcpy into it: `adb connect localhost:5555` and `scrcpy -s localhost:5555`
3. Register the device with google
4. Connect as root: `adb -s localhost:5555 root`
5. Query the android_id:
   `adb -s localhost:5555 shell 'sqlite3 /data/data/com.google.android.gsf/databases/gservices.db "select * from main where name = \"android_id\";"'`
6. Register the device [at Google](https://www.google.com/android/uncertified)
7. Login in to Google Play Services
8. Install the current version of `The Tower`
9. Log in to your `The Tower`-Account
10. Configure the system time to match your phones to prevent that the Gem button is not hidden

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