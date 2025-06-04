package com.example.itemtesla

import androidx.room.Dao // Anotação para marcar a interface como um DAO
import androidx.room.Delete // Anotação para operação de exclusão
import androidx.room.Insert // Anotação para operação de inserção
import androidx.room.OnConflictStrategy // Estratégia de resolução de conflitos em inserções
import androidx.room.Query // Anotação para consultas SQL personalizadas
import androidx.room.Update // Anotação para operação de atualização
import kotlinx.coroutines.flow.Flow // Usado para observar mudanças nos dados do banco em tempo real

/**
 * Data Access Object (DAO) para a entidade [Product].
 *
 * Define os métodos abstratos que o Room Database implementará para interagir
 * com a tabela 'products' no banco de dados.
 */
@Dao
interface ProductDao {

    /**
     * Retorna todos os produtos do banco de dados, ordenados pelo tipo.
     *
     * O retorno [Flow]<[List]<[Product]>> permite observar mudanças nos dados em tempo real.
     * Sempre que um produto é adicionado, removido ou atualizado, o Flow emitirá uma nova lista.
     */
    @Query("SELECT * FROM products ORDER BY type ASC")
    fun getAllProducts(): Flow<List<Product>>

    /**
     * Insere um novo produto no banco de dados.
     * Se um produto com o mesmo ID já existir, ele será substituído.
     *
     * @param product O [Product] a ser inserido.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product) // 'suspend' indica que esta é uma função de corrotina (assíncrona).

    /**
     * Atualiza um produto existente no banco de dados.
     *
     * @param product O [Product] com os dados atualizados.
     */
    @Update
    suspend fun updateProduct(product: Product)

    /**
     * Exclui um produto do banco de dados.
     *
     * @param product O [Product] a ser excluído.
     */
    @Delete
    suspend fun deleteProduct(product: Product)
}