package net.teekee.exifrenamer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.NoSuchElementException;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class Renamer {

  public static void main(String[] args) {

    if (args.length == 0) {
      throw new RuntimeException("引数でフォルダ、またはファイルを指定してください。");
    }

    File file = new File(args[0]);

    if (!file.exists()) {
      throw new RuntimeException("ファイルまたはフォルダが存在しません。");
    }

    change(file);
  }

  public static void change(File file) {
    if (file.isDirectory()) {
      Arrays.stream(file.listFiles()).forEach(Renamer::change);
    } else {
      changeFileNameAndLastModified(file);
    }

  }

  public static boolean changeFileNameAndLastModified(File file) {

    String name = file.getName();
    String ext = name.substring(name.lastIndexOf("."));

    if (!".jpg".equals(ext.toLowerCase())) {
      return false;
    }

    Date originalDate = getDate(file);

    file.setLastModified(originalDate.getTime());

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
    String formatedDate = sdf.format(originalDate);

    for (int i = 0; i < 100000; i++) {
      String fileName = String.format("%s/%s_%05d.jpg", file.getParentFile().getAbsolutePath(), formatedDate, i);

      File newFile = new File(fileName);

      if (!newFile.exists()) {
        file.renameTo(newFile);
        return true;
      }
    }

    // TODO throw Exception
    return false;

  }

  public static Date getDate(final File file) {
    try {
      final Metadata metadata = ImageMetadataReader.readMetadata(file);

      final ExifSubIFDDirectory directory = metadata.getDirectoriesOfType(ExifSubIFDDirectory.class).iterator().next();

      if (directory.getDateOriginal() != null) {
        return directory.getDateOriginal();
      }

      for (final Directory directory2 : metadata.getDirectories()) {
        for (Tag tag : directory2.getTags()) {
          if ("File Modified Date".equals(tag.getTagName())) {
            return directory2.getDate(tag.getTagType());
          }
        }
      }

      return new Date(file.lastModified());

    } catch (ImageProcessingException e) {
      e.printStackTrace();
      throw new RuntimeException("");
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("");
    } catch (NoSuchElementException e) {
      return new Date(file.lastModified());
    }
  }
}
