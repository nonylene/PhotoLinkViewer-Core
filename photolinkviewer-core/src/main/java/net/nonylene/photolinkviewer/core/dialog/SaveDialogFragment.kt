package net.nonylene.photolinkviewer.core.dialog

import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import android.widget.TextView
import net.nonylene.photolinkviewer.core.R
import java.util.*

class SaveDialogFragment : DialogFragment() {

    companion object {
        val INFO_KEY = "info"

        fun createArguments(infoList: ArrayList<Info>): Bundle {
            return Bundle().apply {
                putParcelableArrayList(INFO_KEY, infoList)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val infoList = arguments.getParcelableArrayList<Info>(INFO_KEY)[0]
        // set custom view
        val view = View.inflate(activity, R.layout.plv_core_save_path, null)
        (view.findViewById(R.id.path_TextView) as TextView).setText(infoList.dirName)
        (view.findViewById(R.id.path_EditText) as EditText).setText(infoList.fileName)
        return AlertDialog.Builder(activity).setView(view)
                .setTitle(getString(R.string.plv_core_save_dialog_title))
                .setPositiveButton(getString(R.string.plv_core_save_dialog_positive), { dialogInterface, i ->
                        // get filename
                        val fileName = (dialog.findViewById(R.id.path_EditText) as EditText).text.toString()
                        targetFragment.onActivityResult(targetRequestCode, Activity.RESULT_OK,
                                Intent().putExtra(INFO_KEY, arrayListOf(Info(fileName, infoList.dirName, infoList.downloadUrl, infoList.thumbnailUrl)))
                        )
                    }
                )
                .setNegativeButton(getString(android.R.string.cancel), null)
                .create()
    }

    class Info : Parcelable{

        val fileName: String
        val dirName: String
        val downloadUrl: String
        val thumbnailUrl: String

        constructor(fileName: String, dirName: String, downloadUrl: String, thumbnailUrl: String) {
            this.fileName = fileName
            this.dirName = dirName
            this.downloadUrl = downloadUrl
            this.thumbnailUrl = thumbnailUrl
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(fileName)
            dest.writeString(dirName)
            dest.writeString(downloadUrl)
            dest.writeString(thumbnailUrl)
        }

        constructor(source: Parcel) {
            this.fileName = source.readString()
            this.dirName = source.readString()
            this.downloadUrl = source.readString()
            this.thumbnailUrl = source.readString()
        }

        companion object {
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
