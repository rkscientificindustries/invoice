package com.rkscientificindustries.invoice.backend.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Profile("demo")
@Component
public class DataLoaderRegistry {
  private final List<DataLoader> loaders;

  public DataLoaderRegistry(List<DataLoader> loaders) {
    this.loaders = loaders;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void loadAll() {
    log.info("Starting data loading with {} loaders", loaders.size());
    loaders.forEach(loader -> {
      try {
        loader.load();
      } catch (Exception e) {
        log.error("Error loading data with {}", loader.getClass().getSimpleName(), e);
      }
    });
    log.info("✅ Data loading completed");
  }
}
