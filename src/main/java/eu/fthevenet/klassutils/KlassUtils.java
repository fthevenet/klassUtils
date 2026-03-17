/*
 * Copyright 2026 Frederic Thevenet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.fthevenet.klassutils;

import net.bytebuddy.ByteBuddy;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Command(
        name = "klass-utils",
        aliases = {"ku"},
        mixinStandardHelpOptions = true,
        version = KlassUtils.VERSION
)
public class KlassUtils {

    public static final String VERSION = "0.1";

    @Command(name = "generate-dummy-classes",
            aliases = {"gen"},
            mixinStandardHelpOptions = true
    )
    public int generateDummyClasses(
            @Option(names = {"-o", "--output"},
                    description = "Output path") Path output,
            @Option(names = {"-n", "--num-classes"},
                    description = "number of classes to generate",
                    defaultValue = "1") int n,
            @Option(names = {"-m", "--num-methods"},
                    description = "number of methods per class",
                    defaultValue = "1") int m,
            @Option(names = {"-f", "--num-fields"},
                    description = "number of fields per class",
                    defaultValue = "1") int f,
            @Option(names = {"-w", "--wait"},
                    description = "Wait for user action before halting") boolean wait) throws IOException {
        if (Files.exists(output)) {
            if (!Files.isDirectory(output)) {
                throw new IOException("Output path cannot be a file");
            }
        } else {
            Files.createDirectories(output);
        }
        for (int i = 0; i < n; i++) {
            String className = "Class_" + i;
            try (var writer = new BufferedWriter(new FileWriter(output.resolve(className + ".java").toString()))) {
                writer.write(String.format("""
                        public class %s {
                        
                        """, className));
                for (int j = 0; j < f; j++) {
                    writer.write(String.format("""
                                public int foo_%s;
                            """, j, j));
                }
                writer.write("""
                        
                        """);
                for (int j = 0; j < m; j++) {
                    writer.write(String.format("""
                                public void bar%s(int i, int j){
                                    var v = i + j;
                                }
                            
                            """, j));
                }
                writer.write("""
                        }
                        """);
            }
        }
        try (var writer = new BufferedWriter(new FileWriter(output.resolve("Main.java").toString()))) {
            writer.write(String.format("""
                    public class ClassLoadTest {
                        public static void main(String[] args) {
                            try {
                                for (int i = 0; i < %s; i++) {
                                    System.out.println("Load Class_" + i);
                                    Main.class.getClassLoader().loadClass("Class_" + i);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    """, n));
            if (wait) {
                writer.write("""
                            IO.readln("Press any key...");
                        """);
            }
            writer.write("""
                        }
                    }
                    """);
        }
        return 0;
    }

    @Command(name = "load-dummy-classes",
            aliases = {"load"},
            mixinStandardHelpOptions = true
    )
    public int emitDummyClasses(
            @Option(names = {"-n", "--num-classes"},
                    description = "number of classes to generate",
                    defaultValue = "1") int n,
            @Option(names = {"-w", "--wait"},
                    description = "Wait for user action before halting") boolean wait,
            @Option(names = {"-v", "--verbose"}) boolean verbose) {
        // retain the objects to avoid class unloading
        Object[] keepAlive = new Object[n];
        int progress = -1;
        for (int i = 0; i < n; i++) {
            String className = "Class_" + i;
            keepAlive[i] = new ByteBuddy()
                    .subclass(Object.class)
                    .name(className)
                    .make()
                    .load(getClass().getClassLoader())
                    .getLoaded();
            int current = (int) Math.round(((double) i / n) * 100);
            if (verbose) {
                IO.print(String.format("\r%s%% (%s/%s)", current, i, n));
            } else {
                if (current > progress) {
                    IO.print(String.format("\r%s%%", current));
                    progress = current;
                }
            }
        }
        if (wait) {
            IO.readln("Press any key...");
        }
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new KlassUtils()).execute(args);
        System.exit(exitCode);
    }
}
