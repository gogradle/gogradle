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

package com.github.blindpirate.gogradle.common;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;

@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public abstract class AbstractFileFilter extends org.apache.commons.io.filefilter.AbstractFileFilter {
    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return acceptDir(file);
        } else {
            return acceptFile(file);
        }
    }

    protected abstract boolean acceptFile(File file);

    protected abstract boolean acceptDir(File dir);
}
