package com.appleader707.syncrecorder.presentation.ui.show_by_chart

import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.appleader707.syncrecorder.navigation.Router
import com.appleader707.syncrecorder.navigation.Screen
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch

@Composable
fun ShowByChartScreen(
    router: Router? = null,
    viewModel: ShowByChartViewModel = hiltViewModel()
) {
    val viewState by viewModel.state.collectAsState()
    val viewEffect by viewModel.effect.asFlow().collectAsState(ShowByChartViewEffect.DoNothing)

    LaunchedEffect(viewEffect) {
        when (val effect = viewEffect) {
            ShowByChartViewEffect.DoNothing -> {}
            ShowByChartViewEffect.GoBackRecordingPage -> {
                router?.goBack(Screen.Recording.route)
            }
        }
        viewModel.clearEffect()
    }

    LaunchedEffect(Unit) {
        viewModel.processEvent(ShowByChartViewEvent.LoadData)
    }

    ShowByChartLayout(
        viewState = viewState,
        onEventHandler = viewModel::processEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowByChartLayout(
    viewState: ShowByChartViewState,
    onEventHandler: (ShowByChartViewEvent) -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var isBottomSheetVisible by remember { mutableStateOf(false) }

    // Main Layout
    Box(modifier = Modifier.fillMaxSize()) {
        // Video Player
        viewState.videoUri?.let { uri ->
            AndroidView(
                factory = { context ->
                    val player = ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(uri))
                        prepare()
                        playWhenReady = false
                    }
                    PlayerView(context).apply {
                        this.player = player
                        useController = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // دکمه باز کردن Bottom Sheet
        Button(
            onClick = {
                isBottomSheetVisible = true
                scope.launch { bottomSheetState.show() }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        ) {
            Text("نمایش نمودارها")
        }

        // Bottom Sheet
        if (isBottomSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                        isBottomSheetVisible = false
                    }
                },
                sheetState = bottomSheetState,
                modifier = Modifier.fillMaxSize().padding(top = 52.dp),
                containerColor = androidx.compose.ui.graphics.Color.LightGray,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Accelerometer", color = androidx.compose.ui.graphics.Color.Black)
                    Chart(
                        xData = viewState.chartDataX,
                        yData = viewState.chartDataY,
                        zData = viewState.chartDataZ
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Gyroscope", color = androidx.compose.ui.graphics.Color.Black)
                    Chart(
                        xData = viewState.gyroDataX,
                        yData = viewState.gyroDataY,
                        zData = viewState.gyroDataZ
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Magnetometer", color = androidx.compose.ui.graphics.Color.Black)
                    Chart(
                        xData = viewState.magnetDataX,
                        yData = viewState.magnetDataY,
                        zData = viewState.magnetDataZ
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(onClick = {
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            isBottomSheetVisible = false
                        }
                    }) {
                        Text("بستن")
                    }
                }
            }
        }
    }
}

@Composable
fun Chart(
    xData: List<Entry>,
    yData: List<Entry>,
    zData: List<Entry>,
) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                data = LineData(
                    LineDataSet(xData, "X").apply {
                        color = Color.RED
                        setDrawValues(false)
                        setDrawCircles(false)
                        lineWidth = 2f
                    },
                    LineDataSet(yData, "Y").apply {
                        color = Color.GREEN
                        setDrawValues(false)
                        setDrawCircles(false)
                        lineWidth = 2f
                    },
                    LineDataSet(zData, "Z").apply {
                        color = Color.BLUE
                        setDrawValues(false)
                        setDrawCircles(false)
                        lineWidth = 2f
                    }
                )
                description.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)
            }
        },
        modifier = Modifier
            .height(150.dp)
            .fillMaxWidth(0.6f)
    )
}