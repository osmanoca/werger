package com.osman.cevir

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast

class TranslatorService : AccessibilityService() {

    private lateinit var wm: WindowManager
    private var bar: LinearLayout? = null
    private var shown = false
    private val handler = Handler(Looper.getMainLooper())

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onServiceConnected() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        buildBar()
    }

    private fun buildBar() {
        val l = LinearLayout(this)
        l.orientation = LinearLayout.HORIZONTAL
        fun btn(label: String, source: String, target: String): Button {
            val b = Button(this)
            b.text = label
            b.isAllCaps = false
            b.textSize = 12f
            b.setTextColor(Color.WHITE)
            val bg = GradientDrawable()
            bg.setColor(0xFF128C7E.toInt())
            bg.cornerRadius = dp(18).toFloat()
            b.background = bg
            b.minHeight = 0
            b.minimumHeight = 0
            b.setPadding(dp(12), 0, dp(12), 0)
            b.setOnClickListener { translate(source, target) }
            return b
        }
        val p1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(36))
        p1.rightMargin = dp(6)
        val p2 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(36))
        l.addView(btn("Werger → KU", "tr", "ku"), p1)
        l.addView(btn("Werger → TR", "ku", "tr"), p2)
        bar = l
    }

    private fun lp(x: Int, y: Int): WindowManager.LayoutParams {
        val p = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        p.gravity = Gravity.TOP or Gravity.START
        p.x = x
        p.y = y
        return p
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val root = rootInActiveWindow
        val pkg = root?.packageName?.toString()
        if (root == null || (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b")) {
            hide(); return
        }
        val f = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        val hint = Build.VERSION.SDK_INT >= 26 && f?.isShowingHintText == true
        if (f != null && f.isEditable && !hint && !f.text.isNullOrBlank()) show(f) else hide()
    }

    private fun show(n: AccessibilityNodeInfo) {
        val b = bar ?: return
        val r = Rect()
        n.getBoundsInScreen(r)
        val x = maxOf(0, r.right - dp(240))
        val y = maxOf(0, r.top - dp(44))
        try {
            if (!shown) { wm.addView(b, lp(x, y)); shown = true }
            else wm.updateViewLayout(b, lp(x, y))
        } catch (e: Exception) { }
    }

    private fun hide() {
        if (!shown) return
        try { bar?.let { wm.removeView(it) } } catch (e: Exception) { }
        shown = false
    }

    private fun translate(source: String, target: String) {
        val node = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return
        val text = node.text?.toString() ?: return
        val key = getSharedPreferences("p", MODE_PRIVATE).getString("key", "") ?: ""
        if (key.isBlank()) {
            Toast.makeText(this, "Önce uygulamada API anahtarını girin", Toast.LENGTH_LONG).show()
            return
        }
        Thread {
            try {
                val out = Translator.translate(key, text, source, target)
                handler.post {
                    val n = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                    if (n != null) {
                        val args = Bundle()
                        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, out)
                        n.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                    }
                }
            } catch (e: Exception) {
                handler.post { Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }.start()
    }

    override fun onInterrupt() {}

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        hide()
        return super.onUnbind(intent)
    }
}
