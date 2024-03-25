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
            Log.d(TAG,"old:${notificationsViewModel.getLastUploadResult()}")
            if (notificationsViewModel.getLastUploadResult()!=result){
                notificationsViewModel.addAssistantMessageString(result)
                notificationsViewModel.updateLastUploadResult(result)
            }
        }

        dashboardViewModel.imageAzimuthData.observe(viewLifecycleOwner){
                data ->
            // 检查是否有新的URI和方位角信息，并与之前的信息进行比较
            val lastImageAzimuth = notificationsViewModel.getLastImageAzimuth()
            if (lastImageAzimuth?.first != data.imageUri || lastImageAzimuth?.second != data.azimuth) {
                // 更新 ViewModel 中的最后图像URI和方位角信息
                notificationsViewModel.updateLastImageAzimuth(data.imageUri, data.azimuth)
                // 如果需要，这里可以进行额外的UI更新或其他逻辑处理
                notificationsViewModel.addImageMessage()
                // 例如，展示新图像和方位角信息
                Log.d(TAG, "dashboardViewModel: URI=${data.imageUri}, Azimuth=${data.azimuth}")
            }
        }
//        Log.d(TAG, "观察者回调触发，收到新值: $dashboardViewModel.uploadResult")

    }

    override fun onResume() {
        super.onResume()
        // 重新注册传感器监听器
        notificationsViewModel.registerSensorListeners()
    }
    override fun onPause() {
        super.onPause()
        notificationsViewModel.unregisterSensorListeners()
    }

}




