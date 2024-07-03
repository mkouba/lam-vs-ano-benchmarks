#!/bin/sh
COUNT="10"
BATCH=10000

# Generate beans with anonymous classes
./generate.java ano $COUNT lambda-test-ano/src/main/java/org/acme/ano/

cd lambda-test-ano
mvn clean package
java -jar target/lambda-test-ano-1.0.0-SNAPSHOT.jar -bs $BATCH -wbs $BATCH -rf json -rff ../results-ano.json
java -jar target/lambda-test-ano-1.0.0-SNAPSHOT.jar -bs $BATCH -wbs $BATCH -f 1 -prof gc -rf json -rff ../results-ano-gc.json
cd ../

# Generate beans with lambdas
./generate.java lam $COUNT lambda-test-lam/src/main/java/org/acme/lam/
cd lambda-test-lam
mvn clean package
java -jar target/lambda-test-lam-1.0.0-SNAPSHOT.jar -bs $BATCH -wbs $BATCH -rf json -rff ../results-lam.json
java -jar target/lambda-test-lam-1.0.0-SNAPSHOT.jar -bs $BATCH -wbs $BATCH -f 1 -prof gc -rf json -rff ../results-lam-gc.json
cd ../

# Generate chart
./chart.java results-ano.json results-lam.json results-lam-gc.json results-ano-gc.json