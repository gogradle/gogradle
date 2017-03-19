package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.task.AbstractGolangTask;
import com.github.blindpirate.gogradle.task.GolangTaskContainer;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.NumberUtils;
import com.google.common.collect.ImmutableMap;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.util.IOUtils.copyURLToFile;
import static com.github.blindpirate.gogradle.util.IOUtils.countLines;
import static com.github.blindpirate.gogradle.util.IOUtils.decodeInternally;
import static com.github.blindpirate.gogradle.util.IOUtils.mkdir;
import static com.github.blindpirate.gogradle.util.IOUtils.readLines;
import static com.github.blindpirate.gogradle.util.IOUtils.safeListFiles;
import static com.github.blindpirate.gogradle.util.IOUtils.write;
import static com.github.blindpirate.gogradle.util.StringUtils.render;
import static com.github.blindpirate.gogradle.util.StringUtils.substring;
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;
import static java.util.Arrays.asList;
import static org.gradle.internal.impldep.org.apache.commons.lang.StringUtils.lastIndexOf;

public class GoCoverTask extends AbstractGolangTask {

    public static final String COVERAGE_PROFILES_PATH = ".gogradle/reports/coverage/profiles";
    private static final String COVERAGE_HTMLS_PATH = ".gogradle/reports/coverage";
    private static final String COVERAGE_HTML_STATIC_PATH = ".gogradle/reports/coverage/static";
    private static final String COVERAGE_STATIC_RESOURCE = "coverage/static/";
    private static final Logger LOGGER = Logging.getLogger(GoCoverTask.class);

    @Inject
    private GolangPluginSetting setting;
    @Inject
    private BuildManager buildManager;

    public GoCoverTask() {
        dependsOn(GolangTaskContainer.TEST_TASK_NAME);
    }

    @TaskAction
    public void coverage() {
        if (profileGeneratedInTest()) {
            safeListFiles(new File(getProject().getRootDir(), COVERAGE_PROFILES_PATH))
                    .forEach(this::analyzeProfile);
            writeIndexHtml();
            updateCoverageHtmls();
            copyStaticResources();
        } else {
            LOGGER.warn("No coverage profile generated in test task, skip.");
        }
    }

    private boolean profileGeneratedInTest() {
        return getTask(GoTestTask.class).isGenerateCoverageProfile();
    }

    private void copyStaticResources() {
        mkdir(getProject().getRootDir(), COVERAGE_HTML_STATIC_PATH);
        List<String> files = readLines(
                GoCoverTask.class.getClassLoader().getResourceAsStream(COVERAGE_STATIC_RESOURCE));

        files.forEach(fileName -> {
            URL url = getClass().getClassLoader().getResource(COVERAGE_STATIC_RESOURCE + "/" + fileName);
            File file = new File(getProject().getRootDir(), COVERAGE_HTML_STATIC_PATH + "/" + fileName);
            copyURLToFile(url, file);
        });
    }

    private void updateCoverageHtmls() {
        safeListFiles(new File(getProject().getRootDir(), COVERAGE_HTMLS_PATH))
                .stream()
                .filter(File::isFile)
                .forEach(file -> {
                    String html = IOUtils.toString(file);
                    html = html.replace("<select id=\"files\">",
                            "<a href=\"index.html\" style=\"color:white;\">"
                                    + getProject().getName()
                                    + "</a><span style=\"color: white;\"> &gt; </span><select id=\"files\">");
                    write(file, html);
                });
    }

    private void writeIndexHtml() {
        List<PackageCoverage> packageCoverages =
                safeListFiles(new File(getProject().getRootDir(), COVERAGE_PROFILES_PATH))
                        .stream()
                        .map(this::extractCoverageInfo)
                        .collect(Collectors.toList());
        long totalMissedLines = calculateTotalMissedLines(packageCoverages);
        long totalLines = calculateLines(packageCoverages);
        int totalCoverageRate = NumberUtils.percentage(totalMissedLines, totalLines);

        sortByName(packageCoverages);
        sortByCoverageRate(packageCoverages);
        setMaxPackageLines(packageCoverages);

        Map<String, Object> context = ImmutableMap.<String, Object>builder()
                .put("packages", packageCoverages)
                .put("projectName", getProject().getName())
                .put("gogradleVersion", GogradleGlobal.GOGRADLE_VERSION)
                .put("totalMissedLines", totalMissedLines)
                .put("totalCoverageRate", totalCoverageRate)
                .put("totalLines", totalLines)
                .build();

        String template = IOUtils.toString(getClass().getClassLoader()
                .getResourceAsStream("coverage/templates/index.html.template"));
        String html = render(template, context);
        write(getProject().getRootDir(), COVERAGE_HTMLS_PATH + "/index.html", html);
    }

    private long calculateMaxPackageLines(List<PackageCoverage> packageCoverages) {
        Optional<Long> ret = packageCoverages
                .stream()
                .map(PackageCoverage::getTotalLineCount)
                .max(Long::compare);
        return ret.isPresent() ? ret.get() : 0L;
    }

