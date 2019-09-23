package ai.aliz.script.cleanup.fileutils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.function.Predicate;

/**
 * MultiThreadedFileTreeWalk
 *
 * @author Tibor Kulcsar
 * @date 9/23/2019
 * @since 1.0
 */
public class ConcurrentFileTreeWalk extends RecursiveAction {
    private final Path dir;
    private final Predicate<Path> fileNamePredicate;


    public ConcurrentFileTreeWalk(Path dir, Predicate<Path> fileNamePredicate) {
        this.dir = dir;
        this.fileNamePredicate = fileNamePredicate;

    }

    @Override
    protected void compute() {
        final List<ConcurrentFileTreeWalk> walks = new ArrayList<>();
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {


                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if(fileNamePredicate.test(file)){
                        Files.delete(file);
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!dir.equals(ConcurrentFileTreeWalk.this.dir)) {
                        ConcurrentFileTreeWalk walk = new ConcurrentFileTreeWalk(dir, fileNamePredicate);
                        walk.fork();
                        walks.add(walk);
                        return FileVisitResult.SKIP_SUBTREE;
                    } else {
                        return FileVisitResult.CONTINUE;
                    }
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if(Files.isDirectory(dir) && !Files.list(dir).findAny().isPresent()){
                        Files.delete(dir);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (ConcurrentFileTreeWalk w : walks) {
            w.join();
        }
    }
}
