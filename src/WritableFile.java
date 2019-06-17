public interface WritableFile extends AbstractFile {
  void writeb(int index, byte val) throws Exception;
  void writew(int index, short val) throws Exception;
  void writel(int index, int val) throws Exception;
  void writeq(int index, long val) throws Exception;
}
