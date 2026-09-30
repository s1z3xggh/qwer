package ru.mdk.graphics.strategy;

import javafx.scene.shape.Shape;

/** Strategy: an interchangeable operation on any JavaFX Shape. */
public interface ShapeAction {
    void apply(Shape shape);
}
