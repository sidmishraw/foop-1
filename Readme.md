# Foop

This is a WIP.

To build the project from scratch, please use the included gradle wrapper.

### For *NIX systems:
```bash
./gradlew -Dorg.gradle.java.home="$(/usr/libexec/java_home -version 1.8.0_144)" build
```

### For Windows systems:
```cmd
gradlew.bat -Dorg.gradle.java.home="path-to-jdk" build
```

It will build the project into an Eclipse project ready to be imported.

### Prerequisites:
* Eclipse with JDK8-9 support, preferably Mars v2 and above
* [Project Lombok plugin](https://projectlombok.org/setup/eclipse)
* [Eclipse Gradle plugin](https://marketplace.eclipse.org/content/buildship-gradle-integration)