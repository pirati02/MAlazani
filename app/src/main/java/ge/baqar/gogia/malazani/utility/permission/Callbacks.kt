package ge.baqar.gogia.malazani.utility.permission

interface OnGrantPermissions {
    operator fun get(grantedPermissions: List<String>)
}

interface OnPermissionsFailure {
    fun fail(e: Exception)
}

interface OnDenyPermissions {
    operator fun get(deniedPermissions: List<String>)
}