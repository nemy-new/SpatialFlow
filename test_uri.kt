import android.net.Uri
fun main() {
    val uri = Uri.parse("innertube://_abc_def")
    val vId = uri.host ?: uri.authority ?: uri.toString().substringAfter("innertube://")
    println("videoId: " + vId)
}
