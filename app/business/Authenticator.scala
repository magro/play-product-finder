package business

/**
 * Authenticates credentials.
 */
trait Authenticator {
  def authenticate(username: String, password: String): Boolean
}

/**
 * Authenticates credentials against static values.
 */
object StaticAuthenticator extends Authenticator {

  val ValidUsername = "play@example.com"
  val ValidPassword = "secret"

  override def authenticate(username: String, password: String): Boolean = {
    // as simple as possible
    ValidUsername.equals(username) && ValidPassword.equals(password)
  }
}
