# DM-2: Reinforcement Learning Strategies![GNU GPL v3](https://www.gnu.org/graphics/gplv3-88x31.png)
   [![Build Status](https://travis-ci.org/uds-se/droidmate-bandits.svg?branch=master)](https://travis-ci.org/uds-se/droidmate-bandits)
   [![](https://jitpack.io/v/uds-se/droidmate-bandits.svg)](https://jitpack.io/#uds-se/droidmate-bandits)

Strategies for [DroidMate-2](https://github.com/uds-se/droidmate): A Platform for Android Test Generation.

[ISSTA'19 paper: Learning User Interface Element Interactions](https://publications.cispa.saarland/2883/)

# Building

__DM-2: Bandits__ is built on top of DroidMate-2. On most environments with a JDK and Android SDK installed it should automatically obtain all dependencies from JitPack and Maven Central.

The tool is also integrated into the Travis-CI, its [configuration file](https://github.com/uds-se/droidmate-bandits/blob/master/.travis.yml) contains all commands used to configure and execute it.  

In case of trouble compiling the tool, have a look at the [DM-2 wiki](https://github.com/uds-se/droidmate/wiki/Building) 

# Usage instructions

## Running explorations

### Random

1. Create an `apks` folder
2. Add the APKs to the apks folder
3. Execute (On Linux/OSX): `./gradlew run` from the project's root directory.

__Note:__ To run with custom arguments use `--args='<ARGS>'`

### Strategies from the paper:

Use one of the following arguments:

```
  -e Boolean, --epsilon=Boolean          Enable Epsilon-Greedy Strategy
  -eh Boolean, --epsilonHybrid=Boolean   Enable Epsilon-Greedy Strategy with Crowd-Model
  -t Boolean, --thompson=Boolean         Enable Thompson Sampling Strategy
  -th Boolean, --thompsonHybrid=Boolean  Enable Thompson Sampling Strategy
  -fps Boolean, --fps=Boolean            Enable Fitness Proportionate Selection Strategy (Baseline)
  -fpsh Boolean, --fpsHybrid=Boolean     Enable Fitness Proportionate Selection Hybrid Strategy
```

Example to run Epsilon-Greedy `./gradlew run --args="-e True <OTHER ARGS>"`  


### List all available arguments:

To list all available parameters, run with `--help`

```
./gradlew run --args="--help"
```

### Example configuration with arguments:

To run all apps located in the `./apks` folder a configuration would be similar to: 

```
./gradlew run --args="-e true --Selectors-randomSeed=0 --Selectors-actionLimit=100"
```

## Importing on another project

To import Dm-2 bandits on another project built on top of the DroidMate-2 platform follow the [Jitpack instructions](https://jitpack.io/#uds-se/droidmate-bandits/master-SNAPSHOT)

# Obtaining coverage

To obtain coverage the apps should first be instrumented.

Use

```
./gradlew run --args="--ExecutionMode-explore=false --ExecutionMode-coverage=true"
```

to instrument an app (it will take the first app from the `./apks` folder).

To monitor coverage while exploring an app add the argument `--StatementCoverage-enableCoverage=true` to your command. For example:

```
./gradlew run --args="-e true --Selectors-randomSeed=0 --Selectors-actionLimit=100 --StatementCoverage-enableCoverage=true"
``` 

__Note:__ The instrumentation keeps the original (non-instrumented) app in the same location, to run only the version with coverage move the original apk to another folder.  

# Troubleshooting

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

# More

More customization instructions can be found on the [DroidMate-2](https://github.com/uds-se/droidmate) project.
