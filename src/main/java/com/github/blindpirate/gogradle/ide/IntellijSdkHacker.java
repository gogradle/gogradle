package com.github.blindpirate.gogradle.ide;

import com.github.blindpirate.gogradle.crossplatform.Os;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;

@SuppressWarnings("checkstyle:linelength")
@Singleton
public class IntellijSdkHacker {
    private static final String GO_SDK = "Go SDK";
    private static final Map<String, List<String>> PRODUCTS = ImmutableMap
            .<String, List<String>>builder()
            .put("IntelliJIdea", asList("2016.1", "2016.2", "2016.3"))
            .put("IdeaIC", asList("2016.1", "2016.2", "2016.3"))
            .put("WebStorm", asList("2016.1", "2016.2", "2016.3"))
            .put("PhpStorm", asList("2016.1", "2016.2", "2016.3"))
            .put("PyCharm", asList("2016.1", "2016.2", "2016.3"))
            .put("RubyMine", asList("2016.1", "2016.2", "2016.3"))
            .put("CLion", asList("2016.1", "2016.2", "2016.3"))
            //.put("Android Studio", asList())
            .build();

    // See https://intellij-support.jetbrains.com/hc/en-us/articles/206544519-Directories-used-by-the-IDE-to-store-settings-caches-plugins-and-logs
    private static final String SETTING_LOCATION_ON_MAC = "${userHome}/Library/Preferences/${product}${version}/options/jdk.table.xml";
    private static final String SETTING_LOCATION_ON_OTHER_OS = "${userHome}/.${product}${version}/options/jdk.table.xml";

    private static final Logger LOGGER = Logging.getLogger(IntellijSdkHacker.class);

