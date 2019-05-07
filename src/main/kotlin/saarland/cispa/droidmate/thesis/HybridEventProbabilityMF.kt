package saarland.cispa.droidmate.thesis

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.droidmate.deviceInterface.exploration.ExplorationAction
import org.droidmate.exploration.ExplorationContext
import org.droidmate.exploration.actions.availableActions
import org.droidmate.exploration.modelFeatures.EventProbabilityMF
import org.droidmate.explorationModel.interaction.State
import org.droidmate.explorationModel.interaction.Widget
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

open class HybridEventProbabilityMF(
    modelName: String,
    arffName: String,
    useClassMembershipProbability: Boolean,
    private val psi: Double = 20.0
) : EventProbabilityMF(modelName, arffName, useClassMembershipProbability) {

    constructor(
        modelName: String,
        arffName: String,
        useClassMembershipProbability: Boolean,
        modelPath: Path?,
        psi: Double = 20.0
    ) : this(modelName, arffName, useClassMembershipProbability, psi) {

        if (modelPath != null && Files.exists(modelPath)) {
            log.info("Reading trials from file $modelPath")
            val lines = Files.readAllLines(modelPath)
            lines.asSequence()
                .drop(1)
                .filter { it.startsWith("<") }
                .forEach { line -> initializeFromModel(line) }
        }
    }

    private val actionSet = setOf("Click", "LongClick", "Swipe-Up", "Swipe-Down", "Swipe-Left", "Swipe-Right")

    /* Number of trials and wins */
    protected val trials: MutableMap<String, MutableMap<String, Double>> = mutableMapOf()
    protected val wins: MutableMap<String, MutableMap<String, Double>> = mutableMapOf()

    /* Original value from model
    *  (just so that we can add it into the output csv) */
    private val originalTrials: MutableMap<String, MutableMap<String, Double>> = mutableMapOf()
    private val originalWins: MutableMap<String, MutableMap<String, Double>> = mutableMapOf()

    override suspend fun onNewInteracted(
        traceId: UUID,
        targetWidgets: List<Widget>,
        prevState: State,
        newState: State
    ) {
        // do nothing
    }

    override suspend fun onNewInteracted(
        traceId: UUID,
        actionIdx: Int,
        action: ExplorationAction,
        targetWidgets: List<Widget>,
        prevState: State,
        newState: State
    ) {
        try {
            mutex.lock()

            val targetWidget = targetWidgets.firstOrNull()

            // Add reward
            if (targetWidget != null) {
                val classId = targetWidget.toClassIdentifier(prevState)
                val actionId = action.toActionId()

                assert(trials.containsKey(classId)) { "Hybrid event probability trials feature was not correctly initialized. ClassId $classId not found" }
                assert(trials[classId]!!.containsKey(actionId)) { "Hybrid event probability trials feature was not correctly initialized. ActionId $actionId not found" }

                val oldProbability = wins[classId]!![actionId]!! / trials[classId]!![actionId]!!
                trials[classId]!![actionId] = trials[classId]!![actionId]!! + 1

                if (prevState != newState) {
                    // Was effective
                    wins[classId]!![actionId] = wins[classId]!![actionId]!! + 1
                }

                val newProbability = wins[classId]!![actionId]!! / trials[classId]!![actionId]!!
                log.debug("Updating probabilities for class $classId and action $actionId from $oldProbability to $newProbability (${wins[classId]!![actionId]!!}/${trials[classId]!![actionId]!!})")
            }

            addNewWidgets(newState)
        } finally {
            mutex.unlock()
        }
    }

    override suspend fun dump(context: ExplorationContext) {
        join()
        val out = StringBuffer()
        out.appendln("instance;action;old-wins;old-trials;new-wins;new-trials")

        wins.keys.forEach { instance ->
            actionSet.forEach { actionId ->
                out.append("$instance;")
                    .append("$actionId;")
                    .append("${originalWins[instance]!![actionId] ?: 0.0};")
                    .append("${originalTrials[instance]!![actionId] ?: 0.0};")
                    .append("${wins[instance]!![actionId] ?: 0.0};")
                    .append("${trials[instance]!![actionId] ?: 0.0}")
                    .append("\n")
            }
        }

        val file = context.model.config.baseDir.resolve("hybrid_model.csv")
        withContext(Dispatchers.IO) { Files.write(file, out.lines()) }
    }

    override fun getProbabilities(state: State): Map<Widget, Double> {
        try {
            runBlocking { mutex.lock() }
            return state.actionableWidgets
                .map { widget ->
                    // Return the maximum event probability for a widget, given the types of event that can be triggered
                    val probabilities = getProbabilitiesUnsync(widget, state, widget.availableActions(0, false))

                    widget to (probabilities.maxBy { it.value }?.value ?: 0.0)
                }
                .toMap()
        } finally {
            mutex.unlock()
        }
    }

    fun getProbabilitiesUnsync(
        widget: Widget,
        state: State,
        actionList: List<ExplorationAction>
    ): Map<ExplorationAction, Double> {
        // Return the maximum event probability for a widget, given the types of event that can be triggered
        val classId = widget.toClassIdentifier(state)
        return actionList
            .map {
                val actionId = it.toActionId()
                it to wins[classId]!![actionId]!! / trials[classId]!![actionId]!!
            }
            .toMap()
    }

    fun getProbabilities(
        widget: Widget,
        state: State,
        actionList: List<ExplorationAction>
    ): Map<ExplorationAction, Double> {
        try {
            runBlocking { mutex.lock() }

            return getProbabilitiesUnsync(widget, state, actionList)
        } finally {
            mutex.unlock()
        }
    }

    private fun initializeFromModel(line: String) {
        val elements = line.split(";")

        val classId = elements[0]
        val oldWins = elements[2].toDouble()
        val oldTrials = elements[3].toDouble()

        if (!trials.containsKey(classId)) {
            trials[classId] = mutableMapOf()
            wins[classId] = mutableMapOf()
            originalTrials[classId] = mutableMapOf()
            originalWins[classId] = mutableMapOf()

            actionSet.forEach { actionId ->
                trials[classId]!![actionId] = 1.0
                wins[classId]!![actionId] = 1.0
                originalTrials[classId]!![actionId] = 0.0
                originalWins[classId]!![actionId] = 0.0
            }
        }

        val actionId = elements[1]
        trials[classId]!![actionId] = oldTrials
        wins[classId]!![actionId] = oldWins
        originalTrials[classId]!![actionId] = oldTrials
        originalWins[classId]!![actionId] = oldWins
    }

    private fun addNewWidgets(newState: State) {
        newState.actionableWidgets.forEach { widget ->
            val classId = widget.toClassIdentifier(newState)

            // Initialize trials and wins for new widget type instance
            if (!trials.containsKey(classId)) {
                trials[classId] = mutableMapOf()
                wins[classId] = mutableMapOf()
                originalTrials[classId] = mutableMapOf()
                originalWins[classId] = mutableMapOf()

                val predictionProbability = widget.getProbabilityFromModel(newState)

                actionSet.forEach { actionId ->
                    trials[classId]!![actionId] = psi
                    wins[classId]!![actionId] = psi * predictionProbability
                    originalWins[classId]!![actionId] = psi * predictionProbability
                    originalTrials[classId]!![actionId] = psi * 1.0
                }

                log.debug("Initialized new element $classId and actions [${actionSet.joinToString(",")}]")
            }
        }
    }

    private fun Widget.getProbabilityFromModel(state: State): Double {
        wekaInstances.delete()
        wekaInstances.add(this.toWekaInstance(state, wekaInstances))
        assert(wekaInstances.numInstances() == 1) { "Unable to convert widget $this into weka instances" }

        val instance = wekaInstances.instance(0)
        return try {
            // Probability of having event
            val predictionProbability = if (useClassMembershipProbability) {
                // Get probability distribution of the prediction ( [false, true] )
                classifier.distributionForInstance(instance)[1]
            } else {
                val classification = classifier.classifyInstance(instance)
                // Classified as true = 1.0
                classification
            }

            predictionProbability
        } catch (e: Exception) {
            log.error("Could not classify widget $this. Ignoring it", e)
            // do nothing

            0.0
        }
    }
}