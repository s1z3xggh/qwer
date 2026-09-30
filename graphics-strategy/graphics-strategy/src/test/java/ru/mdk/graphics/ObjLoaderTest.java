package ru.mdk.graphics;

import java.io.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ObjLoaderTest {
    private static final String VERTICES = "v 0 0 0\nv 1 0 0\nv 0 1 0\n";
    @Test void triangleAndNegativeIndicesAreLoaded() throws IOException {
        var mesh = ObjLoader.load(new StringReader(VERTICES + "f -3/1 -2/1 -1/1\n"));
        assertEquals(9, mesh.getPoints().size());
        assertArrayEquals(new int[]{0, 0, 1, 0, 2, 0}, mesh.getFaces().toArray(null));
    }
    @Test void packagedCubeHasTwelveTriangles() throws IOException {
        var reader = new InputStreamReader(getClass().getResourceAsStream("/models/cube.obj"));
        var mesh = ObjLoader.load(reader);
        assertEquals(24, mesh.getPoints().size());
        assertEquals(72, mesh.getFaces().size());
    }
    @Test void invalidIndexIsRejected() {
        assertThrows(IOException.class, () -> ObjLoader.load(new StringReader(VERTICES + "f 1 2 7")));
    }
    @Test void emptyFileIsRejected() {
        assertThrows(IOException.class, () -> ObjLoader.load(new StringReader("# empty")));
    }
    @Test void nonFiniteCoordinateIsRejected() {
        assertThrows(IOException.class, () -> ObjLoader.load(new StringReader("v NaN 0 0\nf 1 1 1")));
    }
}
