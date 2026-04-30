package com.example.dblog.controller;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

final class CaptchaUtil {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int WIDTH = 120;
    private static final int HEIGHT = 44;
    private static final Random RND = new Random();

    private CaptchaUtil() {}

    static String generateCode() {
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(CHARS.charAt(RND.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    static BufferedImage generateImage(String code) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // background with slight noise
        g.setColor(new Color(245, 247, 250));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        for (int i = 0; i < 60; i++) {
            g.setColor(new Color(180 + RND.nextInt(60), 190 + RND.nextInt(50), 200 + RND.nextInt(40)));
            g.fillOval(RND.nextInt(WIDTH), RND.nextInt(HEIGHT), 3, 3);
        }

        // draw each character with random rotation and position
        Font[] fonts = {
            new Font("Arial", Font.BOLD, 26),
            new Font("Arial", Font.ITALIC, 28),
            new Font("Georgia", Font.BOLD, 24)
        };
        for (int i = 0; i < code.length(); i++) {
            g.setFont(fonts[RND.nextInt(fonts.length)]);
            g.setColor(new Color(30 + RND.nextInt(80), 40 + RND.nextInt(60), 80 + RND.nextInt(100)));
            double angle = (RND.nextDouble() - 0.5) * 0.5;
            int x = 15 + i * 24 + RND.nextInt(6);
            int y = 28 + RND.nextInt(10);
            g.rotate(angle, x, y);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.rotate(-angle, x, y);
        }

        // interfering lines
        for (int i = 0; i < 3; i++) {
            g.setColor(new Color(160 + RND.nextInt(40), 170 + RND.nextInt(30), 190 + RND.nextInt(30)));
            g.drawLine(RND.nextInt(WIDTH / 3), RND.nextInt(HEIGHT), WIDTH - RND.nextInt(WIDTH / 3), RND.nextInt(HEIGHT));
        }

        g.dispose();
        return img;
    }
}
