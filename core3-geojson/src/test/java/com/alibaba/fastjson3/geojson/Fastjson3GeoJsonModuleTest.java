package com.alibaba.fastjson3.geojson;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonLineString;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Fastjson3GeoJsonModuleTest {
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper = ObjectMapper.builder().build();
        Fastjson3GeoJsonModule.register(mapper);
    }

    @Test
    void pointWriteIsRawCoordArray() {
        String json = mapper.writeValueAsString(new Point(1.0, 2.0));
        assertEquals("[1.0,2.0]", json);
    }

    @Test
    void pointReadFromCoordArray() {
        Point p = mapper.readValue("[3.0,4.0]", Point.class);
        assertEquals(3.0, p.getX(), 0.0001);
        assertEquals(4.0, p.getY(), 0.0001);
    }

    @Test
    void geoJsonPointRoundTrip() {
        GeoJsonPoint src = new GeoJsonPoint(10.5, 20.7);
        String json = mapper.writeValueAsString(src);
        assertTrue(json.contains("\"type\":\"Point\""));
        assertTrue(json.contains("\"coordinates\":[10.5,20.7]"));

        GeoJsonPoint dst = mapper.readValue(json, GeoJsonPoint.class);
        assertEquals(src.getX(), dst.getX(), 0.0001);
        assertEquals(src.getY(), dst.getY(), 0.0001);
    }

    @Test
    void geoJsonLineStringRoundTrip() {
        GeoJsonLineString src = new GeoJsonLineString(Arrays.asList(
                new Point(0.0, 0.0), new Point(1.0, 1.0), new Point(2.0, 0.5)));
        String json = mapper.writeValueAsString(src);
        assertTrue(json.contains("\"type\":\"LineString\""));
        assertTrue(json.contains("\"coordinates\":[[0.0,0.0],[1.0,1.0],[2.0,0.5]]"));

        GeoJsonLineString dst = mapper.readValue(json, GeoJsonLineString.class);
        List<Point> pts = dst.getCoordinates();
        assertEquals(3, pts.size());
        assertEquals(0.0, pts.get(0).getX(), 0.0001);
        assertEquals(2.0, pts.get(2).getX(), 0.0001);
    }

    @Test
    void geoJsonPolygonRoundTrip() {
        // Square polygon (closed ring: 5 points)
        List<Point> ring = Arrays.asList(
                new Point(0.0, 0.0),
                new Point(1.0, 0.0),
                new Point(1.0, 1.0),
                new Point(0.0, 1.0),
                new Point(0.0, 0.0));
        GeoJsonPolygon src = new GeoJsonPolygon(ring);
        String json = mapper.writeValueAsString(src);
        assertTrue(json.contains("\"type\":\"Polygon\""));
        assertTrue(json.contains("\"coordinates\":[[[0.0,0.0],[1.0,0.0],[1.0,1.0],[0.0,1.0],[0.0,0.0]]]"));

        GeoJsonPolygon dst = mapper.readValue(json, GeoJsonPolygon.class);
        assertEquals(1, dst.getCoordinates().size());
        assertEquals(5, dst.getCoordinates().get(0).getCoordinates().size());
    }

    @Test
    void geoJsonPolygonWithInnerRing() {
        // Outer + inner ring (donut)
        List<Point> outer = Arrays.asList(
                new Point(0.0, 0.0), new Point(10.0, 0.0),
                new Point(10.0, 10.0), new Point(0.0, 10.0),
                new Point(0.0, 0.0));
        List<Point> inner = Arrays.asList(
                new Point(2.0, 2.0), new Point(8.0, 2.0),
                new Point(8.0, 8.0), new Point(2.0, 8.0),
                new Point(2.0, 2.0));
        GeoJsonPolygon src = new GeoJsonPolygon(outer).withInnerRing(inner);
        String json = mapper.writeValueAsString(src);

        GeoJsonPolygon dst = mapper.readValue(json, GeoJsonPolygon.class);
        assertEquals(2, dst.getCoordinates().size());
    }

    @Test
    void typeMismatchRejected() {
        // A Point JSON cannot be read as Polygon
        String pointJson = "{\"type\":\"Point\",\"coordinates\":[1,2]}";
        assertThrows(JSONException.class,
                () -> mapper.readValue(pointJson, GeoJsonPolygon.class));
    }

    @Test
    void registerNullMapperRejected() {
        assertThrows(IllegalArgumentException.class, () -> Fastjson3GeoJsonModule.register(null));
    }

    @Test
    void nullInputReturnsNull() {
        assertEquals(null, mapper.readValue("null", GeoJsonPoint.class));
        assertEquals(null, mapper.readValue("null", GeoJsonLineString.class));
        assertEquals(null, mapper.readValue("null", GeoJsonPolygon.class));
    }
}
