package ai.aliz.script.cleanup;

import ai.aliz.script.cleanup.fileutils.ConcurrentFileTreeWalk;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.ForkJoinPool;

/**
 * DropBaks
 *
 * @author Tibor Kulcsar
 * @date 9/23/2019
 * @since 1.0
 */
public class DropBaks {
    public static void main(String[] args) {

        File workDir = Paths.get(args[0]).toFile();

        if(!workDir.exists()){
            System.out.println("The folder \"" + workDir.getAbsolutePath() + "\" does not exist!");
        }

        if(!workDir.isDirectory()){
            System.out.println("The entered path \"" + workDir.getAbsolutePath() + "\" is a file. Please give a directory!");
        }

        ConcurrentFileTreeWalk walk = new ConcurrentFileTreeWalk(workDir, n -> n.endsWith(".bak"));

        /**
         * The concurrency is achieved by {@link ForkJoinPool}, where each directory in the tree is checked by one thread including
         * the possible deletion of the folder itself.
         * The solution is <code>dept first<code/>, the child directories are deleted before the parent ones.
         */
        ForkJoinPool pool = ForkJoinPool.commonPool();

        pool.invoke(walk);

        walk.join();
    }
}
