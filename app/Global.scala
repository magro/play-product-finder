import play.api._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    if(app.mode == Mode.Prod)
      Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    if(app.mode == Mode.Prod)
      Logger.info("Application shutdown...")
  }
}