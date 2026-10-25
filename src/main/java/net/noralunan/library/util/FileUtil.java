package net.noralunan.library.util;

import net.noralunan.library.NoraLib;
import net.noralunan.library.common.NoraLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class FileUtil
{
    private static final NoraLogger LOGGER = NoraLib.logger();

    public static void create(Path path)
    {
        NoraLib.confirmNotNull(path);
        create(path.toString());
    }

    public static void create(String path)
    {
        NoraLib.confirmNotNull(path);
        create(new File(path));
    }

    public static boolean create(File file)
    {
        NoraLib.confirmNotNull(file);
        if (file.exists()) return true;
        try
        {
            File dir = file.getParentFile();
            if (dir != null && !dir.exists())
            {
                if (!dir.mkdirs())
                {
                    LOGGER.error("Failed to create parent directories for file: {}", dir);
                    return false;
                }
            }

            if (file.createNewFile())
            {
                LOGGER.info("A new file has been created: {}", file.getPath());
                return true;
            }
        }
        catch (IOException exception)
        {
            LOGGER.error("File creation failed: {}", exception);
        }
        LOGGER.error("Failed to create the file: {}", file.getPath());
        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean write(File file, List<String> content)
    {
        NoraLib.confirmNotNull(file, content);
        return write(file, String.join("\n", content));
    }

    public static boolean write(File file, String content)
    {
        NoraLib.confirmNotNull(file, content);
        File temp = new File(file.getAbsolutePath() + ".tmp");
        // 写入临时文件
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(temp), StandardCharsets.UTF_8))
        {
            writer.write(content);
            writer.flush();
        }
        catch (IOException exception)
        {
            LOGGER.error("Failed to write file: {}", exception);
            removeTemp(temp);
            return false;
        }
        // 写入文件
        try
        {
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            LOGGER.info("File has been successfully updated: {}", file.toPath());
            return true;
        }
        catch (IOException abc)
        {
            try
            {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("File has been successfully updated: {}", file.toPath());
                return true;
            }
            catch (IOException exception)
            {
                LOGGER.error("Failed to replace file: {}", exception);
            }
        }
        finally
        {
            removeTemp(temp);
        }
        return false;
    }

    private static void removeTemp(File temp)
    {
        if (temp.exists() && !temp.delete())
        {
            LOGGER.error("Failed to delete temporary file: {}", temp);
        }
    }
}
