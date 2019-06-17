import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Display implements WritableFile {

  final private int MAX_SIZE = 0x100000;
  final private int W;  // display's width
  final private int H;  // display's height
  final private int MAX_POS;

  private int[] ram = new int [MAX_SIZE];  // display's memory

  private IOBridge ioBridge = null;

  private DisplayCanvas canvas;

  public Display(int w, int h) {
    W = w / 2; H = h / 2;
    MAX_POS = W * H;
    if (MAX_POS > MAX_SIZE) {
      System.exit(0);
    }

    canvas = new DisplayCanvas();
    /* clear the memory */
    for (int i = 0; i < MAX_SIZE; i++) ram[i] = 0;
  }

  public void setIOBridge(IOBridge iob) {
    ioBridge = iob;
    ioBridge.setDisplay(this);
  }

  public DisplayCanvas getCanvas() {
    return canvas;
  }

  @Override
  public int getMaxSize() {
    return MAX_SIZE;
  }

  @Override
  public boolean isReadable() {
    return false;
  }

  @Override
  public boolean isWritable() {
    return true;
  }

  @Override
  public void writeb(int index, byte val) throws Exception {
    throw new Exception("Data size banned !");
  }

  @Override
  public void writew(int index, short val) throws Exception {
    throw new Exception("Data size banned !");
  }

  @Override
  public synchronized void writel(int index, int val) throws Exception {
    if (index > MAX_SIZE) {
      throw new Exception("Invalid index !");
    }
    if ((index & 3) > 0) {
      throw new Exception("Data aligning is required !");
    }
    index >>= 2;
    ram[index] = val;
  }

  @Override
  public void writeq(int index, long val) throws Exception {
    throw new Exception("Data size banned !");
  }

  public void clear() {
    /* clear the display */
    for (int i = 0; i < MAX_POS; i++) {
      ram[i] = 0;
    }
    canvas.paint();
  }

  public synchronized void paint() {
    /* paint current frame and clear the memory */
    canvas.paint();
    for (int i = 0; i < MAX_POS; i++) {
      ram[i] = 0;
    }
  }

  public class DisplayCanvas extends Canvas {

    private GraphicsContext gc;

    public DisplayCanvas() {
      super(W * 2, H * 2);
      gc = this.getGraphicsContext2D();
      paint();
    }

    public void paint() {
      /* draw pixels to the display */
      gc.setFill(Color.BLACK);
      gc.fillRect(0, 0, this.getWidth(), this.getHeight());

      int pos = 0;
      for (int x = 0; x < W; x++) {
        for (int y = 0; y < H; y++, pos++) {
          int value = ram[pos];
          int red = (value >> 24) & 0xff;
          int green = (value >> 16) & 0xff;
          int blue = (value >> 8) & 0xff;
          int alpha = value & 0xff;
          double r = red / 255.0;
          double g = green / 255.0;
          double b = blue / 255.0;
          double a = alpha / 255.0;
          gc.setFill(Color.color(r, g, b, a));
          gc.fillRect(x << 1, y << 1, 2, 2);
        }
      }
    }

  }

}
