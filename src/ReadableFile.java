public interface ReadableFile extends AbstractFile {
  byte readb(int index) throws Exception;
  short readw(int index) throws Exception;
  int readl(int index) throws Exception;
  long readq(int index) throws Exception;
}
