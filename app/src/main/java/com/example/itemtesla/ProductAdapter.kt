package com.example.itemtesla

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.itemtesla.R // IMPORTANTE: Verifique se este import está correto para o seu pacote
// e se o 'R' não está em vermelho.

/**
 * Adaptador para o RecyclerView que exibe a lista de produtos.
 *
 * Responsável por criar e gerenciar as visualizações dos itens da lista,
 * e por conectar os dados dos produtos aos elementos de UI. Ele também define
 * os listeners para interações do usuário com os itens individuais.
 *
 * @param products Uma lista mutável de [Product] a ser exibida. Esta lista será
 * atualizada externamente pela Activity (via Room Flow).
 * @param removeItemClickListener Uma função lambda para ser chamada quando o botão "Remover" de um item for clicado.
 * @param addImageClickListener Uma função lambda para ser chamada quando o botão "Adicionar/Mudar Foto" de um item for clicado.
 * @param editItemLongClickListener Uma função lambda para ser chamada quando um item for pressionado longamente para edição.
 */
class ProductAdapter(
    private val products: MutableList<Product>,
    private val removeItemClickListener: (Product) -> Unit,
    private val addImageClickListener: (Product) -> Unit,
    private val editItemLongClickListener: (Product) -> Boolean
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    /**
     * ViewHolder para cada item da lista.
     *
     * Esta classe contém as referências para os elementos de UI (TextViews, ImageView, Buttons)
     * dentro do layout individual de cada item ([item_product.xml]), permitindo que o adaptador
     * acesse e manipule esses elementos de forma eficiente.
     *
     * @param itemView A View raiz do layout do item individual.
     */
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // As referências aos IDs devem corresponder exatamente aos IDs em item_product.xml
        val tvType: TextView = itemView.findViewById(R.id.tvProductType)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvProductQuantity)
        val tvModel: TextView = itemView.findViewById(R.id.tvProductModel)
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val btnAddImage: Button = itemView.findViewById(R.id.btnAddImage)
        val btnRemove: Button = itemView.findViewById(R.id.btnRemoveItem)
    }

    /**
     * Chamado quando o RecyclerView precisa de um novo [ProductViewHolder].
     * Infla o layout [item_product.xml] para criar a visualização de um item individual.
     *
     * @param parent O ViewGroup ao qual a nova View será anexada (o próprio RecyclerView).
     * @param viewType O tipo de visualização da nova View (útil para múltiplos layouts de item).
     * @return Um novo [ProductViewHolder] que contém a View do item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    /**
     * Retorna o número total de itens atualmente na lista de dados do adaptador.
     *
     * O RecyclerView usa este método para determinar quantos itens ele precisa exibir.
     *
     * @return O número de itens na lista de produtos.
     */
    override fun getItemCount(): Int = products.size

    /**
     * Chamado pelo RecyclerView para exibir os dados em uma posição específica.
     * Atualiza o conteúdo do [ProductViewHolder] para refletir o item na posição dada.
     *
     * @param holder O [ProductViewHolder] que deve ser atualizado com os dados do item.
     * @param position A posição do item dentro do conjunto de dados (lista 'products') do adaptador.
     */
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentProduct = products[position]
        holder.tvType.text = "Tipo: ${currentProduct.type}"
        holder.tvQuantity.text = "Quantidade: ${currentProduct.quantity}"
        holder.tvModel.text = "Modelo: ${currentProduct.model}"

        if (currentProduct.imageUrl != null && currentProduct.imageUrl!!.isNotEmpty()) {
            holder.ivProductImage.visibility = View.VISIBLE
            holder.ivProductImage.setImageURI(Uri.parse(currentProduct.imageUrl!!))
        } else {
            holder.ivProductImage.visibility = View.GONE
            holder.ivProductImage.setImageURI(null)
        }

        holder.btnAddImage.setOnClickListener {
            addImageClickListener(currentProduct)
        }

        holder.btnRemove.setOnClickListener {
            removeItemClickListener(currentProduct)
        }

        holder.itemView.setOnLongClickListener {
            editItemLongClickListener(currentProduct)
        }
    }

    /**
     * Remove um produto da lista interna do adaptador e notifica o RecyclerView.
     *
     * @param product O [Product] a ser removido da lista visual.
     */
    fun removeItem(product: Product) {
        val position = products.indexOf(product)
        if (position != -1) {
            products.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * Adiciona um novo produto à lista interna do adaptador e notifica o RecyclerView.
     *
     * @param product O [Product] a ser adicionado à lista visual.
     */
    fun addItem(product: Product) {
        products.add(product)
        notifyItemInserted(products.size - 1)
    }

    /**
     * Atualiza um produto existente na lista interna do adaptador e notifica o RecyclerView.
     *
     * @param updatedProduct O [Product] com os dados atualizados (que já existe na lista).
     */
    fun updateItem(updatedProduct: Product) {
        val position = products.indexOfFirst { it.id == updatedProduct.id }
        if (position != -1) {
            products[position] = updatedProduct
            notifyItemChanged(position)
        }
    }

    /**
     * Atualiza a lista completa de produtos do adaptador com uma nova lista.
     *
     * @param newProducts A nova [List]<[Product]> que o adaptador deve exibir.
     */
    fun updateProductList(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }
}