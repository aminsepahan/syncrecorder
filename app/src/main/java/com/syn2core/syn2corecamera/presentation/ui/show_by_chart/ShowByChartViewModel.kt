package com.syn2core.syn2corecamera.presentation.ui.show_by_chart

import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.google.gson.Gson
import com.syn2core.common.ui.base.BaseViewModel
import com.syn2core.common.ui.livedata.SingleLiveData
import com.syn2core.syn2corecamera.business.usecase.directory.GetSyn2CoreCameraDirectoryUseCase
import com.syn2core.syn2corecamera.domain.SensorSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 *
 * @see BaseViewModel
 */

@HiltViewModel
class ShowByChartViewModel @Inject constructor(
    private val getSyn2CoreCameraDirectoryUseCase: GetSyn2CoreCameraDirectoryUseCase,
) : BaseViewModel<ShowByChartViewEvent>() {

    private val _state = MutableStateFlow(ShowByChartViewState())
    val state: StateFlow<ShowByChartViewState> = _state.asStateFlow()

    val effect = SingleLiveData<ShowByChartViewEffect>()

    override fun processEvent(event: ShowByChartViewEvent) {
        when (event) {
            ShowByChartViewEvent.LoadData -> loadDataFromJson()

            ShowByChartViewEvent.GoBackToRecordingPage -> {
                effect.postValue(ShowByChartViewEffect.GoBackRecordingPage)
            }

            is ShowByChartViewEvent.PlayerControlsVisibilityChanged -> {
                updateState { it.copy(showControls = event.visible) }
            }
        }
    }

    private fun loadDataFromJson() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dir = getSyn2CoreCameraDirectoryUseCase()
                val jsonFile = File(dir, "sensor_data_1.json")
                val videoFile = File(dir, "s2c_embedded_.mp4")

                if (jsonFile.exists()) {
                    val jsonString = jsonFile.readText()
                    val data: List<SensorSnapshot> = Gson().fromJson(
                        jsonString,
                        Array<SensorSnapshot>::class.java
                    ).toList()

                    val accelData = data.filter { it.name == "accelerometer" }
                    val gyroData = data.filter { it.name == "gyroscope" }
                    val magnetData = data.filter { it.name == "magnetometer" }

                    val accelX = mutableListOf<Entry>()
                    val accelY = mutableListOf<Entry>()
                    val accelZ = mutableListOf<Entry>()
                    val gyroX = mutableListOf<Entry>()
                    val gyroY = mutableListOf<Entry>()
                    val gyroZ = mutableListOf<Entry>()
                    val magnetX = mutableListOf<Entry>()
                    val magnetY = mutableListOf<Entry>()
                    val magnetZ = mutableListOf<Entry>()

                    accelData.forEachIndexed { index, snapshot ->
                        val t = index.toFloat()
                        accelX.add(Entry(t, snapshot.values[0]))
                        accelY.add(Entry(t, snapshot.values[1]))
                        accelZ.add(Entry(t, snapshot.values[2]))
                    }

                    gyroData.forEachIndexed { index, snapshot ->
                        val t = index.toFloat()
                        gyroX.add(Entry(t, snapshot.values[0]))
                        gyroY.add(Entry(t, snapshot.values[1]))
                        gyroZ.add(Entry(t, snapshot.values[2]))
                    }

                    magnetData.forEachIndexed { index, snapshot ->
                        val t = index.toFloat()
                        magnetX.add(Entry(t, snapshot.values[0]))
                        magnetY.add(Entry(t, snapshot.values[1]))
                        magnetZ.add(Entry(t, snapshot.values[2]))
                    }

                    updateState {
                        it.copy(
                            chartDataX = accelX,
                            chartDataY = accelY,
                            chartDataZ = accelZ,
                            gyroDataX = gyroX,
                            gyroDataY = gyroY,
                            gyroDataZ = gyroZ,
                            magnetDataX = magnetX,
                            magnetDataY = magnetY,
                            magnetDataZ = magnetZ,
                            videoUri = videoFile.toUri()
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateState(update: (ShowByChartViewState) -> ShowByChartViewState) {
        _state.update(update)
    }

    fun clearEffect() {
        effect.postValue(ShowByChartViewEffect.DoNothing)
    }
}