import asset.pipeline.AssetHelper
import asset.pipeline.markdown.MarkdownAssetFile

class MarkdownAssetPipelineGrailsPlugin {

    def title = "Markdown Asset Pipeline Plugin"
    def author = "Martin Schimak"
    def authorEmail = "martin.schimak@plexiti.com"
    def version = "0.1.0-SNAPSHOT"
    def organization = [ name: "plexiti GmbH", url: "http://plexiti.com" ]
    def license = "APACHE"

    def description = '''\
This plugin integrates the 'txtmark' Markdown Processor with Grails' asset-pipeline plugin. Files
with extensions *.md (for markdown) are passed through the txtmark processor and  delivered to the 
requesting client as html fragments.
'''

    def documentation = "http://grails.org/plugin/markdown-asset-pipeline"
    def scm = [ url: "https://github.com/plexiti/markdown-asset-pipeline-grails-plugin" ]
    def issueManagement = [ system: "github", url: "https://github.com/plexiti/markdown-asset-pipeline-grails-plugin/issues" ]
    def grailsVersion = "2.3 > *"

    def doWithWebDescriptor = { xml ->
        // TODO Add filter serving markdown files and replacing server context path on the fly. 
        // Don't register filter in case a fixed url is configured.
    }

    def doWithDynamicMethods = { ctx ->
        AssetHelper.assetSpecs << MarkdownAssetFile
    }

}
