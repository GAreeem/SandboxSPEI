package com.example.sandboxspei.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Habilita la ejecución asíncrona ({@code @Async}) y define el pool de
 * hilos dedicado al procesamiento en segundo plano de las operaciones.
 *
 * <p>Cada operación ocupa un hilo durante ~2 segundos (dos esperas de
 * {@code sandbox.retardo-ms}), por eso el pool es amplio y con cola grande:
 * así muchas operaciones simultáneas no se serializan unas tras otras.</p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String EJECUTOR_OPERACIONES = "procesadorOperacionesExecutor";

    @Bean(name = EJECUTOR_OPERACIONES)
    public Executor procesadorOperacionesExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(10_000);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("spei-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        return executor;
    }
}
