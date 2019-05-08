package org.droidmate.bandits

import org.apache.commons.math3.distribution.BetaDistribution
import org.droidmate.deviceInterface.exploration.ExplorationAction
import org.droidmate.exploration.actions.availableActions
import org.droidmate.explorationModel.interaction.State
import org.droidmate.explorationModel.interaction.Widget
import java.nio.file.Path

class HybridEventDistributionMF(
    modelName: String,
    arffName: String,
    useClassMembershipProbability: Boolean,
    psi: Double,
    useCrowdModel: Boolean,
    modelPath: Path?
) : HybridEventProbabilityMF(modelName, arffName, useClassMembershipProbability, psi, useCrowdModel, modelPath) {

    suspend fun getActionDistributions(state: State): Map<ExplorationAction, BetaDistribution> {
        try {
            mutex.lock()
            val actionDistributions = mutableMapOf<ExplorationAction, BetaDistribution>()
            state.visibleTargets.forEach { widget ->
                val distributions = getDistributionsForWidgetUnsync(widget, state, widget.availableActions(0, false))
                actionDistributions.putAll(distributions)
            }
            return actionDistributions
        } finally {
            mutex.unlock()
        }
    }

    private fun getDistributionsForWidgetUnsync(widget: Widget, state: State, actionList: List<ExplorationAction>): Map<ExplorationAction, BetaDistribution> {
        val classId = widget.toClassIdentifier(state)
        return actionList
                .map {
                    val actionId = it.toActionId()
                    val w = wins[classId]!![actionId]!!
                    val t = trials[classId]!![actionId]!!
                    it to BetaDistribution(1 + w, 1 + t - w)
                }
                .toMap()
    }

    suspend fun getDistributionsForWidget(widget: Widget, state: State, actionList: List<ExplorationAction>): Map<ExplorationAction, BetaDistribution> {
        try {
            mutex.lock()
            return getDistributionsForWidgetUnsync(widget, state, actionList)
        } finally {
            mutex.unlock()
        }
    }
}