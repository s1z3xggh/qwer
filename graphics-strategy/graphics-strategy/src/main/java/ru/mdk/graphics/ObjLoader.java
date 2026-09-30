package ru.mdk.graphics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.TriangleMesh;

/** Minimal OBJ loader: v and triangular f only. Materials and UVs are ignored. */
public final class ObjLoader {
    private ObjLoader() { }
    public static TriangleMesh load(Reader reader) throws IOException {
        List<Float> points = new ArrayList<>();
        List<int[]> triangles = new ArrayList<>();
        try (var input = new BufferedReader(reader)) {
            String line;
            int lineNumber = 0;
            while ((line = input.readLine()) != null) {
                lineNumber++;
                line = line.split("#", 2)[0].trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\s+");
                try {
                    if (p[0].equals("v")) {
                        if (p.length != 4) throw new IllegalArgumentException("нужны три координаты");
                        for (int i = 1; i <= 3; i++) {
                            float value = Float.parseFloat(p[i]);
                            if (!Float.isFinite(value)) throw new IllegalArgumentException("нечисловая координата");
                            points.add(value);
                        }
                    } else if (p[0].equals("f")) {
                        if (p.length != 4) throw new IllegalArgumentException("поддерживаются только треугольники");
                        int[] face = new int[3];
                        for (int i = 0; i < 3; i++) {
                            int raw = Integer.parseInt(p[i + 1].split("/", -1)[0]);
                            int index = raw > 0 ? raw - 1 : points.size() / 3 + raw;
                            if (raw == 0 || index < 0 || index >= points.size() / 3)
                                throw new IllegalArgumentException("неверный индекс вершины");
                            face[i] = index;
                        }
                        triangles.add(face);
                    }
                } catch (IllegalArgumentException ex) {
                    throw new IOException("Строка " + lineNumber + ": " + ex.getMessage(), ex);
                }
            }
        }
        if (points.isEmpty() || triangles.isEmpty()) throw new IOException("В модели нет вершин или треугольников");
        var mesh = new TriangleMesh();
        for (float value : points) mesh.getPoints().addAll(value);
        mesh.getTexCoords().addAll(0f, 0f);
        for (int[] face : triangles) mesh.getFaces().addAll(face[0], 0, face[1], 0, face[2], 0);
        return mesh;
    }
}
