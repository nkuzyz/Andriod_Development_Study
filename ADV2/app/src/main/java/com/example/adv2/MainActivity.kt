package com.example.adv2

import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.adv2.databinding.ActivityMainBinding
import com.example.adv2.function.PermissionsManager
import com.example.adv2.ui.dashboard.DashboardViewModel
import com.example.adv2.ui.notifications.NotificationsViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionsManager: PermissionsManager
    // 创建一个全局的ViewModel
//    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //初始化视图绑定
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 使用ViewModelProvider来获取或创建SharedViewModel的实例
//        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        //获取BottomNavigationView的引用
        val navView: BottomNavigationView = binding.navView

        //NavController是负责管理应用导航的对象。
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        supportActionBar?.hide() // 隐藏顶部栏
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
//            )
//        )

        //设置ActionBar与NavController的连接
//        setupActionBarWithNavController(navController, appBarConfiguration)

        // 设置底部导航与NavController的连接
        navView.setupWithNavController(navController)

        // get permission
        // Request camera permissions
        // 获得权限

        permissionsManager = PermissionsManager(this)
        if (!permissionsManager.allPermissionsGranted()) {
            permissionsManager.requestPermissions()
        }
    }
    // 添加一个方法来获取SharedViewModel实例
//    fun getSharedViewModel(): SharedViewModel {
//        return sharedViewModel
//    }
}

