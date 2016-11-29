package com.github.blindpirate.golang.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class GolangPlugin implements Plugin<Project> {

    public static final String PREPARE_TASK_NAME = "prepare";
    public static final String DEPENDENCIES_TASK_NAME = "dependencies";
    public static final String MIGRATE_TASK_NAME = "migrate";
    public static final String CHECK_TASK_NAME = "check";
    public static final String BUILD_TASK_NAME = "build";
    public static final String CLEAN_TASK_NAME = "clean";
    public static final String INSTALL_TASK_NAME = "install";
    public static final String TEST_TASK_NAME = "test";
    public static final String COVERAGE_CHECK_TASK_NAME = "coverageCheck";

    // Not implemented yet
    public static final String MAKE_TASK_NAME = "make";
    public static final String FMT_TASK_NAME = "fmt";
    public static final String VET_TASK_NAME = "vet";
    public static final String GENERATE_TASK_NAME = "generate";
    public static final String TOOL_TASK_NAME = "tool";

    public static final String BUILD_CONFIGURATION_NAME = "build";

    @Override
    void apply(Project project) {
    }
}
