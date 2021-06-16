package id.dicka.shoppingcart

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import id.dicka.shoppingcart.adapter.MyDrinkAdapter
import id.dicka.shoppingcart.eventbus.UpdateCartEvent
import id.dicka.shoppingcart.listener.ICartLoadListener
import id.dicka.shoppingcart.listener.IDrinkLoadListener
import id.dicka.shoppingcart.model.CartModel
import id.dicka.shoppingcart.model.DrinkModel
import id.dicka.shoppingcart.utils.SpaceItemDecoration
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity(), IDrinkLoadListener, ICartLoadListener {

    lateinit var drinkLoadListener: IDrinkLoadListener
    lateinit var cartLoadListener: ICartLoadListener

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().hasSubscriberForEvent(UpdateCartEvent::class.java))
             EventBus.getDefault().removeStickyEvent(UpdateCartEvent::class.java)
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public fun onUpdateCartEvent(event:UpdateCartEvent){
        countCartFromFireBase()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        loadDrinkFromFirebase()
        countCartFromFireBase()
    }

    private fun init(){
        drinkLoadListener = this
        cartLoadListener = this

        val gridLayoutManager = GridLayoutManager(this, 2)
        recyler_drink.layoutManager = gridLayoutManager
        recyler_drink.addItemDecoration(SpaceItemDecoration())
    }

    /** add item to cart **/
    private fun countCartFromFireBase(){
        val cartModels: MutableList<CartModel> = ArrayList()
        FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child("UNIQUE_USER_ID")
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                        cartLoadListener.onLoadCartFailed(error.message)
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (cartSnapshot in snapshot.children){
                            val  cartModel = cartSnapshot.getValue(CartModel::class.java)
                            cartModel!!.key = cartSnapshot.key
                            cartModels.add(cartModel)
                        }
                        cartLoadListener.onLoadCartSuccess(cartModels)
                    }

                })
    }

    /** load data from firebase **/
    private fun loadDrinkFromFirebase(){
        val drinkModels: MutableList<DrinkModel> = ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("Drink")
            .addListenerForSingleValueEvent(object:ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    drinkLoadListener.onLoadFailed(error.message)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (drinkSnapshot in snapshot.children){
                            val drinkModel = drinkSnapshot.getValue(DrinkModel::class.java)
                            drinkModel!!.key = drinkSnapshot.key
                            drinkModels.add(drinkModel)
                        }
                        drinkLoadListener.onLoadSuccess(drinkModels)
                    }else{
                        drinkLoadListener.onLoadFailed("Drink Items not exists")
                    }
                }

            })
    }

    override fun onLoadSuccess(drinkModelList: List<DrinkModel>?) {
        val adapter = MyDrinkAdapter(this, drinkModelList!!, cartLoadListener)
        recyler_drink.adapter = adapter
    }

    override fun onLoadFailed(message: String?) {
        Snackbar.make(mainLayout, message!!, Snackbar.LENGTH_LONG).show()
    }

    override fun onLoadCartSuccess(cartModelList: List<CartModel>) {
        var cartSum = 0
        for (cartModel in cartModelList!!)
            cartSum += cartModel!!.quantity
        badge.setNumber(cartSum)
    }

    override fun onLoadCartFailed(message: String?) {
        Snackbar.make(mainLayout, message!!, Snackbar.LENGTH_LONG).show()
    }


}