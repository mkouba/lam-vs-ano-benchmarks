package org.acme.ano;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.acme.ano.test.MyBeanTest;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
public class MyBeanAnoBenchmark {

    int val = 0;

    MyBeanTest test = new MyBeanTest();

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 3, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(3)
    public void stateless() {
        int result = test.ping(val);
        if ((val + test.getBeanCount()) != result) {
            throw new IllegalStateException("Unexpected result: " + result);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 3, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(3)
    public void stateful() {
        int val = 0;
        int result = test.pingStateful(val);
        if ((val + test.getBeanCount()) != result) {
            throw new IllegalStateException("Unexpected result: " + result);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 3) {
            throw new IllegalArgumentException("Type, loop and sleep must be set");
        }
        String type = args[0];
        int loop = Integer.parseInt(args[1]);
        long sleep = Long.parseLong(args[2]);

        MyBeanAnoBenchmark benchmark = new MyBeanAnoBenchmark();

        if (type.equals("stateless")) {
            for (int i = 0; i < loop; i++) {
                benchmark.stateless();
            }
        } else {
            for (int i = 0; i < loop; i++) {
                benchmark.stateful();
            }
        }

        printMemoryUsage();

        for (int i = 1; i <= sleep; i++) {
            System.out.println("Sleeping [" + i + "/" + sleep + "]");
            TimeUnit.SECONDS.sleep(1);
        }
    }

    static void printMemoryUsage() {
        System.out.println("\n\nMemory usage\nxxxxxxxxxxxx\n");
        List<MemoryPoolMXBean> pool = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean bean : pool) {
            System.out.println(bean.getName() + ": " + bean.getUsage());
        }
        System.out.println("\n\n");
    }

}
