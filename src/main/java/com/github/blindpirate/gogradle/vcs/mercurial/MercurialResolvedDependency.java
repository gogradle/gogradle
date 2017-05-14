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

package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.vcs.VcsResolvedDependency;
import com.github.blindpirate.gogradle.vcs.VcsType;

public class MercurialResolvedDependency extends VcsResolvedDependency {
    public MercurialResolvedDependency(String name, String url, String commitId, long commitTime) {
        super(name, url, commitId, commitTime);
    }

    @Override
    public VcsType getVcsType() {
        return VcsType.MERCURIAL;
    }
}
