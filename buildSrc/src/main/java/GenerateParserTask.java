import java.util.Collections;

import com.javacc.Main;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public abstract class GenerateParserTask extends DefaultTask {
    @InputFile
    public abstract RegularFileProperty getGrammarFile();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void run() {
        getProject().delete(getOutputDir());

        int returnValue;
        try {
            returnValue = Main.mainProgram(
                    getGrammarFile().get().getAsFile().toPath(),
                    getOutputDir().get().getAsFile().toPath(),
                    "java",
                    8,
                    true,
                    Collections.emptyMap()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (returnValue != 0) {
            throw new RuntimeException("JavaCC returned a non-zero exit code.");
        }
    }
}
