package saarland.cispa.droidmate.thesis


import java.nio.file.Path


/**
 * Same as EpsilonGreedyHybrid but psi = 0
 */
class EpsilonGreedyStrategy @JvmOverloads constructor(randomSeed: Long,
                                                      modelPath: Path?,
                                                      modelName: String = "HasModel.model",
                                                      arffName: String = "baseModelFile.arff",
                                                      epsilon: Double = 0.3) : EpsilonGreedyHybridStrategy(randomSeed, modelPath, modelName, arffName, epsilon, 0.0)