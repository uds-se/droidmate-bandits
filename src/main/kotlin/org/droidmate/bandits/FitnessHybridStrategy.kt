package org.droidmate.bandits

import org.droidmate.configuration.ConfigurationWrapper
import org.droidmate.deviceInterface.exploration.ExplorationAction
import org.droidmate.exploration.ExplorationContext
import org.droidmate.exploration.actions.availableActions
import org.droidmate.exploration.modelFeatures.EventProbabilityMF
import org.droidmate.exploration.strategy.FitnessProportionateSelection
import org.droidmate.explorationModel.interaction.Widget
import java.nio.file.Path

open class FitnessHybridStrategy constructor(
    randomSeed: Long,
    private val modelPath: Path?,
    modelName: String = "HasModel.model",
    arffName: String = "baseModelFile.arff"
) : FitnessProportionateSelection(randomSeed, modelName, arffName) {

    constructor(
        cfg: ConfigurationWrapper,
        modelPath: Path?,
        modelName: String = "HasModel.model",
        arffName: String = "baseModelFile.arff"
    ) : this(cfg.randomSeed, modelPath, modelName, arffName)

    override val eventWatcher: EventProbabilityMF
            get() = (eContext.findWatcher { it is HybridEventProbabilityMF } as EventProbabilityMF)

    override fun chooseActionForWidget(chosenWidget: Widget): ExplorationAction {
        var widget = chosenWidget

        while (!chosenWidget.canInteractWith) {
            widget = currentState.widgets.first { it.id == chosenWidget.parentId }
        }

        logger.debug("Chosen widget: $widget: ${widget.canInteractWith}\t${widget.clickable}\t${widget.checked}\t${widget.longClickable}\t${widget.scrollable}")

        val actionList = widget.availableActions(0, false)

        assert(actionList.isNotEmpty()) { "No actions can be performed on the widget $widget" }

        val actionWithProbabilities = (eventWatcher as HybridEventProbabilityMF).getProbabilities(chosenWidget, currentState, actionList)
        logger.debug("Available actions: [${actionWithProbabilities.map { "${it.key} -> ${it.value}" }.joinToString(", ")}]")
        val selectedIdx = stochasticSelect(actionWithProbabilities.values, 10)

        return actionList[selectedIdx]
    }

    override fun initialize(memory: ExplorationContext) {
        super.initialize(memory)

        eContext.addWatcher(
            HybridEventProbabilityMF(
                modelName,
                arffName,
                true,
                1.0,
                useCrowdModel = true,
                modelPath = null)
        )
    }
}