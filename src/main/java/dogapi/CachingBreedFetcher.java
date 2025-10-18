package dogapi;

import java.util.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
/**
 * This BreedFetcher caches fetch request results to improve performance and
 * lessen the load on the underlying data source. An implementation of BreedFetcher
 * must be provided. The number of calls to the underlying fetcher are recorded.
 *
 * If a call to getSubBreeds produces a BreedNotFoundException, then it is NOT cached
 * in this implementation. The provided tests check for this behaviour.
 *
 * The cache maps the name of a breed to its list of sub breed names.
 */
public class CachingBreedFetcher implements BreedFetcher {

    private final BreedFetcher delegate;
    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();
    private int callsMade = 0;

    public CachingBreedFetcher(BreedFetcher fetcher) {
        this.delegate = Objects.requireNonNull(fetcher, "fetcher must not be null");
    }

    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        Objects.requireNonNull(breed, "breed must not be null");
        final String key = breed.trim().toLowerCase();

        List<String> cached = cache.get(key);
        if (cached != null) return cached;

        callsMade++;
        try {
            List<String> fetched = delegate.getSubBreeds(breed);
            List<String> snapshot = Collections.unmodifiableList(List.copyOf(fetched));
            cache.put(key, snapshot);
            return snapshot;
        } catch (BreedNotFoundException e) {
            throw e;
        }
    }
    public int getCallsMade() {
        return callsMade;
    }
}