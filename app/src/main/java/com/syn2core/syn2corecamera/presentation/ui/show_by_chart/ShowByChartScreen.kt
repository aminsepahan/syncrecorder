package com.syn2core.syn2corecamera.presentation.ui.show_by_chart

import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.syn2core.syn2corecamera.navigation.Router
import com.syn2core.syn2corecamera.navigation.Screen

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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Charts Sensors") },
                navigationIcon = {
                    IconButton(onClick = {
                        onEventHandler(ShowByChartViewEvent.GoBackToRecordingPage)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Accelerometer")
                    Chart(
                        xData = viewState.chartDataX,
                        yData = viewState.chartDataY,
                        zData = viewState.chartDataZ
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Gyroscope")
                    Chart(
                        xData = viewState.gyroDataX,
                        yData = viewState.gyroDataY,
                        zData = viewState.gyroDataZ
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Magnetometer")
                    Chart(
                        xData = viewState.magnetDataX,
                        yData = viewState.magnetDataY,
                        zData = viewState.magnetDataZ
                    )
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