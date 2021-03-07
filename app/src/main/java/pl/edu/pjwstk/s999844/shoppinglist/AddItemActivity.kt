package pl.edu.pjwstk.s999844.shoppinglist

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_add_item.*
import pl.edu.pjwstk.s999844.shoppinglist.dal.ShoppingListDao
import pl.edu.pjwstk.s999844.shoppinglist.dal.ShoppingListDatabase
import pl.edu.pjwstk.s999844.shoppinglist.models.RequiredItem
import java.util.*

class AddItemActivity : AppCompatActivity() {
	private val inputMethodManager: InputMethodManager by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

	private val currentLocale: Locale
		get() = baseContext.resources.configuration.locales.get(0)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_add_item)
	}

	override fun onStart() {
		super.onStart()

		// german-speaking people capitalize nouns - as a shopping list usually contains nouns we capitalize the first letter automatically. They can still use lowercase manually if they need a pronoun
		when (currentLocale.language) {
			Locale.GERMAN.language -> addItemNameInput.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or addItemNameInput.inputType
		}
		addItemNameInput.requestFocus()

		title = getString(R.string.addTitleBarText)
	}

	@Suppress("UNUSED_PARAMETER")
	fun addItem(view: View) {
		inputMethodManager.hideSoftInputFromWindow(addItemNameInput.windowToken, 0)

		val name: String = addItemNameInput.text.toString().trim()
		if (name.isEmpty()) {
			warnError(getString(R.string.addNameIsEmptyMessage))
			return
		}

		val amountString = addItemAmountInput.text.toString().trim()
		val amount: Int = if (amountString.isEmpty() || amountString.isBlank()) {
			1
		} else {
			try {
				amountString.toInt()
			} catch (e: NumberFormatException) {
				warnError(getString(R.string.addAmountNotANumberMessage))
				return
			}
		}

		if (amount <= 0) {
			warnError(getString(R.string.addAmountTooSmallMessage))
			return
		}

		val shoppingListDao: ShoppingListDao = ShoppingListDatabase.getInstance(applicationContext).getShoppingListDao();

		if (shoppingListDao.findByNameLike(name).isNotEmpty()) {
			warnError(getString(R.string.addItemAlreadyExistsMessage))
			return
		}

		val item = RequiredItem(name, amount)
		shoppingListDao.add(item)

		finish()
	}

	private fun warnError(message: String) = Snackbar.make(saveButton, message, Snackbar.LENGTH_SHORT).show()
}