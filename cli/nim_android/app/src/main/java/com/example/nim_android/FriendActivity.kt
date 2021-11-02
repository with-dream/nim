package com.example.nim_android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.nim_android.databinding.ActivityFriendBinding

class FriendActivity : AppCompatActivity() {
    val fragmentList = listOf(FriendListFragment(), GroupListFragment())
    lateinit var binding: ActivityFriendBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewpager.adapter = PagerAdapter(supportFragmentManager, fragmentList)
    }

    class PagerAdapter(fm: FragmentManager, private val data: List<Fragment>) :
        FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return data.size
        }

        override fun getItem(position: Int): Fragment {
            return data[position]
        }
    }
}