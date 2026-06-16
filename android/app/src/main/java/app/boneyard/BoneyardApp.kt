package app.boneyard

import android.app.Application
import app.boneyard.di.AppGraph
import dev.zacsweers.metro.createGraphFactory

class BoneyardApp : Application() {
    val appGraph: AppGraph by lazy {
        createGraphFactory<AppGraph.Factory>().create(this)
    }
}
