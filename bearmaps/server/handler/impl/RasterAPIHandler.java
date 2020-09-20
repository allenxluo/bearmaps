package bearmaps.server.handler.impl;

import bearmaps.AugmentedStreetMapGraph;
import bearmaps.server.handler.APIRouteHandler;
import spark.Request;
import spark.Response;
import bearmaps.utils.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bearmaps.utils.Constants.*;

/**
 * Handles requests from the web browser for map images. These images
 * will be rastered into one large image to be displayed to the user.
 * @author rahul, Josh Hug, _________
 */
public class RasterAPIHandler extends APIRouteHandler<Map<String, Double>, Map<String, Object>> {

    /**
     * Each raster request to the server will have the following parameters
     * as keys in the params map accessible by,
     * i.e., params.get("ullat") inside RasterAPIHandler.processRequest(). <br>
     * ullat : upper left corner latitude, <br> ullon : upper left corner longitude, <br>
     * lrlat : lower right corner latitude,<br> lrlon : lower right corner longitude <br>
     * w : user viewport window width in pixels,<br> h : user viewport height in pixels.
     **/
    private static final String[] REQUIRED_RASTER_REQUEST_PARAMS = {"ullat", "ullon", "lrlat",
            "lrlon", "w", "h"};

    /**
     * The result of rastering must be a map containing all of the
     * fields listed in the comments for RasterAPIHandler.processRequest.
     **/
    private static final String[] REQUIRED_RASTER_RESULT_PARAMS = {"render_grid", "raster_ul_lon",
            "raster_ul_lat", "raster_lr_lon", "raster_lr_lat", "depth", "query_success"};

    private static final double[] LON_DPP_BY_DEPTH = {3.4332275390625E-4, 1.71661376953125E-4, 8.58306884765625E-5, 4.291534423828125E-5, 2.1457672119140625E-5, 1.0728836059570312E-5, 5.364418029785156E-6, 2.682209014892578E-6};

