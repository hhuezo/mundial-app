pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Mirror de Maven Central (mismo contenido; otro host que repo.maven.apache.org)
        maven {
            name = "MavenCentral"
            url = uri("https://repo1.maven.org/maven2/")
        }
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven {
            name = "MavenCentral"
            url = uri("https://repo1.maven.org/maven2/")
        }
        // Respaldo si el host principal de Maven no responde en tu red
        maven {
            name = "AliyunMavenCentral"
            url = uri("https://maven.aliyun.com/repository/central")
        }
    }
}

rootProject.name = "Mundial"
include(":app")
