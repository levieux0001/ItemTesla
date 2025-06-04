package com.example.itemtesla

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

/**
 * [ProductsActivity] é a tela que exibe e gerencia a lista de itens do depósito.
 *
 * Permite ao usuário visualizar, adicionar, remover e editar a quantidade de itens,
 * além de anexar fotos a eles através da câmera ou galeria.
 * Os dados são **persistidos em um banco de dados Room** nesta versão,
 * garantindo que não se percam ao fechar o aplicativo.
 */
class ProductsActivity : AppCompatActivity() { // Abre a classe ProductsActivity

    private lateinit var productAdapter: ProductAdapter
    private lateinit var productDao: ProductDao

    private var currentProductForImage: Product? = null
    private var latestTmpUri: Uri? = null

    /**
     * Launcher para solicitar múltiplas permissões em tempo de execução (Android 6.0+).
     * O resultado das permissões é tratado pela função [handlePermissionsResult].
     */
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        handlePermissionsResult(permissions)
    }

    /**
     * Launcher para capturar uma imagem usando o aplicativo da câmera.
     * O resultado (sucesso e URI da imagem) é tratado no callback.
     */
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            latestTmpUri?.let { uri ->
                currentProductForImage?.imageUrl = uri.toString()
                currentProductForImage?.let { product ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        productDao.updateProduct(product)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Foto não capturada.", Toast.LENGTH_SHORT).show()
        }
        currentProductForImage = null
        latestTmpUri = null
    }

    /**
     * Launcher para selecionar uma imagem da galeria do dispositivo.
     * O resultado (URI da imagem selecionada) é tratado no callback.
     */
    private val selectPictureLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            currentProductForImage?.imageUrl = it.toString()
            currentProductForImage?.let { product ->
                lifecycleScope.launch(Dispatchers.IO) {
                    productDao.updateProduct(product)
                }
            }
        } ?: Toast.makeText(this, "Nenhuma imagem selecionada.", Toast.LENGTH_SHORT).show()
        currentProductForImage = null
    }

    /**
     * Chamado quando a Activity é criada pela primeira vez.
     * Configura o layout, a ActionBar, o RecyclerView, os adaptadores e os listeners.
     *
     * @param savedInstanceState Se a Activity estiver sendo recriada após ter sido destruída,
     * este Bundle contém os dados mais recentemente fornecidos em [onSaveInstanceState].
     * Caso contrário, é nulo.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Itens no seu depósito"

        productDao = AppDatabase.getDatabase(applicationContext).productDao()

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        productAdapter = ProductAdapter(
            mutableListOf(),
            removeItemClickListener = { productToRemove ->
                removeItem(productToRemove)
            },
            addImageClickListener = { productToUpdate ->
                currentProductForImage = productToUpdate
                checkAndRequestPermissions()
            },
            editItemLongClickListener = { productToEdit ->
                showEditQuantityDialog(productToEdit)
                true
            }
        )
        recyclerView.adapter = productAdapter

        lifecycleScope.launch {
            productDao.getAllProducts().collectLatest { products ->
                productAdapter.updateProductList(products)
            }
        }

        setupInitialProducts()

        val fabAddProduct: FloatingActionButton = findViewById(R.id.fabAddProduct)
        fabAddProduct.setOnClickListener {
            showAddProductDialog()
        }
    }

    /**
     * Chamado quando o botão de voltar na ActionBar é clicado.
     * Simula o comportamento do botão de voltar do sistema.
     *
     * @return true para indicar que o evento foi tratado.
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    /**
     * Popula o banco de dados com alguns produtos iniciais se ele estiver vazio.
     * As operações de banco de dados são executadas em uma corrotina em [Dispatchers.IO].
     */
    private fun setupInitialProducts() {
        lifecycleScope.launch(Dispatchers.IO) {
            val currentProducts = productDao.getAllProducts().firstOrNull()
            if (currentProducts.isNullOrEmpty()) {
                productDao.insertProduct(Product(UUID.randomUUID().toString(), "Eletrônico", 5, "Smartphone X"))
                productDao.insertProduct(Product(UUID.randomUUID().toString(), "Ferramenta", 2, "Chave de Fenda Philips"))
                productDao.insertProduct(Product(UUID.randomUUID().toString(), "Mobília", 1, "Cadeira Ergonômica"))
            }
        }
    }

    /**
     * Exibe um diálogo para o usuário adicionar um novo item ao depósito.
     * Permite inserir Tipo, Quantidade e Modelo.
     * A adição é feita no banco de dados via DAO.
     */
    private fun showAddProductDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Adicionar Novo Item")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val inputType = EditText(this)
        inputType.hint = "Tipo (ex: Eletrônico)"
        layout.addView(inputType)

        val inputQuantity = EditText(this)
        inputQuantity.hint = "Quantidade"
        inputQuantity.inputType = InputType.TYPE_CLASS_NUMBER
        layout.addView(inputQuantity)

        val inputModel = EditText(this)
        inputModel.hint = "Modelo (ex: Smartphone X)"
        layout.addView(inputModel)

        builder.setView(layout)

        builder.setPositiveButton("Adicionar") { dialog, _ ->
            val type = inputType.text.toString().trim()
            val quantityStr = inputQuantity.text.toString().trim()
            val model = inputModel.text.toString().trim()

            if (type.isNotEmpty() && quantityStr.isNotEmpty() && model.isNotEmpty()) {
                try {
                    val quantity = quantityStr.toInt()
                    val newProduct = Product(UUID.randomUUID().toString(), type, quantity, model, null)
                    lifecycleScope.launch(Dispatchers.IO) {
                        productDao.insertProduct(newProduct)
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Quantidade deve ser um número.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    /**
     * Exibe um diálogo de confirmação antes de remover um item.
     * A remoção é feita no banco de dados via DAO.
     *
     * @param product O [Product] a ser removido.
     */
    private fun removeItem(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Remover Item")
            .setMessage("Tem certeza que deseja remover o item '${product.type} - ${product.model}'?")
            .setPositiveButton("Sim") { dialog, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    productDao.deleteProduct(product)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Não") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    /**
     * Exibe um diálogo para editar a quantidade de um produto existente.
     * A atualização é feita no banco de dados via DAO.
     *
     * @param product O [Product] cuja quantidade será editada.
     */
    private fun showEditQuantityDialog(product: Product) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Quantidade")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setText(product.quantity.toString())
        input.setSelection(input.text.length)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)
        layout.addView(input)

        builder.setView(layout)

        builder.setPositiveButton("Salvar") { dialog, _ ->
            val newQuantityStr = input.text.toString().trim()
            if (newQuantityStr.isNotEmpty()) {
                try {
                    val newQuantity = newQuantityStr.toInt()
                    val updatedProduct = product.copy(quantity = newQuantity)
                    lifecycleScope.launch(Dispatchers.IO) {
                        productDao.updateProduct(updatedProduct)
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Quantidade inválida.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "A quantidade não pode ser vazia.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    // --- Métodos de Gerenciamento de Permissões e Imagem (Funções auxiliares para fotos) ---

    /**
     * Verifica e solicita as permissões necessárias para acessar a câmera e o armazenamento.
     * As permissões são diferentes para Android 13+ e versões anteriores para melhor compatibilidade.
     */
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Para Android 13 (API 33) e superior, a permissão READ_MEDIA_IMAGES é usada para imagens.
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            // Para Android 12 e anteriores, a permissão READ_EXTERNAL_STORAGE é usada para acesso geral ao armazenamento.
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        // A permissão para a câmera é universal para todas as versões do Android que suportam o aplicativo.
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.CAMERA)
        }

        // Se houver permissões pendentes, o launcher solicita ao usuário.
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            // Se todas as permissões já foram concedidas, prossegue para o diálogo de escolha de imagem.
            showImagePickerDialog()
        }
    }

    /**
     * Lida com o resultado da solicitação de permissões de câmera e armazenamento.
     *
     * @param permissions Um mapa onde a chave é o nome da permissão e o valor é true se concedida, false caso contrário.
     */
    private fun handlePermissionsResult(permissions: Map<String, Boolean>) {
        // Verifica se todas as permissões essenciais foram concedidas.
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // Se todas as permissões foram concedidas, mostra o diálogo de escolha de imagem.
            showImagePickerDialog()
        } else {
            // Se alguma permissão necessária foi negada, informa ao usuário.
            Toast.makeText(this, "Permissões de câmera e/ou armazenamento são necessárias para adicionar fotos.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Mostra um diálogo para o usuário escolher entre tirar uma foto com a câmera ou selecionar uma da galeria.
     */
    private fun showImagePickerDialog() {
        val options = arrayOf("Tirar Foto", "Escolher da Galeria")
        AlertDialog.Builder(this)
            .setTitle("Adicionar Imagem")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> dispatchTakePictureIntent() // Inicia a câmera.
                    1 -> dispatchSelectPictureIntent() // Inicia o seletor de galeria.
                }
            }
            .setNegativeButton("Cancelar", null) // Botão de cancelar o diálogo.
            .show() // Exibe o diálogo.
    }

    /**
     * Lança uma Intent para capturar uma foto usando o aplicativo da câmera padrão do dispositivo.
     * A foto será salva em um arquivo temporário acessível via [FileProvider].
     */
    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            createImageFile() // Tenta criar um arquivo para a imagem no armazenamento do aplicativo.
        } catch (ex: Exception) {
            Toast.makeText(this, "Erro ao criar arquivo de imagem: ${ex.message}", Toast.LENGTH_LONG).show()
            null
        }
        // Se o arquivo foi criado com sucesso, obtém a URI segura e lança a câmera.
        photoFile?.also {
            // Obtém uma URI segura para o arquivo usando FileProvider.
            latestTmpUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider", // O authority deve corresponder ao AndroidManifest.xml.
                it
            )
            // Lança a câmera e passa a URI para que a foto seja salva neste local.
            takePictureLauncher.launch(latestTmpUri!!)
        }
    }

    /**
     * Cria um arquivo de imagem temporário com um nome único (baseado no carimbo de data/hora)
     * dentro do diretório de fotos privado do aplicativo no armazenamento externo.
     *
     * @return O objeto [File] criado, representando o local onde a imagem será salva.
     */
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        // `getExternalFilesDir` fornece um diretório específico para arquivos do aplicativo,
        // que são excluídos quando o app é desinstalado.
        val storageDir: File? = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* Prefixo do nome do arquivo. */
            ".jpg", /* Sufixo do nome do arquivo. */
            storageDir /* Diretório onde o arquivo será salvo. */
        )
    }

    /**
     * Lança uma Intent para selecionar uma imagem da galeria do dispositivo.
     * A imagem selecionada será retornada como uma URI de conteúdo.
     */
    private fun dispatchSelectPictureIntent() {
        // Solicita ao sistema para abrir o seletor de conteúdo (galeria) para imagens de qualquer tipo.
        selectPictureLauncher.launch("image/*")
    }
} // Fecha a classe ProductsActivity