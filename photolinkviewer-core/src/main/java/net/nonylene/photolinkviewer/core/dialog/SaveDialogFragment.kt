package net.nonylene.photolinkviewer.core.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import net.nonylene.photolinkviewer.core.R
import net.nonylene.photolinkviewer.core.view.SaveDialogItemView
import java.util.*

//todo: move to recyclerview
class SaveDialogFragment : DialogFragment() {

    companion object {
        val DIR_KEY = "dir"
        val INFO_KEY = "info"
        val QUALITY_KEY = "quality"

        fun createArguments(dirName: String, quality: String?, infoList: ArrayList<Info>): Bundle {
            return Bundle().apply {
                putString(DIR_KEY, dirName)
                putString(QUALITY_KEY, quality)
                putParcelableArrayList(INFO_KEY, infoList)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val infoList = arguments.getParcelableArrayList<Info>(INFO_KEY)
        // set custom view
        val view = View.inflate(activity, R.layout.plv_core_save_path, null)
        val dirname = arguments.getString(DIR_KEY)
        (view.findViewById(R.id.path_text_view) as TextView).text = dirname
        val linearLayout = view.findViewById(R.id.path_linear_layout) as LinearLayout

        for (info in infoList) {
            linearLayout.addView(
                    (LayoutInflater.from(context).inflate(R.layout.plv_core_save_path_item, linearLayout, false) as SaveDialogItemView).apply {
                        setThumbnailUrl(info.thumbnailUrl)
                        setFileName(info.fileName)
                        checkedChangeListener = {
                            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                                    (0..linearLayout.childCount - 1).fold(false) { result, i ->
                                        result || (linearLayout.getChildAt(i) as SaveDialogItemView).isChecked
                                    }
                        }
                    }
            )
        }

        return AlertDialog.Builder(activity).setView(view)
                .setTitle(arguments.getString(QUALITY_KEY)?.let { getString(R.string.plv_core_save_dialog_title).format(it) }
                        ?: getString(R.string.plv_core_save_dialog_title_null))
                .setPositiveButton(getString(R.string.plv_core_save_dialog_positive), { _, _ ->
                    // get filename
                    val newInfoList = (0..linearLayout.childCount - 1).map {
                        linearLayout.getChildAt(it) as SaveDialogItemView to infoList[it]
                    }.filter {
                        it.first.isChecked
                    }.map {
                        Info(it.first.getFileName(), it.second.downloadUrl, it.second.thumbnailUrl)
                    }.toCollection(ArrayList())

                    targetFragment.onActivityResult(targetRequestCode, Activity.RESULT_OK,
                            Intent().putParcelableArrayListExtra(INFO_KEY, newInfoList)
                                    .putExtra(DIR_KEY, dirname)
                    )
                }
                )
                .setNegativeButton(getString(android.R.string.cancel), null)
                .create()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    class Info(val fileName: String, val downloadUrl: String, val thumbnailUrl: String) : Parcelable {

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(fileName)
            dest.writeString(downloadUrl)
            dest.writeString(thumbnailUrl)
        }

        constructor(source: Parcel) : this(
                source.readString(),
                source.readString(),
                source.readString()
        )

        companion object CREATOR: Parcelable.Creator<Info> {
            override fun createFromParcel(source: Parcel): Info {
                return Info(source)
            }

            override fun newArray(size: Int): Array<Info?> {
                return arrayOfNulls(size)
            }
        }
    }
}
