package com.znggis.sampleservicebookingapp.di.component

import com.znggis.sampleservicebookingapp.di.module.FactoryModule
import com.znggis.sampleservicebookingapp.di.module.ViewModelModule
import com.znggis.sampleservicebookingapp.ui.home.HomeFragment
import dagger.Component


@Component(
    dependencies = [CoreComponent::class],
    modules = [
        FactoryModule::class,
        ViewModelModule::class
    ]
)
interface HomeComponent {

    fun inject(homeFragment: HomeFragment)

    @Component.Factory
    interface Factory {
        fun create(
            component: CoreComponent
        ): HomeComponent
    }
}