package com.specknet.pdiotapp.doctor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson


/**
 * 用户数据
 * 目前有两个按钮
 */
class UserDetailActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserDetailActivity"
    }

    private var uid = ""

    private val gson = Gson()

    private val database = Firebase.database

    private var reference: DatabaseReference? = null

    private var valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val value = snapshot.value
            val map = value as Map<String, Any>

            Log.i(TAG, "onDataChange: ${gson.toJson(map)}")

        }

        override fun onCancelled(error: DatabaseError) {

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)
        uid = intent.getStringExtra("uid") ?: ""
        Log.i(TAG, "onCreate: uid = $uid")


        findViewById<Button>(R.id.btnAction).setOnClickListener {
            val intent = Intent(this, ActionHistoryActivity::class.java)
            intent.putExtra("uid",uid)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnSpo2AndHeartRate).setOnClickListener {
            val intent = Intent(this, Spo2AndHeartRateHistoryActivity::class.java)
            intent.putExtra("uid",uid)
            startActivity(intent)
        }


        val dbPath = uid
        reference = database.getReference(dbPath)
        reference?.addValueEventListener(valueEventListener)


    }


    override fun onDestroy() {
        super.onDestroy()
        reference?.removeEventListener(valueEventListener)
    }

}