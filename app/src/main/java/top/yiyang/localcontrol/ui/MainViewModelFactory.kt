package top.yiyang.localcontrol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import top.yiyang.localcontrol.data.DiscoveryScanner
import top.yiyang.localcontrol.data.LocalStorage

class MainViewModelFactory(
    private val storage: LocalStorage,
    private val scanner: DiscoveryScanner,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(storage, scanner) as T
        }
        throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
    }
}

