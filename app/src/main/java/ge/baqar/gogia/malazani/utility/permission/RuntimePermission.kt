package ge.baqar.gogia.malazani.utility.permission

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class RuntimePermissioner {

    class Builder {

        fun requestCode(code: Int): RuntimePermissions {
            requestCode = code
            return runtimePermissions
        }
    }

    class RuntimePermissions {
        fun permission(permission: String): RuntimePermissions {
            allPermissions.put(counter, permission)
            counter++
            return runtimePermissions
        }

        fun callBack(
            onGrantPermissions: OnGrantPermissions,
            onDenyPermissions: OnDenyPermissions,
            onFailure: OnPermissionsFailure
        ): RuntimePermissioner {
            grantPermissions = onGrantPermissions
            denyPermissions = onDenyPermissions
            failure = onFailure
            return runtimePermission
        }
    }

    fun onPermissionsResult(code: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == code) {
            grantResults.forEach {
                val permission = permissions[it]
                val grantType = grantResults[it]
                if (grantType == PackageManager.PERMISSION_GRANTED) {
                    grantedPermissions.add(permission)
                } else {
                    deniedPermissions.add(permission)
                }
            }
            grantPermissions?.get(grantedPermissions)
            denyPermissions?.get(deniedPermissions)
        }
    }

    fun request(activity: Activity) {
        try {
            val tempArr = arrayOf<String>()
            allPermissions.forEach {
                if (ContextCompat.checkSelfPermission(
                        activity,
                        it.value
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    tempArr[it.key] = it.value
                }
            }
            if (tempArr.isNotEmpty())
                ActivityCompat.requestPermissions(activity, tempArr, requestCode)
            else
                ActivityCompat.requestPermissions(activity, allPermissions.map { it.value }.toTypedArray(), requestCode)
        } catch (e: Exception) {
            failure?.fail(e)
        }

    }

    companion object {
        private var requestCode = 0
        private val runtimePermissions: RuntimePermissions by lazy { RuntimePermissions() }
        private val runtimePermission: RuntimePermissioner by lazy { RuntimePermissioner() }
        private val grantedPermissions = ArrayList<String>()
        private val deniedPermissions = ArrayList<String>()
        private var counter = 0
        val builder: Builder by lazy { Builder() }
        private var denyPermissions: OnDenyPermissions? = null
        private var grantPermissions: OnGrantPermissions? = null
        private var failure: OnPermissionsFailure? = null

        @SuppressLint("UseSparseArrays")
        private val allPermissions = HashMap<Int, String>()

        fun builder(): Builder? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return builder
            } else {
                return null
            }
        }
    }
}