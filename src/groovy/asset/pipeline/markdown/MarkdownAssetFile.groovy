package asset.pipeline.markdown

import asset.pipeline.AbstractAssetFile

class MarkdownAssetFile extends AbstractAssetFile {
    
	static contentType = 'text/html'
	static extensions = ['md']
	static compiledExtension = 'md'
	static processors = [MarkdownProcessor]

	String directiveForLine(String line) {
		line.find(/--=(.*)/) { fullMatch, directive -> return directive }
	}

}
