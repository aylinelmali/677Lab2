# Asterix and the Trading Post

This is the source code of the *Asterix and the Trading Post* lab.

## How to run

This project uses Java and the Gradle build tool. Therefore, you need Java to run the program.

I use the following JDK on my machine:

    openjdk version "17.0.11" 2024-04-16 LTS
    OpenJDK Runtime Environment Corretto-17.0.11.9.1 (build 17.0.11+9-LTS)
    OpenJDK 64-Bit Server VM Corretto-17.0.11.9.1 (build 17.0.11+9-LTS, mixed mode, sharing)

To see your Java version, run `java -version`.

### Unix-based system:

1. First `cd` into the project folder.
2. Run `./gradlew build` to generate the .jar file. You will see a .jar file in `./build/libs`. **IMPORTANT: Always run this command when changing the source code!**
3. Execute the jar file with `java -jar <path_to_jar_file> <number_of_peers>`. 
   - `<path_to_jar_file>`: Path to the .jar file.
   - `<number_of_peers>`: The number of peers in the system.

Here is an example of the last step:

    java -jar ./build/libs/AsterixAndTheTradingPost-1.0-SNAPSHOT.jar 3

### Windows-based system:

1. First `cd` into the project folder.
2. Run `gradlew.bat build` to generate the .jar file. You will see a .jar file in `\build\libs`. **IMPORTANT: Always run this command when changing the source code!**
3. Execute the jar file with `java -jar <path_to_jar_file> <number_of_peers>`.
   - `<path_to_jar_file>`: Path to the .jar file. This should be the full path, starting from C:\Users\...
   - `<number_of_peers>`: The number of peers in the system.