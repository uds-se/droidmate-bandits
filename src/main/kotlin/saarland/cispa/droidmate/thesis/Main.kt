@file:Suppress("unused")

package saarland.cispa.droidmate.thesis

import org.droidmate.ExplorationAPI
import org.droidmate.command.ExploreCommand
import org.droidmate.configuration.ConfigurationWrapper
import org.droidmate.exploration.StrategySelector
import org.droidmate.exploration.strategy.widget.FitnessProportionateSelection
import org.droidmate.report.Reporter
import org.droidmate.report.apk.EffectiveActions
import java.nio.file.Path

object Main {
    @JvmStatic fun main(args: Array<String>) {
        // --Selectors-actionLimit=500
		val modelPath : Path? = null/*Paths.get("./data")
				.resolve("runs")
				.resolve("fps-h")//.resolve("fps-h-new-pre-loaded")
				.resolve("dev${cfg[deviceIndex]}")
				.resolve("model")
				.resolve("com.wikihow.wikihowapp")//.resolve("com.dougkeen.bart")
				.resolve("hybrid_model.csv")
				.toAbsolutePath()*/

        runFPSH(args, modelPath)
        runFPS(args)
        runEpsilonHybrid(args, modelPath)
        runEpsilonPure(args)
        runTSHybrid(args, modelPath)
        runTSPure(args)
		runRandom(args)
    }

    private fun getDefaultReporters(cfg: ConfigurationWrapper): List<Reporter>{
        val defaultReporter = ExploreCommand.defaultReportWatcher(cfg).dropLast(1)
        val actions = EffectiveActions()
        val reporter : MutableList<Reporter> = mutableListOf()
        defaultReporter.forEach { reporter.add(it) }
        reporter.add(actions)

        return reporter
    }

    private fun runFPSH(originalArgs: Array<String>, modelPath: Path?) {
		val args = arrayOf(*originalArgs)
		val argIdx = args.indexOfFirst { it.contains("./data/runs/") }
		args[argIdx] = args[argIdx].replace("./data/runs/", "./data/runs/fps-h/")
		val cfg = ExplorationAPI.config(args)

        val hybridStrategy = FitnessHybridStrategy(cfg, modelPath)
        val customStrategySelector =
                StrategySelector(8, "hybrid", { context, pool, _ ->
                    // Force synchronization
                    val feature = context.findWatcher { it is HybridEventProbabilityMF }
                    feature?.await()

                    pool.getFirstInstanceOf(FitnessHybridStrategy::class.java)})

        val defaultStrategies = ExploreCommand.getDefaultStrategies(cfg)
        val back = defaultStrategies.component1()
        val reset = defaultStrategies.component2()
        val terminate = defaultStrategies.component3()
        val permission = defaultStrategies.component5()
        val strategies = listOf(back, reset, terminate, permission, hybridStrategy)

        val defaultSelectors = ExploreCommand.getDefaultSelectors(cfg).dropLast(1)
        val selectors : MutableList<StrategySelector> = mutableListOf()
        defaultSelectors.forEach { selectors.add(it) }
        selectors.add(customStrategySelector)

        val reporter = getDefaultReporters(cfg)

        ExplorationAPI.explore(cfg, strategies = strategies, selectors = selectors, reportCreators = reporter)
    }

    private fun runFPS(originalArgs: Array<String>) {
		val args = arrayOf(*originalArgs)
		val argIdx = args.indexOfFirst { it.contains("./data/runs/") }
		args[argIdx] = args[argIdx].replace("./data/runs/", "./data/runs/fps/")
		val cfg = ExplorationAPI.config(args)

        val defaultStrategies = ExploreCommand.getDefaultStrategies(cfg)
        val back = defaultStrategies.component1()
        val reset = defaultStrategies.component2()
        val terminate = defaultStrategies.component3()
        val permission = defaultStrategies.component5()
        val fps = FitnessProportionateSelection(cfg.randomSeed)
        val strategies = listOf(back, reset, terminate, permission, fps)

        val defaultSelectors = ExploreCommand.getDefaultSelectors(cfg).dropLast(1)
        val selectors : MutableList<StrategySelector> = mutableListOf()
        defaultSelectors.forEach { selectors.add(it) }

        selectors.add(StrategySelector(selectors.size, "randomBiased", StrategySelector.randomBiased))

		val reporter = getDefaultReporters(cfg)

		ExplorationAPI.explore(cfg, strategies = strategies, selectors = selectors, reportCreators = reporter)
	}

