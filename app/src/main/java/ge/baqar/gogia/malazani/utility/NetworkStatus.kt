package ge.baqar.gogia.malazani.utility

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi

class NetworkStatus(private val application: Application) {

    @RequiresApi(Build.VERSION_CODES.M)
    fun isOnline(): Boolean {
//        val context: Context = application.applicationContext!!
//        val connectivityManager =
//            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
//        connectivityManager?.let {
//            val capabilities =
//                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
//            if (capabilities != null) {
//                when {
//                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
//                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
//                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true
//                }
//            }
//        }
//        return false

        val context: Context = application.applicationContext!!
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            val nwInfo = connectivityManager.activeNetworkInfo ?: return false
            return nwInfo.isConnected
        }
    }
}