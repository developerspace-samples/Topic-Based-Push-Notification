package com.dwarsh.pushnotificationsample

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.*
import java.lang.System.currentTimeMillis
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        var token : String? = null

        val key : String = "------------------------------------------------------------------"

        fun subscribeTopic(context: Context, topic: String) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnSuccessListener {
                Toast.makeText(context, "Subscribed $topic", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to Subscribe $topic", Toast.LENGTH_LONG).show()
            }
        }

        fun unsubscribeTopic(context: Context, topic: String) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnSuccessListener {
                Toast.makeText(context, "Unsubscribed $topic", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to Unsubscribe $topic", Toast.LENGTH_LONG).show()
            }
        }

        fun sendMessage(title: String, content: String,topic: String) {
            GlobalScope.launch {
                val endpoint = "https://fcm.googleapis.com/fcm/send"
                try {
                    val url = URL(endpoint)
                    val httpsURLConnection: HttpsURLConnection = url.openConnection() as HttpsURLConnection
                    httpsURLConnection.readTimeout = 10000
                    httpsURLConnection.connectTimeout = 15000
                    httpsURLConnection.requestMethod = "POST"
                    httpsURLConnection.doInput = true
                    httpsURLConnection.doOutput = true

                    // Adding the necessary headers
                    httpsURLConnection.setRequestProperty("authorization", "key=$key")
                    httpsURLConnection.setRequestProperty("Content-Type", "application/json")

                    // Creating the JSON with post params
                    val body = JSONObject()

                    val data = JSONObject()
                    data.put("title", title)
                    data.put("content", content)
                    body.put("data",data)

                    body.put("to","/topics/$topic")

                    val outputStream: OutputStream = BufferedOutputStream(httpsURLConnection.outputStream)
                    val writer = BufferedWriter(OutputStreamWriter(outputStream, "utf-8"))
                    writer.write(body.toString())
                    writer.flush()
                    writer.close()
                    outputStream.close()
                    val responseCode: Int = httpsURLConnection.responseCode
                    val responseMessage: String = httpsURLConnection.responseMessage
                    Log.d("Response:", "$responseCode $responseMessage")
                    var result = String()
                    var inputStream: InputStream? = null
                    inputStream = if (responseCode in 400..499) {
                        httpsURLConnection.errorStream
                    } else {
                        httpsURLConnection.inputStream
                    }

                    if (responseCode == 200) {
                        Log.e("Success:", "notification sent $title \n $content")
                        // The details of the user can be obtained from the result variable in JSON format
                    } else {
                        Log.e("Error", "Error Response")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Log.e("onMessageReceived: ", p0.data.toString())

        val title = p0.data.get("title")
        val content = p0.data.get("content")
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val intent = Intent(this,MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(applicationContext,0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            checkNotificationChannel("1")
        }

//        val person = Person.Builder().setName("test").build()
        val notification = NotificationCompat.Builder(applicationContext,"1")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
//                .setStyle(NotificationCompat.MessagingStyle(person)
//                        .setGroupConversation(false)
//                        .addMessage(title,
//                                currentTimeMillis(), person)
//                )
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(defaultSound)

        val notificationManager : NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1,notification.build())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkNotificationChannel(CHANNEL_ID:String) {
        val notificationChannel = NotificationChannel(CHANNEL_ID,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.description = "CHANNEL_DESCRIPTION"
        notificationChannel.enableLights(true)
        notificationChannel.enableVibration(true)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }
    override fun onNewToken(p0: String) {
        token = p0
        super.onNewToken(p0)
    }
}