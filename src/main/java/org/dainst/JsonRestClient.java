package org.dainst;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @author Daniel M. de Oliveira
 */
class JsonRestClient {

    final static Logger logger = Logger.getLogger(JsonRestClient.class);

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private final String url;
    private static final OkHttpClient client = new OkHttpClient();

    public JsonRestClient(
            final String url) {
        this.url = url;
    }

    /**
     *
     * @param path
     * @param method
     * @param json
     * @return null if a JsonNode could not get generated properly
     *   from the response body.
     */
    private JsonNode restApi(String path, String method, JsonNode json) {

        Request.Builder b = getBuilder(method, json).url(url + path);

        Response response = null;
        try {
            response = client.newCall(b.build()).execute();
            String body= response.body().string();
            if (body.isEmpty()) return null;
            return jsonNode(body);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    private static JsonNode jsonNode(String s) throws IOException {
        return new ObjectMapper().readTree(s);
    }

    Request.Builder getBuilder(String method, JsonNode json) {
        Request.Builder b = new Request.Builder();

        if (method.equals("GET")) {
            b.get();
        } else if
                (method.equals("DELETE")) {
            b.delete();
        }
        else {
            RequestBody body = RequestBody.create(JSON, json.toString());
            if (method.equals("POST")) {
                b.post(body);
            }
            if (method.equals("PUT")) {
                b.put(body);
            }
        }
        return b;
    }

    public JsonNode post(String path, JsonNode json) {
        return restApi(path,"POST",json);
    }

    public JsonNode put(String path, JsonNode json) {
        return restApi(path,"PUT",json);
    }

    public JsonNode get(String path) {
        return restApi(path,"GET",null);
    }

    public JsonNode delete(String path) {
        return restApi(path,"DELETE",null);
    }
}