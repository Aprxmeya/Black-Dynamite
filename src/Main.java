import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Main {

    // ASCII ramp (dense → light)
    private static final String ASCII = "@%#*+=-:. ";

    // Defaults (can be overridden by CLI)
    private static int startFrame = 4;
    private static int totalFrames = 133;
    private static int fpsDelayMs = 80; // ~7 FPS
    private static boolean enableColor = false; // default OFF (smooth)

    public static void main(String[] args) throws Exception {

        // ---------- Parse CLI arguments ----------
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {

                case "--color":
                    enableColor = true;
                    break;


                case "--start":
                    startFrame = Integer.parseInt(args[++i]);
                    break;

                case "--help":
                    printHelp();
                    return;
            }
        }

        // Hide cursor
        System.out.print("\033[?25l");

        // ---------- Main animation loop ----------
        for (int frame = startFrame; frame <= totalFrames; frame++) {

            clearTerminal();

            String framePath = String.format("frames/frame_%03d.png", frame);
            BufferedImage image = ImageIO.read(new File(framePath));

            renderFrame(image);

            Thread.sleep(fpsDelayMs);
        }

        // Show cursor back
        System.out.print("\033[?25h");
    }

    // ================= RENDERING =================

    private static void renderFrame(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();

        StringBuilder frameBuffer = new StringBuilder();

        // Skip rows to fix terminal aspect ratio
        for (int y = 0; y < height; y += 2) {

            for (int x = 0; x < width; x++) {

                int pixel = image.getRGB(x, y);

                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;

                int gray = luminance(r, g, b);
                gray = contrastStretch(gray);

                int index = mapToAscii(gray);
                char asciiChar = ASCII.charAt(index);

                if (enableColor) {
                    frameBuffer.append(colorize(asciiChar, r, g, b));
                } else {
                    frameBuffer.append(asciiChar);
                }
            }
            frameBuffer.append('\n');
        }

        // ONE print per frame (smooth)
        System.out.print(frameBuffer.toString());
    }

    // ================= IMAGE PROCESSING =================

    private static int luminance(int r, int g, int b) {
        return (int) (0.299 * r + 0.587 * g + 0.114 * b);
    }

    private static int contrastStretch(int gray) {
        int stretched = (gray - 30) * 255 / 180;
        return Math.max(0, Math.min(255, stretched));
    }

    private static int mapToAscii(int gray) {
        return (255 - gray) * (ASCII.length() - 1) / 255;
    }

    // ================= COLOR =================

    private static String colorize(char c, int r, int g, int b) {
        return "\033[38;2;" + r + ";" + g + ";" + b + "m" + c + "\033[0m";
    }

    // ================= TERMINAL =================

    private static void clearTerminal() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // ================= HELP =================

    private static void printHelp() {
        System.out.println("""
        ASCII Video Player (Java)

        Usage:
          java -jar asciivideo.jar [options]

        Options:
          --color            Enable RGB color mode
          --fps <number>     Frames per second (e.g. 7)
          --start <frame>    Start from frame number
          --help             Show this help
        """);
    }
}
