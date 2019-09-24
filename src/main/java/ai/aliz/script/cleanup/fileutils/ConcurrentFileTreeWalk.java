package ai.aliz.script.cleanup.fileutils;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * MultiThreadedFileTreeWalk
 *
 * @author Tibor Kulcsar
 * @date 9/23/2019
 * @since 1.0
 */
public class ConcurrentFileTreeWalk extends RecursiveTask<Boolean> {
    private final Path startDir;
    private final Predicate<String> fileNamePredicate;


    public ConcurrentFileTreeWalk(Path startDir, Predicate<String> fileNamePredicate) {
        this.startDir = startDir;
        this.fileNamePredicate = fileNamePredicate;

    }

    @Override
    protected Boolean compute() {
        final List<ConcurrentFileTreeWalk> walks = new ArrayList<>();
        final AtomicBoolean keepDir = new AtomicBoolean(false);

        try {
            Files.list(this.startDir).forEach(new Consumer<Path>() {
                @Override
                public void accept(Path path) {
                    File file = path.toFile();

                    if(file.isDirectory()) {
                        ConcurrentFileTreeWalk walk = new ConcurrentFileTreeWalk(path, fileNamePredicate);
                        walk.fork();
                        walks.add(walk);
                        return;
                    }
                    if(file.isFile() && fileNamePredicate.test(file.getName())){
                        System.out.println("File Deleted: " + file.getAbsolutePath() + " " + file.delete());
                    }
                    keepDir.set(true);
                }
            });

            for (ConcurrentFileTreeWalk w : walks) {
                keepDir.set(keepDir.get() || w.join());
            }

            File currentDir = startDir.toFile();


            if(!keepDir.get() && currentDir.isDirectory()){
                System.out.println("Delete: " + startDir);
                if(!currentDir.delete()){
                    currentDir.deleteOnExit();
                }
            }

            return keepDir.get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;

    }
}
