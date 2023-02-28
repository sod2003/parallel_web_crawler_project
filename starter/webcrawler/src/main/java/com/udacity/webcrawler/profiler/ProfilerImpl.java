package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);
    Method[] methods = klass.getMethods();
    boolean flag = false;
    for (Method method : methods) {
    	if (method.isAnnotationPresent(Profiled.class)) {
    		flag = true;
    	}
    }  
    if (!flag) {
        throw new IllegalArgumentException("Profiled Annotation is missing");
    }
    
    Object proxy = Proxy.newProxyInstance(
                klass.getClassLoader(),
                new Class<?>[] {klass},
                new ProfilingMethodInterceptor(delegate, clock, state));
    return (T) proxy;
  }

  @Override
  public void writeData(Path path) {
	   try(Writer writer = Files.newBufferedWriter(path)) {
	    	state.write(writer);
	   } catch (Exception e) {
			e.printStackTrace();
	   }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
