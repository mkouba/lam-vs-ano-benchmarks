///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus.qute:qute-core:999-SNAPSHOT

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;

public class generate {

    static String lamTemplate = """
            package org.acme.lam.beans;

            import java.util.function.Supplier;

            public class MyBean{index} {

                public Supplier<Integer> ping(int value) {
                    return () -> value + 1;
                }

            }
                """;

    static String anoTemplate = """
            package org.acme.ano.beans;

            import java.util.function.Supplier;

            public class MyBean{index} {

                public Supplier<Integer> ping(int value) {
                    return new Supplier<Integer>() {
                        public Integer get() {
                            return value + 1;
                        }
                    };
                }

            }
            """;

    static String testTemplate = """
            package org.acme.{package}.test;

            {#for i in beanCount}
            import org.acme.{package}.beans.MyBean{i_index};
            {/for}

            public class MyBeanTest {

                {#for i in beanCount}
                private MyBean{i_index} myBean{i_index} = new MyBean{i_index}();
                {/for}

                public int ping(int val) {
                    {#for i in beanCount}
                    val = new MyBean{i_index}().ping(val).get();
                    {/for}
                    return val;
                }

                public int pingStateful(int val) {
                    {#for i in beanCount}
                    val = this.myBean{i_index}.ping(val).get();
                    {/for}
                    return val;
                }

                public int getBeanCount() {
                    return {beanCount};
                }

            }
            """;

    public static void main(String... args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("Type, count and target must be set");
        }
        String type = args[0];
        int count = Integer.parseInt(args[1]);
        String target = args[2];

        String userDirectory = System.getProperty("user.dir");
        Engine engine = Engine.builder().addDefaults().build();
        Template sourceTemplate = type.equals("ano") ? engine.parse(anoTemplate)
                : engine.parse(lamTemplate);
        Template testTemplate = engine.parse(generate.testTemplate);

        Path targetDirectory = Path.of(userDirectory, target);
        Path beansDirectory = targetDirectory.resolve("beans");

        if (Files.exists(beansDirectory)) {
            Files.walk(beansDirectory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectory(beansDirectory);

        for (int i = 0; i < count; i++) {
            String fileName = "MyBean" + i + ".java";
            Path path = beansDirectory.resolve(fileName);
            Files.writeString(targetDirectory.resolve(path), sourceTemplate.data("index", i).render());
        }
        System.out.println("Generated " + count + " bean files into " + beansDirectory);

        Files.writeString(targetDirectory.resolve("test").resolve("MyBeanTest.java"),
                testTemplate.data("package",  type, "beanCount", count).render());

    }

}
