import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Draws the replaced bitmaps and icons for Space Shooter from scratch, as 8-bit BMP
 * files (and 4-bit icons) that VC0608.rc compiles into the program.
 *
 * Run from the repository root (JDK 11 or newer, works headless):
 *
 *     java tools/DrawArt.java
 *
 * It writes bitmap_s.bmp (start screen), bmp00001.bmp (player ship), bmp00002.bmp
 * (straight shot), bmp00004.bmp (bouncing shot), bitmap2/bmp00005/bmp00006.bmp
 * (enemy A, full health then damaged), bmp00003/bmp00007/bmp00008.bmp (enemy B, the
 * same three states), bmp00009.bmp (the boss), VC0608.ico and small.ico. Every file
 * keeps the name, pixel size and bit depth the game already used. bitmap1.bmp (stars),
 * bitmap_i.bmp (item B), bitmap_m.bmp (menu panel) and bitmap_o.bmp (GAME OVER) are the
 * author's own drawings and are not touched.
 *
 * Everything is drawn without anti-aliasing so each image stays within a 256-colour palette.
 */
public class DrawArt {

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        Path out = Paths.get(args.length > 0 ? args[0] : ".");

        writeBmp8(drawShip(), out.resolve("bmp00001.bmp"));
        writeBmp8(drawShot(), out.resolve("bmp00002.bmp"));
        writeBmp8(drawOrb(), out.resolve("bmp00004.bmp"));
        writeBmp8(drawBlob(0), out.resolve("bitmap2.bmp"));
        writeBmp8(drawBlob(1), out.resolve("bmp00005.bmp"));
        writeBmp8(drawBlob(2), out.resolve("bmp00006.bmp"));
        writeBmp8(drawSaucer(0), out.resolve("bmp00003.bmp"));
        writeBmp8(drawSaucer(1), out.resolve("bmp00007.bmp"));
        writeBmp8(drawSaucer(2), out.resolve("bmp00008.bmp"));
        writeBmp8(drawBoss(), out.resolve("bmp00009.bmp"));
        writeBmp8(drawStart(), out.resolve("bitmap_s.bmp"));

        List<BufferedImage> big = new ArrayList<>();
        big.add(drawIcon(32));
        big.add(drawIcon(16));
        writeIco(big, out.resolve("VC0608.ico"));
        List<BufferedImage> small = new ArrayList<>();
        small.add(drawIcon(16));
        writeIco(small, out.resolve("small.ico"));

