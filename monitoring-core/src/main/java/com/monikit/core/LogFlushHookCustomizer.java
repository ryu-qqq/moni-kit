package com.monikit.core;

import java.util.List;

public interface LogFlushHookCustomizer {
    void customize(List<LogFlushHook> hooks);
}