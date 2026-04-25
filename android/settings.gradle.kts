pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DrinkReminder"

include(":app")

// Core
include(":core:core-designsystem")
include(":core:core-model")
include(":core:core-notification")

// Feature
include(":feature:feature-onboarding")
include(":feature:feature-reminder-list")
include(":feature:feature-water-reminder")
include(":feature:feature-log")
include(":feature:feature-settings")

// Data
include(":data:data-settings")
