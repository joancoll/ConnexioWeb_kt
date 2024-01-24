package cat.dam.andy.connexioweb_kt

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class ConnectionDetect(private val context: Context) {

    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun haveConnection(): Boolean {
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        return network != null && networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
    }
}
