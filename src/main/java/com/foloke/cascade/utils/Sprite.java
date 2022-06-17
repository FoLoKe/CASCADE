package com.foloke.cascade.utils;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Sprite {
    Image sheet;
    double sx;
    double sy;
    double sw;
    double sh;
    double posX;
    double posY;
    double scaleX = 1;
    double scaleY = 1;

    private Sprite() {}

    private Sprite(Image sheet, double sx, double sy, double sw, double sh) {
        this.sheet = sheet;
        this.sx = sx;
        this.sy = sy;
        this.sw = sw;
        this.sh = sh;
    }

    public static Sprite create(Image sheet) {
        if (sheet == null) {
            LogUtils.log("no image given");
            return new Sprite();
        }

        return new Sprite(sheet, 0, 0, sheet.getWidth(), sheet.getHeight());
    }

    public static Sprite create(Image sheet, double sx, double sy, double sw, double sh) {
        if (sheet == null) {
            LogUtils.log("no image given");
            return new Sprite();
        }

        if (sx < 0 || sy < 0
                || sw <= 0 || sh <= 0
                || sx + sw > sheet.getWidth() || sy + sh > sheet.getHeight()) {
            LogUtils.log("wrong sprite dimensions");
            return new Sprite(sheet, 0, 0, sheet.getWidth(), sheet.getHeight());
        }

        return new Sprite(sheet, sx, sy, sw, sh);
    }

    public static Sprite create(Image sheet, double sx, double sy, double sw, double sh, double scale) {
        Sprite sprite = create(sheet, sx, sy, sw, sh);
        sprite.scaleX = scale;
        sprite.scaleY = scale;

        return sprite;
    }

    public void setPosition(double x, double y) {
        this.posX = x;
        this.posY = y;
    }

    public void render(GraphicsContext context) {
        if(sheet != null) {
            context.drawImage(sheet, sx, sy, sw, sh, posX, posY, sw * scaleX, sh * scaleY);
        }
    }

    public void render(GraphicsContext context, double x, double y) {
        setPosition(x, y);

        render(context);
    }
}
