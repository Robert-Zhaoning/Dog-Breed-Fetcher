package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     *
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */

    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        if (breed == null || breed.isBlank()) {
            throw new IllegalArgumentException("Breed must not be null or blank");
        }

        String normalized = breed.trim().toLowerCase(Locale.ROOT);
        String url = "https://dog.ceo/api/breed/" + normalized + "/list";

        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {

            if (response.body() == null) {
                throw new BreedNotFoundException("Empty response from Dog API");
            }

            String jsonData = response.body().string();
            JSONObject json = new JSONObject(jsonData);
            String status = json.optString("status", "");
            int code = json.optInt("code", 0);
            String apiMsg = json.optString("message", "");

            if ("error".equalsIgnoreCase(status) && code == 404) {
                throw new BreedNotFoundException(
                        apiMsg.isBlank() ? ("Breed not found: " + normalized) : apiMsg
                );
            }

            if (!response.isSuccessful() || !"success".equalsIgnoreCase(status)) {
                throw new BreedNotFoundException(
                        "Unexpected Dog API response: HTTP " + response.code() + ", status=" + status +
                                (apiMsg.isBlank() ? "" : (", message=" + apiMsg))
                );
            }

            JSONArray arr = json.optJSONArray("message");
            List<String> result = new ArrayList<>();
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    result.add(arr.getString(i));
                }
            }
            return Collections.unmodifiableList(result);
        } catch (org.json.JSONException e) {
            throw new BreedNotFoundException("Failed to parse Dog API JSON");
        } catch (IOException e) {
            throw new BreedNotFoundException("I/O error calling Dog API");
        }
    }
}