Play Product Finder - Reactive Showcase
=====================================

This is a Play! Framework 2 sample project that shows reactive (non-blocking) database access and reactive WS handling.

[![Build Status](https://jenkins.inoio.de/job/play-webshop/badge/icon)](http://jenkins.inoio.de/job/play-webshop/)

This project demonstrates:

* End to end async/reactive I/O (REST/WS + DB)
* Using [Activate](http://activate-framework.org/) (Async/Reactive) Persistence
  * Activate database migrations
  * How to use different datases for test/dev
* Frontend tooling
  * Javascript Router
  * RequireJS integration
  * Coffee Script
* CRUD, with table pagination and CRUD forms
* Integrating with a CSS framework (Twitter Bootstrap )
  * Twitter Bootstrap requires a different form layout to the default one that the Play form helper generates, so this application also provides an example of integrating a custom form input constructor.
  * Provides multiple bootstrap themes (powered by [bootswatch](http://bootswatch.com/2/))
* XML/XPath processing using [Scales Xml](https://github.com/chris-twiner/scalesXml)
* Simple security/auth using action composition (and the built in [AuthenticatedBuilder](http://www.playframework.com/documentation/2.2.x/api/scala/index.html#play.api.mvc.Security$$AuthenticatedBuilder$))
