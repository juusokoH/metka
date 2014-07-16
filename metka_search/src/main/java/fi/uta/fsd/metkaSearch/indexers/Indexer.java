package fi.uta.fsd.metkaSearch.indexers;

import fi.uta.fsd.metkaSearch.directory.DirectoryManager;
import fi.uta.fsd.metkaSearch.LuceneConfig;
import fi.uta.fsd.metkaSearch.commands.indexer.IndexerCommand;
import fi.uta.fsd.metkaSearch.enums.IndexerConfigurationType;
import fi.uta.fsd.metkaSearch.enums.IndexerStatusMessage;
import fi.uta.fsd.metkaSearch.directory.DirectoryInformation;
import org.apache.lucene.index.Term;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Base class for all Indexers in the software.
 * Implements basic functionality common for all indexers but all specialized stuff needs to be handled elsewhere.
 */
public abstract class Indexer implements Callable<IndexerStatusMessage>, IndexerCommandHandler {
    /**
     * Returns an Indexer of the correct subclass based on the type
     * @param path
     * @return
     */
    public static Indexer indexerFactory(DirectoryManager.DirectoryPath path) throws IOException {
        Indexer indexer = null;
        switch(path.getType())  {
            case WIKIPEDIA:
                indexer = new WikipediaIndexer(path.isUseRam(), path.getLanguage());
                break;
            case DUMMY:
                indexer = new DummyIndexer(path.isUseRam(), path.getLanguage());
                break;
        }
        return indexer;
    }

    private IndexerStatusMessage status;

    // INFORMATION FOR INDEX HANDLING
    // Switch to see if there has been indexed data that needs to be flushed to disk
    private boolean indexChanged = false;
    // Counter for idle loops. If there's been changes to index they will be flushed after certain number of times.
    private int idleLoops = 0;
    // Counter for processed commands. Used to detect when to force flush the index if that setting is true.
    private int commandBatch = 0;

    private final DirectoryInformation indexer;

    protected BlockingQueue<IndexerCommand> commandQueue = new LinkedBlockingQueue<>();

    protected Indexer(DirectoryManager.DirectoryPath path) throws IOException {
        indexer = DirectoryManager.getIndexDirectory(path);
    }

    @Override
    public boolean addCommand(IndexerCommand command) {
        // If this handler is in the process of quitting or has stopped running then there's no point in
        // adding the command to the queue.
        if(Thread.currentThread().isInterrupted() || status == IndexerStatusMessage.STOP || status == IndexerStatusMessage.RETURNED) {
            return false;
        }
        // If command is meant to another indexer then don't add it
        if(!command.getPath().equals(indexer.getPath())) {
            return false;
        }
        return commandQueue.add(command);
    }

    protected void setStatus(IndexerStatusMessage status) {
        this.status = status;
    }

    protected IndexerStatusMessage getStatus() {
        return status;
    }

    protected DirectoryInformation getIndexer() {
        return indexer;
    }

    @Override
    public IndexerStatusMessage call() throws Exception {

        // DEBUG
        long timeHandlingCommands = 0L;

        while(status != IndexerStatusMessage.STOP && status != IndexerStatusMessage.RETURNED) {
            try {
                IndexerCommand command = commandQueue.poll(5, TimeUnit.SECONDS);
                if(command != null) {
                    long start = System.currentTimeMillis();
                    System.err.println("Started new command for: "+command.getPath());
                    status = IndexerStatusMessage.PROCESSING;

                    // Reset idle loops since there's work
                    idleLoops = 0;

                    // Handle command
                    if(command.getAction() == IndexerCommand.Action.STOP) {
                        // Indexer is requested to stop
                        setStatus(IndexerStatusMessage.STOP);
                    } else {
                        // Forward handling to implementation
                        handleCommand(command);
                        // Set indexChanged to true since command was handled
                        indexChanged = true;
                    }

                    if(LuceneConfig.FORCE_FLUSH_AFTER_BATCH_OF_COMMANDS) {
                        commandBatch++;
                    }
                    // DEBUG
                    long end = System.currentTimeMillis();
                    timeHandlingCommands += end-start;
                    System.err.println("Took "+(end-start)+"ms to handle command");
                } else {
                    if(idleLoops == 0) {
                        System.err.println("Queue clear. Spent "+timeHandlingCommands+"ms handling commands");
                        timeHandlingCommands = 0L;
                    }
                    // Increase idleLoops counter if index has changed
                    if(indexChanged) {
                        idleLoops++;
                        System.err.println("Number of idle loops: "+idleLoops);
                    }
                }

                if(idleLoops > 1 && status != IndexerStatusMessage.IDLING) {
                    // State that the handler is idling only after at least one idle loop has been passed
                    // It's safer to assume that we are actually idle if we have been idle for at least a loop
                    status = IndexerStatusMessage.IDLING;
                }

                if(idleLoops >= LuceneConfig.IDLE_LOOPS_BEFORE_FLUSH
                        || (LuceneConfig.FORCE_FLUSH_AFTER_BATCH_OF_COMMANDS && commandBatch >= LuceneConfig.MAX_COMMAND_BATCH_SIZE)) {
                    flushIndex();
                }

                // Check if process should continue running
                if(Thread.currentThread().isInterrupted()) {
                    status = IndexerStatusMessage.STOP;
                }
            } catch (InterruptedException ex) {
                // Try to close the indexer
                indexer.getIndexWriter().close();
                ex.printStackTrace();
                throw new InterruptedException();
            }
        }
        if(status == IndexerStatusMessage.STOP) {
            indexer.getIndexWriter().close();
        }
        status = IndexerStatusMessage.RETURNED;
        return IndexerStatusMessage.RETURNED;
    }

    protected abstract void handleCommand(IndexerCommand command) throws IOException, SAXException;

    protected void removeDocument(Term term) throws IOException {
        // TODO: OutOfMemory checks and writer closing
        indexer.getIndexWriter().deleteDocuments(term);
    }

    private void flushIndex() throws IOException {
        indexChanged = false;
        idleLoops = 0;
        commandBatch = 0;
        if(indexer != null) {
            try {
                // Try to commit the writer
                indexer.getIndexWriter().commit();
                // Set indexer to dirty state so that searchers know to update their index
                indexer.setDirty(true);
            } catch (OutOfMemoryError er) {
                er.printStackTrace();
                // If we get an OutOfMemoryError then close the writer immediately
                try {
                    // Try closing the writer
                    indexer.getIndexWriter().close();
                } catch(OutOfMemoryError erc) {
                    // As I understand it we should get another OutOfMemoryError, close the writer again
                    indexer.getIndexWriter().close();
                } finally {
                    // Interrupt current Thread since we can't continue indexing
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}