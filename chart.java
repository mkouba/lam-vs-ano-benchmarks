///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.knowm.xchart:xchart:3.8.8
//DEPS com.google.code.gson:gson:2.11.0

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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.style.CategoryStyler;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * JBang script to create some charts from the files stored in the results directory.
 */
public class chart {

    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            throw new IllegalStateException("Results directory must be set");
        }

        String userDirectory = System.getProperty("user.dir");
        Path resultsDirectory = Path.of(userDirectory, args[0]);
        if (!Files.exists(resultsDirectory)) {
            throw new IllegalStateException("Results directory does not exist: " + resultsDirectory);
        }
        List<Path> resultFiles = Files.list(resultsDirectory).toList();
        plotChart(resultFiles.stream().filter(p -> !p.getFileName().toString().contains("-gc")).toList(), "");
        plotChart(resultFiles.stream().filter(p -> p.getFileName().toString().contains("-gc")).toList(), "-gc");
    }

    static void plotChart(List<Path> resultFiles, String chartFileSuffix) throws IOException {

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
        // E.g. "results-lam" for "results-lam.json" and "results-lam-gc.json"
        Map<String, Series> seriesMap = new HashMap<>();

        for (Path file : resultFiles) {
            String fileName = file.getFileName().toString();
            String seriesName = fileName.contains("-lam") ? "Lambda" : "Anonymous";
            // results-ano-10.json -> 10
            String beanCount = padZeros(fileName.substring(fileName.lastIndexOf("-") + 1, fileName.lastIndexOf(".")));
            Series series = seriesMap.get(seriesName);
            if (series == null) {
                series = new Series(seriesName, new ArrayList<>());
                seriesMap.put(seriesName, series);
            }

            JsonArray results = readJsonElementFromFile(file.toFile()).getAsJsonArray();
            for (JsonElement result : results) {
                JsonObject benchmark = result.getAsJsonObject();
                // E.g. "org.acme.ano.MyBeanAnoBenchmark.stateful"
                String benchmarkName = benchmark.get("benchmark").getAsString() + "_" + beanCount;
                if (benchmarkName.contains(".")) {
                    benchmarkName = benchmarkName.substring(benchmarkName.lastIndexOf(".") + 1, benchmarkName.length());
                }
                boolean hasGcAlloc = benchmark.has("secondaryMetrics")
                        && benchmark.getAsJsonObject("secondaryMetrics").has("gc.alloc.rate");
                if (hasGcAlloc) {
                    benchmarkName += "_gc.alloc.rate";
                }
                BigDecimal score;
                BigDecimal error;
                if (hasGcAlloc) {
                    // GC alloc profiler
                    JsonObject gcAllocRate = benchmark.getAsJsonObject("secondaryMetrics")
                            .getAsJsonObject("gc.alloc.rate");
                    score = gcAllocRate.get("score").getAsBigDecimal().setScale(0, RoundingMode.HALF_UP);
                    JsonElement scoreErrorElement = gcAllocRate.get("scoreError");
                    error = "NaN".equals(scoreErrorElement.getAsString()) ? BigDecimal.ZERO
                            : scoreErrorElement.getAsBigDecimal().setScale(0, RoundingMode.HALF_UP);
                } else {
                    score = benchmark.get("primaryMetric").getAsJsonObject().get("score").getAsBigDecimal()
                            .setScale(0, RoundingMode.HALF_UP);
                    error = benchmark.get("primaryMetric").getAsJsonObject().get("scoreError").getAsBigDecimal()
                            .setScale(0, RoundingMode.HALF_UP);
                }
                series.data().add(new SeriesData(benchmarkName, score, error));
            }
        }

        for (Series series : seriesMap.values()) {
            series.sortData();
            chart.addSeries(series.name(), series.data().stream().map(SeriesData::metricName).toList(),
                    series.data().stream().map(SeriesData::score).toList(),
                    series.data().stream().map(SeriesData::error).toList());
        }

        // Save as png
        BitmapEncoder.saveBitmap(chart, "./lambda-vs-anonymous" + chartFileSuffix, BitmapFormat.PNG);
    }

    static JsonElement readJsonElementFromFile(File inputFile) throws IOException {
        try (Reader reader = Files.newBufferedReader(inputFile.toPath(), Charset.forName("UTF-8"))) {
            return JsonParser.parseReader(reader);
        }
    }

    static String padZeros(String val) {
        if (val.length() >= 4) {
            return val;
        }
        return "0".repeat(4 - val.length()) + val;
    }

    record Series(String name, List<SeriesData> data) {

        void sortData() {
            data.sort(Comparator.comparing(SeriesData::metricName));
        }

    }

    record SeriesData(String metricName, BigDecimal score, BigDecimal error) {
    }
}
