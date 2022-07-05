package model

/**
 * Holds information about the current state of a git repo
 */
class GitRepoStatus(
    var path: String? = null,
    var checkedOutTag: String? = null,
    var isClean: Boolean
) {}