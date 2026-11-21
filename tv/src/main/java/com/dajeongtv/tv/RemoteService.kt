package com.dajeongtv.tv
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class RemoteService : AccessibilityService() {
 @Volatile private var running=false
 private var server:ServerSocket?=null
 override fun onServiceConnected() {
  super.onServiceConnected(); running=true
  thread(name="dajeongtv-server") {
   try {
    ServerSocket(8765).use { listener ->
     server=listener
     while(running) { val client=listener.accept(); thread { handle(client) } }
    }
   } catch(_:Exception) {}
  }
 }
 private fun handle(socket:Socket) {
  socket.use { s ->
   try {
    s.soTimeout=2500
    val line=s.getInputStream().bufferedReader().readLine() ?: return
    val parts=line.split(" ",limit=2)
    val pin=getSharedPreferences("pair",0).getString("code","")
    val cmd=if(parts.size==2 && parts[0]==pin) parts[1] else ""
    val success=when(cmd) {
     "HOME"->performGlobalAction(GLOBAL_ACTION_HOME)
     "BACK"->performGlobalAction(GLOBAL_ACTION_BACK)
     "RECENTS"->performGlobalAction(GLOBAL_ACTION_RECENTS)
     "YOUTUBE"->launch("com.google.android.youtube.tv")
     "NETFLIX"->launch("com.netflix.ninja")
     "BROWSER"->listOf("com.android.chrome","com.tvweb.browser","com.google.android.tv.browser").any { launch(it) }
     else->false
    }
    s.getOutputStream().write((if(success)"OK\n" else "UNSUPPORTED\n").toByteArray())
   } catch(_:Exception) {}
  }
 }
 private fun launch(pkg:String):Boolean {
  return try {
   val intent=packageManager.getLaunchIntentForPackage(pkg) ?: return false
   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(intent); true
  } catch(_:Exception) { false }
 }
 override fun onAccessibilityEvent(event:AccessibilityEvent?) {}
 override fun onInterrupt() {}
 override fun onDestroy() { running=false; try { server?.close() } catch(_:Exception) {}; super.onDestroy() }
}
