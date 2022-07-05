package model

/**
 * Settings describing how (and if) a build should be included in a composite build
 */
data class BuildInclusionSettings(
    val path: String,
    var substitute: String?,
    var using: String?,
    var include: Boolean) {

    val inclusionStatement: String
        get() {
            val default = "includeBuild '${path}'"
            substitute ?: return default
            using ?: return default
            if (substitute == "" || using == "") { return default }
            return "includeBuild('$path') {dependencySubstitution {substitute module('$substitute') using project(':$using')}}"
        }
}