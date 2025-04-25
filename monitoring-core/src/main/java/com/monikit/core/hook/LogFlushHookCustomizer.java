package com.monikit.core.hook;

import java.util.List;

public interface LogFlushHookCustomizer {
    void customize(List<LogFlushHook> hooks);
}