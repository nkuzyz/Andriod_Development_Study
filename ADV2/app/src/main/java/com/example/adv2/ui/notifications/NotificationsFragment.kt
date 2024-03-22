package com.example.adv2.ui.notifications

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.adv2.model.Message
import com.example.adv2.ui.dashboard.DashboardViewModel

import com.example.adv2.ui.theme.WechatDemoTheme

class NotificationsFragment : Fragment() {
    private val dashboardViewModel:DashboardViewModel by activityViewModels()
    private val notificationsViewModel: NotificationsViewModel by activityViewModels()
    private var newMessage: Message? = null

    companion object {
        private const val TAG = "ZYZ"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val navController = findNavController()

        return ComposeView(requireContext()).apply {
            setContent {
                WechatDemoTheme {
                    ChatScreen(navController = navController, notificationsViewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        dashboardViewModel = ViewModelProvider(requireActivity()).get(DashboardViewModel::class.java)
        setupObservers()
    }

    private fun setupObservers() {

        dashboardViewModel.uploadResult.observe(viewLifecycleOwner) { result ->
            //在viewmodel里操作list 列表加一条消息
            Log.d(TAG, "观察者回调触发，收到新值: $result")
            notificationsViewModel.addAssistantMessageString(result)
        }
//        Log.d(TAG, "观察者回调触发，收到新值: $dashboardViewModel.uploadResult")

    }

}




