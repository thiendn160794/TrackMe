package com.thiendn.trackme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        addFragment(HistoryFragment())

        toolbar.setOnClickListener {
            addFragment(PracticeFragment())
        }
    }

    private fun addFragment(fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .add(flContainer.id, fragment)
            .addToBackStack(fragment.tag)
            .commit()
        println(supportFragmentManager.backStackEntryCount)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1)
            supportFragmentManager.popBackStackImmediate()
        else
            super.onBackPressed()
    }

}