    private fun runEpsilonHybrid(originalArgs: Array<String>, modelPath: Path?) {
        runEpsilon(originalArgs, modelPath, "epsilon-h")
    }

    private fun runEpsilonPure(originalArgs: Array<String>) {
        runEpsilon(originalArgs, null, "epsilon", psi = 0.0)
    }

    private fun runEpsilon(originalArgs: Array<String>, modelPath: Path?, output: String, psi: Double = 20.0) {
        val args = arrayOf(*originalArgs)
        val argIdx = args.indexOfFirst { it.contains("./data/runs/") }
        args[argIdx] = args[argIdx].replace("./data/runs/", "./data/runs/$output/")
        val cfg = ExplorationAPI.config(args)

        val hybridStrategy = EpsilonGreedyHybridStrategy(cfg, modelPath, psi = psi)
        val customStrategySelector =
                StrategySelector(8, "hybrid", { context, pool, _ ->
                    // Force synchronization
                    val feature = context.findWatcher { it is HybridEventProbabilityMF }
                    feature?.await()

                    pool.getFirstInstanceOf(EpsilonGreedyHybridStrategy::class.java)})

        val defaultStrategies = ExploreCommand.getDefaultStrategies(cfg)
        val back = defaultStrategies.component1()
        val reset = defaultStrategies.component2()
        val terminate = defaultStrategies.component3()
        val permission = defaultStrategies.component5()
        val strategies = listOf(back, reset, terminate, permission, hybridStrategy)

        val defaultSelectors = ExploreCommand.getDefaultSelectors(cfg).dropLast(1)
        val selectors : MutableList<StrategySelector> = mutableListOf()
        defaultSelectors.forEach { selectors.add(it) }
        selectors.add(customStrategySelector)

        val reporter = getDefaultReporters(cfg)

        ExplorationAPI.explore(cfg, strategies = strategies, selectors = selectors, reportCreators = reporter)
    }

    private fun runTSHybrid(originalArgs: Array<String>, modelPath: Path?) {
        runTS(originalArgs, modelPath, "thompson-h")
    }

    private fun runTSPure(originalArgs: Array<String>) {
        runTS(originalArgs, null, "thompson", psi = 0.0)
    }

    private fun runTS(originalArgs: Array<String>, modelPath: Path?, output: String, psi: Double = 20.0) {
        val args = arrayOf(*originalArgs)
        val argIdx = args.indexOfFirst { it.contains("./data/runs/") }
        args[argIdx] = args[argIdx].replace("./data/runs/", "./data/runs/$output/")
        val cfg = ExplorationAPI.config(args)

        val hybridStrategy = ThompsonSamplingHybridStrategy(cfg, modelPath, psi = psi)
        val customStrategySelector =
                StrategySelector(8, "hybrid", { context, pool, _ ->
                    // Force synchronization
                    val feature = context.findWatcher { it is HybridEventDistributionMF }
                    feature?.await()

                    pool.getFirstInstanceOf(ThompsonSamplingHybridStrategy::class.java)})

        val defaultStrategies = ExploreCommand.getDefaultStrategies(cfg)
        val back = defaultStrategies.component1()
        val reset = defaultStrategies.component2()
        val terminate = defaultStrategies.component3()
        val permission = defaultStrategies.component5()
        val strategies = listOf(back, reset, terminate, permission, hybridStrategy)

        val defaultSelectors = ExploreCommand.getDefaultSelectors(cfg).dropLast(1)
        val selectors : MutableList<StrategySelector> = mutableListOf()
        defaultSelectors.forEach { selectors.add(it) }
        selectors.add(customStrategySelector)

        val reporter = getDefaultReporters(cfg)

        ExplorationAPI.explore(cfg, strategies = strategies, selectors = selectors, reportCreators = reporter)
    }

    private fun runRandom(originalArgs: Array<String>) {
		val args = arrayOf(*originalArgs)
		val argIdx = args.indexOfFirst { it.contains("./data/runs/") }
		args[argIdx] = args[argIdx].replace("./data/runs/", "./data/runs/random/")
		val cfg = ExplorationAPI.config(args)

        val strategies = ExploreCommand.getDefaultStrategies(cfg)
        val selectors = ExploreCommand.getDefaultSelectors(cfg)

		val reporter = getDefaultReporters(cfg)

		ExplorationAPI.explore(cfg, strategies = strategies, selectors = selectors, reportCreators = reporter)
    }
}
