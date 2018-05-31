package chat.rocket.android.wear.dagger

import android.app.Application
import chat.rocket.android.wear.app.RocketChatWearApplication
import chat.rocket.android.wear.dagger.module.ActivityBuilder
import chat.rocket.android.wear.dagger.module.AppModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, AppModule::class, ActivityBuilder::class])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: RocketChatWearApplication)
}