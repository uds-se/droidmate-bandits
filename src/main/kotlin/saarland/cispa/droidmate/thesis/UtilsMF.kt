package saarland.cispa.droidmate.thesis

import org.apache.commons.lang3.StringUtils
import org.droidmate.deviceInterface.guimodel.ExplorationAction
import org.droidmate.deviceInterface.guimodel.Swipe
import org.droidmate.exploration.statemodel.StateData
import org.droidmate.exploration.statemodel.Widget
import org.droidmate.exploration.strategy.AbstractStrategy

fun ExplorationAction.toActionId(): String{
	return when {
		this is Swipe ->
			when{
				(this.start.first > this.end.first) -> "Swipe-Left"
				(this.end.first > this.start.first) -> "Swipe-Right"
				(this.start.second > this.end.second) -> "Swipe-Up"
				else -> "Swipe-Down" //(this.end.second > this.start.second)
			}
		else -> this.name
	}
}

fun Widget.toClassIdentifier(state: StateData): String {
	val type = this.getRefinedType()

	if ((this.parentId != null) && (state.widgets.firstOrNull { it.id == parentId } == null))
		println(this)

	val parentType = if (this.parentId != null)
		state.widgets.first { it.id == this.parentId }.getRefinedType()
	else
		"none"

	val children = state.widgets.filter { p -> p.parentId == this.id }

	val child1Type = if (children.isNotEmpty())
		children.first().getRefinedType()
	else
		"none"

	val child2Type = if (children.size > 1)
		children[1].getRefinedType()
	else
		"none"

	return "<'$type', '$parentType', '$child1Type', '$child2Type'>"
}

fun Widget.getRefinedType(): String {
	return if (AbstractStrategy.VALID_WIDGETS.contains(this.className.toLowerCase()))
		className.toLowerCase()
	else {
		//Get last part
		val parts = className.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
		var refType = parts[parts.size - 1].toLowerCase()
		refType = findClosestView(refType)

		refType.toLowerCase()
	}
}

fun findClosestView(target: String): String {
	var distance = Integer.MAX_VALUE
	var closest = ""

	for (compareObject in AbstractStrategy.VALID_WIDGETS) {
		val currentDistance = StringUtils.getLevenshteinDistance(compareObject, target)
		if (currentDistance < distance) {
			distance = currentDistance
			closest = compareObject
		}
	}
	return closest
}