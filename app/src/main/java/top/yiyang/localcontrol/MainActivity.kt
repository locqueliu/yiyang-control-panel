package top.yiyang.localcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import top.yiyang.localcontrol.data.DeviceApiClient
import top.yiyang.localcontrol.data.DiscoveryScanner
import top.yiyang.localcontrol.data.LocalStorage
import top.yiyang.localcontrol.ui.MainViewModel
import top.yiyang.localcontrol.ui.MainViewModelFactory
import top.yiyang.localcontrol.ui.YiyangApp
import top.yiyang.localcontrol.ui.theme.YiyangTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val apiClient = DeviceApiClient()
        val storage = LocalStorage(applicationContext)
        val scanner = DiscoveryScanner(applicationContext, apiClient)

        setContent {
            YiyangTheme {
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(storage, scanner),
                )
                YiyangApp(
                    viewModel = viewModel,
                    apiClient = apiClient,
                    storage = storage,
                )
            }
        }
    }
}


