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

package com.github.blindpirate.gogradle.build;

import com.github.blindpirate.gogradle.common.GoSourceCodeFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;

public class TestPatternFilter extends GoSourceCodeFilter {
    public static TestPatternFilter withPattern(List<String> patterns) {
        WildcardFileFilter wildcardFileFilter = new WildcardFileFilter(patterns);
        Predicate<File> andPredicate = (file) -> wildcardFileFilter.accept(file) && isTestGoFile(file);
        return new TestPatternFilter(andPredicate);
    }

    private TestPatternFilter(Predicate<File> filePredicate) {
        super(filePredicate);
    }
}
