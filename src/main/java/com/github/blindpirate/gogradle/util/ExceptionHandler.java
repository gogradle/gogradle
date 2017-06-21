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

package com.github.blindpirate.gogradle.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHandler {
    public static class UncheckedException extends RuntimeException {
        public UncheckedException(Throwable e) {
            super(e);
        }
    }

    public static RuntimeException uncheckException(Throwable e) {
        return new UncheckedException(e);
    }

    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static Throwable getRootCause(Throwable e) {
        while (true) {
            if (e.getCause() == null) {
                return e;
            }
            e = e.getCause();
        }
    }
}
