import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class FileMappings {
    public static final HashMap<String, String> mappings;

    static {
        mappings = new HashMap<>();
        // Common types

        mappings.put("audio/*", "audiotrack.svg");
        mappings.put("image/*", "image.svg");
        mappings.put("video/*", "video_file.svg");
        mappings.put("text/*", "text_snippet.svg");
        mappings.put("model/*", "model.svg");
        mappings.put("application/pdf", "pdf.svg");

        // Defaults
        mappings.put("application/octet-stream", "binary.svg");

        // Executables
        mappings.put("application/x-msdos-program", "applications.svg");
        mappings.put("application/vnd.android.package-archive", "android.svg");


        // Compressed archive
        putAll(new String[]{
                "application/x-bzip",
                "application/x-bzip2",
                "application/gzip",
                "application/vnd.rar",
                "application/x-tar",
                "application/zip",
                "application/x-7z-compressed"
        }, "folder_zip.svg");

        // Word files
        putAll(new String[]{
                "application/msword",
                "application/vnd.ms-word.document.macroenabled.12",
                "application/vnd.ms-word.template.macroenabled.12",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.template"
        }, "word.svg");

        // Powerpoint files
        putAll(new String[]{
                "application/powerpoint",
                "application/mspowerpoint",
                "application/vnd.ms-powerpoint",
                "application/vnd.ms-powerpoint.presentation.macroenabled.12",
                "application/vnd.ms-powerpoint.slideshow.macroenabled.12",
                "application/vnd.ms-powerpoint.template.macroenabled.12",
                "application/vnd.ms-powerpoint.addin.macroenabled.12",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.openxmlformats-officedocument.presentationml.slideshow"
        }, "powerpoint.svg");

        // Excel files
        putAll(new String[]{
                "application/excel",
                "application/x-excel",
                "application/x-msexcel",
                "application/vnd.ms-excel",
                "application/vnd.ms-excel.sheet.macroenabled.12",
                "application/vnd.ms-excel.sheet.binary.macroenabled.12",
                "application/vnd.ms-excel.template.macroenabled.12",
                "application/vnd.ms-excel.addin.macroenabled.12",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
                "image/vnd.xiff"
        }, "excel.svg");
    }
    private static void putAll(String[] types, String pathToIcon) {
        assert mappings != null;
        for (String type : types)
            mappings.put(type, pathToIcon);
    }
    public static ImageIcon getIconFromFilename(File file) {
        String mime = null;
        try {
            mime = Files.probeContentType(file.toPath());
        } catch (IOException ignored) {}
        mime = (mime == null) ? "application/octet-stream" : mime;

        String found;
        // Specific search
        found = mappings.get(mime);
        // Generic search
        if (found == null) {
            found = mappings.get(mime.substring(0, mime.lastIndexOf('/') + 1).concat("*"));
        }
        // Default (binary file)
        if (found == null) {
            found = "binary.svg";
        }
        return new ImageIcon(new SVGImageLoader("./res/fileIcons/".concat(found), 32, 32).getImage());
    }
}