package com.smarttrainner.feature.training.impl

internal fun instructionWithoutRepeatedStepTitle(label: String, instruction: String): String {
    val trimmedLabel = label.trim()
    if (trimmedLabel.isEmpty()) return instruction.trim()

    val trimmedInstruction = instruction.trim()
    val colonIndex = trimmedInstruction.indexOfAny(charArrayOf(':', '：'))
    if (colonIndex == -1) return trimmedInstruction

    val prefix = trimmedInstruction.substring(0, colonIndex).trim()
    return if (prefix.equals(trimmedLabel, ignoreCase = true)) {
        trimmedInstruction.substring(colonIndex + 1).trim()
    } else {
        trimmedInstruction
    }
}
