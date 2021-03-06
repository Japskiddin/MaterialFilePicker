package io.github.japskiddin.materialfilepicker.utils;

import java.io.File;

import io.github.japskiddin.materialfilepicker.R;

public class FileTypeUtils {
  public enum FileType {
    DIRECTORY(R.drawable.ic_folder, R.string.type_directory), DOCUMENT(R.drawable.ic_file,
        R.string.type_document);

    private int icon;
    private int description;

    FileType(int icon, int description) {
      this.icon = icon;
      this.description = description;
    }

    public int getIcon() {
      return icon;
    }

    public int getDescription() {
      return description;
    }
  }

  public static FileType getFileType(File file) {
    if (file.isDirectory()) {
      return FileType.DIRECTORY;
    }

    return FileType.DOCUMENT;
  }
}
