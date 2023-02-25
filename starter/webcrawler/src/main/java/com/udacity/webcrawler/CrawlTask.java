package com.udacity.webcrawler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

public class CrawlTask extends RecursiveAction {
	private final Clock clock;
	private final Instant deadline;
	private final PageParserFactory parserFactory;
	private final int maxDepth;
	private final List<Pattern> ignoredUrls;
	private final Set<String> visitedUrls;
	private final ForkJoinPool pool;
	private final String url;
	private final Map<String, Integer> counts;
	
	public CrawlTask(String url,
		      Instant deadline,
		      int maxDepth,
		      Map<String, Integer> counts,
		      Set<String> visitedUrls,
		      Clock clock,
		      ForkJoinPool pool,
		      List<Pattern> ignoredUrls,
		      PageParserFactory parserFactory
		      ) {
		this.url = url;
		this.clock = clock;
	    this.deadline = deadline;
	    this.maxDepth = maxDepth;
	    this.ignoredUrls = ignoredUrls;
	    this.visitedUrls = visitedUrls;
	    this.pool = pool;
	    this.counts = counts;
	    this.parserFactory = parserFactory;
	}


	@Override
	protected void compute() {
		if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
		      return;
		    }
	    for (Pattern pattern : ignoredUrls) {
	      if (pattern.matcher(url).matches()) {
	        return;
	      }
	    }
	    if (visitedUrls.contains(url)) {
	      return;
	    }
	    visitedUrls.add(url);
	    PageParser.Result result = parserFactory.get(url).parse();
	    for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
	      if (counts.containsKey(e.getKey())) {
	        counts.put(e.getKey(), e.getValue() + counts.get(e.getKey()));
	      } else {
	        counts.put(e.getKey(), e.getValue());
	      }
	    }
	    for (String link : result.getLinks()) {
	      pool.invoke(new CrawlTask(link, deadline, maxDepth - 1, counts, visitedUrls, clock, pool, ignoredUrls, parserFactory));
	    }
	}
}
