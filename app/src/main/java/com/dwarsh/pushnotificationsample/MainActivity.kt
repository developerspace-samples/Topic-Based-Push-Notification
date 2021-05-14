package com.dwarsh.pushnotificationsample

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName"))
                startActivityForResult(intent,1)
            }
        }
        topic_subscribe.setEndIconOnClickListener {
            if (!topic_toSubscribe.text.toString().isNullOrEmpty()) {
                MyFirebaseMessagingService.subscribeTopic(this@MainActivity,topic_toSubscribe.text.toString())
            } else {
                topic_toSubscribe.error = "Fill this field"
            }
        }

        topic_unsubscribe.setEndIconOnClickListener {
            if (!topic_toUnsubscribe.text.toString().isNullOrEmpty()) {
                MyFirebaseMessagingService.unsubscribeTopic(this@MainActivity,topic_toUnsubscribe.text.toString())
            } else {
                topic_toUnsubscribe.error = "Fill this field"
            }
        }

        send_btn.setOnClickListener {
            when {
                title_edt.text.toString().isEmpty() -> title_edt.error = "Fill this field"
                content_edt.text.toString().isEmpty() -> content_edt.error = "Fill this field"
                else -> {
                    MyFirebaseMessagingService.sendMessage(title_edt.text.toString(),
                        content_edt.text.toString(),
                        topic_toSend.text.toString())
                }
            }
        }
    }
}