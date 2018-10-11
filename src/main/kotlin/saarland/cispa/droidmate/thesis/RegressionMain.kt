package saarland.cispa.droidmate.thesis

import org.droidmate.ExplorationAPI
import org.droidmate.command.ExploreCommand
import org.droidmate.exploration.StrategySelector
import org.droidmate.report.Reporter
import org.droidmate.report.apk.EffectiveActions
import java.io.File
import java.nio.file.Path
import java.util.*

object RegressionMain {

    private const val state = "state.txt"

    @JvmStatic
    fun main(args: Array<String>) {

        if (args.isEmpty()) {
            System.out.println("Usage: <deviceSerial> [apkFolder]")
            System.exit(1)
        }

        val deviceSerial = args[0]
        val apkFolder = if (args.size >= 2) args[1] else "../apks"


        System.out.println("Starting EvaluationMain")
        System.out.println("Available memory: ${Runtime.getRuntime().maxMemory()}")
        System.out.println("Serial: $deviceSerial")
        System.out.println("Apks: $apkFolder")


        var modelPath: Path? = null

        File(apkFolder).list().sorted().forEach {

            System.out.println(it)

            // create output directory
            val directory = File("$it/droidMate")
            directory.mkdirs()

            // run Droidmate
            modelPath = runDroidMate(deviceSerial, "$it/droidMate", "$apkFolder/$it", modelPath)

        }
    }

    fun runDroidMate(deviceSerial: String, output: String, apks: String, modelPath: Path?): Path? {

        val argsString = "--Exploration-deviceSerialNumber=$deviceSerial --Output-outputDir=$output --Exploration-apksDir=$apks --Selectors-actionLimit=500 --Selectors-resetEvery=100"
        val runArgs = argsString.split(" ").toTypedArray()

        val cfg = ExplorationAPI.config(runArgs)

        // val modelPath = cfg.apksDirPath.resolve("hybrid_model.csv")

        val hybridStrategy = FitnessHybridStrategy(cfg, modelPath)
        val customStrategySelector =
                StrategySelector(8, "hybrid", { _, pool, _ ->
                    pool.getFirstInstanceOf(FitnessHybridStrategy::class.java)})

        val defaultStrategies = ExploreCommand.getDefaultStrategies(cfg)
        val back = defaultStrategies.component1()
        val reset = defaultStrategies.component2()
        val terminate = defaultStrategies.component3()
        val permission = defaultStrategies.component5()
        val strategies = listOf(back, reset, terminate, permission, hybridStrategy)

        val defaultSelectors = ExploreCommand.getDefaultSelectors(cfg).dropLast(1)
        val selectors = LinkedList<StrategySelector>()
        defaultSelectors.forEach { selectors.add(it) }
        selectors.add(customStrategySelector)

        val defaultReporter = ExploreCommand.defaultReportWatcher(cfg).dropLast(1)
        val actions = EffectiveActions()
        val reporter = LinkedList<Reporter>()
        defaultReporter.forEach { reporter.add(it) }
        reporter.add(actions)

        val explorations = ExplorationAPI.explore(cfg, strategies = strategies, selectors = selectors, reportCreators = reporter)
        return explorations.last().getModel().config.baseDir.resolve("hybrid_model.csv")
    }
}