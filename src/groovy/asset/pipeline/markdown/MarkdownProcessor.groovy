package asset.pipeline.markdown

import asset.pipeline.AssetHelper
import asset.pipeline.DirectiveProcessor
import com.github.rjeschke.txtmark.Processor
import grails.util.Holders
import grails.util.Metadata
import groovy.util.logging.Log4j

@Log4j
class MarkdownProcessor {
  
    def precompilerMode

    MarkdownProcessor(precompiler = false) {
        this.precompilerMode = precompiler ? true : false
    }

    def process(inputText, assetFile) {
        def cachedPaths = [:]
        try {
            return Processor.process(inputText
                    .replaceAll('@context.path@', contextPath())
                    .replaceAll(/!\[(.+)\]\s*\(([a-zA-Z0-9\-\_\.\/\@\#\?\ \&\+\%\=]+)\)/) { fullMatch, altText, assetPath ->
                def replacementPath = assetPath.trim()
                if(cachedPaths[assetPath]) {
                    replacementPath = cachedPaths[assetPath]
                } else if(isRelativePath(assetPath)) {
                    def urlRep = new URL("http://hostname/${assetPath}") // Split out subcomponents
                    def relativeFileName = [relativePath(assetFile.file), urlRep.path].join(File.separator)
                    def markdownFile = AssetHelper.fileForFullName(relativeFileName)
                    if(markdownFile) {
                        replacementPath = absolutePathToBaseFile(markdownFile, this.precompilerMode)
                        if(urlRep.query != null) {
                            replacementPath += "?${urlRep.query}"
                        }
                        if(urlRep.ref) {
                            replacementPath += "#${urlRep.ref}"
                        }
                        cachedPaths[assetPath] = replacementPath
                    }
                }
                return "![$altText](${replacementPath})"
            } as String)
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

    private static isRelativePath(assetPath) {
        return !assetPath.startsWith("/") && !assetPath.startsWith("http")
    }

    private static absolutePathToBaseFile(file, useDigest=false) {
        def grailsApplication = Holders.getGrailsApplication()
        def currentRelativePath = relativePath(file, false).split(AssetHelper.DIRECTIVE_FILE_SEPARATOR).findAll({it}).reverse()
        def filePathIndex=currentRelativePath.size()- 1
        def calculatedPath = []
        def assetsUrl = grailsApplication?.config?.grails?.assets?.url
        if (assetsUrl && assetsUrl instanceof String) {
            if (assetsUrl.endsWith('/')) {
                assetsUrl = assetsUrl.substring(0, assetsUrl.length() - 1)
            }
            calculatedPath << assetsUrl
        } else {
            calculatedPath << contextPath()
            calculatedPath << (grailsApplication?.config?.grails?.assets?.mapping ?: 'assets')
        }
        for(;filePathIndex>=0;filePathIndex--) {
            calculatedPath << currentRelativePath[filePathIndex]
        }
        if(useDigest) {
            def extension = AssetHelper.extensionFromURI(file.getName())
            def fileName  = AssetHelper.nameWithoutExtension(file.getName())
            def assetFile = AssetHelper.assetForFile(file)
            def digestName
            if(assetFile != file) {
                def directiveProcessor = new DirectiveProcessor(assetFile.contentType, true)
                def fileData = directiveProcessor.compile(assetFile)
                digestName = AssetHelper.getByteDigest(fileData.bytes)
            }
            else {
                digestName = AssetHelper.getByteDigest(file.bytes)
            }
            calculatedPath << "${fileName}-${digestName}.${extension}"
        } else {
            calculatedPath << file.getName()
        }
        return calculatedPath.join(AssetHelper.DIRECTIVE_FILE_SEPARATOR)
    }

    private static relativePath(file, includeFileName=false) {
        def path
        if(includeFileName) {
            path = file.class.name == 'java.io.File' ? file.getCanonicalPath().split(AssetHelper.QUOTED_FILE_SEPARATOR) : file.file.getCanonicalPath().split(AssetHelper.QUOTED_FILE_SEPARATOR)
        } else {
            path = file.class.name == 'java.io.File' ? new File(file.getParent()).getCanonicalPath().split(AssetHelper.QUOTED_FILE_SEPARATOR) : new File(file.file.getParent()).getCanonicalPath().split(AssetHelper.QUOTED_FILE_SEPARATOR)
        }
        def startPosition = path.findLastIndexOf{ it == "grails-app" }
        if(startPosition == -1) {
            startPosition = path.findLastIndexOf{ it == 'web-app' }
            if(startPosition +2 >= path.length) {
                return ""
            }
            path = path[(startPosition + 2)..-1]
        }
        else {
            if(startPosition + 3 >= path.length) {
                return ""
            }
            path = path[(startPosition + 3)..-1]
        }
        return path.join(AssetHelper.DIRECTIVE_FILE_SEPARATOR)
    }
    
    private static contextPath() {
        def contextPath = Metadata.current?.'app.context'
        if (!contextPath) {
            contextPath = Holders.getGrailsApplication()?.config?.grails?.app?.context
            if (!contextPath)
                contextPath = Metadata.current?.'app.name' ? "/${Metadata.current.'app.name'}" : "/"
        }
        return (contextPath == '/' ? '' : contextPath) 
    }

}
