package ai.aliz.script.cleanup;

import ai.aliz.script.cleanup.fileutils.ConcurrentFileTreeWalk;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * DropBaks
 *
 * @author Tibor Kulcsar
 * @date 9/23/2019
 * @since 1.0
 */
public class DropBaks {
    public static void main(String[] args) throws Exception {

        Path path = Paths.get(args[0]);
        Predicate<Path> fileNamePredicate = path1 -> path1.endsWith(".bak");

//        ConcurrentFileTreeWalk walk = new ConcurrentFileTreeWalk(path, path1 -> path1.endsWith(".bak"));

        ForkJoinPool pool = new ForkJoinPool();

//        pool.invoke(walk);

        Files.walk(path).parallel()
                .sorted(Comparator.reverseOrder())
                .filter(new Predicate<Path>() {
                    @Override
                    public boolean test(Path path) {
                        return path.toString().endsWith(".bak");
                    }
                })
                .map(new Function<Path, File>() {
                    @Override
                    public File apply(Path path) {
                        return path.toFile();
                    }
                })
                .forEach(new Consumer<File>() {
                    @Override
                    public void accept(File file) {
                        file.delete();
                    }
                });


    }
}
