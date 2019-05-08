# DM-2: ModelBased and FitnessProportionateSelection strategies![GNU GPL v3](https://www.gnu.org/graphics/gplv3-88x31.png)
   [![Build Status](https://travis-ci.org/uds-se/droidmate-bandits.svg?branch=master)](https://travis-ci.org/uds-se/droidmate-bandits)
   [![](https://jitpack.io/v/uds-se/droidmate-bandits.svg)](https://jitpack.io/#uds-se/droidmate-bandits)

Strategies for [DroidMate-2](https://github.com/uds-se/droidmate): A Platform for Android Test Generation.

[ISSTA'19 paper: Learning User Interface Element Interactions](https://publications.cispa.saarland/2883/)

# Building

__DM-2: Bandits__ is built on top of DroidMate-2. On most environments with a JDK and Android SDK installed it should automatically obtain all dependencies from JitPack and Maven Central.

The tool is also integrated into the Travis-CI, the [CI configuration file](https://github.com/uds-se/droidmate-bandits/blob/master/.travis.yml) contains all commands used to run it.  

In case of trouble compiling the tool use have a look at [DM-2 wiki](https://github.com/uds-se/droidmate/wiki/Building) 

# Executing 

## Random

1. Create an `apks` folder
2. Add the APKs to the apks folder
3. Execute (On Linux/OSX): `./gradlew run` from the project's root directory.

__Note:__ To run with custom arguments use `--args='<ARGS>'`

## Strategies from the paper:

Use one of the following arguments:

```
  -e Boolean, --epsilon=Boolean          Enable Epsilon-Greedy Strategy
  -eh Boolean, --epsilonHybrid=Boolean   Enable Epsilon-Greedy Strategy with Crowd-Model
  -t Boolean, --thompson=Boolean         Enable Thompson Sampling Strategy
  -th Boolean, --thompsonHybrid=Boolean  Enable Thompson Sampling Strategy
  -fps Boolean, --fpsHybrid=Boolean      Enable Fitness Proportionate Selection Hybrid Strategy
```

Example to run Epsilon-Greedy `./gradlew run --args="-e True"`  


## List all available arguments:

To list all available parameters, run with `--help`

```
./gradlew --args='--help'
```

# Example configuration with arguments:

To run all apps located in the `./apks` folder a configuration would be similar to: 

```
./gradlew run --args='--Selectors-randomSeed=0 --Selectors-actionLimit=100 --DeviceCommunication-deviceOperationDelay=0 --UiAutomatorServer-waitForIdleTimeout=1000 --UiAutomatorServer-waitForInteractableTimeout=1000 --Deploy-replaceResources=true --Deploy-installAux=true --Deploy-installMonitor=false'
```

# More

More customization instructions can be found on the [DroidMate-2](https://github.com/uds-se/droidmate) project.
