package com.osman.cevir

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(pad, pad, pad, pad)

        val info = TextView(this)
        info.textSize = 15f
        info.text = "WhatsApp'ta mesaj yazarken yazı alanının üstünde \"Werger → KU\" ve \"Werger → TR\" düğmeleri çıkar. " +
            "Basınca yazdığınız metin çevrilip yerine konur.\n\n" +
            "1) Google Cloud Translation API anahtarınızı girip kaydedin.\n" +
            "2) Erişilebilirlik ayarlarında \"Werger\" servisini açın.\n\n" +
            "Android 13+ cihazlarda servis gri ise: Ayarlar > Uygulamalar > Werger > ⋮ > Kısıtlı ayarlara izin ver."
        col.addView(info)

        val prefs = getSharedPreferences("p", MODE_PRIVATE)
        val key = EditText(this)
        key.hint = "API anahtarı"
        key.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        key.setText(prefs.getString("key", ""))
        col.addView(key)

        val save = Button(this)
        save.text = "Kaydet"
        save.setOnClickListener {
            prefs.edit().putString("key", key.text.toString().trim()).apply()
            Toast.makeText(this, "Kaydedildi", Toast.LENGTH_SHORT).show()
        }
        col.addView(save)

        val acc = Button(this)
        acc.text = "Erişilebilirlik ayarlarını aç"
        acc.setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        col.addView(acc)

        val sv = ScrollView(this)
        sv.addView(col)
        setContentView(sv)
    }
}
