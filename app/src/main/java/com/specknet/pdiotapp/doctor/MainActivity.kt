package com.specknet.pdiotapp.doctor

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {


    companion object {
        private const val TAG = "MainActivity"
    }

    private val database = Firebase.database

    private var rootDbPath = ""
    private var rootRef: DatabaseReference? = null
    private var rootValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val value = snapshot.value
            val map = value as Map<String, Any>

            userList.clear()
            map.entries.forEach {
                val uid = it.key
                val value1 = it.value as Map<String, Any>

                val userInfoBean = UserInfoBean()
                userInfoBean.uid = uid

                if (value1["userInfo"] != null) {
                    val userInfoMap = value1["userInfo"] as Map<String, Any>
                    val email = userInfoMap["email"]
                    Log.i(TAG, "onDataChange: email =  ${email}")
                    userInfoBean.email = email.toString()
                } else {
                    userInfoBean.email = uid
                }
                userList.add(userInfoBean)
            }
            updateUIList(userList)

        }

        override fun onCancelled(error: DatabaseError) {

        }

    }

    private val userList = mutableListOf<UserInfoBean>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        rootDbPath = ""
        rootRef = database.getReference(rootDbPath)
        rootRef?.addValueEventListener(rootValueEventListener)

    }

    private fun updateUIList(userList: List<UserInfoBean>) {

        val listView = findViewById<ListView>(R.id.user_list)


        // 给listview的每一个item设置点击事件
        listView.setOnItemClickListener { parent, view, position, id ->
            showBottomSheetDialog(userList[position].uid)
        }

        listView.adapter = object : BaseAdapter() {
            // 列表展示多少条数据
            override fun getCount(): Int {
                return userList.size
            }

            override fun getItem(i: Int): Any {
                return userList[i]
            }

            override fun getItemId(position: Int): Long {
                return position.toLong()
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

                var itemView = convertView
                if (itemView == null) {
                    itemView = layoutInflater.inflate(R.layout.item_alert_message, parent, false)
                }

                // 这个itemview要展示的数据
                val email = userList[position].email

                val tvEmail = itemView!!.findViewById<TextView>(R.id.tv_email)

                tvEmail.text = email.toString()


                return itemView
            }
        }

    }

    private fun showBottomSheetDialog(uid: String) {
        val bottomSheetDialog = BottomSheetDialog(this)


        val view = View.inflate(this, R.layout.main_bottom_sheet_dialog, null)

        bottomSheetDialog.setContentView(view)

        view.findViewById<View>(R.id.btn_spo2)?.setOnClickListener {
            Log.i(TAG, "showBottomSheetDialog: btn_spo2")
            bottomSheetDialog.dismiss()

            val intent = Intent(this, Spo2AndHeartRateHistoryActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
        }

        view.findViewById<View>(R.id.btn_action)?.setOnClickListener {
            Log.i(TAG, "showBottomSheetDialog: btn_action")
            bottomSheetDialog.dismiss()

            val intent = Intent(this, ActionHistoryActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
        }

        view.findViewById<View>(R.id.btn_realtime_action)?.setOnClickListener {
            Log.i(TAG, "showBottomSheetDialog: btn_realtime_action")
            bottomSheetDialog.dismiss()

            val intent = Intent(this, ActionRealtimeHistoryActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
        }

        bottomSheetDialog.show()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.home_title_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {

            R.id.navigation_logout -> {
                logout()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun logout() {


        // this 就是 MainActivity的实例
        // this.startActivity(Intent(this, LoginActivity::class.java))


        AlertDialog.Builder(this).setTitle("确定退出吗").setMessage("是否退出")
            .setPositiveButton("确定",
                // 匿名内部类
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                        // 这里的 this 是匿名内部类 不是HomeActivity
                        // this.startActivity(Intent(this, LoginActivity::class.java))

                        // firebase退出方法
                        Firebase.auth.signOut()
                        this@MainActivity.startActivity(
                            Intent(
                                this@MainActivity, LoginActivity::class.java
                            )
                        )
                        finish()
                    }

                }).setNegativeButton("取消", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {

                }

            }).show()


    }


}