package id.dicka.shoppingcart.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import id.dicka.shoppingcart.R
import id.dicka.shoppingcart.eventbus.UpdateCartEvent
import id.dicka.shoppingcart.listener.ICartLoadListener
import id.dicka.shoppingcart.listener.IRecylerClickListener
import id.dicka.shoppingcart.model.CartModel
import id.dicka.shoppingcart.model.DrinkModel
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

class MyDrinkAdapter(
    private val context: Context,
    private val list:List<DrinkModel>,
    private val cartListener: ICartLoadListener
): RecyclerView.Adapter<MyDrinkAdapter.MyDrinkViewHolder>(){

    class MyDrinkViewHolder(itemView:View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var imageView: ImageView
        var txtName:TextView
        var txtPrice:TextView

        private var clickListener:IRecylerClickListener? = null

        fun setClickListener(clickListener: IRecylerClickListener){
            this.clickListener = clickListener
        }

        init {
            imageView = itemView.findViewById(R.id.imageView) as ImageView
            txtName = itemView.findViewById(R.id.txtName) as TextView
            txtPrice = itemView.findViewById(R.id.txtPrince) as TextView

            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener!!.onItemClickListener(v, adapterPosition)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyDrinkViewHolder {
        return MyDrinkViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_drink_item, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyDrinkViewHolder, position: Int) {
        Glide.with(context)
                .load(list[position].image)
                .into(holder.imageView!!)
        holder.txtName!!.text = StringBuilder().append(list[position].name)
        holder.txtPrice!!.text = StringBuffer().append(list[position].price)

        /** click item **/
        holder.setClickListener(object:IRecylerClickListener{
            override fun onItemClickListener(view: View?, position: Int) {
                addToCart(list[position])
            }
        })
    }

    private fun addToCart(drinkModel: DrinkModel){
        val useCart = FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child("UNIQUE_USER_ID")

        useCart.child(drinkModel.key!!)
                .addListenerForSingleValueEvent(object:ValueEventListener{

                    override fun onCancelled(error: DatabaseError) {
                        cartListener.onLoadCartFailed(error.message)
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            /** if item is already **/
                            val cartModel = snapshot.getValue(CartModel::class.java)
                            val updateData: MutableMap<String, Any> = HashMap()
                            cartModel!!.quantity = cartModel!!.quantity+1
                            updateData["quantity"] = cartModel!!.quantity+1
                            updateData["totalPrice"] = cartModel!!.quantity * cartModel.price!!.toFloat()

                            useCart.child(drinkModel.key!!)
                                    .updateChildren(updateData)
                                    .addOnSuccessListener {
                                        EventBus.getDefault().postSticky(UpdateCartEvent())
                                        cartListener.onLoadCartFailed("Success add to cart")
                                    }
                                    .addOnFailureListener { e-> cartListener.onLoadCartFailed(e.message)}
                        }else{
                            val cartModel = CartModel()
                            cartModel.key = drinkModel.key
                            cartModel.name = drinkModel.name
                            cartModel.price = drinkModel.price
                            cartModel.image = drinkModel.image
                            cartModel.quantity = 1
                            cartModel.totalPrice = drinkModel.price!!.toFloat()

                            useCart.child(drinkModel.key!!)
                                    .setValue(cartModel)
                                    .addOnSuccessListener {
                                        EventBus.getDefault().postSticky(UpdateCartEvent())
                                        cartListener.onLoadCartFailed("Success add to cart")
                                    }
                                    .addOnFailureListener { e-> cartListener.onLoadCartFailed(e.message)}
                        }
                    }

                })
    }

}