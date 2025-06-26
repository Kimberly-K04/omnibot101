package com.kwamboka.omnibot101

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.kwamboka.omnibot101.navigation.AppNavHost
import com.kwamboka.omnibot101.navigation.ROUTE_SPLASH

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    // Start your navigation host with splash screen as start destination
                    AppNavHost()
                }
            }
        }
    }
}
