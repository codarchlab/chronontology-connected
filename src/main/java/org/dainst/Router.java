package org.dainst;

import static org.dainst.C.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpUtils;
import java.io.IOException;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

/**
 * @author Daniel M. de Oliveira
 */
public class Router {

    final static Logger logger = Logger.getLogger(Router.class);
    public static final String ID = ":id";

    private static JsonNode json(String s) throws IOException {
        return new ObjectMapper().readTree(s);
    }

    private boolean shouldBeDirect(final String directParam) {
        return (directParam!=null&&
                directParam.equals("true"));
    }

    /**
     * Converts a String to an int.
     *
     * @param sizeParam
     * @return -1 if sizeParam is null
     *   or cannot be parsed properly.
     */
    private Integer sizeAsInt(final String sizeParam) {
        if (sizeParam==null) return -1;
        int size = -1;
        try {
            if (sizeParam != null)
                size = Integer.parseInt(sizeParam);
        } catch (NumberFormatException e) {
            logger.error("Illegal format for number in param: " + sizeParam);
            return -1;
        }
        return size;
    }

    public Router(
            final FileSystemDatastoreConnector mainDatastore,
            final ElasticSearchDatastoreConnector connectDatastore
    ){

        get("/"+TYPE_NAME+"/", (req,res) -> {

                    return connectDatastore.search(
                        req.queryParams("q"), sizeAsInt(req.queryParams("size")));
                }
        );

        get("/" + TYPE_NAME + "/" + ID, (req,res) -> {

                    String id = req.params(ID);

                    if (shouldBeDirect(req.queryParams("direct")))
                        return mainDatastore.get(id);
                    else
                        return connectDatastore.get(id);
                }
        );

        post("/" + TYPE_NAME + "/" + ID, (req, res) -> {

                    String id = req.params(ID);
                    JsonNode oldDoc = mainDatastore.get(id);

                    res.header("location", id);

                    if (oldDoc!=null) {
                        res.status(HTTP_FORBIDDEN);
                        return "";
                    }

                    JsonNode doc = new DocumentModel(json(req.body()))
                            .addStorageInfo(id);

                    mainDatastore.put(id, doc);
                    connectDatastore.put(id, doc);

                    res.status(HTTP_CREATED);

                    return doc;
                }
        );

        put("/" + TYPE_NAME + "/" + ID, (req, res) -> {

                    String id = req.params(ID);
                    JsonNode oldDoc = mainDatastore.get(id);

                    res.header("location", id);

                    DocumentModel dm = new DocumentModel(json(req.body()));
                    JsonNode doc = null;
                    if (oldDoc!=null) {
                        doc= dm.addStorageInfo(oldDoc, id);
                        res.status(HTTP_OK);
                    } else {
                        doc= dm.addStorageInfo(id);
                        res.status(HTTP_CREATED);
                    }

                    mainDatastore.put(id, doc);
                    connectDatastore.put(id, doc);

                    return doc;
                }
        );
    }
}
