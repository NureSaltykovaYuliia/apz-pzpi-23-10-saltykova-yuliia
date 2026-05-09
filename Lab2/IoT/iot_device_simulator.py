import requests
import time
import uuid
import sys

class IoTDeviceSimulator:
    def __init__(self, api_url, device_guid=None):
        self.api_url = api_url.rstrip('/')
        self.device_guid = device_guid or "SIM-DOG-001"
        self.token = None
        self.dog_id = None

    def register_device(self):
        """Регистрация устройства в базе данных"""
        print(f"[INFO] Регистрация устройства с GUID: {self.device_guid}")

        try:
            response = requests.post(
                f"{self.api_url}/api/smartdevices/register-device",
                json={"deviceGuid": self.device_guid},
                headers={"Content-Type": "application/json"}
            )

            if response.status_code == 200:
                data = response.json()
                device_info = data.get("device", {})
                self.token = data.get("token")
                print(f"[SUCCESS] Устройство успешно зарегистрировано/авторизовано!")
                print(f"[INFO] ID устройства в БД: {device_info.get('id')}")
                print(f"[INFO] Token: {self.token[:20]}...")
                return True
            else:
                print(f"[ERROR] Ошибка регистрации: {response.status_code}")
                print(f"[ERROR] {response.text}")
                return False
        except Exception as e:
            print(f"[ERROR] Ошибка при регистрации: {e}")
            return False

    def check_dog_assignment(self):
        """Проверка, назначена ли собака этому устройству"""
        try:
            response = requests.get(
                f"{self.api_url}/api/smartdevices/device/{self.device_guid}/dog"
            )

            if response.status_code == 200:
                data = response.json()
                return data.get('dogId')
            else:
                print(f"[ERROR] Ошибка проверки собаки: {response.status_code}")
                return None
        except Exception as e:
            print(f"[ERROR] Ошибка при проверке собаки: {e}")
            return None

    def wait_for_dog_assignment(self, check_interval=5):
        """Ожидание назначения собаки устройству"""
        print("[INFO] Сканирование сервера на наличие собаки...")

        while True:
            dog_id = self.check_dog_assignment()

            if dog_id is not None:
                self.dog_id = dog_id
                print(f"\n{'='*50}")
                print(f"[SUCCESS] Собака найдена!")
                print(f"[INFO] ID собаки: {dog_id}")
                print(f"{'='*50}\n")
                return dog_id
            else:
                print(f"[INFO] Собака ещё не назначена. Ожидание {check_interval} секунд...")
                time.sleep(check_interval)

    def send_telemetry(self, latitude, longitude, battery):
        """Отправка данных на сервер"""
        try:
            # Находим ID устройства в БД по GUID (обычно он возвращается при регистрации)
            # В этом симуляторе мы используем упрощенный подход: 
            # Сначала получаем информацию об устройстве
            response = requests.get(f"{self.api_url}/api/smartdevices/device/{self.device_guid}/dog")
            if response.status_code != 200:
                return False
            
            # В реальном API нам нужен числовой ID устройства для PUT /api/smartdevices/device/{id}/telemetry
            # Но наше API позволяет получить его
            device_info = requests.get(f"{self.api_url}/api/smartdevices").json()
            device = next((d for d in device_info if d['deviceGuid'] == self.device_guid), None)
            
            if not device:
                print("[ERROR] Устройство не найдено в списке")
                return False

            device_id = device['id']
            
            payload = {
                "lastLatitude": latitude,
                "lastLongitude": longitude,
                "batteryLevel": battery
            }
            
            headers = {
                "Content-Type": "application/json"
            }
            if self.token:
                headers["Authorization"] = f"Bearer {self.token}"

            response = requests.put(
                f"{self.api_url}/api/smartdevices/device/{device_id}/telemetry",
                json=payload,
                headers=headers
            )
            
            return response.status_code == 204
        except Exception as e:
            print(f"[ERROR] Ошибка отправки телеметрии: {e}")
            return False

    def run(self):
        """Основной цикл работы устройства"""
        print("="*50)
        print("IoT Device Simulator - MyDogSpace")
        print("="*50)

        # Шаг 1: Регистрация устройства
        if not self.register_device():
            print("[ERROR] Не удалось зарегистрировать устройство. Завершение работы.")
            return

        print()

        # Шаг 2: Ожидание назначения собаки
        self.wait_for_dog_assignment()

        # Шаг 3: Отправка телеметрии
        print("[INFO] Устройство готово к работе! Начинаю отправку данных...")
        
        lat = 49.9935
        lon = 36.2304
        battery = 100

        import random
        while True:
            # Симуляция движения
            lat += (random.uniform(-0.0005, 0.0005))
            lon += (random.uniform(-0.0005, 0.0005))
            battery = max(0, battery - 0.1)

            if self.send_telemetry(lat, lon, int(battery)):
                print(f"[SEND] OK: {lat:.6f}, {lon:.6f} | Бат: {int(battery)}%")
            else:
                print("[SEND] FAILED")

            time.sleep(5)

def main():
    # Параметры подключения
    API_URL = "http://localhost:5000"  # Стандартный порт для локального запуска ASP.NET
    
    # Пытаемся определить порт из config если он есть
    # В данном случае просто дефолт
    
    # Можно указать свой GUID или он будет сгенерирован автоматически
    DEVICE_GUID = "SIM-DOG-001" 

    if len(sys.argv) > 1:
        DEVICE_GUID = sys.argv[1]
        print(f"[INFO] Используется указанный GUID: {DEVICE_GUID}")

    if len(sys.argv) > 2:
        API_URL = sys.argv[2]
        print(f"[INFO] Используется указанный URL API: {API_URL}")

    # Создание и запуск симулятора
    simulator = IoTDeviceSimulator(API_URL, DEVICE_GUID)

    try:
        simulator.run()
    except KeyboardInterrupt:
        print("\n[INFO] Устройство остановлено пользователем.")
    except Exception as e:
        import traceback
        traceback.print_exc()
        print(f"\n[ERROR] Критическая ошибка: {e}")

if __name__ == "__main__":
    main()
