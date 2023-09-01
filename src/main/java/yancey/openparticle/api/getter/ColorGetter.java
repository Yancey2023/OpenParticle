package yancey.openparticle.api.getter;

import yancey.openparticle.api.math.Vec3;

import java.awt.*;

public abstract class ColorGetter {

    public static ColorGetter color(Color color) {
        return new ColorGetter() {
            @Override
            public Color get(double tick, int maxAge, Vec3 vec3) {
                return color;
            }
        };
    }

    public static ColorGetter transform(Color colorStart, Color colorEnd) {
        return new ColorGetter() {
            @Override
            public Color get(double tick, int maxAge, Vec3 vec3) {
                double a = (double) tick / maxAge;
                int redStart = colorStart.getRed();
                int greenStart = colorStart.getGreen();
                int blueStart = colorStart.getBlue();
                int alphaStart = colorStart.getAlpha();
                int redEnd = colorEnd.getRed();
                int greenEnd = colorEnd.getGreen();
                int blueEnd = colorEnd.getBlue();
                int alphaEnd = colorEnd.getAlpha();
                return new Color(
                        (int) (redStart + a * (redEnd - redStart)),
                        (int) (greenStart + a * (greenEnd - greenStart)),
                        (int) (blueStart + a * (blueEnd - blueStart)),
                        (int) (alphaStart + a * (alphaEnd - alphaStart))
                );
            }
        };
    }

    public static ColorGetter transform(Color[] colors) {
        return new ColorGetter() {
            @Override
            public Color get(double tick, int maxAge, Vec3 vec3) {
                double which0 = tick * (colors.length - 1) / maxAge;
                int whichStart = (int) which0;
                if(whichStart == which0){
                    return colors[whichStart];
                }
                double a = which0 - whichStart;
                Color colorStart = colors[whichStart];
                Color colorEnd = colors[whichStart + 1];
                int redStart = colorStart.getRed();
                int greenStart = colorStart.getGreen();
                int blueStart = colorStart.getBlue();
                int alphaStart = colorStart.getAlpha();
                int redEnd = colorEnd.getRed();
                int greenEnd = colorEnd.getGreen();
                int blueEnd = colorEnd.getBlue();
                int alphaEnd = colorEnd.getAlpha();
                return new Color(
                        (int) (redStart + a * (redEnd - redStart)),
                        (int) (greenStart + a * (greenEnd - greenStart)),
                        (int) (blueStart + a * (blueEnd - blueStart)),
                        (int) (alphaStart + a * (alphaEnd - alphaStart))
                );
            }
        };
    }

    public abstract Color get(double tick, int maxAge, Vec3 vec3);

}
