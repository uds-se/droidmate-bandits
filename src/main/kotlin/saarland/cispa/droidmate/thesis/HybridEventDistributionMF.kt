package saarland.cispa.droidmate.thesis


import kotlinx.coroutines.experimental.runBlocking
import org.apache.commons.math3.distribution.BetaDistribution
import org.droidmate.deviceInterface.guimodel.ExplorationAction
import org.droidmate.exploration.actions.availableActions
import org.droidmate.exploration.statemodel.StateData
import org.droidmate.exploration.statemodel.Widget
import java.nio.file.Path

class HybridEventDistributionMF(modelName: String,
                                arffName: String,
                                useClassMembershipProbability: Boolean,
                                modelPath: Path? = null,
                                psi: Double = 20.0) : HybridEventProbabilityMF(modelName, arffName, useClassMembershipProbability, modelPath, psi) {


    fun getActionDistributions(state: StateData): Map<ExplorationAction, BetaDistribution> {
        try {
            runBlocking { mutex.lock() }
            val actionDistributions = mutableMapOf<ExplorationAction, BetaDistribution>()
            state.actionableWidgets.forEach { widget ->
                val distributions = getDistributionsForWidgetUnsync(widget, state, widget.availableActions())
                actionDistributions.putAll(distributions)
            }
            return actionDistributions
        } finally {
            mutex.unlock()
        }
    }

    fun getDistributionsForWidgetUnsync(widget: Widget, state: StateData, actionList: List<ExplorationAction>): Map<ExplorationAction, BetaDistribution> {
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

    fun getDistributionsForWidget(widget: Widget, state: StateData, actionList: List<ExplorationAction>): Map<ExplorationAction, BetaDistribution> {
        try {
            runBlocking { mutex.lock() }
            return getDistributionsForWidgetUnsync(widget, state, actionList)
        } finally {
            mutex.unlock()
        }
    }
}