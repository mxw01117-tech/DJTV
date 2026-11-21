package com.dajeongtv.phone
import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.widget.*
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : Activity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val prefs=getSharedPreferences("tv",0)
  val root=LinearLayout(this).apply { orientation=1; setPadding(24,28,24,20) }
  val ip=EditText(this).apply { hint="TV IP (예: 192.168.0.10)"; setSingleLine(); setText(prefs.getString("ip","")) }
  val code=EditText(this).apply { hint="TV 연결 코드 6자리"; setSingleLine(); setText(prefs.getString("code","")) }
  val status=TextView(this).apply { text="TV용 다정tv 연결 앱에서 IP와 코드를 확인하세요." }
  root.addView(TextView(this).apply { text="다정tv 📺"; textSize=27f })
  root.addView(ip); root.addView(code); root.addView(status)
  fun send(cmd:String) {
   val host=ip.text.toString().trim(); val pin=code.text.toString().trim()
   prefs.edit().putString("ip",host).putString("code",pin).apply()
   thread {
    val result=try {
     Socket().use { s ->
      s.connect(java.net.InetSocketAddress(host,8765),2000); s.soTimeout=2500
      s.getOutputStream().write("$pin $cmd\n".toByteArray())
      s.getInputStream().bufferedReader().readLine()?:"응답 없음"
     }
    } catch(e:Exception) { "연결 실패: ${e.message}" }
    runOnUiThread { status.text=result }
   }
  }
  fun button(label:String,cmd:String) { root.addView(Button(this).apply { text=label; setOnClickListener { send(cmd) } }) }
  button("홈","HOME"); button("뒤로","BACK"); button("최근 앱","RECENTS")
  button("유튜브","YOUTUBE"); button("넷플릭스","NETFLIX"); button("인터넷 브라우저","BROWSER")
  root.addView(Button(this).apply { text="미러링 설정"; setOnClickListener {
   try { startActivity(Intent(Settings.ACTION_CAST_SETTINGS)) }
   catch(e:Exception) { Toast.makeText(this@MainActivity,"이 기기에서 미러링 설정을 열 수 없습니다",Toast.LENGTH_LONG).show() }
  } })
  root.addView(TextView(this).apply { text="시험 버전: TV 보조 앱 설치와 접근성 서비스 활성화가 필요합니다. 미러링은 TV 호환 여부에 따라 다릅니다. 볼륨·채널·전원·방향키는 미구현입니다." })
  setContentView(ScrollView(this).apply { addView(root) })
 }
}
