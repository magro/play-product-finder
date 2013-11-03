package models

import org.joda.money.format.MoneyFormatterBuilder
import org.joda.money.Money

/**
 * The product information presented in the public search interface.
 */
case class ProductInfo(val name: String, val price: Money, val imageUrl: String, val detailsUrl: String, val shopName: String) {
  def priceFormatted(implicit lang: play.api.i18n.Lang) = ProductInfo.moneyFormatter.withLocale(lang.toLocale).print(price)
}

object ProductInfo {
  private val moneyFormatter = new MoneyFormatterBuilder().appendAmountLocalized().appendLiteral(" ").appendCurrencySymbolLocalized().toFormatter()
}