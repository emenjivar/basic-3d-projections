package com.emenjivar.threedimensionalprojections

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.emenjivar.threedimensionalprojections.ui.MainScreen
import com.emenjivar.threedimensionalprojections.ui.theme.ThreeDimensionalProjectionsTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ThreeDimensionalProjectionsTheme {
                MainScreen()
            }
        }
    }
}

