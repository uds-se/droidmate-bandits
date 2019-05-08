package saarland.cispa.droidmate.thesis

import kotlinx.coroutines.runBlocking
import org.droidmate.api.ExplorationAPI
import org.droidmate.command.ExploreCommandBuilder
import org.droidmate.exploration.SelectorFunction
import org.droidmate.exploration.StrategySelector
import java.io.File
import java.nio.file.Path

object RegressionMain {

    private const val state = "state.txt"

    @JvmStatic
    fun main(args: Array<String>) {

        runBlocking {
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
    }

    suspend fun runDroidMate(deviceSerial: String, output: String, apks: String, modelPath: Path?): Path? {

        val argsString = "--Exploration-deviceSerialNumber=$deviceSerial --Output-outputDir=$output --Exploration-apksDir=$apks --Selectors-actionLimit=500 --Selectors-resetEvery=100"
        val runArgs = argsString.split(" ").toTypedArray()

        val cfg = ExplorationAPI.config(runArgs)

        // val modelPath = cfg.apksDirPath.resolve("hybrid_model.csv")

        val hybridStrategy = FitnessHybridStrategy(cfg, modelPath)
        val customStrategySelector: SelectorFunction = { _, pool, _ ->
                    pool.getFirstInstanceOf(FitnessHybridStrategy::class.java)
                }

        val builder = ExploreCommandBuilder.fromConfig(cfg)
            .withStrategy(hybridStrategy)
            .insertBefore(StrategySelector.randomWidget, "hybrid", customStrategySelector)

        val explorations = ExplorationAPI.explore(cfg, builder)
        val apk = explorations.entries.last().key
        return cfg.droidmateOutputDirPath.resolve(apk.packageName).resolve("model").resolve("hybrid_model.csv")
    }
}