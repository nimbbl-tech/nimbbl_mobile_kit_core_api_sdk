package tech.nimbbl.coreapisdk.utils

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import tech.nimbbl.coreapisdk.utils.Constants.is_debug_enabled
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
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
        println(message);
    }
}


fun getAPIRequestBody(jsonObject: JSONObject) =
    RequestBody.create(
        "application/json; charset=utf-8".toMediaTypeOrNull(),
        jsonObject.toString()
    )

fun getXNimbblKey(subMerchantId: String, input: String): String {
    val md = MessageDigest.getInstance("MD5")
    val md5 = BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    return "$subMerchantId-$md5"
}

fun md5(input: String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
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
                    val sAddr: String = addr.hostAddress
                    //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                    val isIPv4 = sAddr.indexOf(':') < 0
                    if (useIPv4) {
                        if (isIPv4) return sAddr
                    } else {
                        if (!isIPv4) {
                            val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                            return if (delim < 0) sAddr.uppercase() else sAddr.substring(
                                0,
                                delim
                            ).uppercase()
                        }
                    }
                }
            }
        }
    } catch (ignored: java.lang.Exception) {
    } // for now eat exceptions
    return ""
}


fun writeResponseBodyToDisk(destnUrl: String, body: ResponseBody?, fileName: String): String {
    return try {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val fileReader = ByteArray(4096)
            var fileSizeDownloaded: Long = 0
            if (body != null) {
                inputStream = body.byteStream()
            }
            if (createDirIfNotExists(destnUrl)) {
                val outputFile = File(File(destnUrl), fileName)
                try {
                    outputFile.createNewFile()
                    outputStream = FileOutputStream(outputFile)
                    while (true) {
                        val read: Int = inputStream!!.read(fileReader)
                        if (read == -1) {
                            break
                        }
                        outputStream.write(fileReader, 0, read)
                        fileSizeDownloaded += read.toLong()
                    }
                    outputStream.flush()
                    outputFile.absolutePath
                } catch (e: IOException) {
                    e.printStackTrace()
                    ""
                }

            } else {
                ""
            }
        } catch (e: IOException) {
            ""
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    } catch (e: IOException) {
        ""
    }
}


fun createDirIfNotExists(path: String): Boolean {
    var ret = true
    val file = File(path)
    if (!file.exists()) {
        if (!file.mkdirs()) {
            ret = false
        }
    } else {
        file.delete()
    }
    return ret
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



