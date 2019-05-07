package saarland.cispa.droidmate.thesis

import kotlinx.coroutines.runBlocking
import org.droidmate.configuration.ConfigurationWrapper
import org.apache.commons.math3.distribution.BetaDistribution
import org.droidmate.deviceInterface.exploration.ExplorationAction
import org.droidmate.exploration.ExplorationContext
import org.droidmate.exploration.modelFeatures.EventProbabilityMF
import org.droidmate.exploration.strategy.FitnessProportionateSelection
import java.nio.file.Path

class ThompsonSamplingHybridStrategy(
    randomSeed: Long,
    private val modelPath: Path?,
    modelName: String = "HasModel.model",
    arffName: String = "baseModelFile.arff",
    private val psi: Double
) : FitnessProportionateSelection(randomSeed, modelName, arffName) {

    constructor(
        cfg: ConfigurationWrapper,
        modelPath: Path?,
        modelName: String = "HasModel.model",
        arffName: String = "baseModelFile.arff",
        psi: Double
    ) : this(cfg.randomSeed, modelPath, modelName, arffName, psi)

    override val eventWatcher: EventProbabilityMF
        get() = (eContext.findWatcher { it is HybridEventProbabilityMF } as EventProbabilityMF)

    private val hybridWatcher: HybridEventDistributionMF
        get() = (eContext.findWatcher { it is HybridEventDistributionMF } as HybridEventDistributionMF)

    /**
     * Selects a widget following "Thompson Sampling"
     */
    override suspend fun chooseRandomWidget(): ExplorationAction {
        val candidates = this.internalGetWidgets()
        assert(candidates.isNotEmpty())

        /* Sample from the bandit's priors, and select the largest sample */
        val distributions = runBlocking { getActionDistributions() }
        val samples = distributions
                .map {
                    it.key to it.value.sample()
                }
                .toMap()

        return samples.randomItemWithMaxValue()
    }

    /**
     * Returns probability distributions for all available actions for all available widgets
     */
    private suspend fun getActionDistributions(): Map<ExplorationAction, BetaDistribution> {
        return hybridWatcher.getActionDistributions(currentState)
    }

    private fun <K, V : Comparable<V>> Map<K, V>.randomItemWithMaxValue(): K {
        val maxValue = this.maxBy { it.value }!!.value
        val itemsWithMaxValue = this.filter { entry -> entry.value == maxValue }.map { it.key }

        val selectedIdx = random.nextInt(itemsWithMaxValue.size)
        return itemsWithMaxValue[selectedIdx]
    }

    override fun initialize(memory: ExplorationContext) {
        super.initialize(memory)

        eContext.addWatcher(HybridEventDistributionMF(modelName, arffName, true, modelPath, psi))
    }
}