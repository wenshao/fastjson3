package com.alibaba.fastjson3.geojson;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.JSONGenerator;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.JSONParser;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.ObjectReader;
import com.alibaba.fastjson3.ObjectWriter;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonLineString;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * fastjson3 module for Spring Data MongoDB GeoJSON geometry types
 * ({@code org.springframework.data.mongodb.core.geo.GeoJson*}). Register
 * the readers/writers on an {@link ObjectMapper} via {@link #register}:
 *
 * <pre>{@code
 *   ObjectMapper mapper = ObjectMapper.builder().build();
 *   Fastjson3GeoJsonModule.register(mapper);
 * }</pre>
 *
 * <p>Supported types in this initial release:
 * <ul>
 *   <li>{@link Point} — base 2D coordinate as {@code [lon, lat]}</li>
 *   <li>{@link GeoJsonPoint} — {@code {"type":"Point","coordinates":[lon,lat]}}</li>
 *   <li>{@link GeoJsonLineString} —
 *       {@code {"type":"LineString","coordinates":[[lon,lat],...]}}</li>
 *   <li>{@link GeoJsonPolygon} —
 *       {@code {"type":"Polygon","coordinates":[[[lon,lat],...],...]}}</li>
 * </ul>
 *
 * <p>Multi* / GeometryCollection types are out of scope for this release
 * — track separately if needed.
 */
public final class Fastjson3GeoJsonModule {
    private Fastjson3GeoJsonModule() {
    }

    /**
     * Register all GeoJSON readers and writers on the given mapper.
     */
    public static void register(ObjectMapper mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        mapper.registerWriter(Point.class, PointWriter.INSTANCE);
        mapper.registerWriter(GeoJsonPoint.class, GeoJsonPointWriter.INSTANCE);
        mapper.registerWriter(GeoJsonLineString.class, GeoJsonLineStringWriter.INSTANCE);
        mapper.registerWriter(GeoJsonPolygon.class, GeoJsonPolygonWriter.INSTANCE);
        mapper.registerReader(Point.class, PointReader.INSTANCE);
        mapper.registerReader(GeoJsonPoint.class, GeoJsonPointReader.INSTANCE);
        mapper.registerReader(GeoJsonLineString.class, GeoJsonLineStringReader.INSTANCE);
        mapper.registerReader(GeoJsonPolygon.class, GeoJsonPolygonReader.INSTANCE);
    }

    private static void writeCoord(JSONGenerator gen, Point pt) {
        gen.writeDoubleArray(new double[]{pt.getX(), pt.getY()});
    }

    private static void writePointArray(JSONGenerator gen, List<Point> points) {
        gen.startArray();
        for (int i = 0, len = points.size(); i < len; i++) {
            gen.beforeArrayValue();
            writeCoord(gen, points.get(i));
        }
        gen.endArray();
    }

    // ============================================================
    // Writers
    // ============================================================

    static final class PointWriter implements ObjectWriter<Point> {
        static final PointWriter INSTANCE = new PointWriter();

        @Override
        public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                gen.writeNull();
                return;
            }
            writeCoord(gen, (Point) object);
        }
    }

    static final class GeoJsonPointWriter implements ObjectWriter<GeoJsonPoint> {
        static final GeoJsonPointWriter INSTANCE = new GeoJsonPointWriter();

        @Override
        public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                gen.writeNull();
                return;
            }
            GeoJsonPoint p = (GeoJsonPoint) object;
            gen.startObject();
            gen.writeNameValue("type", "Point");
            gen.writeName("coordinates");
            writeCoord(gen, p);
            gen.endObject();
        }
    }

    static final class GeoJsonLineStringWriter implements ObjectWriter<GeoJsonLineString> {
        static final GeoJsonLineStringWriter INSTANCE = new GeoJsonLineStringWriter();

        @Override
        public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                gen.writeNull();
                return;
            }
            GeoJsonLineString line = (GeoJsonLineString) object;
            gen.startObject();
            gen.writeNameValue("type", "LineString");
            gen.writeName("coordinates");
            writePointArray(gen, line.getCoordinates());
            gen.endObject();
        }
    }

    static final class GeoJsonPolygonWriter implements ObjectWriter<GeoJsonPolygon> {
        static final GeoJsonPolygonWriter INSTANCE = new GeoJsonPolygonWriter();

        @Override
        public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                gen.writeNull();
                return;
            }
            GeoJsonPolygon polygon = (GeoJsonPolygon) object;
            gen.startObject();
            gen.writeNameValue("type", "Polygon");
            gen.writeName("coordinates");
            // Polygon coordinates are an array of LineStrings (rings)
            List<GeoJsonLineString> rings = polygon.getCoordinates();
            gen.startArray();
            for (int i = 0, len = rings.size(); i < len; i++) {
                gen.beforeArrayValue();
                writePointArray(gen, rings.get(i).getCoordinates());
            }
            gen.endArray();
            gen.endObject();
        }
    }

    // ============================================================
    // Readers
    // ============================================================
    //
    // Read JSON as JSONObject / JSONArray (untyped trees) and walk the
    // structure manually. fj3 JSONObject.getObject only accepts Class<T>,
    // so generic-typed nested arrays must be unpacked through JSONArray.

    static final class PointReader implements ObjectReader<Point> {
        static final PointReader INSTANCE = new PointReader();

        @Override
        public Point readObject(JSONParser parser, Type fieldType, Object fieldName, long features) {
            // Accept [lon, lat] (raw coord array) — the embedded form used inside
            // GeoJson types.
            com.alibaba.fastjson3.JSONArray arr = parser.read(com.alibaba.fastjson3.JSONArray.class);
            if (arr == null) {
                return null;
            }
            if (arr.size() < 2) {
                throw new JSONException("Point requires 2 coordinates, got " + arr.size());
            }
            return new Point(arr.getDouble(0), arr.getDouble(1));
        }
    }

    static final class GeoJsonPointReader implements ObjectReader<GeoJsonPoint> {
        static final GeoJsonPointReader INSTANCE = new GeoJsonPointReader();

        @Override
        public GeoJsonPoint readObject(JSONParser parser, Type fieldType, Object fieldName, long features) {
            JSONObject obj = parser.read(JSONObject.class);
            if (obj == null) {
                return null;
            }
            requireType(obj, "Point");
            com.alibaba.fastjson3.JSONArray c = obj.getJSONArray("coordinates");
            if (c == null || c.size() < 2) {
                throw new JSONException("GeoJsonPoint requires 2 coordinates");
            }
            return new GeoJsonPoint(c.getDouble(0), c.getDouble(1));
        }
    }

    static final class GeoJsonLineStringReader implements ObjectReader<GeoJsonLineString> {
        static final GeoJsonLineStringReader INSTANCE = new GeoJsonLineStringReader();

        @Override
        public GeoJsonLineString readObject(JSONParser parser, Type fieldType, Object fieldName, long features) {
            JSONObject obj = parser.read(JSONObject.class);
            if (obj == null) {
                return null;
            }
            requireType(obj, "LineString");
            com.alibaba.fastjson3.JSONArray raw = obj.getJSONArray("coordinates");
            if (raw == null) {
                throw new JSONException("GeoJsonLineString missing coordinates");
            }
            return new GeoJsonLineString(toPoints(raw));
        }
    }

    static final class GeoJsonPolygonReader implements ObjectReader<GeoJsonPolygon> {
        static final GeoJsonPolygonReader INSTANCE = new GeoJsonPolygonReader();

        @Override
        public GeoJsonPolygon readObject(JSONParser parser, Type fieldType, Object fieldName, long features) {
            JSONObject obj = parser.read(JSONObject.class);
            if (obj == null) {
                return null;
            }
            requireType(obj, "Polygon");
            com.alibaba.fastjson3.JSONArray raw = obj.getJSONArray("coordinates");
            if (raw == null || raw.isEmpty()) {
                throw new JSONException("GeoJsonPolygon missing coordinates");
            }
            // First ring is the outer; the rest are inner rings (holes).
            List<Point> outer = toPoints(raw.getJSONArray(0));
            GeoJsonPolygon polygon = new GeoJsonPolygon(outer);
            for (int i = 1; i < raw.size(); i++) {
                polygon = polygon.withInnerRing(toPoints(raw.getJSONArray(i)));
            }
            return polygon;
        }
    }

    private static List<Point> toPoints(com.alibaba.fastjson3.JSONArray raw) {
        List<Point> points = new ArrayList<>(raw.size());
        for (int i = 0, len = raw.size(); i < len; i++) {
            com.alibaba.fastjson3.JSONArray c = raw.getJSONArray(i);
            if (c == null || c.size() < 2) {
                throw new JSONException("Point requires 2 coordinates");
            }
            points.add(new Point(c.getDouble(0), c.getDouble(1)));
        }
        return points;
    }

    private static void requireType(JSONObject obj, String expected) {
        Object actual = obj.get("type");
        if (!expected.equals(actual)) {
            throw new JSONException("Expected GeoJSON type '" + expected + "', got '" + actual + "'");
        }
    }
}
