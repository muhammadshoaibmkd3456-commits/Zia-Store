package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CartItem
import com.example.data.model.DateFilter
import com.example.data.model.ProductEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ZiaStore", appName)
  }

  @Test
  fun `verify cart item calculations`() {
    val product = ProductEntity(
      id = 1,
      name = "Tapal Danedar 475g",
      barcode = "896400100102",
      category = "Kiryana",
      salePrice = 680.0,
      wholesalePrice = 650.0,
      costPrice = 620.0,
      stockQty = 25.0
    )

    val item = CartItem(
      product = product,
      qty = 3.0,
      customPrice = 680.0
    )

    assertEquals(2040.0, item.lineTotal, 0.001)
    assertEquals(1860.0, item.costTotal, 0.001)
  }

  @Test
  fun `verify date filter options exist`() {
    assertTrue(DateFilter.values().any { it.label == "Today" && it.urduLabel == "Aaj" })
    assertTrue(DateFilter.values().any { it.label == "This Month" && it.urduLabel == "Is Maah" })
  }
}
