package org.droidmate.bandits

import com.natpryce.konfig.booleanType
import com.natpryce.konfig.getValue

object CommandLineConfig {
    val epsilon by booleanType
    val epsilonHybrid by booleanType
    val thompson by booleanType
    val thompsonHybrid by booleanType
    val fpsHybrid by booleanType
}