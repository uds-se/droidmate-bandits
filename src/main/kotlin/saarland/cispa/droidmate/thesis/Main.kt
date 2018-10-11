@file:Suppress("unused")

package saarland.cispa.droidmate.thesis

import org.droidmate.ExplorationAPI
import org.droidmate.command.ExploreCommand
import org.droidmate.configuration.ConfigurationWrapper
import org.droidmate.exploration.StrategySelector
import org.droidmate.exploration.strategy.ISelectableExplorationStrategy
import org.droidmate.exploration.strategy.widget.FitnessProportionateSelection
import org.droidmate.report.Reporter
import org.droidmate.report.apk.EffectiveActions
import java.nio.file.Path

object Main {
    val modelPath : Path? = null//cfg.apksDirPath.resolve("hybrid_model.csv")

    @JvmStatic fun main(args: Array<String>) {
        // --Selectors-actionLimit=500
        val cfg = ExplorationAPI.config(args)

        //val data = prepareFPSH(cfg, modelPath)
        val data = prepareFPS(cfg)
		//val data = prepareRandom(cfg)
        val strategies = data.first
        val selectors = data.second

        val defaultReporter = ExploreCommand.defaultReportWatcher(cfg).dropLast(1)
        val actions = EffectiveActions()
        val reporter : MutableList<Reporter> = mutableListOf()
        defaultReporter.forEach { reporter.add(it) }
        reporter.add(actions)

        ExplorationAPI.explore(cfg, strategies = strategies, selectors = selectors, reportCreators = reporter)
    }

    private fun prepareFPSH(cfg: ConfigurationWrapper): Pair<List<ISelectableExplorationStrategy>, List<StrategySelector>>{
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

        return Pair(strategies, selectors)
    }

    private fun prepareFPS(cfg: ConfigurationWrapper): Pair<List<ISelectableExplorationStrategy>, List<StrategySelector>>{
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

        return Pair(strategies, selectors)
    }

    private fun prepareRandom(cfg: ConfigurationWrapper): Pair<List<ISelectableExplorationStrategy>, List<StrategySelector>>{
        val defaultStrategies = ExploreCommand.getDefaultStrategies(cfg)
        val defaultSelectors = ExploreCommand.getDefaultSelectors(cfg)

        return Pair(defaultStrategies, defaultSelectors)
    }
}
