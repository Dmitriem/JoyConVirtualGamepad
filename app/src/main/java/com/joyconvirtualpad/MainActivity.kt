class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val serviceIntent by lazy { Intent(this, JoyConMapperService::class.java) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUi()
        checkRootAccess()
    }
    
    private fun setupUi() {
        binding.switchEnable.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (RootUtils.isRootAvailable()) {
                    startService(serviceIntent)
                } else {
                    showRootRequiredDialog()
                }
            } else {
                stopService(serviceIntent)
            }
        }
    }
    
    private fun checkRootAccess() {
        binding.tvStatus.text = if (RootUtils.isRootAvailable()) {
            "Root доступ получен ✓"
        } else {
            "Требуется root доступ ⚠️"
        }
    }
    
    private fun showRootRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("Требуется Root")
            .setMessage("Для работы приложения необходим root доступ")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}