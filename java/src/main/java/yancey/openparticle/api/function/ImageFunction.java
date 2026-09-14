package yancey.openparticle.api.function;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageFunction {

    public enum Type {
        CENTER, LEFT, RIGHT, UP, DOWN, LEFT_UP, LEFT_DOWN, RIGHT_UP, RIGHT_DOWN
    }

    private static BufferedImage createTextImage(String str, Font font) {
        int size = font.getSize();
        BufferedImage bufferedImage0 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        FontMetrics metrics = bufferedImage0.getGraphics().getFontMetrics(font);
        int width = metrics.stringWidth(str);
        int height = metrics.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);
        graphics.drawString(str, 0, metrics.getAscent());
        graphics.dispose();
        return bufferedImage;
    }

    private static Font mcFont;

    private static BufferedImage createMCTextImage(String str) {
        File fontFile = new File("D:\\IDEA\\project\\others\\OpenParticleAPI\\resources\\Minecraft.ttf");
        if (mcFont == null) {
            try {
                mcFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            } catch (FontFormatException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        return ImageFunction.createTextImage(str, mcFont.deriveFont(100F));
    }

//    public static Function<Particle, List<Particle>> loadTextImage(String str, Font font, double height, double interval, Type type) {
//        return loadImage(ImageFunction.createTextImage(str, font), height, interval, false, type);
//    }
//
//    public static Function<Particle, List<Particle>> loadMCTextImage(String str, double height, double interval, Type type) {
//        return loadImage(ImageFunction.createMCTextImage(str), height, interval, false, type);
//    }

//    public static Function<Particle, List<Particle>> loadImage(BufferedImage bufferedImage, double height, double interval, boolean isUseImageColor, Type type) {
//        if (interval < 0) {
//            throw new RuntimeException("粒子间隔不可以是负数");
//        }
//        int imgWidth = bufferedImage.getWidth();
//        int imgHeight = bufferedImage.getHeight();
//        double width = height * imgWidth / imgHeight;
//        double halfX = width / 2;
//        double halfY = height / 2;
//        return particle -> {
//            List<Particle> result = new ArrayList<>();
//            for (double x = 0; x <= width; x += interval) {
//                for (double y = 0; y <= height; y += interval) {
//                    int x0 = Math.min(Math.max((int) (x * imgWidth / width), 0), imgWidth - 1);
//                    int y0 = Math.min(Math.max((int) (y * imgHeight / height), 0), imgHeight - 1);
//                    Color color = new Color(bufferedImage.getRGB(x0, y0));
//                    if (color.getRed() < 100 && color.getGreen() < 100 && color.getBlue() < 100) {
//                        Particle par = isUseImageColor ? particle : particle.copy();
//                        result.add(switch (type) {
//                            case CENTER -> par.position(0, halfY - y, x - halfX);
//                            case LEFT -> par.position(0, halfY - y, x - width);
//                            case RIGHT -> par.position(0, halfY - y, x);
//                            case UP -> par.position(0, height - y, x - halfX);
//                            case DOWN -> par.position(0, -y, x - halfX);
//                            case LEFT_UP -> par.position(0, height - y, x - width);
//                            case LEFT_DOWN -> par.position(0, -y, x - width);
//                            case RIGHT_UP -> par.position(0, height - y, x);
//                            case RIGHT_DOWN -> par.position(0, -y, x);
//                        });
//                    }
//                }
//            }
//            return result;
//        };
//    }

}
