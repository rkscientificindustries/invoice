package com.rkscientificindustries.invoice.backend.data;

 /// Plugin interface for data loading strategies.
 /// Implementations should be registered as @Component beans with @Profile("demo").
public interface DataLoader {
  void load();
}
