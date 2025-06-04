package com.example.itemtesla

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.itemtesla.R

/**
 * [MainActivity] é a tela inicial do aplicativo ItemTesla.
 *
 * Esta Activity contém um único botão "Depósito" que, ao ser clicado,
 * navega para a [ProductsActivity] onde a lista de itens é gerenciada.
 */
class MainActivity : AppCompatActivity() {
    /**
     * Chamado quando a Activity é criada pela primeira vez.
     * Configura o layout da tela e o listener de clique para o botão "Depósito".
     *
     * @param savedInstanceState Se a Activity estiver sendo recriada após ter sido destruída
     * (ex: rotação da tela), este Bundle contém os dados mais recentemente
     * fornecidos em [onSaveInstanceState]. Caso contrário, é nulo.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Define o layout XML para esta Activity.

        // Encontra o botão no layout pelo seu ID.
        val depositButton: Button = findViewById(R.id.depositButton)

        // Define um listener para o clique do botão.
        depositButton.setOnClickListener {
            // Cria um Intent para iniciar a ProductsActivity.
            // Um Intent é uma mensagem que o Android usa para solicitar uma ação de outro componente.
            val intent = Intent(this, ProductsActivity::class.java)
            startActivity(intent) // Inicia a nova Activity.
        }
    }
}