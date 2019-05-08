@file:Suppress("unused")

package org.droidmate.bandits

import com.natpryce.konfig.CommandLineOption
import com.natpryce.konfig.Key
import kotlinx.coroutines.runBlocking
import org.droidmate.api.ExplorationAPI
import org.droidmate.command.ExploreCommandBuilder
import org.droidmate.configuration.ConfigProperties
import org.droidmate.configuration.ConfigurationWrapper
import org.droidmate.exploration.StrategySelector
import org.droidmate.exploration.modelFeatures.EventProbabilityMF
import org.droidmate.exploration.strategy.FitnessProportionateSelection
import org.slf4j.LoggerFactory

/**
 * Recommended run config:
 * JVM Options: -Dkotlinx.coroutines.debug -Dlogback.configurationFile=default-logback.xml
 * Command line args: -e True --Selectors-actionLimit=1000 --Selectors-randomSeed=0
 */
object Main {
    @JvmStatic
    private val log by lazy { LoggerFactory.getLogger(this::class.java) }

    @JvmStatic
    private fun extraCmdOptions() = arrayOf(
        CommandLineOption(
            CommandLineConfig.epsilon,
            description = "Enable Epsilon-Greedy Strategy",
            short = "e",
            metavar = "Boolean"
        ),
        CommandLineOption(
            CommandLineConfig.epsilonHybrid,
            description = "Enable Epsilon-Greedy Strategy with Crowd-Model",
            short = "eh",
            metavar = "Boolean"
        ),
        CommandLineOption(
            CommandLineConfig.thompson,
            description = "Enable Thompson Sampling Strategy",
            short = "t",
            metavar = "Boolean"
        ),
        CommandLineOption(
            CommandLineConfig.thompsonHybrid,
            description = "Enable Thompson Sampling Strategy",
            short = "th",
            metavar = "Boolean"
        ),
        CommandLineOption(
            CommandLineConfig.fps,
            description = "Enable Fitness Proportionate Selection Hybrid Strategy",
            short = "fps",
            metavar = "Boolean"
        ),
        CommandLineOption(
            CommandLineConfig.fpsHybrid,
            description = "Enable Fitness Proportionate Selection Hybrid Strategy",
            short = "fpsh",
            metavar = "Boolean"
        )
    )

    private fun ConfigurationWrapper.asInt(key: Key<*>): Int {
        return if (this.getOrNull(key) == true) {
            1
        } else {
            0
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            val cfg = ExplorationAPI.config(args, *extraCmdOptions())

            if (cfg[ConfigProperties.ExecutionMode.coverage]) {
                ExplorationAPI.instrument(cfg)
                System.exit(0)
            }

            val epsilon = cfg.asInt(CommandLineConfig.epsilon)
            val epsilonHybrid = cfg.asInt(CommandLineConfig.epsilonHybrid)
            val thompson = cfg.asInt(CommandLineConfig.thompson)
            val thompsonHybrid = cfg.asInt(CommandLineConfig.thompsonHybrid)
            val fps = cfg.asInt(CommandLineConfig.fps)
            val fpsH = cfg.asInt(CommandLineConfig.fpsHybrid)

            val numberActive = epsilon + epsilonHybrid + thompson + thompsonHybrid + fps + fpsH

            if (numberActive > 1) {
                log.error(
                    "Only one strategy among Epsilon-Greedy, Epsilon-Greedy Hybrid, Thompson, Thompson " +
                            "Hybrid and Fitness Proportionate Selection hybrid can be active at a time."
                )

                System.exit(1)
            }

            val builder = ExploreCommandBuilder.fromConfig(cfg)

            when {
                epsilon > 0 -> addEpsilon(builder, cfg)
                epsilonHybrid > 0 -> addEpsilonHybrid(builder, cfg)
                thompson > 0 -> addThompson(builder, cfg)
                thompsonHybrid > 0 -> addThompsonHybrid(builder, cfg)
                fps > 0 -> addFPS(builder, cfg)
                fpsH > 0 -> addFPSHybrid(builder, cfg)
            }

            ExplorationAPI.explore(cfg, builder)
        }
    }

    private fun addEpsilonSelector(builder: ExploreCommandBuilder, description: String = "epsilon") {
        builder.insertBefore(StrategySelector.randomWidget,
            description,
            { context, pool, _ ->
                // Force synchronization
                val feature = context.findWatcher { it is HybridEventProbabilityMF }
                feature?.join()

                pool.getFirstInstanceOf(EpsilonGreedyHybridStrategy::class.java)
            })
    }

    private fun addEpsilon(builder: ExploreCommandBuilder, cfg: ConfigurationWrapper) {
        val strategy = EpsilonGreedyHybridStrategy(cfg, false, psi = 1.0)
        builder.withStrategy(strategy)
        addEpsilonSelector(builder)
    }

    private fun addEpsilonHybrid(builder: ExploreCommandBuilder, cfg: ConfigurationWrapper) {
        val strategy = EpsilonGreedyHybridStrategy(cfg, true, psi = 20.0)
        builder.withStrategy(strategy)
        addEpsilonSelector(builder, "epsilon-h")
    }

    private fun addThompsonSelector(builder: ExploreCommandBuilder, description: String = "thompson") {
        builder.insertBefore(StrategySelector.randomWidget,
            description,
            { context, pool, _ ->
                // Force synchronization
                val feature = context.findWatcher { it is HybridEventDistributionMF }
                feature?.join()

                pool.getFirstInstanceOf(ThompsonSamplingHybridStrategy::class.java)
            })
    }

    private fun addThompson(builder: ExploreCommandBuilder, cfg: ConfigurationWrapper) {
        val strategy = ThompsonSamplingHybridStrategy(cfg, false, psi = 1.0)
        builder.withStrategy(strategy)
        addThompsonSelector(builder)
    }

    private fun addThompsonHybrid(builder: ExploreCommandBuilder, cfg: ConfigurationWrapper) {
        val strategy = ThompsonSamplingHybridStrategy(cfg, true, psi = 20.0)
        builder.withStrategy(strategy)
        addThompsonSelector(builder, "thompson-h")
    }

    private fun addFPS(builder: ExploreCommandBuilder, cfg: ConfigurationWrapper) {
        val strategy = FitnessProportionateSelection(cfg.randomSeed)
        builder.withStrategy(strategy)

        builder.insertBefore(StrategySelector.randomWidget,
            "fps",
            { context, pool, _ ->
                // Force synchronization
                val feature = context.findWatcher { it is EventProbabilityMF }
                feature?.join()

                pool.getFirstInstanceOf(FitnessProportionateSelection::class.java)
            })
    }

    private fun addFPSHybrid(builder: ExploreCommandBuilder, cfg: ConfigurationWrapper) {
        val strategy = FitnessHybridStrategy(cfg, modelPath = null)
        builder.withStrategy(strategy)

        builder.insertBefore(StrategySelector.randomWidget,
            "fps-h",
            { context, pool, _ ->
                // Force synchronization
                val feature = context.findWatcher { it is EventProbabilityMF }
                feature?.join()

                pool.getFirstInstanceOf(FitnessHybridStrategy::class.java)
            })
    }
}
