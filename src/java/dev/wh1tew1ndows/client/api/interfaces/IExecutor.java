package dev.wh1tew1ndows.client.api.interfaces;

import dev.wh1tew1ndows.common.impl.thread.ThreadPool;

public interface IExecutor {
    ThreadPool THREAD_POOL = new ThreadPool();
}
