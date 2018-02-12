/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.task.AbstractGolangTask;
import com.github.blindpirate.gogradle.task.GolangTaskContainer;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.NumberUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.google.common.collect.ImmutableMap;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.util.IOUtils.copyURLToFile;
import static com.github.blindpirate.gogradle.util.IOUtils.countLines;
import static com.github.blindpirate.gogradle.util.IOUtils.decodeInternally;
import static com.github.blindpirate.gogradle.util.IOUtils.mkdir;
import static com.github.blindpirate.gogradle.util.IOUtils.safeListFiles;
import static com.github.blindpirate.gogradle.util.IOUtils.write;
import static com.github.blindpirate.gogradle.util.StringUtils.render;
import static com.github.blindpirate.gogradle.util.StringUtils.substring;
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;
import static java.util.Arrays.asList;

public class GoCover extends AbstractGolangTask {

    public static final String COVERAGE_PROFILES_PATH = ".gogradle/reports/coverage/profiles";
    private static final String COVERAGE_HTMLS_PATH = ".gogradle/reports/coverage";
    private static final String COVERAGE_HTML_STATIC_PATH = ".gogradle/reports/coverage/static";
    private static final String COVERAGE_STATIC_RESOURCE = "/coverage/static/";

    @Inject
    private GolangPluginSetting setting;
    @Inject
    private BuildManager buildManager;

    public GoCover() {
        setDescription("Run coverage task and generate coverage report.");
        dependsOn(GolangTaskContainer.TEST_TASK_NAME);
    }

    @TaskAction
    public void coverage() {
        setGogradleGlobalContext();

        List<File> coverageFiles = safeListFiles(new File(getProject().getProjectDir(), COVERAGE_PROFILES_PATH));
        coverageFiles.forEach(this::analyzeProfile);
        writeIndexHtml();
        updateCoverageHtmls();
        copyStaticResources();
    }

    @InputDirectory
    @SkipWhenEmpty
    public File getInputCoverageDirectory() {
        return new File(getProject().getProjectDir(), COVERAGE_PROFILES_PATH);
    }

    @OutputDirectory
    public File getCoverageDirectory() {
        return new File(getProject().getProjectDir(), COVERAGE_HTMLS_PATH);
    }

    private List<File> getOutputHtmls() {
        return safeListFiles(new File(getProject().getProjectDir(), COVERAGE_HTMLS_PATH))
                .stream()
                .filter(file -> file.isFile() && file.getName().endsWith(".html"))
                .collect(Collectors.toList());
    }

    private void copyStaticResources() {
        mkdir(getProject().getProjectDir(), COVERAGE_HTML_STATIC_PATH);
        List<String> files = asList("bundle.gif", "down.gif", "greenbar.gif", "group.gif",
                "package.gif", "redbar.gif", "report.gif", "sort.gif", "source.gif", "up.gif");

        files.forEach(fileName -> {
            URL url = getClass().getResource(COVERAGE_STATIC_RESOURCE + fileName);
            File file = new File(getProject().getProjectDir(), COVERAGE_HTML_STATIC_PATH + "/" + fileName);
            copyURLToFile(url, file);
        });
    }

    private void updateCoverageHtmls() {
        getOutputHtmls().forEach(file -> {
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
                safeListFiles(new File(getProject().getProjectDir(), COVERAGE_PROFILES_PATH))
                        .stream()
                        .map(this::extractCoverageInfo)
                        .filter(PackageCoverage::isNotEmpty)
                        .collect(Collectors.toList());
        long totalMissedLines = calculateTotalMissedLines(packageCoverages);
        long totalLines = calculateLines(packageCoverages);
        int totalCoverageRate = NumberUtils.percentage(totalLines - totalMissedLines, totalLines);

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
        write(getProject().getProjectDir(), COVERAGE_HTMLS_PATH + "/index.html", html);
    }

    private long calculateMaxPackageLines(List<PackageCoverage> packageCoverages) {
        Optional<Long> ret = packageCoverages
                .stream()
                .map(PackageCoverage::getTotalLineCount)
                .max(Long::compare);
        return ret.orElse(0L);
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
            ret.name = decodeInternally(removeOutExtension(profileFile));
            ret.url = htmlFile.getName();
            return ret;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String removeOutExtension(File profileFile) {
        // file.out -> file
        return profileFile.getName().substring(0, profileFile.getName().length() - 4);
    }

    // <option value="file0">github.com/gogits/gogs/models/access.go (0.0%)</option>
    @SuppressWarnings({"checkstyle:magicnumber"})
    private FileCoverage extractFileCoverageInfo(Element element) {
        String fileAndCoverage = element.text();
        int lastLeftParenIndex = StringUtils.lastIndexOf(fileAndCoverage, "(");
        String filePath = fileAndCoverage.substring(0, lastLeftParenIndex - 1);
        Double coverage = Double.parseDouble(substring(fileAndCoverage, lastLeftParenIndex + 1, -2));
        long lineCount = countLinesInFile(filePath);
        return new FileCoverage(coverage, lineCount);
    }

    private long countLinesInFile(String filePath) {
        Path relativePath = Paths.get(setting.getPackagePath()).relativize(Paths.get(filePath));
        return countLines(getProject().getProjectDir().toPath().resolve(relativePath));
    }

    private void analyzeProfile(File profile) {
        String htmlOutputFilePath = toUnixString(profileFileToHtmlFile(profile));
        String profilePath = toUnixString(profile);
        buildManager.go(asList("tool", "cover", "-html=" + profilePath, "-o", htmlOutputFilePath),
                Collections.emptyMap());
    }

    private File profileFileToHtmlFile(File profile) {
        File htmlOutputDir = new File(getProject().getProjectDir(), COVERAGE_HTMLS_PATH);
        return new File(htmlOutputDir, removeOutExtension(profile) + ".html");
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
            long width = MAX_BAR_WIDTH * coveredLineCount / maxPackageTotalLine;
            return width <= 0 ? 1 : width;
        }

        public long getUncoveredLineWidth() {
            long width = MAX_BAR_WIDTH * uncoveredLineCount / maxPackageTotalLine;
            return width <= 0 ? 1 : width;
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

        public static boolean isNotEmpty(PackageCoverage packageCoverage) {
            return packageCoverage.coveredLineCount != 0 || packageCoverage.uncoveredLineCount != 0;
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
