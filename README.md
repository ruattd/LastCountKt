# Last Count

Exam countdown timer for Chinese senior high students.

Based on Kotlin/JVM & Compose Desktop

Thanks to the compactness and flexibility of Compose, the whole application is written into [a single file](https://github.com/ruattd/LastCountKt/blob/main/src/main/kotlin/Main.kt).

## Usage

Install Java Runtime 21 or above, such as [Azul Zulu](https://www.azul.com/downloads/?version=java-21-lts&os=windows&architecture=x86-64-bit&package=jre#zulu)

Download it from [GitHub Releases](https://github.com/ruattd/LastCountKt/releases).

Drop it into an empty folder.

Double click to run or create a startup item with command: 
  - Linux/Mac: `java -jar LastCount-xxx.jar`
  - Windows: `javaw.exe -jar LastCount-xxx.jar`

## Build

Clone the repository and run `gradlew packageUberJarForCurrentOS`

For Chinese users: You may need a special network environment for gradle to download dependencies.

## Thanks

[Kotlin](https://kotlinlang.org/)

[Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)

[darkokoa/compose-datetime-wheel-picker](https://github.com/darkokoa/compose-datetime-wheel-picker)
