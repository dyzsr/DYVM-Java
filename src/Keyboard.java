import javafx.scene.layout.Pane;

public class Keyboard implements ReadableFile {

  /*
   * TODO
   */

  public final int MAX_SIZE;
  private byte[] buffer;

  private IOBridge ioBridge = null;

  private KeyboardPane pane = new KeyboardPane();

  public Keyboard() {
    this(0x80000);
  }

  public Keyboard(int maxSize) {
    buffer = new byte[maxSize];
    MAX_SIZE = maxSize;
  }

  public void setIOBridge(IOBridge iob) {
    ioBridge = iob;
    ioBridge.setKeyboard(this);
  }

  public KeyboardPane getPane() {
    return pane;
  }

  @Override
  public int getMaxSize() {
    return MAX_SIZE;
  }

  @Override
  public boolean isReadable() {
    return true;
  }

  @Override
  public boolean isWritable() {
    return false;
  }

  @Override
  public byte readb(int index) throws Exception {
    if (index < 0 || index >= MAX_SIZE) {
      throw new Exception();
    }
    return buffer[index];
  }

  @Override
  public short readw(int index) throws Exception {
    if (index < 0 || index + 2 > MAX_SIZE) {
      throw new Exception();
    }
    short val = buffer[index + 1];
    val <<= 8;
    val += ((short) buffer[index] & 0xff);
    return val;
  }

  @Override
  public int readl(int index) throws Exception {
    if (index < 0 || index + 4 > MAX_SIZE) {
      throw new Exception();
    }
    int val = 0;
    for (int i = 3; i >= 0; i--) {
      val <<= 8;
      val += ((int) buffer[index + i] & 0xff);
    }
    return val;
  }

  @Override
  public long readq(int index) throws Exception {
    if (index < 0 || index + 8 > MAX_SIZE) {
      throw new Exception();
    }
    long val = 0;
    for (int i = 7; i >= 0; i--) {
      val <<= 8;
      val += ((long) buffer[index + i] & 0xff);
    }
    return val;
  }

  public class KeyboardPane extends Pane {
    public KeyboardPane() {
      this.setId("keyboard-pane");
    }
  }
}