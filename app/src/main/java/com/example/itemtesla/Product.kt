package com.example.itemtesla

import androidx.room.Entity // Importa a anotação Entity do Room
import androidx.room.PrimaryKey // Importa a anotação PrimaryKey do Room

/**
 * Representa uma entidade de produto no banco de dados Room.
 *
 * Esta data class mapeia para uma tabela chamada "products" no banco de dados.
 * Cada propriedade da classe corresponde a uma coluna na tabela.
 *
 * @property id ID único do produto, servindo como chave primária da tabela.
 * É gerado com UUID para garantir unicidade.
 * @property type O tipo do item (ex: Eletrônico, Ferramenta).
 * @property quantity A quantidade atual do item.
 * @property model O modelo específico ou descrição detalhada do item.
 * @property imageUrl URL da imagem associada ao produto, pode ser nula.
 */
@Entity(tableName = "products") // Anotação que marca esta classe como uma entidade do Room e define o nome da tabela.
data class Product(
    @PrimaryKey // Anotação que marca 'id' como a chave primária da tabela.
    val id: String,
    var type: String,
    var quantity: Int,
    var model: String,
    var imageUrl: String? = null
)