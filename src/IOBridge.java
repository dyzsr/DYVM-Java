import java.io.*;

public class IOBridge {

  private AbstractFile[] files = new AbstractFile[4];

  private Processor processor = null;

  private Memory memory;
  private Keyboard keyboard;
  private TextOutput textOutput;
  private Display display;

  public void setMemory(Memory memory) {
    this.memory = memory;
    files[0] = memory;
  }

  public void setKeyboard(Keyboard keyboard) {
    this.keyboard = keyboard;
    files[1] = keyboard;
  }

  public void setTextOutput(TextOutput textOutput) {
    this.textOutput = textOutput;
    files[2] = textOutput;
  }

  public void setDisplay(Display display) {
    this.display = display;
    files[3] = display;
  }

  public void setProcessor(Processor pro) {
    processor = pro;
  }

  public synchronized void setKeyboardInterrupt() {
    // TODO
  }

  public void loadObject(File file) throws IOException {
    memory.load(file);
  }

  public void clearTextOutput() {
    textOutput.clear();
  }

  public void clearDisplay() {
    display.clear();
  }

  public void refresh() throws Exception {
    memory.refresh();
  }

  public synchronized void setInputBufferSize() throws Exception {
    // TODO
  }

  public synchronized void setOutputBufferSize(long value) throws Exception {
    memory.writeq(8, value);
  }

  public long getOutputBufferSize() throws Exception {
    return memory.readq(8);
  }

  public synchronized void setRandomSeed(int value) throws Exception {
    memory.writel(16, value);
  }

  public void interupt(int value) throws Exception {
    switch (value) {
      case 1: /* TODO */ break;
      case 2: textOutput.print(); break;
      case 3: display.paint(); break;
    }
  }

  private int absoluteIndex;

  private AbstractFile getAbstractFile(int index) throws Exception {
    absoluteIndex = index;
    if (absoluteIndex < 0) throw new Exception("^V^V^V^V^V^V^");
    for (AbstractFile file : files) {
      if (absoluteIndex < file.getMaxSize()) return file;
      absoluteIndex -= file.getMaxSize();
    }
    throw new Exception("Illegal Memory Address !");
  }

  private ReadableFile getReadableFile(int index) throws Exception {
    AbstractFile file = getAbstractFile(index);
    if (!file.isReadable()) throw new Exception("NotAReadableFile");
    return (ReadableFile)file;
  }

  private WritableFile getWritableFile(int index) throws Exception {
    AbstractFile file = getAbstractFile(index);
    if (!file.isWritable()) throw new Exception("NotAWritableFile");
    return (WritableFile)file;
  }

  public byte readb(int index) throws Exception {
    ReadableFile file = getReadableFile(index);
    return file.readb(absoluteIndex);
  }

  public short readw(int index) throws Exception {
    ReadableFile file = getReadableFile(index);
    return file.readw(absoluteIndex);
  }

  public int readl(int index) throws Exception {
    ReadableFile file = getReadableFile(index);
    return file.readl(absoluteIndex);
  }

  public long readq(int index) throws Exception {
    ReadableFile file = getReadableFile(index);
    return file.readq(absoluteIndex);
  }

  public void writeb(int index, byte val) throws Exception {
    WritableFile file = getWritableFile(index);
    file.writeb(absoluteIndex, val);
  }

  public void writew(int index, short val) throws Exception {
    WritableFile file = getWritableFile(index);
    file.writew(absoluteIndex, val);
  }

  public void writel(int index, int val) throws Exception {
    WritableFile file = getWritableFile(index);
    file.writel(absoluteIndex, val);
  }

  public void writeq(int index, long val) throws Exception {
    WritableFile file = getWritableFile(index);
    file.writeq(absoluteIndex, val);
  }

}
