package com.rkscientificindustries.invoice.backend.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("demo")
public class DataLoaderRegistry {
  private static final Logger logger = LoggerFactory.getLogger(DataLoaderRegistry.class);

  private final List<DataLoader> loaders;

  public DataLoaderRegistry(List<DataLoader> loaders) {
    this.loaders = loaders;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void loadAll() {
    logger.info("Starting data loading with {} loaders", loaders.size());
    loaders.forEach(loader -> {
      try {
        loader.load();
      } catch (Exception e) {
        logger.error("Error loading data with {}", loader.getClass().getSimpleName(), e);
      }
    });
    logger.info("✅ Data loading completed");
  }
}