    private List<File> getExistentFiles() {
        return PRODUCTS.entrySet().stream()
                .map(entry -> asList(asList(entry.getKey()), entry.getValue()))
                // [[Idea, 2016.1][Idea,2016.2],[Idea,2016.3]]
                .map(Lists::cartesianProduct)
                // [<Idea,2016.1>,<Idea,2016.2>,<Idea,2016.3>]
                .map(this::listListToPairList)
                // [[<Idea,2016.1>,<Idea,2016.2>,<Idea,2016.3>],[<WebStorm,2016.1>, ... ], ...]
                .collect(Collectors.toList())
                .stream()
                // flatten: [<Idea,2016.1>,<Idea,2016.2>,...]
                .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll)
                .stream()
                .map(obj -> (Pair<String, String>) obj)
                .map(this::ofProductAndVersion)
                .filter(File::exists)
                .collect(Collectors.toList());
    }

    public void ensureSpecificSdkExist(String version, Path goroot) {
        getExistentFiles().forEach(file -> ensureSpecificSdkExist(version, goroot, file));
    }

    private void ensureSpecificSdkExist(String version, Path goroot, File file) {
        Document jdkTable = parseDocument(IOUtils.toString(file));
        Node component = child(child(jdkTable, "application"), "component");

        NodeList sdks = component.getChildNodes();
        Optional<Node> resultGoSdk = findGoSdkWithSpecificVersion(sdks, version);
        if (!resultGoSdk.isPresent()) {
            Node newNode = createSdkNode(jdkTable, version, goroot);
            component.appendChild(newNode);
            writeXmlToFile(jdkTable, file);
            LOGGER.quiet("Added Go SDK {} to {}, you need to restart the IDE to make it come into effect",
                    version, file.getAbsolutePath());
        }
    }

    private void writeXmlToFile(Document document, File file) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(document), new StreamResult(file));
        } catch (TransformerException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }


    private Document parseDocument(String xml) {
        try {
            InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    private Node createSdkNode(Document document, String version, Path goroot) {
        String gorootSrc = goroot.resolve("src").toAbsolutePath().toString();

        Element jdk = createElement(document, "jdk", "version", "2");
        jdk.appendChild(createElement(document, "name", "value", "Go " + version));
        jdk.appendChild(createElement(document, "type", "value", "Go SDK"));
        jdk.appendChild(createElement(document, "version", "value", version));
        jdk.appendChild(createElement(document, "homePath", "value", goroot.toAbsolutePath().toString()));

        Element roots = document.createElement("roots");

        Element annotationsPath = document.createElement("annotationsPath");
        annotationsPath.appendChild(createElement(document, "root", "type", "composite"));
        roots.appendChild(annotationsPath);

        Element classPath = document.createElement("classPath");
        Element classPathRoot = createElement(document, "root", "type", "composite");
        classPathRoot.appendChild(createElement(document, "root", "type", "simple", "url", "file://" + gorootSrc));
        classPath.appendChild(classPathRoot);

        Element javadocPath = document.createElement("javadocPath");
        javadocPath.appendChild(createElement(document, "root", "type", "composite"));

        Element sourcePath = document.createElement("sourcePath");
        Element sourcePathRoot = createElement(document, "root", "type", "composite");
        sourcePathRoot.appendChild(createElement(document, "root", "type", "simple", "url", "file://" + gorootSrc));
        sourcePath.appendChild(sourcePathRoot);

        roots.appendChild(annotationsPath);
        roots.appendChild(classPath);
        roots.appendChild(javadocPath);
        roots.appendChild(sourcePath);

        jdk.appendChild(roots);
        jdk.appendChild(document.createElement("additional"));

        return jdk;
    }

    private Element createElement(Document doc, String tagName, String attrName, String attrValue) {
        Element ret = doc.createElement(tagName);
        ret.setAttribute(attrName, attrValue);
        return ret;
    }

    private Element createElement(Document doc, String tagName, String attrName1, String attrValue1,
                                  String attrName2, String attrValue2) {
        Element ret = doc.createElement(tagName);
        ret.setAttribute(attrName1, attrValue1);
        ret.setAttribute(attrName2, attrValue2);
        return ret;
    }

    private Optional<Node> findGoSdkWithSpecificVersion(NodeList jdkList, String specificVersion) {
        /*
        <jdk version="2">
      <name value="Go 1.7.1" />
      <type value="Go SDK" />
      <version value="1.7.1" />
      <homePath value="/usr/local/Cellar/go/1.7.1/libexec" />
      <roots>
        <annotationsPath>
          <root type="composite" />
        </annotationsPath>
        <classPath>
          <root type="composite">
            <root type="simple" url="file:///usr/local/Cellar/go/1.7.1/libexec/src" />
          </root>
        </classPath>
        <javadocPath>
          <root type="composite" />
        </javadocPath>
        <sourcePath>
          <root type="composite">
            <root type="simple" url="file:///usr/local/Cellar/go/1.7.1/libexec/src" />
          </root>
        </sourcePath>
      </roots>
      <additional />
    </jdk>
         */
        for (int i = 0; i < jdkList.getLength(); ++i) {
            Node node = jdkList.item(i);
            if (!(node instanceof Element)) {
                continue;
            }
            Assert.isTrue("2".equals(attr(node, "version")), "Only version 2 is supported!");
            Node type = child(node, "type");
            Node version = child(node, "version");
            if (GO_SDK.equals(attr(type, "value"))
                    && specificVersion.equals(attr(version, "value"))) {
                return Optional.of(node);
            }
        }
        return Optional.empty();
    }

    private Node child(Node node, String name) {
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element && (name.equals(child.getNodeName()))) {
                return child;
            }
        }
        return null;
    }

    private String attr(Node node, String attr) {
        return Element.class.cast(node).getAttribute(attr);
    }

    private File ofProductAndVersion(Pair<String, String> productAndVersion) {
        String locationTemplate = determineLocation();
        String realPath = StringUtils.render(locationTemplate,
                of("userHome", System.getProperty("user.home"),
                        "product", productAndVersion.getLeft(),
                        "version", productAndVersion.getRight()));
        return new File(realPath);
    }

    private String determineLocation() {
        if (Os.getHostOs() == Os.DARWIN) {
            return SETTING_LOCATION_ON_MAC;
        } else {
            return SETTING_LOCATION_ON_OTHER_OS;
        }
    }

    private List<Pair<String, String>> listListToPairList(List<List<String>> listList) {
        return listList.stream().map(list -> Pair.of(list.get(0), list.get(1))).collect(Collectors.toList());
    }
}
