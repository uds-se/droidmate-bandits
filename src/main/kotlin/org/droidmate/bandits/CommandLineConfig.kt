package saarland.cispa.droidmate.thesis

import com.natpryce.konfig.booleanType
import com.natpryce.konfig.getValue
import com.natpryce.konfig.uriType

object CommandLineConfig {
    val apk by uriType
    val onlyAppPackage by booleanType
    val printToLogcat by booleanType
    val outputDir by uriType
}