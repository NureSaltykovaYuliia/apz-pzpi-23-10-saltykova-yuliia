"""
Головний файл IoT пристрою для проекту MyDogSpace (ESP32 Wokwi)
Реалізовано підключення через Localtunnel з підтримкою DNS
"""

import network
import time
from machine import Pin
import config
from device_manager import device_manager
from gps_sensor import gps_sensor
from battery_monitor import battery_monitor
from geofence_monitor import geofence_monitor

# LED індикатори
led_wifi = Pin(config.LED_WIFI_PIN, Pin.OUT)
led_status = Pin(config.LED_STATUS_PIN, Pin.OUT)


def connect_wifi():
    """Підключення до WiFi (Wokwi-GUEST)"""
    print("\n" + "="*50)
    print("MyDogSpace IoT Device - " + config.DEVICE_NAME)
    print("="*50)
    
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)

    if not wlan.isconnected():
        print(f"[WiFi] Підключення до {config.WIFI_SSID}...")
        wlan.connect(config.WIFI_SSID, config.WIFI_PASSWORD)

        while not wlan.isconnected():
            led_wifi.value(not led_wifi.value())
            time.sleep(1)
            print(".", end="")

    led_wifi.value(1)
    print(f"\n[WiFi] ✓ Підключено! IP: {wlan.ifconfig()[0]}")
    return True


def initialize_device():
    """Ініціалізація пристрою в MyDogSpace"""
    print("\n[INIT] Реєстрація Smart Device...")

    if device_manager.register_device_without_dog():
        print(f"[INIT] ✓ Пристрій готовий (ID={device_manager.device_id})")
        print(f"[INIT] dog_id={device_manager.dog_id}, dog_assigned={device_manager.dog_assigned}")
        if device_manager.dog_assigned:
            print(f"[INIT] ✓ Собака вже прив'язана! ID={device_manager.dog_id} — починаємо телеметрію")
        else:
            print("[INIT] ⏳ Собака не прив'язана — чекаємо призначення...")
        return True
    else:
        print("[INIT] ✗ Помилка реєстрації. Перевірте з'єднання з сервером!")
        return False


def main_loop():
    """Головний цикл моніторингу собаки"""
    print("\n" + "="*50)
    print("Запуск моніторингу MyDogSpace")
    print("="*50 + "\n")

    iteration = 0
    while True:
        try:
            iteration += 1
            print(f"\n--- Моніторинг #{iteration} ---")
            led_status.value(not led_status.value())

            # Перевірка чи призначена собака через веб-панель
            if not device_manager.dog_assigned:
                print("[CHECK] Очікування призначення собаки...")
                device_manager.check_dog_assignment()

            # Читання сенсорів (симуляція)
            latitude, longitude = gps_sensor.read_coordinates()
            battery = battery_monitor.read_battery_level()

            print(f"Локація: {latitude:.6f}, {longitude:.6f} | Батарея: {battery:.1f}%")

            # Геозонування та сповіщення
            if device_manager.dog_assigned:
                is_safe, distance = geofence_monitor.check_position(latitude, longitude)
                if not is_safe and not geofence_monitor.last_alert_sent:
                    print("⚠ УВАГА: Собака вийшла за межі безпечної зони!")
                    device_manager.send_notification(
                        "Увага! Геозона", 
                        "Ваша собака покинула безпечну зону!", 
                        "danger_zone"
                    )
                    geofence_monitor.last_alert_sent = True
                elif is_safe:
                    geofence_monitor.last_alert_sent = False

                # Відправка телеметрії на сервер
                device_manager.send_telemetry()
            else:
                print("⏳ Телеметрія не відправляється: собака не призначена")

            time.sleep(config.DATA_SEND_INTERVAL)

        except Exception as e:
            print(f"[ERROR] Помилка циклу: {e}")
            time.sleep(5)


def main():
    """Точка входу"""
    led_wifi.value(0)
    led_status.value(0)

    if connect_wifi():
        if initialize_device():
            main_loop()

if __name__ == "__main__":
    main()
