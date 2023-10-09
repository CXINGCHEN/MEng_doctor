package com.specknet.pdiotapp.doctor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class ActionHistoryActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ActionHistoryActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_history)


        val uid = intent.getStringExtra("uid") ?: ""
        Log.i(TAG, "onCreate: uid = $uid")


        val historyFragment = HistoryFragment.newInstance()

        val bundle = Bundle()
        bundle.putString("uid",uid)
        historyFragment.arguments = bundle


        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, historyFragment).commit()

    }
}