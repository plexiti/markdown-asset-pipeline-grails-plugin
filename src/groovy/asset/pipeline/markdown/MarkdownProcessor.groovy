package asset.pipeline.markdown

import com.github.rjeschke.txtmark.Processor
import groovy.util.logging.Log4j

@Log4j
class MarkdownProcessor {
  
    def precompilerMode

    MarkdownProcessor(precompiler = false) {
        this.precompilerMode = precompiler ? true : false
    }

    def process(String inputText, MarkdownAssetFile assetFile) {
        try {
            // TODO replace image urls with fixed, configured server url or with 
            // @replacement@ string to be replaced by filter on the fly.           
            return Processor.process(inputText)
        } catch (RuntimeException e) {
            if(precompilerMode) {
                def errorDetails = "txtmark processing failed for '${assetFile.file.name}'.\n"
                errorDetails += "**Did you mean to compile this file individually (check docs on exclusion)?**\n"
                log.error(errorDetails, e)
            } else {
                throw e
            }
        }
    }

}
