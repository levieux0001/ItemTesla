package com.example.itemtesla

import android.content.Context
import androidx.room.Database // Anotação para marcar a classe como um banco de dados Room
import androidx.room.Room // Para construir a instância do banco de dados
import androidx.room.RoomDatabase // Classe base para o banco de dados Room

/**
 * Classe que representa o banco de dados Room para o aplicativo ItemTesla.
 *
 * Esta classe abstrata define as entidades (tabelas) no banco de dados e fornece
 * o ponto de acesso para os DAOs (Data Access Objects).
 *
 * @property entities Array das classes de entidade que pertencem a este banco de dados (ex: [Product::class]).
 * @property version A versão do esquema do banco de dados. Deve ser incrementada quando o esquema muda.
 * @property exportSchema Define se o esquema deve ser exportado para uma pasta (geralmente 'false' para apps simples).
 */
@Database(entities = [Product::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Retorna a instância do [ProductDao] para interagir com a tabela de produtos.
     * O Room implementa este método automaticamente.
     */
    abstract fun productDao(): ProductDao

    /**
     * Objeto companheiro (similar a um método estático em Java) que fornece
     * uma instância singleton do [AppDatabase].
     *
     * Usar o padrão Singleton garante que haja apenas uma instância do banco de dados
     * rodando, otimizando o uso de recursos.
     */
    companion object {
        @Volatile // Garante que a escrita para esta variável seja imediatamente visível para outros threads.
        private var INSTANCE: AppDatabase? = null

        /**
         * Retorna a instância única do banco de dados [AppDatabase].
         * Se a instância ainda não existir, ela será criada.
         *
         * @param context O contexto da aplicação para criar o banco de dados.
         * @return A instância singleton de [AppDatabase].
         */
        fun getDatabase(context: Context): AppDatabase {
            // Se a instância já existe, retorna ela.
            // Caso contrário, entra em um bloco sincronizado para criar a instância.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Contexto da aplicação.
                    AppDatabase::class.java, // Classe do banco de dados.
                    "item_tesla_database" // Nome do arquivo do banco de dados no dispositivo.
                )
                    // .fallbackToDestructiveMigration() // Opcional: Se a versão do banco mudar, destroi e recria o banco. Use com cautela!
                    .build()
                INSTANCE = instance // Armazena a instância criada.
                instance
            }
        }
    }
}