import urequests
import ujson
import usocket
import config
from gps_sensor import gps_sensor
from battery_monitor import battery_monitor


class DeviceManager:
    """Клас для управління Smart Device (MyDogSpace)"""

    def __init__(self):
        self.device_id = None
        self.device_guid = config.DEVICE_GUID
        self.dog_id = None
        self.is_registered = False
        self.dog_assigned = False

    def resolve_dns(self, hostname):
        """Примусове вирішення DNS для Wokwi"""
        try:
            addr_info = usocket.getaddrinfo(hostname, 80)
            return addr_info[0][-1][0]
        except Exception as e:
            if config.DEBUG:
                print(f"[DNS] Помилка DNS {hostname}: {e}")
            return None

    def register_device_without_dog(self):
        """
        Реєстрація пристрою в MyDogSpace
        POST /api/SmartDevices/register-device
        """
        url = config.API_BASE_URL + config.API_SMART_DEVICES + "/register-device"

        payload = {
            "deviceGuid": self.device_guid
        }

        print("\n--- Реєстрація пристрою MyDogSpace ---")
        print(f"[DEVICE] URL: {url}")
        print(f"[DEVICE] GUID: {self.device_guid}")

        try:
            response = urequests.post(
                url,
                headers=config.LT_HEADERS,
                data=ujson.dumps(payload),
                timeout=15
            )

            print(f"[DEVICE] Статус реєстрації: {response.status_code}")

            if response.status_code == 200:
                data = response.json()
                print(f"[DEVICE] Відповідь: {data}")

                self.device_id = data.get("id")
                raw_dog_id = data.get("dogId")

                print(f"[DEVICE] device_id={self.device_id}, raw_dog_id={raw_dog_id}, type={type(raw_dog_id)}")

                # Явна перевірка на None і ненульове значення
                if raw_dog_id is not None and raw_dog_id != 0 and raw_dog_id != "null":
                    self.dog_id = int(raw_dog_id)
                    self.dog_assigned = True
                    print(f"[DEVICE] ✓ Пристрій прив'язано до собаки ID={self.dog_id}")
                else:
                    self.dog_assigned = False
                    print(f"[DEVICE] ⚠ Собаку ще не призначено (dogId={raw_dog_id})")

                self.is_registered = True
                print(f"[DEVICE] ✓ Пристрій зареєстровано (ID: {self.device_id})")
                response.close()
                return True
            else:
                body = response.text
                print(f"[DEVICE] ✗ Помилка реєстрації: {response.status_code} — {body}")
                response.close()
                return False

        except Exception as e:
            print(f"[DEVICE] ✗ Виняток при реєстрації: {e}")
            return False

    def send_telemetry(self):
        """Відправка телеметрії в MyDogSpace"""
        if not self.is_registered:
            print("[DEVICE] ✗ Телеметрія: пристрій не зареєстрований")
            return False

        latitude, longitude = gps_sensor.read_coordinates()
        battery_level = battery_monitor.read_battery_level()

        if latitude is None:
            print("[DEVICE] ✗ Телеметрія: GPS недоступний")
            return False

        url = f"{config.API_BASE_URL}{config.API_SMART_DEVICES}/device/{self.device_id}/telemetry"

        # Схема UpdateSmartDeviceDto
        payload = {
            "lastLatitude": latitude,
            "lastLongitude": longitude,
            "batteryLevel": battery_level
        }

        try:
            response = urequests.put(
                url,
                headers=config.LT_HEADERS,
                data=ujson.dumps(payload),
                timeout=10
            )

            if response.status_code == 204:
                print(f"[DEVICE] ✓ Телеметрія відправлена: {latitude:.4f},{longitude:.4f} bat={battery_level:.1f}%")
                response.close()
                return True
            else:
                print(f"[DEVICE] ✗ Помилка телеметрії: {response.status_code}")
                response.close()
                return False
        except Exception as e:
            print(f"[DEVICE] ✗ Виняток телеметрії: {e}")
            return False

    def check_dog_assignment(self):
        """Перевірка призначення собаки в MyDogSpace"""
        url = f"{config.API_BASE_URL}{config.API_SMART_DEVICES}/device/{self.device_guid}/dog"
        print(f"[CHECK] Запит: {url}")

        try:
            response = urequests.get(url, headers=config.LT_HEADERS, timeout=10)
            print(f"[CHECK] Статус: {response.status_code}")

            if response.status_code == 200:
                data = response.json()
                print(f"[CHECK] Дані: {data}")
                raw_dog_id = data.get("dogId")

                if raw_dog_id is not None and raw_dog_id != 0:
                    self.dog_id = int(raw_dog_id)
                    self.dog_assigned = True
                    print(f"[CHECK] ✓ Собака призначена! dog_id={self.dog_id}")
                    response.close()
                    return self.dog_id
                else:
                    print(f"[CHECK] ⚠ dogId={raw_dog_id} — собака не призначена")
            else:
                body = response.text
                print(f"[CHECK] ✗ Відповідь {response.status_code}: {body}")

            response.close()
            return None

        except Exception as e:
            print(f"[CHECK] ✗ Помилка: {e}")
            return None

    def send_notification(self, title, message, notification_type):
        """Відправка сповіщень MyDogSpace"""
        if not self.dog_id:
            return False

        url = f"{config.API_BASE_URL}{config.API_NOTIFICATIONS}/iot-alert"

        payload = {
            "title": title,
            "message": message,
            "notificationType": notification_type,
            "dogId": self.dog_id
        }

        try:
            response = urequests.post(
                url,
                headers=config.LT_HEADERS,
                data=ujson.dumps(payload),
                timeout=10
            )
            success = response.status_code == 201
            response.close()
            return success
        except Exception as e:
            print(f"[NOTIFY] ✗ Помилка: {e}")
            return False


device_manager = DeviceManager()
