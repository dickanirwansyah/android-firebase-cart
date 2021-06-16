package id.dicka.shoppingcart.listener

import id.dicka.shoppingcart.model.CartModel

interface ICartLoadListener {
    fun onLoadCartSuccess(cartModelList: List<CartModel>)
    fun onLoadCartFailed(message:String?)
}