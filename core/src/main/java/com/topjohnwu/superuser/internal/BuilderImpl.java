/*
 * Copyright 2021 John "topjohnwu" Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.topjohnwu.superuser.internal;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.topjohnwu.superuser.NoShellException;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class BuilderImpl extends Shell.Builder {

    public BuilderImpl(Shell.Factory factory) {
        addShell(factory);
    }

    @NonNull
    @Override
    public Shell build() {
        Context context = Utils.getContext();
        outer:
        for (Shell.Factory factory : factories) {
            Shell shell;
            try {
                shell = new ShellImpl(timeout, flags, factory);
            } catch (Throwable e) {
                exceptionHandler.onException(factory, e);
                continue;
            }
            List<Shell.Initializer> initializers = factory.getInitializers();
            for (Shell.Initializer initializer : initializers) {
                if (!initializer.onInit(context, shell)) {
                    continue outer;
                }
            }
            return shell;
        }
        throw new NoShellException("Unable to create shell!");
    }

    @NonNull
    @Override
    public Shell build(String... commands) {
        Shell shell;
        try {
            shell = new ShellImpl(timeout, flags, Shell.Factory.create(commands));
        } catch (IOException e) {
            Utils.ex(e);
            throw new NoShellException("Unable to create a shell!", e);
        }
        return shell;
    }
}