    @Override
    protected Map<String, Double> parseRequestParams(Request request) {
        return getRequestParams(request, REQUIRED_RASTER_REQUEST_PARAMS);
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param requestParams Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @param response : Not used by this function. You may ignore.
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image;
     *                    can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    @Override
    public Map<String, Object> processRequest(Map<String, Double> requestParams, Response response) {
        System.out.println("yo, wanna know the parameters given by the web browser? They are:");
        System.out.println(requestParams);
        Map<String, Object> results = new HashMap<>();
        double ullat = requestParams.get("ullat");
        double ullon = requestParams.get("ullon");
        double lrlat = requestParams.get("lrlat");
        double lrlon = requestParams.get("lrlon");
        int level = findLevel((lrlon - ullon) / requestParams.get("w"));

        Map raster_ullon = getIndex(true, ullon, level);
        Map raster_ullat = getIndex(false, ullat, level);
        Map raster_lrlon = getIndex(true, lrlon, level);
        Map raster_lrlat = getIndex(false, lrlat, level);

        int raster_ullon_index = (int) raster_ullon.get("index");
        int raster_ullat_index = (int) raster_ullat.get("index");
        int raster_lrlon_index = (int) raster_lrlon.get("index");
        int raster_lrlat_index = (int) raster_lrlat.get("index");
        int raster_width = raster_lrlon_index - raster_ullon_index + 1;
        int raster_height = raster_lrlat_index - raster_ullat_index + 1;

        if (outOfBounds((int) raster_lrlon.get("inBounds"), (int) raster_ullon.get("inBounds")) || outOfBounds((int) raster_lrlat.get("inBounds"),(int) raster_ullat.get("inBounds"))) {
            results.put("query_success", false);
            return results;
        } else if (ullon > lrlon || ullat < lrlat) {
            results.put("query_success", false);
            return results;
        }
        else {
            results.put("query_success", true);
        }

        String[][] render_grid = new String[raster_height][raster_width];
        for (int i = 0; i < raster_height; i++) {
            for (int j = 0; j < raster_width; j++) {
                render_grid[i][j] = "d" + level + "_x" + (j + raster_ullon_index) + "_y" + (i + raster_ullat_index) + ".png";
            }
        }
        results.put("render_grid", render_grid);
        results.put("raster_ul_lon", getLonTileBounds(level, raster_ullon_index).get("ullon"));
        results.put("raster_ul_lat", getLatTileBounds(level, raster_ullat_index).get("ullat"));
        results.put("raster_lr_lon", getLonTileBounds(level, raster_lrlon_index).get("lrlon"));
        results.put("raster_lr_lat", getLatTileBounds(level, raster_lrlat_index).get("lrlat"));
        results.put("depth", level);

        return results;
    }

    public boolean outOfBounds(int c1, int c2) {
        return (c1 == -1 && c2 == -1) || (c1 == 1 && c2 == 1);
    }

    public int findLevel(double requestedDPP) {
        for (int i = 0; i < 7; i++) {
            if (LON_DPP_BY_DEPTH[i] <= requestedDPP) {
                return i;
            }
        }
        return 7;
    }

    public boolean inLonBoundsOfTile(double lon, HashMap<String, Double> tileBounds) {
        if (lon >= tileBounds.get("ullon") && lon <= tileBounds.get("lrlon")) {
            return true;
        }
        return false;
    }

    public boolean inLatBoundsOfTile(double lat, HashMap<String, Double> tileBounds) {
        if (lat <= tileBounds.get("ullat") && lat >= tileBounds.get("lrlat")) {
            return true;
        }
        return false;
    }

    public HashMap<String, Double> getLonTileBounds(int level, int x) {
        HashMap<String, Double> tileBounds = new HashMap<>();
        double w = (ROOT_LRLON - ROOT_ULLON) / Math.pow(2, level);
        tileBounds.put("ullon", ROOT_ULLON + (w * x));
        tileBounds.put("lrlon", tileBounds.get("ullon") + w);
        return tileBounds;
    }

    public HashMap<String, Double> getLatTileBounds(int level, int y) {
        HashMap<String, Double> tileBounds = new HashMap<>();
        double h = (ROOT_ULLAT - ROOT_LRLAT) / Math.pow(2, level);
        tileBounds.put("ullat", ROOT_ULLAT + - (h * y));
        tileBounds.put("lrlat", tileBounds.get("ullat") - h);
        return tileBounds;
    }

    public Map<String, Integer> getIndex(boolean lon, double coord, int level) {
        if (lon) {
            if (coord < ROOT_ULLON) {
                return Map.of("index", 0, "inBounds", -1);
            }
            if (coord > ROOT_LRLON) {
                return Map.of("index", (int) Math.pow(2, level) - 1, "inBounds", 1);
            }
        } else {
            if (coord > ROOT_ULLAT) {
                return Map.of("index", 0, "inBounds", -1);
            }
            if (coord < ROOT_LRLAT) {
                return Map.of("index", (int) Math.pow(2, level) - 1, "inBounds", 1);
            }
        }
        for (int i = 0; i < Math.pow(2, level); i++) {
            if (lon) {
                if (inLonBoundsOfTile(coord, getLonTileBounds(level, i))) {
                    return Map.of("index", i, "inBounds", 0);
                }
            } else {
                if (inLatBoundsOfTile(coord, getLatTileBounds(level, i))) {
                    return Map.of("index", i, "inBounds", 0);
                }
            }
        }
        return null;
    }

    @Override
    protected Object buildJsonResponse(Map<String, Object> result) {
        boolean rasterSuccess = validateRasteredImgParams(result);

        if (rasterSuccess) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            writeImagesToOutputStream(result, os);
            String encodedImage = Base64.getEncoder().encodeToString(os.toByteArray());
            result.put("b64_encoded_image_data", encodedImage);
        }
        return super.buildJsonResponse(result);
    }

