package io.github.japskiddin.materialfilepicker;

public class FileItem {
  private String fileName, filePath;

  public FileItem(String fileName, String filePath) {
    this.fileName = fileName;
    this.filePath = filePath;
  }

  public String getFileName() {
    return fileName;
  }

  public String getFilePath() {
    return filePath;
  }
}
