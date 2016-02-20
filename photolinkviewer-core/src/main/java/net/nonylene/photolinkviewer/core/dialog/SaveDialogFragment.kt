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
import android.widget.*
import net.nonylene.photolinkviewer.core.R
import net.nonylene.photolinkviewer.core.view.SaveDialogItemView
import java.util.*

//todo: move to recyclerview
class SaveDialogFragment : DialogFragment() {

    companion object {
        val DIR_KEY = "dir"
        val INFO_KEY = "info"

        fun createArguments(dirName: String, infoList: ArrayList<Info>): Bundle {
            return Bundle().apply {
                putString(DIR_KEY, dirName)
                putParcelableArrayList(INFO_KEY, infoList)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val infoList = arguments.getParcelableArrayList<Info>(INFO_KEY)
        // set custom view
        val view = View.inflate(activity, R.layout.plv_core_save_path, null)
        (view.findViewById(R.id.path_text_view) as TextView).text = arguments.getString(DIR_KEY)
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
                .setTitle(getString(R.string.plv_core_save_dialog_title))
                .setPositiveButton(getString(R.string.plv_core_save_dialog_positive), { dialogInterface, i ->
                    // get filename
                    val newInfoList = (0..linearLayout.childCount - 1).map {
                        linearLayout.getChildAt(it) as SaveDialogItemView to infoList[it]
                    }.filter {
                        it.first.isChecked
                    }.map {
                        Info(it.first.getFileName(), it.second.downloadUrl, it.second.thumbnailUrl)
                    }.toCollection(ArrayList())

                    val textView = dialog.findViewById(R.id.path_text_view) as TextView
                    targetFragment.onActivityResult(targetRequestCode, Activity.RESULT_OK,
                            Intent().putParcelableArrayListExtra(INFO_KEY, newInfoList)
                                    .putExtra(DIR_KEY, textView.text)
                    )
                }
                )
                .setNegativeButton(getString(android.R.string.cancel), null)
                .create()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    class Info : Parcelable {

        val fileName: String
        val downloadUrl: String
        val thumbnailUrl: String

        constructor(fileName: String, downloadUrl: String, thumbnailUrl: String) {
            this.fileName = fileName
            this.downloadUrl = downloadUrl
            this.thumbnailUrl = thumbnailUrl
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(fileName)
            dest.writeString(downloadUrl)
            dest.writeString(thumbnailUrl)
        }

        constructor(source: Parcel) {
            this.fileName = source.readString()
            this.downloadUrl = source.readString()
            this.thumbnailUrl = source.readString()
        }

        companion object {
            @JvmStatic
            @Suppress("unused")
            val CREATOR: Parcelable.Creator<Info> = object : Parcelable.Creator<Info> {
                override fun createFromParcel(source: Parcel): Info {
                    return Info(source)
                }

                override fun newArray(size: Int): Array<Info?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}
