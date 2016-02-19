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
import android.view.ViewGroup
import android.widget.*
import net.nonylene.photolinkviewer.core.R
import net.nonylene.photolinkviewer.core.tool.OkHttpManager
import java.util.*

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
        (view.findViewById(R.id.path_list_view) as ListView).adapter = SaveDialogLitAdapter(infoList)
        return AlertDialog.Builder(activity).setView(view)
                .setTitle(getString(R.string.plv_core_save_dialog_title))
                .setPositiveButton(getString(R.string.plv_core_save_dialog_positive), { dialogInterface, i ->
                    // get filename
                    val listView = dialog.findViewById(R.id.path_list_view) as ListView
                    val newInfoList = (0..infoList.size - 1).map {
                        Info(
                                (listView.getChildAt(it).findViewById(R.id.path_edit_text) as EditText).text.toString(),
                                infoList[it].downloadUrl, infoList[it].thumbnailUrl)
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

    //todo: move to recyclerview
    class SaveDialogLitAdapter(private val infoList: List<Info>) : BaseAdapter() {

        class ViewHolder(private val itemView: View) {
            val thumbImageView: ImageView
            val savePathEditText: EditText

            init {
                thumbImageView = itemView.findViewById(R.id.path_image_view) as ImageView
                savePathEditText = itemView.findViewById(R.id.path_edit_text) as EditText
            }

            fun bindView(info: Info) {
                OkHttpManager.getPicasso(itemView.context).load(info.thumbnailUrl).into(thumbImageView)
                savePathEditText.setText(info.fileName)
            }
        }

        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup): View? {
            val view = convertView ?: View.inflate(viewGroup.context, R.layout.plv_core_save_path_item, null).apply {
                tag = ViewHolder(this)
            }
            (view.tag as ViewHolder).bindView(infoList[position])
            return view
        }

        override fun getItem(position: Int): Any? {
            return position
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return infoList.size
        }
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
