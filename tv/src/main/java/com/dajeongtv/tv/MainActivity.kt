package com.dajeongtv.tv
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import java.net.NetworkInterface

class MainActivity : Activity() {
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  val ip=NetworkInterface.getNetworkInterfaces().toList().flatMap { it.inetAddresses.toList() }.firstOrNull { !it.isLoopbackAddress && it.hostAddress?.contains(':')==false }?.hostAddress?:"TV IP 확인 필요"
  val prefs=getSharedPreferences("pair",0)
  val code=prefs.getString("code",null) ?: (100000..999999).random().toString().also { prefs.edit().putString("code",it).apply() }
  val root=LinearLayout(this).apply { orientation=1; setPadding(35,50,35,20) }
  root.addView(TextView(this).apply { text="다정tv TV 연결\n\nTV IP: $ip\n포트: 8765\n연결 코드: $code\n\n접근성 설정에서 '다정tv TV 연결'을 활성화하세요. 같은 Wi-Fi에서만 사용하세요."; textSize=22f })
  root.addView(Button(this).apply { text="접근성 설정 열기"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } })
  setContentView(root)
 }
}
