package saarland.cispa.droidmate.thesis

import kotlinx.coroutines.runBlocking
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
    modelPath: Path? = null,
    psi: Double = 20.0
) : HybridEventProbabilityMF(modelName, arffName, useClassMembershipProbability, modelPath, psi) {

    suspend fun getActionDistributions(state: State): Map<ExplorationAction, BetaDistribution> {
        try {
            mutex.lock()
            val actionDistributions = mutableMapOf<ExplorationAction, BetaDistribution>()
            state.actionableWidgets.forEach { widget ->
                val distributions = getDistributionsForWidgetUnsync(widget, state, widget.availableActions(0, false))
                actionDistributions.putAll(distributions)
            }
            return actionDistributions
        } finally {
            mutex.unlock()
        }
    }

    fun getDistributionsForWidgetUnsync(widget: Widget, state: State, actionList: List<ExplorationAction>): Map<ExplorationAction, BetaDistribution> {
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

    fun getDistributionsForWidget(widget: Widget, state: State, actionList: List<ExplorationAction>): Map<ExplorationAction, BetaDistribution> {
        try {
            runBlocking { mutex.lock() }
            return getDistributionsForWidgetUnsync(widget, state, actionList)
        } finally {
            mutex.unlock()
        }
    }
}