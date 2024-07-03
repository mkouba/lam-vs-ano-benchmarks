///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.knowm.xchart:xchart:3.8.8
//DEPS com.google.code.gson:gson:2.11.0

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.style.CategoryStyler;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class chart {

    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            throw new IllegalStateException("Source files must be set");
        }

        String userDirectory = System.getProperty("user.dir");
        List<Path> files = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            Path p = Path.of(userDirectory, args[i]);
            if (!Files.exists(p)) {
                throw new IllegalStateException("File does not exist: " + p);
            }
            files.add(p);
        }

        CategoryChart chart = new CategoryChartBuilder()
                .width(1920)
                .height(1080)
                .title("Lambda vs Anonymous - " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .xAxisTitle("Benchmark")
                .yAxisTitle("Score")
                .build();

        CategoryStyler styler = chart.getStyler();
        styler.setLegendPosition(LegendPosition.InsideNW);
        styler.setLabelsVisible(true);
        styler.setLabelsFont(new Font("Monospaced", Font.PLAIN, 16));

        // Series name is derived from the file name
        // E.g. "results-lam" fro "results-lam.json" and "results-lam-gc.json"
        Map<String, Series> seriesMap = new HashMap<>();

        for (Path file : files) {
            String seriesName = file.getFileName().toString().contains("-lam") ? "Lambda" : "Anonymous";
            Series series = seriesMap.get(seriesName);
            if (series == null) {
                series = new Series(seriesName, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                seriesMap.put(seriesName, series);
            }

            JsonArray results = readJsonElementFromFile(file.toFile()).getAsJsonArray();
            for (JsonElement result : results) {
                JsonObject benchmark = result.getAsJsonObject();
                // E.g. "org.acme.ano.MyBeanAnoBenchmark.stateful"
                String benchmarkName = benchmark.get("benchmark").getAsString();
                if (benchmarkName.contains(".")) {
                    benchmarkName = benchmarkName.substring(benchmarkName.lastIndexOf(".") + 1, benchmarkName.length());
                }
                boolean hasGcAlloc = benchmark.has("secondaryMetrics")
                        && benchmark.getAsJsonObject("secondaryMetrics").has("gc.alloc.rate");
                if (hasGcAlloc) {
                    benchmarkName += "_gc.alloc.rate";
                }
                series.metrics.add(benchmarkName);
                if (hasGcAlloc) {
                    // GC alloc profiler
                    JsonObject gcAllocRate = benchmark.getAsJsonObject("secondaryMetrics")
                            .getAsJsonObject("gc.alloc.rate");
                    series.scores().add(gcAllocRate.get("score").getAsBigDecimal().setScale(0, RoundingMode.HALF_UP));
                    series.errors()
                            .add(gcAllocRate.get("scoreError").getAsBigDecimal().setScale(0, RoundingMode.HALF_UP));
                } else {
                    series.scores()
                            .add(benchmark.get("primaryMetric").getAsJsonObject().get("score").getAsBigDecimal()
                                    .setScale(0, RoundingMode.HALF_UP));
                    series.errors().add(
                            benchmark.get("primaryMetric").getAsJsonObject().get("scoreError").getAsBigDecimal()
                                    .setScale(0, RoundingMode.HALF_UP));
                }
            }
        }

        for (Series series : seriesMap.values()) {
            chart.addSeries(series.name(), series.metrics(), series.scores(), series.errors());
        }

        // Save as png
        BitmapEncoder.saveBitmap(chart, "./lambda-vs-anonymous", BitmapFormat.PNG);
    }

    static JsonElement readJsonElementFromFile(File inputFile) throws IOException {
        try (Reader reader = Files.newBufferedReader(inputFile.toPath(), Charset.forName("UTF-8"))) {
            return JsonParser.parseReader(reader);
        }
    }

    record Series(String name, List<String> metrics, List<BigDecimal> scores, List<BigDecimal> errors) {
    }
}
