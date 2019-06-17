import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.io.*;

public class Memory implements ReadableFile, WritableFile {

  private final int MAX_SIZE;
  private byte[] ram;

  private IOBridge ioBridge = null;

  private MemoryPane pane = new MemoryPane();

  public MemoryPane getPane() {
    return pane;
  }

  final public static int RESERVE_SECTION_POS = 0x0000000;
  final public static int OBJECT_SECTION_POS = 0x1000000;
  final public static int STACK_SECTION_POS = 0x2000000;
  final public static int STACK_SECTION_LIMIT = 0x3000000;
  final public static int DATA_SECTION_POS = 0x3000000;

  public Memory() {
    this(0x10000000);
  }

  public Memory(int maxSize) {
    ram = new byte[maxSize];
    MAX_SIZE = maxSize;
  }

  public void setIOBridge(IOBridge iob) {
    ioBridge = iob;
    ioBridge.setMemory(this);
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
    return true;
  }

  public void load(File file) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(file, "r");
    raf.read(ram, OBJECT_SECTION_POS, MAX_SIZE - OBJECT_SECTION_POS);
  }

  @Override
  public byte readb(int index) throws Exception {
    if (index < 0 || index >= MAX_SIZE) {
      throw new Exception();
    }
    return ram[index];
  }

  @Override
  public short readw(int index) throws Exception {
    if (index < 0 || index + 2 > MAX_SIZE) {
      throw new Exception();
    }
    short val = (short) (((short)ram[index] << 8) +
        ((short) ram[index + 1] & 0xff));
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
      val += ((int) ram[index + i] & 0xff);
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
      val += ((long) ram[index + i] & 0xff);
    }
    return val;
  }

  @Override
  public void writeb(int index, byte val) throws Exception {
    if (index < 0 || index >= MAX_SIZE) {
      throw new Exception();
    }
    ram[index] = val;
  }

  @Override
  public void writew(int index, short val) throws Exception {
    if (index < 0 || index + 2 > MAX_SIZE) {
      throw new Exception();
    }
    ram[index] = (byte) (val & 0xff);
    ram[index + 1] = (byte) (val >> 8 & 0xff);
  }

  @Override
  public void writel(int index, int val) throws Exception {
    if (index < 0 || index + 4 > MAX_SIZE) {
      throw new Exception();
    }
    ram[index] = (byte) (val & 0xff);
    ram[index + 1] = (byte) (val >> 8 & 0xff);
    ram[index + 2] = (byte) (val >> 16 & 0xff);
    ram[index + 3] = (byte) (val >> 24 & 0xff);
  }

  @Override
  public void writeq(int index, long val) throws Exception {
    if (index < 0 || index + 8 > MAX_SIZE) {
      throw new Exception();
    }
    ram[index] = (byte) (val & 0xff);
    ram[index + 1] = (byte) (val >> 8 & 0xff);
    ram[index + 2] = (byte) (val >> 16 & 0xff);
    ram[index + 3] = (byte) (val >> 24 & 0xff);
    ram[index + 4] = (byte) (val >> 32 & 0xff);
    ram[index + 5] = (byte) (val >> 40 & 0xff);
    ram[index + 6] = (byte) (val >> 48 & 0xff);
    ram[index + 7] = (byte) (val >> 56 & 0xff);
  }

  public void refresh() throws Exception {
    pane.refresh();
  }

  public class MemoryPane extends GridPane {
    private TextField[] address = new TextField[10];
    private Label[] value = new Label[10];

    public MemoryPane() {
      this.setId("memory-pane");
      // add components
      for (int i = 0; i < 10; i++) {
        TextField tf = new TextField();
        Label lb = new Label();
        this.widthProperty().addListener(ov -> {
          tf.setPrefWidth(getWidth() / 2);
          lb.setPrefWidth(getWidth() - tf.getWidth());
        });
        this.heightProperty().addListener(ov -> {
          tf.setPrefHeight(getHeight() / 10);
          lb.setPrefHeight(getHeight() / 10);
        });
        tf.setId("memory-text-field");
        lb.setId("memory-label");
        address[i] = tf;
        value[i] = lb;
        this.add(tf, 0, i);
        this.add(lb, 1, i);
      }
    }

    private int parseInt(String str) {
      if (str.isEmpty()) return -1;

      StringBuilder builder = new StringBuilder(str);
      boolean hex = false;
      if (builder.length() > 2 && builder.substring(0, 2).equals("0x")) {
        hex = true;
        builder.delete(0, 2);
      }
      int val = 0;
      if (hex) {
        int len = builder.length();
        for (int i = 0; i < len; i++) {
          char c = builder.charAt(i);
          if (Character.isDigit(c) || Character.isAlphabetic(c)) {
            c = Character.toLowerCase(c);
            if (c > 'f') return -1;
            if (Character.isDigit(c)) val = (val << 4) + c - '0';
            else val = (val << 4) + c - 'a' + 10;
          } else return -1;
        }
      } else {
        int len = builder.length();
        for (int i = 0; i < len; i++) {
          char c = builder.charAt(i);
          if (Character.isDigit(c)) {
            val = val * 10 + c - '0';
          } else return -1;
        }
      }
      if (val + 16 > MAX_SIZE || val < 0) return -1;
      return val;
    }

    private void refresh() throws Exception {
      for (int i = 0; i < 10; i++) {
        int addr = parseInt(address[i].getText());
        if (addr == -1) {
          value[i].setText("");
        } else {
          StringBuffer strVal = new StringBuffer();
          for (int j = 0; j < 16; j++) {
            if (j > 0) strVal.append(' ');
            if (j == 8) strVal.append(' ');
            int val = 0xff & (int) readb(addr + j);
            StringBuffer byteVal = new StringBuffer(Integer.toHexString(val));
            if (byteVal.length() < 2) byteVal.insert(0, '0');
            strVal.append(byteVal);
          }
          value[i].setText(strVal.toString());
        }
      }
    }
  }

}
