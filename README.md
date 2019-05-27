# DM-2: Bandits (Reinforcement Learning Strategies)![GNU GPL v3](https://www.gnu.org/graphics/gplv3-88x31.png)
   [![Build Status](https://travis-ci.org/uds-se/droidmate-bandits.svg?branch=master)](https://travis-ci.org/uds-se/droidmate-bandits)
   [![](https://jitpack.io/v/uds-se/droidmate-bandits.svg)](https://jitpack.io/#uds-se/droidmate-bandits)

Strategies for [DroidMate-2](https://github.com/uds-se/droidmate): A Platform for Android Test Generation.

[ISSTA'19 paper: Learning User Interface Element Interactions](https://publications.cispa.saarland/2883/)

# Directory Convention

The tool can be downloaded to any directory.
In this document we name the directory where the tool is located as `<DM>`

By default input apks should be located in `<DM>/apks` and the output will be store in `<DM>/out`

# Building

__DM-2-Bandits__ is built on top of DroidMate-2 and automatically downloads all dependencies from JitPack and Maven Central.
 

## Environment configuration

It requires both JDK and Android SDK to be installed and their respective environment variables `JAVA_HOME` and `ANDROID_HOME` to be configured.

[Here](https://www.ntu.edu.sg/home/ehchua/programming/android/Android_HowTo.html) is a link with detailed about how to install and the JDK and Android SDK.

The tool is also integrated into the Travis-CI, its [configuration file](https://github.com/uds-se/droidmate-bandits/blob/master/.travis.yml) contains all commands used to configure and execute it.  

In case of trouble compiling the tool, have a look at the [DM-2 wiki](https://github.com/uds-se/droidmate/wiki/Building) 

## Android Devices

The tool requires a device or emulator to be executed and the device has to be recognized by the `adb devices` command.
The emulator or device _must be connected_ before executing the tool.

We don't ship a device emulator with the tool to facilitate its integration on different setups, such as physical devices, preferred emulator or external device farms.

### Emulator Support 

The tool can be used with an emulator. 
The Android Virtual Device (AVD) emulator is part of the SDK. 
Google provides the [following tutorial](https://developer.android.com/studio/run/managing-avds) on how to create a virtual device using Android Studio and [this tutorial](https://developer.android.com/studio/run/emulator-commandline) to start it from the command line.  
We mainly use Nexus 5X API 25 or Google Pixel XL API 26.

### Physical Devices

Check DM-2 list of [supported device models](https://github.com/uds-se/droidmate/wiki/Compatible-devices).

## Compiling

on `<DM>` folder execute `/gradlew clean build` (Linux/OSX) or `gradlew.bat clean build` (Windows)

The Gradle wrapper will automatically download all necessary dependencies

## Testing your installation

To confirm that everything worked fine execute:

```
./gradlew run --args="--help"
```

It will list all available configuration parameters.

## Obtaining apks

Apks for testing can be downloaded from [F-Droid](https://f-droid.org/) or alternative app stores, such as [ApkPure](https://apkpure.com/).

# Running the experiments

The tool is executed in 2 steps: _instrumentation_ and _exploration_.

Instrumentation recompile the APK with log statements so that we can monitor the coverage during testing.
Exploration executes the app and interacts with it.

By default both steps search for apks in the `<DM>/apks` directory.
Also, the _instrumentation_ step produces its output in the `<DM>/apks` directory, while the _exploration_ step stores its results in the `<DM>/out/droidMate` directory.  

__Please note that the output folder is cleaned after each exploration, if you sequentially execute multiple experiments your results will be overwritten.__ 

## Step 1: Instrumenting apps for code coverage

For the tool to obtain coverage, the apps should first be instrumented. 
on `<DM>` folder execute the command below to instrument an app (it will take the first app from the `<DM>/apks` folder).

```
./gradlew run --args="--ExecutionMode-explore=false --ExecutionMode-coverage=true"
```

Once the instrumentation is complete it produces 2 files:
* Instrumented APK
* List of instrumented statements

__Before executing an exploration, remove the original apk file from the input folder__.

## Step 2: Exploring an app  

### Executing an exploration with FPS (Fitness Proportionate Selection) -- Only Static Model

To run the experiment with the baseline strategy for 100 clicks, on `<DM>` folder execute:

```
./gradlew run --args="-fps true --StatementCoverage-enableCoverage=true --Selectors-randomSeed=0 --Selectors-actionLimit=100"
```

### Executing an exploration with FPS-H (Fitness Proportionate Selection Hybrid) -- Static Model + RL

To run the experiment with the baseline strategy for 100 clicks, on `<DM>` folder execute:

```
./gradlew run --args="-fpsh true --StatementCoverage-enableCoverage=true --Selectors-randomSeed=0 --Selectors-actionLimit=100"
```

### Executing an exploration with Epsilon-Greedy -- Only RL

To run the experiment with the baseline strategy for 100 clicks, on `<DM>` folder execute:

```
./gradlew run --args="-e true --StatementCoverage-enableCoverage=true --Selectors-randomSeed=0 --Selectors-actionLimit=100"
```

### Executing an exploration with Epsilon-Greedy Hybrid -- Static Model + RL

To run the experiment with the baseline strategy for 100 clicks, on `<DM>` folder execute:

```
./gradlew run --args="-eh true --StatementCoverage-enableCoverage=true --Selectors-randomSeed=0 --Selectors-actionLimit=100"
```

### Executing an exploration with Thompson Sampling -- Only RL

To run the experiment with the baseline strategy for 100 clicks, on `<DM>` folder execute:

```
./gradlew run --args="-t true --StatementCoverage-enableCoverage=true --Selectors-randomSeed=0 --Selectors-actionLimit=100"
```

### Executing an exploration with Thompson Sampling Hybrid -- Static Model + RL

To run the experiment with the baseline strategy for 100 clicks, on `<DM>` folder execute:

```
./gradlew run --args="-th true --StatementCoverage-enableCoverage=true --Selectors-randomSeed=0 --Selectors-actionLimit=100"
```

__Note:__ The instrumentation keeps the original (non-instrumented) app in the same location, to run only the version with coverage move the original apk to another folder.  


# Reading the results

The results are by default stored in the `<DM>/out/droidMate` directory.

The most significant elements of the output folder are:
* `coverage`: contains the statements reached in each exploration action. 
This data can be compared against the original set of instrumented statements to obtain coverage.
* `model`: contains the DM-2 model of the app, with all performed actions (`trace*.csv`) as well as all observed `states` and `widgets`.
* `log`: Copy of the device logcat during exploration.

For the paper experiments we used only the reached statements from the `<DM>/out/droidMate/coverage`.

__If the coverage folder is empty you are executing an apk which has not been instrumented. Please check Step 1__

# Misc comments

## List all available arguments:

To list all available parameters, run with `--help`

```
./gradlew run --args="--help"
```

## Importing on another project

To import Dm-2 bandits on another project built on top of the DroidMate-2 platform follow the [Jitpack instructions](https://jitpack.io/#uds-se/droidmate-bandits/master-SNAPSHOT)


# Troubleshooting

## Window could not be extracted

If you see the following error message:

```
14:49:43 WARN  [main @coroutine#1]  o.droidmate.command.ExploreCommand  initial fetch (warnIfNotHomeScreen) failed
java.lang.IllegalStateException: Error: Displayed Windows could not be extracted [DisplayedWindow(w=AppWindow(windowId=969, pkgName=com.android.systemui, hasInputFocus=false, hasFocus=false, boundaries=0:0:1440:84), initialArea=[Rect(0, 0 - 1440, 84)], rootNode=null, isKeyboard=false, layer=1, bounds=Rect(0, 0 - 1440, 84), windowType=3, isLauncher=false)]
	at org.droidmate.uiautomator2daemon.uiautomatorExtensions.UiAutomationEnvironment.getDisplayedWindows(UiAutomationEnvironment.kt:191)
	at org.droidmate.uiautomator2daemon.uiautomatorExtensions.UiAutomationEnvironment$getDisplayedWindows$1.invokeSuspend(Unknown Source)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:32)
	at kotlinx.coroutines.ResumeModeKt.resumeMode(ResumeMode.kt:67)
	at kotlinx.coroutines.DispatchedKt.resume(Dispatched.kt:272)
	at kotlinx.coroutines.DispatchedKt.dispatch(Dispatched.kt:261)
	at kotlinx.coroutines.CancellableContinuationImpl.dispatchResume(CancellableContinuationImpl.kt:218)
	at kotlinx.coroutines.CancellableContinuationImpl.resumeImpl(CancellableContinuationImpl.kt:227)
	at kotlinx.coroutines.CancellableContinuationImpl.resumeUndispatched(CancellableContinuationImpl.kt:299)
	at kotlinx.coroutines.EventLoopImplBase$DelayedResumeTask.run(EventLoop.kt:298)
	at kotlinx.coroutines.EventLoopImplBase.processNextEvent(EventLoop.kt:116)
	at kotlinx.coroutines.BlockingCoroutine.joinBlocking(Builders.kt:76)
	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking(Builders.kt:53)
	at kotlinx.coroutines.BuildersKt.runBlocking(Unknown Source)
	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking$default(Builders.kt:35)
	at kotlinx.coroutines.BuildersKt.runBlocking$default(Unknown Source)
	at org.droidmate.uiautomator2daemon.UiAutomator2DaemonDriver.executeCommand(UiAutomator2DaemonDriver.kt:48)
	at org.droidmate.uiautomator2daemon.UiAutomator2DaemonServer.onServerRequest(UiAutomator2DaemonServer.kt:42)
	at org.droidmate.uiautomator2daemon.UiAutomator2DaemonServer.onServerRequest(UiAutomator2DaemonServer.kt:33)
	at org.droidmate.uiautomator2daemon.Uiautomator2DaemonTcpServerBase$ServerRunnable.run(Uiautomator2DaemonTcpServerBase.java:145)
	at java.lang.Thread.run(Thread.java:764)
```

It means that DM-2 could not connect to the accessibility service on the device.

Please restart the device and try the exploration again.


## No Android Devices Found exception

If you see a `NoAndroidDevicesFoundException` please check your device connection with the command `adb devices`. 
A device should be identified by ADB to be used in DM. 

## Missing Qt/lib

If you are experiencing issues installing the emulator, such as:

```
[4601566656]:ERROR:android/android-emu/android/qt/qt_setup.cpp:28:Qt library not found at ../emulator/lib64/qt/lib
Could not launch '/usr/local/Caskroom/android-sdk/4333796/tools/../emulator/qemu/darwin-x86_64/qemu-system-i386': No such file or directory
```

The Android emulator require special host characteristics, such as qemu (e.g. when running on Docker) and a graphics card (to use hardware accelerated rendering). 
Have a look at [this](https://stackoverflow.com/questions/42554337/cannot-launch-avd-in-emulatorqt-library-not-found) stack overflow thread.


# More

More customization instructions can be found on the [DroidMate-2](https://github.com/uds-se/droidmate) project.
