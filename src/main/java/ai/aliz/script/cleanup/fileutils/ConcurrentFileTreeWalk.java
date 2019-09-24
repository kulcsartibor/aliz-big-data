package ai.aliz.script.cleanup.fileutils;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * MultiThreadedFileTreeWalk
 *
 * @author Tibor Kulcsar
 * @date 9/23/2019
 * @since 1.0
 */
public class ConcurrentFileTreeWalk extends RecursiveTask<Boolean> {
    private final File workDir;
    private final Predicate<String> fileNamePredicate;

    /**
     * Constructor
     *
     * @param workDir [{@link File}] :: The {@link File} reference to the working directory which is checked by the
     *                           currently created {@link RecursiveTask}.
     * @param fileNamePredicate [{@link Predicate}<String>] :: This predicate is used to check the filename. This shall
     *                          return <code>true<code/> if the file is marked for deletion
     */
    public ConcurrentFileTreeWalk(File workDir, Predicate<String> fileNamePredicate) {
        this.workDir = workDir;
        this.fileNamePredicate = fileNamePredicate;
    }

    /**
     * @inheritDocs
     *
     * @return
     */
    @Override
    protected Boolean compute() {
        final List<ConcurrentFileTreeWalk> walks = new ArrayList<>();
        final AtomicBoolean keepDir = new AtomicBoolean(false);

        try {
            File[] parentFiles = workDir.listFiles();

            if(Objects.isNull(parentFiles)){
                return true;
            }

            for (File file : parentFiles) {
                if(file.isDirectory()) {
                    ConcurrentFileTreeWalk walk = new ConcurrentFileTreeWalk(file, fileNamePredicate);
                    walk.fork();
                    walks.add(walk);
                } else if(file.isFile() && fileNamePredicate.test(file.getName())){
                    if(!file.delete()){
                        file.deleteOnExit();
                    }
                } else {
                    keepDir.set(true);
                }
            }

            // Waits for the sub folders to be processed. this loop check is there is any sub folder which needs to be kept.
            for (ConcurrentFileTreeWalk w : walks) {
                keepDir.set(keepDir.get() || w.join());
            }

            // Delete the working folder if nothing is left in it.
            if(!keepDir.get() && workDir.isDirectory()){
                if(!workDir.delete()){
                    workDir.deleteOnExit();
                }
            }

            return keepDir.get();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}
