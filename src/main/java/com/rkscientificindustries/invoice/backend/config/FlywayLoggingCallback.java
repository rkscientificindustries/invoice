package com.rkscientificindustries.invoice.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FlywayLoggingCallback implements Callback {
  @Override
  public String getCallbackName() {
    return "FlywayLoggingCallback";
  }

  @Override
  public boolean supports(Event event, Context context) {
    return event == Event.BEFORE_EACH_MIGRATE
        || event == Event.AFTER_EACH_MIGRATE
        || event == Event.BEFORE_MIGRATE
        || event == Event.AFTER_MIGRATE;
  }

  @Override
  public boolean canHandleInTransaction(Event event, Context context) {
    return true;
  }

  @Override
  public void handle(Event event, Context context) {
    switch (event) {
      case BEFORE_MIGRATE -> log.info("🚀 Starting database migrations...");
      case AFTER_MIGRATE -> log.info("✅ Database migrations completed successfully.");
      case BEFORE_EACH_MIGRATE -> log.info("⏳ Executing migration: v{} - {}",
          context.getMigrationInfo().getVersion(),
          context.getMigrationInfo().getDescription());
      case AFTER_EACH_MIGRATE -> log.info("✔️ Successfully applied migration: v{}",
          context.getMigrationInfo().getVersion());
    }
  }
}

