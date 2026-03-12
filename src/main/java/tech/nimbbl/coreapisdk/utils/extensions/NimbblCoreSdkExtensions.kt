package tech.nimbbl.coreapisdk.utils.extensions

import org.json.JSONArray
import org.json.JSONObject
import tech.nimbbl.coreapisdk.core.constants.Constants.is_debug_enabled
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigInteger
import java.net.InetAddress
import java.net.NetworkInterface
import java.security.MessageDigest
import java.util.Collections


/*
Created by Sandeep Yadav on 23/02/22.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/



fun printLog(tag: String, message: String) {
    if (is_debug_enabled) {
        println("[$tag] $message");
    }
}


fun getAPIRequestBody(jsonObject: JSONObject): String =
    jsonObject.toString()

fun getXNimbblKey(subMerchantId: String, input: String): String {
    val md = MessageDigest.getInstance("MD5")
    val md5 = BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    return "$subMerchantId-$md5"
}



/**
 * Get IP address from first non-localhost interface
 * @param useIPv4   true=return ipv4, false=return ipv6
 * @return  address or empty string
 */
fun getIPAddress(useIPv4: Boolean): String {
    try {
        val interfaces: List<NetworkInterface> =
            Collections.list(NetworkInterface.getNetworkInterfaces())
        for (intf in interfaces) {
            val addrs: List<InetAddress> = Collections.list(intf.getInetAddresses())
            for (addr in addrs) {
                if (!addr.isLoopbackAddress) {
                    val sAddr: String? = addr.hostAddress
                    if (sAddr != null) {
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (useIPv4) {
                            if (isIPv4) return sAddr else return ""
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) sAddr.uppercase() else sAddr.take(delim).uppercase()
                            } else return ""
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        if (is_debug_enabled) {
            android.util.Log.w("NimbblCoreSdk", "getIPAddress failed: ${e.message}")
        }
    }
    return ""
}


fun writeResponseBodyToDisk(destnUrl: String, body: ByteArray?, fileName: String): String {
    if (body == null) return ""
    return try {
        if (createDirIfNotExists(destnUrl)) {
            val outputFile = File(File(destnUrl), fileName)
            try {
                outputFile.createNewFile()
                FileOutputStream(outputFile).use { it.write(body); it.flush() }
                outputFile.absolutePath
            } catch (e: IOException) {
                e.printStackTrace()
                ""
            }
        } else ""
    } catch (e: IOException) {
        ""
    }
}


fun createDirIfNotExists(path: String): Boolean {
    val file = File(path)
    return if (!file.exists()) {
        file.mkdirs()
    } else {
        file.isDirectory
    }
}

fun JSONObject.toMap(): Map<String, *> = keys().asSequence().associateWith {
    when (val value = this[it]) {
        is JSONArray -> {
            val map = (0 until value.length()).associate { Pair(it.toString(), value[it]) }
            JSONObject(map).toMap().values.toList()
        }

        is JSONObject -> value.toMap()
        JSONObject.NULL -> null
        else -> value
    }
}
