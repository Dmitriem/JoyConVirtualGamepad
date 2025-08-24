class JoyConMapperService : Service() {
    private lateinit var virtualGamepad: VirtualGamepadManager
    private lateinit var inputHandler: JoyConInputHandler
    private var isRunning = false
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!RootUtils.isRootAvailable()) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        startForegroundService()
        initializeComponents()
        startJoyConMapping()
        
        return START_STICKY
    }
    
    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun initializeComponents() {
        virtualGamepad = VirtualGamepadManager()
        inputHandler = JoyConInputHandler { event -> handleInputEvent(event) }
        
        virtualGamepad.createVirtualGamepad()
        inputHandler.startListening()
    }
    
    private fun handleInputEvent(event: InputEvent) {
        when (event) {
            is KeyEvent -> virtualGamepad.sendKeyEvent(event)
            is MotionEvent -> virtualGamepad.sendMotionEvent(event)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cleanup()
    }
    
    private fun cleanup() {
        inputHandler.stopListening()
        virtualGamepad.destroy()
        isRunning = false
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}