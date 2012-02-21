var elements = document.getElementsByTagName("pre");
for (var i = 0; i < elements.length; i++) {
    if (elements[i].parentElement.className.indexOf("brush") == 0) {
        elements[i].className = "brush: xml;";
    }
}

SyntaxHighlighter.defaults['gutter'] = false;
SyntaxHighlighter.defaults['toolbar'] = false;
SyntaxHighlighter.all();
