package net.nonylene.photolinkviewer.core.test

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import net.nonylene.photolinkviewer.core.PLVPreferenceActivity
import net.nonylene.photolinkviewer.core.PhotoLinkViewer
import kotlin.reflect.declaredMemberProperties

class TestIntentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PhotoLinkViewer.with(
                PhotoLinkViewer.TwitterKeys("", ""), BuildConfig.FLICKR_KEY, BuildConfig.TUMBLR_KEY,
                PLVPreferenceActivity::class.java)

        val scrollView = ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }

        val linearLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
        }

        for (member in TestUrls::class.declaredMemberProperties) {
            linearLayout.addView(
                    Button(this).apply {
                        text = member.name
                        setOnClickListener {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(member.get(TestUrls) as String)))
                        }
                    }
            )
        }

        scrollView.addView(linearLayout)

        setContentView(scrollView)
    }
}