    private Map<String, Object> queryFail() {
        Map<String, Object> results = new HashMap<>();
        results.put("render_grid", null);
        results.put("raster_ul_lon", 0);
        results.put("raster_ul_lat", 0);
        results.put("raster_lr_lon", 0);
        results.put("raster_lr_lat", 0);
        results.put("depth", 0);
        results.put("query_success", false);
        return results;
    }

    /**
     * Validates that Rasterer has returned a result that can be rendered.
     * @param rip : Parameters provided by the rasterer
     */
    private boolean validateRasteredImgParams(Map<String, Object> rip) {
        for (String p : REQUIRED_RASTER_RESULT_PARAMS) {
            if (!rip.containsKey(p)) {
                System.out.println("Your rastering result is missing the " + p + " field.");
                return false;
            }
        }
        if (rip.containsKey("query_success")) {
            boolean success = (boolean) rip.get("query_success");
            if (!success) {
                System.out.println("query_success was reported as a failure");
                return false;
            }
        }
        return true;
    }

    /**
     * Writes the images corresponding to rasteredImgParams to the output stream.
     * In Spring 2016, students had to do this on their own, but in 2017,
     * we made this into provided code since it was just a bit too low level.
     */
    private  void writeImagesToOutputStream(Map<String, Object> rasteredImageParams,
                                                  ByteArrayOutputStream os) {
        String[][] renderGrid = (String[][]) rasteredImageParams.get("render_grid");
        int numVertTiles = renderGrid.length;
        int numHorizTiles = renderGrid[0].length;

        BufferedImage img = new BufferedImage(numHorizTiles * Constants.TILE_SIZE,
                numVertTiles * Constants.TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics graphic = img.getGraphics();
        int x = 0, y = 0;

        for (int r = 0; r < numVertTiles; r += 1) {
            for (int c = 0; c < numHorizTiles; c += 1) {
                graphic.drawImage(getImage(Constants.IMG_ROOT + renderGrid[r][c]), x, y, null);
                x += Constants.TILE_SIZE;
                if (x >= img.getWidth()) {
                    x = 0;
                    y += Constants.TILE_SIZE;
                }
            }
        }

        /* If there is a route, draw it. */
        double ullon = (double) rasteredImageParams.get("raster_ul_lon"); //tiles.get(0).ulp;
        double ullat = (double) rasteredImageParams.get("raster_ul_lat"); //tiles.get(0).ulp;
        double lrlon = (double) rasteredImageParams.get("raster_lr_lon"); //tiles.get(0).ulp;
        double lrlat = (double) rasteredImageParams.get("raster_lr_lat"); //tiles.get(0).ulp;

        final double wdpp = (lrlon - ullon) / img.getWidth();
        final double hdpp = (ullat - lrlat) / img.getHeight();
        AugmentedStreetMapGraph graph = SEMANTIC_STREET_GRAPH;
        List<Long> route = ROUTE_LIST;

        if (route != null && !route.isEmpty()) {
            Graphics2D g2d = (Graphics2D) graphic;
            g2d.setColor(Constants.ROUTE_STROKE_COLOR);
            g2d.setStroke(new BasicStroke(Constants.ROUTE_STROKE_WIDTH_PX,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            route.stream().reduce((v, w) -> {
                g2d.drawLine((int) ((graph.lon(v) - ullon) * (1 / wdpp)),
                        (int) ((ullat - graph.lat(v)) * (1 / hdpp)),
                        (int) ((graph.lon(w) - ullon) * (1 / wdpp)),
                        (int) ((ullat - graph.lat(w)) * (1 / hdpp)));
                return w;
            });
        }

        rasteredImageParams.put("raster_width", img.getWidth());
        rasteredImageParams.put("raster_height", img.getHeight());

        try {
            ImageIO.write(img, "png", os);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private BufferedImage getImage(String imgPath) {
        BufferedImage tileImg = null;
        if (tileImg == null) {
            try {
                File in = new File(imgPath);
                tileImg = ImageIO.read(in);
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return tileImg;
    }
}
