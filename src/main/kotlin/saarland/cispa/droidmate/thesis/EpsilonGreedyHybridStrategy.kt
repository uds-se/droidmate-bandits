package saarland.cispa.droidmate.thesis

import org.droidmate.deviceInterface.guimodel.ExplorationAction
import org.droidmate.exploration.ExplorationContext
import org.droidmate.exploration.actions.availableActions
import org.droidmate.exploration.statemodel.Widget
import org.droidmate.exploration.statemodel.features.EventProbabilityMF
import org.droidmate.exploration.strategy.widget.FitnessProportionateSelection
import java.nio.file.Path

open class EpsilonGreedyHybridStrategy @JvmOverloads constructor(randomSeed: Long,
                                                                 private val modelPath: Path?,
                                                                 modelName: String = "HasModel.model",
                                                                 arffName: String = "baseModelFile.arff",
                                                                 private val epsilon: Double = 0.3,
																 private val psi: Double = 20.0) : FitnessProportionateSelection(randomSeed, modelName, arffName) {

    override val eventWatcher: EventProbabilityMF
        get() = (eContext.findWatcher { it is HybridEventProbabilityMF } as EventProbabilityMF)

    /**
     * Selects a widget following "Epsilon Greedy"
     */
    override fun chooseRandomWidget(): ExplorationAction {
        val candidates = this.internalGetWidgets()
        assert(candidates.isNotEmpty())

        val probabilities = getCandidatesProbabilities()

        val widget =
                if ((this.random.nextInt(100) / 10.0) <= epsilon) {
                    /* Exploration */
                    candidates[random.nextInt(candidates.size)]
                } else {
                    /* Exploitation */
					probabilities.randomItemWithMaxValue()
                }

        return chooseActionForWidget(widget)
    }

	override fun chooseActionForWidget(chosenWidget: Widget): ExplorationAction {
		var widget = chosenWidget

		while (!chosenWidget.canBeActedUpon) {
			widget = currentState.widgets.first { it.id == chosenWidget.parentId }
		}

		logger.debug("Chosen widget: $widget: ${widget.canBeActedUpon}\t${widget.clickable}\t${widget.checked}\t${widget.longClickable}\t${widget.scrollable}")

		val actionList = widget.availableActions()

		assert(actionList.isNotEmpty()) { "No actions can be performed on the widget $widget" }

		val actionWithProbabilities = (eventWatcher as HybridEventProbabilityMF).getProbabilities(chosenWidget, currentState, actionList)
		logger.debug("Available actions: [${actionWithProbabilities.map { "${it.key} -> ${it.value}" }.joinToString(", ")}]")

		return if ((this.random.nextInt(100) / 10.0) <= epsilon) {
			val selectedIdx = this.random.nextInt(actionList.size)
			actionList[selectedIdx]
		} else
			actionWithProbabilities.randomItemWithMaxValue()
	}

    override fun initialize(memory: ExplorationContext) {
        super.initialize(memory)

        eContext.addWatcher(HybridEventProbabilityMF(modelName, arffName, true, modelPath, psi))
    }

	private fun <K,V : Comparable<V>> Map<K,V>.randomItemWithMaxValue() : K {
		val maxValue = this.maxBy { it.value }!!.value
		val itemsWithMaxValue = this.filter { entry -> entry.value == maxValue }.map { it.key }

		val selectedIdx = random.nextInt(itemsWithMaxValue.size)
		return itemsWithMaxValue[selectedIdx]
	}
}