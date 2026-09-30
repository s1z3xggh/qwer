package ru.mdk.graphics;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.mdk.graphics.strategy.*;

public final class GraphicsApp extends Application {
    private RotateTransition animation;

    @Override public void start(Stage stage) throws Exception {
        Tab shapes = new Tab("Фигуры", shapesPane());
        var tabs = new TabPane(shapes, new Tab("Холст 2D", canvasPane()), new Tab("Модель 3D", modelPane()));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != shapes) animation.pause();
        });
        stage.setTitle("Графика — шаблон Стратегия");
        stage.setScene(new Scene(tabs, 820, 600));
        stage.setMinWidth(820); stage.setMinHeight(600);
        stage.show();
    }

    private BorderPane shapesPane() {
        var color = new ColorAction();
        var resize = new ResizeAction();
        var controller = new ShapeController(color);
        var modes = new ComboBox<ShapeAction>();
        modes.getItems().addAll(color, resize);
        modes.setValue(color);
        modes.valueProperty().addListener((obs, old, value) -> { if (value != null) controller.setAction(value); });

        var rectangle = new Rectangle(90, 120, 160, 100);
        var circle = new Circle(405, 170, 65);
        var line = new Line(570, 140, 710, 210);
        line.setStrokeWidth(8);
        List<Shape> shapes = List.of(rectangle, circle, line);
        var surface = new Pane();
        surface.setPrefSize(800, 470);
        surface.setStyle("-fx-background-color: #f3f6fa;");
        var clip = new Rectangle();
        clip.widthProperty().bind(surface.widthProperty());
        clip.heightProperty().bind(surface.heightProperty());
        surface.setClip(clip);
        surface.getChildren().addAll(shapes);
        // One handler on the parent; the selected operation is delegated to the context.
        surface.setOnMouseClicked(event -> {
            if (event.getTarget() instanceof Shape shape && shapes.contains(shape)) controller.apply(shape);
        });
        var reset = new Button("Сброс");
        reset.setOnAction(event -> {
            animation.stop(); rectangle.setRotate(0);
            for (Shape shape : shapes) {
                shape.setScaleX(1); shape.setScaleY(1);
                if (shape instanceof Line) shape.setStroke(Color.CORNFLOWERBLUE);
                else shape.setFill(Color.CORNFLOWERBLUE);
            }
        });
        for (Shape shape : shapes) {
            if (shape instanceof Line) shape.setStroke(Color.CORNFLOWERBLUE);
            else shape.setFill(Color.CORNFLOWERBLUE);
            shape.setCursor(Cursor.HAND);
        }
        animation = new RotateTransition(Duration.seconds(4), rectangle);
        animation.setByAngle(360); animation.setCycleCount(Animation.INDEFINITE);
        var animate = new Button("Старт / пауза вращения");
        animate.setOnAction(event -> {
            if (animation.getStatus() == Animation.Status.RUNNING) animation.pause();
            else animation.play();
        });
        var toolbar = new HBox(12, new Label("Действие по щелчку:"), modes, animate, reset);
        toolbar.setPadding(new Insets(12));
        var pane = new BorderPane(surface);
        pane.setTop(toolbar);
        var hint = new Label("Щелкните по кругу, прямоугольнику или линии. Режим можно менять во время работы.");
        BorderPane.setMargin(hint, new Insets(12)); pane.setBottom(hint);
        return pane;
    }

    private BorderPane canvasPane() {
        var canvas = new Canvas(780, 450);
        Image background = createBackground();
        var color = new ColorPicker(Color.DARKSLATEBLUE);
        var width = new Slider(1, 12, 3);
        Runnable redraw = () -> {
            var g = canvas.getGraphicsContext2D();
            g.clearRect(0, 0, 780, 450);
            g.drawImage(background, 0, 0, 780, 450);
            g.setStroke(color.getValue()); g.setFill(color.getValue()); g.setLineWidth(width.getValue());
            g.strokeLine(60, 70, 700, 70);
            g.strokeOval(100, 140, 180, 180);
            g.strokeRect(390, 140, 240, 150);
            g.fillText("Графика на Canvas", 300, 385);
        };
        color.valueProperty().addListener((obs, old, value) -> redraw.run());
        width.valueProperty().addListener((obs, old, value) -> redraw.run());
        redraw.run(); // Redraw on changes, rather than on every animation pulse.
        var tools = new HBox(12, new Label("Цвет:"), color, new Label("Толщина:"), width);
        tools.setPadding(new Insets(12));
        var pane = new BorderPane(new StackPane(canvas)); pane.setTop(tools);
        return pane;
    }

    private Image createBackground() {
        var image = new WritableImage(78, 45);
        for (int y = 0; y < 45; y++) {
            for (int x = 0; x < 78; x++) {
                image.getPixelWriter().setColor(x, y,
                    (x / 6 + y / 6) % 2 == 0 ? Color.ALICEBLUE : Color.WHITE);
            }
        }
        return image;
    }

    private BorderPane modelPane() throws Exception {
        TriangleMesh mesh;
        try (var reader = new InputStreamReader(getClass().getResourceAsStream("/models/cube.obj"), StandardCharsets.UTF_8)) {
            mesh = ObjLoader.load(reader);
        }
        var view = new MeshView(mesh);
        view.setMaterial(new PhongMaterial(Color.CORNFLOWERBLUE));
        var model = new Group(view);
        var x = new Rotate(20, Rotate.X_AXIS);
        var y = new Rotate(-25, Rotate.Y_AXIS);
        model.getTransforms().addAll(x, y);
        var root = new Group(model, new AmbientLight(Color.WHITE));
        var scene = new SubScene(root, 780, 450, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.web("#edf2f8"));
        var camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-600); camera.setNearClip(0.1); camera.setFarClip(5000);
        scene.setCamera(camera);
        double[] anchor = new double[2];
        scene.setOnMousePressed(event -> {
            anchor[0] = event.getSceneX(); anchor[1] = event.getSceneY(); scene.requestFocus();
        });
        scene.setOnMouseDragged(event -> {
            y.setAngle(y.getAngle() + (event.getSceneX() - anchor[0]) * 0.5);
            x.setAngle(x.getAngle() - (event.getSceneY() - anchor[1]) * 0.5);
            anchor[0] = event.getSceneX(); anchor[1] = event.getSceneY();
        });
        scene.setOnScroll(event -> {
            double scale = Math.max(0.3, Math.min(3, model.getScaleX() * Math.exp(event.getDeltaY() * 0.002)));
            model.setScaleX(scale); model.setScaleY(scale); model.setScaleZ(scale);
        });
        scene.setFocusTraversable(true);
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT -> model.setTranslateX(model.getTranslateX() - 10);
                case RIGHT -> model.setTranslateX(model.getTranslateX() + 10);
                case UP -> model.setTranslateY(model.getTranslateY() - 10);
                case DOWN -> model.setTranslateY(model.getTranslateY() + 10);
                default -> { }
            }
        });
        var reset = new Button("Сброс модели");
        reset.setOnAction(event -> {
            x.setAngle(20); y.setAngle(-25); model.setTranslateX(0); model.setTranslateY(0);
            model.setScaleX(1); model.setScaleY(1); model.setScaleZ(1); scene.requestFocus();
        });
        var tools = new HBox(12, reset, new Label("Мышь — вращение; колесо — масштаб; щелчок и стрелки — перемещение"));
        tools.setPadding(new Insets(12));
        var pane = new BorderPane(new StackPane(scene)); pane.setTop(tools);
        return pane;
    }

    @Override public void stop() { if (animation != null) animation.stop(); }
    public static void main(String[] args) { launch(args); }
}
