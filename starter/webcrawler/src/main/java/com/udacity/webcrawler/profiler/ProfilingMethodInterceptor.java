package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {
	private Object targetObject;
	private final Clock clock;
	private ProfilingState state;

  // TODO: You will need to add more instance fields and constructor arguments to this class.
  ProfilingMethodInterceptor(Object targetObject, Clock clock, ProfilingState state) {
	  this.targetObject = targetObject;
	  this.clock = Objects.requireNonNull(clock);
	  this.state = state;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // TODO: This method interceptor should inspect the called method to see if it is a profiled
    //       method. For profiled methods, the interceptor should record the start time, then
    //       invoke the method using the object that is being profiled. Finally, for profiled
    //       methods, the interceptor should record how long the method call took, using the
    //       ProfilingState methods.
	  Instant startTime = Instant.now();
      Object result = null;
      if (method.getDeclaringClass().equals(Object.class)) {
		  try {
			  result = method.invoke(targetObject, args);
		  } catch (InvocationTargetException e) {
		      throw e.getTargetException();
		  } catch (IllegalAccessException e) {
	          throw new RuntimeException(e);
		  }finally {
		      Duration elapsed = Duration.between(startTime, Instant.now());
		      state.record(targetObject.getClass(), method, elapsed);
		  }
      }
    return result;
  }
}
