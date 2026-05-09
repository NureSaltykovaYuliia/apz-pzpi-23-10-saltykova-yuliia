# config.py

# === API конфігурація ===
# Постійний Localtunnel домен (запускати: lt --port 5000 --subdomain my-dog-space1)
DOMAIN = "my-dog-space1.loca.lt"
API_BASE_URL = "https://{}/api".format(DOMAIN)

# Ендпоінти MyDogSpace
API_SMART_DEVICES = "/SmartDevices"
API_NOTIFICATIONS = "/Notifications"

# === Localtunnel Headers ===
# Обов'язкові заголовки для обходу екрану Localtunnel та роботи з JSON
LT_HEADERS = {
    "Host": DOMAIN,
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    "Bypass-Tunnel-Reminder": "true",
    "Content-Type": "application/json",
    "Accept": "application/json",
    "Connection": "close"
}

# === WiFi конфігурація ===
WIFI_SSID = "Wokwi-GUEST"
WIFI_PASSWORD = ""

# === Пристрій конфігурація (MyDogSpace) ===
# DEVICE_GUID МАЄ ТОЧНО ЗБІГАТИСЯ з тим, що ви вводили при прив'язці в мобільному додатку!
DEVICE_GUID = "ESP32_Wokwi_Yuliia"
DEVICE_NAME = "ESP32_Wokwi_Yuliia"

# ID собаки (буде отримано після призначення)
DOG_ID = None

# === Налаштування сенсорів ===
GPS_UPDATE_INTERVAL = 10
DEFAULT_LATITUDE = 50.4501
DEFAULT_LONGITUDE = 30.5234

# === Гео-зони (Geofencing) ===
DANGER_ZONE = {
    "lat": 50.4501,
    "lon": 30.5234,
    "radius": 100
}

# Моніторинг батареї
BATTERY_CHECK_INTERVAL = 30
BATTERY_LOW_THRESHOLD = 20

# === Налаштування відправки даних ===
DATA_SEND_INTERVAL = 10  # Оптимально для тесту

# === LED індикатори ===
LED_WIFI_PIN = 2
LED_STATUS_PIN = 4

# === Режим debug ===
DEBUG = True