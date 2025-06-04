package com.example.itemtesla

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.itemtesla.R

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
    // A lista de produtos agora será atualizada pelo Flow do Room vindo da Activity.
    // Ela é mutable para que possamos limpar e adicionar novos dados.
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
        val tvType: TextView = itemView.findViewById(R.id.tvProductType)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvProductQuantity)
        val tvModel: TextView = itemView.findViewById(R.id.tvProductModel)
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val btnAddImage: Button = itemView.findViewById(R.id.btnAddImage)
        val btnRemove: Button = itemView.findViewById(R.id.btnRemoveItem)
    }

    /**
     * Chamado quando o RecyclerView precisa de um novo [ProductViewHolder].
     *
     * Infla o layout [item_product.xml] para criar a visualização de um item individual.
     * Este método é chamado apenas o número de vezes necessário para preencher a tela,
     * e os ViewHolders são reutilizados para melhorar a performance de rolagem.
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
     *
     * Este método é responsável por atualizar o conteúdo de um [ProductViewHolder]
     * (preencher TextViews, carregar imagens, configurar listeners de cliques)
     * para refletir os dados do item na posição dada.
     *
     * @param holder O [ProductViewHolder] que deve ser atualizado com os dados do item.
     * @param position A posição do item dentro do conjunto de dados (lista 'products') do adaptador.
     */
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentProduct = products[position] // Obtém o produto atual para esta posição.

        // Atualiza os TextViews com os dados do produto.
        holder.tvType.text = "Tipo: ${currentProduct.type}"
        holder.tvQuantity.text = "Quantidade: ${currentProduct.quantity}"
        holder.tvModel.text = "Modelo: ${currentProduct.model}"

        // Lógica para exibir a imagem do produto, se houver.
        if (currentProduct.imageUrl != null && currentProduct.imageUrl!!.isNotEmpty()) {
            holder.ivProductImage.visibility = View.VISIBLE // Torna a ImageView visível.
            // Carrega a imagem a partir da URI (String) e a exibe na ImageView.
            // `Uri.parse()` converte a String URI para um objeto Uri, e `!!` afirma que não é nulo.
            holder.ivProductImage.setImageURI(Uri.parse(currentProduct.imageUrl!!))
        } else {
            holder.ivProductImage.visibility = View.GONE // Esconde a ImageView se não houver URL de imagem.
            holder.ivProductImage.setImageURI(null) // Limpa qualquer imagem anterior para evitar reutilização errada.
        }

        // Configura o listener de clique para o botão "Adicionar/Mudar Foto".
        holder.btnAddImage.setOnClickListener {
            // Invoca a lambda fornecida no construtor do adaptador, passando o produto atual.
            addImageClickListener(currentProduct)
        }

        // Configura o listener de clique para o botão "Remover".
        holder.btnRemove.setOnClickListener {
            // Invoca a lambda fornecida no construtor do adaptador, passando o produto a ser removido.
            removeItemClickListener(currentProduct)
        }

        // Configura o listener de CLIQUE LONGO para a View raiz do item (CardView).
        // Isso permite que o usuário edite o item ao pressionar e segurar.
        holder.itemView.setOnLongClickListener {
            // Invoca a lambda fornecida no construtor do adaptador, passando o produto a ser editado.
            // Retorna `true` para indicar que o evento de clique longo foi consumido e não deve ser propagado.
            editItemLongClickListener(currentProduct)
        }
    }

    /**
     * Remove um produto da lista interna do adaptador e notifica o RecyclerView.
     *
     * Nota: A remoção real do banco de dados é feita na Activity via DAO.
     * Esta função é primariamente para a consistência visual imediata do adaptador.
     * O Flow do Room na Activity irá eventualmente chamar [updateProductList]
     * para uma atualização completa da lista.
     *
     * @param product O [Product] a ser removido da lista visual.
     */
    fun removeItem(product: Product) {
        val position = products.indexOf(product) // Encontra a posição do produto na lista atual.
        if (position != -1) {
            products.removeAt(position) // Remove o produto da lista interna.
            notifyItemRemoved(position) // Notifica o RecyclerView que um item foi removido nesta posição, para uma animação suave.
        }
    }

    /**
     * Adiciona um novo produto à lista interna do adaptador e notifica o RecyclerView.
     *
     * Nota: A adição real ao banco de dados é feita na Activity via DAO.
     * Esta função é primariamente para a consistência visual imediata do adaptador.
     * O Flow do Room na Activity irá eventualmente chamar [updateProductList]
     * para uma atualização completa da lista.
     *
     * @param product O [Product] a ser adicionado à lista visual.
     */
    fun addItem(product: Product) {
        products.add(product) // Adiciona o produto à lista interna.
        // Notifica o RecyclerView que um item foi inserido na última posição, para uma animação suave.
        notifyItemInserted(products.size - 1)
    }

    /**
     * Atualiza um produto existente na lista interna do adaptador e notifica o RecyclerView.
     *
     * Nota: A atualização real do banco de dados é feita na Activity via DAO.
     * Esta função é primariamente para a consistência visual imediata do adaptador.
     * O Flow do Room na Activity irá eventualmente chamar [updateProductList]
     * para uma atualização completa da lista.
     *
     * @param updatedProduct O [Product] com os dados atualizados (que já existe na lista).
     */
    fun updateItem(updatedProduct: Product) {
        // Encontra a posição do produto com o mesmo ID na lista atual.
        val position = products.indexOfFirst { it.id == updatedProduct.id }
        if (position != -1) {
            products[position] = updatedProduct // Atualiza o objeto Product na lista interna.
            // Notifica o RecyclerView que o item nesta posição foi alterado, para redesenhar apenas ele.
            notifyItemChanged(position)
        }
    }

    /**
     * Atualiza a lista completa de produtos do adaptador com uma nova lista.
     *
     * Este método é chamado pela [ProductsActivity] (que está observando o [Flow] do Room)
     * sempre que os dados no banco de dados mudam (inserção, atualização, remoção).
     * Ele garante que o [RecyclerView] reflita o estado mais recente do banco de dados.
     *
     * @param newProducts A nova [List]<[Product]> que o adaptador deve exibir.
     */
    fun updateProductList(newProducts: List<Product>) {
        products.clear() // Limpa a lista atual do adaptador.
        products.addAll(newProducts) // Adiciona todos os novos produtos recebidos do banco de dados.
        // Notifica o RecyclerView que todo o conjunto de dados mudou, forçando um redesenho completo.
        // Para listas muito grandes, considerar usar `DiffUtil` para atualizações mais eficientes.
        notifyDataSetChanged()
    }
}