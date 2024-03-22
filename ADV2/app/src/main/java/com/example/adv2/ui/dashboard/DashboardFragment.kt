package com.example.adv2.ui.dashboard

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.adv2.R
import com.example.adv2.databinding.FragmentDashboardBinding
import com.example.adv2.ui.notifications.NotificationsFragment
import com.example.adv2.ui.notifications.NotificationsViewModel

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    private val notificationsViewModel: NotificationsViewModel by activityViewModels()
    private val serverUrl: String = "http://116.205.128.125:8000/upload-files/"
    companion object {
        private const val TAG = "ZYZ"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupListeners()

        dashboardViewModel.startCamera()




    }
    private fun setupObservers() {
        dashboardViewModel.previewViewProvider.observe(viewLifecycleOwner) { previewViewProvider ->
            previewViewProvider?.let { bindCameraUseCases(it.preview, it.videoCapture, it.cameraSelector) }
        }

        dashboardViewModel.recordingState.observe(viewLifecycleOwner) { isRecording ->
            updateButtonState(isRecording)
        }

        dashboardViewModel.recordingMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                dashboardViewModel.clearRecordingMessage()
            }
        }
    }
    private fun setupListeners() {
        binding.videoCaptureButton.setOnClickListener {
            dashboardViewModel.captureVideo()
        }
        binding.uploadButton.setOnClickListener{
            dashboardViewModel.uploadFiles(serverUrl)
            findNavController().navigate(R.id.action_dashboard_to_notifications)
        }
    }
    private fun updateButtonState(isRecording: Boolean) {
        if (isRecording) {
            binding.videoCaptureButton.text = getString(R.string.stop_capture)

        } else {
            binding.videoCaptureButton.text = getString(R.string.start_capture)
        }
        binding.videoCaptureButton.isEnabled = true
    }


    private fun bindCameraUseCases(preview: Preview, videoCapture: VideoCapture<Recorder>, cameraSelector: CameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, videoCapture)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onPause() {
        super.onPause()
        // 如果ExecutorService和传感器监听在整个Fragment或Activity生命周期内都需要，可以考虑移到onDestroy中
//        cameraExecutor.shutdown()
        dashboardViewModel.unregisterSensorListeners()
    }

}