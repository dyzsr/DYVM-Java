import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

public class TextOutput implements WritableFile {

  final private int MAX_SIZE = 0x80000;

  private byte[] ram = new byte[MAX_SIZE];

  private IOBridge ioBridge = null;

  private TextOutputPane pane = new TextOutputPane();

  public void setIOBridge(IOBridge iob) {
    ioBridge = iob;
    ioBridge.setTextOutput(this);
  }

  public TextOutputPane getPane() {
    return pane;
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

  public void clear() {
    /* clear the buffer and the output pane */
    pane.clear();
  }

  public synchronized void print() throws Exception {
    /* move the content from the buffer to the output pane
     * then clear the buffer */
    pane.print();
  }

  public class TextOutputPane extends Pane {
    private TextArea textArea = new TextArea();
    private String textStr = "";

    public TextOutputPane() {
      textArea.setText(textStr);
      this.widthProperty().addListener(ov ->
          textArea.setPrefWidth(this.getWidth()));
      this.heightProperty().addListener(ov ->
          textArea.setPrefHeight(this.getHeight()));
      textArea.setEditable(false);
      textArea.setId("text-output-text-area");
      this.setId("text-output-pane");
      this.getChildren().add(textArea);
    }

    public void clear() {
      textStr = "";
      textArea.setText(textStr);
    }

    public synchronized void print() throws Exception {
      long size = ioBridge.getOutputBufferSize();
      StringBuilder str = new StringBuilder();
      for (int i = 0; i < MAX_SIZE && i < size; i++) {
        str.append((char) ram[i]);
      }

      textStr += str;
      textArea.setText(textStr);
      textArea.setScrollTop(Double.MAX_VALUE);
      ioBridge.setOutputBufferSize(0);
    }
  }

}
