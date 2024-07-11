#!/bin/bash
# Bash script to run some benchmarks based on the configuration

# Benchmark configuration
WARMUP_ITERATIONS=3
ITERATIONS=5
# Numbers of generated beans
counts=(5 50 100)
# Corresponding batch sizes
batches=(100000 10000 1000)

generateBeans() {
    local testType=$1
    local dirName="lambda-test-${testType}"
    local count=$2
    ./generate.java $testType $count "${dirName}/src/main/java/org/acme/${testType}/"
}

plotCharts() {
    ./chart.java results
}

runBenchmark() {
    local testType=$1
    local dirName="lambda-test-${1}"
    local count=$2
    local batch=$3
    cd $dirName
    mvn clean package
    java -jar "target/lambda-test-${testType}-1.0.0-SNAPSHOT.jar" -i $ITERATIONS -wi $WARMUP_ITERATIONS -bs $batch -wbs $batch -rf json -rff "../results/results-${testType}-${count}.json"
    java -jar "target/lambda-test-${testType}-1.0.0-SNAPSHOT.jar" -i $ITERATIONS -wi $WARMUP_ITERATIONS -bs $batch -wbs $batch -f 1 -prof gc -rf json -rff "../results/results-${testType}-gc-${count}.json"
    cd ../
}

# First remove previous results
rm -rf results
mkdir results
# Next run the benchmarks
for countKey in "${!counts[@]}"
do
    count=${counts[$countKey]}
    batch=${batches[$countKey]}
    generateBeans "ano" $count
    runBenchmark "ano" $count $batch
    generateBeans "lam" $count
    runBenchmark "lam" $count $batch
done
# Then charts are genereated
plotCharts

