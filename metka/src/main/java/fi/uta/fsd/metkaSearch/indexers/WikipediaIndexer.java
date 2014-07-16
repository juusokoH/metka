package fi.uta.fsd.metkaSearch.indexers;

import fi.uta.fsd.metkaSearch.commands.indexer.IndexerCommand;
import fi.uta.fsd.metkaSearch.commands.indexer.WikipediaIndexerCommand;
import fi.uta.fsd.metkaSearch.directory.DirectoryManager;
import fi.uta.fsd.metkaSearch.entity.IndexerCommandRepository;
import fi.uta.fsd.metkaSearch.enums.IndexerConfigurationType;
import fi.uta.fsd.metkaSearch.handlers.WikipediaHandler;
import org.apache.lucene.index.Term;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.FileReader;
import java.io.IOException;

public class WikipediaIndexer extends Indexer {

    public static WikipediaIndexer build(DirectoryManager.DirectoryPath path, IndexerCommandRepository commands) throws IOException, UnsupportedOperationException {
        checkPathType(path, IndexerConfigurationType.WIKIPEDIA);
        // Check additional parameters
        if(path.getAdditionalParameters() != null && path.getAdditionalParameters().length > 0) {
            throw new UnsupportedOperationException("Wikipedia indexer doesn't accept any additional parameters");
        }
        return new WikipediaIndexer(path, commands);
    }

    // Should only be used from the factory method in Indexer
    private WikipediaIndexer(DirectoryManager.DirectoryPath path, IndexerCommandRepository commands) throws IOException {
        super(path, commands);
    }

    protected void handleCommand(IndexerCommand command) throws IOException, SAXException {
        // This is a safe type conversion since Indexers add command only accepts commands of correct type
        WikipediaIndexerCommand wCom = (WikipediaIndexerCommand) command;

        switch(wCom.getAction()) {
            case REMOVE:
                // Create term for identification
                if(StringUtils.isEmpty(wCom.getPageId())) {
                    break;
                }
                Term term = new Term("pageId", wCom.getPageId());
                removeDocument(term);
                break;
            case INDEX:
                indexCommand(wCom);
                break;
            case STOP:
                // This is here to remove compiler warning, actual STOP command is handled earlier
                break;
        }
    }

    /**
     * Create Document out of an XML file and add it to the writer.
     *
     * @param command
     */
    private void indexCommand(WikipediaIndexerCommand command) throws IOException, SAXException {
        if(StringUtils.isEmpty(command.getFilePath())) {
            // No sense in trying to parse empty path
            return;
        }
        // Create sax parser
        XMLReader xr;
        try {
            xr = XMLReaderFactory.createXMLReader();
        } catch(SAXException sex) {
            sex.printStackTrace();
            return;
        }
        // Create handler
        WikipediaHandler handler = new WikipediaHandler(getIndexer(), command);
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);

        // Try parsing the file
        FileReader fr = new FileReader(command.getFilePath());
        // This blocks for as long as the parsing takes
        xr.parse(new InputSource(fr));
    }
}