        System.out.println("Wrote 11 bitmaps and 2 icons to " + out.toAbsolutePath().normalize());
    }

    // ------------------------------------------------------------------ sprites
    // The game blits sprites with a plain copy, so each one sits on a black background.

    static final Color BLACK = new Color(0, 0, 0);
    static final Color ORANGE = new Color(255, 140, 40);
    static final Color FLAME = new Color(255, 200, 60);
    static final Color FLAME_CORE = new Color(255, 90, 30);

    /** Player ship, 35 x 30: a teal hull with a round cockpit between two engine pods. */
    static BufferedImage drawShip() {
        BufferedImage img = sprite(35, 30);
        Graphics2D g = img.createGraphics();
        // flames
        g.setColor(FLAME);
        g.fillPolygon(new int[] { 3, 8, 5 }, new int[] { 24, 24, 29 }, 3);
        g.fillPolygon(new int[] { 27, 32, 29 }, new int[] { 24, 24, 29 }, 3);
        g.fillPolygon(new int[] { 15, 20, 17 }, new int[] { 25, 25, 29 }, 3);
        g.setColor(FLAME_CORE);
        g.fillRect(5, 24, 1, 3);
        g.fillRect(29, 24, 1, 3);
        g.fillRect(17, 25, 1, 2);
        // wing bar
        g.setColor(new Color(50, 110, 140));
        g.fillRect(6, 14, 23, 5);
        g.setColor(new Color(110, 190, 210));
        g.fillRect(6, 14, 23, 1);
        // pods
        g.setColor(new Color(70, 150, 170));
        g.fill(new RoundRectangle2D.Double(2, 9, 7, 15, 6, 6));
        g.fill(new RoundRectangle2D.Double(26, 9, 7, 15, 6, 6));
        g.setColor(ORANGE);
        g.fillRect(4, 9, 3, 2);
        g.fillRect(28, 9, 3, 2);
        g.setColor(new Color(30, 80, 100));
        g.fillRect(3, 21, 5, 2);
        g.fillRect(27, 21, 5, 2);
        // hull
        g.setColor(new Color(120, 215, 225));
        g.fillPolygon(new int[] { 17, 20, 22, 22, 19, 15, 12, 12, 14 }, new int[] { 0, 6, 12, 22, 25, 25, 22, 12, 6 }, 9);
        g.setColor(new Color(80, 170, 190));
        g.fillPolygon(new int[] { 18, 20, 22, 22, 19, 18 }, new int[] { 2, 6, 12, 22, 25, 25 }, 6);
        // cockpit
        g.setColor(new Color(255, 200, 70));
        g.fill(new Ellipse2D.Double(14, 7, 7, 8));
        g.setColor(Color.WHITE);
        g.fillRect(15, 9, 2, 2);
        // stripe
        g.setColor(ORANGE);
        g.fillRect(16, 17, 3, 5);
        g.dispose();
        return img;
    }

    /** Straight shot, 10 x 20: a cyan bolt with a white core. */
    static BufferedImage drawShot() {
        BufferedImage img = sprite(10, 20);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(40, 120, 200));
        g.fill(new RoundRectangle2D.Double(2, 1, 6, 18, 6, 6));
        g.setColor(new Color(80, 220, 255));
        g.fill(new RoundRectangle2D.Double(3, 2, 4, 16, 4, 4));
        g.setColor(Color.WHITE);
        g.fillRect(4, 4, 2, 11);
        g.dispose();
        return img;
    }

    /** Bouncing shot, 20 x 20: a four-pointed spark. */
    static BufferedImage drawOrb() {
        BufferedImage img = sprite(20, 20);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(120, 200, 40));
        g.fillPolygon(new int[] { 10, 13, 19, 13, 10, 7, 1, 7 }, new int[] { 1, 7, 10, 13, 19, 13, 10, 7 }, 8);
        g.setColor(new Color(220, 255, 90));
        g.fillPolygon(new int[] { 10, 12, 16, 12, 10, 8, 4, 8 }, new int[] { 4, 8, 10, 12, 16, 12, 10, 8 }, 8);
        g.setColor(Color.WHITE);
        g.fillRect(9, 9, 3, 3);
        g.dispose();
        return img;
    }

    /**
     * Enemy A, 20 x 20: a one-eyed slime with three feet. state 0 is full health,
     * 1 and 2 are the damaged frames the game shows as it takes hits.
     */
    static BufferedImage drawBlob(int state) {
        Color[] body = { new Color(90, 200, 90), new Color(220, 200, 60), new Color(235, 105, 50) };
        Color[] shade = { new Color(40, 130, 60), new Color(160, 130, 30), new Color(160, 50, 30) };
        BufferedImage img = sprite(20, 20);
        Graphics2D g = img.createGraphics();
        g.setColor(shade[state]);
        g.fill(new RoundRectangle2D.Double(3, 14, 4, 5, 3, 3));
        g.fill(new RoundRectangle2D.Double(8, 15, 4, 5, 3, 3));
        g.fill(new RoundRectangle2D.Double(13, 14, 4, 5, 3, 3));
        g.fill(new Ellipse2D.Double(1, 3, 18, 14));
        g.setColor(body[state]);
        g.fill(new Ellipse2D.Double(1, 2, 18, 13));
        g.setColor(new Color(255, 255, 255));
        if (state < 2) {
            g.fill(new Ellipse2D.Double(6, 4, 8, 8));
            g.setColor(new Color(20, 20, 50));
            g.fill(new Ellipse2D.Double(9, 6, 3, 4));
        } else {
            g.fillRect(6, 7, 8, 3); // half-closed eye
            g.setColor(new Color(20, 20, 50));
            g.fillRect(9, 8, 3, 2);
        }
        g.setColor(new Color(255, 255, 210));
        g.fillRect(3, 6, 1, 2);
        g.fillRect(15, 5, 2, 1);
        if (state >= 1) {
            g.setColor(new Color(60, 30, 20));
            g.drawLine(3, 10, 5, 12);
            g.drawLine(5, 12, 4, 14);
        }
        if (state == 2) {
            g.drawLine(15, 9, 13, 12);
            g.drawLine(13, 12, 15, 14);
        }
        g.dispose();
        return img;
    }

    /** Enemy B, 20 x 20: a small saucer with a glass dome and three lights; states as above. */
    static BufferedImage drawSaucer(int state) {
        Color[] hull = { new Color(150, 90, 220), new Color(220, 80, 180), new Color(170, 70, 50) };
        Color[] rim = { new Color(90, 50, 150), new Color(150, 40, 120), new Color(100, 40, 30) };
        BufferedImage img = sprite(20, 20);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(160, 160, 170));
        g.drawLine(5, 13, 3, 18);
        g.drawLine(14, 13, 16, 18);
        g.drawLine(2, 18, 4, 18);
        g.drawLine(15, 18, 17, 18);
        g.setColor(new Color(150, 230, 255));
        g.fill(new Ellipse2D.Double(6, 2, 8, 8));
        g.setColor(Color.WHITE);
        g.fillRect(8, 3, 2, 2);
        g.setColor(rim[state]);
        g.fill(new Ellipse2D.Double(0, 8, 20, 7));
        g.setColor(hull[state]);
        g.fill(new Ellipse2D.Double(1, 7, 18, 6));
        Color on = new Color(255, 230, 80);
        Color off = new Color(80, 70, 60);
        g.setColor(on);
        g.fillRect(4, 10, 2, 2);
        g.setColor(state >= 1 ? off : on);
        g.fillRect(9, 11, 2, 2);
        g.setColor(state >= 2 ? off : on);
        g.fillRect(14, 10, 2, 2);
        if (state == 2) {
            g.setColor(new Color(120, 120, 120));
            g.fillRect(15, 3, 2, 2);
            g.fillRect(17, 1, 2, 2);
            g.setColor(new Color(40, 60, 80));
            g.drawLine(9, 4, 11, 7);
        }
        g.dispose();
        return img;
    }

    /** The boss, 30 x 30: a spiked red core with one big eye. */
    static BufferedImage drawBoss() {
        BufferedImage img = sprite(30, 30);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(255, 150, 60));
        int cx = 15, cy = 15;
        for (int k = 0; k < 8; k++) {
            double a = Math.PI / 4 * k;
            double b1 = a - 0.32, b2 = a + 0.32;
            g.fillPolygon(
                new int[] { (int) Math.round(cx + 14.5 * Math.cos(a)), (int) Math.round(cx + 9 * Math.cos(b1)), (int) Math.round(cx + 9 * Math.cos(b2)) },
                new int[] { (int) Math.round(cy + 14.5 * Math.sin(a)), (int) Math.round(cy + 9 * Math.sin(b1)), (int) Math.round(cy + 9 * Math.sin(b2)) },
                3);
        }
        g.setColor(new Color(150, 30, 30));
        g.fill(new Ellipse2D.Double(5, 5, 20, 20));
        g.setColor(new Color(220, 60, 40));
        g.fill(new Ellipse2D.Double(5, 5, 19, 19));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(9, 9, 12, 11));
        g.setColor(new Color(255, 200, 0));
        g.fill(new Ellipse2D.Double(12, 11, 7, 7));
        g.setColor(BLACK);
        g.fill(new Ellipse2D.Double(14, 13, 3, 3));
        g.setColor(Color.WHITE);
        g.fillRect(13, 12, 1, 1);
        g.dispose();
        return img;
    }

    // ------------------------------------------------------------------ start screen

    /**
     * The start screen, 601 x 450. The game stretches its left 600 x 450 pixels over the
     * 1100 x 550 window, so the layout is done in window coordinates and scaled down.
     */
    static BufferedImage drawStart() {
        int w = 601, h = 450;
        double sx = 600 / 1100.0, sy = 450 / 550.0;
        BufferedImage img = sprite(w, h);
        Graphics2D g = img.createGraphics();

        // stars, one pixel each, placed in bitmap pixels
        Random rnd = new Random(2012);
        Color[] star = { new Color(200, 200, 200), new Color(120, 120, 160), new Color(255, 230, 150), new Color(150, 200, 255), new Color(90, 90, 90) };
        for (int k = 0; k < 260; k++) {
            g.setColor(star[rnd.nextInt(star.length)]);
            g.fillRect(rnd.nextInt(w), rnd.nextInt(h), 1, 1);
        }

        g.scale(sx, sy);
        double tpx = 10;
        String title = "SPACE SHOOTER";
        double tw = PixelFont.width(title, tpx);
        PixelFont.draw(g, title, (1100 - tw) / 2 + 6, 96 + 6, tpx, new Color(190, 50, 40));
        PixelFont.draw(g, title, (1100 - tw) / 2, 96, tpx, new Color(255, 200, 60));

        // a few of the game's own sprites as decoration
        Graphics2D gd = (Graphics2D) g.create();
        gd.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        gd.drawImage(drawShip(), 150, 300, 140, 120, null);
        gd.drawImage(drawSaucer(0), 840, 270, 60, 60, null);
        gd.drawImage(drawBlob(0), 920, 330, 60, 60, null);
        gd.drawImage(drawBlob(0), 820, 380, 60, 60, null);
        gd.dispose();
        g.setColor(new Color(80, 220, 255));
        g.fill(new Rectangle2D.Double(217, 250, 6, 26));
        g.fill(new Rectangle2D.Double(217, 205, 6, 26));

        double mpx = 5;
        PixelFont.draw(g, "1 PLAYER", 430, 280, mpx, Color.WHITE);
        PixelFont.draw(g, "2 PLAYERS", 430, 340, mpx, Color.WHITE);
        double hpx = 3;
        String hint = "PRESS 1 OR 2";
        PixelFont.draw(g, hint, (1100 - PixelFont.width(hint, hpx)) / 2, 420, hpx, new Color(120, 230, 255));

        double bpx = 4;
        String maker = "BNI Tech";
        PixelFont.draw(g, maker, (1100 - PixelFont.width(maker, bpx)) / 2, 470, bpx, new Color(235, 110, 60));
        double ypx = 2;
        PixelFont.draw(g, "2012", (1100 - PixelFont.width("2012", ypx)) / 2, 515, ypx, new Color(140, 140, 150));
        g.dispose();
        return img;
    }

    // ------------------------------------------------------------------ icon

    /** Application icon: the ship on a transparent background. */
    static BufferedImage drawIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        double u = size / 16.0;
        g.scale(u, u);
        g.setColor(new Color(255, 255, 0));
        g.fill(new Rectangle2D.Double(1.5, 12, 2, 3));
        g.fill(new Rectangle2D.Double(12.5, 12, 2, 3));
        g.setColor(new Color(0, 128, 128));
        g.fill(new Rectangle2D.Double(1, 5, 3, 7));
        g.fill(new Rectangle2D.Double(12, 5, 3, 7));
        g.fill(new Rectangle2D.Double(3, 8, 10, 2));
        g.setColor(new Color(0, 255, 255));
        g.fill(new Polygon(new int[] { 8, 10, 10, 9, 7, 6, 6 }, new int[] { 0, 4, 12, 14, 14, 12, 4 }, 7));
        g.setColor(new Color(255, 255, 0));
        g.fill(new Rectangle2D.Double(7, 5, 2, 3));
        g.dispose();
        return img;
    }

    // ------------------------------------------------------------------ file writers

    static BufferedImage sprite(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(BLACK);
        g.fillRect(0, 0, w, h);
        g.dispose();
        return img;
    }

    /** Writes an uncompressed 8-bit BMP (BITMAPINFOHEADER, 256-entry palette, bottom-up rows). */
    static void writeBmp8(BufferedImage img, Path file) throws IOException {
        int w = img.getWidth(), h = img.getHeight();
        Map<Integer, Integer> index = new LinkedHashMap<>();
        index.put(0x000000, 0);
        int[] px = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y) & 0xFFFFFF;
                Integer i = index.get(rgb);
                if (i == null) {
                    i = index.size();
                    if (i > 255) {
                        throw new IllegalStateException(file.getFileName() + " needs more than 256 colours");
                    }
                    index.put(rgb, i);
                }
                px[y * w + x] = i;
            }
        }
        int row = (w + 3) & ~3;
        int imageSize = row * h;
        int offset = 14 + 40 + 256 * 4;
        ByteBuffer b = ByteBuffer.allocate(offset + imageSize).order(ByteOrder.LITTLE_ENDIAN);
        b.put((byte) 'B').put((byte) 'M').putInt(offset + imageSize).putShort((short) 0).putShort((short) 0).putInt(offset);
        b.putInt(40).putInt(w).putInt(h).putShort((short) 1).putShort((short) 8).putInt(0).putInt(imageSize)
            .putInt(0).putInt(0).putInt(0).putInt(0);
        int[] palette = new int[256];
        for (Map.Entry<Integer, Integer> e : index.entrySet()) {
            palette[e.getValue()] = e.getKey();
        }
        for (int c : palette) {
            b.put((byte) c).put((byte) (c >> 8)).put((byte) (c >> 16)).put((byte) 0);
        }
        for (int y = h - 1; y >= 0; y--) {
            for (int x = 0; x < w; x++) {
                b.put((byte) px[y * w + x]);
            }
            for (int p = w; p < row; p++) {
                b.put((byte) 0);
            }
        }
        Files.write(file, b.array());
    }

    /** The standard 16-colour Windows palette, in the order the original icons used. */
    static final int[] VGA16 = {
        0x000000, 0x800000, 0x008000, 0x808000, 0x000080, 0x800080, 0x008080, 0x808080,
        0xC0C0C0, 0xFF0000, 0x00FF00, 0xFFFF00, 0x0000FF, 0xFF00FF, 0x00FFFF, 0xFFFFFF
    };

    static int nearestVga(int rgb) {
        int best = 0;
        long bestD = Long.MAX_VALUE;
        for (int i = 0; i < VGA16.length; i++) {
            int dr = ((rgb >> 16) & 255) - ((VGA16[i] >> 16) & 255);
            int dg = ((rgb >> 8) & 255) - ((VGA16[i] >> 8) & 255);
            int db = (rgb & 255) - (VGA16[i] & 255);
            long d = (long) dr * dr + (long) dg * dg + (long) db * db;
            if (d < bestD) {
                bestD = d;
                best = i;
            }
        }
        return best;
    }

    /** Writes a .ico holding 4-bit (16-colour) images with an AND transparency mask. */
    static void writeIco(List<BufferedImage> images, Path file) throws IOException {
        List<byte[]> blobs = new ArrayList<>();
        for (BufferedImage img : images) {
            int w = img.getWidth(), h = img.getHeight();
            int xorRow = ((w * 4 + 31) / 32) * 4;
            int andRow = ((w + 31) / 32) * 4;
            ByteBuffer b = ByteBuffer.allocate(40 + 64 + xorRow * h + andRow * h).order(ByteOrder.LITTLE_ENDIAN);
            b.putInt(40).putInt(w).putInt(h * 2).putShort((short) 1).putShort((short) 4).putInt(0)
                .putInt(xorRow * h + andRow * h).putInt(0).putInt(0).putInt(0).putInt(0);
            for (int c : VGA16) {
                b.put((byte) c).put((byte) (c >> 8)).put((byte) (c >> 16)).put((byte) 0);
            }
            for (int y = h - 1; y >= 0; y--) {
                byte[] rowBytes = new byte[xorRow];
                for (int x = 0; x < w; x++) {
                    int argb = img.getRGB(x, y);
                    int v = (argb >>> 24) < 128 ? 0 : nearestVga(argb & 0xFFFFFF);
                    rowBytes[x / 2] |= (byte) (x % 2 == 0 ? v << 4 : v);
                }
                b.put(rowBytes);
            }
            for (int y = h - 1; y >= 0; y--) {
                byte[] rowBytes = new byte[andRow];
                for (int x = 0; x < w; x++) {
                    if ((img.getRGB(x, y) >>> 24) < 128) {
                        rowBytes[x / 8] |= (byte) (0x80 >> (x % 8));
                    }
                }
                b.put(rowBytes);
            }
            blobs.add(b.array());
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteBuffer dir = ByteBuffer.allocate(6 + 16 * images.size()).order(ByteOrder.LITTLE_ENDIAN);
        dir.putShort((short) 0).putShort((short) 1).putShort((short) images.size());
        int offset = 6 + 16 * images.size();
        for (int i = 0; i < images.size(); i++) {
            BufferedImage img = images.get(i);
            dir.put((byte) img.getWidth()).put((byte) img.getHeight()).put((byte) 16).put((byte) 0)
                .putShort((short) 0).putShort((short) 0).putInt(blobs.get(i).length).putInt(offset);
            offset += blobs.get(i).length;
        }
        out.write(dir.array());
        for (byte[] blob : blobs) {
            out.write(blob);
        }
        Files.write(file, out.toByteArray());
    }

    // ------------------------------------------------------------------ pixel font

    /** A 5 x 7 pixel font drawn as filled squares. */
    static final class PixelFont {
        static final Map<Character, String[]> GLYPHS = new HashMap<>();

        static void put(char c, String... rows) {
            GLYPHS.put(c, rows);
        }

        static {
            put('A', ".###.", "#...#", "#...#", "#####", "#...#", "#...#", "#...#");
            put('B', "####.", "#...#", "#...#", "####.", "#...#", "#...#", "####.");
            put('C', ".###.", "#...#", "#....", "#....", "#....", "#...#", ".###.");
            put('E', "#####", "#....", "#....", "####.", "#....", "#....", "#####");
            put('H', "#...#", "#...#", "#...#", "#####", "#...#", "#...#", "#...#");
            put('I', ".###.", "..#..", "..#..", "..#..", "..#..", "..#..", ".###.");
            put('L', "#....", "#....", "#....", "#....", "#....", "#....", "#####");
            put('N', "#...#", "#...#", "##..#", "#.#.#", "#..##", "#...#", "#...#");
            put('O', ".###.", "#...#", "#...#", "#...#", "#...#", "#...#", ".###.");
            put('P', "####.", "#...#", "#...#", "####.", "#....", "#....", "#....");
            put('R', "####.", "#...#", "#...#", "####.", "#.#..", "#..#.", "#...#");
            put('S', ".####", "#....", "#....", ".###.", "....#", "....#", "####.");
            put('T', "#####", "..#..", "..#..", "..#..", "..#..", "..#..", "..#..");
            put('Y', "#...#", "#...#", ".#.#.", "..#..", "..#..", "..#..", "..#..");
            put('c', ".....", ".....", ".###.", "#....", "#....", "#...#", ".###.");
            put('e', ".....", ".....", ".###.", "#...#", "#####", "#....", ".###.");
            put('h', "#....", "#....", "#.##.", "##..#", "#...#", "#...#", "#...#");
            put('0', ".###.", "#...#", "#..##", "#.#.#", "##..#", "#...#", ".###.");
            put('1', "..#..", ".##..", "..#..", "..#..", "..#..", "..#..", ".###.");
            put('2', ".###.", "#...#", "....#", "...#.", "..#..", ".#...", "#####");
            put(' ', "...", "...", "...", "...", "...", "...", "...");
        }

        static String[] glyph(char c) {
            String[] rows = GLYPHS.get(c);
            if (rows == null) {
                throw new IllegalArgumentException("no glyph for '" + c + "'");
            }
            return rows;
        }

        static double width(String s, double px) {
            int n = 0;
            for (int i = 0; i < s.length(); i++) {
                n += glyph(s.charAt(i))[0].length() + (i + 1 < s.length() ? 1 : 0);
            }
            return n * px;
        }

        static void draw(Graphics2D g, String s, double x, double y, double px, Color fill) {
            g.setColor(fill);
            double cx = x;
            for (int i = 0; i < s.length(); i++) {
                String[] rows = glyph(s.charAt(i));
                for (int r = 0; r < rows.length; r++) {
                    for (int c = 0; c < rows[r].length(); c++) {
                        if (rows[r].charAt(c) == '#') {
                            g.fill(new Rectangle2D.Double(cx + c * px, y + r * px, px, px));
                        }
                    }
                }
                cx += (rows[0].length() + 1) * px;
            }
        }
    }
}
