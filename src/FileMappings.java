import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class FileMappings {
    public static final HashMap<String, String> mappings;

    static {
        mappings = new HashMap<>();

        putAll(new String[]{"aac", "aiff", "flac", "m4a", "m4b", "m4p", "mmf", "mp3", "ogg", "oga", "mogg", "row", "wma", "wav"}, "fileIcons/audiotrack.svg");
        putAll(new String[]{"apng", "bmp", "gif", "ico", "jpeg", "jpg", "m4a", "png", "webp", "svg"}, "fileIcons/image.svg");
        putAll(new String[]{"webm", "mpg", "mpeg", "mp4", "m4p", "m4v", "avi", "wmv", "mov"}, "fileIcons/video_file.svg");
        mappings.put("txt", "fileIcons/text_snippet.svg");
        putAll(new String[]{"3ds", "collada", "fbx", "gltf", "glb", "obj", "blend"}, "fileIcons/model.svg");
        putAll(new String[]{"rar", "zip", "7z", "jar"}, "fileIcons/folder_zip.svg");
        mappings.put("asm", "fileIcons/asm_seti.svg");
        mappings.put("c", "fileIcons/c_seti.svg");
        mappings.put("cpp", "fileIcons/cpp_seti.svg");
        mappings.put("cs", "fileIcons/c-sharp_seti.svg");
        mappings.put("css", "fileIcons/css_seti.svg");
        mappings.put("d", "fileIcons/d_seti.svg");
        mappings.put("dart", "fileIcons/dart_seti.svg");
        mappings.put("fs", "fileIcons/f-sharp_seti.svg");
        mappings.put("go", "fileIcons/go2_seti.svg");
        mappings.put("gradle", "fileIcons/gradle_seti.svg");
        putAll(new String[]{"html", "htm"}, "fileIcons/html_seti.svg");
        mappings.put("java", "fileIcons/java_seti.svg");
        mappings.put("js", "fileIcons/javascript_seti.svg");
        mappings.put("json", "fileIcons/json_seti.svg");
        mappings.put("kt", "fileIcons/kotlin_seti.svg");
        mappings.put("lua", "fileIcons/lua_seti.svg");
        mappings.put("pl", "fileIcons/perl_seti.svg");
        mappings.put("php", "fileIcons/php_seti.svg");
        mappings.put("py", "fileIcons/python_seti.svg");
        mappings.put("r", "fileIcons/R_seti.svg");
        mappings.put("rb", "fileIcons/ruby_seti.svg");
        mappings.put("rs", "fileIcons/rust_seti.svg");
        mappings.put("swift", "fileIcons/swift_seti.svg");
        mappings.put("ts", "fileIcons/typescript.svg");
        putAll(new String[]{"docx", "doc"}, "fileIcons/word.svg");
        putAll(new String[]{"pptx", "ppt"}, "fileIcons/powerpoint.svg");
        putAll(new String[]{"xlsx", "xls"}, "fileIcons/excel.svg");
        mappings.put("apk", "fileIcons/android.svg");
        mappings.put("exe", "fileIcons/applications.svg");
        mappings.put("bin", "fileIcons/binary.svg");
        mappings.put("pdf", "fileIcons/pdf.svg");
    }
    private static void putAll(String[] types, String pathToIcon) {
        assert mappings != null;
        for (String type : types)
            mappings.put(type, pathToIcon);
    }
    public static ImageIcon getIconFromFilename(File file) {
        String ext = file.getName().substring(file.getName().lastIndexOf('.') + 1);
        String found = mappings.get(ext);
        if (found == null) {
            found = "fileIcons/binary.svg";
        }
        return new ImageIcon(new SVGImageLoader(FileMappings.class.getResource(found).toString(), 32, 32).getImage());
    }
}