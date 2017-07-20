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

package com.github.blindpirate.gogradle.core.exceptions;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.util.StringUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

public class ResolutionStackWrappingException extends RuntimeException {
    private ResolutionStackWrappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ResolutionStackWrappingException wrapWithResolutionStack(Throwable e, ResolveContext context) {
        Deque<GolangDependency> resolutionStack = new ArrayDeque<>();
        while (context != null) {
            resolutionStack.addFirst(context.getDependency());
            context = context.getParent();
        }

        StringBuilder message = new StringBuilder("Exception in resolution, message is:\n");
        message.append(e.getMessage()).append("\n");
        message.append("Resolution stack is:\n");

        AtomicInteger counter = new AtomicInteger(0);

        resolutionStack.forEach(dependency -> {
            StringUtils.appendNSpaces(message, counter.getAndIncrement());
            message.append("+- ").append(dependency.resolve(null).toString()).append("\n");
        });

        return new ResolutionStackWrappingException(message.toString(), e);
    }
}
