package com.example.ticketsystem.services;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

	private final Clock clock;
	private final ConcurrentMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

	public RateLimitService() {
		this(Clock.systemUTC());
	}

	RateLimitService(Clock clock) {
		this.clock = clock;
	}

	public boolean tryAcquire(String key, int maxRequests, Duration window) {
		if (maxRequests <= 0) {
			return true;
		}

		long now = clock.millis();
		long windowMillis = window.toMillis();
		WindowCounter counter = counters.computeIfAbsent(key, ignored -> new WindowCounter(now));
		return counter.tryAcquire(now, maxRequests, windowMillis);
	}

	private static final class WindowCounter {

		private long windowStartedAt;
		private int count;

		private WindowCounter(long windowStartedAt) {
			this.windowStartedAt = windowStartedAt;
		}

		private synchronized boolean tryAcquire(long now, int maxRequests, long windowMillis) {
			if (now - windowStartedAt >= windowMillis) {
				windowStartedAt = now;
				count = 0;
			}

			if (count >= maxRequests) {
				return false;
			}

			count++;
			return true;
		}
	}
}
