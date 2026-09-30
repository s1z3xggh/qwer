package ru.mdk.graphics.strategy;

import javafx.scene.shape.Shape;

/** Bounded toggle: repeated clicks cannot grow the shape indefinitely. */
public final class ResizeAction implements ShapeAction {
    @Override public void apply(Shape shape) {
        double next = shape.getScaleX() > 1.0 ? 1.0 : 1.4;
        shape.setScaleX(next);
        shape.setScaleY(next);
    }
    @Override public String toString() { return "Изменить размер"; }
}
