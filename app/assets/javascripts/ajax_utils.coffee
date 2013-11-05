###
Performs DOM replacements according to the given json.
Expects json like
<code>
{
  "htmlBySelector" : {
    "title": '<title>Some title</title>',
    "#someId": '<div id="someId">...</div>',
    "#anotherId.text": '<div id="anotherId">text to change</div>'
  }
}
</code>
The keys of htmlBySelector are jquery selectors. The values
are the replacement HTML. If the key ends with ".text" the
value will be treated as replacement text.
###
define ['jquery'], ($) ->
  updateHtmlBySelector = (json) ->
    selector = undefined
    if json.htmlBySelector?
      for selector of json.htmlBySelector
        content = json.htmlBySelector[selector]
        if selector.indexOf(".text") isnt -1
          selector = selector.substr(0, selector.indexOf(".text"))
          # console.log "updating text", selector, content
          $(selector).text content
        else
          # console.log "updating html", selector, content
          $(selector).replaceWith content
  return updateHtmlBySelector: updateHtmlBySelector