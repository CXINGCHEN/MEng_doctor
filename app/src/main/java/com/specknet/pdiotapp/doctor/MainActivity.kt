package com.specknet.pdiotapp.doctor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {


    companion object {
        private const val TAG = "MainActivity"
    }

    private val database = Firebase.database
    private val sdf = SimpleDateFormat("yyyyMMdd")
    private val gson = Gson()

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

                if(value1["userInfo"] != null) {
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
            val intent = Intent(this, UserDetailActivity::class.java)
            intent.putExtra("uid", userList[position].uid)
            startActivity(intent)
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


}