package id.dicka.shoppingcart.listener

import id.dicka.shoppingcart.model.DrinkModel

interface IDrinkLoadListener {

    fun onLoadSuccess(drinkModelList: List<DrinkModel>?)
    fun onLoadFailed(message:String?)
}