    private long calculateLines(List<PackageCoverage> packageCoverages) {
        return packageCoverages.stream()
                .mapToLong(PackageCoverage::getTotalLineCount)
                .sum();
    }

    private long calculateTotalMissedLines(List<PackageCoverage> packageCoverages) {
        return packageCoverages.stream()
                .mapToLong(PackageCoverage::getUncoveredLineCount)
                .sum();
    }

    private void sortByCoverageRate(List<PackageCoverage> packageCoverages) {
        packageCoverages.sort(
                Comparator.comparing(PackageCoverage::getCoveredLineRate).reversed());
        for (int i = 0; i < packageCoverages.size(); ++i) {
            packageCoverages.get(i).coverageRateOrder = i;
        }
    }

    private void sortByName(List<PackageCoverage> packageCoverages) {
        packageCoverages.sort(
                Comparator.comparing(PackageCoverage::getName).reversed());
        for (int i = 0; i < packageCoverages.size(); ++i) {
            packageCoverages.get(i).nameOrder = i;
        }
    }

    private PackageCoverage extractCoverageInfo(File profileFile) {
        try {
            File htmlFile = profileFileToHtmlFile(profileFile);
            Document document = Jsoup.parse(htmlFile, GogradleGlobal.DEFAULT_CHARSET);
            Elements elements = document.select("option");
            PackageCoverage ret = elements.stream()
                    .map(this::extractFileCoverageInfo)
                    .collect(PackageCoverage::new, PackageCoverage::add, PackageCoverage::add);
            ret.name = decodeInternally(profileFile.getName());
            ret.url = htmlFile.getName();
            return ret;
        } catch (IOException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    // <option value="file0">github.com/gogits/gogs/models/access.go (0.0%)</option>
    @SuppressWarnings({"checkstyle:magicnumber"})
    private FileCoverage extractFileCoverageInfo(Element element) {
        String fileAndCoverage = element.text();
        int lastLeftParenIndex = lastIndexOf(fileAndCoverage, "(");
        String filePath = fileAndCoverage.substring(0, lastLeftParenIndex - 1);
        Double coverage = Double.parseDouble(substring(fileAndCoverage, lastLeftParenIndex + 1, -2));
        long lineCount = countLinesInFile(filePath);
        return new FileCoverage(coverage, lineCount);
    }

    private long countLinesInFile(String filePath) {
        Path relativePath = Paths.get(setting.getPackagePath()).relativize(Paths.get(filePath));
        return countLines(getProject().getRootDir().toPath().resolve(relativePath));
    }

    private void analyzeProfile(File profile) {
        String htmlOutputFilePath = toUnixString(profileFileToHtmlFile(profile));
        String profilePath = toUnixString(profile);
        buildManager.go(asList("tool", "cover", "-html=" + profilePath, "-o", htmlOutputFilePath), null);
    }

    private File profileFileToHtmlFile(File profile) {
        File htmlOutputDir = new File(getProject().getRootDir(), COVERAGE_HTMLS_PATH);
        return new File(htmlOutputDir, profile.getName() + ".html");
    }

    public void setMaxPackageLines(List<PackageCoverage> packages) {
        long maxPackageLines = calculateMaxPackageLines(packages);
        packages.forEach(p -> p.maxPackageTotalLine = maxPackageLines);
    }

    private static class PackageCoverage {
        private static final int MAX_BAR_WIDTH = 120;
        private String name;
        private String url;
        private long coveredLineCount;
        private long uncoveredLineCount;
        private int nameOrder;
        private int coverageRateOrder;

        // we need the maxmium package line count to calculate the width of coverage progress bar
        private long maxPackageTotalLine;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public int getNameOrder() {
            return nameOrder;
        }

        public int getCoverageRateOrder() {
            return coverageRateOrder;
        }

        public long getCoveredLineCount() {
            return coveredLineCount;
        }

        public long getUncoveredLineCount() {
            return uncoveredLineCount;
        }

        public long getTotalLineCount() {
            return coveredLineCount + uncoveredLineCount;
        }

        public int getCoveredLineRate() {
            return NumberUtils.percentage(coveredLineCount, coveredLineCount + uncoveredLineCount);
        }

        public long getCoveredLineWidth() {
            return MAX_BAR_WIDTH * coveredLineCount / maxPackageTotalLine;
        }

        public long getUncoveredLineWidth() {
            return MAX_BAR_WIDTH * uncoveredLineCount / maxPackageTotalLine;
        }

        private void add(FileCoverage coverage) {
            long coveredLinesInThatFile = Math.round(coverage.coverageRate * coverage.lineCount / 100d);

            coveredLineCount += coveredLinesInThatFile;
            uncoveredLineCount += coverage.lineCount - coveredLinesInThatFile;
        }

        private void add(PackageCoverage another) {
            coveredLineCount += another.coveredLineCount;
            uncoveredLineCount += another.uncoveredLineCount;
        }
    }

    private static class FileCoverage {
        private double coverageRate;
        private long lineCount;

        private FileCoverage(double coverageRate, long lineCount) {
            this.coverageRate = coverageRate;
            this.lineCount = lineCount;
        }
    }
}
