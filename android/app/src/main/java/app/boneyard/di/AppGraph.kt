package app.boneyard.di

import android.app.Application
import app.boneyard.domain.repository.SettingsRepository
import com.slack.circuit.foundation.Circuit
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@DependencyGraph(AppScope::class, bindingContainers = [DatabaseBindings::class])
interface AppGraph {
    val settingsRepository: SettingsRepository
    val circuit: Circuit

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: Application): AppGraph
    }
}
