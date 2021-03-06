package net.nonylene.photolinkviewer.core

import android.graphics.*
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.widget.*
import butterknife.bindView
import net.nonylene.photolinkviewer.core.tool.getImageViewMaxSize
import net.nonylene.photolinkviewer.core.tool.putImageViewMaxSize

class PLVMaxSizePreferenceActivity : AppCompatActivity() {

    private val imageView: ImageView by bindView(R.id.imageView)
    private val textView: TextView by bindView(R.id.textView)
    private val seekBar: SeekBar by bindView(R.id.seekBar)
    private val setButton: Button by bindView(R.id.setButton)

    private val blackPaint = Paint().apply {
        color = Color.BLACK
    }
    private val bluePaint = Paint().apply {
        color = Color.parseColor("#4cb6ed")
    }
    private val greenPaint = Paint().apply {
        color = Color.parseColor("#46d249")
    }
    private val redPaint = Paint().apply {
        color = Color.parseColor("#f4554f")
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 200f
        textAlign = Paint.Align.CENTER
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.plv_core_activity_max_preference)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView.text = "${(progress + 1) * 1024}px"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                setButton.isEnabled = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                updateImageView(seekBar!!.progress)
                setButton.isEnabled = true
            }
        })

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        setButton.setOnClickListener {
            val size = seekBar.progress + 1
            sharedPref.edit().putImageViewMaxSize(size).apply()
            Toast.makeText(this.applicationContext, getString(R.string.plv_core_max_preference_toast, size * 1024), Toast.LENGTH_LONG).show()
        }

        val index = sharedPref.getImageViewMaxSize() - 1;
        updateImageView(index)
        seekBar.progress = index
    }

    private fun updateImageView(index: Int) {
        imageView.setImageDrawable(null)
        val size = (index + 1) * 1024
        val s2 = size / 2f
        val scaledBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(scaledBitmap)
        // background
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), blackPaint)
        // circle
        canvas.drawCircle(s2, s2, size / 3f, bluePaint)
        // rhombus
        canvas.drawPath(Path().apply {
            val s5 = size / 5f
            moveTo(s2, s5)
            lineTo(s5, s2)
            lineTo(s2, s5 * 4)
            lineTo(s5 * 4, s2)
            close()
        }, greenPaint)
        canvas.drawCircle(s2, s2, size / 5f, redPaint)
        canvas.drawText("${size}px", s2, s2, textPaint)
        imageView.setImageBitmap(scaledBitmap)
    }

}
